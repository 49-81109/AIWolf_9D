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
	
	/** 非背徳者の候補リスト(背徳者の可能性が非常に低いAgent, 妖狐は除く) */
	private List<Agent> notImmoralistCandidates = new ArrayList<>();
	
	/** 非背徳者候補に投票をする確率 */
	private static final int P_VoteNotRiCandidate = 63;
	
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
				if(aliveEnemies.size() > 0 && randP(P_VoteNotRiCandidate)) {
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
