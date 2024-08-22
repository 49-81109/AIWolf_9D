package org.aiwolf.sample.player;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Judge;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.sample.player.arrange_tool.output;
import org.aiwolf.sample.player.arrange_tool.readData;
/**
 * 盤面整理ツールとつなげるための関数
 * 
 */
public class ArrangeToolLink {
	private final SampleBasePlayer base;
	private final int day;
	private final String input;
	private readData read;
	private output output1;
	private String[][] data;
	private Map<Role, String> roleName = new HashMap<>();
	// 狂人いれるとバグるので注意
	final String roles;
	final private List<Role> rolelabel;
	final int roleNum;
	
 	ArrangeToolLink(SampleBasePlayer base) {
		this.base = base;
		this.day = base.day;
		
		roleName.put(Role.VILLAGER, "村人");
		roleName.put(Role.SEER, "占い師");
		roleName.put(Role.MEDIUM, "霊能者");
		roleName.put(Role.BODYGUARD, "狩人");
		roleName.put(Role.WEREWOLF, "人狼");
		roleName.put(Role.POSSESSED, "狂人");
		roleName.put(Role.FOX, "妖狐");
		roleName.put(Role.IMMORALIST, "背徳者");
		roleNum = base.playerNum;
		List<Role> roleLabel = new ArrayList<>();
		for(Role role : Role.values()) {
			for(int i = 0; i < base.roleCast.get(role); i++) {
				if(role != Role.TOXIC) {
					roleLabel.add(role);
				}
			}
		}
		for(int i = 0; i < base.roleCast.get(Role.TOXIC); i++) {
			roleLabel.add(Role.TOXIC);
		}
		rolelabel = roleLabel;
		String roles = "role=[";
		for(Role role : Role.values()) {
			if(base.roleCast.get(role) > 0) {
				if(role == Role.FOX) {
					roles = roles + "foxspirit:" + base.roleCast.get(role) + ",";
				}
				else {
					roles = roles + role.toString().toLowerCase() + ":" + base.roleCast.get(role) + ",";
				}
			}
		}
		roles = roles.substring(0, roles.length() - 1) + "];\n";
		this.roles = roles;
		this.input = makeInput();
//		System.out.println(input);
		executeTotalArrange();
	}
	
	private final String makeInput() {
		// プレイヤーリスト
		String input = roles;
		input = input + "player=[";
		for(int i = 0; i < base.currentGameInfo.getAgentList().size(); i++) {
			input = input + "[" + base.currentGameInfo.getAgentList().get(i).getAgentIdx() + "]" +  base.currentGameInfo.getAgentName(base.currentGameInfo.getAgentList().get(i)) + ":" + base.currentGameInfo.getAgentList().get(i).getAgentIdx();
			if(i < base.currentGameInfo.getAgentList().size() - 1) {
				input = input + ",";
			}
			else {
				input = input + "];\n";
			}
		}
		
		// 犠牲者
		input = input + "victim=[";
		int ac = 0;
		for(List<Agent> dayVic : base.victimAgents) {
			ac++;
			if(ac > 1) {
				input = input + "{";
				int c = 0;
				for(Agent vi : dayVic) {
					c++;
					input = input + vi.getAgentIdx();
					if(dayVic.size() > c) {
						input = input + ",";
					}
				}
				input = input + "}";
				if(base.victimAgents.size() > ac) {
					input = input + ",";
				}
			}
		}
		input = input + "];\n";
		ac = 0;
		// 追放者
		input = input + "expelled=[";
		for (Agent exe : base.executedAgents) {
			ac++;
			input = input + exe.getAgentIdx();
			if(base.executedAgents.size() > ac) {
				input = input + ",";
			}
		}
		input = input + "];\n";
		base.resultSort();
		// 初日呪殺
		if(base.victimAgents.size() > 0) {
			if(base.victimAgents.get(0).size() > 0) {
				input = input + "firstVictim=[";
				int c = 0;
				for(Agent vi : base.victimAgents.get(0)) {
					c++;
					input = input + vi.getAgentIdx();
					if(base.victimAgents.get(0).size() > c) {
						input = input + ",";
					}
				}
				input = input + "];\n";
			}
		}
		
		// 妖狐追放での背徳者の道連れ
		if(base.executedFoxDay != -1) {
			input = input + "suicideDay=" + base.executedFoxDay + ";\nsuicide=[";
			int c = 0;
			for(Agent vi : base.suicide) {
				c++;
				input = input + vi.getAgentIdx();
				if(base.suicide.size() > c) {
					input = input + ",";
				}
			}
			input = input + "];\n";
		}
		
		// 占い師CO
		for(Agent seer : base.currentGameInfo.getAgentList()) {
			String seerCOstr = "";
			if(base.comingoutMap.get(seer) == Role.SEER) {
				seerCOstr = "seerCO=[" + seer.getAgentIdx() + ",{";
				List<Judge> seerResult = getSeerResult(seer);
				int divD = 1;
				for(Judge j : seerResult) {
					if(j.getDay() == divD) {
						seerCOstr = seerCOstr + j.getTarget().getAgentIdx() + ":";
						if(j.getResult() == Species.HUMAN) {
							seerCOstr = seerCOstr + "false";
						}
						else {
							seerCOstr = seerCOstr + "true";
						}
						seerCOstr = seerCOstr + ",";
						divD++;
					}
				}
				if(seerResult.size() > 0) {
					seerCOstr = seerCOstr.substring(0, seerCOstr.length() - 1);
				}
				seerCOstr = seerCOstr + "}," + base.CODayMap.get(seer) + "];\n";
			}
			input = input + seerCOstr;
		}
		return input;
	}
	
	private final List<Judge> getSeerResult(Agent seer) {
		List<Judge> seerResult = new ArrayList<>();
		for(Judge j : base.getDivinationList()) {
			if(j.getAgent() == seer) {
				seerResult.add(j);
			}
		}
		return seerResult;
	}
	
	private void executeTotalArrange() {
		String fname = "src/org/aiwolf/sample/player/arrange_tool/data/" + base.me.getName() + "_" + day + ".txt";
		try {
			File file = new File(fname);
			FileWriter filewriter = new FileWriter(file);
			
			filewriter.write(input);
			filewriter.close();
			
			//盤面整理ツールの実行
			read = new readData(base.me.getName() + "_" + day);
			output1 = new output(read);
//			output1.printInput();
			data = output1.nonPrintArrangement();
		} catch (IOException e) {
			System.out.println("An error occurred. : ArrangeToolLink 167");
			e.printStackTrace();
			System.exit(1);
        }
	}

	/** 村視点での盤面整理実行 */
	String[][] executeArrangement() {
		String fname = "src/org/aiwolf/sample/player/arrange_tool/data/" + base.me.getName() + "_" + day + ".txt";
		if(output1 != null) {
			return output1.copyData(data);
		}
		try {
			System.out.println("本来呼び出されるはずがない場所");
			File file = new File(fname);
			FileWriter filewriter = new FileWriter(file);
			
			filewriter.write(input);
			filewriter.close();
			
			//盤面整理ツールの実行
			read = new readData(base.me.getName() + "_" + day);
			output1 = new output(read);
//			output1.printInput();
			data = output1.nonPrintArrangement();
			return data;
			
		} catch (IOException e) {
			System.out.println("An error occurred. : ArrangeToolLink 167");
			e.printStackTrace();
			System.exit(1);
			return null;
        }
	}
	
	/** 各視点での盤面整理実行 */
	String[][] executeArrangement(Agent agent, Role role) {
		return executeArrangement(data, agent, role, true);
	}
	
	String[][] executeArrangement(String[][] data, Agent agent, Role role) {
		return executeArrangement(data, agent, role, true);
	}
	
	String[][] executeArrangement(String[][] data, Agent agent, Role role, boolean isDisiOrNot) {
		if(data == null) {
			data = executeArrangement();
			/*
			System.out.println("*****");
			printroleCandidate(data);
			System.out.println("*****");
			//*/
		}
		String[][] optional;
		if(isDisiOrNot) {
			optional = output1.nonPrintOptional(agent.getAgentIdx(), roleName.get(role), data, output1.getTable());
		}
		else {
			optional = output1.nonPrintOptionalDelete(agent.getAgentIdx(), roleName.get(role), data, output1.getTable());
		}
		return optional;
	}
	
	/** 入力データ確認 */
	void printInput() {
		if(output1 == null) {
			return;
		}
		output1.printInput();
	}
	
	/** dataからagentの役職候補を取得 */
	List<Role> roleCandidate(String[][] data, Agent agent) {
		List<Role> roles = new ArrayList<>();
		for(int i = 0; i < roleNum; i++) {
			if(!data[agent.getAgentIdx()-1][i].equals("×")) {
				if(!roles.contains(rolelabel.get(i))) {
					roles.add(rolelabel.get(i));
				}
			}
		}
		return roles;
	}
	
	/** dataから役職のagent候補を取得 */
	List<Agent> agentCandidate(String[][] data, Role role) {
		List<Agent> agents = new ArrayList<>();
		int roleId = -1;
		for(int i = 0; i < roleNum; i++) {
			if(rolelabel.get(i) == role) {
				roleId = i;
				break;
			}
		}
		if(roleId == -1) {
			return agents;
		}
		for(int i = 0; i < roleNum; i++) {
			if(!data[i][roleId].equals("×")) {
				agents.add(Agent.getAgent(i+1));
			}
		}
		return agents;
	}
	
	/** dataから役職のagentを取得 */
	List<Agent> agentDisition(String[][] data, Role role) {
		List<Agent> agents = new ArrayList<>();
		int roleId = -1;
		for(int i = 0; i < roleNum; i++) {
			if(rolelabel.get(i) == role) {
				roleId = i;
				break;
			}
		}
		if(roleId == -1) {
			return agents;
		}
		for(int i = 0; i < roleNum; i++) {
			if(data[i][roleId].equals("○")) {
				agents.add(Agent.getAgent(i+1));
			}
		}
		return agents;
	}
	
	/** dataを表示 */
	void printData(String[][] data) {
		output1.printData(data);
	}
	
	/** すべてのAgentに対して候補役職を表示 */
	void printroleCandidate(String[][] data) {
		System.out.println("-------Role Candidate-------");
		for(Agent a : base.currentGameInfo.getAgentList()) {
			List<Role> roles = roleCandidate(data, a);
			System.out.print("[" + a.getAgentIdx() + "]" + a.getName() + " : ");
			for(Role r : roles) {
				System.out.print(r + " ");
			}
			System.out.println();
		}
		System.out.println("----------------------------");
	}
	
	/** すべての役職に対して候補Agentを表示 */
	void printagentCandidate(String[][] data) {
		System.out.println("-------Agent Candidate-------");
		for(Role role : Role.values()) {
			int cast = 0;
			for(Role r : rolelabel) {
				if(r == role) {
					cast++;
				}
			}
			if(cast > 0) {
				System.out.print(role + " (" + cast + ") : ");
				List<Agent> agents = agentCandidate(data, role);
				for(Agent a : agents) {
					System.out.print("[" + a.getAgentIdx() + "]" + a.getName() + " ");
				}
				System.out.println();
			}
		}
		System.out.println("-----------------------------");
	}

	/** ステータス表示 */
	void printStatus(String[][] data) {
		if(output1 == null) {
			return;
		}
		output1.printStatus(data, output1.getTable());
	}
	
	/**
	 *  stateの取得 
	 *  <small><br>cast-Swf:配役人外数、cast-Rw:配役人狼数、cast-Sw:配役人狼陣営数、min-pretend-Swf:最小騙り人外数
	 *  <br>max-hidden-Swf:最大潜伏人外数、count-expelled:縄数、max-a-Swf:最大生存人外数、max-a-Rw:最大生存人狼数
	 *  <br>max-a-Rf:最大生存妖狐数、min-a-Rf:最小生存妖狐数、disi-a-Swf:確定生存人外数、disi-a-Rw:確定生存人狼数、disi-a-Sv:確定生存村人陣営数</small>
	 *  */
	Map<String, Integer> getTotalState(String[][] data) {
		if(output1 == null) {
			return new HashMap<>();
		}
		return output1.getTotalState(data);
	}
	
	/**
	 * 生存確定人外の取得
	 * @param data
	 * @return
	 */
	List<Agent> getDisitionSwfList(String[][] data) {
		List<Agent> list = new ArrayList<>();
		if(output1 == null) {
			return list;
		}
		List<Integer> id = output1.getDisitionAliveSwfList(data);
		for(Integer i : id) {
			list.add(Agent.getAgent(i));
		}
		return list;
	}
	
	/**
	 * 生存確定人狼の取得
	 * @param data
	 * @return
	 */
	List<Agent> getDisitionRwList(String[][] data) {
		List<Agent> list = new ArrayList<>();
		if(output1 == null) {
			return list;
		}
		List<Integer> id = output1.getDisitionAliveRwList(data);
		for(Integer i : id) {
			list.add(Agent.getAgent(i));
		}
		return list;
	}
	
	/**
	 * 生存確定村人陣営の取得
	 * @param data
	 * @return
	 */
	List<Agent> getDisitionSvList(String[][] data) {
		List<Agent> list = new ArrayList<>();
		if(output1 == null) {
			return list;
		}
		List<Integer> id = output1.getDisitionAliveSvList(data);
		for(Integer i : id) {
			list.add(Agent.getAgent(i));
		}
		return list;
	}
	
	/**
	 * 生存確白の取得
	 * @param data
	 * @return
	 */
	List<Agent> getDisitionNRwList(String[][] data) {
		List<Agent> list = new ArrayList<>();
		if(output1 == null) {
			return list;
		}
		List<Integer> id = output1.getDisitionAliveNRwList(data);
		for(Integer i : id) {
			list.add(Agent.getAgent(i));
		}
		return list;
	}
	
	int getDay() {
		return day;
	}
	
	/** dataが破綻しているかを返す */
	boolean isBankruptcy(String[][] data) {
		return output1.isBankruptcy(data);
	}
	
}
