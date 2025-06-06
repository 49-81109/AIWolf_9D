package org.aiwolf.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.aiwolf.common.AIWolfRuntimeException;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Guard;
import org.aiwolf.common.data.Judge;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.data.Status;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.data.Team;
import org.aiwolf.common.data.Vote;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameInfoToSend;
import org.aiwolf.common.net.GameSetting;
import org.aiwolf.common.net.JudgeToSend;
import org.aiwolf.common.net.TalkToSend;
import org.aiwolf.common.net.VoteToSend;

/**
 * Record game information of a day
 * @author tori
 *
 */
public class GameData {
	static final int firstDay = 1;

	/**
	 * The day of the data このデータの日数
	 */
	protected int day;

	/**
	 * status of each agents プレイヤーの生死の状態
	 */
	protected Map<Agent, Status> agentStatusMap;

	/**
	 * roles of each agents プレイヤーの役職
	 */
	protected Map<Agent, Role> agentRoleMap;

	/**
	 * プレイヤーの名前
	 */
	protected Map<Agent, String> agentNameMap;
	
	/**
	 *
	 */
	protected List<Talk> talkList;

	/**
	 *
	 */
	protected List<Talk> whisperList;

	/**
	 *
	 */
	protected List<Vote> voteList;

	protected Map<Integer, List<Vote>> revoteList;
	
	/**
	 * <div lang="ja">直近の投票リスト</div>
	 *
	 * <div lang="en">The latest list of votes.</div>
	 */
	protected List<Vote> latestVoteList;

	/**
	 *
	 */
	protected List<Vote> attackVoteList;

	/**
	 * <div lang="ja">直近の襲撃投票リスト</div>
	 *
	 * <div lang="en">The latest list of votes for attack.</div>
	 */
	protected List<Vote> latestAttackVoteList;

	/**
	 *
	 */
	protected Map<Agent, Integer> remainTalkMap;

	/**
	 *
	 */
	protected Map<Agent, Integer> remainWhisperMap;

	/**
	 * Result of divination 占い結果の情報
	 */
	protected Judge divine;

	/**
	 * Guard 護衛の情報
	 */
	protected Guard guard;

	/**
	 * executed agent 追放されたプレイヤー
	 */
	protected Agent executed;
	
	/**
	 * <div lang="ja">吊り+道連れで死亡した背徳者のリスト</div>
	 * 
	 */
	protected List<Agent> suicideImmoralistWithExecutedFoxList;
	
	/**
	 * <div lang="ja">昨夜人狼に襲われ死亡したエージェント</div>
	 *
	 * <div lang="en">the agent who died last night because of the attack by werewolf.</div>
	 */
	protected Agent attackedDead;

	/**
	 * <div lang="ja">昨夜人狼が襲ったエージェント（成否は問わない）</div>
	 *
	 * <div lang="en">the agent werewolves attacked last night (no matter whether or not the attack succeeded)</div>
	 */
	protected Agent attacked;

	/**
	 * <div lang="ja">呪殺された妖狐</div>
	 *
	 * <div lang="en">the fox killed by curse</div>
	 */
	protected Agent cursedFox;
	
	/**
	 * <div lang="ja">呪殺+道連れで死亡した背徳者のリスト</div>
	 * 
	 */
	protected List<Agent> suicideImmoralistWithCursedFoxList;

	/**
	 * <div lang="ja">昨夜死亡したエージェントのリスト</div>
	 *
	 * <div lang="en">the list of agents who died last night</div>
	 */
	protected List<Agent> lastDeadAgentList;

	/**
	 * agents who sudden death 突然死？
	 */
	protected List<Agent> suddendeathList;

	/**
	 * game data of one day before 前日のgameData
	 */
	protected GameData dayBefore;

	protected int talkIdx;

	protected int wisperIdx;


	/**
	 * ゲームの設定
	 */
	protected GameSetting gameSetting;

	public GameData(GameSetting gameSetting){
//		System.out.println("create GameData");
		agentStatusMap = new LinkedHashMap<>();
		agentRoleMap = new HashMap<>();
		agentNameMap = new HashMap<>();
		remainTalkMap = new HashMap<>();
		remainWhisperMap = new HashMap<>();
		talkList = new ArrayList<>();
		whisperList = new ArrayList<>();
		voteList = new ArrayList<>();
		revoteList = new HashMap<>();
		latestVoteList = new ArrayList<>();
		attackVoteList = new ArrayList<>();
		latestAttackVoteList = new ArrayList<>();
		lastDeadAgentList = new ArrayList<>();
		suddendeathList = new ArrayList<>();
		suicideImmoralistWithCursedFoxList = new ArrayList<>();
		suicideImmoralistWithExecutedFoxList = new ArrayList<>();

		this.gameSetting = gameSetting;
	}

	/**
	 * get specific game information
	 * @param agent
	 * @return
	 */
	public GameInfo getGameInfo(Agent agent){
		return getGameInfoToSend(agent).toGameInfo();
	}

	/**
	 * get final game information
	 * @param agent
	 * @return
	 */
	public GameInfo getFinalGameInfo(Agent agent){
		return getFinalGameInfoToSend(agent).toGameInfo();
	}

	/**
	 * get game info with all information
	 * @return
	 */
	public GameInfo getGameInfo(){
		return getFinalGameInfo(null);
	}


	/**
	 * プレイヤー別に取得できる送信情報を作成
	 * @param agent
	 *            - if null, get all information
	 * @return
	 */
	public GameInfoToSend getGameInfoToSend(Agent agent){
		GameData today = this;
		GameInfoToSend gi = new GameInfoToSend();

		int day = today.getDay();
		if(agent != null){
			// プレイヤー番号のセット
			gi.setAgent(agent.getAgentIdx());
		}
		if (gameSetting.isVoteVisible()) {
			List<VoteToSend> latestVoteList = new ArrayList<>();
			for (Vote vote : getLatestVoteList()) {
				latestVoteList.add(new VoteToSend(vote));
			}
			// 投票が可視化可能な場合、投票情報をセット
			gi.setLatestVoteList(latestVoteList);
		}
		if (getExecuted() != null) {
			// 追放があった場合、追放者の番号をセット(当日)
			gi.setLatestExecutedAgent(getExecuted().getAgentIdx());
		}
		if (agent == null || getRole(agent) == Role.WEREWOLF) {
			// プレイヤーが未定義？またはプレイヤーが人狼の場合()
			List<VoteToSend> latestAttackVoteList = new ArrayList<>();
			for (Vote vote : getLatestAttackVoteList()) {
				latestAttackVoteList.add(new VoteToSend(vote));
			}
			// 襲撃投票の候補リストをセット(人狼以外の役職の場合は空のままになる)
			gi.setLatestAttackVoteList(latestAttackVoteList);
		}

		GameData yesterday = today.getDayBefore();

		if (yesterday != null) {
			Agent executed = yesterday.getExecuted();
			if(executed != null){
				// 昨日の追放者の番号セット(霊能結果に使用？)
				gi.setExecutedAgent(executed.getAgentIdx());
				
				if (yesterday.suicideImmoralistWithExecutedFoxList != null) {
					// 吊りによって背徳者の道連れがあった場合
					if (yesterday.suicideImmoralistWithExecutedFoxList.size() > 0) {
						List<Integer> suicideList = new ArrayList<>();
						for(Agent suicide : yesterday.suicideImmoralistWithExecutedFoxList) {
							suicideList.add(suicide.getAgentIdx());
						}
						// 道連れになった背徳者の番号をセット
						gi.setSuicideImmoralistWithExecutedFox(suicideList);
						
						// 背徳者の道連れがあった日にちをセット
						gi.setExecutedFoxDay(yesterday.getDay());
					}
				}
			}

			ArrayList<Integer> lastDeadAgentList = new ArrayList<>();
			for (Agent a : yesterday.getLastDeadAgentList()) {
				lastDeadAgentList.add(new Integer(a.getAgentIdx()));
			}
			// 昨夜死亡したプレイヤーの番号をセット
			gi.setLastDeadAgentList(lastDeadAgentList);

			if(gameSetting.isVoteVisible()){
				List<VoteToSend> voteList = new ArrayList<>();
				for(Vote vote : yesterday.getVoteList()){
					voteList.add(new VoteToSend(vote));
				}
				// 昨日の投票情報をセット
//				System.out.println("set vote : GameData 298");			
				gi.setVoteList(voteList);
			}

			if (agent != null && today.getRole(agent) == Role.MEDIUM && executed != null) {
				// プレイヤーが霊能者で追放者がいた場合()
				// 追放者の役職をgetRoleで読み込んで、その種族を返す
				Species result = yesterday.getRole(executed).getSpecies();
				// 霊能結果の情報(日付、霊能者プレイヤー、追放者、結果の種族)、(霊能者以外のプレイヤーは空のまま)
				gi.setMediumResult(new JudgeToSend(new Judge(day, agent, executed, result)));
			}

			if (agent == null || today.getRole(agent) == Role.SEER) {
				// プレイヤーが未定義？またはプレイヤーが占い師の場合()
				Judge divine = yesterday.getDivine();
				if (divine != null && divine.getTarget() != null) {
					// 占い対象の役職をgetRoleで読み込んで、その種族を返す
					Species result = yesterday.getRole(divine.getTarget()).getSpecies();
					// 占い結果の情報(日付、占い師プレイヤー、占い対象、結果の種族)、(占い師以外のプレイヤーは空のまま)
					gi.setDivineResult(new JudgeToSend(new Judge(day, divine.getAgent(), divine.getTarget(), result)));
				}
			}

			if (agent == null || today.getRole(agent) == Role.WEREWOLF) {
				// プレイヤーが未定義？またはプレイヤーが人狼の場合()
				Agent attacked = yesterday.getAttacked();
				if (attacked != null) {
					// 襲撃者の番号をセット　(人狼以外のプレイヤーは空のまま, 襲撃失敗含む)
					gi.setAttackedAgent(attacked.getAgentIdx());
				}

				List<VoteToSend> attackVoteList = new ArrayList<VoteToSend>();
				for(Vote vote:yesterday.getAttackVoteList()){
					attackVoteList.add(new VoteToSend(vote));
				}
				// 襲撃投票のリストをセット
				gi.setAttackVoteList(attackVoteList);
			}
			if (agent == null || today.getRole(agent) == Role.BODYGUARD) {
				// プレイヤーが未定義？またはプレイヤーが狩人の場合()
				Guard guard = yesterday.getGuard();
				if(guard != null){
					// 護衛対象プレイヤーの番号をセット　(狩人以外のプレイヤーは空のまま)
					gi.setGuardedAgent(guard.getTarget().getAgentIdx());
				}
			}
			//System.out.println("*****---0" + agent);
			if (true /*agent == null*/) {
				if(yesterday.getDivine() != null) {
					//System.out.println(yesterday.getDivine().getTarget().getAgentIdx() + " : " + yesterday.getRole(yesterday.getDivine().getTarget()));
				}
				//System.out.println(yesterday.cursedFox != null);
				if (yesterday.cursedFox != null) {
					// 妖狐の呪殺があった場合、呪殺された妖狐の番号をセット
					gi.setCursedFox(yesterday.cursedFox.getAgentIdx());
					//System.out.println("*****---1");
					if (yesterday.suicideImmoralistWithCursedFoxList != null) {
						//System.out.println("*****---2");
						// 呪殺によって背徳者の道連れがあった場合
						if (yesterday.suicideImmoralistWithCursedFoxList.size() > 0) {
							//System.out.println("*****---3");
							List<Integer> suicideList = new ArrayList<>();
							for(Agent suicide : yesterday.suicideImmoralistWithCursedFoxList) {
								suicideList.add(suicide.getAgentIdx());
								//System.out.println("*****" + suicide.getAgentIdx());
							}
							// 道連れになった背徳者の番号をセット
							gi.setSuicideImmoralistWithCursedFox(suicideList);
						}
					}
				}
			}
		}
		
		List<TalkToSend> talkList = new ArrayList<TalkToSend>();
		for(Talk talk:today.getTalkList()){
			talkList.add(new TalkToSend(talk));
		}
		gi.setTalkList(talkList);

		LinkedHashMap<Integer, String> statusMap = new LinkedHashMap<Integer, String>();
		LinkedHashMap<Integer, String> nameMap = new LinkedHashMap<Integer, String>();
		for(Agent a : agentStatusMap.keySet()){
			statusMap.put(a.getAgentIdx(), agentStatusMap.get(a).toString());
		}
		for(Agent a : agentNameMap.keySet()){
			//System.out.println(agentNameMap.get(a).toString() + " -> GameData 382");
			nameMap.put(a.getAgentIdx(), agentNameMap.get(a).toString());
		}
		// 生死情報をセット
		gi.setStatusMap(statusMap);
		gi.setAgentNameMap(nameMap);
		

		// プレイヤー番号と役職の文字列のマップ(プレイヤー視点で把握できるもの)
		LinkedHashMap<Integer, String> roleMap = new LinkedHashMap<Integer, String>();
		// 自分の役職
		Role role = agentRoleMap.get(agent);

		Set<String> existingRoleSet = new TreeSet<>();
		// セットにしてからリストに変更することでプレイヤー番号とは無関係に役職を出力
		for(Role r : agentRoleMap.values()){
			existingRoleSet.add(r.toString());
		}
		// 配役に含まれる役職のリストをセット
		gi.setExistingRoleList(new ArrayList<>(existingRoleSet));

		LinkedHashMap<Integer, Integer> remainTalkMap = new LinkedHashMap<Integer, Integer>();
		for(Agent a:this.remainTalkMap.keySet()){
			remainTalkMap.put(a.getAgentIdx(), this.remainTalkMap.get(a));
		}
		gi.setRemainTalkMap(remainTalkMap);

		LinkedHashMap<Integer, Integer> remainWhisperMap = new LinkedHashMap<Integer, Integer>();
		if(role == Role.WEREWOLF){
			for(Agent a:this.remainWhisperMap.keySet()){
				remainWhisperMap.put(a.getAgentIdx(), this.remainWhisperMap.get(a));
			}
		}
		gi.setRemainWhisperMap(remainWhisperMap);

		if (role == Role.WEREWOLF || agent == null) {
			List<TalkToSend> whisperList = new ArrayList<>();
			for(Talk talk:today.getWhisperList()){
				whisperList.add(new TalkToSend(talk));
			}
			gi.setWhisperList(whisperList);
		}

		if(role != null){
			// 自分の番号と役職の文字列を格納
			roleMap.put(agent.getAgentIdx(), role.toString());
			//人狼の相方把握
			if (today.getRole(agent) == Role.WEREWOLF) {
//				List<TalkToSend> whisperList = new ArrayList<TalkToSend>();
//				for(Talk talk:today.getWhisperList()){
//					whisperList.add(new TalkToSend(talk));
//				}
//				gi.setWhisperList(whisperList);

				for (Agent target : today.getAgentList()) {
					if (today.getRole(target) == Role.WEREWOLF) {
						// wolfList.add(target);
						roleMap.put(target.getAgentIdx(), Role.WEREWOLF.toString());
					}
				}
			}
			// 狂信者の人狼把握？(多分自分のプレイヤーも混じって表示される)
			if (today.getRole(agent) == Role.FANATIC) {
				for (Agent target : today.getAgentList()) {
					if (today.getRole(target) == Role.WEREWOLF) {
						roleMap.put(target.getAgentIdx(), Role.WEREWOLF.toString());
					}
				}
			}
			// 共有者の相方把握
			if (today.getRole(agent) == Role.FREEMASON) {
				for (Agent target : today.getAgentList()) {
					if (today.getRole(target) == Role.FREEMASON) {
						roleMap.put(target.getAgentIdx(), Role.FREEMASON.toString());
					}
				}
			}
			// 妖狐の相方把握
			if (today.getRole(agent) == Role.FOX) {
				for (Agent target : today.getAgentList()) {
					if (today.getRole(target) == Role.FOX) {
						roleMap.put(target.getAgentIdx(), Role.FOX.toString());
					}
				}
			}
			// 背徳者の妖狐把握？(多分自分のプレイヤーも混じって表示される)
			if (today.getRole(agent) == Role.IMMORALIST) {
				for (Agent target : today.getAgentList()) {
					if (today.getRole(target) == Role.FOX) {
						roleMap.put(target.getAgentIdx(), Role.FOX.toString());
					}
				}
			}
		}
		gi.setRoleMap(roleMap);
		gi.setRemainTalkMap(remainTalkMap);
		gi.setDay(day);

		return gi;
	}

	public GameInfoToSend getFinalGameInfoToSend(Agent agent) {
		GameInfoToSend gi = getGameInfoToSend(agent);

		LinkedHashMap<Integer, String> roleMap = new LinkedHashMap<Integer, String>();
		for(Agent a:agentRoleMap.keySet()){
			roleMap.put(a.getAgentIdx(), agentRoleMap.get(a).toString());
		}
		gi.setRoleMap(roleMap);

		return gi;
	}


	/**
	 * Add new agent with their role
	 *
	 * @param agent
	 * @param status
	 * @param role
	 */
	public void addAgent(Agent agent, Status status, Role role){
		agentRoleMap.put(agent, role);
		agentStatusMap.put(agent, status);
		remainTalkMap.put(agent, gameSetting.getMaxTalk());
		if(getRole(agent) == Role.WEREWOLF){
			remainWhisperMap.put(agent, gameSetting.getMaxWhisper());
		}
	}

	/**
	 * get agents
	 * @return
	 */
	public List<Agent> getAgentList() {
		return new ArrayList<Agent>(agentRoleMap.keySet());
	}

	/**
	 * get status of agent
	 * @param agent
	 */
	public Status getStatus(Agent agent) {
		return agentStatusMap.get(agent);
	}

	/**
	 *
	 * @param agent
	 * @return
	 */
	public Role getRole(Agent agent) {
		return agentRoleMap.get(agent);
	}



	/**
	 *
	 * @param agent
	 * @param talk
	 */
	public void addTalk(Agent agent, Talk talk) {
		int remainTalk = remainTalkMap.get(agent);
		if(!talk.isOver() && !talk.isSkip()){
			if(remainTalk == 0){
				throw new AIWolfRuntimeException("No remain talk but try to talk. #Contact to AIWolf Platform Developer");
			}
			remainTalkMap.put(agent, remainTalk-1);
		}
		talkList.add(talk);
	}

	public void addWhisper(Agent agent, Talk whisper) {
		int remainWhisper = remainWhisperMap.get(agent);
		if(!whisper.isOver() && !whisper.isSkip()){
			if(remainWhisper == 0){
				throw new AIWolfRuntimeException("No remain whisper but try to whisper. #Contact to AIWolf Platform Developer");
			}
			remainWhisperMap.put(agent, remainWhisper-1);
		}
		whisperList.add(whisper);
	}

	/**
	 * Add vote data
	 *
	 * @param vote
	 */
	public void addVote(Vote vote) {
		voteList.add(vote);
	}
	
	public void addRevote(Integer i) {
		List<Vote> votes = new ArrayList<>();
		for(Vote v : voteList) {
			votes.add(v);
		}
		revoteList.put(i, votes);
	}

	/**
	 * Add divine
	 *
	 * @param divine
	 */
	public void addDivine(Judge divine) {
		this.divine = divine;
	}

	public void addGuard(Guard guard) {
		this.guard = guard;
	}

	public void addAttack(Vote attack) {
		attackVoteList.add(attack);
	}

	public List<Vote> getVoteList() {
		return voteList;
	}

	public Map<Integer, List<Vote>> getRevoteList() {
		return revoteList;
	}
	
	/**
	 * set executed
	 *
	 * @param target
	 */
	public void setExecutedTarget(Agent executed) {
		this.executed = executed;
		if (executed != null) {
			agentStatusMap.put(executed, Status.DEAD);
		}
	}
	
	
	/**
	 * 妖狐吊りでの全滅時の背徳者の道連れ実行
	 * @param executed
	 */
	public void setExecuteSuicideTarget(Agent executed) {
		if(this.getRole(executed) == Role.FOX) {
			boolean isFoxAlive = false;
			for(Agent a : this.getAgentList()){
				if(this.getRole(a) == Role.FOX && getStatus(a) == Status.ALIVE) {
					isFoxAlive = true;
					break;
				}
			}
			// 生存している妖狐がいなくなった場合、背徳者が道連れになる
			if(!isFoxAlive) {
				for(Agent a : this.getAgentList()){
					if(this.getRole(a) == Role.IMMORALIST && getStatus(a) == Status.ALIVE) {
						agentStatusMap.put(a, Status.DEAD);
						suicideImmoralistWithExecutedFoxList.add(a);
					}
				}
			}
		}
	}

	/**
	 *
	 * @param attacked
	 */
	public void setAttackedTarget(Agent attacked) {
		this.attacked = attacked;
	}

	/**
	 *
	 * @return
	 */
	public List<Vote> getAttackVoteList() {
		return attackVoteList;
	}

	/**
	 *
	 * @return
	 */
	public Guard getGuard() {
		return guard;
	}

	/**
	 * @return day
	 */
	public int getDay() {
		return day;
	}

	/**
	 * @return talkList
	 */
	public List<Talk> getTalkList() {
		return talkList;
	}

	/**
	 * @return wisperList
	 */
	public List<Talk> getWhisperList() {
		return whisperList;
	}

	/**
	 * @return divine
	 */
	public Judge getDivine() {
		return divine;
	}

	/**
	 * @return executed
	 */
	public Agent getExecuted() {
		return executed;
	}

	/**
	 * <div lang="ja">昨夜人狼が襲ったエージェント（成否は問わない）を返す</div>
	 *
	 * <div lang="en">Returns the agent werewolves attacked last night (no
	 * matter whether or not the attack succeeded).</div>
	 *
	 * @return attackedAgent - <div lang="ja">昨夜人狼が襲ったエージェント</div>
	 *
	 *         <div lang="en">the agent werewolves attacked last night</div>
	 */
	public Agent getAttacked() {
		return attacked;
	}

	/**
	 * <div lang="ja">昨夜死亡したエージェントを追加する
	 *
	 * </div> <div lang="en">Adds the agent who died last night.</div>
	 *
	 * @param agent
	 *            <div lang="ja">追加するエージェント</div>
	 *
	 *            <div lang="en">the agent to be added</div>
	 */
	public void addLastDeadAgent(Agent agent) {
		if (!lastDeadAgentList.contains(agent)) {
			lastDeadAgentList.add(agent);
		}
	}

	/**
	 * @return <div lang="ja">昨夜死亡したエージェントのリスト</div>
	 *
	 *         <div lang="en">the list of agents who died last night</div>
	 */
	public List<Agent> getLastDeadAgentList() {
		return lastDeadAgentList;
	}

	/**
	 * @return suddendeathList
	 */
	public List<Agent> getSuddendeathList() {
		return suddendeathList;
	}

	/**
	 * @return remainTalkMap
	 */
	public Map<Agent, Integer> getRemainTalkMap() {
		return remainTalkMap;
	}

	/**
	 * @return remainTalkMap
	 */
	public Map<Agent, Integer> getRemainWhisperMap() {
		return remainWhisperMap;
	}

	/**
	 * Create GameData of next day
	 * @return
	 */
	public GameData nextDay(){
		GameData gameData = new GameData(gameSetting);

		gameData.day = this.day+1;
		gameData.agentStatusMap = new HashMap<Agent, Status>(agentStatusMap);

		for (Agent a : lastDeadAgentList) {
			gameData.agentStatusMap.put(a, Status.DEAD);
		}
		gameData.agentRoleMap = new HashMap<Agent, Role>(agentRoleMap);
		
		boolean isFoxAlive = false;
		for(Agent a : gameData.getAgentList()){
			if(gameData.getRole(a) == Role.FOX && gameData.getStatus(a) == Status.ALIVE) {
				isFoxAlive = true;
				break;
			}
		}
		// 生存している妖狐がいなくなった場合、背徳者が道連れになる
		if(!isFoxAlive) {
			for(Agent a : gameData.getAgentList()){
				if(gameData.getRole(a) == Role.IMMORALIST && gameData.getStatus(a) == Status.ALIVE) {
					lastDeadAgentList.add(a);
					gameData.agentStatusMap.put(a, Status.DEAD);
					this.setAddSuicideImmoralistWithCursedFoxList(a);
				}
			}
		}
		
		for(Agent a:gameData.getAgentList()){
			if(gameData.getStatus(a) == Status.ALIVE){
				gameData.remainTalkMap.put(a, gameSetting.getMaxTalk());
				if(gameData.getRole(a) == Role.WEREWOLF){
					gameData.remainWhisperMap.put(a, gameSetting.getMaxWhisper());
				}
			}
		}

		gameData.dayBefore = this;

		return gameData;
	}

	/**
	 * get game data of one day before
	 * @return
	 */
	public GameData getDayBefore() {
		return dayBefore;
	}

//	/**
//	 * get wolf agents
//	 * @return
//	 */
//	public List<Agent> getWolfList(){
//		List<Agent> wolfList = new ArrayList<>();
//		for(Agent agent:getAgentList()){
//			if(getRole(agent).getSpecies() == Species.Werewolf){
//				wolfList.add(agent);
//			}
//		}
//		return wolfList;
//	}
//
//	/**
//	 * get human agents
//	 * @return
//	 */
//	public List<Agent> getHumanList(){
//		List<Agent> humanList = new ArrayList<>(getAgentList());
//		humanList.removeAll(getWolfList());
//		return humanList;
//	}

	protected List<Agent> getFilteredAgentList(List<Agent> agentList, Species species){
		List<Agent> resultList = new ArrayList<Agent>();
		for(Agent agent:agentList){
			if(getRole(agent).getSpecies() == species){
				resultList.add(agent);
			}
		}
		return resultList;
	}

	protected List<Agent> getFilteredAgentList(List<Agent> agentList, Status status){
		List<Agent> resultList = new ArrayList<Agent>();
		for(Agent agent:agentList){
			if(getStatus(agent) == status){
				resultList.add(agent);
			}
		}
		return resultList;
	}

	protected List<Agent> getFilteredAgentList(List<Agent> agentList, Role role){
		List<Agent> resultList = new ArrayList<Agent>();
		for(Agent agent:agentList){
			if(getRole(agent) == role){
				resultList.add(agent);
			}
		}
		return resultList;
	}

	protected List<Agent> getFilteredAgentList(List<Agent> agentList, Team team){
		List<Agent> resultList = new ArrayList<Agent>();
		for(Agent agent:agentList){
			if(getRole(agent).getTeam() == team){
				resultList.add(agent);
			}
		}
		return resultList;
	}




	public int nextTalkIdx() {
		return talkIdx++;
	}

	public int nextWhisperIdx() {
		return wisperIdx++;
	}

	/**
	 * <div lang="ja">昨夜人狼に襲われ死亡したエージェントを返す．</div>
	 *
	 * <div lang="en">Returns the agent who died last night because of the attack by werewolf.</div>
	 *
	 * @return the attackedDead
	 */
	public Agent getAttackedDead() {
		return attackedDead;
	}

	/**
	 * <div lang="ja">昨夜人狼に襲われ死亡したエージェントをセットする．</div>
	 *
	 * <div lang="en">Sets the agent who died last night because of the attack by werewolf.</div>
	 *
	 * @param attackedDead
	 *            the attackedDead to set
	 */
	public void setAttackedDead(Agent attackedDead) {
		this.attackedDead = attackedDead;
	}

	/**
	 * <div lang="ja">呪殺された妖狐を返す．</div>
	 *
	 * <div lang="en">Returns the fox killed by curse.</div>
	 *
	 * @return <div lang="ja">呪殺された妖狐</div>
	 *
	 *         <div lang="en">the fox killed by curse</div>
	 */
	public Agent getCursedFox() {
		return cursedFox;
	}

	/**
	 * <div lang="ja">呪殺された妖狐をセットする．</div>
	 *
	 * <div lang="en">Sets the fox killed by curse.</div>
	 *
	 * @param cursedFox
	 *            <div lang="ja">呪殺された妖狐</div>
	 *
	 *            <div lang="en">the fox killed by curse</div>
	 */
	public void setCursedFox(Agent cursedFox) {
		this.cursedFox = cursedFox;
	}
	
	/**
	 * <div lang="ja">呪殺+道連れで死亡した背徳者のリストを返す</div>
	 * 
	 * <div lang="en">Returns the immoralists dead with the last fox killed by curse.</div>
	 * 
	 * @return <div lang="ja">呪殺+道連れで死亡した背徳者のリスト</div>
	 * 
	 *         <div lang="en">the immoralists dead with the last fox killed by curse.</div>
	 */
	public List<Agent> getSuicideImmoralistWithCursedFoxList() {
		return suicideImmoralistWithCursedFoxList;
	}
	
	/**
	 * <div lang="ja">呪殺+道連れで死亡した背徳者のリストをセットする．</div>
	 * 
	 * <div lang="en">Sets the immoralists dead with the last fox killed by curse.</div>
	 * 
	 * @return <div lang="ja">呪殺+道連れで死亡した背徳者のリスト</div>
	 * 
	 *         <div lang="en">the immoralists dead with the last fox killed by curse.</div>
	 */
	public void setAddSuicideImmoralistWithCursedFoxList(Agent suicideImmoralist) {
		this.suicideImmoralistWithCursedFoxList.add(suicideImmoralist);
	}

	/**
	 * <div lang="ja">吊り+道連れで死亡した背徳者のリストを返す</div>
	 * 
	 * <div lang="en">Returns the immoralists dead with the last executed fox.</div>
	 * 
	 * @return <div lang="ja">吊り+道連れで死亡した背徳者のリスト</div>
	 * 
	 *         <div lang="en">the immoralists dead with the last executed fox.</div>
	 */
	public List<Agent> getSuicideImmoralistWithExecutedFoxList() {
		return suicideImmoralistWithExecutedFoxList;
	}
	
	/**
	 * <div lang="ja">直近の投票リストを返す</div>
	 *
	 * <div lang="en">Returns the latest list of votes.</div>
	 *
	 * @return <div lang="ja">投票リストを表す{@code List<Vote>}</div>
	 *
	 *         <div lang="en">{@code List<Vote>} representing the list of votes.</div>
	 */
	public List<Vote> getLatestVoteList() {
		return latestVoteList;
	}

	/**
	 * <div lang="ja">直近の投票リストをセットする</div>
	 *
	 * <div lang="en">Sets the latest list of votes.</div>
	 *
	 * @param latestVoteList
	 *            <div lang="ja">投票リストを表す{@code List<Vote>}</div>
	 *
	 *            <div lang="en">{@code List<Vote>} representing the list of votes.</div>
	 *
	 */
	public void setLatestVoteList(List<Vote> latestVoteList) {
		this.latestVoteList = latestVoteList;
	}

	/**
	 * <div lang="ja">直近の襲撃投票リストを返す</div>
	 *
	 * <div lang="en">Returns the latest list of votes for attack.</div>
	 *
	 * @return <div lang="ja">投票リストを表す{@code List<Vote>}</div>
	 *
	 *         <div lang="en">{@code List<Vote>} representing the list of votes.</div>
	 */
	public List<Vote> getLatestAttackVoteList() {
		return latestAttackVoteList;
	}

	/**
	 * <div lang="ja">直近の襲撃投票リストをセットする</div>
	 *
	 * <div lang="en">Sets the latest list of votes for attack.</div>
	 *
	 * @param latestAttackVoteList
	 *            <div lang="ja">投票リストを表す{@code List<Vote>}</div>
	 *
	 *            <div lang="en">{@code List<Vote>} representing the list of votes.</div>
	 *
	 */
	public void setLatestAttackVoteList(List<Vote> latestAttackVoteList) {
		this.latestAttackVoteList = latestAttackVoteList;
	}

	/**
	 *
	 * <div lang="ja">指定エージェントがゲームに含まれているかどうかを調べる</div>
	 *
	 * <div lang="en">Check whether the agents is joining in game.</div>
	 *
	 * @param latestAttackVoteList
	 *            <div lang="ja">含まれているかどうか{@code boolean}</div>
	 *
	 *            <div lang="en">{@code boolean} is contains in the game.</div>
	 */
	public boolean contains(Agent target) {
		return this.agentRoleMap.containsKey(target);
	}

}
