package org.aiwolf.ui.log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import javax.swing.JFrame;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.net.GameSetting;
import org.aiwolf.ui.GameViewer;
import org.aiwolf.ui.res.AIWolfResource;
import org.aiwolf.ui.res.JapaneseResource;

public class GUILogViewer {

	File logFile;
	LogGameData gameData;
	ExGame game;

	GameViewer gameLogger;
	private AIWolfResource resource;

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		GUILogViewer lta = new GUILogViewer("../CEDEC2016/game/000.log.gz", null);
		lta.start();
	}

	/**
	 *
	 * @param logFileName
	 * @throws IOException
	 * @throws NumberFormatException
	 */
	public GUILogViewer(String logFileName, AIWolfResource resource) throws NumberFormatException, IOException {
		this(new File(logFileName), resource);
	}

	/**
	 *
	 * @param logFile
	 * @throws IOException
	 * @throws NumberFormatException
	 */
	public GUILogViewer(File logFile, AIWolfResource resource) throws NumberFormatException, IOException {
		super();
		this.logFile = logFile;

		init(resource);
	}

	private void init(AIWolfResource resource) throws FileNotFoundException, UnsupportedEncodingException, IOException {
		Map<Agent, Role> agentRoleMap = new LinkedHashMap<Agent, Role>();
		if (resource == null) {
			resource = new JapaneseResource();
		}
		BufferedReader br = getBufferedReader();
		String line;
		while ((line = br.readLine()) != null) {

			String[] data = line.split(",");
			if (!data[1].equals("status")) {
				break;
			}
			Agent agent = Agent.getAgent(Integer.parseInt(data[2]));
			Role role = Role.valueOf(data[3]);
			String name = data[5];
			agentRoleMap.put(agent, role);
			resource.setName(agent.getAgentIdx(), name);
		}
		br.close();

		GameSetting gameSetting = GameSetting.getDefaultGame(agentRoleMap.size());
		gameData = new LogGameData(gameSetting, agentRoleMap);
		game = new ExGame(gameSetting, gameData);
		gameLogger = new GameViewer(resource, game);
	}

	private BufferedReader getBufferedReader() throws IOException, FileNotFoundException, UnsupportedEncodingException {
		InputStream is;
		if (logFile.getName().endsWith("gz")) {
			is = new GZIPInputStream(new FileInputStream(logFile));
		} else {
			is = new FileInputStream(logFile);
		}
		InputStreamReader in = new InputStreamReader(is, "UTF-8");
		BufferedReader br = new BufferedReader(in);
		return br;
	}

	public void setResource(AIWolfResource resource) {
		gameLogger = new GameViewer(resource, game);
	}

	public AIWolfResource getResource() {
		return gameLogger.getResource();
	}

	public void start() throws NumberFormatException, IOException {

		// BufferedReader br = new BufferedReader(new FileReader(logFile));
		BufferedReader br = getBufferedReader();
		String line;
		while ((line = br.readLine()) != null) {
			gameData.addMessage(line);
			String[] data = line.split(",");
			if (data[1].equals("result")) {
				// gameLogger.log(line);
				gameLogger.close();
				break;
			}
			gameLogger.log(line);

		}
		br.close();
	}

	public void setCloseOnExist(boolean b) {
		if (b) {
			gameLogger.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		} else {
			gameLogger.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		}
	}

	/**
	 * @return gameLogger
	 */
	public GameViewer getGameLogger() {
		return gameLogger;
	}

}