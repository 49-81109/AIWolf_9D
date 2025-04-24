package org.aiwolf.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.TalkType;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.data.Status;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.data.Team;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;
import org.aiwolf.server.util.GameLogger;
import org.aiwolf.ui.log.LogGameData;
import org.aiwolf.ui.res.AIWolfResource;

public class GameViewer2 extends JFrame implements GameLogger, ActionListener{

	private static final int ACTION_PANEL_HEIGHT = 30;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	
	public static final int DEFAULT_WAIT_TIME = 500;
	/**
	 * 
	 */
	public static final int PANEL_WIDTH = 960;
	
	/**
	 * 
	 */
	public static final int PANEL_HEIGHT = 800;

	/**
	 * Color of werewolves
	 */
	public static final Color WHISPER_COLOR = new Color(255, 128, 128);

	/**
	 * Color of Villegers
	 */
	public static final Color TALK_COLOR = Color.WHITE;

	/**
	 * color of action and information
	 */
	public static final Color ACTION_COLOR = new Color(128, 255, 128);

	/**
	 * Color of player agent
	 */
	public static final Color PLAYER_COLOR = new Color(255, 255, 128);
	
	/**
	 * Color of agent who have same role
	 */
	public static final Color FRIEND_COLOR = new Color(255, 255, 196);

	/**
	 * Color of agent who have same role
	 */
	public static final Color IMPORTANT_COLOR = new Color(240, 240, 255);

	LogGameData gameData;
	
	/**
	 *  
	 */
	protected boolean isInitialized;
	
	protected InformationPanel infoPanel;
	protected UserActionPanel userActionPanel;

	/**
	 * Resource
	 */
	protected AIWolfResource resource;

	/**
	 * Main panel for GUI
	 */
	protected JPanel mainPanel;
	
	protected GameSetting gameSetting;
	
	protected GameInfo gameInfo;

	/**
	 * 
	 */
//	protected boolean skip;
//	protected boolean step;
//	protected long waitTime = DEFAULT_WAIT_TIME;
//	protected JButton autoButton;
//	protected JButton nextButton;
//	protected JButton skipAllButton;
	protected NextButtonPanel stepActionPanel;

	protected boolean isAutoClose;
	
	
	/**
	 * 
	 * @param resource
	 * @game 
	 */
	public GameViewer2(AIWolfResource resource, GameSetting gameSetting, Map<Agent, Role> agentRoleMap){

		this.resource = resource;
		this.gameSetting = gameSetting;
		
		isInitialized = false;
//		samplePlayer = new SampleRoleAssignPlayer();
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel");
			SwingUtilities.updateComponentTreeUI(this);
		} catch (Exception e) {
//			e.printStackTrace();
		}

		setBounds(10, 10, PANEL_WIDTH, PANEL_HEIGHT);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		mainPanel = new JPanel();
		mainPanel.setOpaque(false);
		mainPanel.setDoubleBuffered(true);
		mainPanel.setLayout(new BorderLayout());
		mainPanel.setBackground(Color.WHITE);
//		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

		infoPanel = new InformationPanel(resource);
		infoPanel.setPreferredSize(new Dimension(HumanPlayer.PANEL_WIDTH, getHeight()-ACTION_PANEL_HEIGHT));
		
//		talkPanel = new TalkPanel(resource);
//		talkPanel.setPreferredSize(new Dimension(HumanPlayer.PANEL_WIDTH, 200));

//		userActionPanel = new UserActionPanel(resource);
//		userActionPanel.setPreferredSize(new Dimension(HumanPlayer.PANEL_WIDTH, ACTION_PANEL_HEIGHT));

		JPanel titlePanel = new JPanel();
		titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.X_AXIS));
		JPanel logoPanel = new JPanel(new FlowLayout());
		URL url=getClass().getClassLoader().getResource("img/aiwolfLogo.png");
		ImageIcon logoIcon = new ImageIcon(url);
		JLabel logoLabel = new JLabel(logoIcon);
		logoPanel.add(logoLabel);
		logoPanel.setBackground(new Color(196,196, 196));
//		titlePanel.add(logoLabel);
//		titlePanel.setBackground(new Color(196,196, 196));
		titlePanel.add(logoPanel);

		
		mainPanel.add(titlePanel, BorderLayout.NORTH);
		mainPanel.add(infoPanel, BorderLayout.CENTER);
//		mainPanel.add(talkPanel, BorderLayout.CENTER);
//		mainPanel.add(userActionPanel, BorderLayout.SOUTH);

//		nextButton = new JButton("NEXT");
//		nextButton.addActionListener(this);
//		
//		autoButton = new JButton("Auto");
//		autoButton.addActionListener(this);

		stepActionPanel = new NextButtonPanel(resource);
		stepActionPanel.setPreferredSize(new Dimension(HumanPlayer.PANEL_WIDTH, ACTION_PANEL_HEIGHT));
		this.mainPanel.add(stepActionPanel, BorderLayout.SOUTH);
		
		getContentPane().add(mainPanel, BorderLayout.CENTER);
		
		gameData = new LogGameData(gameSetting, agentRoleMap);
	}
	
	public void initialize(GameInfo gameInfo, GameSetting gameSetting){
		this.gameSetting = gameSetting;

		infoPanel.initialize(gameInfo, gameSetting);
		update(gameInfo);
		
		setVisible(true);
		infoPanel.setWaitListener(stepActionPanel);
		
		infoPanel.firstInformation(gameInfo, gameSetting);
	}
	
	public void update(GameInfo gameInfo){
		infoPanel.update(gameInfo);

		for(Talk talk:gameInfo.getTalkList()){
			Content u = new Content(talk.getText());
			if(u.getTopic() == Topic.COMINGOUT){
				infoPanel.setComingOut(talk.getAgent(), u.getRole());
			}
			else if(u.getTopic() == Topic.DIVINED){
				infoPanel.setComingOut(talk.getAgent(), Role.SEER);
			}
			else if(u.getTopic() == Topic.IDENTIFIED){
				infoPanel.setComingOut(talk.getAgent(), Role.MEDIUM);
			}
		}
		
		this.gameInfo = gameInfo;

//		repaint();
	}

	
	
	Talk lastTalk;
	Talk lastWhisper;

	int lastTurn = -1;

	/**
	 * update Talk
	 * @param gameInfo
	 */
	protected void updateTalk(GameInfo gameInfo) {
		boolean waitBeforeTalk = false;
		for(int i = infoPanel.getLastTalkIdx(); i < gameInfo.getTalkList().size(); i++){
			Talk talk = gameInfo.getTalkList().get(i);
			if(lastTurn == -1){
				lastTurn = talk.getTurn();
			}
			
			if(lastTalk != null && lastTalk.getAgent() == talk.getAgent() && lastTalk.getText().equals(talk.getText()) && lastTalk.getDay() == talk.getDay()){
				continue;
			}

			if(lastTurn != talk.getTurn()){
//				stepActionPanel.waitForNext();
				lastTurn = talk.getTurn();
				waitBeforeTalk = true;
//				waitSecond();
			}

			if(waitBeforeTalk && !talk.isSkip() && !talk.isOver()){
				stepActionPanel.waitForNext();
				waitBeforeTalk = false;
			}
			boolean isUpdated = infoPanel.updateTalk(gameInfo.getDay(), talk, TalkType.TALK);
			if(isUpdated){
				infoPanel.update(gameInfo);
				lastTalk = talk;
			}
		}
//		if(waitAfterTalk){
//			stepActionPanel.waitForNext();
//		}
		
		for(int i = infoPanel.getLastWhisperIdx(); i < gameInfo.getWhisperList().size(); i++){
			Talk whisper = gameInfo.getWhisperList().get(i);

			if(lastWhisper != null){
				if(lastWhisper.getAgent() == whisper.getAgent() && lastWhisper.getText().equals(whisper.getText()) && lastWhisper.getDay() == whisper.getDay()){
					continue;
				}
			}
			
			boolean isUpdated = infoPanel.updateTalk(gameInfo.getDay(), whisper, TalkType.WHISPER);
			if(isUpdated){
				infoPanel.scrollToTail();
				lastWhisper = whisper;
//				waitSecond();
				stepActionPanel.waitForNext();
			}
		}
//		talkPanel.updateWhisper(gameInfo.getDay(), gameInfo.getWhisperList());
	}

	
	public void dayStart() {
		if(gameInfo.getDay() != 0){
			stepActionPanel.auto(false);
		}
//		userActionPanel.clear();
		infoPanel.dayStart(gameInfo);
//		if(infoPanel.voteResult(gameInfo)){
//			waitForNext();
//		}
		
//		userActionPanel.dayStart(gameInfo);
		lastTurn = -1;
	}
//
	int lastDay = -1;
	@Override
	public void log(String log) {
		GameInfo gameInfo = gameData.getGameInfo();

		if(!isInitialized){
			initialize(gameInfo, gameSetting);
			isInitialized = true;
		}

		update(gameInfo);
		if(gameInfo.getDay() != lastDay){
			dayStart();
			lastDay = gameInfo.getDay();
		}

		
		updateTalk(gameInfo);
		System.out.println(log);
	}

	@Override
	public void flush() {
	}

	@Override
	public void close() {
//		infoPanel.dayStart(gameInfo);
		int humanSide = 0;
		int wolfSide = 0;
		for(Agent agent:gameInfo.getAgentList()){
			if(gameInfo.getStatusMap().get(agent) == Status.DEAD){
				continue;
			}
			if(gameInfo.getRoleMap().get(agent).getSpecies() == Species.HUMAN){
				humanSide++;
			}
			else{
				wolfSide++;
			}
		}

		Team winner = null;
		if(wolfSide == 0){
			winner = Team.VILLAGER;
		}
		else if(wolfSide >= humanSide){
			winner = Team.WEREWOLF;
		}

		infoPanel.setWinner(gameInfo.getDay(), winner);
		stepActionPanel.waitSecond();
		infoPanel.scrollToTail();

		if(isAutoClose){
			setVisible(false);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
//		if(e.getSource() == nextButton){
//			step = true;
//			nextButton.setEnabled(false);
//		}
//		else if(e.getSource() == autoButton){
//			if(!skip){
//				stepActionPanel.auto(true);
//			}
//			else{
//				stepActionPanel.auto(false);
//			}
//		}
//		else if(e.getSource() == skipAllButton){
////			step = true;
////			skip = true;
//			waitTime = 0;
//			stepActionPanel.auto(true);
//		}
//		
	}

	/**
	 * @return resource
	 */
	public AIWolfResource getResource() {
		return resource;
	}

	/**
	 * @param resource セットする resource
	 */
	public void setResource(AIWolfResource resource) {
		this.resource = resource;
		infoPanel.setResource(resource);
	}

	public void setAlwaysAuto(boolean b) {
		stepActionPanel.setAlwaysAuto(b);
		
	}

	public void setAutoClose(boolean b) {
		isAutoClose = b;
	}
	
	public boolean isAutoClose(){
		return isAutoClose;
	}

}
