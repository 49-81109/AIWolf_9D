/**
 * SampleVillager.java
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
import org.aiwolf.common.data.Vote;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

/**
 * 村人役エージェントクラス
 * 
 * @author otsuki
 */
public final class SampleVillager extends SampleBasePlayer {

	/** 人狼候補リスト */
	private List<Agent> wolfCandidates = new ArrayList<>();
	
	/** 人外候補リスト */
	private List<Agent> SwfCandidates = new ArrayList<>();
	
	public void initialize(GameInfo gameInfo, GameSetting gameSetting) {
		super.initialize(gameInfo, gameSetting);
		myRole = Role.VILLAGER;
		SwfCandidates.clear();
	}

	@Override
	void chooseVoteCandidate() {
		wolfCandidates.clear();
		
		if(isAllSeerTalkResult() && !(!isCo(Role.SEER) && currentGameInfo.getLastDeadAgentList().size() > 1)) {
			// 盤面整理ツールに連携
			ArrangeToolLink arrange = getArrangeLink();
			// 全視点での整理
			String[][] every = getBoardArrange(arrange);
			// 自分が村人視点での整理
			String[][] self = getSelfBoardArrange(arrange, false);
			// 人外候補リストの更新
			SwfCandidates = addNonVillagerSideCandidates(arrange, self, SwfCandidates);
			if(!arrange.isBankruptcy(self)) {
				if(arrange.agentDisition(self, Role.VILLAGER).size() > 0) {
					for(Agent villager : arrange.agentDisition(self, Role.VILLAGER)) {
						if(villager != me) {
							enqueue1Talk(declaredContent(me, villager, Role.VILLAGER));
						}
					}
				}
				if(arrange.agentDisition(self, Role.SEER).size() > 0) {
					for(Agent seer : arrange.agentDisition(self, Role.SEER)) {
						enqueue1Talk(declaredContent(me, seer, Role.SEER));
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
				voteCandidate = randomSelect(aliveOthers);
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
				voteCandidate = randomSelect(voteRequestCounter.getRequestMap().values().stream()
						.filter(a -> a != me).collect(Collectors.toList()));
				System.out.println("+++");
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
			// 占い師が確定で死亡していて残り2縄の場合、妖狐が否定されてないプレイヤーからランダム
			List<Agent> seerAliveCandidates = currentGameInfo.getAliveAgentList().stream().filter(a -> arrange.agentCandidate(self, Role.SEER).contains(a)).collect(Collectors.toList());
			if(seerAliveCandidates.size() == 0 && arrange.getTotalState(self).get("count-expelled") == 2) {
				chooseVoteToFox(arrange, every, self, false);
				return true;
			}
			// 残り縄数が3のとき占い師COが2人以下の場合占い師を投票候補から外す、また人狼COが2人以下の場合も人狼を投票候補から外す. 逆に占い師COが4人以上の場合は占い師から投票する
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
			voteCandidates = voteCandidates.stream().filter(a -> arrange.agentCandidate(every, Role.SEER).contains(a)).collect(Collectors.toList());
			// 自視点で確定で偽の占い師がいた場合、その占い師に投票
			List<Agent> pretendSeer = voteCandidates.stream().filter(a -> !arrange.agentCandidate(self, Role.SEER).contains(a)).collect(Collectors.toList());
			if(pretendSeer.size() > 0) {
				voteCandidate = randomSelect(pretendSeer);
				return;
			}
		}
		// 確定村人陣営を投票候補から外す
		voteCandidates = voteCandidates.stream().filter(a -> !arrange.getDisitionSvList(self).contains(a)).collect(Collectors.toList());
		voteCandidate = randomSelect(voteCandidates);
		return;
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
