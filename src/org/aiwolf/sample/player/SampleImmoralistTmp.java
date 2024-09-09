/**
 * SampleImmoralistTmp.java
 * 
 * Copyright (c) 2018 人狼知能プロジェクト
 */
package org.aiwolf.sample.player;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.aiwolf.client.lib.Content;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Judge;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.data.Status;
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
	
	/** 偽判定リスト[占い師騙りのみ使用] (潜伏時は占い対象だけ決めておく、COして結果公開するときに白か黒かを考える) */
	private List<Agent> myFakeDivineTargetList = new ArrayList<>();

	/** 未公表偽判定の待ち行列[占い師騙りのみ使用] */
	private Deque<Agent> myFakeDivineTargetQueue = new LinkedList<>();
	
	/** 未公表偽判定の日にちの待ち行列[占い師騙りのみ使用] */
	private Deque<Integer> myFakeDivinedDayQueue = new LinkedList<>();

	/** 偽白のリスト[占い師騙りのみ使用] */
	private List<Agent> fakeWhiteList = new ArrayList<>();

	/** 偽黒のリスト[占い師騙りのみ使用] */
	private List<Agent> fakeBlackList = new ArrayList<>();
	
	
	/** 非背徳者の候補リスト(背徳者の可能性が非常に低いAgent, 妖狐は除く) */
	private List<Agent> notImmoralistCandidates = new ArrayList<>();
	
	/** 背徳者視点で優先的に吊りたい候補リスト(最初に入っているほうがより吊りたい候補) */
	private List<Agent> wantExeScale = new ArrayList<>();
	
	/** 背徳者視点でできるだけ投票を避けたいAgentリスト(現在は使用してない) */
	private List<Agent> voteNotCandidatesRi = new ArrayList<>();
	
	/** 非背徳者候補に投票をする確率 */
	private static final int P_VoteNotRiCandidate = 77;
	
	/** 占い師を騙る確率 */
	private static final int P_PretendSeer = 35;
	
	/** 背徳者視点で吊りたい該当Agentに投票を決める確率(外れたら次の候補に) */
	private static final int P_PrioScale = 77;
	
	/** 再投票のときに投票を変える確率 */
	private static final int P_RevoteToChange = 21;
	
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
		wantExeScale.clear();
		voteNotCandidatesRi.clear();
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

		if(isAllSeerTalkResult() && !(!isCo(Role.SEER) && currentGameInfo.getLastDeadAgentList().size() > 1)) {
			// 盤面整理ツールに連携
			ArrangeToolLink arrange = getArrangeLink();
			// 全視点での整理
			//String[][] every = getBoardArrange(arrange);
			// 自分が背徳者視点での整理
			//String[][] self = getSelfBoardArrange(arrange, false);
			// 自身が主張する村人陣営役職視点での整理
			String[][] pretend = getCOBoardArrange(arrange, me, false);
			// 人外候補リストの更新
			SwfCandidates = addNonVillagerSideCandidates(arrange, pretend, SwfCandidates);
			
			if(!arrange.isBankruptcy(pretend)) {
				if(arrange.agentDisition(pretend, Role.VILLAGER).size() > 0) {
					for(Agent villager : arrange.agentDisition(pretend, Role.VILLAGER)) {
						if(villager != me) {
							enqueue1Talk(declaredContent(me, villager, Role.VILLAGER));
						}
					}
				}
				if(arrange.agentDisition(pretend, Role.SEER).size() > 0) {
					for(Agent seer : arrange.agentDisition(pretend, Role.SEER)) {
						if(seer != me) {
							enqueue1Talk(declaredContent(me, seer, Role.SEER));
						}
					}
				}
				if(arrange.agentDisition(pretend, Role.WEREWOLF).size() > 0) {
					for(Agent werewolf : arrange.agentDisition(pretend, Role.WEREWOLF)) {
						enqueue1Talk(declaredContent(me, werewolf, Role.WEREWOLF));
					}
				}
				if(arrange.agentDisition(pretend, Role.FOX).size() > 0) {
					for(Agent fox : arrange.agentDisition(pretend, Role.FOX)) {
						enqueue1Talk(declaredContent(me, fox, Role.FOX));
					}
				}
			}
			chooseVoteWithArrangeTool(true);
		}
		
		// 占い師騙り視点 [次の説明は自身が占い師仮定した場合の役職]
		if(getCoRole(me) == Role.SEER) {
			// 偽占い師は人狼か妖狐か背徳者
			for (Agent he : aliveOthers) {
				Content iAm = coContent(me, me, Role.SEER);
				if (comingoutMap.get(he) == Role.SEER) {
					wolfCandidates.add(he);
					// CO後なら推定理由をつける
					if (isCameout) {
						Content heIs = coContent(he, he, Role.SEER);
						Content reason = andContent(me, iAm, heIs);
						// 生存白先の対抗占いは背徳者確定
						if(fakeWhiteList.contains(he)) {
							reason = andContent(me, iAm, heIs, divinedContent(me, he, Species.HUMAN));
							estimateReasonMap.put(new Estimate(me, he, reason, Role.IMMORALIST));
							enqueue1Talk(becauseContent(me, reason, declaredContent(me, he, Role.IMMORALIST)));
							if(!foxes.contains(he)) {
								voteReasonMap.put(me, he, declaredContent(me, he, Role.IMMORALIST));
							}
							// 背徳者基軸からの妖狐位置推定
							List<Agent> foxCandidates = aliveOthers.stream().filter(a -> !getWantExecuteTarget(he).contains(a) && !fakeWhiteList.contains(a) && a != he && !fakeBlackList.contains(a)).collect(Collectors.toList());
							if(foxCandidates.size() > 0 && foxCandidates.size() < 4) {
								List<Content> notVote = new ArrayList<>();
								List<Content> foxCand = new ArrayList<>();
								//List<Content> divina = new ArrayList<>(); 
								for(Agent c : foxCandidates) {
									notVote.add(notContent(me, votedContent(he, c)));
									foxCand.add(estimateContent(me, c, Role.FOX));
									//divina.add(divinationContent(me, c));
									//zone.add(c);
								}
								Content notVoteReason = andContent(me, declaredContent(me, he, Role.IMMORALIST), andContent(me, notVote));
								enqueue1Talk(becauseContent(me, notVoteReason, orContent(me, foxCand)));
								//enqueue1Talk(dayContent(me, day + 1, orContent(me, divina)));
							}
						}
						else if(fakeBlackList.size() > 0) {
							// 対抗占い以外で人狼が見つかっている場合、対抗占いは妖狐か背徳者
							if(fakeBlackList.get(0) != he) {
								reason = andContent(me, iAm, heIs, divinedContent(me, fakeBlackList.get(0), Species.WEREWOLF));
								estimateReasonMap.put(new Estimate(me, he, reason, Role.FOX, Role.IMMORALIST));
								if(!foxes.contains(he)) {
									voteReasonMap.put(me, he, orContent(me, estimateContent(me, he, Role.FOX), estimateContent(me, he, Role.IMMORALIST)));
								}
								enqueue1Talk(becauseContent(me, reason, orContent(me, estimateContent(me, he, Role.FOX), estimateContent(me, he, Role.IMMORALIST))));
								List<Agent> foxCandidates = aliveOthers.stream().filter(a -> !getWantExecuteTarget(he).contains(a) && !fakeWhiteList.contains(a) && a != he && !fakeBlackList.contains(a)).collect(Collectors.toList());
								if(foxCandidates.size() > 0 && foxCandidates.size() < 4) {
									List<Content> notVote = new ArrayList<>();
									List<Content> foxCand = new ArrayList<>();
									//List<Content> divina = new ArrayList<>(); 
									for(Agent c : foxCandidates) {
										notVote.add(notContent(me, votedContent(he, c)));
										foxCand.add(estimateContent(me, c, Role.FOX));
										//divina.add(divinationContent(me, c));
										//zone.add(c);
									}
									Content notVoteReason = andContent(me, notVote);
									enqueue1Talk(ifContent(me, estimateContent(me, he, Role.IMMORALIST), becauseContent(me, notVoteReason, orContent(me, foxCand))));
									//enqueue1Talk(dayContent(me, day + 1, orContent(me, divina)));
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
		wolfCandidates = excludeFoxList(wolfCandidates);

		if (!wolfCandidates.isEmpty()) {
			// 見つかった場合
			if (!wolfCandidates.contains(voteCandidate)) {
				// 新しい投票先の場合，推測発言をする
				voteCandidate = selectVote(wolfCandidates);
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
				List<Agent> aliveEnemies = excludeFoxList(aliveOthers);
				
				// 非背徳者候補の生存者がいる場合は確率P_VoteNotRiCandidateでその候補の中から投票
				List<Agent> aliveEnemies2 = aliveEnemies.stream().filter(a -> notImmoralistCandidates.contains(a)).collect(Collectors.toList());
				if(aliveEnemies2.size() > 0 && randP(P_VoteNotRiCandidate)) {
					voteCandidate = selectVote(aliveEnemies2);
				}
				else {
					voteCandidate = selectVote(aliveEnemies);
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
			
			// 盤面整理ツールと連携 (うまくいかなかった場合はfalseが返される)
			if(chooseVoteWithArrangeTool(false)) {
				return;
			}
			
			// 人狼候補が見つけられなかった場合，初回投票では投票リクエストに応じる
			if (wolfCandidates.isEmpty()) {
				List<Agent> voteReqEnemies = voteRequestCounter.getRequestMap().values().stream().filter(a -> a != me && !foxes.contains(a)).collect(Collectors.toList());
				voteCandidate = selectVote(voteReqEnemies);
				List<Agent> voteReqEnemies2 = voteRequestCounter.getRequestMap().values().stream().filter(a -> a != me && !foxes.contains(a) && notImmoralistCandidates.contains(a)).collect(Collectors.toList());
				if(voteReqEnemies2.size() > 0 && randP(P_VoteNotRiCandidate)) {
					voteCandidate = selectVote(voteReqEnemies2);
				}
				if (voteCandidate == null || !isAlive(voteCandidate)) {
					List<Agent> aliveEnemies = excludeFoxList(aliveOthers);
					// 吊り縄が3のときは確定村人陣営を投票候補から外す
					if(arrange.getTotalState(every).get("count-expelled") == 3) {
						aliveEnemies = aliveEnemies.stream().filter(a -> !arrange.getDisitionSvList(every).contains(a)).collect(Collectors.toList());
					}
					// 非背徳者候補の生存者がいる場合は確率P_VoteNotRiCandidateでその候補の中から投票
					List<Agent> aliveEnemies2 = aliveEnemies.stream().filter(a -> notImmoralistCandidates.contains(a)).collect(Collectors.toList());
					if(aliveEnemies2.size() > 0 && randP(P_VoteNotRiCandidate)) {
						voteCandidate = selectVote(aliveEnemies2);
					}
					else {
						voteCandidate = selectVote(aliveEnemies);
					}
				}
			}
		} else {
			// 再投票の場合は5人盤面以下なら自分と妖狐以外の前回最多得票に入れる
			if(randP(P_RevoteToChange) || currentGameInfo.getAliveAgentList().size() < 6) {
				if(chooseVoteWithArrangeTool(false) && currentGameInfo.getAliveAgentList().size() > 5) {
					return;
				}
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
					List<Agent> aliveEnemies = excludeFoxList(aliveOthers);
					// 非背徳者候補の生存者がいる場合は確率P_VoteNotRiCandidateでその候補の中から投票
					List<Agent> aliveEnemies2 = aliveEnemies.stream().filter(a -> notImmoralistCandidates.contains(a)).collect(Collectors.toList());
					if(aliveEnemies2.size() > 0 && randP(P_VoteNotRiCandidate)) {
						voteCandidate = selectVote(aliveEnemies2);
					}
					else {
						voteCandidate = selectVote(aliveEnemies);
					}
				} else {
					voteCandidate = candidates.get(0);
				}
			}
		}
	}
	
	private boolean chooseVoteWithArrangeTool(boolean isTalk) {
		// 盤面整理ツールに連携
		ArrangeToolLink arrange = getArrangeLink();
		// 全視点での整理
		String[][] every = getBoardArrange(arrange);
		// 背徳者視点での整理
		String[][] self = getSelfBoardArrange(arrange, false);
		// 自身が主張する村人陣営役職視点での整理
		String[][] pretend = getCOBoardArrange(arrange, me, false);
		// 人外候補リストの更新
		SwfCandidates = addNonVillagerSideCandidates(arrange, pretend, SwfCandidates);
		
		// 背徳者視点で吊りたい位置のスケールの更新
		wantExecuteForRi(arrange, every, self);
		
		// 自視点が破綻していた場合はこの関数を使わない
		if(arrange.isBankruptcy(pretend)) {
			return false;
		}
		
		// 残り吊り縄が1の場合人狼候補に対して投票
		if(arrange.getTotalState(pretend).get("count-expelled") == 1) {
			chooseVoteToWolf(arrange, every, pretend, isTalk);
			return true;
		}
		
		// 騙り視点で偽装妖狐が確定生存の場合
		if(arrange.getTotalState(pretend).get("min-a-Rf") == 1) {
			// 確定白人外(妖狐or背徳者)がいる場合
			if(arrange.getTotalState(pretend).get("disi-a-Swf") > 0) {
				// 騙り役職視点での白人外候補(妖狐除く)
				List<Agent> disitionSfList = arrange.getDisitionSwfList(pretend).stream().filter(a -> arrange.getDisitionNRwList(pretend).contains(a) && !foxes.contains(a)).collect(Collectors.toList());
				if(disitionSfList.size() > 0) {
					voteCandidate = randomSelect(disitionSfList);
					if(isTalk && getCoRole(me) == Role.VILLAGER) {
						if(getCoRole(voteCandidate) == Role.SEER) {
							if(getDivinedResultList(voteCandidate, Species.WEREWOLF).size() == 1) {
								// 1.黒先が死亡しているが終わらない
								if(!isAlive(getDivinedResultList(voteCandidate, Species.WEREWOLF).get(0))) {
									Content divined = divinedContent(voteCandidate, getDivinedResultList(voteCandidate, Species.WEREWOLF).get(0), Species.WEREWOLF);   
									Content reason = andContent(me, divined, declaredStatusContent(me, Role.WEREWOLF, Status.ALIVE), notContent(me, estimateContent(me, voteCandidate, Role.WEREWOLF)));
									estimateReasonMap.put(new Estimate(me, voteCandidate, reason, Role.FOX, Role.IMMORALIST));
									voteReasonMap.put(me, voteCandidate, reason);
								}
								// 2.黒先が盤面上確白である
								else if(arrange.getDisitionNRwList(every).contains(getDivinedResultList(voteCandidate, Species.WEREWOLF).get(0))) {
									Content divined = divinedContent(voteCandidate, getDivinedResultList(voteCandidate, Species.WEREWOLF).get(0), Species.WEREWOLF);   
									Content reason = andContent(me, divined, notContent(me, estimateContent(me, getDivinedResultList(voteCandidate, Species.WEREWOLF).get(0), Role.WEREWOLF)), notContent(me, estimateContent(me, voteCandidate, Role.WEREWOLF)));
									estimateReasonMap.put(new Estimate(me, voteCandidate, reason, Role.FOX, Role.IMMORALIST));
									voteReasonMap.put(me, voteCandidate, reason);
								}
							}
							// 3.生存者の中に狼候補がいない
							if(toAliveList(getDivinedResultList(voteCandidate, Species.HUMAN)).size() == aliveOthers.size()) {
//								Content reason = estimateContent(voteCandidate, Content.UNSPEC, Role.WEREWOLF);
							}
							// 4.2人以上に黒を出しているなど
							Content reason = orContent(me, estimateContent(me, voteCandidate, Role.FOX), estimateContent(me, voteCandidate, Role.IMMORALIST));
							voteReasonMap.put(me, voteCandidate, reason);
						}
						Content reason = orContent(me, estimateContent(me, voteCandidate, Role.FOX), estimateContent(me, voteCandidate, Role.IMMORALIST));
						voteReasonMap.put(me, voteCandidate, reason);
					}
					return true;
				}
			}
			// 妖狐or背徳者COしたプレイヤーがいる場合
			List<Agent> SfCOList = new ArrayList<>();
			for(Agent a : currentGameInfo.getAliveAgentList()) {
				if(NotVillagerSideCOMap.get(a) == Role.FOX || NotVillagerSideCOMap.get(a) == Role.IMMORALIST && !foxes.contains(a)) {
					SfCOList.add(a);
				}
			}
			if(SfCOList.size() > 0) {
				voteCandidate = randomSelect(SfCOList);
				return true;
			}
			// 占い師が確定で死亡していて残り2縄の場合、騙り視点での偽装妖狐が否定されてないプレイヤーからランダム
			List<Agent> seerAliveCandidates = currentGameInfo.getAliveAgentList().stream().filter(a -> arrange.agentCandidate(pretend, Role.SEER).contains(a)).collect(Collectors.toList());
			if(seerAliveCandidates.size() == 0 && arrange.getTotalState(pretend).get("count-expelled") == 2) {
				chooseVoteToFox(arrange, every, pretend, isTalk);
				return true;
			}
			// 残り縄数が3のとき占い師COが2人以下の場合占い師を投票候補から外す、また人狼COが2人以下の場合も人狼を投票候補から外す. 逆に占い師COが4人以上の場合は占い師から投票する
			if(arrange.getTotalState(pretend).get("count-expelled") == 3) {
				chooseVoteLeave3(arrange, every, pretend, isTalk);
				return true;
			}
			// それ以外の場合、確定村人陣営と妖狐を除いたプレイヤーからランダム
			voteCandidate = randomSelect(aliveOthers.stream().filter(a -> !arrange.getDisitionSvList(pretend).contains(a) && !foxes.contains(a)).collect(Collectors.toList()));
			return true;
		}
		return false;
	}
	
	/** 背徳者視点で吊りたい位置 */
	private void wantExecuteForRi(ArrangeToolLink arrange, String[][] every, String[][] self) {
		wantExeScale.clear();
		// 1.背徳者視点での確定人狼
		if(toAliveList(arrange.getDisitionRwList(self)).size() > 0) {
			wantExeScale.add(toAliveList(arrange.getDisitionRwList(self)).get(0));
		}
		// 2.背徳者視点での確定人外(自身と妖狐除く)かつ非背徳者候補
		if(toAliveList(arrange.getDisitionSwfList(self)).size() > 2) {
			List<Agent> voteCandidates = toAliveList(arrange.getDisitionSwfList(self)).stream().filter(a -> a != me && !foxes.contains(a) && notImmoralistCandidates.contains(a)).collect(Collectors.toList());
			for(Agent a : voteCandidates) {
				wantExeScale.add(a);
			}
		}
		// 3.背徳者視点での確定占い師
		if(toAliveList(arrange.agentDisition(self, Role.SEER)).size() > 0) {
			wantExeScale.add(toAliveList(arrange.agentDisition(self, Role.SEER)).get(0));
		}
		// 4.背徳者視点で偽が確定していない占い師のうち、次に妖狐を占ってきそうな占い師CO者(ゾーンはとりあえず除く、あからさまになりやすいので)
		   // 具体的には占い師視点でのグレー位置が1つの場合
		List<Agent> villagerCo = currentGameInfo.getAliveAgentList().stream().filter(a -> getCoRole(a) != Role.SEER).collect(Collectors.toList());
		for(Agent seer : arrange.agentCandidate(self, Role.SEER)) {
			List<Agent> divined = getDivinedResultList(seer, Species.ANY);
			List<Agent> gray = villagerCo.stream().filter(a -> !divined.contains(a)).collect(Collectors.toList());
			if(gray.size() < 2 && !wantExeScale.contains(seer)) {
				wantExeScale.add(seer);
			}
		}
		// 5.自身が占い師COしていて背徳者視点で偽が確定していない占い師のうち自身への白結果を持っている占い師CO者(ラインから妖狐推定→占いされるリスク)
		if(getCoRole(me) == Role.SEER) {
			List<Judge> forMeDivine = divinationList.stream().filter(j -> arrange.agentCandidate(self, Role.SEER).contains(j.getAgent())).collect(Collectors.toList());
			forMeDivine = forMeDivine.stream().filter(j -> isAlive(j.getAgent()) && j.getTarget() == me).collect(Collectors.toList());
			for(Judge j : forMeDivine) {
				if(!wantExeScale.contains(j.getAgent())) {
					wantExeScale.add(j.getAgent());
				}
			}
		}
		// 6.妖狐に対して吊りたいと発言、または妖狐に投票したことのあるプレイヤー(非背徳者目)
		for(Agent a : notImmoralistCandidates) {
			if(!wantExeScale.contains(a)) {
				wantExeScale.add(a);
			}
		}
		// 7.背徳者視点で偽が確定していない占い師の黒先
		List<Judge> forDivine = divinationList.stream().filter(j -> arrange.agentCandidate(self, Role.SEER).contains(j.getAgent()) && isAlive(j.getTarget())).collect(Collectors.toList());
		List<Judge> forBlack = forDivine.stream().filter(j -> j.getResult() == Species.WEREWOLF).collect(Collectors.toList());
		for(Judge j : forBlack) {
			if(!wantExeScale.contains(j.getTarget())) {
				wantExeScale.add(j.getTarget());
			}
		}
		// 8.背徳者視点で偽が確定していない占い師の占われ先
		forDivine = forDivine.stream().filter(j -> j.getTarget() != me).collect(Collectors.toList());
		for(Judge j : forDivine) {
			if(!wantExeScale.contains(j.getTarget())) {
				wantExeScale.add(j.getTarget());
			}
		}
	}
	
	/** 偽装人狼狙いの投票 (投票関数優先度:1) */
	private void chooseVoteToWolf(ArrangeToolLink arrange, String[][] every, String[][] pretend, boolean isTalk) {
		// 1.確定人狼がいる場合
		if(arrange.getTotalState(pretend).get("disi-a-Rw") > 0) {
			for(Agent wolf : arrange.getDisitionRwList(pretend)) {
				wolfCandidates.add(wolf);
			}
			if(excludeFoxList(arrange.getDisitionRwList(pretend)).size() > 0) {
				voteCandidate = selectVote(excludeFoxList(arrange.getDisitionRwList(pretend)));
				// 残り1縄での発言生成「人狼が確定しているAgentがいるのでそのAgentに投票します」
				if(isTalk && getCoRole(me) == Role.SEER) {
					// 自身の黒先の場合
					if(fakeBlackList.contains(voteCandidate) && isCameout) {
						Content myDivination = divinedContent(me, voteCandidate, Species.WEREWOLF);
						voteReasonMap.put(me, voteCandidate, myDivination);
					}
					// 対抗占い師の場合
					else if(getCoRole(voteCandidate) == Role.SEER){
						Content reason = andContent(me, coContent(me, me, Role.SEER), coContent(voteCandidate, voteCandidate, Role.SEER));
						estimateReasonMap.put(new Estimate(me, voteCandidate, reason, Role.WEREWOLF));
						voteReasonMap.put(me, voteCandidate, reason);
					}
					// それ以外
					else {
						Content reason = andContent(me, declaredContent(me, voteCandidate, Role.WEREWOLF));
						voteReasonMap.put(me, voteCandidate, reason);
					}
				}
				if(isTalk && getCoRole(me) == Role.VILLAGER) {
					if(arrange.agentDisition(pretend, Role.SEER).size() == 1) {
						// 占い師が確定していてその黒先の場合
						if(getDivinedResultList(arrange.agentDisition(pretend, Role.SEER).get(0), Species.WEREWOLF).contains(voteCandidate)) {
							Content disitionSeer = declaredContent(me, arrange.agentDisition(pretend, Role.SEER).get(0), Role.SEER);
							Content divined = divinedContent(arrange.agentDisition(pretend, Role.SEER).get(0), voteCandidate, Species.WEREWOLF);
							Content reason = andContent(me, disitionSeer, divined);
							voteReasonMap.put(me, voteCandidate, reason);
						}
						// 占い師騙りをしていて破綻している場合
						else if(getCoRole(voteCandidate) == Role.SEER) {
							if(getDivinedResultList(voteCandidate, Species.WEREWOLF).size() == 1) {
								// 1.黒先が死亡しているが終わらない
								if(!isAlive(getDivinedResultList(voteCandidate, Species.WEREWOLF).get(0))) {
									Content divined = divinedContent(voteCandidate, getDivinedResultList(voteCandidate, Species.WEREWOLF).get(0), Species.WEREWOLF);   
									Content reason = andContent(me, divined, declaredStatusContent(me, Role.WEREWOLF, Status.ALIVE));
									estimateReasonMap.put(new Estimate(me, voteCandidate, reason, Role.WEREWOLF));
									voteReasonMap.put(me, voteCandidate, reason);
								}
								// 2.黒先が盤面上確白である
								else if(arrange.getDisitionNRwList(every).contains(getDivinedResultList(voteCandidate, Species.WEREWOLF).get(0))) {
									Content divined = divinedContent(voteCandidate, getDivinedResultList(voteCandidate, Species.WEREWOLF).get(0), Species.WEREWOLF);   
									Content reason = andContent(me, divined, declaredContent(me, getDivinedResultList(voteCandidate, Species.WEREWOLF).get(0), Role.VILLAGER));
									if(getCoRole(getDivinedResultList(voteCandidate, Species.WEREWOLF).get(0)) == Role.SEER) {
										reason = andContent(me, divined, declaredContent(me, getDivinedResultList(voteCandidate, Species.WEREWOLF).get(0), Role.SEER));
									}
									estimateReasonMap.put(new Estimate(me, voteCandidate, reason, Role.WEREWOLF));
									voteReasonMap.put(me, voteCandidate, reason);
								}
							}
							else {
								// 3.生存者の中に狼候補がいない
								if(toAliveList(getDivinedResultList(voteCandidate, Species.HUMAN)).size() == aliveOthers.size()) {
//									Content reason = estimateContent(voteCandidate, Content.UNSPEC, Role.WEREWOLF);
								}
								// 4.呪殺対応ができていない、2人以上に黒を出しているなど
								voteReasonMap.put(me, voteCandidate, declaredContent(me, voteCandidate, Role.WEREWOLF));
							}
						}
						else {
							voteReasonMap.put(me, voteCandidate, declaredContent(me, voteCandidate, Role.WEREWOLF));
						}
					}
					else {
						voteReasonMap.put(me, voteCandidate, declaredContent(me, voteCandidate, Role.WEREWOLF));
					}
				}
				return;
			}
		}
		// 2.人狼COしたプレイヤーがいる場合(妖狐除く)
		List<Agent> werewolfCOList = new ArrayList<>();
		for(Agent a : currentGameInfo.getAliveAgentList()) {
			if(NotVillagerSideCOMap.get(a) == Role.WEREWOLF && !foxes.contains(a)) {
				wolfCandidates.add(a);
				werewolfCOList.add(a);
			}
		}
		if(werewolfCOList.size() > 0) {
			voteCandidate = selectVote(werewolfCOList);
			// 発言生成「人狼COしているAgentがいるのでそのAgentに投票します」
			if(isTalk) {
				Content reason = andContent(me, coContent(voteCandidate, voteCandidate, Role.WEREWOLF));
				voteReasonMap.put(me, voteCandidate, reason);
			}
			return;
		}
		// 3.確定人外がいる場合
		if(arrange.getTotalState(pretend).get("disi-a-Swf") > 0) {
			for(Agent Swf : arrange.getDisitionSwfList(pretend)) {
				wolfCandidates.add(Swf);
			}
			if(excludeFoxList(arrange.getDisitionSwfList(pretend)).size() > 0) {
				voteCandidate = selectVote(excludeFoxList(arrange.getDisitionSwfList(pretend)));
				return;
			}
		}
		// 4.妖狐を除いた人外候補がいる場合
		if(excludeFoxList(toAliveList(SwfCandidates)).size() > 0) {
			voteCandidate = selectVote(excludeFoxList(toAliveList(SwfCandidates)));
			return;
		}
		// 5.それ以外の場合は妖狐と確定村人陣営を除いたプレイヤーからランダム
		voteCandidate = selectVote(aliveOthers.stream().filter(a -> !arrange.getDisitionSvList(pretend).contains(a) && !foxes.contains(a)).collect(Collectors.toList()));
		return;
	}
	
	/** 偽装妖狐狙いの投票 (投票関数優先度:2) */
	private void chooseVoteToFox(ArrangeToolLink arrange, String[][] every, String[][] pretend, boolean isTalk) {
		// 盤面上で確定人外がいる場合
		if(arrange.getTotalState(pretend).get("disi-a-Swf") > 0) {
			for(Agent Swf : arrange.getDisitionSwfList(pretend)) {
				wolfCandidates.add(Swf);
			}
			if(excludeFoxList(arrange.getDisitionSwfList(pretend)).size() > 0) {
				voteCandidate = selectVote(excludeFoxList(arrange.getDisitionSwfList(pretend)));
				if(isTalk && getCoRole(me) == Role.VILLAGER) {
					if(getCoRole(voteCandidate) == Role.SEER) {
						if(getDivinedResultList(voteCandidate, Species.WEREWOLF).size() == 1) {
							// 1.黒先が死亡しているが終わらない
							if(!isAlive(getDivinedResultList(voteCandidate, Species.WEREWOLF).get(0))) {
								Content divined = divinedContent(voteCandidate, getDivinedResultList(voteCandidate, Species.WEREWOLF).get(0), Species.WEREWOLF);   
								Content reason = andContent(me, divined, declaredStatusContent(me, Role.WEREWOLF, Status.ALIVE));
								estimateReasonMap.put(new Estimate(me, voteCandidate, reason, Role.WEREWOLF, Role.FOX, Role.IMMORALIST));
								voteReasonMap.put(me, voteCandidate, reason);
							}
							// 2.黒先が盤面上確白である
							else if(arrange.getDisitionNRwList(every).contains(getDivinedResultList(voteCandidate, Species.WEREWOLF).get(0))) {
								Content divined = divinedContent(voteCandidate, getDivinedResultList(voteCandidate, Species.WEREWOLF).get(0), Species.WEREWOLF);   
								Content reason = andContent(me, divined, notContent(me, estimateContent(me, getDivinedResultList(voteCandidate, Species.WEREWOLF).get(0), Role.WEREWOLF)));
								estimateReasonMap.put(new Estimate(me, voteCandidate, reason, Role.WEREWOLF, Role.FOX, Role.IMMORALIST));
								voteReasonMap.put(me, voteCandidate, reason);
							}
						}
						else {
							// 3.生存者の中に狼候補がいない
							if(toAliveList(getDivinedResultList(voteCandidate, Species.HUMAN)).size() == aliveOthers.size()) {
//								Content reason = estimateContent(voteCandidate, Content.UNSPEC, Role.WEREWOLF);
							}
							// 4.2人以上に黒を出しているなど
							Content reason = orContent(me, estimateContent(me, voteCandidate, Role.WEREWOLF), estimateContent(me, voteCandidate, Role.FOX), estimateContent(me, voteCandidate, Role.IMMORALIST));
							voteReasonMap.put(me, voteCandidate, reason);
						}
					}
				}
				return;
			}
		}
		// 人外候補がいる場合
		if(excludeFoxList(toAliveList(SwfCandidates)).size() > 0) {
			voteCandidate = selectVote(excludeFoxList(toAliveList(SwfCandidates)));
			return;
		}
		List<Agent> voteCandidates = excludeFoxList(currentGameInfo.getAliveAgentList().stream().filter(a -> arrange.agentCandidate(pretend, Role.FOX).contains(a)).collect(Collectors.toList()));
		if(voteCandidates.size() > 0) {
			voteCandidate = selectVote(voteCandidates);
			return;
		}
	}
	
	/** 3縄ある場合の投票 (投票関数優先度:3) */
	private void chooseVoteLeave3(ArrangeToolLink arrange, String[][] every, String[][] pretend, boolean isTalk) {
		// 生存者から確定人狼候補と妖狐は投票先から外す
		List<Agent> voteCandidates = aliveOthers.stream().filter(a -> !arrange.getDisitionRwList(pretend).contains(a) && !foxes.contains(a)).collect(Collectors.toList());
		
		if(isCo(Role.SEER) && arrange.agentCandidate(every, Role.SEER).size() < 3) {
			voteCandidates = voteCandidates.stream().filter(a -> !arrange.agentCandidate(pretend, Role.SEER).contains(a)).collect(Collectors.toList());
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
			voteCandidate = selectVote(voteCandidates);
			return;
		}
		// 確定村人陣営を投票候補から外す
		voteCandidates = voteCandidates.stream().filter(a -> !arrange.getDisitionSvList(pretend).contains(a)).collect(Collectors.toList());
		voteCandidate = selectVote(voteCandidates);
		return;
	}
	
	/** 投票候補から背徳者視点で吊りたい位置を優先して選択する */
	private Agent selectVote(List<Agent> voteCandidates) {
		if(wantExeScale.size() > 0) {
			for(Agent vote : wantExeScale) {
				if(voteCandidates.contains(vote) && randP(P_PrioScale)) {
					return vote;
				}
			}
		}
		return randomSelect(voteCandidates);
	}
	
	@Override
	public Agent vote() {
		chooseFinalVoteCandidate();
		isRevote = true;
		if(foxes.contains(voteCandidate)) {
			List<Agent> aliveEnemies = aliveOthers.stream().filter(a -> !foxes.contains(a)).collect(Collectors.toList());
			voteCandidate = selectVote(aliveEnemies);
		}
//		System.out.println(me.getName() + "----" + voteCandidate.getName());
		return voteCandidate;
	}
	
	/** 非妖狐のみをフィルタ */
	public List<Agent> excludeFoxList(List<Agent> list) {
		return list.stream().filter(a -> !foxes.contains(a)).collect(Collectors.toList());
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
		// もし妖狐が占い師騙りをしたら潜伏する
		boolean isFoxPretend = false;
		for(Agent fox : foxes) {
			if(getCoRole(fox) == Role.SEER) {
				isFoxPretend = true;
			}
		}
		if(isFoxPretend) {
			fakeRole = Role.VILLAGER;
			return;
		}
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
			// カミングアウトしたらこれまでの偽判定結果をすべて公開
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
					// 偽占い結果を登録
					if(divined == Species.HUMAN) {
						fakeWhiteList.add(judge.getTarget());
					}
					else {
						fakeBlackList.add(judge.getTarget());
					}
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
