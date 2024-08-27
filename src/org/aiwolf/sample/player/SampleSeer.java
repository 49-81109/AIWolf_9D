/**
 * SampleSeer.java
 * 
 * Copyright (c) 2018 人狼知能プロジェクト
 */
package org.aiwolf.sample.player;

import java.util.ArrayList;
import java.util.HashMap;
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
//import org.aiwolf.sample.player.ArrangeToolLink;

/**
 * 占い師役エージェントクラス
 */
public final class SampleSeer extends SampleBasePlayer {

	/** COする日 */
	private int comingoutDay;

	/** CO済みならtrue */
	private boolean isCameout;

	/** 自分の占い結果の時系列 */
	private List<Judge> myDivinationList = new ArrayList<>();

	/** 自分の占い済みエージェントと判定のマップ */
	private Map<Agent, Judge> myDivinationMap = new HashMap<>();

	/** 生存人狼 */
	private List<Agent> aliveWolves;

	/** 人狼候補 */
	private List<Agent> wolfCandidates = new ArrayList<>();

	/** 白リスト */
	private List<Agent> whiteList = new ArrayList<>();

	/** 黒リスト */
	private List<Agent> blackList = new ArrayList<>();

	/** 灰リスト */
	private List<Agent> grayList = new ArrayList<>();
	
	/** 呪殺確定リスト */
	private List<Agent> cursedList = new ArrayList<>();
	
	/** 占い予告リスト(ゾーン) */
	private List<Agent> zone = new ArrayList<>();

	/** 宣言済みの裏切り者 */
	private Agent declaredPossessed;
	
	/** 人外候補リスト */
	private List<Agent> SwfCandidates = new ArrayList<>();
	
	/** 2日目に初日追放者と相互投票しているAgentを占う確率 */
	private static final int P_Executed_CrossVote = 56;
	
	/** 2日目に初日追放者に投票しているAgentを占う確率 */
	private static final int P_VoteToExecuted = 77;

	@Override
	public void initialize(GameInfo gameInfo, GameSetting gameSetting) {
		super.initialize(gameInfo, gameSetting);
		myRole = Role.SEER;
		SwfCandidates.clear();
		comingoutDay = (int) (Math.random() * 3 + 1);
		isCameout = false;
		myDivinationList.clear();
		myDivinationMap.clear();
		wolfCandidates.clear();
		whiteList.clear();
		blackList.clear();
		grayList = new ArrayList<>(aliveOthers);
		cursedList.clear();
		declaredPossessed = null;
	}

	@Override
	public void dayStart() {
		super.dayStart();
		zone.clear();
		// 占い結果を登録し，白黒に振り分ける
		Judge divination = currentGameInfo.getDivineResult();
		if (divination != null) {
			Agent divined = divination.getTarget();
			myDivinationList.add(divination);
			grayList.remove(divined);
			if (divination.getResult() == Species.HUMAN) {
				whiteList.add(divined);
				// 2犠牲者以上いた場合は呪殺確定
				if(currentGameInfo.getLastDeadAgentList().size() > 1) {
					cursedList.add(divined);
				}
			} else {
				blackList.add(divined);
			}
			myDivinationMap.put(divined, divination);
		}
	}

	@Override
	void chooseVoteCandidate() {
		Content iAm = isCameout ? coContent(me, me, Role.SEER) : coContent(me, me, Role.VILLAGER);
		voteCandidateWithArrangeTool();
		


		// 偽占い師は人狼か妖狐か背徳者
		for (Agent he : aliveOthers) {
			if (comingoutMap.get(he) == Role.SEER) {
				wolfCandidates.add(he);
				// CO後なら推定理由をつける
				if (isCameout) {
					Content heIs = coContent(he, he, Role.SEER);
					Content reason = andContent(me, iAm, heIs);
					// 生存白先の対抗占いは背徳者確定
					if(whiteList.contains(he)) {
						reason = andContent(me, iAm, heIs, divinedContent(me, he, Species.HUMAN));
						estimateReasonMap.put(new Estimate(me, he, reason, Role.IMMORALIST));
						enqueue1Talk(becauseContent(me, reason, declaredContent(me, he, Role.IMMORALIST)));
						List<Agent> foxCandidates = aliveOthers.stream().filter(a -> !getWantExecuteTarget(he).contains(a) && !whiteList.contains(a)).collect(Collectors.toList());
						if(foxCandidates.size() > 0 && foxCandidates.size() < 4) {
							List<Content> notVote = new ArrayList<>();
							List<Content> foxCand = new ArrayList<>();
							for(Agent c : foxCandidates) {
								notVote.add(notContent(me, votedContent(he, c)));
								foxCand.add(estimateContent(me, c, Role.FOX));
							}
							Content notVoteReason = andContent(me, declaredContent(me, he, Role.IMMORALIST), andContent(me, notVote));
							enqueue1Talk(becauseContent(me, notVoteReason, orContent(me, foxCand)));
						}
					}
					else if(blackList.size() > 0) {
						// 対抗占い以外で人狼が見つかっている場合、対抗占いは妖狐か背徳者
						if(blackList.get(0) != he) {
							reason = andContent(me, iAm, heIs, divinedContent(me, blackList.get(0), Species.WEREWOLF));
							estimateReasonMap.put(new Estimate(me, he, reason, Role.FOX, Role.IMMORALIST));
							enqueue1Talk(becauseContent(me, reason, andContent(me, estimateContent(me, he, Role.FOX), estimateContent(me, he, Role.IMMORALIST))));
							List<Agent> foxCandidates = aliveOthers.stream().filter(a -> !getWantExecuteTarget(he).contains(a) && !whiteList.contains(a)).collect(Collectors.toList());
							if(foxCandidates.size() > 0 && foxCandidates.size() < 4) {
								List<Content> notVote = new ArrayList<>();
								List<Content> foxCand = new ArrayList<>();
								for(Agent c : foxCandidates) {
									notVote.add(notContent(me, votedContent(he, c)));
									foxCand.add(estimateContent(me, c, Role.FOX));
								}
								Content notVoteReason = andContent(me, notVote);
								enqueue1Talk(ifContent(me, estimateContent(me, he, Role.IMMORALIST), becauseContent(me, notVoteReason, orContent(me, foxCand))));
							}
						}
						else {
							estimateReasonMap.put(new Estimate(me, he, reason, Role.WEREWOLF));
						}
					}
					else {
						estimateReasonMap.put(new Estimate(me, he, reason, Role.WEREWOLF, Role.FOX, Role.IMMORALIST));		
					}

				}
			}
		}

		// 生存人狼がいれば当然投票
		aliveWolves = blackList.stream().filter(a -> isAlive(a)).collect(Collectors.toList());
		// 既定の投票先が生存人狼でない場合投票先を変える
		if (!aliveWolves.isEmpty()) {
			if (!aliveWolves.contains(voteCandidate)) {
				voteCandidate = randomSelect(aliveWolves);
				// CO後なら投票理由を付ける
				if (isCameout) {
					Content myDivination = divinedContent(me, voteCandidate, myDivinationMap.get(voteCandidate).getResult());
					Content reason = dayContent(me, myDivinationMap.get(voteCandidate).getDay(), myDivination);
					voteReasonMap.put(me, voteCandidate, reason);
				}
			}
			return;
		}

		// これ以降は生存人狼がいない場合
		wolfCandidates.clear();
		
		// 自分の判定と矛盾する偽霊媒師は人狼か裏切り者 ←使わない
		for (Judge ident : identList) {
			Agent he = ident.getAgent();
			Agent target = ident.getTarget();
			Species result = ident.getResult();
			Content hisIdent = dayContent(me, ident.getDay(), identContent(he, target, result));
			Judge myJudge = myDivinationMap.get(target);
			if ((myJudge != null && result != myJudge.getResult())) {
				if (isAlive(he) && !wolfCandidates.contains(he)) {
					wolfCandidates.add(he);
					// CO後なら推定理由をつける
					if (isCameout) {
						Content myDivination = dayContent(me, myJudge.getDay(), divinedContent(me, myJudge.getTarget(), myJudge.getResult()));
						Content reason = andContent(me, myDivination, hisIdent);
						estimateReasonMap.put(new Estimate(me, he, reason, Role.WEREWOLF, Role.FOX, Role.IMMORALIST));
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
			if (target == me) {
				// 自分を人狼と判定した自称占い師は人狼か裏切り者なので投票先候補に追加
				wolfCandidates.add(he);
				Content hisDivination = dayContent(me, divination.getDay(), divinedContent(he, target, result));
				Content reason = andContent(me, coContent(me, me, Role.VILLAGER), hisDivination);
				estimateReasonMap.put(new Estimate(me, he, reason, Role.WEREWOLF, Role.FOX, Role.IMMORALIST));
			} else if (isKilled(target)) {
				// 殺されたエージェントを人狼と判定した自称占い師は人狼か裏切り者なので投票先候補に追加
				wolfCandidates.add(he);
				Content hisDivination = dayContent(me, divination.getDay(), divinedContent(he, target, result));
				Content reason = andContent(me, attackedContent(Content.ANY, target), hisDivination);
				estimateReasonMap.put(new Estimate(me, he, reason, Role.WEREWOLF, Role.FOX, Role.IMMORALIST));
			}
		}

		// 裏切り者関連アルゴリズム
		List<Agent> possessedList = new ArrayList<>();
		for (Agent he : wolfCandidates) {
			// 人狼候補なのに人間⇒裏切り者
			if (whiteList.contains(he)) {
				possessedList.add(he);
				// CO後なら推定理由をつける
				if (isCameout) {
					Content heIs = dayContent(me, myDivinationMap.get(he).getDay(), divinedContent(me, he, Species.HUMAN));
					// 既存の推測理由があれば推測役職を裏切り者にする
					Estimate estimate = estimateReasonMap.getEstimate(me, he);
					if (estimate != null) {
						estimate.resetRole(Role.IMMORALIST);
						estimate.addReason(heIs);
					}
				}
			}
		}
		if (!possessedList.isEmpty()) {
			// 裏切り者を人狼候補から除く
			wolfCandidates.removeAll(possessedList);
			// 裏切り者新発見の場合
			if (declaredPossessed == null || !possessedList.contains(declaredPossessed)) {
				declaredPossessed = randomSelect(possessedList);
				// CO後なら理由を付けてESTIMATE
				if (isCameout) {
					Estimate estimate = estimateReasonMap.getEstimate(me, declaredPossessed);
					if (estimate != null) {
						enqueueTalk(estimateReasonMap.getEstimate(me, declaredPossessed).toContent());
					}
				}
			}
		}

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
			// 見つからなかった場合
			if (voteCandidate == null || !isAlive(voteCandidate)) {
				if (!grayList.isEmpty()) {
					// 灰がいたら灰からランダム
					voteCandidate = randomSelect(grayList);
				} else {
					voteCandidate = randomSelect(aliveOthers);
				}
			}
		}
	}

	void voteCandidateWithArrangeTool() {
		if(isAllSeerTalkResult() && !(!isCo(me) && currentGameInfo.getLastDeadAgentList().size() > 1)) {
			// 盤面整理ツールに連携
			ArrangeToolLink arrange = getArrangeLink();
			// 全視点での整理
			String[][] every = getBoardArrange(arrange);
			// 自分が占い師視点での整理
			String[][] self = getSelfBoardArrange(arrange, false);
			// 人外候補リストの更新
			SwfCandidates = addNonVillagerSideCandidates(arrange, self, SwfCandidates);
			
			if(arrange.getTotalState(every).get("max-a-Rf") == 0) {

			}
			// 確定事項
			if(arrange.agentDisition(self, Role.VILLAGER).size() > 0) {
				for(Agent villager : arrange.agentDisition(self, Role.VILLAGER)) {
					enqueue1Talk(declaredContent(me, villager, Role.VILLAGER));
				}
			}
			if(arrange.agentDisition(self, Role.WEREWOLF).size() > 0) {
				for(Agent werewolf : arrange.agentDisition(self, Role.WEREWOLF)) {
					enqueue1Talk(declaredContent(me, werewolf, Role.WEREWOLF));
				}
			}
			if(arrange.agentDisition(self, Role.FOX).size() > 0) {
				for(Agent fox : arrange.agentDisition(self, Role.FOX)) {
					enqueue1Talk(declaredContent(me, fox, Role.FOX));
				}
			}
			
			if(arrange.agentDisition(self, Role.IMMORALIST).size() > 0) {
				for(Agent immoralist : arrange.agentDisition(self, Role.IMMORALIST)) {
					// 白先が対抗の場合の理由つけ
					if(getCoRole(immoralist) == Role.SEER && whiteList.contains(immoralist)) {
						enqueue1Talk(becauseContent(me, andContent(me, coContent(immoralist, immoralist, Role.SEER), divinedContent(me, immoralist, Species.HUMAN)), declaredContent(me, immoralist, Role.IMMORALIST)));
					}
					// 対抗が犠牲になったときその日に対抗を占ってない場合の理由つけ
					if(getCoRole(immoralist) == Role.SEER && killedAgents.contains(immoralist)) {
						int vicDay = 0;
						for(int i = 0; i < victimAgents.size(); i++) {
							if(victimAgents.get(i).contains(immoralist)) {
								vicDay = i + 1;
								break;
							}
						}
//						System.out.println(">>>> " + vicDay);
						Judge vicDivination = null;
						for(Agent div : myDivinationMap.keySet()) {
							if(myDivinationMap.get(div).getDay() == vicDay) {
								vicDivination = myDivinationMap.get(div);
							}
						}
						if(vicDivination != null) {
//							System.out.println(">>> " + vicDivination.getTarget());
							if(vicDivination.getTarget() != immoralist) {
								Content notfox = andContent(me, dayContent(me, vicDay, attackedContent(immoralist)), notContent(me, dayContent(me, vicDay, divinationContent(me, immoralist))));
								enqueue1Talk(becauseContent(me, andContent(me, coContent(immoralist, immoralist, Role.SEER), notfox), declaredContent(me, immoralist, Role.IMMORALIST)));
							}
						}
					}
					enqueue1Talk(declaredContent(me, immoralist, Role.IMMORALIST));
				}
			}	
		}
	}
	
	@Override
	void chooseFinalVoteCandidate() {
		if (!isRevote) {
			// 盤面整理ツールと連携 (うまくいかなかった場合はfalseが返される)
			if(chooseVoteWithArrangeTool(false)) {
				return;
			}
			
			// 人狼（候補）が見つけられなかった場合，初回投票では投票リクエストに応じる
			if (aliveWolves.isEmpty() && wolfCandidates.isEmpty()) {
				voteCandidate = randomSelect(voteRequestCounter.getRequestMap().values().stream()
						.filter(a -> a != me).collect(Collectors.toList()));
				if (voteCandidate == null || !isAlive(voteCandidate)) {
					voteCandidate = randomSelect(aliveOthers);
				}
			}
		} else {
			// 再投票の場合は自分以外の前回最多得票に入れる
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

	private boolean chooseVoteWithArrangeTool(boolean isTalk) {
		// 盤面整理ツールに連携
		ArrangeToolLink arrange = getArrangeLink();
		// 全視点での整理
		String[][] every = getBoardArrange(arrange);
		// 自分が村人視点での整理
		String[][] self = getSelfBoardArrange(arrange, false);
		// 人外候補リストの更新
		SwfCandidates = addNonVillagerSideCandidates(arrange, self, SwfCandidates);
		
		// 残り吊り縄が1の場合、または妖狐が確定で死亡している場合、人狼候補に対して投票
		if(arrange.getTotalState(self).get("count-expelled") == 1 || arrange.getTotalState(self).get("max-a-Rf") == 0) {
			chooseVoteToWolf(arrange, every, self, false);
			return true;
		}
		
		// 妖狐が確定生存の場合
		if(arrange.getTotalState(self).get("min-a-Rf") == 1) {
			// 確定白人外(妖狐or背徳者)がいる場合
			if(arrange.getTotalState(self).get("disi-a-Swf") > 0) {
				List<Agent> disitionSfList = arrange.getDisitionSwfList(self).stream().filter(a -> arrange.getDisitionNRwList(self).contains(a)).collect(Collectors.toList());
				if(disitionSfList.size() > 0) {
					voteCandidate = randomSelect(disitionSfList);
					return true;
				}
			}
			// 妖狐or背徳者COしたプレイヤーがいる場合
			List<Agent> SfCOList = new ArrayList<>();
			for(Agent a : currentGameInfo.getAliveAgentList()) {
				if(NotVillagerSideCOMap.get(a) == Role.FOX || NotVillagerSideCOMap.get(a) == Role.IMMORALIST) {
					SfCOList.add(a);
				}
			}
			if(SfCOList.size() > 0) {
				voteCandidate = randomSelect(SfCOList);
				return true;
			}
			// 残り2縄の場合、妖狐が否定されてないプレイヤーからランダム
			if(arrange.getTotalState(self).get("count-expelled") == 2) {
				chooseVoteToFox(arrange, every, self, false);
				return true;
			}
			// 残り縄数が3のとき占い師COが2人以下の場合対抗(ただし確定背徳者を除く)を投票候補から外す、また人狼COが2人以下の場合も人狼を投票候補から外す. 逆に占い師COが4人以上の場合は対抗占い師から投票する
			if(arrange.getTotalState(self).get("count-expelled") == 3) {
				chooseVoteLeave3(arrange, every, self, false);
				return true;
			}
			// それ以外の場合、確定村人陣営を除いたプレイヤーからランダム
			voteCandidate = randomSelect(aliveOthers.stream().filter(a -> !arrange.getDisitionSvList(self).contains(a)).collect(Collectors.toList()));
			return true;
		}
		return false;
	}
	
	/** 人狼狙いの投票 (投票関数優先度:1) */
	private void chooseVoteToWolf(ArrangeToolLink arrange, String[][] every, String[][] self, boolean isTalk) {
		// 確定人狼がいる場合
		if(arrange.getTotalState(self).get("disi-a-Rw") > 0) {
			for(Agent wolf : arrange.getDisitionRwList(self)) {
				wolfCandidates.add(wolf);
			}
			voteCandidate = randomSelect(arrange.getDisitionRwList(self));
			return;
		}
		// 人狼COしたプレイヤーがいる場合
		List<Agent> werewolfCOList = new ArrayList<>();
		for(Agent a : currentGameInfo.getAliveAgentList()) {
			if(NotVillagerSideCOMap.get(a) == Role.WEREWOLF) {
				wolfCandidates.add(a);
				werewolfCOList.add(a);
			}
		}
		if(werewolfCOList.size() > 0) {
			voteCandidate = randomSelect(werewolfCOList);
			return;
		}
		// 確定人外がいる場合
		if(arrange.getTotalState(self).get("disi-a-Swf") > 0) {
			for(Agent Swf : arrange.getDisitionSwfList(self)) {
				wolfCandidates.add(Swf);
			}
			voteCandidate = randomSelect(arrange.getDisitionSwfList(self));
			return;
		}
		// 人外候補がいる場合
		if(toAliveList(SwfCandidates).size() > 0) {
			voteCandidate = randomSelect(toAliveList(SwfCandidates));
			return;
		}
		// それ以外の場合は確白を除いたプレイヤーからランダム
		voteCandidate = randomSelect(aliveOthers.stream().filter(a -> !arrange.getDisitionNRwList(self).contains(a)).collect(Collectors.toList()));
		return;
	}

	/** 妖狐狙いの投票 (投票関数優先度:2) */
	private void chooseVoteToFox(ArrangeToolLink arrange, String[][] every, String[][] self, boolean isTalk) {
		// 盤面上で確定人外がいる場合
		if(arrange.getTotalState(self).get("disi-a-Swf") > 0) {
			for(Agent Swf : arrange.getDisitionSwfList(self)) {
				wolfCandidates.add(Swf);
			}
			voteCandidate = randomSelect(arrange.getDisitionSwfList(self));
			return;
		}
		// 人外候補がいる場合
		if(toAliveList(SwfCandidates).size() > 0) {
			voteCandidate = randomSelect(toAliveList(SwfCandidates));
			return;
		}
		voteCandidate = randomSelect(currentGameInfo.getAliveAgentList().stream().filter(a -> arrange.agentCandidate(self, Role.FOX).contains(a)).collect(Collectors.toList()));
		return;
	}
	
	/** 3縄ある場合の投票 (投票関数優先度:3) */
	private void chooseVoteLeave3(ArrangeToolLink arrange, String[][] every, String[][] self, boolean isTalk) {
		// 生存者から確定人狼候補は投票先から外す
		List<Agent> voteCandidates = aliveOthers.stream().filter(a -> !arrange.getDisitionRwList(self).contains(a)).collect(Collectors.toList());
		
		if(isCo(Role.SEER) && arrange.agentCandidate(every, Role.SEER).size() < 3) {
			voteCandidates = voteCandidates.stream().filter(a -> !arrange.agentCandidate(self, Role.SEER).contains(a)).collect(Collectors.toList());
			// 占い師が2CO以下の場合、初日の黒先を投票候補から外す
			for(Judge j : getDivinationList()) {
				if(j.getResult() == Species.WEREWOLF) {
					voteCandidates = voteCandidates.stream().filter(a -> a != j.getTarget()).collect(Collectors.toList());
				}
			}
		}
		if(aliveOthers.stream().filter(a -> NotVillagerSideCOMap.get(a) == Role.WEREWOLF).count() < 3) {
			voteCandidates = voteCandidates.stream().filter(a -> NotVillagerSideCOMap.get(a) != Role.WEREWOLF).collect(Collectors.toList());
		}
		if(isCo(Role.SEER) && arrange.agentCandidate(every, Role.SEER).size() > 3) {
			// 対抗占い師に投票 (白先対抗がいる場合はこの前の関数ですでに投票候補になっている)
			voteCandidates = voteCandidates.stream().filter(a -> arrange.agentCandidate(every, Role.SEER).contains(a) && a != me).collect(Collectors.toList());
			voteCandidate = randomSelect(voteCandidates);
			return;
		}
		// 確定村人陣営を投票候補から外す
		voteCandidates = voteCandidates.stream().filter(a -> !arrange.getDisitionSvList(self).contains(a)).collect(Collectors.toList());
		voteCandidate = randomSelect(voteCandidates);
		return;
	}
	
	@Override
	public String talk() {
		// カミングアウトする日になったら，あるいは占い結果が人狼だったら, またはその日の犠牲者が2人以上いる場合
		// あるいは占い師カミングアウトが出たらカミングアウト
		if (!isCameout && (day >= comingoutDay || isCo(Role.SEER) || currentGameInfo.getLastDeadAgentList().size() > 1 || yesterdayGameInfo.getSuicideImmoralistWithExecutedFox().size() > 0
				|| (!myDivinationList.isEmpty() && myDivinationList.get(myDivinationList.size() - 1).getResult() == Species.WEREWOLF))) {
			enqueueTalk(coContent(me, me, Role.SEER));
			isCameout = true;
		}
		// カミングアウトしたらこれまでの占い結果をすべて公開
		if (isCameout) {
			Content[] judges = myDivinationList.stream().map(j -> dayContent(me, j.getDay(),
					divinedContent(me, j.getTarget(), j.getResult()))).toArray(size -> new Content[size]);
			if (judges.length == 1) {
				enqueueTalk(judges[0]);
				enqueueTalk(judges[0].getContentList().get(0));
			} else if (judges.length > 1) {
				enqueueTalk(andContent(me, judges));
				for (Content c : judges) {
					enqueueTalk(c.getContentList().get(0));
				}
			}
			myDivinationList.clear();
		}
		return super.talk();
	}
	
@Override
	public Agent divine() {
//		System.out.println(day + "divine-------------------------------------------------");
		List<Agent> divineCandidates = grayList.stream().filter(a -> isAlive(a)).collect(Collectors.toList());
		// ゾーンをとっていた場合はゾーン内のプレイヤーから占う
		if(divineCandidates.stream().filter(a -> zone.contains(a)).collect(Collectors.toList()).size() > 0) {
			divineCandidates = divineCandidates.stream().filter(a -> zone.contains(a)).collect(Collectors.toList());
		}
		// 2日目の占い先(dayは1になっている)
		if(day == 1) {
			/** 基本方針としては「妖狐を狙う」
			 *  背徳者が2人いるため妖狐は得票されにくいことから得票数が少ない位置を狙う
			 *  また、妖狐への投票意思を見せたプレイヤーは妖狐や背徳者から非背徳者がほぼ透けるので、妖狐や背徳者からの票が飛びやすい
			 */
			// もし初日の追放者の投票先のAgentがその追放者に投票を入れていた場合、確率P_Executed_CrossVoteでそのAgentを占う
			Agent executedVoteTar = getVoteTarget(currentGameInfo.getLatestExecutedAgent(), 1);
			if(getVoteTarget(executedVoteTar, 1) == currentGameInfo.getLatestExecutedAgent() && grayList.contains(executedVoteTar) && randP(P_Executed_CrossVote) && divineCandidates.contains(executedVoteTar)) {
				return executedVoteTar;
			}
			// そうでない場合、1票以下のAgentからランダム
			List<Agent> candidates = divineCandidates.stream().filter(a -> isAlive(a) && getVotedCount(a, 1) < 2).collect(Collectors.toList());
			// 確率P_VoteToExecutedで追放者に投票していた人からランダムに占う
			List<Agent> VoteToExecuted = candidates.stream().filter(a -> getVoteAgent(currentGameInfo.getLatestExecutedAgent(), 1).contains(a)).collect(Collectors.toList());
			if(VoteToExecuted.size() > 0 && randP(P_VoteToExecuted)) {
				return randomSelect(VoteToExecuted);
			}
			if(candidates.size() > 0) {
				return randomSelect(candidates);
			}
		}
		// 3日目以降の占い先
		if(day > 1) {
			/** 基本的に背徳者が1人も死亡していない場合には勝ち目がほぼないので、死亡者に背徳者が含まれている仮定を取る
			 *  そのため基本方針としては「死亡者を背徳者と仮定したときの妖狐候補位置を狙う」
			 */
			List<Agent> deadList = currentGameInfo.getAgentList().stream().filter(a -> !currentGameInfo.getAliveAgentList().contains(a)).collect(Collectors.toList());
			// 死亡者の投票先リスト
			List<Agent> deadVotedTar = new ArrayList<>();
			for(Agent d : deadList) {
				for(Agent t : getVoteTarget(d)) {
					deadVotedTar.add(t);
				}
			}
			// 死亡者が投票してない位置の中からランダムに占う
			List<Agent> candidates = divineCandidates.stream().filter(a -> !deadVotedTar.contains(a)).collect(Collectors.toList());
			if(candidates.size() > 0) {
				return randomSelect(candidates);
			}
		}
		/*
		// 人狼候補がいればそれらからランダムに占う
		if (!wolfCandidates.isEmpty()) {
			return randomSelect(wolfCandidates);
		}
		//*/
		// 人狼候補がいない場合，まだ占っていない生存者からランダムに占う
		List<Agent> candidates = divineCandidates;
		if (candidates.isEmpty()) {
			return null;
		}
		Agent a = randomSelect(candidates);
		return a;
	}

	/** 占い師視点の整理実行 */
	@Override
	public String[][] getSelfBoardArrange(ArrangeToolLink arrange, boolean isPrint) {
		if(currentDataSelf != null && currentArrangeToolLink == arrange) {
			return currentDataSelf;
		}
		String[][] data = arrange.copyData(arrange.executeArrangement(me, myRole));
		// 潜伏している場合
		if(!isCo(me)) {
			// 黒結果
			for(Agent wolf : blackList) {
				data = getBoardArrange(arrange, data, wolf, Role.WEREWOLF);
			}
			// 白結果
			for(Agent notWolf : whiteList) {
				data = getBoardArrange(arrange, data, notWolf, Role.WEREWOLF, false);
				if(currentGameInfo.getAliveAgentList().contains(notWolf)) {
					data = getBoardArrange(arrange, data, notWolf, Role.FOX, false);
				}
			}
		}
		currentDataSelf = data;
		if(isPrint) {
			System.out.println("[" + me.getAgentIdx() + "]" + me.getName() + " → " + myRole + "視点");
		}
		return data;
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
	public Agent guard() {
		throw new UnsupportedOperationException();
	}

}
