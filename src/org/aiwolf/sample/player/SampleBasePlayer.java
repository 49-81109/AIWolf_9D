/**
 * SampleBasePlayer.java
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
import java.util.Random;
import java.util.stream.Collectors;

import org.aiwolf.client.lib.AgreeContentBuilder;
import org.aiwolf.client.lib.AndContentBuilder;
import org.aiwolf.client.lib.AttackContentBuilder;
import org.aiwolf.client.lib.AttackedContentBuilder;
import org.aiwolf.client.lib.BecauseContentBuilder;
import org.aiwolf.client.lib.ComingoutContentBuilder;
import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.DayContentBuilder;
import org.aiwolf.client.lib.DeclaredContentBuilder;
import org.aiwolf.client.lib.DeclaredNotContentBuilder;
import org.aiwolf.client.lib.DeclaredStatusContentBuilder;
import org.aiwolf.client.lib.DisagreeContentBuilder;
import org.aiwolf.client.lib.DivinationContentBuilder;
import org.aiwolf.client.lib.DivinedResultContentBuilder;
import org.aiwolf.client.lib.EstimateContentBuilder;
import org.aiwolf.client.lib.EstimateStatusContentBuilder;
import org.aiwolf.client.lib.GuardCandidateContentBuilder;
import org.aiwolf.client.lib.GuardedAgentContentBuilder;
import org.aiwolf.client.lib.IdentContentBuilder;
import org.aiwolf.client.lib.IfContentBuilder;
import org.aiwolf.client.lib.InquiryContentBuilder;
import org.aiwolf.client.lib.NotContentBuilder;
import org.aiwolf.client.lib.OrContentBuilder;
import org.aiwolf.client.lib.RequestContentBuilder;
import org.aiwolf.client.lib.TalkType;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.client.lib.VoteContentBuilder;
import org.aiwolf.client.lib.VotedContentBuilder;
import org.aiwolf.client.lib.XorContentBuilder;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Judge;
import org.aiwolf.common.data.Player;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.data.Status;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.data.Vote;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

/**
 * すべての役職のベースとなるクラス
 * 
 * @author otsuki
 */
public class SampleBasePlayer implements Player {

	/** このエージェント */
	Agent me;
	
	/** このエージェントの役職 */
	Role myRole;
	
	/** 配役 */
	Map<Role, Integer> roleCast = new HashMap<>();
	
	int playerNum;

	/** 日付 */
	int day;

	/** 再投票のときtrue */
	boolean isRevote;

	/** 最新のゲーム情報 */
	GameInfo currentGameInfo;
	
	/** 昨日のゲーム情報 */
	GameInfo yesterdayGameInfo;
	
	/** 最新の盤面整理ツールリンク */
	ArrangeToolLink currentArrangeToolLink;
	
	/** 村視点での整理データ */
	String[][] currentDataEvery;
	
	/** 自視点での整理データ(人外は自身が人外の視点) */
	String[][] currentDataSelf;
	
	/** 自身のCO役職視点での整理データ */
	Map<Agent, String[][]> currentDataCO = new HashMap<>();
	
	/** 以前の盤面整理ツールリンク */
	Map<Integer, ArrangeToolLink> totalLink = new HashMap<>();
	
	/** 以前の全体盤面整理データ */
	Map<Integer, String[][]> totalEveryData = new HashMap<>();
	
	/** 以前の全体盤面整理データ */
	Map<Agent, String[][]> yesterdayCoDataList = new HashMap<>();

	/** 自分以外の生存エージェント */
	List<Agent> aliveOthers;

	/** 追放されたエージェント */
	List<Agent> executedAgents = new ArrayList<>();

	/** 殺されたエージェント */
	List<Agent> killedAgents = new ArrayList<>();

	/** 犠牲者のエージェント */
	List<List<Agent>> victimAgents = new ArrayList<>();
	
	/** 妖狐追放による背徳者の道連れリスト */
	List<Agent> suicide = new ArrayList<>();
	
	/** 妖狐追放によって背徳者の道連れが起きた場合の道連れの日数 */
	int executedFoxDay = -1;
	
	/** 発言された占い結果報告のリスト */
	List<Judge> divinationList = new ArrayList<>();

	/** 発言された霊媒結果報告のリスト */
	List<Judge> identList = new ArrayList<>();
	
	/** 占い予告 */
	Map<Agent, List<Agent>> zoneMap = new HashMap<>();
	
	/** 誰かの占い予告先のプレイヤーのリスト */
	List<Agent> zoneContainList = new ArrayList<>();

	/** 発言用待ち行列 */
	private Deque<Content> talkQueue = new LinkedList<>();

	/** 投票先候補 */
	Agent voteCandidate;

	/** 宣言済み投票先候補 */
	Agent declaredVoteCandidate;

	/** カミングアウト状況 */
	Map<Agent, Role> comingoutMap = new HashMap<>();
	
	/** 人外カミングアウト状況 */
	Map<Agent, Role> NotVillagerSideCOMap = new HashMap<>();

	/** カミングアウトした日にち */
	Map<Agent, Integer> CODayMap = new HashMap<>();
	
	/** 占い結果を話したプレイヤーのリスト */
	List<Agent> speakResultList = new ArrayList<>();
	
	/** GameInfo.talkList読み込みのヘッド */
	int talkListHead;

	/** 推測理由マップ */
	EstimateReasonMap estimateReasonMap = new EstimateReasonMap();

	/** 投票理由マップ */
	VoteReasonMap voteReasonMap = new VoteReasonMap();

	/** 投票リクエストカウンタ */
	VoteRequestCounter voteRequestCounter = new VoteRequestCounter();
	
	/** 実際の投票先リスト */
	List<Vote> voteList = new ArrayList<>();
	
	/** 発話リスト */
	List<Content> talkedContent = new ArrayList<>();

	/** 確率の最大値 */
	static final int P_1 = 100;
	
	/** talk()のターン */
	int talkTurn;

	/**
	 * エージェントが生きているかどうかを返す
	 * 
	 * @param agent
	 * @return
	 */
	boolean isAlive(Agent agent) {
		return currentGameInfo.getStatusMap().get(agent) == Status.ALIVE;
	}

	/**
	 * エージェントが殺されたかどうかを返す
	 * 
	 * @param agent
	 * @return
	 */
	boolean isKilled(Agent agent) {
		return killedAgents.contains(agent);
	}

	/**
	 * エージェントがなんの役職をカミングアウトしたか、してない場合は村人が返される
	 * 
	 * @param agent
	 * @return
	 */
	Role getCoRole(Agent agent) {
		Role co = Role.VILLAGER;
		if(isCo(agent)) {
			co = comingoutMap.get(agent);
		}
		if(isNotVillagerSideCo(agent)) {
			co = NotVillagerSideCOMap.get(agent);
		}
		return co;
	}
	
	/**
	 * エージェントが村役職カミングアウトしたかどうかを返す
	 * 
	 * @param agent
	 * @return
	 */
	boolean isCo(Agent agent) {
		return comingoutMap.containsKey(agent);
	}
	
	/**
	 * エージェントが人外カミングアウトしたかどうかを返す
	 * 
	 * @param agent
	 * @return
	 */
	boolean isNotVillagerSideCo(Agent agent) {
		return NotVillagerSideCOMap.containsKey(agent);
	}

	/**
	 * 役職がカミングアウトされたかどうかを返す
	 * 
	 * @param role
	 * @return
	 */
	boolean isCo(Role role) {
		return comingoutMap.containsValue(role);
	}

	/** 生存している占い師CO者が全員占い結果を言ったかどうかを返す */
	boolean isAllSeerTalkResult() {
		List<Agent> aliveSeerCo = currentGameInfo.getAliveAgentList().stream().filter(a -> getCoRole(a) == Role.SEER).collect(Collectors.toList());
		return aliveSeerCo.size() == speakResultList.size();
	}
	
	/** 占い師CO数 */
	int getSeerCoNum() {
		return (int) currentGameInfo.getAgentList().stream().filter(a -> getCoRole(a) == Role.SEER).count();
	}
	
	/**
	 * リストからランダムに選んで返す
	 * 
	 * @param list
	 * @return
	 */
	<T> T randomSelect(List<T> list) {
		if (list.isEmpty()) {
			return null;
		} else {
			return list.get((int) (Math.random() * list.size()));
		}
	}

	@Override
	public String getName() {
		return "SampleBasePlayer";
	}

	@Override
	public void initialize(GameInfo gameInfo, GameSetting gameSetting) {
		yesterdayGameInfo = null;
		currentGameInfo = gameInfo;
		currentArrangeToolLink = null;
		day = -1;
		me = currentGameInfo.getAgent();
		aliveOthers = new ArrayList<>(currentGameInfo.getAliveAgentList());
		aliveOthers.remove(me);
		executedAgents.clear();
		killedAgents.clear();
		divinationList.clear();
		identList.clear();
		comingoutMap.clear();
		NotVillagerSideCOMap.clear();
		CODayMap.clear();
		estimateReasonMap.clear();
		victimAgents.clear();
		suicide.clear();
		voteList.clear();
		totalLink.clear();
		speakResultList.clear();
		totalEveryData.clear();
		executedFoxDay = -1;
		roleCast = gameSetting.getRoleNumMap();
		playerNum = gameSetting.getPlayerNum();
	}

	@Override
	public void update(GameInfo gameInfo) {
		GameInfo before = currentGameInfo;
		currentGameInfo = gameInfo;
		// 1日の最初の呼び出しはdayStart()の前なので何もしない
		if (currentGameInfo.getDay() == day + 1) {
			// 昨日のリンクを格納
			if(currentArrangeToolLink != null) {
				totalLink.put(day, currentArrangeToolLink);
			}
			if(currentDataEvery != null) {
				totalEveryData.put(day, currentDataEvery);
			}
			day = currentGameInfo.getDay();
			// 昨日のGameInfoを格納
			yesterdayGameInfo = before;
			/*
			yesterdayCoDataList.clear();
			for(Agent a : before.getAliveAgentList()) {
				if(currentArrangeToolLink != null) { 
					String[][] agentdata = getCOBoardArrange(currentArrangeToolLink, a, false);
					yesterdayCoDataList.put(a, agentdata);
				}
			}
			//*/
			currentArrangeToolLink = null;
			currentDataEvery = null;
			currentDataSelf = null;
			currentDataCO.clear();
			return;
		}
		// 2回目の呼び出し以降
		// （夜限定）追放されたエージェントを登録
		addExecutedAgent(currentGameInfo.getLatestExecutedAgent());
		
		// GameInfo.talkListからカミングアウト・占い報告・霊媒報告を抽出
		for (int i = talkListHead; i < currentGameInfo.getTalkList().size(); i++) {
			Talk talk = currentGameInfo.getTalkList().get(i);
			Agent talker = talk.getAgent();
			if (talker == me) {
				//continue;
			}
			Content content = new Content(talk.getText());

			// subjectがUNSPECの場合は発話者に入れ替える
			if (content.getSubject() == Content.UNSPEC) {
				content = replaceSubject(content, talker);
			}

			parseSentence(content);
		}

		talkListHead = currentGameInfo.getTalkList().size();
	}

	// 再帰的に文を解析する
	void parseSentence(Content content) {
		if (estimateReasonMap.put(content)) {
			return; // 推測文と解析できた
		}
		if (voteReasonMap.put(content)) {
			return; // 投票宣言と解析できた
		}
		switch (content.getTopic()) {
		case COMINGOUT:
			if(content.getRole() == Role.WEREWOLF || content.getRole() == Role.FOX || content.getRole() == Role.IMMORALIST) {
				NotVillagerSideCOMap.put(content.getTarget(), content.getRole());
			}
			else {
				comingoutMap.put(content.getTarget(), content.getRole());
				CODayMap.put(content.getTarget(), day);
			}
			return;
		case DIVINATION:
			if(zoneMap.containsKey(content.getSubject())) {
				List<Agent> zone = zoneMap.get(content.getSubject());
				zone.add(content.getTarget());
			}
			else {
				List<Agent> zone = new ArrayList<>();
				zone.add(content.getTarget());
				zoneMap.put(content.getSubject(), zone);
			}
			return;
		case IDENTIFIED:
			identList.add(new Judge(day, content.getSubject(), content.getTarget(), content.getResult()));
			if(!speakResultList.contains(content.getSubject())) {
				speakResultList.add(content.getSubject());
			}
			return;
		case OPERATOR:
			parseOperator(content);
			return;
		default:
			break;
		}
	}

	// 演算子文を解析する
	void parseOperator(Content content) {
		switch (content.getOperator()) {
		case BECAUSE:
		case IF:
			parseSentence(content.getContentList().get(1));
			break;
		case DAY:
//			System.out.println(content.getDay() + "day : cont [" + content.getContentList());
			if(content.getContentList().get(0).getTopic() == Topic.DIVINED) {
				divinationList.add(new Judge(content.getDay(), content.getContentList().get(0).getSubject(), content.getContentList().get(0).getTarget(), content.getContentList().get(0).getResult()));
				if(!speakResultList.contains(content.getContentList().get(0).getSubject())) {
					speakResultList.add(content.getContentList().get(0).getSubject());
				}
			}
			parseSentence(content.getContentList().get(0));
			break;
		case AND:
		case OR:
		case XOR:
			for (Content c : content.getContentList()) {
				parseSentence(c);
			}
			break;
		case REQUEST:
			if (voteRequestCounter.add(content)) {
				return; // 投票リクエストと解析できた
			}
			break;
		case INQUIRE:
			break;
		default:
			break;
		}
	}

	@Override
	public void dayStart() {
		isRevote = false;
		talkQueue.clear();
		declaredVoteCandidate = null;
		voteCandidate = null;
		talkListHead = 0;
		talkTurn = -1;
		voteReasonMap.clear();
		voteRequestCounter.clear();
		speakResultList.clear();
		talkedContent.clear();
		zoneMap.clear();
		zoneContainList.clear();
		// 前日に追放されたエージェントを登録
		addExecutedAgent(currentGameInfo.getExecutedAgent());
		
		// 妖狐が追放されて背徳者が道連れになった場合の道連れ対象を登録
		if(currentGameInfo.getSuicideImmoralistWithExecutedFox().size() > 0) {
			if(suicide.size() == 0) {
				for(Agent a : currentGameInfo.getSuicideImmoralistWithExecutedFox()) {
					suicide.add(a);
					aliveOthers.remove(a);
				}
			}
		}
		if(currentGameInfo.getExecutedFoxDay() != -1) {
			executedFoxDay = currentGameInfo.getExecutedFoxDay();
		}
		
		// 昨夜死亡した（襲撃された）エージェントを登録
		if (day != 0) {
			List<Agent> dayVic = new ArrayList<>();
			for(Agent a : currentGameInfo.getLastDeadAgentList()) {
				addKilledAgent(a, dayVic);
			}
			victimAgents.add(dayVic);
		}
		if(currentGameInfo.getVoteList() != null) {
			for(Vote vote : currentGameInfo.getVoteList()) {
				voteList.add(vote);
			}
		}
	}

	private void addExecutedAgent(Agent executedAgent) {
		if (executedAgent != null) {
			aliveOthers.remove(executedAgent);
			if (!executedAgents.contains(executedAgent)) {
				executedAgents.add(executedAgent);
			}
		}
	}

	private void addKilledAgent(Agent killedAgent, List<Agent> dayVic) {
		if (killedAgent != null) {
			aliveOthers.remove(killedAgent);
			if (!killedAgents.contains(killedAgent)) {
				killedAgents.add(killedAgent);
			}
			if (!dayVic.contains(killedAgent)) {
				dayVic.add(killedAgent);
			}
		}
	}

	/** あるAgentが投票したことのあるAgentのリスト */
	List<Agent> getVoteTarget(Agent voter) {
		List<Agent> voteTarget = new ArrayList<>();
		//System.out.println(voteList.size());
		//System.out.println(currentGameInfo.getVoteList().size());
		for(Vote vote : voteList) {
			//System.out.println(vote.getDay() + ":" + vote.getAgent() + "-> " + vote.getTarget());
			if(vote.getAgent() == voter) {
				if(!voteTarget.contains(vote.getTarget())) {
					voteTarget.add(vote.getTarget());
					
				}
			}
		}
		return voteTarget;
	}
	
	/** Agentがその日投票した先 */
	Agent getVoteTarget(Agent voter, int day) {
		for(Vote vote : currentGameInfo.getLatestVoteList()) {
			if(vote.getAgent() == voter && vote.getDay() == day) {
				return vote.getTarget();
			}
		}
		return null;
	}
	
	/** あるAgentにその日投票したAgentのリスト */
	List<Agent> getVoteAgent(Agent voted, int day) {
		List<Agent> voteAgent = new ArrayList<>();
		for(Vote vote : currentGameInfo.getLatestVoteList()) {
			if(vote.getTarget() == voted && vote.getDay() == day) {
				voteAgent.add(vote.getAgent());
			}
		}
		return voteAgent;
	}
	
	/** あるAgentの得票数 */
	int getVotedCount(Agent voted, int day) {
		List<Agent> voteAgent = new ArrayList<>();
		int votedCount = 0;
		for(Vote vote : currentGameInfo.getLatestVoteList()) {
			if(vote.getTarget() == voted && vote.getDay() == day) {
				if(voteAgent.contains(vote.getAgent())) {
					votedCount++;
				}
				voteAgent.add(vote.getAgent());
			}
		}
		return votedCount;
	}
	
	Agent getMaxVoted(int day) {
		
		Agent max = voteList.get(0).getTarget();
		for(Agent a : currentGameInfo.getAgentList()) {
			if(getVotedCount(max, day) < getVotedCount(a, day)) {
				max = a;
			}
		}
		return max;
	}
	
	/** あるAgentが追放したいと思っているAgentのリスト */
	List<Agent> getWantExecuteTarget(Agent agent) {
		List<Agent> wantExe = getVoteTarget(agent);
		for(Agent a : voteRequestCounter.getRequestMap().keySet()) {
			if(a == agent && !wantExe.contains(voteRequestCounter.getRequestMap().get(a))) {
				wantExe.add(voteRequestCounter.getRequestMap().get(a));
			}
		}
		return wantExe;
	}
	
	/** 人外候補リストの追加(originは昨日時点での人外候補リスト)<br>
	 *  最初の呼び出しのときはoriginは new ArrayList&lt;Agent&gt;() で呼ぶこと
	 */
	List<Agent> addNonVillagerSideCandidates(ArrangeToolLink arrange, String[][] data, List<Agent> origin) {
		List<Agent> Swf = new ArrayList<>();
		// もとの人外候補リストをコピー
		for(Agent a : origin) {
			Swf.add(a);
		}
		// 自視点での確定人外を格納
		for(Agent a : arrange.getDisitionSwfList(data)) {
			if(!Swf.contains(a)) {
				Swf.add(a);
			}
		}
		// 人外COしたAgentを格納
		for(Agent a : NotVillagerSideCOMap.keySet()) {
			if(!Swf.contains(a)) {
				Swf.add(a);
			}
		}
		if(day > 1) {
			/*
			if(totalLink.containsKey(day - 1)) {
				// 昨日時点のデータ
				ArrangeToolLink ye = totalLink.get(day - 1);
				for(Agent agent : currentGameInfo.getAgentList()) {
					if(yesterdayCoDataList.containsKey(agent)) {
						// Agent視点での確定村人陣営
						List<Agent> DisitionSv = ye.getDisitionSvList(yesterdayCoDataList.get(agent));
						// Agent視点での確定人狼
//						List<Agent> DisitionRw = ye.getDisitionRwList(yesterdayCoDataList.get(agent));					
						// もし昨日の投票先が確定村人陣営の場合、そのAgentを人外リストに追加
						if(DisitionSv.contains(getVoteTarget(agent, day - 1)) && !arrange.getDisitionSvList(data).contains(agent)) {
							Swf.add(agent);
						}
					}
				}
			}
			//*/
			//*
			if(totalEveryData.containsKey(day - 1)) {
				ArrangeToolLink ye = totalLink.get(day - 1);
				List<Agent> DisitionSv = ye.getDisitionSvList(totalEveryData.get(day - 1));
				for(Agent agent : currentGameInfo.getAgentList()) {
					if(DisitionSv.contains(getVoteTarget(agent, day - 1)) && !arrange.getDisitionSvList(data).contains(agent)) {
						Swf.add(agent);
					}
				}
			}
			//*/
		}
		return Swf;
	}
	
	/** 占い師CO者[seer]の占い結果が[species]のAgentのリスト<br>speciesがANYなら占い先すべてを返す, 非占い師CO者の場合空のリストが返される */
	List<Agent> getDivinedResultList(Agent seer, Species species) {
		List<Agent> divineList = new ArrayList<>();
		for(Judge j : divinationList) {
			if(j.getAgent() == seer) {
				if(species == Species.ANY) {
					divineList.add(j.getTarget());
				}
				else {
					if(species == Species.HUMAN) {
						if(j.getResult() == Species.HUMAN) {
							divineList.add(j.getTarget());
						}
					}
					else {
						if(j.getResult() == Species.WEREWOLF) {
							divineList.add(j.getTarget());
						}
					}
				}
			}
		}
		return divineList;
	}
	
	/**
	 * 投票先候補を選びvoteCandidateにセットする
	 * 
	 * <blockquote>talk()から呼ばれる</blockquote>
	 */
	void chooseVoteCandidate() {
	}

	/**
	 * 投票先候補を選びvoteCandidateにセットする
	 * 
	 * <blockquote>vote()から呼ばれる</blockquote>
	 */
	void chooseFinalVoteCandidate() {
	}

	@Override
	public String talk() {
		talkTurn++;
		chooseVoteCandidate();
		if (voteCandidate != null && voteCandidate != declaredVoteCandidate) {
			// 話すことがない場合は投票先を宣言
			if (talkQueue.isEmpty()) {
				Content vote = voteContent(me, voteCandidate);
				Content request = requestContent(me, Content.ANY, voteContent(Content.ANY, voteCandidate));
				Content reason = voteReasonMap.getReason(me, voteCandidate);
				Content and = andContent(me, vote, request);
				if (reason != null) {
					enqueueTalk(becauseContent(me, reason, and));
				}
				enqueueTalk(vote);
				enqueueTalk(request);
				declaredVoteCandidate = voteCandidate;
			}
		}
		return dequeueTalk();
	}

	void enqueueTalk(Content content) {
		if (content.getSubject() == Content.UNSPEC) {
			talkQueue.offer(replaceSubject(content, me));
		} else {
			talkQueue.offer(content);
		}
	}
	
	void enqueue1Talk(Content content) {
		boolean isAlreadyTalked = false;
		for(Content c : talkedContent) {
			if(content.equals(c)) {
				isAlreadyTalked = true;
			}
		}
		if(!isAlreadyTalked) {
			talkedContent.add(content);
			enqueueTalk(content);
		}
	}

	String dequeueTalk() {
		if (talkQueue.isEmpty()) {
			return Talk.SKIP;
		}
		Content content = talkQueue.poll();
		if (content.getSubject() == me) {
			return Content.stripSubject(content.getText());
		}
		return content.getText();
	}

	@Override
	public String whisper() {
		return null;
	}

	@Override
	public Agent vote() {
		chooseFinalVoteCandidate();
		isRevote = true;
		if(voteCandidate == null) {
//			System.out.println("******************************************************** null : " + me.getName());
		}
		else if (voteCandidate == me) {
//			System.out.println("******************************************************** self : " + me.getName());
		}
		return voteCandidate;
	}

	@Override
	public Agent attack() {
		return null;
	}

	@Override
	public Agent divine() {
		return null;
	}

	@Override
	public Agent guard() {
		return null;
	}
	
	@Override
	public void finish() {
	}

	/** 占い(霊能)結果の整理 */
	public void resultSort() {
		// 占い結果の整理
		List<Judge> sortedDivineList = new ArrayList<>();
		/*
		System.out.println("----->>");
		for(Judge j : divinationList) {
			System.out.println("seer : " + j.getAgent().getName() + "→ " + j.getTarget().getName() + " : day " + j.getDay());
		}
		System.out.println("-----");
		//*/
		// 重複結果の削除
		for(Judge j : divinationList) {
			boolean isSame = false;
			for(Judge bef : sortedDivineList) {
				if(j.getDay() == bef.getDay() && j.getAgent() == bef.getAgent() && j.getTarget() == bef.getTarget()) {
					isSame = true;
					break;
				}
			}
			if(!isSame) {
				sortedDivineList.add(j);
			}
		}
		List<Judge> sortedDayDivineList = new ArrayList<>();
		Map<Agent, Integer> seerResult = new HashMap<>();
		for(Judge j : sortedDivineList) {
			Judge tmp;
			if(seerResult.containsKey(j.getAgent())) {
				int day = seerResult.get(j.getAgent());
				seerResult.remove(j.getAgent());
				seerResult.put(j.getAgent(), day + 1);
				tmp = new Judge(day + 1, j.getAgent(), j.getTarget(), j.getResult());
			}
			else {
				seerResult.put(j.getAgent(), 1);
				tmp = new Judge(1, j.getAgent(), j.getTarget(), j.getResult());
			}
			sortedDayDivineList.add(tmp);
		}
		divinationList = sortedDayDivineList;
	}
	
	// 占い師別に結果を取得(非占い師CO者は空のリストが返される)
	List<Judge> getSeerResult(Agent seer) {
		List<Judge> seerResult = new ArrayList<>();
		for(Judge j : divinationList) {
			if(j.getAgent() == seer) {
				seerResult.add(j);
			}
		}
		return seerResult;
	}
	
	List<Judge> getDivinationList() {
		List<Judge> seerResult = new ArrayList<>();
		for(Judge j : divinationList) {
			seerResult.add(j);
		}
		return seerResult;
	}
	
	/** 盤面整理ツールと接続 */
	public ArrangeToolLink getArrangeLink() {
		// すでにその日の盤面整理リンクと接続している場合
		if(currentArrangeToolLink != null) {
			// 同日に占い師CO数が変わった場合は新しく盤面整理ツールを起動させる
			if(currentArrangeToolLink.getSeerCONum() != getSeerCoNum()) {
				currentArrangeToolLink = new ArrangeToolLink(this);
				currentDataEvery = null;
				currentDataSelf = null;
				currentDataCO.clear();
				// 占い師CO前のデータは削除
				totalLink.remove(day);
				// 新しくデータを格納
				totalLink.put(day, currentArrangeToolLink);
				getTotalBoardArrange();
				return currentArrangeToolLink;
			}
			// データが変わらない場合は予め持っているデータを返す
			return currentArrangeToolLink;
		}
		
		// その日の盤面整理リンクがない場合は新たに作成
		currentArrangeToolLink = new ArrangeToolLink(this);
		currentDataEvery = null;
		currentDataSelf = null;
		currentDataCO.clear();
		getTotalBoardArrange();
		// その日でのデータを格納
		totalLink.put(day, currentArrangeToolLink);
		return currentArrangeToolLink;
	}
	
	void getTotalBoardArrange() {
		// 生存者のうち最小の番号のプレイヤーのみの盤面情報を表示(必ず1日1回のみ盤面が表示されるように)
		boolean isSmallestAliveId = true;
		for(int i = 1; i < me.getAgentIdx(); i++) {
			if(isAlive(Agent.getAgent(i))) {
				isSmallestAliveId = false;
			}
		}
		if(isSmallestAliveId) {
			currentArrangeToolLink.printInput();
			String[][] every = getBoardArrange(currentArrangeToolLink);
			System.out.println("Total");
			currentArrangeToolLink.printroleCandidate(every);
		}
	}
	
	/** 村視点の整理実行 */
	public String[][] getBoardArrange(ArrangeToolLink arrange) {
		if(currentDataEvery != null) {
			return currentDataEvery;
		}
		String[][] data = arrange.copyData(arrange.executeArrangement());
		currentDataEvery = data;
		return data;
	}
	
	/** 真視点の整理実行 */
	public String[][] getSelfBoardArrange(ArrangeToolLink arrange, boolean isPrint) {
		if(currentDataSelf != null) {
			return currentDataSelf;
		}
		String[][] data = arrange.copyData(arrange.executeArrangement(me, myRole));
		currentDataSelf = data;
		if(isPrint) {
			System.out.println("[" + me.getAgentIdx() + "]" + me.getName() + " → " + myRole + "視点");
		}
		return data;
	}
	
	/** CO役職視点の整理実行 (COなしの場合は村人視点、人外COは除く) */
	public String[][] getCOBoardArrange(ArrangeToolLink arrange, Agent agent, boolean isPrint) {
		if(currentDataCO != null) {
			if(currentDataCO.containsKey(agent) && currentArrangeToolLink == arrange) {
				return currentDataCO.get(agent);
			}
		}
		Role role = Role.VILLAGER;
		if(!isNotVillagerSideCo(agent)) {
			if(isCo(agent)) {
				if(CODayMap.get(agent) <= arrange.getDay()) {
					role = getCoRole(agent);
				}
			}
		}
		String[][] data = getBoardArrange(arrange, agent, role, isPrint);
		currentDataCO.put(agent, data);
		return data;
	}
	
	/** 各視点で整理実行 */
	public String[][] getBoardArrange(ArrangeToolLink arrange, Agent agent, Role role, boolean isPrint) {
		String[][] data = arrange.copyData(arrange.executeArrangement(agent, role));
		if(isPrint) {
			System.out.println("[" + agent.getAgentIdx() + "]" + agent.getName() + " → " + role + "視点");
		}
		return data;
	}
	
	/** 追加条件で整理 */
	public String[][] getBoardArrange(ArrangeToolLink arrange, String[][] data, Agent agent, Role role) {
		String[][] data2 = arrange.copyData(arrange.executeArrangement(data, agent, role));
		return data2;
	}
	
	/** 追加条件で整理 */
	public String[][] getBoardArrange(ArrangeToolLink arrange, String[][] data, Agent agent, Role role, boolean isDisi) {
		String[][] data2 = arrange.copyData(arrange.executeArrangement(data, agent, role, isDisi));
		return data2;
	}

	/** 役職候補の表示 */
	public void printRoleCandidate(ArrangeToolLink arrange, String[][] data) {
		arrange.printroleCandidate(data);
	}
	
	/** p%の確率の実行 */
	public boolean randP(int p) {
		Random rand = new Random();
		return rand.nextInt(P_1) < p;
	}
	
	/** 生存者のみをフィルタ */
	public List<Agent> toAliveList(List<Agent> list) {
		return list.stream().filter(a -> currentGameInfo.getAliveAgentList().contains(a)).collect(Collectors.toList());
	}
	
	/** Agentリストのシャローコピー */
	public List<Agent> copyAgentList(List<Agent> list) {
		List<Agent> cp = new ArrayList<>();
		for(Agent a : list) {
			cp.add(a);
		}
		return cp;
	}
	
	static Content replaceSubject(Content content, Agent newSubject) {
		if (content.getTopic() == Topic.SKIP || content.getTopic() == Topic.OVER) {
			return content;
		}
		if (newSubject == Content.UNSPEC) {
			return new Content(Content.stripSubject(content.getText()));
		} else {
			return new Content(newSubject + " " + Content.stripSubject(content.getText()));
		}
	}
	
	// 発話生成を簡略化するためのwrapper
	
	/** 発言の同意 */
	static Content agreeContent(Agent subject, TalkType talkType, int talkDay, int talkID) {
		return new Content(new AgreeContentBuilder(subject, talkType, talkDay, talkID));
	}

	/** 発言の非同意 */
	static Content disagreeContent(Agent subject, TalkType talkType, int talkDay, int talkID) {
		return new Content(new DisagreeContentBuilder(subject, talkType, talkDay, talkID));
	}

	/** 投票宣言 */
	static Content voteContent(Agent subject, Agent target) {
		return new Content(new VoteContentBuilder(subject, target));
	}

	/** 投票した報告 */
	static Content votedContent(Agent subject, Agent target) {
		return new Content(new VotedContentBuilder(subject, target));
	}

	/** 襲撃宣言(襲撃予告) */
	static Content attackContent(Agent subject, Agent target) {
		return new Content(new AttackContentBuilder(subject, target));
	}

	/** 襲撃した報告 */
	static Content attackedContent(Agent subject, Agent target) {
		return new Content(new AttackedContentBuilder(subject, target));
	}
	
	/** 襲撃された報告 */
	static Content attackedContent(Agent target) {
		return new Content(new AttackedContentBuilder(target));
	}

	/** 護衛宣言(9Dでは狩人いないから多分使わん) */
	static Content guardContent(Agent subject, Agent target) {
		return new Content(new GuardCandidateContentBuilder(subject, target));
	}

	/** 護衛した報告(9Dでは狩人いないから多分使わん) */
	static Content guardedContent(Agent subject, Agent target) {
		return new Content(new GuardedAgentContentBuilder(subject, target));
	}

	/** 推定 ([target]の役職は[role]だと思う) */
	static Content estimateContent(Agent subject, Agent target, Role role) {
		return new Content(new EstimateContentBuilder(subject, target, role));
	}
	
	/** 断言 ([target]の役職は[role]だ) */
	static Content declaredContent(Agent subject, Agent target, Role role) {
		return new Content(new DeclaredContentBuilder(subject, target, role));
	}
	
	/** 否定断言 ([target]の役職は[role]ではない) */
	static Content declaredNotContent(Agent subject, Agent target, Role role) {
		return new Content(new DeclaredNotContentBuilder(subject, target, role));
	}

	/** 状態推定 ([role]は 生存している/死亡している と思う) */
	static Content estimateStatusContent(Agent subject, Role role, Status status) {
		return new Content(new EstimateStatusContentBuilder(subject, role, status));
	}
	
	/** 状態断言 ([role]は確定で 生存している/死亡している) */
	static Content declaredStatusContent(Agent subject, Role role, Status status) {
		return new Content(new DeclaredStatusContentBuilder(subject, role, status));
	}
	
	/** CO */
	static Content coContent(Agent subject, Agent target, Role role) {
		return new Content(new ComingoutContentBuilder(subject, target, role));
	}

	/** 要求 ([Agent]に対して[content]をすることを要求) */
	static Content requestContent(Agent subject, Agent target, Content content) {
		return new Content(new RequestContentBuilder(subject, target, content));
	}

	static Content inquiryContent(Agent subject, Agent target, Content content) {
		return new Content(new InquiryContentBuilder(subject, target, content));
	}

	/** 占い宣言(占い予告) */
	static Content divinationContent(Agent subject, Agent target) {
		return new Content(new DivinationContentBuilder(subject, target));
	}

	/** 占い結果報告 */
	static Content divinedContent(Agent subject, Agent target, Species result) {
		return new Content(new DivinedResultContentBuilder(subject, target, result));
	}

	/** 霊能結果報告(9Dでは霊能者いないから使わない) */
	static Content identContent(Agent subject, Agent target, Species result) {
		return new Content(new IdentContentBuilder(subject, target, result));
	}

	static Content andContent(Agent subject, Content... contents) {
		return new Content(new AndContentBuilder(subject, contents));
	}
	static Content andContent(Agent subject, List<Content> contents) {
		return new Content(new AndContentBuilder(subject, contents));
	}

	static Content orContent(Agent subject, Content... contents) {
		return new Content(new OrContentBuilder(subject, contents));
	}
	static Content orContent(Agent subject, List<Content> contents) {
		return new Content(new OrContentBuilder(subject, contents));
	}

	static Content xorContent(Agent subject, Content content1, Content content2) {
		return new Content(new XorContentBuilder(subject, content1, content2));
	}

	static Content notContent(Agent subject, Content content) {
		return new Content(new NotContentBuilder(subject, content));
	}

	static Content dayContent(Agent subject, int day, Content content) {
		return new Content(new DayContentBuilder(subject, day, content));
	}

	static Content becauseContent(Agent subject, Content reason, Content action) {
		return new Content(new BecauseContentBuilder(subject, reason, action));
	}
	
	static Content ifContent(Agent subject, Content reason, Content action) {
		return new Content(new IfContentBuilder(subject, reason, action));
	}
}
