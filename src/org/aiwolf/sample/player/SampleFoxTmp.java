/**
 * SampleFoxTmp.java
 * 
 * Copyright (c) 2018 人狼知能プロジェクト
 */
package org.aiwolf.sample.player;

import java.util.ArrayList;
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
 * 妖狐(仮)役エージェントクラス
 * 
 * @author otsuki
 */
public final class SampleFoxTmp extends SampleBasePlayer {

	/** 人狼候補リスト */
	private List<Agent> wolfCandidates = new ArrayList<>();
	
	/** 妖狐リスト */
	private List<Agent> foxes = new ArrayList<>();
	
	/** 非背徳者の候補リスト(背徳者の可能性が非常に低いAgent) */
	private List<Agent> notImmoralistCandidates = new ArrayList<>();
	
	/** 妖狐視点で優先的に吊りたい候補リスト(最初に入っているほうがより吊りたい候補) */
	private List<Agent> wantExeScale = new ArrayList<>();
	
	/** 非背徳者候補に投票をする確率 */
	private static final int P_VoteNotRiCandidate = 63;
	
	/** 妖狐視点で吊りたい該当Agentに投票を決める確率(外れたら次の候補に) */
	private static final int P_PrioScale = 77;
	
	/** 人外候補リスト */
	private List<Agent> SwfCandidates = new ArrayList<>();
	
	public void initialize(GameInfo gameInfo, GameSetting gameSetting) {
		super.initialize(gameInfo, gameSetting);
		myRole = Role.FOX;
		foxes = new ArrayList<>(gameInfo.getRoleMap().keySet());
		SwfCandidates.clear();
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
//			String[][] every = getBoardArrange(arrange);
			// 自分が妖狐視点での整理
//			String[][] self = getSelfBoardArrange(arrange, false);
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
						enqueue1Talk(declaredContent(me, seer, Role.SEER));
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
				chooseVoteWithArrangeTool(true);
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
				// できるだけ妖狐以外や非背徳者候補から投票先を決める
				List<Agent> aliveEnemies = aliveOthers.stream().filter(a -> !foxes.contains(a) && notImmoralistCandidates.contains(a)).collect(Collectors.toList());
				
				// 4人盤面のときは100%の確率で非背徳者候補がいたらそこに入れる
				if(aliveEnemies.size() > 0 && aliveOthers.size() == 3) {
					voteCandidate = randomSelect(aliveEnemies);
				}
				else if(aliveEnemies.size() > 0 && randP(P_VoteNotRiCandidate)) {
					voteCandidate = randomSelect(aliveEnemies);
				}
				else {
					voteCandidate = randomSelect(aliveOthers);
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
			
			// 人狼候補が見つけられなかった場合，初回投票では投票リクエストに応じる
			if (wolfCandidates.isEmpty()) {
				List<Agent> voteReqEnemies = voteRequestCounter.getRequestMap().values().stream().filter(a -> a != me && !foxes.contains(a)).collect(Collectors.toList());
				voteCandidate = randomSelect(voteReqEnemies);
				List<Agent> voteReqEnemies2 = voteRequestCounter.getRequestMap().values().stream().filter(a -> a != me && !foxes.contains(a) && notImmoralistCandidates.contains(a)).collect(Collectors.toList());
				if(voteReqEnemies2.size() > 0 && randP(P_VoteNotRiCandidate)) {
					voteCandidate = randomSelect(voteReqEnemies2);
				}
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
				// できるだけ妖狐以外や非背徳者候補から投票先を決める
				List<Agent> aliveEnemies = aliveOthers.stream().filter(a -> !foxes.contains(a) && notImmoralistCandidates.contains(a)).collect(Collectors.toList());
				if(aliveEnemies.size() > 0 && randP(P_VoteNotRiCandidate)) {
					voteCandidate = randomSelect(aliveEnemies);
				}
				else {
					voteCandidate = randomSelect(aliveOthers);
				}
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
		// 妖狐視点での整理
		String[][] self = getSelfBoardArrange(arrange, false);
		// 自身が主張する村人陣営役職視点での整理
		String[][] pretend = getCOBoardArrange(arrange, me, false);
		// 人外候補リストの更新
		SwfCandidates = addNonVillagerSideCandidates(arrange, pretend, SwfCandidates);
		
		// 妖狐視点で吊りたい位置のスケールの更新
		wantExecuteForRf(arrange, every, self);
		
		// 自視点が破綻していた場合はこの関数を使わない
		if(arrange.isBankruptcy(pretend)) {
			return false;
		}
		
		// 残り吊り縄が1の場合、非背徳者候補がいた場合はその候補に投票、それ以外は人狼候補に対して投票
		if(arrange.getTotalState(pretend).get("count-expelled") == 1) {
			if(toAliveList(notImmoralistCandidates).size() > 0) {
				voteCandidate = selectVote(toAliveList(notImmoralistCandidates));
				return true;
			}
			chooseVoteToWolf(arrange, every, pretend, isTalk);
			return true;
		}
		
		// 騙り視点で偽装妖狐が確定生存の場合
		if(arrange.getTotalState(pretend).get("min-a-Rf") == 1) {
			// 確定白人外(背徳者)がいる場合
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
			// それ以外の場合、確定村人陣営を除いたプレイヤーからランダム
			voteCandidate = randomSelect(aliveOthers.stream().filter(a -> !arrange.getDisitionSvList(pretend).contains(a) && !foxes.contains(a)).collect(Collectors.toList()));
			return true;
		}
		return false;
	}
			
	/** 妖狐視点で吊りたい位置 */
	private void wantExecuteForRf(ArrangeToolLink arrange, String[][] every, String[][] self) {
		wantExeScale.clear();
		// 1.妖狐視点での確定人狼
		if(toAliveList(arrange.getDisitionRwList(self)).size() > 0) {
			wantExeScale.add(toAliveList(arrange.getDisitionRwList(self)).get(0));
		}
		// 2.妖狐視点での確定人外(自身除く)かつ非背徳者候補
		if(toAliveList(arrange.getDisitionSwfList(self)).size() > 2) {
			List<Agent> voteCandidates = toAliveList(arrange.getDisitionSwfList(self)).stream().filter(a -> !foxes.contains(a) && notImmoralistCandidates.contains(a)).collect(Collectors.toList());
			for(Agent a : voteCandidates) {
				wantExeScale.add(a);
			}
		}
		// 3.妖狐視点での確定占い師
		if(toAliveList(arrange.agentDisition(self, Role.SEER)).size() > 0) {
			wantExeScale.add(toAliveList(arrange.agentDisition(self, Role.SEER)).get(0));
		}
		// 4.妖狐視点で偽が確定していない占い師のうち、次に自身を占ってきそうな占い師CO者(ゾーンはとりあえず除く、あからさまになりやすいので)
		   // 具体的には占い師視点でのグレー位置が1つの場合
		List<Agent> villagerCo = currentGameInfo.getAliveAgentList().stream().filter(a -> getCoRole(a) != Role.SEER).collect(Collectors.toList());
		for(Agent seer : arrange.agentCandidate(self, Role.SEER)) {
			List<Agent> divined = getDivinedResultList(seer, Species.ANY);
			List<Agent> gray = villagerCo.stream().filter(a -> !divined.contains(a)).collect(Collectors.toList());
			if(gray.size() < 2 && !wantExeScale.contains(seer)) {
				wantExeScale.add(seer);
			}
		}
		// 5.自身に対して吊りたいと発言、または投票されたことのあるプレイヤー(非背徳者目)
		for(Agent a : notImmoralistCandidates) {
			if(!wantExeScale.contains(a)) {
				wantExeScale.add(a);
			}
		}
		// 6.妖狐視点で偽が確定していない占い師の黒先
		List<Judge> forDivine = divinationList.stream().filter(j -> arrange.agentCandidate(self, Role.SEER).contains(j.getAgent()) && isAlive(j.getTarget())).collect(Collectors.toList());
		List<Judge> forBlack = forDivine.stream().filter(j -> j.getResult() == Species.WEREWOLF).collect(Collectors.toList());
		for(Judge j : forBlack) {
			if(!wantExeScale.contains(j.getTarget())) {
				wantExeScale.add(j.getTarget());
			}
		}
		// 7.妖狐視点で偽が確定していない占い師の占われ先
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
			if(arrange.getDisitionRwList(pretend).size() > 0) {
				voteCandidate = selectVote(arrange.getDisitionRwList(pretend));
				// 残り1縄での発言生成「人狼が確定しているAgentがいるのでそのAgentに投票します」
				if(isTalk && getCoRole(me) == Role.SEER) {
					// 自身の黒先の場合
					
					
					// 対抗占い師の場合
					if(getCoRole(voteCandidate) == Role.SEER){
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
			if(arrange.getDisitionSwfList(pretend).size() > 0) {
				voteCandidate = selectVote(arrange.getDisitionSwfList(pretend));
				return;
			}
		}
		// 4.妖狐を除いた人外候補がいる場合
		if(toAliveList(SwfCandidates).size() > 0) {
			voteCandidate = selectVote(toAliveList(SwfCandidates));
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
			if(arrange.getDisitionSwfList(pretend).size() > 0) {
				voteCandidate = selectVote(arrange.getDisitionSwfList(pretend));
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
		if(toAliveList(SwfCandidates).size() > 0) {
			voteCandidate = selectVote(toAliveList(SwfCandidates));
			return;
		}
		List<Agent> voteCandidates = currentGameInfo.getAliveAgentList().stream().filter(a -> arrange.agentCandidate(pretend, Role.FOX).contains(a)).collect(Collectors.toList());
		if(voteCandidates.size() > 0) {
			voteCandidate = selectVote(voteCandidates);
			return;
		}
	}
	
	/** 3縄ある場合の投票 (投票関数優先度:3) */
	private void chooseVoteLeave3(ArrangeToolLink arrange, String[][] every, String[][] pretend, boolean isTalk) {
		// 生存者から確定人狼候補は投票先から外す
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
	
	
	/** 投票候補から妖狐視点で吊りたい位置を優先して選択する */
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
