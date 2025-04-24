package org.aiwolf.ui.bin;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.UIManager;

import org.aiwolf.common.data.Player;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.net.TcpipClient;


/**
 * GUI server starter
 * @author tori
 *
 */
public class GUIClientStarter extends JFrame implements ActionListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final int MAX_AGENT = 18;
	private GridBagLayout gridBagLayout;
	private GridBagConstraints gbc;

	private JTextField portField;
	private JCheckBox viewLogBox;
	private JCheckBox saveLogBox;
	private JTextField hostField;
	
	private JButton waitButton;
	private JButton addAgentButton;
	
	private ClientSelectPanel clientSelectPanel;
	
	protected List<JTextField> agentFieldList;
	
	boolean isStart = false;

	File initFile = new File("client.ini");
	
	static public void main(String[] args){
		
		GUIClientStarter guiServerStarter = new GUIClientStarter();
		guiServerStarter.setVisible(true);
		
	}

	public GUIClientStarter(){
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
//		UIManager.put("Button.font", new FontUIResource()); //Buttonのフォント設定
//		UIManager.put("Label.font",new Font("ＭＳ ゴシック", Font.PLAIN, 14)); //Labelのフォント設定
//		UIManager.put("List.font",new Font("ＭＳ ゴシック", Font.PLAIN, 14)); //Listのフォント設定
//		UIManager.put("ComboBox.font",new Font("ＭＳ ゴシック", Font.PLAIN, 14)); //ComboBoxのフォント設定
		try 
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		
		Font font = new Font("Meiryo", Font.PLAIN, 16);
		setTitle("AIWolf Client Starter");
		
		
		gridBagLayout = new GridBagLayout();
		setLayout(gridBagLayout);
		
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.insets = new Insets(2, 5, 2, 5);
		
		JLabel portLabel = new JLabel("port");
		portLabel.setFont(font);
		addComponent(portLabel, 0, 1);

		portField = new JTextField("10000");
		portField.setFont(font);
		addComponent(portField, 1, 1);

		JLabel hostLabel = new JLabel("host");
		hostLabel.setFont(font);
		addComponent(hostLabel, 0, 2);

		hostField = new JTextField("localhost");
		hostField.setPreferredSize(new Dimension(200, portField.getPreferredSize().height));
		hostField.setFont(font);
		addComponent(hostField, 1, 2);

		
		addAgentButton = new JButton("Connect Agent");
		addAgentButton.setFont(font);
		addComponent(addAgentButton, 0, 3);
		addAgentButton.addActionListener(this);

//		waitButton = new JButton("Connect");
//		waitButton.setFont(font);
//		addComponent(waitButton, 1, 3);
//		waitButton.addActionListener(this);

		////////////////////////////////////////////////////////
		
		clientSelectPanel = new ClientSelectPanel();
		clientSelectPanel.setFont(font);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.insets = new Insets(0, 5, 0, 5);
		gridBagLayout.setConstraints(clientSelectPanel, gbc);
		add(clientSelectPanel);
//		addComponent(clientSelectPanel, 0, 0);
		
//		JLabel logLabel = new JLabel("View log");
//		logLabel.setFont(font);
//		addComponent(logLabel, 0, 3);
//		
//		viewLogBox = new JCheckBox();
//		addComponent(viewLogBox, 1, 3);
//
//		JLabel saveLogLabel = new JLabel("Log Game");
//		saveLogLabel.setFont(font);
//		addComponent(saveLogLabel, 0, 4);
//		
//		saveLogBox = new JCheckBox();
//		saveLogBox.setSelected(true);
//		addComponent(saveLogBox, 1, 4);
//		
//		logDirField = new JTextField("./log");
//		logDirField.setFont(font);
//		logDirField.setPreferredSize(new Dimension(100, (int)logDirField.getPreferredSize().getHeight()));
//		addComponent(logDirField, 2, 4);

		////////////////////////////////////////////////////////

		agentFieldList = new ArrayList<>();
		for(int i = 0; i < MAX_AGENT; i++){
			JTextField agentField = new JTextField("");
//			agentField.setSize(100, 20);
			agentField.setPreferredSize(new Dimension(600, 20));
			agentField.setEnabled(false);
			agentField.setEditable(false);
			agentField.setBackground(null);
			gbc.gridx = 0;
			gbc.gridy = 4+i;
			gbc.gridwidth = 8;
			gbc.insets = new Insets(0, 5, 0, 5);
			gridBagLayout.setConstraints(agentField, gbc);
			add(agentField);
			
			agentFieldList.add(agentField);
		}

//		JPanel panel = new JPanel();
//		panel.setLayout(new BorderLayout());
//		panel.add(logScrollPane, BorderLayout.CENTER);
//		gbc.gridx = 9;
//		gbc.gridy = 0;
//		gbc.gridheight = MAX_AGENT+4;
//		gbc.insets = new Insets(0, 5, 0, 5);
//		gridBagLayout.setConstraints(panel, gbc);
//		add(panel);
//		
//		
		pack();
		
		setResizable(false);
		
		Runnable r = new Runnable() {
			
			@Override
			public void run() {
				load(initFile);
			}
		};
		Thread th = new Thread(r);
		th.start();
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
			startClient();
		}
		else if(e.getSource() == addAgentButton){
			startClient();
		}
//		else if(e.getSource() == addAgentButton){
//			reset();
//		}
		
	}
	
	/**
	 * Start Aiwolf Client
	 */
	public void startClient(){
		try{
			save(initFile);
			
			Class<Player> playerClass = clientSelectPanel.getSelectedClass();
			if(playerClass == null){
				return;
			}
			final Player player = playerClass.newInstance();
			final Role role = clientSelectPanel.getSelectedRole();
			final int port = getPort();
			final String host = hostField.getText();
			
			Runnable r = new Runnable() {
				
				@Override
				public void run() {
					TcpipClient client = new TcpipClient(host, port, role);
					if(client.connect(player)){
						System.out.println("Player connected to server:"+player);
						for(JTextField field: agentFieldList){
							if(field.getText().isEmpty()){
								StringBuffer buf = new StringBuffer();
								buf.append(host);
								buf.append(":");
								buf.append(port);
								buf.append(" ");
								buf.append(String.format("%s(%s)", player.getName(), role));
								field.setText(buf.toString());
								break;
							}
						}
					}
					else{
						
					}
				}
			};
			
			Thread t = new Thread(r);
			t.start();
			isStart = true;

		}catch(NumberFormatException e){
//			log("players must be number");
		}catch(IllegalArgumentException e){
//			log(e.getMessage());
		} catch (InstantiationException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}
	
	public int getPort(){
		int port = Integer.parseInt(portField.getText());
		return port;
	}
	
	public String getHost(){
		return hostField.getText();
	}

	/**
	 * 
	 * @param file
	 */
	public void save(File file){
		Properties p = new Properties();
		p.setProperty("host", getHost());
		p.setProperty("port", getPort()+"");
		StringBuffer buf = new StringBuffer();
		for(File jarFile:clientSelectPanel.getJarFileList()){
			buf.append(jarFile.getAbsolutePath());
			buf.append(",");
		}
		p.setProperty("jars", buf.toString());
		
		try {
			FileWriter fw = new FileWriter(file);
			p.store(fw, "Clients");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param file
	 */
	public void load(File file){
		try {
			if(!file.exists()){
				return;
			}
			
			FileReader fr = new FileReader(file);
			Properties p = new Properties();
			p.load(fr);
			fr.close();

			hostField.setText(p.getProperty("host"));
			portField.setText(p.getProperty("port"));
			String[] jars = p.getProperty("jars").split(",");
			for(String jar:jars){
				clientSelectPanel.addJarFile(new File(jar));
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
}
