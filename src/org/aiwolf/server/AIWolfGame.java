/**
 * AIWolfGame.java
 *
 * Copyright (c) 2014 人狼知能プロジェクト
 */
package org.aiwolf.server;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.aiwolf.client.lib.Content;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Guard;
import org.aiwolf.common.data.Judge;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.data.Status;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.data.Team;
import org.aiwolf.common.data.Vote;
import org.aiwolf.common.net.GameSetting;
import org.aiwolf.common.util.Counter;
import org.aiwolf.server.net.GameServer;
import org.aiwolf.server.util.FileGameLogger;
import org.aiwolf.server.util.GameLogger;

/**
 * Game Class of AI Wolf Contest
 * @author tori and otsuki
 *
 */
public class AIWolfGame {


	protected Random rand;

	/**
	 * Settings of the game
	 */
	protected GameSetting gameSetting;

	/**
	 * server to connect clients
	 */
	protected GameServer gameServer;

	/**
	 *
	 */
	protected Map<Integer, GameData> gameDataMap;

	/**
	 *
	 */
	protected GameData gameData;

	/**
	 * Show console log?
	 */
	protected boolean isShowConsoleLog = true;


	/**
	 * ログを記録するファイル
	 */
//	protected File logFile;
	
	// 引き分け処理
	private boolean isDraw;

	/**
	 * Logger
	 */
	protected GameLogger gameLogger;

	/**
	 * Name of Agents
	 */
	protected Map<Agent, String> agentNameMap;
	List<Agent> agents;

	/**
	 *
	 */
	public AIWolfGame(GameSetting gameSetting, GameServer gameServer) {
		rand = new Random();
		this.gameSetting = gameSetting;
		this.gameServer = gameServer;

//		gameLogger = AiWolfLoggerFactory.getSimpleLogger(this.getClass().getSimpleName());
	}


	/**
	 * @return logFile
	 */
//	public File getLogFile() {
//		return logFile;
//	}


	/**
	 * @param logFile セットする logFile
	 * @throws IOException
	 */
	public void setLogFile(File logFile) throws IOException {
//		this.logFile = logFile;
		gameLogger = new FileGameLogger(logFile);
	}

	/**
	 * set GameLogger
	 * @param gameLogger
	 */
	public void setGameLogger(GameLogger gameLogger){
		this.gameLogger = gameLogger;
	}

	/**
	 * get GameLogger
	 */
	public GameLogger getGameLogger(){
		return this.gameLogger;
	}


	/**
	 * Set Random Class
	 * @param rand
	 */
	public void setRand(Random rand) {
		this.rand = rand;
	}

	/**
	 * Initialize Game
	 */
	protected void init(){
		gameDataMap = new TreeMap<Integer, GameData>();
//		System.out.println("init AIWolfGame");
		gameData = new GameData(gameSetting);
		agentNameMap = new HashMap<Agent, String>();
		gameServer.setGameData(gameData);
		isDraw = false;
		List<Agent> agentList = gameServer.getConnectedAgentList();
		agents = agentList;

		if(agentList.size() != gameSetting.getPlayerNum()){
			throw new IllegalPlayerNumException("Player num is "+gameSetting.getPlayerNum()+" but connected agent is "+agentList.size());
		}

		Collections.shuffle(agentList, rand);

		Map<Role, List<Agent>> requestRoleMap = new HashMap<Role, List<Agent>>();
		for(Role role:Role.values()){
			requestRoleMap.put(role, new ArrayList<Agent>());
		}
		List<Agent> noRequestAgentList = new ArrayList<Agent>();
		for(Agent agent:agentList){
			if(gameSetting.isEnableRoleRequest()) {
				Role requestedRole = gameServer.requestRequestRole(agent);
				if(requestedRole != null){
					if(requestRoleMap.get(requestedRole).size() < gameSetting.getRoleNum(requestedRole)){
						requestRoleMap.get(requestedRole).add(agent);
					}
					else{
						noRequestAgentList.add(agent);
					}
	//				System.out.println(agent+" request "+requestedRole);
				}
				else{
					noRequestAgentList.add(agent);
	//				System.out.println(agent+" request no role");
				}
			}
			else {
				noRequestAgentList.add(agent);
			}
		}


		for(Role role:Role.values()){
			List<Agent> requestedAgentList = requestRoleMap.get(role);
			for(int i = 0; i < gameSetting.getRoleNum(role); i++){
				if(requestedAgentList.isEmpty()){
					gameData.addAgent(noRequestAgentList.remove(0), Status.ALIVE, role);
				}
				else{
					gameData.addAgent(requestedAgentList.remove(0), Status.ALIVE, role);
				}
			}
		}

		gameDataMap.put(gameData.getDay(), gameData);

		gameServer.setGameSetting(gameSetting);
		
		for(Agent agent:agentList){
			gameServer.init(agent);
			String requestName = gameServer.requestName(agent);
			agentNameMap.put(agent, requestName);
			gameData.agentNameMap.put(agent, requestName);
			//System.out.println(requestName + " -> AIWolfGame 209");
		}
	}


	/**
	 * Start game
	 */
	public void start(){
		try{
			init();

		//		System.out.printf("%d-%d\n", getAliveHumanList().size(), getAliveWolfList().size());
			while(!isGameFinished()){
				consoleLog();

				day();
				night();
				if(gameLogger != null){
					gameLogger.flush();
				}
			}
			consoleLog();
			finish();

			if(isShowConsoleLog){
				if(isDraw) {
					System.out.println("This game is draw.");
				}
				else {
					System.out.println("Winner:" + getWinner());
				}
			}
		//		for(Agent agent:gameData.getAgentList()){
		//			GameInfo gameInfo = gameData.getGameInfo(agent);
		////			System.out.println(JSON.encode(gameInfo));
		//			break;
		//		}
		}catch(LostClientException e){
			if(gameLogger != null){
				gameLogger.log("Lost Connection of "+e.getAgent());
			}
			throw e;
		}
	}

	public void finish(){
		if(gameLogger != null){
			for(Agent agent:new TreeSet<Agent>(gameData.getAgentList())){
				gameLogger.log(String.format("%d,status,%d,%s,%s,%s", gameData.getDay(), agent.getAgentIdx(),gameData.getRole(agent), gameData.getStatus(agent), agentNameMap.get(agent)));
			}
			if(isDraw) {
				gameLogger.log(String.format("%d,result,%d,%d,%d,DRAW", gameData.getDay(), getAliveHumanList().size(), getAliveWolfList().size(), getAliveFoxList().size()));
			}
			else {
				gameLogger.log(String.format("%d,result,%d,%d,%d,%s", gameData.getDay(), getAliveHumanList().size(), getAliveWolfList().size(), getAliveFoxList().size(), getWinner()));
			}
			gameLogger.close();
		}

		for(Agent agent:gameData.getAgentList()){
//			System.out.println("Send finish to "+agent);
			gameServer.finish(agent);
		}
/*		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}*/
	}

	/**
	 * Get won team.
	 * if game not finished, return null
	 * @return
	 */
	public Team getWinner(){
		int humanSide = 0;
		int wolfSide = 0;
		int otherSide = 0;
		for(Agent agent:gameData.getAgentList()){
			if(gameData.getStatus(agent) == Status.DEAD){
				continue;
			}


			if(gameData.getRole(agent).getTeam() == Team.OTHERS){
				otherSide++;
			}
			if(gameData.getRole(agent).getSpecies() == Species.HUMAN && gameData.getRole(agent) != Role.FOX){ //***
				humanSide++;
			}
			else if(gameData.getRole(agent) != Role.FOX){
				wolfSide++;
			}
		}
		if(wolfSide == 0){
			if(otherSide > 0){
				return Team.OTHERS;
			}
			return Team.VILLAGER;
		}
		else if(humanSide <= wolfSide){
			if(otherSide > 0){
				return Team.OTHERS;
			}
			return Team.WEREWOLF;
		}
		else{
			return null;
		}

	}

	private void consoleLog() {

		if(!isShowConsoleLog){
			return;
		}

		GameData yesterday = gameData.getDayBefore();
		for(Agent a : agentNameMap.keySet()){
			gameData.agentNameMap.put(a, agentNameMap.get(a).toString());
			a.setAgentName(agentNameMap.get(a).toString());
		}

		System.out.println("=============================================");
		if(yesterday != null){
			System.out.printf("Day %02d\n", yesterday.getDay());
			System.out.println("========talk========");
			for(Talk talk:yesterday.getTalkList()){
				//System.out.println(talk);
				Pattern p = Pattern.compile("(Agent\\[[0-9]+\\])");
				Pattern p2 = Pattern.compile("(Agent\\[[0-9]+\\].*)");
				String context = talk.getText();
				Matcher m = p.matcher(context);
				Matcher m2 = p2.matcher(context);
				List<String> results = new ArrayList<>();
				while (m.find()) {
				    results.add("[" + Integer.parseInt(m.group().substring(6, m.group(1).length() - 1)) + "]" + Agent.getAgent(Integer.parseInt(m.group().substring(6, m.group(1).length() - 1))).getName());
				    
				}
				//System.out.println(results);
				m = p.matcher(context);
				int c = 0;
				while(m2.find()) {
					if(results.size() > c) {
						context = context.substring(0, context.length() - m2.group().length()) + results.get(c) + m2.group().substring(9);
					}
					//System.out.println(context + "^^^");
					m2 = p2.matcher(context);
					c++;
				}
				//System.out.println(context);
				//System.out.println(c);
				System.out.printf("Day%02d %02d[%03d]\t[%d]%s\t%s\n", talk.getDay(), talk.getTurn(), talk.getIdx(), talk.getAgent().getAgentIdx(), getAgentName(talk.getAgent()), context);
			}
/*			System.out.println("========Whisper========");
			for(Talk whisper:yesterday.getWhisperList()){
				//System.out.println(whisper);
				System.out.printf("Day%02d %02d[%03d]\t%d, %s\t%s\n", whisper.getDay(), whisper.getTurn(), whisper.getIdx(), whisper.getAgent().getAgentIdx(), getAgentName(whisper.getAgent()), whisper.getText());
			}
//*/
			if(yesterday.getDay() > 0) {
				System.out.println("========Vote========");
			}
			Map<Agent, Integer> votedNum = new HashMap<>();
			
			
			for (int i = 0; i <= gameSetting.getMaxRevote(); i++) {
				if(yesterday.getRevoteList().containsKey(i)) {
					if(i > 0) {
						System.out.println("-------Revote-------");
					}
					// 得票数の初期化
					for(Agent a : gameData.getAgentList()) {
						votedNum.put(a, 0);
					}
					for(Vote vote:yesterday.getRevoteList().get(i)){
						int addNum = votedNum.get(vote.getTarget());
						votedNum.remove(vote.getTarget());
						votedNum.put(vote.getTarget(), addNum + 1);
					}
					for(Vote vote:yesterday.getRevoteList().get(i)){
						System.out.printf("Vote:[%d]%s (%d voted) -> [%d]%s\n", vote.getAgent().getAgentIdx(), getAgentName(vote.getAgent()), votedNum.get(vote.getAgent()), vote.getTarget().getAgentIdx(), getAgentName(vote.getTarget()));
						//System.out.printf("Vote:[%d]%s (%d voted) -> [%d]%s\n", vote.getAgent().getAgentIdx(), getAgentName(vote.getAgent()), votedNum.get(vote.getAgent()), vote.getTarget().getAgentIdx(), vote.getTarget().getName());
					}
				}
			}


			Judge divine = yesterday.getDivine();
			if(yesterday.getDay() > 0) {
				System.out.println("========Execute========");
			}
			if(yesterday.getExecuted() != null) {
				System.out.printf("[%d]%s executed\n", yesterday.getExecuted().getAgentIdx(), getAgentName(yesterday.getExecuted()));
			}
			if(yesterday.getSuicideImmoralistWithExecutedFoxList().size() > 0) {
				for(Agent a : yesterday.getSuicideImmoralistWithExecutedFoxList()) {
					System.out.printf("[%d]%s suicided after the death of fox ( [%d]%s )\n", a.getAgentIdx(), getAgentName(a), yesterday.getExecuted().getAgentIdx(), getAgentName(yesterday.getExecuted()));
				}
			}
			
			List<Agent> alivesLastnight = new ArrayList<>();
			for(Agent agent : gameData.getAgentList()) {
				if(gameData.getStatus(agent) == Status.ALIVE) {
					alivesLastnight.add(agent);
				}
				else if(yesterday.getAttackedDead() == agent) {
					alivesLastnight.add(agent);
				}
				else if(yesterday.getCursedFox() == agent) {
					alivesLastnight.add(agent);
				}
				else if(yesterday.getSuicideImmoralistWithCursedFoxList().contains(agent)) {
					alivesLastnight.add(agent);
				}
			}
			int wolfCount = 0;
			int humanCount = 0;
			for(Agent agent : alivesLastnight) {
				if(gameData.getRole(agent) == Role.WEREWOLF) {
					wolfCount++;
				}
				if((gameData.getRole(agent) == Role.VILLAGER || gameData.getRole(agent) == Role.SEER || gameData.getRole(agent) == Role.IMMORALIST)) {
					humanCount++;
				}
			}
//			System.out.println("wolf : " + wolfCount + ", human : " + humanCount + ", total : " + alivesLastnight.size());
			if(wolfCount > 0 && humanCount > wolfCount) {
				System.out.println("========Actions========");
				if(divine != null){
					System.out.printf("[%d]%s divine [%d]%s. Result is %s\n", divine.getAgent().getAgentIdx(), getAgentName(divine.getAgent()), divine.getTarget().getAgentIdx(), getAgentName(divine.getTarget()), divine.getResult());
				}
				
//				System.out.println("Attack Vote Result");
				for(Vote vote:yesterday.getAttackVoteList()){
					System.out.printf("AttackVote:[%d]%s -> [%d]%s\n", vote.getAgent().getAgentIdx(), getAgentName(vote.getAgent()), vote.getTarget().getAgentIdx(), getAgentName(vote.getTarget()));
				}
				
				Guard guard = yesterday.getGuard();
				List<Agent> victims = new ArrayList<>();
				if(guard != null){
					System.out.printf("%s guarded\n", guard);
				}

				if (yesterday.getAttackedDead() != null) {
					System.out.printf("[%d]%s attacked\n", yesterday.getAttackedDead().getAgentIdx(), getAgentName(yesterday.getAttackedDead()));
					victims.add(yesterday.getAttackedDead());
				}

				if (yesterday.getCursedFox() != null) {
					System.out.printf("[%d]%s cursed\n", yesterday.getCursedFox().getAgentIdx(), getAgentName(yesterday.getCursedFox()));
					victims.add(yesterday.getCursedFox());
					if(yesterday.getSuicideImmoralistWithCursedFoxList().size() > 0) {
						for(Agent a : yesterday.getSuicideImmoralistWithCursedFoxList()) {
							System.out.printf("[%d]%s suicided after the death of fox ( [%d]%s )\n", a.getAgentIdx(), getAgentName(a), yesterday.getCursedFox().getAgentIdx(), getAgentName(yesterday.getCursedFox()));
							victims.add(a);
						}
					}
				}
				System.out.println("========Victims========");
				if(victims.size() == 0) {
					System.out.println("No one dead last night");
				}
				else {
					for(Agent v : victims) {
						System.out.printf("[%d]%s dead last night\n", v.getAgentIdx(), getAgentName(v));
					}
				}
			}
		}
		
		System.out.println("======");
		List<Agent> agentList = gameData.getAgentList();
		Collections.sort(agentList, new Comparator<Agent>() {
			@Override
			public int compare(Agent o1, Agent o2) {
				return o1.getAgentIdx()-o2.getAgentIdx();
			}
		});
		for(Agent agent:agentList){
			System.out.printf("%s\t%s\t%s\t%s", agent, agentNameMap.get(agent), gameData.getStatus(agent), gameData.getRole(agent));
			if(yesterday != null){
				if (yesterday.getExecuted() == agent) {
					System.out.print("\texecuted");
				}

				if (agent == yesterday.getAttackedDead()) {
					System.out.print("\tattacked");
				}

				Judge divine = yesterday.getDivine();
				if(divine != null && divine.getTarget() == agent){
					System.out.print("\tdivined");
				}
				Guard guard = yesterday.getGuard();
				if(guard != null && guard.getTarget() == agent){
					System.out.print("\tguarded");
				}

				if (agent == yesterday.getCursedFox()) {
					System.out.print("\tcursed");
				}
				
				if(yesterday.getSuicideImmoralistWithCursedFoxList().contains(agent) || yesterday.getSuicideImmoralistWithExecutedFoxList().contains(agent)) {
					System.out.print("\tsuicided");
				}
			}
			System.out.println();
		}
		System.out.printf("Human:%d\nWerewolf:%d\n", getAliveHumanList().size() - gameData.getFilteredAgentList(getAliveAgentList(), Role.FOX).size(), getAliveWolfList().size());
		if (gameSetting.getRoleNum(Role.FOX) != 0) {
			System.out.printf("Others:%d\n", gameData.getFilteredAgentList(getAliveAgentList(), Team.OTHERS).size());
		}

		System.out.println("=============================================");
	}


	protected void day() {
		dayStart();
		if (gameData.getDay() == 0) {
			if (gameSetting.isTalkOnFirstDay()) {
				whisper();
				talk();
			}
		}
		else {
			talk();
		}
	}

	/**
	 *
	 */
	protected void night() {

//		for (Agent agent : getAliveAgentList()) {
//			gameServer.dayFinish(agent);
//		}
		for (Agent agent : getGameData().getAgentList()) {
			gameServer.dayFinish(agent);
		}

		if(!gameSetting.isTalkOnFirstDay() && gameData.getDay() == 0){
			whisper();
		}

		// Vote and execute except day 0
		Agent executed = null;
		List<Agent> candidates = null;
		if (gameData.getDay() != 0) {
			gameData.revoteList.clear();
			for (int i = 0; i <= gameSetting.getMaxRevote(); i++) {
				vote();
				gameData.addRevote(i);
				candidates = getVotedCandidates(gameData.getVoteList());
				if (candidates.size() == 1) {
					executed = candidates.get(0);
					break;
				}
			}

			if (executed == null && !gameSetting.isEnableNoExecution()) {
				// 4人盤面で決戦投票に妖狐が含まれていた場合は引き分けになる
				if(getAliveAgentList().size() == 4) {
					boolean isContainFox = false;
					for(Agent a : candidates) {
						if(gameData.agentRoleMap.get(a) == Role.FOX) {
							isContainFox = true;
							break;
						}
					}
					if(isContainFox) {
						isDraw = true;
					}
					else {
						Collections.shuffle(candidates, rand);
						executed = candidates.get(0);				
					}
				}
				else {
					Collections.shuffle(candidates, rand);
					executed = candidates.get(0);
				}
			}

			if (executed != null) {
				gameData.setExecutedTarget(executed);
				gameData.setExecuteSuicideTarget(executed);
				if (gameLogger != null) {
					gameLogger.log(String.format("%d,execute,%d,%s", gameData.getDay(), executed.getAgentIdx(), gameData.getRole(executed)));
					if(gameData.getSuicideImmoralistWithExecutedFoxList().size() > 0) {
						for(Agent a : gameData.getSuicideImmoralistWithExecutedFoxList()) {
							gameLogger.log(String.format("%d,suicide,%d,%s", gameData.getDay(), a.getAgentIdx(), gameData.getRole(a)));
						}
					}
				}
			}
		}
		
		if(!isGameFinished()) {
			divine();
		}

		if (gameData.getDay() != 0) {
			whisper();
			guard();

			// attackVote and attack except day 0
			Agent attacked = null;
			if (getAliveWolfList().size() > 0 && !isGameFinished()) {
				for (int i = 0; i <= gameSetting.getMaxAttackRevote(); i++) {
					if(i > 0 && gameSetting.isWhisperBeforeRevote()){
						whisper();
					}
					attackVote();
					List<Vote> attackCandidateList = gameData.getAttackVoteList();
					Iterator<Vote> it = attackCandidateList.iterator();
					while (it.hasNext()) {
						Vote vote = it.next();
						if (vote.getAgent() == executed) {
							it.remove();
						}
					}
					candidates = getAttackVotedCandidates(attackCandidateList);
					if (candidates.size() == 1) {
						attacked = candidates.get(0);
						break;
					}
				}
				
				// 噛みなし禁止なら襲撃投票のうちランダムで襲撃する
				if (attacked == null && !gameSetting.isEnableNoAttack()) {
					Collections.shuffle(candidates, rand);
					if(candidates.size() > 0) {
						System.out.println("no attack candidate");
						attacked = candidates.get(0);
					}
				}

				gameData.setAttackedTarget(attacked);

				boolean isGuarded = false;
				if (gameData.getGuard() != null) {
					// 噛みなしではなく、噛み先と護衛先が一致していたら護衛成功 (GameDataのguard対象が1人しか選択できないので狩人は1人しか配役にいないと予測)
					if (gameData.getGuard().getTarget() == attacked && attacked != null) {
						if (gameData.getExecuted() == null || !(gameData.getExecuted() == gameData.getGuard().getAgent())) {
							isGuarded = true;
						}
					}
				}
				// 護衛成功してない場合、かつ噛み先が妖狐でない場合、噛みなしでない場合は襲撃者は死亡する
				if (!isGuarded && attacked != null && gameData.getRole(attacked) != Role.FOX && !isGameFinished()) {
					gameData.setAttackedDead(attacked);
					gameData.addLastDeadAgent(attacked);

					if (gameLogger != null) {
						gameLogger.log(String.format("%d,attack,%d,true", gameData.getDay(), attacked.getAgentIdx()));
					}
				} else if (attacked != null) {
					if (gameLogger != null) {
						gameLogger.log(String.format("%d,attack,%d,false", gameData.getDay(), attacked.getAgentIdx()));
					}
				} else {
					if (gameLogger != null) {
						gameLogger.log(String.format("%d,attack,-1,false", gameData.getDay()));
					}
				}
			}
		}
		
		//gameData.setCurseSuicideTarget();
		
		GameData next = gameData.nextDay();
		// 背徳者の道連れログの表示
		if(gameData.getSuicideImmoralistWithCursedFoxList().size() > 0) {
			for(Agent a : gameData.getSuicideImmoralistWithCursedFoxList()) {
				gameLogger.log(String.format("%d,suicide,%d,%s", gameData.getDay(), a.getAgentIdx(), gameData.getRole(a)));
			}
		}
		gameData = next;
		gameDataMap.put(gameData.getDay(), gameData);
		gameServer.setGameData(gameData);

	}


	/**
	 *
	 * @param voteList
	 * @return
	 */
	protected List<Agent> getVotedCandidates(List<Vote> voteList) {
		Counter<Agent> counter = new Counter<Agent>();
		for(Vote vote:voteList){
			if(gameData.getStatus(vote.getTarget()) == Status.ALIVE){
				counter.add(vote.getTarget());
			}
		}

		int max = counter.get(counter.getLargest());
		List<Agent> candidateList = new ArrayList<Agent>();
		for(Agent agent:counter){
			if(counter.get(agent) == max){
				candidateList.add(agent);
			}
		}
		return candidateList;
	}

	/**
	 *
	 * @param voteList
	 * @return
	 */
	protected List<Agent> getAttackVotedCandidates(List<Vote> voteList) {
		Counter<Agent> counter = new Counter<Agent>();
		for (Vote vote : voteList) {
			if (gameData.getStatus(vote.getTarget()) == Status.ALIVE
					&& gameData.getRole(vote.getTarget()) != Role.WEREWOLF) {
				counter.add(vote.getTarget());
			}
		}
		if (!gameSetting.isEnableNoAttack()) {
			for (Agent agent : getAliveHumanList()) {
				counter.add(agent);
			}
		}

		int max = counter.get(counter.getLargest());
		List<Agent> candidateList = new ArrayList<Agent>();
		for (Agent agent : counter) {
			if (counter.get(agent) == max) {
				candidateList.add(agent);
			}
		}
		return candidateList;
	}

	/**
	 *
	 */
	protected void dayStart(){
		if(gameLogger != null){
			for(Agent agent:new TreeSet<Agent>(gameData.getAgentList())){
				gameLogger.log(String.format("%d,status,%d,%s,%s,%s", gameData.getDay(), agent.getAgentIdx(),gameData.getRole(agent), gameData.getStatus(agent), agentNameMap.get(agent)));
			}
		}

		for (Agent agent : gameData.getAgentList()) {
			gameServer.dayStart(agent);
		}

	}

	/**
	 * 
	 */
	protected void talk() {

		List<Agent> aliveList = getAliveAgentList();
		for(Agent agent:aliveList){
			gameData.remainTalkMap.put(agent, gameSetting.getMaxTalk());
		}

		Counter<Agent> skipCounter = new Counter<>();
		for(int time = 0; time < gameSetting.getMaxTalkTurn(); time++){
			Collections.shuffle(aliveList);

			boolean continueTalk = false;
			for(Agent agent:aliveList){
				String talkText = Talk.OVER;
				if(gameData.getRemainTalkMap().get(agent) > 0){
					talkText = gameServer.requestTalk(agent);
				}
				if(talkText == null || talkText.isEmpty()){
					talkText = Talk.SKIP;
				}
				if (gameSetting.isValidateUtterance()) {
					if (!Content.validate(talkText)) {
						talkText = Talk.SKIP;
					}
				}
				if (talkText.equals(Talk.SKIP)) {
					skipCounter.add(agent);
					if(skipCounter.get(agent) > gameSetting.getMaxSkip()){
						talkText = Talk.OVER;
					}
				}
				Talk talk = new Talk(gameData.nextTalkIdx(), gameData.getDay(), time, agent, talkText);
				gameData.addTalk(talk.getAgent(), talk);
				if(gameLogger != null){
					gameLogger.log(String.format("%d,talk,%d,%d,%d,%s", gameData.getDay(), talk.getIdx(), talk.getTurn(), talk.getAgent().getAgentIdx(), talk.getText()));
				}

				if(!talk.isOver() && !talk.isSkip()){
					skipCounter.put(agent, 0);
				}
				if(!talk.isOver()){
					continueTalk = true;
				}
			}

			if(!continueTalk){
				break;
			}

		}
	}

	protected void whisper() {
		List<Agent> aliveWolfList = gameData.getFilteredAgentList(getAliveAgentList(), Role.WEREWOLF);
		// No whisper in case of lonely wolf.
		if(aliveWolfList.size() == 1){
			return;
		}
		for(Agent agent:aliveWolfList){
			gameData.remainWhisperMap.put(agent, gameSetting.getMaxWhisper());
		}

		Counter<Agent> skipCounter = new Counter<>();
		for (int turn = 0; turn < gameSetting.getMaxWhisperTurn(); turn++) {
			Collections.shuffle(aliveWolfList);

			boolean continueWhisper = false;
			for(Agent agent:aliveWolfList){
				String whisperText = Talk.OVER;
				if(gameData.getRemainWhisperMap().get(agent) > 0){
					whisperText = gameServer.requestWhisper(agent);
				}
				if(whisperText == null || whisperText.isEmpty()){
					whisperText = Talk.SKIP;
				}
				if (gameSetting.isValidateUtterance()) {
					if (!Content.validate(whisperText)) {
						whisperText = Talk.SKIP;
					}
				}
				if (whisperText.equals(Talk.SKIP)) {
					skipCounter.add(agent);
					if(skipCounter.get(agent) > gameSetting.getMaxSkip()){
						whisperText = Talk.OVER;
					}
				}
				Talk whisper = new Talk(gameData.nextWhisperIdx(), gameData.getDay(), turn, agent, whisperText);
				gameData.addWhisper(whisper.getAgent(), whisper);
				if(gameLogger != null){
					gameLogger.log(String.format("%d,whisper,%d,%d,%d,%s", gameData.getDay(), whisper.getIdx(), whisper.getTurn(), whisper.getAgent().getAgentIdx(), whisper.getText()));
				}
				
				if(!whisper.isOver() && !whisper.isSkip()){
					skipCounter.put(agent, 0);
				}
				if(!whisper.isOver()){
					continueWhisper = true;
				}
			}

			if(!continueWhisper){
				break;
			}
		}
	}

	/**
	 * <div lang="ja">投票</div>
	 *
	 * <div lang="en">Vote</div>
	 *
	 */
	protected void vote() {
		gameData.getVoteList().clear();
		List<Agent> voters = getAliveAgentList();
		List<Agent> aliveCandidates = voters;
		List<Vote> latestVoteList = new ArrayList<>();
		for (Agent agent : voters) {
			Agent target = gameServer.requestVote(agent);
			if (target == null || gameData.getStatus(target) == null || gameData.getStatus(target) == Status.DEAD || agent == target) {
				target = getRandomAgent(aliveCandidates, agent);
			}
			Vote vote = new Vote(gameData.getDay(), agent, target);
			gameData.addVote(vote);
			latestVoteList.add(vote);
		}
		gameData.setLatestVoteList(latestVoteList);

		for (Vote vote : latestVoteList) {
			if (gameLogger != null) {
				gameLogger.log(String.format("%d,vote,%d,%d", gameData.getDay(), vote.getAgent().getAgentIdx(), vote.getTarget().getAgentIdx()));
			}
		}
	}

	/**
	 * <div lang="ja">占い行動</div>
	 */
	protected void divine() {
		List<Agent> agentList = getAliveAgentList();
		for(Agent agent:getAliveAgentList()){
			// 占い師のプレイヤーについて
			if(gameData.getRole(agent) == Role.SEER){
				//占い対象の取得
				Agent target = gameServer.requestDivineTarget(agent);
				Role targetRole = gameData.getRole(target);
				if(gameData.getStatus(target) == Status.DEAD || target == null || targetRole == null){
//					target = getRandomAgent(agentList, agent);
				}
				else{
					Judge divine = new Judge(gameData.getDay(), agent, target, targetRole.getSpecies());
					gameData.addDivine(divine);

					//FOX 呪殺処理
					if(gameData.getRole(target) == Role.FOX){
						gameData.addLastDeadAgent(target);
						gameData.setCursedFox(target);
					}

					if(gameLogger != null){
						gameLogger.log(String.format("%d,divine,%d,%d,%s", gameData.getDay(), divine.getAgent().getAgentIdx(), divine.getTarget().getAgentIdx(), divine.getResult()));
						
						// 呪殺発生時のログ (背徳者の道連れは後に実行)
						if(gameData.getRole(target) == Role.FOX){
							gameLogger.log(String.format("%d,cursed,%d,%s", gameData.getDay(), divine.getTarget().getAgentIdx(), gameData.getRole(target)));
						}
					}
				}
			}
		}
	}

	/**
	 * <div lang="ja">護衛行動</div>
	 */
	protected void guard() {
		List<Agent> agentList = getAliveAgentList();
		for(Agent agent:getAliveAgentList()){
			if(gameData.getRole(agent) == Role.BODYGUARD){
				if (agent == gameData.getExecuted()) {
					continue;
				}
				Agent target = gameServer.requestGuardTarget(agent);
				if (target == null || gameData.getStatus(target) == null || agent == target) {
//					target = getRandomAgent(agentList, agent);
				}
				else{
					Guard guard = new Guard(gameData.getDay(), agent, target);
					gameData.addGuard(guard);

					if(gameLogger != null){
						gameLogger.log(String.format("%d,guard,%d,%d,%s", gameData.getDay(), guard.getAgent().getAgentIdx(), guard.getTarget().getAgentIdx(), gameData.getRole(guard.getTarget())));
					}
				}
			}
		}
	}

	protected void attackVote() {
		gameData.getAttackVoteList().clear();
		List<Agent> voters = getAliveWolfList();
		List<Agent> candidates = getAliveHumanList();
		for (Agent agent : voters) {
			Agent target = gameServer.requestAttackTarget(agent);
			if (target == null || gameData.getStatus(target) == null || gameData.getStatus(target) == Status.DEAD || gameData.getRole(target) == Role.WEREWOLF) {
				// target = getRandomAgent(candidateList, agent);
			}
			else {
				Vote attackVote = new Vote(gameData.getDay(), agent, target);
				gameData.addAttack(attackVote);

				if (gameLogger != null) {
					gameLogger.log(String.format("%d,attackVote,%d,%d", gameData.getDay(),
							attackVote.getAgent().getAgentIdx(), attackVote.getTarget().getAgentIdx()));
				}
			}
		}
		List<Vote> latestAttackVoteList = new ArrayList<>();
		for (Vote v : gameData.getAttackVoteList()) {
			latestAttackVoteList.add(v);
		}
		gameData.setLatestAttackVoteList(latestAttackVoteList);

	}

	/**
	 * ランダムなエージェントを獲得する．ただし，withoutを除く．
	 * @param agentList
	 * @param without
	 * @return
	 */
	protected Agent getRandomAgent(List<Agent> agentList, Agent... without) {
		Agent target;
		List<Agent> list = new ArrayList<Agent>(agentList);
		for(Agent agent:without){
			list.remove(agent);
		}
		target = list.get(rand.nextInt(list.size()));
		return target;
	}

	/**
	 * get alive agents
	 * @return
	 */
	protected List<Agent> getAliveAgentList(){
		List<Agent> agentList = new ArrayList<Agent>();
		for(Agent agent:gameData.getAgentList()){
			if(gameData.getStatus(agent) == Status.ALIVE){
				agentList.add(agent);
			}
		}
		return agentList;
	}

	protected List<Agent> getAliveHumanList(){
		return gameData.getFilteredAgentList(getAliveAgentList(), Species.HUMAN);
	}

	protected List<Agent> getAliveWolfList(){
		return gameData.getFilteredAgentList(getAliveAgentList(), Species.WEREWOLF);
	}

	protected List<Agent> getAliveFoxList(){
		return gameData.getFilteredAgentList(getAliveAgentList(), Role.FOX);
	}


	/**
	 * return is game finished
	 * @return
	 */
	public boolean isGameFinished() {
		Team winner = getWinner();
		return winner != null || isDraw;
	}

	/**
	 * get all data of the game
	 * @return
	 */
	public GameData getGameData() {
		return gameData;
	}

	/**
	 * get setting of the game
	 * @return
	 */
	public GameSetting getGameSetting(){
		return gameSetting;
	}


	/**
	 * @return isShowConsoleLog
	 */
	public boolean isShowConsoleLog() {
		return isShowConsoleLog;
	}


	/**
	 * @param isShowConsoleLog isShowConsoleLog
	 */
	public void setShowConsoleLog(boolean isShowConsoleLog) {
		this.isShowConsoleLog = isShowConsoleLog;
	}

	/**
	 *
	 * @param agent
	 * @return
	 */
	public String getAgentName(Agent agent){
		return agentNameMap.get(agent);
	}

}
