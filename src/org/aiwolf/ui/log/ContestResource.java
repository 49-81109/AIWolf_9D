package org.aiwolf.ui.log;

import java.awt.Image;
import java.io.Serializable;
import java.net.URL;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.swing.ImageIcon;

import org.aiwolf.client.lib.Topic;
import org.aiwolf.client.lib.Content;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Judge;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.data.Team;
import org.aiwolf.common.data.Vote;
import org.aiwolf.common.util.BidiMap;
import org.aiwolf.server.AIWolfGame;
import org.aiwolf.ui.res.AIWolfResource;
import org.aiwolf.ui.res.JapaneseResource;

public class ContestResource extends JapaneseResource implements Serializable{


	/**
	 * 
	 */
	private static final long serialVersionUID = -5033734829293769603L;

//	String[][] agentResourceAry = {
//			{"不幸なタットン", "img/Male_01.png"}, 
//			{"学生デイクル", "img/Male_02.png"}, 
//			{"御曹司ランダル", "img/Male_03.png"}, 
//			{"用心棒ダコタ", "img/Male_04.png"}, 
//			{"調査員ピース", "img/Male_05.png"}, 
//			{"教師ジョアキム", "img/Male_06.png"}, 
//			{"絵描きロデリック", "img/Male_07.png"}, 
//			{"村長イワン", "img/Male_08.png"}, 
//			{"嘘つきジャック", "img/Male_09.png"}, 
//			{"牧童クゥ", "img/Male_10.png"}, 
//			{"探偵トーリイ", "img/Male_11.png"}, 
//			{"露天商クリフ", "img/Male_12.png"}, 
//			{"雑貨屋ノラ", "img/Female_01.png"}, 
//			{"堅物クレア", "img/Female_02.png"}, 
//			{"老婆キノ", "img/Female_03.png"}, 
//			{"洗濯屋メアリ", "img/Female_04.png"}, 
//			{"村娘カサリナ", "img/Female_05.png"}, 
//			{"優等生デニース", "img/Female_06.png"}, 
//			{"記者キャサリン", "img/Female_07.png"}, 
//			{"不機嫌エイミー", "img/Female_08.png"}, 
//			{"踊り子バルバラ", "img/Female_09.png"}, 
//			{"令嬢ブレンダ", "img/Female_10.png"}, 
//			{"力持ちメリーズ", "img/Female_11.png"}, 
//			{"眼鏡マリンダ", "img/Female_12.png"}, 
//		};

	
	AIWolfGame game;

	public ContestResource(AIWolfGame game){
		this();
		this.game = game;
	}
	
	public ContestResource() {
//		agentResourceList = Arrays.asList(agentResourceAry);
		super();
		long seed = 30;
		Collections.shuffle(agentResourceList, new Random(seed));

		bidiMap = new BidiMap<>();
	}
	
	public void setAIWolfGame(AIWolfGame game){
		this.game = game;
	}
	
	public void setName(int i, String name){
		agentResourceList.get(i)[0] = name;
	}


	@Override
	public String convertAgent(Agent agent) {
		if(game != null){
			for(Agent a:game.getGameData().getAgentList()){
				setName(a.getAgentIdx(), game.getAgentName(a));
			}
			game = null;
		}
		String name = agentResourceList.get(agent.getAgentIdx())[0];
		bidiMap.put(agent, name);
//		System.out.println(agent+"\t"+name);
		return name;
	}

}
