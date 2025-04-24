package org.aiwolf.ui;

import java.awt.Color;
import java.awt.FlowLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.aiwolf.client.lib.AgreeContentBuilder;
import org.aiwolf.client.lib.AttackContentBuilder;
import org.aiwolf.client.lib.ComingoutContentBuilder;
import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.DisagreeContentBuilder;
import org.aiwolf.client.lib.DivinedResultContentBuilder;
import org.aiwolf.client.lib.EstimateContentBuilder;
import org.aiwolf.client.lib.GuardedAgentContentBuilder;
import org.aiwolf.client.lib.IdentContentBuilder;
import org.aiwolf.client.lib.TalkType;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.client.lib.VoteContentBuilder;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;
import org.aiwolf.ui.res.AIWolfResource;

/**
 * Panel to get UserAction 
 * @author tori
 *
 */
public class UserActionPanel extends JPanel implements ItemListener, ActionListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final Color WHISPER_COLOR = HumanPlayer.WHISPER_COLOR;
	private static final Color TALK_COLOR = HumanPlayer.TALK_COLOR;
	private static final Color ACTION_COLOR = HumanPlayer.ACTION_COLOR;

	JComboBox<String> voteComboBox;
	JComboBox<String> attackVoteComboBox;
	JComboBox<String> divineComboBox;
	JComboBox<String> guardComboBox;
	JButton actionButton;
	
	JComboBox<Item<Topic>> utteranceBox;
	JComboBox<Item<Species>> speciesBox;
	JComboBox<Item<Role>> roleBox;
	JComboBox<TalkType> agreeTargetBox;
	JComboBox<String> targetBox;
	JComboBox<String> dayBox;
	JComboBox<Item<Integer>> talkIdBox;
	JComboBox<Item<Integer>> whisperIdBox;
	
//	JComboBox<String> optionBox1;
//	JComboBox<String> optionBox2;
	JButton talkButton;
	JButton skipButton;
	JButton overButton;
	JButton finishButton;
	
	JComboBox<Item<Topic>> whisperUtteranceBox;
//	JComboBox<String> whisperOptionBox;
//	JButton whisperTalkButton;
//	JButton whisperSkipButton;
//	JButton whisperOverButton;
//	
	private JPanel actionPanel;
	private JPanel talkPanel;
//	private JPanel whisperPanel;
	
	private GameSetting gameSetting;
	private GameInfo gameInfo;
	private Map<Integer, GameInfo> gameInfoMap;

	Agent agent = null;

	String talk = null;
	
	Agent vote = null;
	Agent attackVote = null;
	Agent divine = null;
	Agent guard = null;
	private boolean isDecideAction;

	int remainTalk;

	int remainWhisper;

//	JLabel remainTalkLabel;
//	JLabel remainWhisperLabel;

	AIWolfResource resource;

	Map<Integer, Boolean> dailyFinishMap;

	public UserActionPanel(AIWolfResource resource){
		this.resource = resource;
		this.agent = agent;
		
		gameInfoMap = new HashMap<Integer, GameInfo>();
		dailyFinishMap = new HashMap<>();
		
		setLayout(new FlowLayout());
		voteComboBox = new JComboBox<String>();
		attackVoteComboBox = new JComboBox<String>();
		divineComboBox = new JComboBox<String>();
		divineComboBox.addItemListener(this);
		guardComboBox = new JComboBox<String>();
		actionButton = new JButton("Action");
		actionButton.addActionListener(this);
		

		utteranceBox = new JComboBox<Item<Topic>>();
		utteranceBox.addItem(new Item<Topic>("---------", null));
		utteranceBox.addItem(new Item<Topic>(resource.convertTopic(Topic.VOTE), Topic.VOTE));
		utteranceBox.addItem(new Item<Topic>(resource.convertTopic(Topic.COMINGOUT), Topic.COMINGOUT));
		utteranceBox.addItem(new Item<Topic>(resource.convertTopic(Topic.DIVINED), Topic.DIVINED));
		utteranceBox.addItem(new Item<Topic>(resource.convertTopic(Topic.ESTIMATE), Topic.ESTIMATE));
		utteranceBox.addItem(new Item<Topic>(resource.convertTopic(Topic.GUARDED), Topic.GUARDED));
		utteranceBox.addItem(new Item<Topic>(resource.convertTopic(Topic.IDENTIFIED), Topic.IDENTIFIED));
		utteranceBox.addItem(new Item<Topic>(resource.convertTopic(Topic.AGREE), Topic.AGREE));
		utteranceBox.addItem(new Item<Topic>(resource.convertTopic(Topic.DISAGREE), Topic.DISAGREE));
		utteranceBox.addItemListener(this);
//		optionBox1 = new JComboBox<String>();
//		optionBox1.setVisible(false);
//		optionBox2 = new JComboBox<String>();
//		optionBox2.setVisible(false);
		talkButton = new JButton("Talk");
		talkButton.setEnabled(false);
		talkButton.addActionListener(this);
		skipButton = new JButton(resource.convertText(Talk.SKIP));
		skipButton.addActionListener(this);
		overButton = new JButton(resource.convertText(Talk.OVER));
		overButton.addActionListener(this);
		finishButton = new JButton(resource.convertText("finish"));
		finishButton.addActionListener(this);
		
		whisperUtteranceBox = new JComboBox<Item<Topic>>();
		whisperUtteranceBox.addItem(new Item<Topic>("---------", null));
		whisperUtteranceBox.addItem(new Item<Topic>(resource.convertTopic(Topic.ATTACK), Topic.ATTACK));
		whisperUtteranceBox.addItem(new Item<Topic>(resource.convertTopic(Topic.VOTE), Topic.VOTE));
		whisperUtteranceBox.addItem(new Item<Topic>(resource.convertTopic(Topic.COMINGOUT), Topic.COMINGOUT));
		whisperUtteranceBox.addItem(new Item<Topic>(resource.convertTopic(Topic.DIVINED), Topic.DIVINED));
		whisperUtteranceBox.addItem(new Item<Topic>(resource.convertTopic(Topic.ESTIMATE), Topic.ESTIMATE));
		whisperUtteranceBox.addItem(new Item<Topic>(resource.convertTopic(Topic.GUARDED), Topic.GUARDED));
		whisperUtteranceBox.addItem(new Item<Topic>(resource.convertTopic(Topic.IDENTIFIED), Topic.IDENTIFIED));
		whisperUtteranceBox.addItem(new Item<Topic>(resource.convertTopic(Topic.AGREE), Topic.AGREE));
		whisperUtteranceBox.addItem(new Item<Topic>(resource.convertTopic(Topic.DISAGREE), Topic.DISAGREE));
//
//		whisperUtteranceBox.addItem("---------");
//		whisperUtteranceBox.addItem("attack");
//		whisperUtteranceBox.addItem("vote");
//		whisperUtteranceBox.addItem("comingout");
//		whisperUtteranceBox.addItem("divined");
//		whisperUtteranceBox.addItem("estimate");
//		whisperUtteranceBox.addItem("guarded");
//		whisperUtteranceBox.addItem("inquested");
//		whisperUtteranceBox.addItem("agree");
//		whisperUtteranceBox.addItem("disagree");
//		whisperUtteranceBox.addItem("agree whisper");
//		whisperUtteranceBox.addItem("disagree whisper");
		whisperUtteranceBox.addItemListener(this);
//		whisperOptionBox = new JComboBox<String>();
//		whisperTalkButton = new JButton("Whisper");
//		whisperSkipButton = new JButton(Talk.SKIP);
//		whisperOverButton = new JButton(Talk.OVER);
		
		agreeTargetBox = new JComboBox<>();
		agreeTargetBox.addItem(TalkType.TALK);
		agreeTargetBox.addItem(TalkType.WHISPER);
		agreeTargetBox.addItemListener(this);
		agreeTargetBox.setVisible(false);
		
		speciesBox = new JComboBox<Item<Species>>();
		for(Species species:Species.values()){
			speciesBox.addItem(new Item<Species>(resource.convertSpecies(species), species));
		}
		roleBox = new JComboBox<Item<Role>>();
		for(Role role:Role.values()){
			if(role == Role.FREEMASON){
				continue;
			}
			roleBox.addItem(new Item<Role>(resource.convertRole(role), role));
		}
		targetBox = new JComboBox<String>();
		dayBox = new JComboBox<String>();
		talkIdBox = new JComboBox<Item<Integer>>();
		whisperIdBox = new JComboBox<Item<Integer>>();
		
		speciesBox.setVisible(false);
		roleBox.setVisible(false);
		targetBox.setVisible(false);
		dayBox.setVisible(false);
		talkIdBox.setVisible(false);
		whisperIdBox.setVisible(false);
		
		
		
	}

	public Agent getAgent() {
		return agent;
	}

	public void setAgent(Agent agent) {
		this.agent = agent;
	}

	/**
	 * 
	 * @param gameInfo
	 * @param gameSetting
	 */
	public void initialize(GameInfo gameInfo, GameSetting gameSetting) {
		this.gameSetting = gameSetting;
		this.gameInfo = gameInfo;
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		actionPanel = new JPanel();
		actionPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
		
		actionPanel.add(new JLabel("Vote"));
		actionPanel.add(voteComboBox);
		
		if(gameInfo.getRole() == Role.WEREWOLF){
			actionPanel.add(new JLabel("Attack"));
			actionPanel.add(attackVoteComboBox);
		}
		else if(gameInfo.getRole() == Role.SEER){
			actionPanel.add(new JLabel("Divine"));
			actionPanel.add(divineComboBox);
		}
		else if(gameInfo.getRole() == Role.BODYGUARD){
			actionPanel.add(new JLabel("Guard"));
			actionPanel.add(guardComboBox);
		}
		actionPanel.add(actionButton);
		actionPanel.setBackground(ACTION_COLOR);
		
		
		talkPanel = new JPanel();
		talkPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
		talkPanel.add(utteranceBox);
		talkPanel.add(whisperUtteranceBox);
		talkPanel.add(targetBox);
		talkPanel.add(speciesBox);
		talkPanel.add(roleBox);
		talkPanel.add(agreeTargetBox);
		talkPanel.add(dayBox);
		talkPanel.add(talkIdBox);
		talkPanel.add(whisperIdBox);
		talkPanel.add(talkButton);
		talkPanel.add(skipButton);
		talkPanel.add(overButton);
		talkPanel.add(finishButton);
		utteranceBox.setSelectedIndex(0);
		
		add(actionPanel);
		add(talkPanel);
		
		actionPanel.setVisible(false);
		talkPanel.setVisible(false);

		
		//Create Target Here
		targetBox.removeAllItems();
		for(Agent agent:new TreeSet<Agent>(gameInfo.getAgentList())){
			targetBox.addItem(resource.convertAgent(agent));
		}
	}
	
	
	
	public void dayStart(GameInfo gameInfo){
		this.gameInfo = gameInfo;
		gameInfoMap.put(gameInfo.getDay(), gameInfo);
		voteComboBox.removeAllItems();
		attackVoteComboBox.removeAllItems();
		divineComboBox.removeAllItems();
		guardComboBox.removeAllItems();
		for(Agent agent:new TreeSet<>(gameInfo.getAliveAgentList())){
			if(agent.equals(gameInfo.getAgent())){
				continue;
			}
			voteComboBox.addItem(resource.convertAgent(agent));
			if(gameInfo.getRoleMap().get(agent) != Role.WEREWOLF){
				attackVoteComboBox.addItem(resource.convertAgent(agent));
			}
			divineComboBox.addItem(resource.convertAgent(agent));
			guardComboBox.addItem(resource.convertAgent(agent));
		}
		
		//各ComboBoxはここで作成
		
		dayBox.removeAllItems();
		for(int i=0; i <= gameInfo.getDay(); i++){
			dayBox.addItem("Day "+i);
		}
		dayBox.setSelectedIndex(dayBox.getItemCount()-1);
		dayBox.addItemListener(this);
//		createTalkIdBox();
	}
	
	public void update(GameInfo gameInfo) {
		this.gameInfo = gameInfo;
		gameInfoMap.put(gameInfo.getDay(), gameInfo);
		utteranceBox.setSelectedIndex(0);
		whisperUtteranceBox.setSelectedIndex(0);
		createTalkWhisperIdBox();
	
	}

	public void talk(){
		if(dailyFinishMap.containsKey(gameInfo.getDay()) && dailyFinishMap.get(gameInfo.getDay())){
			talk = Talk.OVER;
		}
		else{
			talk = null;
			utteranceBox.setVisible(true);
			whisperUtteranceBox.setVisible(false);
			actionPanel.setVisible(false);
			talkButton.setText(resource.convertText("Talk")+"("+remainTalk+")");
			finishButton.setVisible(true);
	//		remainTalkLabel.setVisible(true);
	//		remainWhisperLabel.setVisible(false);
	
			talkPanel.setBackground(TALK_COLOR);
			talkPanel.setVisible(true);
			while(talk == null){
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			utteranceBox.setVisible(false);
			talkPanel.setVisible(false);
		}
	}
	
	/**
	 * 
	 */
	public void whisper(){
		talk = null;
		utteranceBox.setVisible(false);
		whisperUtteranceBox.setVisible(true);
		actionPanel.setVisible(false);
		talkButton.setText(resource.convertText("Whisper")+"("+remainWhisper+")");
		finishButton.setVisible(false);

//		remainTalkLabel.setVisible(false);
//		remainWhisperLabel.setVisible(true);

		talkPanel.setBackground(WHISPER_COLOR);
		talkPanel.setVisible(true);

		
		while(talk == null){
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}
		utteranceBox.setVisible(false);
		talkPanel.setVisible(false);
	}
	
	/**
	 * 
	 * @return
	 */
	public String getTalk(){
		return talk;
	}
	
	@Override
	public void itemStateChanged(ItemEvent e) {
		if(e.getSource() == utteranceBox || e.getSource() == whisperUtteranceBox){
			speciesBox.setVisible(false);
			roleBox.setVisible(false);
			targetBox.setVisible(false);
			dayBox.setVisible(false);
			talkIdBox.setVisible(false);
			whisperIdBox.setVisible(false);
			speciesBox.setVisible(false);
			agreeTargetBox.setVisible(false);

			talkButton.setEnabled(true);
			
			Topic topic = (Topic)((Item)e.getItem()).getValue();

			if(topic == Topic.AGREE || topic == Topic.DISAGREE){
				talkButton.setEnabled(true);
				createTalkWhisperIdBox();
				if(e.getSource() == utteranceBox){
					dayBox.setVisible(true);
					talkIdBox.setVisible(true);
					talkButton.setEnabled(true);
					if(talkIdBox.getItemCount() == 0){
						talkButton.setEnabled(false);
					}
				}
				else{
					dayBox.setVisible(true);
					agreeTargetBox.setVisible(true);
					talkButton.setEnabled(true);
					if(agreeTargetBox.getSelectedItem() == TalkType.TALK){
						talkIdBox.setVisible(true);
						whisperIdBox.setVisible(false);
						if(talkIdBox.getItemCount() == 0){
							talkButton.setEnabled(false);
						}
					}
					else{
						talkIdBox.setVisible(false);
						whisperIdBox.setVisible(true);
						if(whisperIdBox.getItemCount() == 0){
							talkButton.setEnabled(false);
						}
					}
				}
			}
			else if(topic == Topic.COMINGOUT){
				roleBox.setVisible(true);
			}
			else if(topic == Topic.DIVINED || topic == Topic.IDENTIFIED){
				targetBox.setVisible(true);
				speciesBox.setVisible(true);
			}
			else if(topic == Topic.ESTIMATE){
				targetBox.setVisible(true);
				roleBox.setVisible(true);
			}
			else if(topic == Topic.ATTACK){
				targetBox.setVisible(true);
			}
			else if(topic == Topic.GUARDED){
				targetBox.setVisible(true);
			}
			else if(topic == Topic.VOTE){
				targetBox.setVisible(true);
			}
			else{
				talkButton.setEnabled(false);
			}
		}
		else if(e.getSource() == agreeTargetBox){
			talkButton.setEnabled(true);
			if(e.getItem().equals(TalkType.TALK)){
				dayBox.setVisible(true);
				talkIdBox.setVisible(true);
				whisperIdBox.setVisible(false);
				talkButton.setEnabled(true);
				createTalkWhisperIdBox();
				if(talkIdBox.getItemCount() == 0){
					talkButton.setEnabled(false);
				}
			}
			else if(e.getItem().equals(TalkType.WHISPER)){
				dayBox.setVisible(true);
				talkIdBox.setVisible(false);
				whisperIdBox.setVisible(true);
				talkButton.setEnabled(true);
				createTalkWhisperIdBox();
				if(whisperIdBox.getItemCount() == 0){
					talkButton.setEnabled(false);
				}
			}
		}
		else if(e.getSource() == dayBox){
			talkButton.setEnabled(true);
			createTalkWhisperIdBox();
			if(agreeTargetBox.getSelectedItem() == TalkType.TALK && talkIdBox.getItemCount() == 0){
				talkButton.setEnabled(false);
			}
			else if(agreeTargetBox.getSelectedItem() == TalkType.WHISPER && whisperIdBox.getItemCount() == 0){
				talkButton.setEnabled(false);
			}
		}
//		else if(e.getSource() == divineComboBox){
//			System.err.println(divineComboBox.getSelectedItem());
//		}
	}

	/**
	 * talkIdBoxを作成する
	 */
	protected void createTalkWhisperIdBox() {
		int day = dayBox.getSelectedIndex();

		talkIdBox.removeAllItems();
		GameInfo dayGameInfo = gameInfoMap.get(day);
		if(dayGameInfo != null){
			List<Talk> talkList = dayGameInfo.getTalkList();
			for(Talk talk:talkList){
				if(!talk.isOver() && !talk.isSkip()){
					talkIdBox.addItem(new Item<Integer>(String.format("ID:%03d", talk.getIdx()), talk.getIdx()));
				}
			}
			
			whisperIdBox.removeAllItems();
			List<Talk> whisperList = dayGameInfo.getWhisperList();
			for(Talk whisper:whisperList){
				if(!whisper.isOver() && !whisper.isSkip()){
					whisperIdBox.addItem(new Item<Integer>(String.format("WID:%03d", whisper.getIdx()), whisper.getIdx()));
				}
			}
		}

//		if(agreeTargetBox.getSelectedItem() == TalkType.TALK && talkIdBox.getItemCount() == 0){
//			talkButton.setEnabled(false);
//		}
//		if(agreeTargetBox.getSelectedItem() == TalkType.WHISPER && whisperIdBox.getItemCount() == 0){
//			talkButton.setEnabled(false);
//		}
		
	}

	
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == talkButton){
			if(utteranceBox.isVisible()){
				createTalk();
			}
			else if(whisperUtteranceBox.isVisible()){
				createWhisper();
			}
		}
		else if(e.getSource() == skipButton){
			talk = Talk.SKIP;
		}
		else if(e.getSource() == overButton){
			talk = Talk.OVER;
		}
		else if(e.getSource() == finishButton){
			talk = Talk.OVER;
			dailyFinishMap.put(gameInfo.getDay(), true);
		}
		else if(e.getSource() == actionButton){
			isDecideAction = true;
			actionButton.setEnabled(false);
		}
		
	}

	/**
	 * 選択からTalkを作成する
	 */
	@SuppressWarnings("rawtypes")
	protected void createTalk() {
		Topic item = ((Item<Topic>)utteranceBox.getSelectedItem()).getValue();
		if(item == Topic.AGREE){
			talk = new Content(new AgreeContentBuilder(TalkType.TALK, dayBox.getSelectedIndex(), ((Item<Integer>) talkIdBox.getSelectedItem()).getValue())).getText();
		}
		else if(item == Topic.DISAGREE){
			talk = new Content(new DisagreeContentBuilder(TalkType.TALK, dayBox.getSelectedIndex(), ((Item<Integer>) talkIdBox.getSelectedItem()).getValue())).getText();
		}
		else if(item == Topic.COMINGOUT){
			talk = new Content(new ComingoutContentBuilder(resource.convertToAgent((String) targetBox.getSelectedItem()), (Role) ((Item) (roleBox.getSelectedItem())).getValue())).getText();
		}
		else if(item == Topic.ESTIMATE){
			talk = new Content(new EstimateContentBuilder(resource.convertToAgent((String) targetBox.getSelectedItem()), (Role) ((Item) (roleBox.getSelectedItem())).getValue())).getText();
		}
		else if(item == Topic.GUARDED){
			talk = new Content(new GuardedAgentContentBuilder(resource.convertToAgent((String) targetBox.getSelectedItem()))).getText();
		}
		else if(item == Topic.DIVINED){
			talk = new Content(new DivinedResultContentBuilder(resource.convertToAgent((String) targetBox.getSelectedItem()), (Species) ((Item) speciesBox.getSelectedItem()).getValue())).getText();
		}
		else if(item == Topic.IDENTIFIED){
			talk = new Content(new IdentContentBuilder(resource.convertToAgent((String) targetBox.getSelectedItem()), (Species) ((Item) speciesBox.getSelectedItem()).getValue())).getText();
		}
		else if(item == Topic.VOTE){
			talk = new Content(new VoteContentBuilder(resource.convertToAgent((String) targetBox.getSelectedItem()))).getText();
		}
	}

	/**
	 * 
	 */
	protected void createWhisper() {
//		String item = (String) whisperUtteranceBox.getSelectedItem();
		Topic item = ((Item<Topic>)whisperUtteranceBox.getSelectedItem()).getValue();
		if(item == Topic.AGREE){
			TalkType talkType = (TalkType) agreeTargetBox.getSelectedItem();
			if(talkType == TalkType.TALK){
				talk = new Content(new AgreeContentBuilder(talkType, dayBox.getSelectedIndex(), ((Item<Integer>) talkIdBox.getSelectedItem()).getValue())).getText();
			}
			else{
				talk = new Content(new AgreeContentBuilder(talkType, dayBox.getSelectedIndex(), ((Item<Integer>) whisperIdBox.getSelectedItem()).getValue())).getText();
			}
		}
		else if(item == Topic.DISAGREE){
			TalkType talkType = (TalkType) agreeTargetBox.getSelectedItem();
			if(talkType == TalkType.TALK){
				talk = new Content(new DisagreeContentBuilder(talkType, dayBox.getSelectedIndex(), ((Item<Integer>) talkIdBox.getSelectedItem()).getValue())).getText();
			}
			else{
				talk = new Content(new DisagreeContentBuilder(talkType, dayBox.getSelectedIndex(), ((Item<Integer>) whisperIdBox.getSelectedItem()).getValue())).getText();
			}
		}
		else if(item == Topic.COMINGOUT){
			talk = new Content(new ComingoutContentBuilder(resource.convertToAgent((String) targetBox.getSelectedItem()), (Role) ((Item) (roleBox.getSelectedItem())).getValue())).getText();
		}
		else if(item == Topic.ESTIMATE){
			talk = new Content(new EstimateContentBuilder(resource.convertToAgent((String) targetBox.getSelectedItem()), (Role) ((Item) (roleBox.getSelectedItem())).getValue())).getText();
		}
		else if(item == Topic.GUARDED){
			talk = new Content(new GuardedAgentContentBuilder(resource.convertToAgent((String) targetBox.getSelectedItem()))).getText();
		}
		else if(item == Topic.DIVINED){
			talk = new Content(new DivinedResultContentBuilder(resource.convertToAgent((String) targetBox.getSelectedItem()), (Species) ((Item) speciesBox.getSelectedItem()).getValue())).getText();
		}
		else if(item == Topic.IDENTIFIED){
			talk = new Content(new IdentContentBuilder(resource.convertToAgent((String) targetBox.getSelectedItem()), (Species) ((Item) speciesBox.getSelectedItem()).getValue())).getText();
		}
		else if(item == Topic.VOTE){
			talk = new Content(new VoteContentBuilder(resource.convertToAgent((String) targetBox.getSelectedItem()))).getText();
		}
		else if(item == Topic.ATTACK){
			talk = new Content(new AttackContentBuilder(resource.convertToAgent((String) targetBox.getSelectedItem()))).getText();
		}
//		
//		
//		if(item.equals("agree")){
//			talk = whisperFactory.agree(dayBox.getSelectedIndex(), talkIdBox.getSelectedIndex());
//		}
//		else if(item.equals("disagree")){
//			talk = whisperFactory.agree(dayBox.getSelectedIndex(), talkIdBox.getSelectedIndex());
//		}
//		else if(item.equals("comingout")){
//			talk = whisperFactory.comingout(talkConverter.convertToAgent((String)targetBox.getSelectedItem()), (Role)roleBox.getSelectedItem());
//		}
//		else if(item.equals("estimate")){
//			talk = whisperFactory.estimate(talkConverter.convertToAgent((String)targetBox.getSelectedItem()), (Role)roleBox.getSelectedItem());
//		}
//		else if(item.equals("guarded")){
//			talk = whisperFactory.guarded(talkConverter.convertToAgent((String)targetBox.getSelectedItem()));
//		}
//		else if(item.equals("divined")){
//			talk = whisperFactory.divined(talkConverter.convertToAgent((String)targetBox.getSelectedItem()), (Species)speciesBox.getSelectedItem());
//		}
//		else if(item.equals("inquested")){
//			talk = whisperFactory.inquested(talkConverter.convertToAgent((String)targetBox.getSelectedItem()), (Species)speciesBox.getSelectedItem());
//		}
//		else if(item.equals("vote")){
//			talk = whisperFactory.vote(talkConverter.convertToAgent((String)targetBox.getSelectedItem()));
//		}
//		else if(item.equals("attack")){
//			talk = whisperFactory.attack(talkConverter.convertToAgent((String)targetBox.getSelectedItem()));
//		}
		System.out.println(talk);
	}

	/**
	 * 
	 */
	public void clear() {
		vote = null;
		attackVote = null;
		divine = null;
		guard = null;
		isDecideAction = false;
	}

	/**
	 * 
	 */
	public void action() {
//		isDecideAction = false;
		
		actionPanel.setVisible(true);
		talkPanel.setVisible(false);
		actionButton.setEnabled(true);

		if(gameInfo.getDay() == 0){
			guardComboBox.setEnabled(false);
			voteComboBox.setEnabled(false);
			attackVoteComboBox.setEnabled(false);
		}
		else{
			guardComboBox.setEnabled(true);
			voteComboBox.setEnabled(true);
			attackVoteComboBox.setEnabled(true);
			
		}
		
		while(!isDecideAction){
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		actionButton.setEnabled(false);
		actionPanel.setVisible(false);
	}
	
	public Agent getVote(){
		return resource.convertToAgent((String)voteComboBox.getSelectedItem());
	}

	public Agent getAttack(){
		return resource.convertToAgent((String)attackVoteComboBox.getSelectedItem());
	}

	public Agent getDivine(){
		String targetName = (String)divineComboBox.getSelectedItem();
		Agent target = resource.convertToAgent(targetName);
//		System.err.printf("%s\t%s\n", targetName, target);
		return target;
	}

	public Agent getGuard(){
		return resource.convertToAgent((String)guardComboBox.getSelectedItem());
	}

	/**
	 * set remain talk number
	 * @param i
	 */
	public void setRemainTalk(int i) {
		remainTalk = i;
	}

	/**
	 * set remain whisper number
	 * @param i
	 */
	public void setRemainWhisper(int i) {
		remainWhisper = i;
	}

	/**
	 * 
	 * @param agent
	 */
	public void selectAgent(Agent agent){
		for(int i = 0; i < voteComboBox.getItemCount(); i++){
			String name = voteComboBox.getItemAt(i);
			if(resource.convertAgent(agent).equals(name)){
				voteComboBox.setSelectedIndex(i);
				continue;
			}
		}
		//TODO
	}
}


class Item<V>{
	String text;
	V value;
	
	public Item(String text, V v){
		this.text = text;
		this.value = v;
	}

	public V getValue(){
		return value;
	}
	
	public String toString(){
		return text;
	}
}
