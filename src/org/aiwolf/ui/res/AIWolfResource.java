package org.aiwolf.ui.res;

import java.awt.Image;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.aiwolf.client.lib.TalkType;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Judge;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.data.Status;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.data.Team;
import org.aiwolf.common.data.Vote;
import org.aiwolf.common.util.Counter;

/**
 * Resources for AIWolf Human Agent
 * @author tori
 *
 */
public interface AIWolfResource {

	/**
	 * Add agent's name
	 * @param i
	 * @param name
	 */
	public void setName(int i, String name);

	
	/**
	 * convert agent to name
	 * @param agent
	 * @return
	 */
	String convertAgent(Agent agent);

	/**
	 * convert agent to name
	 * @param agent
	 * @return
	 */
	Agent convertToAgent(String name);

	
	/**
	 * convert talk to Natural Language
	 * @param talk
	 * @return
	 */
	String convertTalk(Talk talk);
	
	/**
	 * convert whisper to Natural Language
	 * @param whisper
	 * @return
	 */
	String convertWhisper(Talk whisper);
	
	/**
	 * 
	 * @param vote
	 * @return
	 */
	String convertVote(Vote vote);
	
	/**
	 * 
	 * @param vote
	 * @return
	 */
	String convertAttackVote(Vote vote);

	/**
	 * 
	 * @param mediumResult
	 * @return
	 */
	String convertMedium(Judge mediumResult);

	/**
	 * 
	 * @param divineResult
	 * @return
	 */
	String convertDivined(Judge divineResult);

	/**
	 * 
	 * @param guardedAgent
	 * @return
	 */
	String convertGuarded(Agent guardedAgent);

	/**
	 * 
	 * @param attackedAgent
	 * @return
	 */
	String convertAttacked(Agent attackedAgent);

	/**
	 * 
	 * @param executedAgent
	 * @return
	 */
	String convertExecuted(Agent executedAgent);



	/**
	 * 
	 * @param agent
	 * @return
	 */
	String convertDead(Agent agent);

	/**
	 * 
	 * @param role
	 * @return
	 */
	String convertRole(Role role);

	/**
	 * 
	 * @param species
	 * @return
	 */
	String convertSpecies(Species species);

	/**
	 * 
	 * @param team
	 * @return
	 */
	String convertTeam(Team team);

	/**
	 * Information of alival agents num
	 * @param size
	 * @return
	 */
	String aliveRemain(int agents);
	
	/**
	 * 
	 * @param topic
	 * @return
	 */
	String convertTopic(Topic topic);

	/**
	 * get ImageIcon of Agent
	 * @return
	 */
	ImageIcon getImageIcon(Agent agent);

	/**
	 * get win text
	 * @param winner
	 * @return
	 */
	String convertWinner(Team winner);

	/**
	 * get win text
	 * @param winner
	 * @return
	 */
	String convertWinner(Team winner, Team yourTeam);

	/**
	 * 
	 * @param turn
	 * @return
	 */
	String convertTurn(int turn);

	
	/**
	 * 
	 * @param agent
	 * @param role
	 * @return
	 */
	String getFirstText(Agent agent, Role role);

	/**
	 * 
	 * @param counter
	 * @return
	 */
	String getRoleInformation(Map<Role, Integer> roleCounter);

	/**
	 * 
	 * @param talkType
	 * @return
	 */
	String convertTalkType(TalkType talkType);

	/**
	 * 
	 * @param day
	 * @return
	 */
	String convertAttackedDay(int day);

	/**
	 * 
	 * @param day
	 * @return
	 */
	String convertExecutedDay(int day);

	/**
	 * 
	 * @param day
	 * @return
	 */
	String convertDeadDay(int day);

	/**
	 * 
	 * @param day
	 * @return
	 */
	String convertCursedDay(int day);

	/**
	 * @param 
	 * @return 
	 */
	String dayStart(int day);

	/**
	 * 
	 * @param status
	 * @return
	 */
	String convertStatus(Status status);

	/**
	 * 
	 * @param text
	 * @return
	 */
	String convertText(String text);

	/**
	 * 
	 * @return
	 */
	String convertVote();

	
}
