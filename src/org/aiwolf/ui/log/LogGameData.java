package org.aiwolf.ui.log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Guard;
import org.aiwolf.common.data.Judge;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Status;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.data.Vote;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameInfoToSend;
import org.aiwolf.common.net.GameSetting;
import org.aiwolf.server.GameData;

/**
 * Game data for GUILogViewer
 * @author tori
 *
 */
public class LogGameData extends GameData {

	GameData today;
	
	Map<Agent, String> agentNameMap;
	
	public LogGameData(GameSetting gameSetting, Map<Agent, Role> agentRoleMap) {
		super(gameSetting);

		today = new GameData(gameSetting);
		
		for(Agent agent:agentRoleMap.keySet()){
			today.addAgent(agent, Status.ALIVE, agentRoleMap.get(agent));
		}
		
		agentNameMap = new HashMap<>();
	}
	
	/**
	 * Add Message to GameData
	 * @param line
	 */
	public void addMessage(String line){
		String[] data = line.split(",");

		if(Integer.parseInt(data[0]) != today.getDay()){
			nextDay();
		}
		if(data[1].equals("status")){
//			Agent agent = Agent.getAgent(Integer.parseInt(data[2]));
//			Role role = Role.valueOf(data[3]);
//			String name = data[5];
////			agentRoleMap.put(agent, role);
//			agentNameMap.put(agent, name);
//			
//			today.addAgent(agent, Status.ALIVE, role);
		}
		else if(data[1].equals("talk")){
			Talk talk = toTalk(data);
			today.addTalk(talk.getAgent(), talk);
		}
		else if(data[1].equals("whisper")){
			Talk talk = toTalk(data);
			today.addWhisper(talk.getAgent(), talk);
		}
		else if(data[1].equals("divine")){
			Judge divine = toJudge(data);
			today.addDivine(divine);
		}
		else if(data[1].equals("guard")){
			Guard guard = toGuard(data);
			today.addGuard(guard);
		}
		else if(data[1].equals("vote")){
			Vote vote = toVote(data);
			today.addVote(vote);
		}
		else if(data[1].equals("attackVote")){
			Vote vote = toVote(data);
			today.addAttack(vote);
		}
		else if (data[1].equals("execute")) {
			Agent target = Agent.getAgent(Integer.parseInt(data[2]));
			today.setExecutedTarget(target);
		}
		else if(data[1].equals("attack")){
			if(data[3].equals("true")){
				Agent target = Agent.getAgent(Integer.parseInt(data[2]));
				today.addLastDeadAgent(target);
				today.setAttackedDead(target);
			}
		}
	}
	

	protected Vote toVote(String[] data) {
		Agent agent = Agent.getAgent(Integer.parseInt(data[2]));
		Agent target = Agent.getAgent(Integer.parseInt(data[3]));
		Vote vote = new Vote(Integer.parseInt(data[2]), agent, target);
		return vote;
	}

	protected Talk toTalk(String[] data) {
		if(data.length == 6){
			Talk talk = new Talk(Integer.parseInt(data[2]), Integer.parseInt(data[0]), Integer.parseInt(data[3]), Agent.getAgent(Integer.parseInt(data[4])), data[5]);
			return talk;
		}
		else{
			Talk talk = new Talk(Integer.parseInt(data[2]), Integer.parseInt(data[0]), 0, Agent.getAgent(Integer.parseInt(data[3])), data[4]);
			return talk;
			
		}
	}

	protected Judge toJudge(String[] data) {
		Agent target = Agent.getAgent(Integer.parseInt(data[3]));
		Judge judge = new Judge(Integer.parseInt(data[0]), Agent.getAgent(Integer.parseInt(data[2])), target, today.getRole(target).getSpecies());
		return judge;
	}
	
	protected Guard toGuard(String[] data) {
		Agent target = Agent.getAgent(Integer.parseInt(data[3]));
		Agent agent = Agent.getAgent(Integer.parseInt(data[2]));
		Guard guard = new Guard(Integer.parseInt(data[0]), agent, target);
		return guard;
	}


	public GameData nextDay(){
		today = today.nextDay();
		return this;
	}

	/**
	 * @param agent
	 * @return
	 * @see org.aiwolf.server.GameData#getGameInfo(org.aiwolf.common.data.Agent)
	 */
	public GameInfo getGameInfo(Agent agent) {
		return today.getGameInfo(agent);
	}

	/**
	 * @param agent
	 * @return
	 * @see org.aiwolf.server.GameData#getFinalGameInfo(org.aiwolf.common.data.Agent)
	 */
	public GameInfo getFinalGameInfo(Agent agent) {
		return today.getFinalGameInfo(agent);
	}

	/**
	 * @return
	 * @see org.aiwolf.server.GameData#getGameInfo()
	 */
	public GameInfo getGameInfo() {
		return today.getGameInfo();
	}

	/**
	 * @param agent
	 * @return
	 * @see org.aiwolf.server.GameData#getGameInfoToSend(org.aiwolf.common.data.Agent)
	 */
	public GameInfoToSend getGameInfoToSend(Agent agent) {
		return today.getGameInfoToSend(agent);
	}

	/**
	 * @return
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return today.toString();
	}

	/**
	 * @param agent
	 * @return
	 * @see org.aiwolf.server.GameData#getFinalGameInfoToSend(org.aiwolf.common.data.Agent)
	 */
	public GameInfoToSend getFinalGameInfoToSend(Agent agent) {
		return today.getFinalGameInfoToSend(agent);
	}

	/**
	 * @param agent
	 * @param status
	 * @param role
	 * @see org.aiwolf.server.GameData#addAgent(org.aiwolf.common.data.Agent, org.aiwolf.common.data.Status, org.aiwolf.common.data.Role)
	 */
	public void addAgent(Agent agent, Status status, Role role) {
		today.addAgent(agent, status, role);
	}

	/**
	 * @return
	 * @see org.aiwolf.server.GameData#getAgentList()
	 */
	public List<Agent> getAgentList() {
		return today.getAgentList();
	}

	/**
	 * @param agent
	 * @return
	 * @see org.aiwolf.server.GameData#getStatus(org.aiwolf.common.data.Agent)
	 */
	public Status getStatus(Agent agent) {
		return today.getStatus(agent);
	}

	/**
	 * @param agent
	 * @return
	 * @see org.aiwolf.server.GameData#getRole(org.aiwolf.common.data.Agent)
	 */
	public Role getRole(Agent agent) {
		return today.getRole(agent);
	}

	/**
	 * @param agent
	 * @param talk
	 * @see org.aiwolf.server.GameData#addTalk(org.aiwolf.common.data.Agent, org.aiwolf.common.data.Talk)
	 */
	public void addTalk(Agent agent, Talk talk) {
		today.addTalk(agent, talk);
	}

	/**
	 * @param agent
	 * @param whisper
	 * @see org.aiwolf.server.GameData#addWhisper(org.aiwolf.common.data.Agent, org.aiwolf.common.data.Talk)
	 */
	public void addWhisper(Agent agent, Talk whisper) {
		today.addWhisper(agent, whisper);
	}

	/**
	 * @param vote
	 * @see org.aiwolf.server.GameData#addVote(org.aiwolf.common.data.Vote)
	 */
	public void addVote(Vote vote) {
		today.addVote(vote);
	}

	/**
	 * @param divine
	 * @see org.aiwolf.server.GameData#addDivine(org.aiwolf.common.data.Judge)
	 */
	public void addDivine(Judge divine) {
		today.addDivine(divine);
	}

	/**
	 * @param guard
	 * @see org.aiwolf.server.GameData#addGuard(org.aiwolf.common.data.Guard)
	 */
	public void addGuard(Guard guard) {
		today.addGuard(guard);
	}

	/**
	 * @param attack
	 * @see org.aiwolf.server.GameData#addAttack(org.aiwolf.common.data.Vote)
	 */
	public void addAttack(Vote attack) {
		today.addAttack(attack);
	}

	/**
	 * @return
	 * @see org.aiwolf.server.GameData#getVoteList()
	 */
	public List<Vote> getVoteList() {
		return today.getVoteList();
	}

	/**
	 * @param target
	 * @see org.aiwolf.server.GameData#setExecutedTarget(org.aiwolf.common.data.Agent)
	 */
	public void setExecutedTarget(Agent target) {
		today.setExecutedTarget(target);
	}

	/**
	 * @param attacked
	 * @see org.aiwolf.server.GameData#setAttackedTarget(org.aiwolf.common.data.Agent)
	 */
	public void setAttackedTarget(Agent attacked) {
		today.setAttackedTarget(attacked);
	}

	/**
	 * @return
	 * @see org.aiwolf.server.GameData#getAttackVoteList()
	 */
	public List<Vote> getAttackVoteList() {
		return today.getAttackVoteList();
	}

	/**
	 * @return
	 * @see org.aiwolf.server.GameData#getGuard()
	 */
	public Guard getGuard() {
		return today.getGuard();
	}

	/**
	 * @return
	 * @see org.aiwolf.server.GameData#getDay()
	 */
	public int getDay() {
		return today.getDay();
	}

	/**
	 * @return
	 * @see org.aiwolf.server.GameData#getTalkList()
	 */
	public List<Talk> getTalkList() {
		return today.getTalkList();
	}

	/**
	 * @return
	 * @see org.aiwolf.server.GameData#getWhisperList()
	 */
	public List<Talk> getWhisperList() {
		return today.getWhisperList();
	}

	/**
	 * @return
	 * @see org.aiwolf.server.GameData#getDivine()
	 */
	public Judge getDivine() {
		return today.getDivine();
	}

	/**
	 * @return
	 * @see org.aiwolf.server.GameData#getExecuted()
	 */
	public Agent getExecuted() {
		return today.getExecuted();
	}

	/**
	 * @return
	 * @see org.aiwolf.server.GameData#getAttacked()
	 */
	public Agent getAttacked() {
		return today.getAttacked();
	}

	/**
	 * @param agent
	 * @see org.aiwolf.server.GameData#addLastDeadAgent(org.aiwolf.common.data.Agent)
	 */
	public void addLastDeadAgent(Agent agent) {
		today.addLastDeadAgent(agent);
	}

	/**
	 * @return
	 * @see org.aiwolf.server.GameData#getLastDeadAgentList()
	 */
	public List<Agent> getLastDeadAgentList() {
		return today.getLastDeadAgentList();
	}

	/**
	 * @return
	 * @see org.aiwolf.server.GameData#getSuddendeathList()
	 */
	public List<Agent> getSuddendeathList() {
		return today.getSuddendeathList();
	}

	/**
	 * @return
	 * @see org.aiwolf.server.GameData#getRemainTalkMap()
	 */
	public Map<Agent, Integer> getRemainTalkMap() {
		return today.getRemainTalkMap();
	}

	/**
	 * @return
	 * @see org.aiwolf.server.GameData#getRemainWhisperMap()
	 */
	public Map<Agent, Integer> getRemainWhisperMap() {
		return today.getRemainWhisperMap();
	}

	/**
	 * @return
	 * @see org.aiwolf.server.GameData#getDayBefore()
	 */
	public GameData getDayBefore() {
		return today.getDayBefore();
	}

	/**
	 * @return
	 * @see org.aiwolf.server.GameData#nextTalkIdx()
	 */
	public int nextTalkIdx() {
		return today.nextTalkIdx();
	}

	/**
	 * @return
	 * @see org.aiwolf.server.GameData#nextWhisperIdx()
	 */
	public int nextWhisperIdx() {
		return today.nextWhisperIdx();
	}

	/**
	 * @return
	 * @see org.aiwolf.server.GameData#getAttackedDead()
	 */
	public Agent getAttackedDead() {
		return today.getAttackedDead();
	}

	/**
	 * @param attackedDead
	 * @see org.aiwolf.server.GameData#setAttackedDead(org.aiwolf.common.data.Agent)
	 */
	public void setAttackedDead(Agent attackedDead) {
		today.setAttackedDead(attackedDead);
	}

	/**
	 * @return
	 * @see org.aiwolf.server.GameData#getCursedFox()
	 */
	public Agent getCursedFox() {
		return today.getCursedFox();
	}

	/**
	 * @param cursedFox
	 * @see org.aiwolf.server.GameData#setCursedFox(org.aiwolf.common.data.Agent)
	 */
	public void setCursedFox(Agent cursedFox) {
		today.setCursedFox(cursedFox);
	}

	/**
	 * @return
	 * @see org.aiwolf.server.GameData#getLatestVoteList()
	 */
	public List<Vote> getLatestVoteList() {
		return today.getLatestVoteList();
	}

	/**
	 * @param latestVoteList
	 * @see org.aiwolf.server.GameData#setLatestVoteList(java.util.List)
	 */
	public void setLatestVoteList(List<Vote> latestVoteList) {
		today.setLatestVoteList(latestVoteList);
	}

	/**
	 * @return
	 * @see org.aiwolf.server.GameData#getLatestAttackVoteList()
	 */
	public List<Vote> getLatestAttackVoteList() {
		return today.getLatestAttackVoteList();
	}

	/**
	 * @param latestAttackVoteList
	 * @see org.aiwolf.server.GameData#setLatestAttackVoteList(java.util.List)
	 */
	public void setLatestAttackVoteList(List<Vote> latestAttackVoteList) {
		today.setLatestAttackVoteList(latestAttackVoteList);
	}
	
}