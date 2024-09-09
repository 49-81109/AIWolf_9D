/**
 * SampleWerewolf.java
 * 
 * Copyright (c) 2018 人狼知能プロジェクト
 */
package org.aiwolf.sample.player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.aiwolf.client.lib.Content;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Judge;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.data.Vote;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

/**
 * 人狼役エージェントクラス
 */
public final class SampleWerewolf extends SampleBasePlayer {

	/** 規定人狼数 */
	private int numWolves;

	/** 騙る役職 */
	private Role fakeRole;

	/** カミングアウトする日 */
	private int comingoutDay;

	/** カミングアウト済みか */
	private boolean isCameout;

	/** 襲撃投票先候補 */
	private Agent attackVoteCandidate;

	/** 宣言済み襲撃投票先候補 */
	private Agent declaredAttackVoteCandidate;

	/** 囁きリスト読み込みのヘッド */
	private int whisperListHead;

	/** 囁き用待ち行列 */
	private Deque<Content> whisperQueue = new LinkedList<>();

	/** 偽判定リスト */
	private List<Judge> myFakeJudgeList = new ArrayList<>();

	/** 偽判定マップ */
	private Map<Agent, Judge> myFakeJudgeMap = new HashMap<>();

	/** 未公表偽判定の待ち行列 */
	private Deque<Judge> myFakeJudgeQueue = new LinkedList<>();

	/** FCO宣言状況 */
	private Map<Agent, Role> fakeComingoutMap = new HashMap<>();

	/** 裏切り者リスト */
	private List<Agent> possessedList = new ArrayList<>();

	/** 人狼リスト */
	private List<Agent> werewolves = new ArrayList<>();
	
	/** 妖狐候補リスト */
	private List<Agent> foxCandidates = new ArrayList<>();
	
	/** 妖狐→背徳者候補リスト */
	private Map<Agent, List<Agent>> immoralistCandidates = new HashMap<>();
	
	/** 人外行動候補リスト */
	private List<Agent> SwfCandidates = new ArrayList<>();
	
	/** 人間リスト */
	private List<Agent> humans = new ArrayList<>();

	/** 村人リスト */
	private List<Agent> villagers = new ArrayList<>();

	/** 生存偽人狼 */
	private List<Agent> aliveFakeWolves;

	/** 偽人狼候補 */
	private List<Agent> fakeWolfCandidates = new ArrayList<>();

	/** 偽白のリスト */
	private List<Agent> fakeWhiteList = new ArrayList<>();

	/** 偽黒のリスト */
	private List<Agent> fakeBlackList = new ArrayList<>();

	/** 偽灰のリスト */
	private List<Agent> fakeGrayList = new ArrayList<>();

	/** 襲撃投票理由マップ */
	private AttackVoteReasonMap attackVoteReasonMap = new AttackVoteReasonMap();
	
	/** 妖狐確定死亡盤面において確定してない占い師(自視点は真確定)を襲撃する確率 [確定している場合は100%占い師を襲撃する] */
	private static final int P_AttackSeer_FoxDead = 77;
	
	/** 妖狐確定死亡盤面以外において2日目に確定占い師を襲撃する確率 */
	private static final int P_2dayAttackDisitionSeer_FoxPosAlive = 70;
	
	/** 占い師が2CO以上で、占い師が残り1人しか生存してない場合に残された占い師を襲撃する確率 */
	private static final int P_AttackLeftSeer_FoxPosAlive = 49;
	
	/** 再投票のときに投票を変える確率 */
	private static final int P_RevoteToChange = 21;

	@Override
	public void initialize(GameInfo gameInfo, GameSetting gameSetting) {
		super.initialize(gameInfo, gameSetting);
		myRole = Role.WEREWOLF;

		numWolves = gameSetting.getRoleNumMap().get(Role.WEREWOLF);

		// ランダムに騙る役職を決める
		fakeRole = randomFakeRole();

		// 1～3日目からランダムにカミングアウトする
		comingoutDay = (int) (Math.random() * 3 + 1);

		isCameout = false;
		attackVoteCandidate = null;
		declaredAttackVoteCandidate = null;
		whisperListHead = 0;
		whisperQueue.clear();
		myFakeJudgeList.clear();
		myFakeJudgeMap.clear();
		myFakeJudgeQueue.clear();
		fakeComingoutMap.clear();
		possessedList.clear();
		werewolves = new ArrayList<>(gameInfo.getRoleMap().keySet());
		foxCandidates.clear();
		immoralistCandidates.clear();
		SwfCandidates.clear();
		humans = aliveOthers.stream().filter(a -> !werewolves.contains(a)).collect(Collectors.toList());
		villagers.clear();
		fakeWhiteList.clear();
		fakeBlackList.clear();
		fakeGrayList = new ArrayList<>(aliveOthers);
		attackVoteReasonMap.clear();
	}

	private Role randomFakeRole() {
		return randomSelect(Arrays.asList(Role.VILLAGER, Role.SEER, Role.MEDIUM).stream()
				.filter(r -> currentGameInfo.getExistingRoles().contains(r)).collect(Collectors.toList()));
	}

	@Override
	public void update(GameInfo gameInfo) {
		super.update(gameInfo);

		// GameInfo.whisperListからFCO宣言を抽出
		for (int i = whisperListHead; i < currentGameInfo.getWhisperList().size(); i++) {
			Talk whisper = currentGameInfo.getWhisperList().get(i);
			Agent whisperer = whisper.getAgent();
			if (whisperer == me) {
				continue;
			}
			Content content = new Content(whisper.getText());

			// subjectがUNSPECの場合は発話者に入れ替える
			if (content.getSubject() == Content.UNSPEC) {
				content = replaceSubject(content, whisperer);
			}

			parseWhisper(content);
		}
		whisperListHead = currentGameInfo.getWhisperList().size();

		// 占い結果が嘘の場合，裏切り者候補
		for (Judge j : getDivinationList()) {
			Agent he = j.getAgent();
			Agent target = j.getTarget();
			Species result = j.getResult();
			if (!werewolves.contains(he) && !possessedList.contains(he)
					&& ((humans.contains(target) && result == Species.WEREWOLF) || (werewolves.contains(target) && result == Species.HUMAN))) {
				possessedList.add(he);
				Content heIs = notContent(me, coContent(me, he, Role.WEREWOLF));
				Content hisDayDivination = dayContent(me, j.getDay(), divinedContent(he, target, result));
				Content targetIs;
				if (humans.contains(target)) {
					targetIs = notContent(me, coContent(me, target, Role.WEREWOLF));
				} else {
					targetIs = coContent(me, target, Role.WEREWOLF);
				}
				Content reason = andContent(me, heIs, targetIs, hisDayDivination);
				Estimate estimate = new Estimate(me, he, reason, Role.POSSESSED);
				estimateReasonMap.put(estimate);
				enqueueWhisper(estimate.toContent());
			}
		}

		// 霊媒結果が嘘の場合，裏切り者候補
		for (Judge j : identList) {
			Agent he = j.getAgent();
			Agent target = j.getTarget();
			Species result = j.getResult();
			if (!werewolves.contains(he) && !possessedList.contains(he)
					&& ((humans.contains(target) && result == Species.WEREWOLF) || (werewolves.contains(target) && result == Species.HUMAN))) {
				possessedList.add(he);
				Content heIs = notContent(me, coContent(me, he, Role.WEREWOLF));
				Content hisDayIdent = dayContent(me, j.getDay(), identContent(he, target, result));
				Content targetIs;
				if (humans.contains(target)) {
					targetIs = notContent(me, coContent(me, target, Role.WEREWOLF));
				} else {
					targetIs = coContent(me, target, Role.WEREWOLF);
				}
				Content reason = andContent(me, heIs, targetIs, hisDayIdent);
				Estimate estimate = new Estimate(me, he, reason, Role.POSSESSED);
				estimateReasonMap.put(estimate);
				enqueueWhisper(estimate.toContent());
			}
		}

		villagers = aliveOthers.stream()
				.filter(a -> !werewolves.contains(a) && !possessedList.contains(a)).collect(Collectors.toList());
	}

	private void parseWhisper(Content content) {
		if (estimateReasonMap.put(content)) {
			return; // 推測文と解析
		}
		if (attackVoteReasonMap.put(content)) {
			return; // 襲撃投票宣言と解析
		}
		switch (content.getTopic()) {
		case COMINGOUT: // Declaration of FCO
			fakeComingoutMap.put(content.getSubject(), content.getRole());
			return;
		default:
			break;
		}
	}

	@Override
	public void dayStart() {
		super.dayStart();

		attackVoteCandidate = null;
		declaredAttackVoteCandidate = null;
		whisperListHead = 0;
		
		// 襲撃対象が死亡しなかった場合、その対象は妖狐候補
		if(!currentGameInfo.getLastDeadAgentList().contains(currentGameInfo.getAttackedAgent())) {
			foxCandidates.add(currentGameInfo.getAttackedAgent());
		}
		
		// 妖狐候補視点での背徳者候補の更新
		if(foxCandidates.size() > 0) {
			for(Agent fox : foxCandidates) {
				List<Agent> notImmoralistCandidates = new ArrayList<>();
				if(immoralistCandidates.containsKey(fox)) {
					immoralistCandidates.remove(fox);
				}
				for(Agent a : currentGameInfo.getAgentList()) {
					// 妖狐に対して投票したり投票を促したりしているAgentは非背徳者の可能性の高い候補
					if(getWantExecuteTarget(a).contains(fox)) {
						if(!notImmoralistCandidates.contains(a)) {
							notImmoralistCandidates.add(a);
						}
					}
				}
				immoralistCandidates.put(fox, currentGameInfo.getAgentList().stream().filter(a -> !notImmoralistCandidates.contains(a)).collect(Collectors.toList()));
			}
		}

		if (day == 0) {
			enqueueWhisper(coContent(me, me, fakeRole));
		}
		// 偽の判定
		else {
			if (fakeRole != Role.VILLAGER) {
				Judge judge = getFakeJudge();
				if (judge != null) {
					myFakeJudgeList.add(judge);
					myFakeJudgeMap.put(judge.getTarget(), judge);
					myFakeJudgeQueue.offer(judge);
					if (judge.getResult() == Species.WEREWOLF) {
						fakeBlackList.add(judge.getTarget());
					} else {
						fakeWhiteList.add(judge.getTarget());
					}
					fakeGrayList.remove(judge.getTarget());
				}
			}
		}
	}

	private Judge getFakeJudge() {
		Agent target = null;

		// 占い師騙りの場合
		if (fakeRole == Role.SEER) {
			// 占える対象を選択 (生存者+今日の犠牲者)
			List<Agent> divinedCandidates = currentGameInfo.getAgentList().stream().filter(a -> aliveOthers.contains(a) || currentGameInfo.getLastDeadAgentList().contains(a)).collect(Collectors.toList());
			List<Agent> candidates = divinedCandidates.stream()
					.filter(a -> !myFakeJudgeMap.containsKey(a) && comingoutMap.get(a) != Role.SEER).collect(Collectors.toList());
			// 犠牲者が2人以上いる場合は犠牲者のうちまだ占ってない対象を占い対象に選択
			if(currentGameInfo.getLastDeadAgentList().size() > 1) {
				candidates = currentGameInfo.getAgentList().stream()
						.filter(a -> !myFakeJudgeMap.containsKey(a) && currentGameInfo.getLastDeadAgentList().contains(a)).collect(Collectors.toList());
			}
			if (candidates.isEmpty()) {
				target = randomSelect(divinedCandidates);
			} else {
				target = randomSelect(candidates);
			}
		}
		// 霊媒師騙りの場合
		else if (fakeRole == Role.MEDIUM) {
			target = currentGameInfo.getExecutedAgent();
		}

		if (target != null) {
			Species result = Species.HUMAN;
			// 人間が偽占い対象の場合
			if (humans.contains(target) && currentGameInfo.getLastDeadAgentList().size() <= 1) {
				int nFakeWolves = (int) myFakeJudgeMap.keySet().stream()
						.filter(a -> myFakeJudgeMap.get(a).getResult() == Species.WEREWOLF).count();
				// 偽人狼に余裕があれば
				if (nFakeWolves < numWolves) {
					// 裏切り者，あるいはまだカミングアウトしていないエージェントの場合，判定は五分五分
					if (possessedList.contains(target) || !isCo(target)) {
						if (Math.random() < 0.3) {
							result = Species.WEREWOLF;
						}
					}
					// それ以外は人狼判定
					else {
						result = Species.WEREWOLF;
					}
				}
			}
			return new Judge(day, me, target, result);
		}
		return null;
	}

	@Override
	void chooseVoteCandidate() {
		Content iAm = isCameout ? coContent(me, me, fakeRole) : coContent(me, me, Role.VILLAGER);

		if (fakeRole == Role.SEER) {
			// 生存偽人狼がいれば当然投票（できれば裏切り者は除く）
			aliveFakeWolves = fakeBlackList.stream()
					.filter(a -> isAlive(a) && !possessedList.contains(a)).collect(Collectors.toList());
			if (aliveFakeWolves.isEmpty()) {
				aliveFakeWolves = fakeBlackList.stream()
						.filter(a -> isAlive(a)).collect(Collectors.toList());
			}
			// 既定の投票先が生存偽人狼でない場合投票先を変える
			if (!aliveFakeWolves.isEmpty()) {
				if (!aliveFakeWolves.contains(voteCandidate)) {
					voteCandidate = randomSelect(aliveFakeWolves);
					// CO後なら投票理由を付ける
					if (isCameout) {
						Content myDivination = divinedContent(me, voteCandidate, myFakeJudgeMap.get(voteCandidate).getResult());
						Content reason = dayContent(me, myFakeJudgeMap.get(voteCandidate).getDay(), myDivination);
						voteReasonMap.put(me, voteCandidate, reason);
					}
				}
				return;
			}

			// これ以降は生存偽人狼がいない場合
			fakeWolfCandidates.clear();

			// 自称占い師を人狼か裏切り者と推測する
			for (Agent he : aliveOthers) {
				if (comingoutMap.get(he) == Role.SEER) {
					fakeWolfCandidates.add(he);
					if (isCameout) {
						Content heIs = coContent(he, he, Role.SEER);
						Content reason = andContent(me, iAm, heIs);
						estimateReasonMap.put(new Estimate(me, he, reason, Role.WEREWOLF, Role.FOX, Role.IMMORALIST));
					}
				}
			}

			// 自分の占いと矛盾する自称霊媒師を人狼か裏切り者と推測する
			for (Judge ident : identList) {
				Agent he = ident.getAgent();
				Agent target = ident.getTarget();
				Species result = ident.getResult();
				Content hisIdent = dayContent(me, ident.getDay(), identContent(he, target, result));
				Judge myJudge = myFakeJudgeMap.get(target);
				if ((myJudge != null && result != myJudge.getResult())) {
					if (isAlive(he) && !fakeWolfCandidates.contains(he)) {
						fakeWolfCandidates.add(he);
						if (isCameout) {
							Content myDivination = dayContent(me, myJudge.getDay(), divinedContent(me, myJudge.getTarget(), myJudge.getResult()));
							Content reason = andContent(me, myDivination, hisIdent);
							estimateReasonMap.put(new Estimate(me, he, reason, Role.WEREWOLF, Role.FOX, Role.IMMORALIST));
						}
					}
				}
			}
		} else if (fakeRole == Role.MEDIUM) {
			// 自称霊媒師を人狼か裏切り者と推測する
			for (Agent he : aliveOthers) {
				if (comingoutMap.get(he) == Role.MEDIUM) {
					fakeWolfCandidates.add(he);
					// CO後なら理由を付ける
					if (isCameout) {
						Content heIs = coContent(he, he, Role.MEDIUM);
						Content reason = andContent(me, iAm, heIs);
						estimateReasonMap.put(new Estimate(me, he, reason, Role.WEREWOLF, Role.FOX, Role.IMMORALIST));
					}
				}
			}
			// 自分の霊媒結果と矛盾する自称占い師を人狼か裏切り者と推測する
			for (Judge divination : getDivinationList()) {
				Agent he = divination.getAgent();
				Agent target = divination.getTarget();
				Species result = divination.getResult();
				Content hisDivination = dayContent(me, divination.getDay(), divinedContent(he, target, result));
				Judge myJudge = myFakeJudgeMap.get(target);
				if ((myJudge != null && result != myJudge.getResult())) {
					if (isAlive(he) && !fakeWolfCandidates.contains(he)) {
						fakeWolfCandidates.add(he);
						if (isCameout) {
							Content myIdent = dayContent(me, myJudge.getDay(), identContent(me, myJudge.getTarget(), myJudge.getResult()));
							Content reason = andContent(me, myIdent, hisDivination);
							estimateReasonMap.put(new Estimate(me, he, reason, Role.WEREWOLF, Role.FOX, Role.IMMORALIST));
						}
					}
				}
			}
		}

		// 村人目線での人狼候補決定アルゴリズム
		for (Judge divination : getDivinationList()) {
			// まず占い結果から人狼候補を見つける
			Agent he = divination.getAgent();
			Species result = divination.getResult();
			if (!isAlive(he) || fakeWolfCandidates.contains(he) || result == Species.HUMAN) {
				continue;
			}
			Agent target = divination.getTarget();
			if (target == me) {
				// 自分を人狼と判定した自称占い師は人狼か裏切り者なので投票先候補に追加
				fakeWolfCandidates.add(he);
				Content hisDivination = dayContent(me, divination.getDay(), divinedContent(he, target, result));
				Content reason = andContent(me, coContent(me, me, Role.VILLAGER), hisDivination);
				estimateReasonMap.put(new Estimate(me, he, reason, Role.WEREWOLF, Role.FOX, Role.IMMORALIST));
			} else if (isKilled(target)) {
				// 殺されたエージェントを人狼と判定した自称占い師は人狼か裏切り者なので投票先候補に追加
				fakeWolfCandidates.add(he);
				Content hisDivination = dayContent(me, divination.getDay(), divinedContent(he, target, result));
				Content reason = andContent(me, attackedContent(Content.ANY, target), hisDivination);
				estimateReasonMap.put(new Estimate(me, he, reason, Role.WEREWOLF, Role.FOX, Role.IMMORALIST));
			}
		}

		// できれば仲間は除く
		List<Agent> fakeWolfCandidates0 = fakeWolfCandidates.stream()
				.filter(a -> !werewolves.contains(a)).collect(Collectors.toList());
		if (!fakeWolfCandidates0.isEmpty()) {
			fakeWolfCandidates = fakeWolfCandidates0;
			// できれば裏切り者は除く
			List<Agent> fakeWolfCandidates1 = fakeWolfCandidates.stream()
					.filter(a -> !possessedList.contains(a)).collect(Collectors.toList());
			if (!fakeWolfCandidates1.isEmpty()) {
				fakeWolfCandidates = fakeWolfCandidates1;
			}
		}

		if (!fakeWolfCandidates.isEmpty()) {
			// 見つかった場合
			if (!fakeWolfCandidates.contains(voteCandidate)) {
				// 新しい投票先の場合，推測発言をする
				voteCandidate = randomSelect(fakeWolfCandidates);
				Estimate estimate = estimateReasonMap.getEstimate(me, voteCandidate);
				if (estimate != null) {
					enqueueTalk(estimate.toContent());
					voteReasonMap.put(me, voteCandidate, estimate.getEstimateContent());
				}
			}
		} else {
			// 見つからなかった場合灰からランダム（できれば仲間や裏切り者は除く）
			if (voteCandidate == null || !isAlive(voteCandidate)) {
				List<Agent> fakeGrayList0 = fakeGrayList.stream()
						.filter(a -> isAlive(a) && !werewolves.contains(a)).collect(Collectors.toList());
				if (!fakeGrayList0.isEmpty()) {
					fakeGrayList = fakeGrayList0;
					List<Agent> fakeGrayList1 = fakeGrayList.stream()
							.filter(a -> !possessedList.contains(a)).collect(Collectors.toList());
					if (!fakeGrayList1.isEmpty()) {
						fakeGrayList = fakeGrayList1;
					}
				}
				if (!fakeGrayList.isEmpty()) {
					voteCandidate = randomSelect(fakeGrayList);
				} else {
					voteCandidate = randomSelect(aliveOthers);
				}
			}
		}
	}

	@Override
	void chooseFinalVoteCandidate() {
		if (!isRevote) {
			// 盤面整理ツールに連携
			ArrangeToolLink arrange = getArrangeLink();
			// 全視点での整理
			String[][] every = getBoardArrange(arrange);
			// 自分が人狼視点での整理
			String[][] self = getSelfBoardArrange(arrange, false);
			
			// 妖狐確定死亡盤面では確白には投票をしない
			if(arrange.getTotalState(every).get("max-a-Rf") == 0) {
				List<Agent> candidates = aliveOthers.stream().filter(a -> !arrange.getDisitionNRwList(every).contains(a)).collect(Collectors.toList());
				if (!candidates.isEmpty()) {
					voteCandidate = randomSelect(candidates);
					return;
				}
			}
			
			// 4人盤面では妖狐確定位置がいたらそこに投票、それ以外の場合妖狐が否定されてないプレイヤーから投票
			if(currentGameInfo.getAliveAgentList().size() == 4) {
				if(foxCandidates.size() > 0) {
					voteCandidate = randomSelect(foxCandidates);
					return;
				}
				List<Agent> candidates = aliveOthers.stream().filter(a -> arrange.agentCandidate(self, Role.FOX).contains(a)).collect(Collectors.toList());
				if (!candidates.isEmpty()) {
					voteCandidate = randomSelect(candidates);
					return;
				}
			}
			
			// 偽人狼（候補）も偽灰も見つからなかった場合，初回投票では投票リクエストに応じる
			if ((fakeRole == Role.SEER && aliveFakeWolves.isEmpty()) && fakeWolfCandidates.isEmpty() && fakeGrayList.isEmpty()) {
				List<Agent> candidates = voteRequestCounter.getRequestMap().values().stream()
						.filter(a -> !werewolves.contains(a)).collect(Collectors.toList());
				if (candidates != null && !candidates.isEmpty()) {
					voteCandidate = randomSelect(candidates);
				} else {
					candidates = aliveOthers;
					List<Agent> candidates0 = candidates.stream()
							.filter(a -> !werewolves.contains(a)).collect(Collectors.toList());
					if (!candidates0.isEmpty()) {
						candidates = candidates0;
						List<Agent> candidates1 = candidates.stream()
								.filter(a -> !possessedList.contains(a)).collect(Collectors.toList());
						if (!candidates1.isEmpty()) {
							candidates = candidates1;
						}
					}
					voteCandidate = randomSelect(candidates);
				}
			}
		} else {
			// 再投票の場合は自分以外の前回最多得票に入れる
			if(randP(P_RevoteToChange)) {
				VoteReasonMap vrmap = new VoteReasonMap();
				for (Vote v : currentGameInfo.getLatestVoteList()) {
					vrmap.put(v.getAgent(), v.getTarget(), null);
				}
				List<Agent> candidates = vrmap.getOrderedList();
				candidates.remove(me);
				if (candidates.isEmpty()) {
					voteCandidate = randomSelect(aliveOthers);
				} else {
					voteCandidate = candidates.get(0);
				}
			}
		}
	}

	@Override
	public String talk() {
		// 妖狐確定死亡盤面で残り2縄以上ある場合は占い師を騙らない
		int maxVic = 0;
		for(List<Agent> li : victimAgents) {
			if(li.size() > maxVic) {
				maxVic = li.size();
			}
		}
		if(executedFoxDay != -1 || maxVic > 1) {
			if(currentGameInfo.getAliveAgentList().size() > 4) {
				fakeRole = Role.VILLAGER;
				return super.talk();
			}
		}
		
		if (fakeRole != Role.VILLAGER) {
			if (!isCameout) {
				// 対抗カミングアウトがある場合，今日カミングアウトする
				for (Agent a : humans) {
					if (comingoutMap.get(a) == fakeRole) {
						comingoutDay = day;
					}
				}
				// カミングアウトするタイミングになったらカミングアウト
				if (day >= comingoutDay) {
					isCameout = true;
					enqueueTalk(coContent(me, me, fakeRole));
				}
			}
			// カミングアウトしたらこれまでの偽判定結果をすべて公開
			else {
				List<Content> judges = new ArrayList<>();
				while (!myFakeJudgeQueue.isEmpty()) {
					Judge judge = myFakeJudgeQueue.poll();
					if (fakeRole == Role.SEER) {
						// 占い対象がすでに死亡している場合、黒結果を白結果に変更
						if(!currentGameInfo.getAliveAgentList().contains(judge.getTarget()) && judge.getResult() == Species.WEREWOLF) {
							judges.add(dayContent(me, judge.getDay(),
									divinedContent(me, judge.getTarget(), Species.HUMAN)));
							myFakeJudgeMap.remove(judge.getTarget());
							myFakeJudgeMap.put(judge.getTarget(), new Judge(judge.getDay(), judge.getAgent(), judge.getTarget(), Species.HUMAN));
						}
						else {
							judges.add(dayContent(me, judge.getDay(),
									divinedContent(me, judge.getTarget(), judge.getResult())));
						}
					} else if (fakeRole == Role.MEDIUM) {
						judges.add(dayContent(me, judge.getDay(),
								identContent(me, judge.getTarget(), judge.getResult())));
					}
				}
				if (judges.size() == 1) {
					enqueueTalk(judges.get(0));
					enqueueTalk(judges.get(0).getContentList().get(0));
				} else if (judges.size() > 1) {
					enqueueTalk(andContent(me, judges.toArray(new Content[0])));
					for (Content c : judges) {
						enqueueTalk(c.getContentList().get(0));
					}
				}
			}
		}
		return super.talk();
	}

	/** 襲撃先候補を選ぶ */
	void chooseAttackVoteCandidate() {
		// 盤面整理ツールに連携
		ArrangeToolLink arrange = getArrangeLink();
		// 全視点での整理
		String[][] every = getBoardArrange(arrange);
		// 人狼視点での整理
		String[][] self = getSelfBoardArrange(arrange, false);
		// 自身が主張する村人陣営役職視点での整理
		String[][] pretend = getCOBoardArrange(arrange, me, false);
		// 人外候補リストの更新
		SwfCandidates = addNonVillagerSideCandidates(arrange, every, SwfCandidates);
		
		// 1.妖狐確定死亡時
		if(arrange.getTotalState(every).get("max-a-Rf") == 0) {
			chooseAttackFoxDisitionDead(arrange, every, self, pretend);
			return;
		}
		// 2.夜時点で4人盤面のとき (飽和回避を最優先で行う→妖狐候補を襲撃)
		else if(currentGameInfo.getAliveAgentList().size() == 4) {
			chooseAttackToFox(arrange, every, self, pretend);
			return;
		}
		// 3.夜時点で5人盤面で 自視点で占い師候補が全滅していて 全視点で自身の人狼が確定しているとき (このときも妖狐候補を襲撃)
		else if(currentGameInfo.getAliveAgentList().size() == 5 && arrange.getDisitionRwList(every).contains(me) && toAliveList(arrange.agentCandidate(self, Role.SEER)).size() == 0) {
			/** 自身の人狼が確定している場合、4人盤面になったら村人たちも妖狐ケアができないから人狼に票を入れる可能性が高い
			 *  なので5人盤面にして村人に妖狐を吊る方向に動いてもらって、妖狐を吊って背徳者の道連れが起きることに懸ける
			 *  そのため5人盤面を維持するために妖狐狙いの襲撃を行う (占い師候補が全滅しているので呪殺は起こらない)
			 */
			chooseAttackToFox(arrange, every, self, pretend);
			return;
		}
		// 4.自分が占い師COしていた場合
		else if(comingoutMap.get(me) == Role.SEER) {
			chooseAttackPretendSeer(arrange, every, self, pretend);
			return;
		}
		// 5.自分が潜伏している場合
		else {
			// 占い師COがある場合
			if(isCo(Role.SEER)) {
				// 占い師が1確している場合 (または全視点で占い師は2CO以上だが1人を除いて全員破綻している場合も含む)
				if(arrange.agentCandidate(every, Role.SEER).size() == 1) {
					chooseAttackDisitionSeer(arrange, every, self, pretend);
					return;
				}
				// 占い師が2CO以上の場合
				else {
					chooseAttackMore2CoSeer(arrange, every, self, pretend);
					return;
				}
			}
			// 占い師のCOがない場合
			else {
				chooseAttackGray(arrange, every, self, pretend);
			}
		}
		
		if(attackVoteCandidate == null) {
			attackVoteCandidate = randomSelect(aliveOthers);
		}
		/*
		// カミングアウトした村人陣営は襲撃先候補
		List<Agent> candidates = villagers.stream().filter(a -> isCo(a)).collect(Collectors.toList());
		for (Agent a : candidates) {
			attackVoteReasonMap.put(me, a, coContent(a, a, comingoutMap.get(a)));
		}
		// 候補がいなければ村人陣営から
		if (candidates.isEmpty()) {
			candidates = villagers;
		}
		// 村人陣営がいない場合は裏切り者を襲う
		if (candidates.isEmpty()) {
			candidates = possessedList;
		}
		if (candidates.isEmpty()) {
			attackVoteCandidate = null;
		} else if (!candidates.contains(declaredAttackVoteCandidate)) {
			attackVoteCandidate = randomSelect(candidates);
		}
		//*/
	}

	/** 妖狐確定死亡盤面での襲撃先の決定関数 (襲撃関数優先度:1) */
	private void chooseAttackFoxDisitionDead(ArrangeToolLink arrange, String[][] every, String[][] self, String[][] pretend) {
		// 自分が占い師COしていた場合、自分と真占い師以外は確白なので確白から襲撃
		if(comingoutMap.get(me) == Role.SEER) {
			attackVoteCandidate = randomSelect(villagers.stream().filter(a -> comingoutMap.get(a) != Role.SEER).collect(Collectors.toList()));
			return;
		}
		// 占い師COしてない場合  
		else {
			// 1.全視点で真占い師が確定していてその占い師が生存している場合→その占い師を襲撃
			if(arrange.agentDisition(every, Role.SEER).size() > 0) {
				if(toAliveList(arrange.agentDisition(every, Role.SEER)).size() > 0) {
					attackVoteCandidate = randomSelect(toAliveList(arrange.agentDisition(every, Role.SEER)));
					return;
				}
			}
			// 2.全視点で占い師候補が死亡している場合→確白を優先して襲撃
			if(toAliveList(arrange.agentCandidate(every, Role.SEER)).size() == 0) {
				if(toAliveList(arrange.getDisitionNRwList(every)).size() > 0) {
					attackVoteCandidate = randomSelect(toAliveList(arrange.getDisitionNRwList(every)));
					return;
				}
				else {
					attackVoteCandidate = randomSelect(aliveOthers);
					return;
				}
			}
			// 3-4.全視点では真占い師が確定してなくて占い師COがある場合
			if(isCo(Role.SEER)) {
				// 生存している占い師COリスト(最大1人のはず)
				List<Agent> seerCO = aliveOthers.stream().filter(a -> comingoutMap.get(a) == Role.SEER).collect(Collectors.toList());
				if(seerCO.size() > 0) {
					List<Agent> candidates = aliveOthers;
					boolean isMeDisitionWolf = false;
					for(Agent seer : seerCO) {
						// 占い師視点でのdata
						String[][] seerPosi = getCOBoardArrange(arrange, seer, false);
						if(arrange.getDisitionRwList(seerPosi).size() > 0) {
							// 占い師CO者は真のはずなので人狼確定位置があるなら自分になるはずだが一応条件に入れる
							if(arrange.getDisitionRwList(seerPosi).get(0) == me) {
								candidates = candidates.stream().filter(a -> a != seer).collect(Collectors.toList());
								isMeDisitionWolf = true;
							}
						}
					}
					// 3.その占い師視点で「自分の人狼」が確定している場合→占い師以外を襲撃(確白を優先して襲撃)
					if(isMeDisitionWolf) {
						List<Agent> disitionWhite = candidates.stream().filter(a -> arrange.getDisitionNRwList(every).contains(a)).collect(Collectors.toList());
						// 確白がいる場合確白から襲撃
						if(disitionWhite.size() > 0) {
							attackVoteCandidate = randomSelect(disitionWhite);
							return;
						}
						else {
							attackVoteCandidate = randomSelect(candidates);
							return;
						}
					}
					// 4.そうでない場合→確率P_AttackSeer_FoxDeadで占い師を襲撃し、それ以外の場合は確白を優先して襲撃
					else {
						if(randP(P_AttackSeer_FoxDead)) {
							attackVoteCandidate = randomSelect(seerCO);
							return;
						}
						else {
							candidates = candidates.stream().filter(a -> comingoutMap.get(a) != Role.SEER).collect(Collectors.toList());
							List<Agent> disitionWhite = candidates.stream().filter(a -> arrange.getDisitionNRwList(every).contains(a)).collect(Collectors.toList());
							// 確白がいる場合確白から襲撃
							if(disitionWhite.size() > 0) {
								attackVoteCandidate = randomSelect(disitionWhite);
								return;
							}
							else {
								attackVoteCandidate = randomSelect(candidates);
								return;
							}
						}
					}
				}
			}
			// 5.それ以外(占い師潜伏)→生存者からランダム
			attackVoteCandidate = randomSelect(aliveOthers);
			return;
		}
	}
	
	/** 妖狐狙いの襲撃先の決定関数 (襲撃関数優先度:2) */
	private void chooseAttackToFox(ArrangeToolLink arrange, String[][] every, String[][] self, String[][] pretend) {
		// 妖狐候補が見つかっている場合、その候補を襲撃
		if(foxCandidates.size() > 0) {
			attackVoteCandidate = randomSelect(foxCandidates);
			return;
		}
		// それ以外の場合、自視点での妖狐候補位置から襲撃
		else {
			attackVoteCandidate = randomSelect(arrange.agentCandidate(self, Role.FOX));
			return;
		}
	}
	
	/** 占い師騙りの場合の襲撃先の決定関数 (襲撃関数優先度:3) */
	private void chooseAttackPretendSeer(ArrangeToolLink arrange, String[][] every, String[][] self, String[][] pretend) {
		// 破綻していた場合
		if(arrange.isBankruptcy(pretend)) {
			// 全視点での確白が半数以上いる場合、確白から
			if(arrange.getDisitionNRwList(every).size() * 2 >= currentGameInfo.getAliveAgentList().size()) {
				attackVoteCandidate = randomSelect(arrange.getDisitionNRwList(every));
				return;
			}
			// 確白が半数未満の場合
			else {
				// 占い師が真確定していたら占い師を襲撃
				if(arrange.agentDisition(every, Role.SEER).size() == 1) {
					attackVoteCandidate = arrange.agentDisition(every, Role.SEER).get(0);
				}
				// それ以外ランダム
				attackVoteCandidate = randomSelect(aliveOthers);
				return;
			}
		}
		// そうでない場合
		else {
			// 自分の黒先は襲撃候補から外す
			List<Agent> candidates = aliveOthers.stream().filter(a -> !arrange.getDisitionRwList(pretend).contains(a)).collect(Collectors.toList());
			// 占い師騙り視点で確白が半数以上いる場合、確白から襲撃
			if(arrange.getDisitionNRwList(pretend).size() * 2 >= currentGameInfo.getAliveAgentList().size()) {
				candidates = candidates.stream().filter(a -> arrange.getDisitionNRwList(pretend).contains(a)).collect(Collectors.toList());
				attackVoteCandidate = randomSelect(candidates);
				return;
			}
			// 妖狐候補は外す
			candidates = candidates.stream().filter(a -> !foxCandidates.contains(a)).collect(Collectors.toList());
			// 自視点の生存確定妖狐陣営リスト
			List<Agent> disiSfList = toAliveList(arrange.getDisitionSwfList(self).stream().filter(a -> a != me).collect(Collectors.toList()));
			// 5人で生存確定妖狐陣営のプレイヤーが2人以上いる場合, できるだけ妖狐候補じゃないほうを襲撃
			if(aliveOthers.size() < 5 && disiSfList.size() > 1) {
				if(candidates.stream().filter(a -> disiSfList.contains(a)).collect(Collectors.toList()).size() > 0) {
					attackVoteCandidate = randomSelect(candidates.stream().filter(a -> disiSfList.contains(a)).collect(Collectors.toList()));
					return;
				}
			}
			// 妖狐候補が見つかっている場合、その妖狐基軸での背徳者候補から襲撃
			if(foxCandidates.size() > 0) {
				// 妖狐候補リストからランダムに1人取り出しているが、基本妖狐候補リストには1人しか入らないはず
				Agent foxCandidate = randomSelect(foxCandidates);
				List<Agent> RiCandidateList = immoralistCandidates.get(foxCandidate);
				if(RiCandidateList.size() > 0) {
					attackVoteCandidate = randomSelect(RiCandidateList);
					return;
				}
			}
			// それ以外なら占い師COしていないプレイヤーから襲撃
			attackVoteCandidate = randomSelect(candidates.stream().filter(a -> getCoRole(a) != Role.SEER).collect(Collectors.toList()));
			return;
		}
	}
	
	/** 占い師1確盤面での襲撃先の決定関数 (襲撃関数優先度:4) */
	private void chooseAttackDisitionSeer(ArrangeToolLink arrange, String[][] every, String[][] self, String[][] pretend) {
		// 1確占い師が生存している場合, 確率P_2dayAttackDisitionSeer_FoxPosAliveで2日目は占い師を襲撃(3日目以降は確実に占い師を襲撃)
		if(currentGameInfo.getAliveAgentList().contains(arrange.agentCandidate(every, Role.SEER).get(0))) {
			if(randP(P_2dayAttackDisitionSeer_FoxPosAlive) || currentGameInfo.getAliveAgentList().size() < 7) {
				attackVoteCandidate = arrange.agentCandidate(every, Role.SEER).get(0);
				return;
			}
		}
		// 1確占い師が死亡している場合、または確率P_2dayAttackDisitionSeer_FoxPosAlive以外ではグレー襲撃
		else {
			chooseAttackGray(arrange, every, self, pretend);
			return;
		}
	}
	
	/** グレー位置の襲撃先の決定関数 */
	private void chooseAttackGray(ArrangeToolLink arrange, String[][] every, String[][] self, String[][] pretend) {
		// 自身の人外が確定している場合
		if(arrange.getDisitionSwfList(every).contains(me)) {
			// 全視点での確白が半数以上いる場合、確白から
			if(arrange.getDisitionNRwList(every).size() * 2 >= currentGameInfo.getAliveAgentList().size()) {
				attackVoteCandidate = randomSelect(arrange.getDisitionNRwList(every));
				return;
			}
			// 確白が半数未満の場合はランダム
			else {
				attackVoteCandidate = randomSelect(aliveOthers);
				return;
			}
		}
		// 自身の人外が確定してない場合
		else {
			// 村人視点での確白が半数以上いる場合
			if(arrange.getDisitionNRwList(pretend).size() * 2 >= currentGameInfo.getAliveAgentList().size()) {
				List<Agent> candidates = arrange.getDisitionNRwList(pretend);
				// 妖狐候補が見つかっている場合、その妖狐基軸での確白背徳者候補から
				if(foxCandidates.size() > 0) {
					// 妖狐候補リストからランダムに1人取り出しているが、基本妖狐候補リストには1人しか入らないはず
					Agent foxCandidate = randomSelect(foxCandidates);
					List<Agent> RiCandidateList = immoralistCandidates.get(foxCandidate);
					RiCandidateList = RiCandidateList.stream().filter(a -> candidates.contains(a)).collect(Collectors.toList());
					if(RiCandidateList.size() > 0) {
						attackVoteCandidate = randomSelect(RiCandidateList);
						return;
					}
				}
				// 妖狐候補が見つからなければ確白からランダム
				else {
					// 確白のうち人外行動をとったプレイヤーがいる場合、そこから襲撃
					List<Agent> SfCandidate = toAliveList(candidates.stream().filter(a -> SwfCandidates.contains(a)).collect(Collectors.toList()));
					if(SfCandidate.size() > 0) {
						attackVoteCandidate = randomSelect(SfCandidate);
						return;
					}
					attackVoteCandidate = randomSelect(candidates);
					return;
				}
			}
			// 確白が半数未満の場合
			else {
				// 妖狐候補が見つかっている場合、その妖狐基軸での背徳者候補から襲撃
				if(foxCandidates.size() > 0) {
					// 妖狐候補リストからランダムに1人取り出しているが、基本妖狐候補リストには1人しか入らないはず
					Agent foxCandidate = randomSelect(foxCandidates);
					List<Agent> RiCandidateList = immoralistCandidates.get(foxCandidate);
					if(RiCandidateList.size() > 0) {
						attackVoteCandidate = randomSelect(RiCandidateList);
						return;
					}
				}
				// 妖狐候補が見つからなければランダム
				else {
					// 人外行動をとったプレイヤーがいる場合、そこから襲撃
					List<Agent> SfCandidate = toAliveList(SwfCandidates);
					if(SfCandidate.size() > 0) {
						attackVoteCandidate = randomSelect(SfCandidate);
						// 特に騙り視点でそのプレイヤーが確白だったらそこを優先
						SfCandidate = SfCandidate.stream().filter(a -> arrange.getDisitionNRwList(pretend).contains(a)).collect(Collectors.toList());
						if(SfCandidate.size() > 0) {
							attackVoteCandidate = randomSelect(SfCandidate);
						}
						return;
					}
					attackVoteCandidate = randomSelect(aliveOthers);
					return;
				}
			}
		}
	}
	
	/** 自分が潜伏で占い師が2CO以上のときの襲撃先の決定関数 (襲撃関数優先度:5) */
	private void chooseAttackMore2CoSeer(ArrangeToolLink arrange, String[][] every, String[][] self, String[][] pretend) {
		// 1.占い師COしているプレイヤーが2人以上生存時 (破綻除く)
		if(toAliveList(arrange.agentCandidate(every, Role.SEER)).size() > 1) {
			// 自視点で真の可能性のある占い師から襲撃
			List<Agent> possibleSeer = aliveOthers.stream().filter(a -> getCoRole(a) == Role.SEER && arrange.agentCandidate(self, Role.SEER).contains(a)).collect(Collectors.toList());
			if(possibleSeer.size() > 0) {
				attackVoteCandidate = randomSelect(possibleSeer);
				return;
			}
			// 自視点で真の可能性のある占い師がいない場合、偽占い師からランダムに襲撃
			else {
				attackVoteCandidate = randomSelect(arrange.agentCandidate(every, Role.SEER));
				return;
			}
		}
		// 2.占い師COしているプレイヤーが1人のみ生存時 (破綻除く)
		else if(toAliveList(arrange.agentCandidate(every, Role.SEER)).size() == 1) {
			// 確率P_AttackLeftSeer_FoxPosAliveで残された占い師を襲撃
			if(randP(P_AttackLeftSeer_FoxPosAlive)) {
				attackVoteCandidate = randomSelect(arrange.agentCandidate(every, Role.SEER));
				return;
			}
			// それ以外はグレー襲撃
			else {
				chooseAttackGray(arrange, every, self, pretend);
				return;
			}
		}
		// 3.占い師CO全滅時
		else {
			chooseAttackGray(arrange, every, self, pretend);
			return;
		}
	}
	
	@Override
	public Agent attack() {
		chooseAttackVoteCandidate();
		return attackVoteCandidate;
	}

	@Override
	public String whisper() {
		if (day == 0) {
			// 騙る役職が重複した場合，選び直す
			if (fakeRole != Role.VILLAGER) {
				for (Agent a : fakeComingoutMap.keySet()) {
					if (fakeComingoutMap.get(a) == fakeRole) {
						Role newFakeRole = randomFakeRole();
						if (newFakeRole != fakeRole) {
							fakeRole = newFakeRole;
							enqueueWhisper(coContent(me, me, fakeRole));
						}
						break;
					}
				}
			}
		} else {
			chooseAttackVoteCandidate();
			if (attackVoteCandidate != null && attackVoteCandidate != declaredAttackVoteCandidate) {
				Content reason = attackVoteReasonMap.getReason(me, attackVoteCandidate);
				if (reason != null) {
					enqueueWhisper(becauseContent(me, reason, attackContent(me, attackVoteCandidate)));
				}
				enqueueWhisper(attackContent(me, attackVoteCandidate));
				declaredAttackVoteCandidate = attackVoteCandidate;
			}
		}
		return dequeueWhisper();
	}

	void enqueueWhisper(Content content) {
		if (content.getSubject() == Content.UNSPEC) {
			whisperQueue.offer(replaceSubject(content, me));
		} else {
			whisperQueue.offer(content);
		}
	}

	String dequeueWhisper() {
		if (whisperQueue.isEmpty()) {
			return Talk.SKIP;
		}
		Content content = whisperQueue.poll();
		if (content.getSubject() == me) {
			return Content.stripSubject(content.getText());
		}
		return content.getText();
	}

	@Override
	public Agent divine() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Agent guard() {
		throw new UnsupportedOperationException();
	}

}
