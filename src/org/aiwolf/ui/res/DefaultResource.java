package org.aiwolf.ui.res;

import java.net.URL;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.swing.ImageIcon;

import org.aiwolf.client.lib.TalkType;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.Operator;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Judge;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.data.Status;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.data.Team;
import org.aiwolf.common.data.Vote;
import org.aiwolf.common.util.BidiMap;

/**
 * Default Resources
 * @author tori and otsuki
 * 
 */
public class DefaultResource implements AIWolfResource {

	String[][] agentResourceAry = {
			{"Tatton", "img/Male_01.png"},
			{"Deicle", "img/Male_02.png"},
			{"Randal", "img/Male_03.png"},
			{"Dacota", "img/Male_04.png"},
			{"Peace", "img/Male_05.png"},
			{"Joakim", "img/Male_06.png"},
			{"Rodelic", "img/Male_07.png"},
			{"Ivan", "img/Male_08.png"},
			{"Jack", "img/Male_09.png"},
			{"Qu", "img/Male_10.png"},
			{"Tory", "img/Male_11.png"},
			{"Chrif", "img/Male_12.png"},
			{"Norah", "img/Female_01.png"},
			{"Clare", "img/Female_02.png"},
			{"Kino", "img/Female_03.png"},
			{"Mary", "img/Female_04.png"},
			{"Casalina", "img/Female_05.png"},
			{"Denis", "img/Female_06.png"},
			{"Catherine", "img/Female_07.png"},
			{"Eimy", "img/Female_08.png"},
			{"Barbara", "img/Female_09.png"},
			{"Brenda", "img/Female_10.png"},
			{"Meries", "img/Female_11.png"},
			{"Marinda", "img/Female_12.png"},
		};

//	String[][] agentResourceAry = {
//			{"Oliver", "img/00_body.png"},
//			{"Deicle", "img/01_body.png"},
//			{"Lenox", "img/02_body.png"},
//			{"Traise", "img/03_body.png"},
//			{"Dylany", "img/04_body.png"},
//			{"Jerico", "img/05_body.png"},
//			{"Safira", "img/06_body.png"},
//			{"Birkley", "img/07_body.png"},
//			{"Rodelic", "img/08_body.png"},
//			{"Monty", "img/09_body.png"},
//			{"Rinsy", "img/10_body.png"},
//			{"Dacota", "img/11_body.png"},
//			{"Masha", "img/12_body.png"},
//			{"Kypling", "img/13_body.png"},
//			{"Chazz", "img/14_body.png"},
//			{"Tory", "img/15_body.png"},
//			{"Tatton", "img/16_body.png"},
//			{"Marinda", "img/17_body.png"},
//			{"Randal", "img/18_body.png"},
//			{"Nikky", "img/19_body.png"},
//			{"Reginald", "img/20_body.png"},
//			{"Michael", "img/21_body.png"},
//			{"Rusias", "img/22_body.png"},
//			{"Joakim", "img/23_body.png"},
//			{"Casalina", "img/24_body.png"},
//			{"Clare", "img/25_body.png"},
//			{"Ted", "img/26_body.png"},
//			{"Garda", "img/27_body.png"},
//			{"Ku", "img/28_body.png"},
//			{"Pink", "img/29_body.png"},
//			{"Tom", "img/30_body.png"},
//			{"Chrif", "img/31_body.png"},
//			{"Alexey", "img/32_body.png"},
//			{"Eimy", "img/33_body.png"},
//			{"Jack", "img/34_body.png"},
//			{"Catherine", "img/35_body.png"},
//			{"Blenda", "img/36_body.png"},
//			{"Pierre", "img/37_body.png"},
//			{"Chocola", "img/38_body.png"},
//			{"Zak", "img/39_body.png"},
//			{"Emet", "img/40_body.png"},
//			{"Emit", "img/41_body.png"},
//			{"Sarah", "img/42_body.png"},
//			{"Boss", "img/43_body.png"},
//			{"Kino", "img/44_body.png"},
//			{"Monica", "img/45_body.png"},
//			{"Denis", "img/46_body.png"},
//			{"Camus", "img/47_body.png"},
//			{"Yurie", "img/48_body.png"},
//			{"Norah", "img/49_body.png"},
//			{"Barbara", "img/50_body.png"},
//			{"Chercy", "img/51_body.png"},
//			{"Cherio", "img/52_body.png"},
//			{"Guen", "img/53_body.png"},
//			{"Lyza", "img/54_body.png"},
//			{"Beth", "img/55_body.png"},
//			{"Walker", "img/56_body.png"},
//			{"Mary", "img/57_body.png"},
//			{"Maxris", "img/58_body.png"},
//			{"Kanabis", "img/59_body.png"},
//			{"Hach", "img/60_body.png"},
//			{"Marble", "img/61_body.png"},
//			{"Peace", "img/62_body.png"},
//			{"Doc", "img/63_body.png"},
//			{"Meries", "img/64_body.png"},
//			{"Fran", "img/65_body.png"},
//			{"Higras", "img/66_body.png"},
//			{"Ivan", "img/67_body.png"},
//			{"Tao", "img/68_body.png"},
//			{"Jima", "img/69_body.png"},
//			{"Tanya", "img/70_body.png"},
//			{"Jonnie", "img/71_body.png"},
//			{"Lindberg", "img/72_body.png"},
//	};

	BidiMap<Agent, String> bidiMap = new BidiMap<>();
	final List<String[]> agentResourceList;

	public DefaultResource(){
		agentResourceList = Arrays.asList(agentResourceAry);
		long seed = Calendar.getInstance().getTimeInMillis()/(1000*60*60);
		Collections.shuffle(agentResourceList, new Random(seed));
		// Collections.shuffle(agentResourceList);

		bidiMap = new BidiMap<>();
	}

	@Override
	public void setName(int i, String name){
		agentResourceList.get(i)[0] = name;
	}


	@Override
	public String convertAgent(Agent agent) {
		if(agent == null){
			return "";
		}
		else if(agent == Content.ANY){
			return "any";
		}
		String name = agentResourceList.get(agent.getAgentIdx())[0];
		bidiMap.put(agent, name);
		return name;

//		String.format("%s%02d", agent.toString(), agent.getAgentIdx());
//		bidiMap.put(agent, agent.toString());
//		return agent.toString();
	}

	@Override
	public Agent convertToAgent(String name) {
		return bidiMap.getKey(name);
	}


	@Override
	public String convertTalk(Talk talk) {
		if(talk.isSkip()){
			return "SKIP";
		}
		else if(talk.isOver()){
			return "OVER";
		}
		try{
			Content contents = new Content(talk.getAgent() + " " + Content.stripSubject(talk.getText()));
			return contentToText(contents.getSubject(), contents, TalkType.TALK);
		}catch(Exception e){
			e.printStackTrace();
			return talk.getText();
		}
	}


	@Override
	public String convertWhisper(Talk whisper) {
		if(whisper.isSkip()){
			return "SKIP";
		}
		else if(whisper.isOver()){
			return "OVER";
		}
		try{
			Content contents = new Content(whisper.getAgent() + " " + Content.stripSubject(whisper.getText()));
			return contentToText(contents.getSubject(), contents, TalkType.WHISPER);
		}catch(Exception e){
			e.printStackTrace();
			return whisper.getText();
		}
	}

	/**
	 * Convert content to text
	 * @param baseContents
	 */
	protected String contentToText(Agent talker, Content baseContents, TalkType talkType) {
		Agent subject = baseContents.getSubject();
		Operator operator = baseContents.getOperator();
		Topic topic = baseContents.getTopic();
		if(operator == null){
			return topicToText(talker, baseContents, topic, talkType);
		}
		else{
			if(operator == Operator.REQUEST){
				StringBuffer buf = new StringBuffer();
				if (subject != talker) {
					buf.append(convertAgent(subject) + "->");
				}
				for(Content content:baseContents.getContentList()) {
					buf.append(String.format("Request：%s ", contentToText(talker, content, talkType)));
//					if(content.getSubject() != null){
//						buf.append(String.format("%sに要望： %s ", convert(content.getSubject()), contentToText(content)));
//					}
//					else{
//					}
				}
				return buf.toString();
			}
			else if(operator == Operator.BECAUSE){
				StringBuffer buf = new StringBuffer();
				if (subject != talker) {
					buf.append(convertAgent(subject) + "->");
				}
				List<Content> contentList = baseContents.getContentList();
				if(contentList.size() == 2){
					Content reason = contentList.get(0);
					Content action = contentList.get(1);
					buf.append(String.format("%s becase %s", contentToText(talker, action, talkType), contentToText(talker, reason, talkType)));
				}
				else{
					return baseContents.getText();
				}
				return buf.toString();
			}
			else if(operator == Operator.INQUIRE){
				StringBuffer buf = new StringBuffer();
				if (subject != talker) {
					buf.append(convertAgent(subject) + "->");
				}
				for(Content content:baseContents.getContentList()){
					buf.append(String.format("Inquire：%s ", contentToText(talker, content, talkType)));
				}
				return buf.toString();

			}
			else if(operator == Operator.AND){
				StringBuffer buf = new StringBuffer();
				if (subject != talker) {
					buf.append(convertAgent(subject) + "->");
				}
				String and = "";
				for(Content content:baseContents.getContentList()){
					buf.append(String.format("%s%s ", and, contentToText(talker, content, talkType)));
					and = "and ";
				}
				return buf.toString();
			}
			else if(operator == Operator.OR){
				StringBuffer buf = new StringBuffer();
				if (subject != talker) {
					buf.append(convertAgent(subject) + "->");
				}
				String and = "";
				for(Content content:baseContents.getContentList()){
					buf.append(String.format("%s%s ", and, contentToText(talker, content, talkType)));
					and = "or ";
				}
				return buf.toString();
			}
			else if(operator == Operator.XOR){
				StringBuffer buf = new StringBuffer();
				if (subject != talker) {
					buf.append(convertAgent(subject) + "->");
				}
				String and = "";
				for(Content content:baseContents.getContentList()){
					buf.append(String.format("%s%s ", and, contentToText(talker, content, talkType)));
					and = "or ";
				}
				if(baseContents.getContentList().size() > 1) {
					buf.append(", but not both ");
				}
				return buf.toString();
			}
			else if(operator == Operator.NOT){
				StringBuffer buf = new StringBuffer();
				if (subject != talker) {
					buf.append(convertAgent(subject) + "->");
				}
				for(Content content:baseContents.getContentList()){
					buf.append(String.format("not %s ", contentToText(talker, content, talkType)));
				}
				return buf.toString();
			}
			else if (operator == Operator.DAY) {
				if (subject == talker) {
					return contentToText(talker, baseContents.getContentList().get(0), talkType) + " on day" + baseContents.getDay();
				} else {
					return convertAgent(subject) + "->" + contentToText(talker, baseContents.getContentList().get(0), talkType) + " on day" + baseContents.getDay();
				}
			}
//			System.out.println(operator);
		}
		return baseContents.getText();

	}

	/**
	 *
	 * @param contents
	 * @param topic
	 * @return
	 */
	protected String topicToText(Agent talker, Content contents, Topic topic, TalkType talkType) {
		Agent subject = contents.getSubject();
		if(topic == Topic.ATTACK){
			if (subject == talker) {
				return String.format("I will attack %s", convertAgent(contents.getTarget()));
			} else {
				return String.format("%s will attack %s", convertAgent(subject), convertAgent(contents.getTarget()));
			}
		}
		else if(topic == Topic.ATTACKED){
			if (subject == talker) {
				return String.format("I attacked %s", convertAgent(contents.getTarget()));
			} else {
				return String.format("%s attacked %s", convertAgent(subject), convertAgent(contents.getTarget()));
			}
		}
		else if(topic == Topic.AGREE){
			if (subject == talker) {
				return String.format("I agree to %03d at day%d", contents.getTalkID(), contents.getTalkDay());
			} else {
				return String.format("%s agrees to %03d at day%d", convertAgent(subject), contents.getTalkID(), contents.getTalkDay());
			}
		}
		else if(topic == Topic.COMINGOUT){
			if(talkType == TalkType.TALK){
				if (subject == talker) {
					if (contents.getTarget() == talker) {
						return String.format("I am %s", convertRole(contents.getRole()));
					}
					else {
						return String.format("%s is %s", convertAgent(contents.getTarget()), convertRole(contents.getRole()));
					}
				}
				else {
					if (subject == contents.getTarget()) {
						return String.format("%s came out about being %s", convertAgent(subject), convertRole(contents.getRole()));
					}
					else {
						return String.format("%s came out that %s is %s", convertAgent(subject), convertAgent(contents.getTarget()), convertRole(contents.getRole()));
					}
				}
			}
			else if(talkType == TalkType.WHISPER){
				if (subject == talker) {
					if (contents.getTarget() == talker) {
						return String.format("I will come out that I am %s", convertRole(contents.getRole()));

					} 
					else {
						return String.format("I will come out that %s is %s", convertAgent(contents.getTarget()), convertRole(contents.getRole()));
					}
				} 
				else {
					if (subject == contents.getTarget()) {
						return String.format("%s came out about being %s", convertAgent(subject), convertRole(contents.getRole()));
					}
					else {
						return String.format("%s came out that %s is %s", convertAgent(subject), convertAgent(contents.getTarget()), convertRole(contents.getRole()));
					}
				}
			}
		}
		else if(topic == Topic.DISAGREE){
			if (subject == talker) {
				return String.format("I disagree to %03d at day %d", contents.getTalkID(), contents.getTalkDay());
			} else {
				return String.format("%s disagrees to %03d at day %d", convertAgent(subject), contents.getTalkID(), contents.getTalkDay());
			}
		}
		else if(topic == Topic.DIVINED){
			if(talkType == TalkType.TALK){
				if (subject == talker) {
					return String.format("Divine Result：%s is %s", convertAgent(contents.getTarget()), convertSpecies(contents.getResult()));
				}
				else {
					return String.format("%s\'s Divine Result：%s is %s", convertAgent(subject), convertAgent(contents.getTarget()), convertSpecies(contents.getResult()));
				}
			}
			else if(talkType == TalkType.WHISPER){
				if (subject == talker) {
					return String.format("Fake Divine Result：%s is %s", convertAgent(contents.getTarget()), convertSpecies(contents.getResult()));
				}
				else {
					return String.format("%s\'s Fake Divine Result：%s is %s", convertAgent(subject), convertAgent(contents.getTarget()), convertSpecies(contents.getResult()));
				}
			}
		}
		else if(topic == Topic.DIVINATION){
			if(talkType == TalkType.TALK){
				if (subject == talker) {
					return String.format("Divine %s", convertAgent(contents.getTarget()));
				}
				else {
					return String.format("%s divines %s", convertAgent(subject), convertAgent(contents.getTarget()));
				}
			}
			else if(talkType == TalkType.WHISPER){
				if (subject == talker) {
					return String.format("I will lie that I will divine %s", convertAgent(contents.getTarget()));
				}
				else{
					return String.format("I lie that %s divine %s", convertAgent(subject), convertAgent(contents.getTarget()));
				}
			}
		}
		else if(topic == Topic.ESTIMATE){
			if (subject == talker) {
				return String.format("I estimate %s is %s．", convertAgent(contents.getTarget()), convertRole(contents.getRole()));
			}
			else {
				return String.format("%s estimates %s is %s．", convertAgent(subject), convertAgent(contents.getTarget()), convertRole(contents.getRole()));
			}
		}
		else if(topic == Topic.GUARDED){
			if(talkType == TalkType.TALK){
				if (subject == talker) {
					return String.format("I guarded %s", convertAgent(contents.getTarget()));
				}
				else {
					return String.format("%s guarded %s", convertAgent(subject), convertAgent(contents.getTarget()));
				}
			}
			else if(talkType == TalkType.WHISPER){
				if (subject == talker) {
					return String.format("I lie that I guarded %s", convertAgent(contents.getTarget()));
				}
				else {
					return String.format("I lie that %s guarded %s", convertAgent(subject), convertAgent(contents.getTarget()));
				}
			}
		}
		else if(topic == Topic.GUARD){
			if(talkType == TalkType.TALK){
				if (subject == talker) {
					return String.format("%s will be guarded", convertAgent(contents.getTarget()));
				}
				else{
					return String.format("%s will guard %s", convertAgent(subject), convertAgent(contents.getTarget()));
				}
			}
			else if(talkType == TalkType.WHISPER){
				if (subject == talker) {
					return String.format("I lie that I will guard %s", convertAgent(contents.getTarget()));
				}
				else{
					return String.format("I lie that %s will guard %s", convertAgent(subject), convertAgent(contents.getTarget()));
				}
			}
		}
		else if(topic == Topic.IDENTIFIED){
			if(talkType == TalkType.TALK){
				if (subject == talker) {
					return String.format("I identified %s as %s", convertAgent(contents.getTarget()), convertSpecies(contents.getResult()));
				}
				else{
					return String.format("%s identified %s as %s", convertAgent(subject), convertAgent(contents.getTarget()), convertSpecies(contents.getResult()));
				}
			}
			else if(talkType == TalkType.WHISPER){
				if (subject == talker) {
					return String.format("I will lie that I identified %s as %s", convertAgent(contents.getTarget()), convertSpecies(contents.getResult()));
				}
				else{
					return String.format("I will lie that %s identified %s as %s", convertAgent(subject), convertAgent(subject), convertAgent(contents.getTarget()),
							convertSpecies(contents.getResult()));
				}
			}
		}
		else if(topic == Topic.VOTE){
			if(talkType == TalkType.TALK){
				if (subject == talker) {
					return String.format("I vote to %s", convertAgent(contents.getTarget()));
				}
				else{
					return String.format("%s votes to %s", convertAgent(subject), convertAgent(contents.getTarget()));
				}
			}
			else if(talkType == TalkType.WHISPER){
				if (subject == talker) {
					return String.format("I will lie that I vote to %s", convertAgent(contents.getTarget()));
				}
				else{
					return String.format("I will lie that %s votes to %s", convertAgent(subject), convertAgent(contents.getTarget()));
				}				
			}
		}
		else if(topic == Topic.VOTED){
			if (subject == talker) {
				return String.format("I voted to %s", convertAgent(contents.getTarget()));
			} else {
				return String.format("%s voted to %s", convertAgent(subject), convertAgent(contents.getTarget()));
			}
		}
		return contents.getText();
	}


	@Override
	public String convertVote(Vote vote) {
		return convertAgent(vote.getAgent())+" voted to "+convertAgent(vote.getTarget());
	}

	@Override
	public String convertAttackVote(Vote vote) {
		return convertAgent(vote.getAgent())+" attack to "+convertAgent(vote.getTarget());
	}

	@Override
	public String convertMedium(Judge mediumResult) {
		if(mediumResult.getAgent() == null){
			return "No one inquested";
		}
		return String.format("%s was %s", convertAgent(mediumResult.getTarget()), convertSpecies(mediumResult.getResult()));
	}

	@Override
	public String convertDivined(Judge divineResult) {
		if(divineResult.getAgent() == null){
			return "No one divined";
		}
		return String.format("%s was %s", convertAgent(divineResult.getTarget()), convertSpecies(divineResult.getResult()));
	}

	@Override
	public String convertGuarded(Agent guardedAgent) {
		if(guardedAgent == null){
			return "No one guarded";
		}

		return String.format("%s guarded", convertAgent(guardedAgent));
	}

	@Override
	public String convertAttacked(Agent attackedAgent) {
		if(attackedAgent == null){
			return "No one attacked";
		}
		return String.format("%s was attacked", convertAgent(attackedAgent));
	}

	@Override
	public String convertExecuted(Agent executedAgent) {
		if(executedAgent == null){
			return "No one executed";
		}
		return String.format("%s was executed", convertAgent(executedAgent));
	}

	@Override
	public String convertDead(Agent agent) {
		if(agent == null){
			return "No one dead";
		}
		return String.format("%s dead", convertAgent(agent));
	}



	@Override
	public String convertRole(Role role) {
		if(role != null){
			return role.toString();
		}else{
			return null;
		}
	}

	@Override
	public String convertSpecies(Species species) {
		if(species != null){
			return species.toString();
		}else{
			return null;
		}
	}

	@Override
	public String convertTeam(Team team) {
		if(team != null){
			return team.toString();
		}else{
			return null;
		}
	}


	@Override
	public String convertStatus(Status status) {
		if(status != null){
			return status.toString();
		}else{
			return null;
		}
	}

	@Override
	public String aliveRemain(int agents) {
		return String.format("%d agents alive", agents);
	}

	@Override
	public String convertTopic(Topic topic) {
		if(topic != null){
			return topic.toString();
		}else{
			return null;
		}
	}



	@Override
	public ImageIcon getImageIcon(Agent agent) {
		String imageUrl = agentResourceList.get(agent.getAgentIdx())[1];
//		System.out.println(imageUrl+"\t"+agentResourceList.get(agent.getAgentIdx())[0]);
		URL url=getClass().getClassLoader().getResource(imageUrl);
		ImageIcon icon = new ImageIcon(url);
		return icon;
//
//		return new ImageIcon(new BufferedImage(1, 1, Image.SCALE_SMOOTH));
	}

	public String convertWinner(Team winner){
		return "Winner:"+convertTeam(winner);
	}

	public String convertWinner(Team winner, Team yourTeam){
		String result = convertWinner(winner)+"\n";
		if(winner == yourTeam){
			return result+"You win";
		}
		else{
			return result+"You lose";
		}
	}

	@Override
	public String getFirstText(Agent agent, Role role) {
		return String.format("You are %s with role %s", convertAgent(agent), convertRole(role));
	}

	@Override
	public String getRoleInformation(Map<Role, Integer> roleCounter) {
		StringBuffer buf = new StringBuffer();
		for(Role role:roleCounter.keySet()){
			if(roleCounter.get(role) == 0){
				continue;
			}
			buf.append(convertRole(role)+":"+roleCounter.get(role)+", ");
		}
//		buf.append("\n");
		return buf.toString();
	}

	@Override
	public String convertTalkType(TalkType talkType) {
		return talkType.toString();
	}

	@Override
	public String convertAttackedDay(int day) {
		return "Attacked@"+day;
	}

	@Override
	public String convertExecutedDay(int day) {
		return "Executed@" + day;
	}

	@Override
	public String dayStart(int day) {
		return "Day "+day+" start";
	}

	@Override
	public String convertText(String text) {
		return text;
	}

	@Override
	public String convertDeadDay(int day) {
		return "Dead@"+day;
	}

	@Override
	public String convertCursedDay(int day) {
		return "Cursed@"+day;
	}

	@Override
	public String convertTurn(int turn) {
		return "Turn "+turn;
	}

	@Override
	public String convertVote() {
		return "Vote";
	}

}
