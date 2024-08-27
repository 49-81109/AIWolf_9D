/**
 * SampleImmoralistTmp.java
 * 
 * Copyright (c) 2018 人狼知能プロジェクト
 */
package org.aiwolf.sample.player;

import java.util.ArrayList;
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
import org.aiwolf.common.data.Vote;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

/**
 * 背徳者(仮)役エージェントクラス
 * 
 * @author otsuki
 */
public final class SampleImmoralistTmp extends SampleBasePlayer {

	/** 人狼候補リスト */
	private List<Agent> wolfCandidates = new ArrayList<>();

	/** 妖狐リスト */
	private List<Agent> foxes = new ArrayList<>();
	
	/** 騙る役職 */
	private Role fakeRole;

	/** カミングアウトする日 */
	private int comingoutDay;

	/** カミングアウト済みか */
	private boolean isCameout;
	
	/** 偽判定リスト (潜伏時は占い対象だけ決めておく、COして結果公開するときに白か黒かを考える) */
	private List<Agent> myFakeDivineTargetList = new ArrayList<>();

	/** 未公表偽判定の待ち行列 */
	private Deque<Agent> myFakeDivineTargetQueue = new LinkedList<>();
	
	/** 未公表偽判定の日にちの待ち行列 */
	private Deque<Integer> myFakeDivinedDayQueue = new LinkedList<>();

	/** 偽白のリスト */
	private List<Agent> fakeWhiteList = new ArrayList<>();

	/** 偽黒のリスト */
	private List<Agent> fakeBlackList = new ArrayList<>();
	
	
	/** 非背徳者の候補リスト(背徳者の可能性が非常に低いAgent, 妖狐は除く) */
	private List<Agent> notImmoralistCandidates = new ArrayList<>();
	
	/** 非背徳者候補に投票をする確率 */
	private static final int P_VoteNotRiCandidate = 77;
	
	/** 占い師を騙る確率 */
	private static final int P_PretendSeer = 49;
	
	/** 人外候補リスト */
	private List<Agent> SwfCandidates = new ArrayList<>();
	
	@Override
	public void initialize(GameInfo gameInfo, GameSetting gameSetting) {
		super.initialize(gameInfo, gameSetting);
		myRole = Role.IMMORALIST;
		SwfCandidates.clear();
		List<Agent> allyList = new ArrayList<>(gameInfo.getRoleMap().keySet());
		// 妖狐のリストを格納
		foxes = allyList.stream().filter(a -> a != me).collect(Collectors.toList());
		for(Agent a : foxes) {
			System.out.println("[" + a.getAgentIdx() + "]" + a.getName() + " is the fox -> [" + me.getAgentIdx() + "]" + me.getName());
		}
		// 占い師を騙るかどうか
		if(randP(P_PretendSeer)) {
			fakeRole = Role.SEER;
		}
		else {
			fakeRole = Role.VILLAGER;
		}
		// 1～3日目からランダムにカミングアウトする
		comingoutDay = (int) (Math.random() * 3 + 1);
		isCameout = false;
		myFakeDivineTargetList.clear();
		myFakeDivineTargetQueue.clear();
		myFakeDivinedDayQueue.clear();
		fakeWhiteList.clear();
		fakeBlackList.clear();
	}
	
	@Override
	public void dayStart() {
		super.dayStart();
		if(day > 0) {
			Agent divineTarget = chooseDivineTarget();
			if (fakeRole != Role.VILLAGER) {
				myFakeDivineTargetList.add(divineTarget);
				myFakeDivineTargetQueue.offer(divineTarget);
				myFakeDivinedDayQueue.offer(day);
			}
		}
	}
	
	private Agent chooseDivineTarget() {
		Agent target = null;
		// 占い師騙りの場合
		if (fakeRole == Role.SEER) {
			// 占える対象を選択 (生存者+今日の犠牲者)
			List<Agent> divinedCandidates = currentGameInfo.getAgentList().stream().filter(a -> aliveOthers.contains(a) || currentGameInfo.getLastDeadAgentList().contains(a)).collect(Collectors.toList());
			List<Agent> candidates = divinedCandidates.stream()
					.filter(a -> !myFakeDivineTargetList.contains(a) && comingoutMap.get(a) != Role.SEER).collect(Collectors.toList());
			if (candidates.isEmpty()) {
				target = randomSelect(divinedCandidates);
			} else {
				target = randomSelect(candidates);
			}
		}
		return target;
	}
	
	@Override
	void chooseVoteCandidate() {
		wolfCandidates.clear();
		notImmoralistCandidates.clear();
		
		for(Agent a : currentGameInfo.getAgentList()) {
			for(Agent fox : foxes) {
				// 妖狐に対して投票したり投票を促したりしている非妖狐Agentは非背徳者の可能性の高い候補
				if(getWantExecuteTarget(a).contains(fox) && !foxes.contains(a)) {
					if(!notImmoralistCandidates.contains(a)) {
						notImmoralistCandidates.add(a);
					}
				}
			}
		}

		// 村人目線での人狼候補決定アルゴリズム
		for (Judge divination : getDivinationList()) {
			// まず占い結果から人狼候補を見つける
			Agent he = divination.getAgent();
			Species result = divination.getResult();
			if (!isAlive(he) || wolfCandidates.contains(he) || result == Species.HUMAN) {
				continue;
			}
			Agent target = divination.getTarget();
			if (target == me && !foxes.contains(he)) {
				// 自分を人狼と判定した自称占い師は人狼か裏切り者なので投票先候補に追加
				wolfCandidates.add(he);
				Content hisDivination = dayContent(me, divination.getDay(), divinedContent(he, target, result));
				Content reason = andContent(me, coContent(me, me, Role.VILLAGER), hisDivination);
				estimateReasonMap.put(new Estimate(me, he, reason, Role.WEREWOLF, Role.FOX, Role.IMMORALIST));
			} else if (isKilled(target) && !foxes.contains(he)) {
				// 殺されたエージェントを人狼と判定した自称占い師は人狼か裏切り者なので投票先候補に追加
				wolfCandidates.add(he);
				Content hisDivination = dayContent(me, divination.getDay(), divinedContent(he, target, result));
				Content reason = andContent(me, attackedContent(Content.ANY, target), hisDivination);
				estimateReasonMap.put(new Estimate(me, he, reason, Role.WEREWOLF, Role.FOX, Role.IMMORALIST));
			}
		}
		wolfCandidates = wolfCandidates.stream().filter(a -> !foxes.contains(a)).collect(Collectors.toList());

		if (!wolfCandidates.isEmpty()) {
			// 見つかった場合
			if (!wolfCandidates.contains(voteCandidate)) {
				// 新しい投票先の場合，推測発言をする
				voteCandidate = randomSelect(wolfCandidates);
				Estimate estimate = estimateReasonMap.getEstimate(me, voteCandidate);
				if (estimate != null) {
					enqueueTalk(estimate.toContent());
					voteReasonMap.put(me, voteCandidate, estimate.getEstimateContent());
				}
			}
		} else {
			// 見つからなかった場合ランダム
			if (voteCandidate == null || !isAlive(voteCandidate)) {
				// 妖狐以外から投票先を決める
				List<Agent> aliveEnemies = aliveOthers.stream().filter(a -> !foxes.contains(a)).collect(Collectors.toList());
				
				// 非背徳者候補の生存者がいる場合は確率P_VoteNotRiCandidateでその候補の中から投票
				List<Agent> aliveEnemies2 = aliveEnemies.stream().filter(a -> notImmoralistCandidates.contains(a)).collect(Collectors.toList());
				if(aliveEnemies2.size() > 0 && randP(P_VoteNotRiCandidate)) {
					voteCandidate = randomSelect(aliveEnemies2);
				}
				else {
					voteCandidate = randomSelect(aliveEnemies);
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
			// 背徳者視点での整理
			String[][] self = getSelfBoardArrange(arrange, false);
			// 自身が主張する村人陣営役職視点での整理
//			String[][] pretend = getCOBoardArrange(arrange, me, false);
			// 人外候補リストの更新
			SwfCandidates = addNonVillagerSideCandidates(arrange, self, SwfCandidates);
			
			
			// 人狼候補が見つけられなかった場合，初回投票では投票リクエストに応じる
			if (wolfCandidates.isEmpty()) {
				List<Agent> voteReqEnemies = voteRequestCounter.getRequestMap().values().stream().filter(a -> a != me && !foxes.contains(a)).collect(Collectors.toList());
				voteCandidate = randomSelect(voteReqEnemies);
				List<Agent> voteReqEnemies2 = voteRequestCounter.getRequestMap().values().stream().filter(a -> a != me && !foxes.contains(a) && notImmoralistCandidates.contains(a)).collect(Collectors.toList());
				if(voteReqEnemies2.size() > 0 && randP(P_VoteNotRiCandidate)) {
					voteCandidate = randomSelect(voteReqEnemies2);
				}
				if (voteCandidate == null || !isAlive(voteCandidate)) {
					List<Agent> aliveEnemies = aliveOthers.stream().filter(a -> !foxes.contains(a)).collect(Collectors.toList());
					// 吊り縄が3のときは確定村人陣営を投票候補から外す
					if(arrange.getTotalState(self).get("count-expelled") == 3) {
						aliveEnemies = aliveEnemies.stream().filter(a -> !arrange.getDisitionSvList(every).contains(a)).collect(Collectors.toList());
					}
					// 非背徳者候補の生存者がいる場合は確率P_VoteNotRiCandidateでその候補の中から投票
					List<Agent> aliveEnemies2 = aliveEnemies.stream().filter(a -> notImmoralistCandidates.contains(a)).collect(Collectors.toList());
					if(aliveEnemies2.size() > 0 && randP(P_VoteNotRiCandidate)) {
						voteCandidate = randomSelect(aliveEnemies2);
					}
					else {
						voteCandidate = randomSelect(aliveEnemies);
					}
				}
			}
		} else {
			// 再投票の場合は自分と妖狐以外の前回最多得票に入れる
			VoteReasonMap vrmap = new VoteReasonMap();
			for (Vote v : currentGameInfo.getLatestVoteList()) {
				vrmap.put(v.getAgent(), v.getTarget(), null);
			}
			List<Agent> candidates = vrmap.getOrderedList();
			candidates.remove(me);
			for(Agent fox : foxes) {
				candidates.remove(fox);
			}
			
			if (candidates.isEmpty()) {
				List<Agent> aliveEnemies = aliveOthers.stream().filter(a -> !foxes.contains(a)).collect(Collectors.toList());
				// 非背徳者候補の生存者がいる場合は確率P_VoteNotRiCandidateでその候補の中から投票
				List<Agent> aliveEnemies2 = aliveEnemies.stream().filter(a -> notImmoralistCandidates.contains(a)).collect(Collectors.toList());
				if(aliveEnemies2.size() > 0 && randP(P_VoteNotRiCandidate)) {
					voteCandidate = randomSelect(aliveEnemies2);
				}
				else {
					voteCandidate = randomSelect(aliveEnemies);
				}
			} else {
				voteCandidate = candidates.get(0);
			}
		}
	}
	
	@Override
	public Agent vote() {
		chooseFinalVoteCandidate();
		isRevote = true;
		if(foxes.contains(voteCandidate)) {
			System.out.println("*********");
			List<Agent> aliveEnemies = aliveOthers.stream().filter(a -> !foxes.contains(a)).collect(Collectors.toList());
			voteCandidate = randomSelect(aliveEnemies);
		}
		arrangeTool();
		return voteCandidate;
	}
	
	/** 背徳者視点の整理実行 */
	@Override
	public String[][] getSelfBoardArrange(ArrangeToolLink arrange, boolean isPrint) {
		if(currentDataSelf != null && currentArrangeToolLink == arrange) {
			return currentDataSelf;
		}
		String[][] data = arrange.copyData(arrange.executeArrangement(me, myRole));
		for(Agent fox : foxes) {
			data = getBoardArrange(arrange, data, fox, Role.FOX);
		}
		currentDataSelf = data;
		if(isPrint) {
			System.out.println("[" + me.getAgentIdx() + "]" + me.getName() + " → " + myRole + "視点");
		}
		return data;
	}

	@Override
	public String talk() {
		pretendSeerCO();
		return super.talk();
	}
	
	/** 占い師騙り */
	private void pretendSeerCO() {
		if(fakeRole == Role.SEER) {
			if (!isCameout) {
				// 対抗カミングアウトがある場合，今日カミングアウトする
				for (Agent a : aliveOthers) {
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
			// カミングアウトしたらこれまでの偽判定結果をすべて公開 (今のところ白結果しか出さない)
			else {
				List<Content> judges = new ArrayList<>();
				while (!myFakeDivineTargetQueue.isEmpty()) {
					Species divined = Species.HUMAN;
					// 4人盤面ですべて白結果を出すと妖狐が人狼位置になってしまう場合には黒を出す
					if(currentGameInfo.getAliveAgentList().size() == 4) {
						List<Agent> wolfCandidate = aliveOthers.stream().filter(a -> !myFakeDivineTargetList.contains(a)).collect(Collectors.toList());
						if(wolfCandidate.size() == 1) {
							if(foxes.contains(wolfCandidate.get(0))) {
								divined = Species.WEREWOLF;
							}
						}
					}
					// 占い結果の生成
					Judge judge = new Judge(myFakeDivinedDayQueue.poll(), me, myFakeDivineTargetQueue.poll(), divined);
					judges.add(dayContent(me, judge.getDay(), divinedContent(me, judge.getTarget(), judge.getResult())));
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
	}
	
	@Override
	public String whisper() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Agent attack() {
		throw new UnsupportedOperationException();
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
