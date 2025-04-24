package org.aiwolf.ui.bin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.net.GameSetting;
import org.aiwolf.common.util.CalendarTools;
import org.aiwolf.server.AIWolfGame;
import org.aiwolf.server.LostClientException;
import org.aiwolf.server.net.ServerListener;
import org.aiwolf.server.net.TcpipServer;
import org.aiwolf.server.util.FileGameLogger;
import org.aiwolf.server.util.GameLogger;
import org.aiwolf.server.util.MultiGameLogger;
import org.aiwolf.ui.GameViewer;
import org.aiwolf.ui.res.JapaneseResource;



/**
 * GUI server starter
 * @author tori
 *
 */
public class GUIServerStarter extends JFrame implements ActionListener, ServerListener, GameLogger{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final int MAX_AGENT = 18;
	private GridBagLayout gridBagLayout;
	private GridBagConstraints gbc;

	private JTextField portField;
//	private JTextField playerNumField;
	private JSpinner playerNumSpinner;
	private JCheckBox viewLogBox;
	private JCheckBox saveLogBox;
	private JTextField logDirField;
	
	private JButton waitButton;
	private JButton resetButton;
	private JButton startButton;
	
	protected List<JTextField> agentFieldList;
	protected Map<Agent, JTextField> agentFieldMap;
	
	protected JTextArea logArea;
	
	boolean isStart = false;
	protected TcpipServer gameServer;
	private JScrollPane logScrollPane;
	
	/**
	 * Game Viewer if exists
	 */
	private GameViewer gameLogViewer;

	static public void main(String[] args){
		
		GUIServerStarter guiServerStarter = new GUIServerStarter();
		guiServerStarter.setVisible(true);
		
	}

	public GUIServerStarter(){
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
//		UIManager.put("Button.font", new FontUIResource()); //Buttonのフォント設定
//		UIManager.put("Label.font",new Font("ＭＳ ゴシック", Font.PLAIN, 14)); //Labelのフォント設定
//		UIManager.put("List.font",new Font("ＭＳ ゴシック", Font.PLAIN, 14)); //Listのフォント設定
//		UIManager.put("ComboBox.font",new Font("ＭＳ ゴシック", Font.PLAIN, 14)); //ComboBoxのフォント設定
		try 
		{
//			System.out.println(UIManager.getSystemLookAndFeelClassName());
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		
		Font font = new Font("Meiryo", Font.PLAIN, 16);
		setTitle("AIWolf Server Starter");
		
		
		gridBagLayout = new GridBagLayout();
		setLayout(gridBagLayout);
		
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.insets = new Insets(2, 5, 2, 5);
		
		JLabel portLabel = new JLabel("port");
		portLabel.setFont(font);
		addComponent(portLabel, 0, 0);
		
		JLabel playerLabel = new JLabel("Num of players");
		playerLabel.setFont(font);
		addComponent(playerLabel, 0, 1);

		portField = new JTextField("10000");
		portField.setFont(font);
		addComponent(portField, 1, 0);

		SpinnerNumberModel model = new SpinnerNumberModel(15, 5, 18, 1);
		playerNumSpinner = new JSpinner(model);
		playerNumSpinner.setFont(font);
		addComponent(playerNumSpinner, 1, 1);
		
		waitButton = new JButton("Connect");
		waitButton.setFont(font);
		addComponent(waitButton, 0, 2);
		waitButton.addActionListener(this);

		resetButton = new JButton("Reset");
		resetButton.setFont(font);
		addComponent(resetButton, 1, 2);
		resetButton.addActionListener(this);

		////////////////////////////////////////////////////////
		
		JLabel logLabel = new JLabel("View log");
		logLabel.setFont(font);
		addComponent(logLabel, 0, 3);
		
		viewLogBox = new JCheckBox();
		addComponent(viewLogBox, 1, 3);

		JLabel saveLogLabel = new JLabel("Log Game");
		saveLogLabel.setFont(font);
		addComponent(saveLogLabel, 0, 4);
		
		saveLogBox = new JCheckBox();
		saveLogBox.setSelected(true);
		addComponent(saveLogBox, 1, 4);
		
		logDirField = new JTextField("./log");
		logDirField.setFont(font);
		logDirField.setPreferredSize(new Dimension(100, (int)logDirField.getPreferredSize().getHeight()));
		addComponent(logDirField, 2, 4);
		
		startButton = new JButton("Start Game");
		startButton.setEnabled(false);
		startButton.setFont(font);
		addComponent(startButton, 0, 5);
		startButton.addActionListener(this);
		////////////////////////////////////////////////////////

		agentFieldList = new ArrayList<>();
		for(int i = 0; i < MAX_AGENT; i++){
			JTextField agentField = new JTextField("");
//			agentField.setSize(100, 20);
			agentField.setPreferredSize(new Dimension(400, 20));
			agentField.setEnabled(false);
			agentField.setEditable(false);
			agentField.setBackground(null);
			gbc.gridx = 0;
			gbc.gridy = 6+i;
			gbc.gridwidth = 8;
			gbc.insets = new Insets(0, 5, 0, 5);
			gridBagLayout.setConstraints(agentField, gbc);
			add(agentField);
			
			agentFieldList.add(agentField);
		}
		agentFieldMap = new HashMap();

		logArea = new JTextArea();
		logArea.setEditable(false);
		logArea.setEnabled(true);
		logArea.setLineWrap(true);
		logArea.setFont(font);
		logScrollPane = new JScrollPane(logArea);
		logScrollPane.setPreferredSize(new Dimension(400, 500));
//		logPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		logScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(logScrollPane, BorderLayout.CENTER);
		gbc.gridx = 9;
		gbc.gridy = 0;
		gbc.gridheight = MAX_AGENT+4;
		gbc.insets = new Insets(0, 5, 0, 5);
		gridBagLayout.setConstraints(panel, gbc);
		add(panel);
		
		
		pack();
		
		setResizable(false);
	}

	private void addComponent(JComponent component, int x, int y) {
		gbc.gridx = x;
		gbc.gridy = y;
		gridBagLayout.setConstraints(component, gbc);
		add(component);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == waitButton){
			startServer();
		}
		else if(e.getSource() == startButton){
			startGame();
		}
		else if(e.getSource() == resetButton){
			reset();
		}
		
	}
	
	/**
	 * Start Aiwolf Server
	 */
	public void startServer(){
		if(isStart){
			return;
		}
		try{
			int port = getPort();
			int playerNum = getPlayerNum();
			GameSetting gameSetting = getGameSetting();
			gameServer = new TcpipServer(port, playerNum, gameSetting);
			gameServer.addServerListener(this);
			waitButton.setEnabled(false);
			portField.setEnabled(false);
			playerNumSpinner.setEnabled(false);
//			viewLogBox.setEnabled(false);
//			startButton.setEnabled(true);
			for(int i = 0; i < getPlayerNum(); i++){
				agentFieldList.get(i).setEnabled(true);
				agentFieldList.get(i).setBackground(Color.WHITE);
			}
			for(int i = getPlayerNum(); i < agentFieldList.size(); i++){
				agentFieldList.get(i).setBackground(null);
			}

			final GameLogger parentLogger = this;
			Runnable r = new Runnable() {
				
				@Override
				public void run() {
					try{
						gameServer.waitForConnection();
						startButton.setEnabled(true);
					}
					catch(LostClientException e){
						log("Lost Connection of "+e.getAgent());
					}
					catch (SocketTimeoutException e) {
						log(e.getMessage());
						e.printStackTrace();
					}catch(SocketException e){
						log(e.getMessage());
						e.printStackTrace();
					} catch (IOException e) {
						log(e.getMessage());
						e.printStackTrace();
					} finally {
						isStart = false;
					}
				}
			};
			
			Thread t = new Thread(r);
			t.start();
			isStart = true;

		}catch(NumberFormatException e){
			log("players must be number");
		}catch(IllegalArgumentException e){
			log(e.getMessage());
		}
	}
	
	/**
	 * stop the waiting of server
	 */
	public void stopServer(){
		gameServer.stopWaitingForConnection();
		waitButton.setEnabled(true);
		portField.setEnabled(true);
		playerNumSpinner.setEnabled(true);
		viewLogBox.setEnabled(true);
		startButton.setEnabled(false);
		for(int i = 0; i < getPlayerNum(); i++){
			agentFieldList.get(i).setEnabled(false);
		}
		for(int i = 0; i < agentFieldList.size(); i++){
			agentFieldList.get(i).setVisible(true);
		}
	}

	public void reset(){
		gameServer.close();
		gameServer = null;
		waitButton.setEnabled(true);
		startButton.setEnabled(false);
		playerNumSpinner.setEnabled(true);
		portField.setEnabled(true);
		for(JTextField agentField:agentFieldList){
			agentField.setBackground(null);
			agentField.setText("");
		}
	}
	
	/**
	 * Start Game
	 */
	public void startGame(){
		try{
			startButton.setEnabled(false);
			final AIWolfGame game = new AIWolfGame(getGameSetting(), gameServer);
			game.setRand(new Random());
			MultiGameLogger gameLogger = new MultiGameLogger();
			if(isSaveLog()){
				String logDir = getLogDir();
				try{
					File logFile = new File(String.format("%s/AIWolf%s.log", logDir, CalendarTools.toDateTime(System.currentTimeMillis()).replaceAll("[\\s-\\/:]", "")));
					logFile.getParentFile().mkdirs();
					gameLogger.add(new FileGameLogger(logFile));
					log("Log to "+logFile);
				}catch(IOException e){
					log(e.getMessage());
				}
			}
			if(isViewLog()){
				
				gameLogViewer = new GameViewer(new JapaneseResource(), game);
				gameLogViewer.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
//				gameFrame.setFocusable(true);
				gameLogger.add(gameLogViewer);
			}
			gameLogger.add(this);
			game.setGameLogger(gameLogger);
			Runnable r = new Runnable() {
				
				@Override
				public void run() {
					game.start();
					stopGame();
				}
			};
			Thread th = new Thread(r);
			th.start();
//			while(th.isAlive()){
//				try {
//					Thread.sleep(100);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//			}
//			
//			startButton.setEnabled(true);
		}catch(LostClientException e){
//			agentFieldList.get(e.getAgent().getAgentIdx()).setText("");
		}
	}
	
	public void stopGame(){
		startButton.setEnabled(true);
		if(gameLogViewer != null){

			JOptionPane.showMessageDialog(gameLogViewer, "Game Finished", "Game Finished", JOptionPane.INFORMATION_MESSAGE);
			gameLogViewer.setVisible(false);
			gameLogViewer.close();
			gameLogViewer = null;
		}
	}
	
	/**
	 * Loggind Directory
	 * @return
	 */
	public String getLogDir() {
		return logDirField.getText();
	}

	/**
	 * 
	 * @return
	 */
	public boolean isSaveLog() {
		return saveLogBox.isSelected();
	}

	public int getPort(){
		int port = Integer.parseInt(portField.getText());
		return port;
	}
	
	public int getPlayerNum(){
		int port = (Integer)playerNumSpinner.getValue();
		return port;
	}

	/**
	 * @return isViewLog
	 */
	public boolean isViewLog() {
		return viewLogBox.isSelected();
	}

	
	public GameSetting getGameSetting(){
		return GameSetting.getDefaultGame(getPlayerNum());
	}

	@Override
	public void connected(Socket socket, Agent agent, String name) {
		for(int i = 0; i < agentFieldList.size(); i++){
			if(agentFieldList.get(i).getText().isEmpty()){
				agentFieldMap.put(agent, agentFieldList.get(i));
				agentFieldList.get(i).setText(name);
				log("Here comes "+agent+" "+name);
				break;
			}
		}
		
	}

	@Override
	public void unconnected(Socket socket, Agent agent, String name) {
		if(agentFieldMap.containsKey(agent)){
			agentFieldMap.get(agent).setText("");
			agentFieldMap.remove(agent);
		}
	}

	/**
	 * @return gameServer
	 */
	public TcpipServer getGameServer() {
		return gameServer;
	}

	@Override
	public void log(String log) {
		logArea.append(log+"\n");
		JViewport viewport = logScrollPane.getViewport();
		Point p = viewport.getViewPosition();
		p.y += logScrollPane.getFont().getSize()*3;
		viewport.setViewPosition(p);
	}

	@Override
	public void flush() {
	}

	@Override
	public void close() {
	}

	
}
