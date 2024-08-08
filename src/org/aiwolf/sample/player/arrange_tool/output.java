package org.aiwolf.sample.player.arrange_tool;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**  <div lang="ja"><big>output</big><br>このクラスは読み込んだデータについて盤面整理を実行するクラスです。</div><br>
 * 
 * @author 49
 *
 */
final public class output {
	final private static String[] roleName = {"村人", "占い師", "霊能者", "狩人", "共有者", "人狼", "狂信者", "妖狐", "背徳者", "埋毒者"}; 
	final private readData read;
	final private table table1;
	final private filtering fil1;
	final private createTable create1;
	private String[][] data;
	private List<String[][]> seerData;
	private List<String[][]> mediumData;
	private List<String[][]> bodyguardData;
	private List<String[][]> latestSeerOptional;
	private List<String[][]> latestMediumOptional;
	private List<String[][]> latestBodyguardOptional;
	private boolean isArrange;
	int s, undisitionPlayer; 
	
	public output(readData read) {
		this.read = read;
		table1 = read.getTable();
		create1 = read.getCreateTable();
		fil1 = new filtering(table1);
		isArrange = false;
	}
	
	public output(readData read, table table2) {
		this.read = read;
		table1 = table2;
		create1 = read.getCreateTable();
		fil1 = new filtering(table2);
		isArrange = false;
	}
	
	public void printInput() {
		table1.printStatus();
		System.out.println();
	}
	
	public void printInput(table table2) {
		table2.printStatus();
		System.out.println();
	}
	
	public final table getTable() {
		return table1;
	}
	
	public final String[][] arrangement() {
		isArrange = true;
		data = create1.initData();
		System.out.println();
		System.out.println("1.犠牲者PLの妖狐をいったん削除、単数死体の埋毒者と人狼(自噛み禁止の場合)を削除、追放者の埋毒者もいったん削除");
		data = fil1.remWolfFox_victims(data);
		System.out.println();
		create1.printTable(data, table1);
		System.out.println(); System.out.println();
		System.out.println("2.COに応じて役職候補を次のように設定(●はCO、△は乗っ取られ可能性あり)");
		data = fil1.COFilter(data);
		System.out.println();
		create1.printTable(data, table1);
		System.out.println(); System.out.println();
		System.out.println("3. 埋毒者、背徳者の道連れがあったときの処理");
		data = fil1.suicideHappened(data);
		System.out.println();
		create1.printTable(data, table1);
		System.out.println(); System.out.println();
		data = fil1.totalPrimarySort(data);
		
		exeFilter();
		//create1.printTable(data);
		if(table1.getSeerCOList().size() > 0) {
			data = fil1.combineData(data, seerData, "占い師");
		}
		if(table1.getMediumCOList().size() > 0) {
			data = fil1.combineData(data, mediumData, "霊能者");
		}
		if(table1.getBodyguardCOList().size() > 0) {
			data = fil1.combineData(data, bodyguardData, "狩人");
		}
		if(table1.getSeerCOList().size() > 0) {
			seerData = fil1.seerCOPositioning(data);
		}
		if(table1.getMediumCOList().size() > 0) {
			mediumData = fil1.mediumCOPositioning(data);
		}
		if(table1.getBodyguardCOList().size() > 0) {
			bodyguardData = fil1.bodyguardCOPositioning(data);
		}
		
		if(table1.getMediumCOList().size() > 0 && table1.getSeerCOList().size() > 0) {
			fil1.crossSeerMedium(seerData, mediumData);
		}
		if(table1.getBodyguardCOList().size() > 0 && table1.getSeerCOList().size() > 0) {
			fil1.crossSeerBodyguard(seerData, bodyguardData);
		}
		if(table1.getMediumCOList().size() > 0 && table1.getBodyguardCOList().size() > 0) {
			fil1.crossMediumBodyguard(mediumData, bodyguardData);
		}
		
		//占い師CO視点の出力
		for(int i = 0; i < table1.getSeerCOList().size(); i++) {
			System.out.println("占い師 >> " + table1.getSeerCOList().get(i).getSeerCOpl().getName() + "視点");
			System.out.println();
			create1.printTable(seerData.get(i), table1);
			System.out.println("破綻しているか？ : " + fil1.isBankruptcy(seerData.get(i)));
			System.out.println(); System.out.println();
		}
		//霊能者CO視点の出力
		for(int i = 0; i < table1.getMediumCOList().size(); i++) {
			System.out.println("霊能者 >> " + table1.getMediumCOList().get(i).getMediumCOpl().getName() + "視点");
			System.out.println();
			create1.printTable(mediumData.get(i), table1);
			System.out.println("破綻しているか？ : " + fil1.isBankruptcy(mediumData.get(i)));
			System.out.println(); System.out.println();
		}
		//狩人CO視点の出力
		for(int i = 0; i < table1.getBodyguardCOList().size(); i++) {
			System.out.println("狩人 >> " + table1.getBodyguardCOList().get(i).getBodyguardCOpl().getName() + "視点");
			System.out.println();
			create1.printTable(bodyguardData.get(i), table1);
			System.out.println("破綻しているか？ : " + fil1.isBankruptcy(bodyguardData.get(i)));
			System.out.println(); System.out.println();
		}
		
		for(int i = table1.getSeerCOList().size(); i < table1.getSeerHideList().size() + table1.getSeerCOList().size(); i++) {
			System.out.println("占い師 >> " + table1.getSeerHideList().get(i - table1.getSeerCOList().size()).getSeerCOpl().getName() + "視点 (潜伏死亡)");
			System.out.println();
			create1.printTable(seerData.get(i), table1);
			System.out.println("破綻しているか？ : " + fil1.isBankruptcy(seerData.get(i)));
			System.out.println(); System.out.println();
		}
		
		for(int i = table1.getMediumCOList().size(); i < table1.getMediumHideList().size() + table1.getMediumCOList().size(); i++) {
			System.out.println("霊能者 >> " + table1.getMediumHideList().get(i - table1.getMediumCOList().size()).getMediumCOpl().getName() + "視点 (潜伏死亡)");
			System.out.println();
			create1.printTable(mediumData.get(i), table1);
			System.out.println("破綻しているか？ : " + fil1.isBankruptcy(mediumData.get(i)));
			System.out.println(); System.out.println();
		}
		
		for(int i = table1.getBodyguardCOList().size(); i < table1.getBodyguardHideList().size() + table1.getBodyguardCOList().size(); i++) {
			System.out.println("狩人 >> " + table1.getBodyguardHideList().get(i - table1.getBodyguardCOList().size()).getBodyguardCOpl().getName() + "視点 (潜伏死亡)");
			System.out.println();
			create1.printTable(bodyguardData.get(i), table1);
			System.out.println("破綻しているか？ : " + fil1.isBankruptcy(bodyguardData.get(i)));
			System.out.println(); System.out.println();
		}
		
		System.out.println();
		System.out.println("全体");
		if(table1.getSeerCOList().size() > 0) {
			data = fil1.combineData(data, seerData, "占い師");
		}
		if(table1.getMediumCOList().size() > 0) {
			data = fil1.combineData(data, mediumData, "霊能者");
		}
		if(table1.getBodyguardCOList().size() > 0) {
			data = fil1.combineData(data, bodyguardData, "狩人");
		}
		
		//create1.printTable(data);
		create1.printTable(data, table1);
		//create1.printTable(data);
		if(fil1.isBankruptcy(data)) {
			System.err.println("この組み合わせは存在しません");
			System.exit(1);
		}
		System.out.println();
		return fil1.copyData(data);
	}
	
	public final String[][] nonPrintArrangement() {
		isArrange = true;
		data = create1.initData();
		data = fil1.remWolfFox_victims(data);
		data = fil1.COFilter(data);
		data = fil1.suicideHappened(data);
		data = fil1.totalPrimarySort(data);
		exeFilter();
		if(table1.getSeerCOList().size() > 0) {
			data = fil1.combineData(data, seerData, "占い師");
		}
		if(table1.getMediumCOList().size() > 0) {
			data = fil1.combineData(data, mediumData, "霊能者");
		}
		if(table1.getBodyguardCOList().size() > 0) {
			data = fil1.combineData(data, bodyguardData, "狩人");
		}
		if(table1.getSeerCOList().size() > 0) {
			seerData = fil1.seerCOPositioning(data);
		}
		if(table1.getMediumCOList().size() > 0) {
			mediumData = fil1.mediumCOPositioning(data);
		}
		if(table1.getBodyguardCOList().size() > 0) {
			bodyguardData = fil1.bodyguardCOPositioning(data);
		}
		
		if(table1.getMediumCOList().size() > 0 && table1.getSeerCOList().size() > 0) {
			fil1.crossSeerMedium(seerData, mediumData);
		}
		if(table1.getBodyguardCOList().size() > 0 && table1.getSeerCOList().size() > 0) {
			fil1.crossSeerBodyguard(seerData, bodyguardData);
		}
		if(table1.getMediumCOList().size() > 0 && table1.getBodyguardCOList().size() > 0) {
			fil1.crossMediumBodyguard(mediumData, bodyguardData);
		}
		if(fil1.isBankruptcy(data)) {
			System.err.println("この組み合わせは存在しません");
			System.exit(1);
		}
		return fil1.copyData(data);
	}
	
	public final String[][] totalOnlyArrangement() {
		String[][] data = nonPrintArrangement();
		System.out.println("全体");
		create1.printTable(data, table1);
		System.out.println();
		return data;
	}
	
	final private void exeFilter() {
		String[][] tmp = create1.initData();
		while(!fil1.isSameData(tmp, data)) {
			tmp = fil1.copyData(data);
			//System.out.println("占い師");
			
			if(table1.getSeerCOList().size() > 0) {
				seerData = fil1.seerCOPositioning(data);
				data = fil1.combineData(data, seerData, "占い師");
			}
			//System.out.println("霊能者");
			if(table1.getMediumCOList().size() > 0) {
				mediumData = fil1.mediumCOPositioning(data);
				data = fil1.combineData(data, mediumData, "霊能者");
			}
			if(table1.getBodyguardCOList().size() > 0) {
				bodyguardData = fil1.bodyguardCOPositioning(data);
				data = fil1.combineData(data, bodyguardData, "狩人");
			}
			if(table1.getSeerCOList().size() > 0) {
				seerData = fil1.seerCOPositioning(data);
			}
			if(table1.getMediumCOList().size() > 0) {
				mediumData = fil1.mediumCOPositioning(data);
			}
			if(table1.getBodyguardCOList().size() > 0) {
				bodyguardData = fil1.bodyguardCOPositioning(data);
			}
			if(table1.getSeerCOList().size() > 0) {
				data = fil1.combineData(data, seerData, "占い師");
			}
			if(table1.getMediumCOList().size() > 0) {
				data = fil1.combineData(data, mediumData, "霊能者");
			}
			if(table1.getBodyguardCOList().size() > 0) {
				data = fil1.combineData(data, bodyguardData, "狩人");
			}
			//System.out.println("結合");
			if(table1.getMediumCOList().size() > 0 && table1.getSeerCOList().size() > 0) {
				fil1.crossSeerMedium(seerData, mediumData);
			}
			if(table1.getBodyguardCOList().size() > 0 && table1.getSeerCOList().size() > 0) {
				fil1.crossSeerBodyguard(seerData, bodyguardData);
			}
			if(table1.getMediumCOList().size() > 0 && table1.getBodyguardCOList().size() > 0) {
				fil1.crossMediumBodyguard(mediumData, bodyguardData);
			}
		}
		
	}
	
	public final String[][] getData() {
		if(!isArrange) {
			System.err.println("まずはarrangementメソッドかnonPrintArrangementメソッドを実行してください");
			System.exit(1);
		}
		return fil1.copyData(data);
	}
	
	// 作為的に役職を指定
	public final String[][] optional(String name, String role, String[][] tmp, table table2) {
		int id = -1;
		for(player pl : table1.getPlayerList()) {
			if(pl.getName().equals(name)) {
				id = pl.getId();
			}
		}
		if(id == -1) {
			System.out.println("そのプレイヤー名は存在しません。 : " + name);
			return tmp;
		}
		return optional(id, role, tmp, table2);
	}
	public final String[][] optional(int id, String role, String[][] tmp, table table2) {
		if(!isArrange) {
			System.err.println("まずはarrangementメソッドかnonPrintArrangementメソッドを実行してください");
			System.exit(1);
		}
		String roleStr = "";
		if(role.equals("人狼") || role.equals("妖狐")) {
			roleStr = role.substring(1);
		}
		else {
			roleStr = role.substring(0,1);
		}
		player tar = new player("×", -1);
		for(player pl : table2.getPlayerList()) {
			if(pl.getId() == id) {
				tar = pl;
			}
		}
		if(tar.getId() == -1) {
			System.err.println("playerを認識出来ませんでした");
			System.exit(1);
		}
		System.out.println();
		System.out.println("【" + tar.getName() + " ===> " + role + "視点】");
		System.out.println();
		String[][] dataOptional = fil1.copyData(tmp);
		//create1.printTable(dataOptional);
		dataOptional = fil1.addCondition(dataOptional, read.getPlayer(id, table2.getPlayerList()), roleStr);
		//create1.printTable(dataOptional);
		String[][] tmpOptional = create1.initData();
		List<String[][]> seerOptional = new ArrayList<>();
		List<String[][]> mediumOptional = new ArrayList<>();
		List<String[][]> bodyguardOptional = new ArrayList<>();
		
		dataOptional = fil1.totalPrimarySort(dataOptional);
		
		
		while(!fil1.isSameData(tmpOptional, dataOptional)) {
			tmpOptional = fil1.copyData(dataOptional);
			
			if(table2.getSeerCOList().size() > 0) {
				seerOptional = fil1.seerCOPositioning(dataOptional);
				dataOptional = fil1.combineData(dataOptional, seerOptional, "占い師");
			}
			if(table2.getMediumCOList().size() > 0) {
				mediumOptional = fil1.mediumCOPositioning(dataOptional);
				dataOptional = fil1.combineData(dataOptional, mediumOptional, "霊能者");
			}
			if(table2.getBodyguardCOList().size() > 0) {
				bodyguardOptional = fil1.bodyguardCOPositioning(dataOptional);
				dataOptional = fil1.combineData(data, bodyguardOptional, "狩人");
			}
			
			roleSort(dataOptional, seerOptional, mediumOptional, bodyguardOptional, table2);
			
			tmpOptional = fil1.totalSort(dataOptional);
			
		}
		
		//*
		if(table2.getSeerCOList().size() > 0) {
			seerOptional = fil1.seerCOPositioning(dataOptional);
		}
		if(table2.getMediumCOList().size() > 0) {
			mediumOptional = fil1.mediumCOPositioning(dataOptional);
		}
		if(table2.getBodyguardCOList().size() > 0) {
			bodyguardOptional = fil1.bodyguardCOPositioning(dataOptional);
		}
		if(table2.getSeerCOList().size() > 0) {
			dataOptional = fil1.combineData(dataOptional, seerOptional, "占い師");
		}
		if(table2.getMediumCOList().size() > 0) {
			dataOptional = fil1.combineData(dataOptional, mediumOptional, "霊能者");
		}
		if(table2.getBodyguardCOList().size() > 0) {
			dataOptional = fil1.combineData(data, bodyguardOptional, "狩人");
		}
		if(table2.getMediumCOList().size() > 0 && table2.getSeerCOList().size() > 0) {
			fil1.crossSeerMedium(seerOptional, mediumOptional);
		}
		if(table2.getBodyguardCOList().size() > 0 && table2.getSeerCOList().size() > 0) {
			fil1.crossSeerBodyguard(seerOptional, bodyguardOptional);
		}
		if(table2.getMediumCOList().size() > 0 && table2.getBodyguardCOList().size() > 0) {
			fil1.crossMediumBodyguard(mediumOptional, bodyguardOptional);
		}
		//*/
		
		System.out.println();
		for(int i = 0; i < table2.getSeerCOList().size(); i++) {
			System.out.println("占い師 >> " + table2.getSeerCOList().get(i).getSeerCOpl().getName() + "視点");
			System.out.println();
			create1.printTable(seerOptional.get(i), table2);
			System.out.println(tar.getName() + role + "視点、" + table2.getSeerCOList().get(i).getSeerCOpl().getName() + "が真の占い師の可能性はあるか？ : " + !fil1.isBankruptcy(seerOptional.get(i)));
			System.out.println(); System.out.println();
		}
		
		for(int i = 0; i < table2.getMediumCOList().size(); i++) {
			System.out.println("霊能者 >> " + table2.getMediumCOList().get(i).getMediumCOpl().getName() + "視点");
			System.out.println();
			create1.printTable(mediumOptional.get(i), table2);
			System.out.println(tar.getName() + role + "視点、" + table2.getMediumCOList().get(i).getMediumCOpl().getName() + "が真の霊能者の可能性はあるか？ : " + !fil1.isBankruptcy(mediumOptional.get(i)));
			System.out.println(); System.out.println();
		}
		
		for(int i = 0; i < table2.getBodyguardCOList().size(); i++) {
			System.out.println("狩人 >> " + table2.getBodyguardCOList().get(i).getBodyguardCOpl().getName() + "視点");
			System.out.println();
			create1.printTable(bodyguardOptional.get(i), table2);
			System.out.println(tar.getName() + role + "視点、" + table2.getBodyguardCOList().get(i).getBodyguardCOpl().getName() + "が真の狩人の可能性はあるか？ : " + !fil1.isBankruptcy(bodyguardOptional.get(i)));
			System.out.println(); System.out.println();
		}
		
		for(int i = table2.getSeerCOList().size(); i < table2.getSeerHideList().size() + table2.getSeerCOList().size(); i++) {
			System.out.println("占い師 >> " + table2.getSeerHideList().get(i - table2.getSeerCOList().size()).getSeerCOpl().getName() + "視点 (潜伏死亡)");
			System.out.println();
			create1.printTable(seerData.get(i), table2);
			System.out.println(tar.getName() + role + "視点、" + table2.getSeerHideList().get(i - table2.getSeerCOList().size()).getSeerCOpl().getName() + "が真の占い師(潜伏死亡)の可能性はあるか？ : " + !fil1.isBankruptcy(seerOptional.get(i)));
			System.out.println(); System.out.println();
		}
		
		for(int i = table2.getMediumCOList().size(); i < table2.getMediumHideList().size() + table2.getMediumCOList().size(); i++) {
			System.out.println("霊能者 >> " + table2.getMediumHideList().get(i - table2.getMediumCOList().size()).getMediumCOpl().getName() + "視点 (潜伏死亡)");
			System.out.println();
			create1.printTable(mediumData.get(i), table2);
			System.out.println(tar.getName() + role + "視点、" + table2.getMediumHideList().get(i - table2.getMediumCOList().size()).getMediumCOpl().getName() + "が真の霊能者(潜伏死亡)の可能性はあるか？ : " + !fil1.isBankruptcy(mediumOptional.get(i)));
			System.out.println(); System.out.println();
		}
		
		for(int i = table2.getBodyguardCOList().size(); i < table2.getBodyguardHideList().size() + table2.getBodyguardCOList().size(); i++) {
			System.out.println("狩人 >> " + table2.getBodyguardHideList().get(i - table2.getBodyguardCOList().size()).getBodyguardCOpl().getName() + "視点 (潜伏死亡)");
			System.out.println();
			create1.printTable(bodyguardData.get(i), table2);
			System.out.println(tar.getName() + role + "視点、" + table2.getBodyguardHideList().get(i - table2.getBodyguardCOList().size()).getBodyguardCOpl().getName() + "が真の狩人(潜伏死亡)の可能性はあるか？ : " + !fil1.isBankruptcy(bodyguardOptional.get(i)));
			System.out.println(); System.out.println();
		}
		
		latestSeerOptional = new ArrayList<>();
		for(String[][] seer : seerOptional) {
			latestSeerOptional.add(fil1.copyData(seer));
		}
		latestMediumOptional = new ArrayList<>();
		for(String[][] medium : mediumOptional) {
			latestMediumOptional.add(fil1.copyData(medium));
		}
		latestBodyguardOptional = new ArrayList<>();
		for(String[][] bodyguard : bodyguardOptional) {
			latestBodyguardOptional.add(fil1.copyData(bodyguard));
		}
		
		System.out.println();
		System.out.println(tar.getName() + role + "視点");
		create1.printTable(dataOptional, table2);
		System.out.println(tar.getName() + role + "視点が存在するか？ : " + !fil1.isBankruptcy(dataOptional));
		System.out.println();
		return fil1.copyData(dataOptional);
	}
	
	// 作為的に役職を指定(表示なし)
	public final String[][] nonPrintOptional(String name, String role, String[][] tmp, table table2) {
		int id = -1;
		for(player pl : table1.getPlayerList()) {
			if(pl.getName().equals(name)) {
				id = pl.getId();
			}
		}
		if(id == -1) {
			System.out.println("そのプレイヤー名は存在しません。 : " + name);
			return tmp;
		}
		return nonPrintOptional(id, role, tmp, table2);
	}
	public final String[][] nonPrintOptional(int id, String role, String[][] tmp, table table2) {
		if(!isArrange) {
			System.err.println("まずはarrangementメソッドかnonPrintArrangementメソッドを実行してください");
			System.exit(1);
		}
		String roleStr = "";
		if(role.equals("人狼") || role.equals("妖狐")) {
			roleStr = role.substring(1);
		}
		else {
			roleStr = role.substring(0,1);
		}
		player tar = new player("×", -1);
		for(player pl : table2.getPlayerList()) {
			if(pl.getId() == id) {
				tar = pl;
			}
		}
		if(tar.getId() == -1) {
			System.err.println("playerを認識出来ませんでした");
			System.exit(1);
		}
		String[][] dataOptional = fil1.copyData(tmp);
		dataOptional = fil1.addCondition(dataOptional, read.getPlayer(id, table2.getPlayerList()), roleStr);
		String[][] tmpOptional = create1.initData();
		List<String[][]> seerOptional = new ArrayList<>();
		List<String[][]> mediumOptional = new ArrayList<>();
		List<String[][]> bodyguardOptional = new ArrayList<>();
		
		dataOptional = fil1.totalPrimarySort(dataOptional);
		
		while(!fil1.isSameData(tmpOptional, dataOptional)) {
			tmpOptional = fil1.copyData(dataOptional);
			if(table2.getSeerCOList().size() > 0) {
				seerOptional = fil1.seerCOPositioning(dataOptional);
				dataOptional = fil1.combineData(dataOptional, seerOptional, "占い師");
			}
			if(table2.getMediumCOList().size() > 0) {
				mediumOptional = fil1.mediumCOPositioning(dataOptional);
				dataOptional = fil1.combineData(dataOptional, mediumOptional, "霊能者");
			}
			if(table2.getBodyguardCOList().size() > 0) {
				bodyguardOptional = fil1.bodyguardCOPositioning(dataOptional);
				dataOptional = fil1.combineData(data, bodyguardOptional, "狩人");
			}
			roleSort(dataOptional, seerOptional, mediumOptional, bodyguardOptional, table2);
			tmpOptional = fil1.totalSort(dataOptional);
		}
		
		if(table2.getSeerCOList().size() > 0) {
			seerOptional = fil1.seerCOPositioning(dataOptional);
		}
		if(table2.getMediumCOList().size() > 0) {
			mediumOptional = fil1.mediumCOPositioning(dataOptional);
		}
		if(table2.getBodyguardCOList().size() > 0) {
			bodyguardOptional = fil1.bodyguardCOPositioning(dataOptional);
		}
		if(table2.getSeerCOList().size() > 0) {
			dataOptional = fil1.combineData(dataOptional, seerOptional, "占い師");
		}
		if(table2.getMediumCOList().size() > 0) {
			dataOptional = fil1.combineData(dataOptional, mediumOptional, "霊能者");
		}
		if(table2.getBodyguardCOList().size() > 0) {
			dataOptional = fil1.combineData(data, bodyguardOptional, "狩人");
		}
		if(table2.getMediumCOList().size() > 0 && table2.getSeerCOList().size() > 0) {
			fil1.crossSeerMedium(seerOptional, mediumOptional);
		}
		if(table2.getBodyguardCOList().size() > 0 && table2.getSeerCOList().size() > 0) {
			fil1.crossSeerBodyguard(seerOptional, bodyguardOptional);
		}
		if(table2.getMediumCOList().size() > 0 && table2.getBodyguardCOList().size() > 0) {
			fil1.crossMediumBodyguard(mediumOptional, bodyguardOptional);
		}
		latestSeerOptional = new ArrayList<>();
		for(String[][] seer : seerOptional) {
			latestSeerOptional.add(fil1.copyData(seer));
		}
		latestMediumOptional = new ArrayList<>();
		for(String[][] medium : mediumOptional) {
			latestMediumOptional.add(fil1.copyData(medium));
		}
		latestBodyguardOptional = new ArrayList<>();
		for(String[][] bodyguard : bodyguardOptional) {
			latestBodyguardOptional.add(fil1.copyData(bodyguard));
		}
		
		return fil1.copyData(dataOptional);
	}
	
	// 作為的に役職候補を削除
	public final String[][] optionalDelete(String name, String role, String[][] tmp, table table2) {
		int id = -1;
		for(player pl : table1.getPlayerList()) {
			if(pl.getName().equals(name)) {
				id = pl.getId();
			}
		}
		if(id == -1) {
			System.out.println("そのプレイヤー名は存在しません。 : " + name);
			return tmp;
		}
		return optionalDelete(id, role, tmp, table2);
	}
	public final String[][] optionalDelete(int id, String role, String[][] tmp, table table2) {
		if(!isArrange) {
			System.err.println("まずはarrangementメソッドかnonPrintArrangementメソッドを実行してください");
			System.exit(1);
		}
		String roleStr = "";
		if(role.equals("人狼") || role.equals("妖狐")) {
			roleStr = role.substring(1);
		}
		else {
			roleStr = role.substring(0,1);
		}
		player tar = new player("×", -1);
		for(player pl : table2.getPlayerList()) {
			if(pl.getId() == id) {
				tar = pl;
			}
		}
		if(tar.getId() == -1) {
			System.err.println("playerを認識出来ませんでした");
			System.exit(1);
		}
		System.out.println();
		System.out.println("【" + tar.getName() + " ===> 非" + role + "視点】");
		System.out.println();
		String[][] dataOptional = fil1.copyData(tmp);
		//create1.printTable(dataOptional);
		dataOptional = fil1.addDeleteCondition(dataOptional, read.getPlayer(id, table2.getPlayerList()), roleStr);
		//create1.printTable(dataOptional);
		String[][] tmpOptional = create1.initData();
		List<String[][]> seerOptional = new ArrayList<>();
		List<String[][]> mediumOptional = new ArrayList<>();
		List<String[][]> bodyguardOptional = new ArrayList<>();
		
		dataOptional = fil1.totalPrimarySort(dataOptional);
		while(!fil1.isSameData(tmpOptional, dataOptional)) {
			tmpOptional = fil1.copyData(dataOptional);
			if(table2.getSeerCOList().size() > 0) {
				seerOptional = fil1.seerCOPositioning(dataOptional);
				dataOptional = fil1.combineData(dataOptional, seerOptional, "占い師");
			}
			if(table2.getMediumCOList().size() > 0) {
				mediumOptional = fil1.mediumCOPositioning(dataOptional);
				dataOptional = fil1.combineData(dataOptional, mediumOptional, "霊能者");
			}
			if(table2.getBodyguardCOList().size() > 0) {
				bodyguardOptional = fil1.bodyguardCOPositioning(dataOptional);
				dataOptional = fil1.combineData(data, bodyguardOptional, "狩人");
			}
			roleSort(dataOptional, seerOptional, mediumOptional, bodyguardOptional, table2);
			tmpOptional = fil1.totalSort(dataOptional);
		}
		
		if(table2.getSeerCOList().size() > 0) {
			seerOptional = fil1.seerCOPositioning(dataOptional);
		}
		if(table2.getMediumCOList().size() > 0) {
			mediumOptional = fil1.mediumCOPositioning(dataOptional);
		}
		if(table2.getBodyguardCOList().size() > 0) {
			bodyguardOptional = fil1.bodyguardCOPositioning(dataOptional);
		}
		if(table2.getSeerCOList().size() > 0) {
			dataOptional = fil1.combineData(dataOptional, seerOptional, "占い師");
		}
		if(table2.getMediumCOList().size() > 0) {
			dataOptional = fil1.combineData(dataOptional, mediumOptional, "霊能者");
		}
		if(table2.getBodyguardCOList().size() > 0) {
			dataOptional = fil1.combineData(data, bodyguardOptional, "狩人");
		}
		if(table2.getMediumCOList().size() > 0 && table2.getSeerCOList().size() > 0) {
			fil1.crossSeerMedium(seerOptional, mediumOptional);
		}
		if(table2.getBodyguardCOList().size() > 0 && table2.getSeerCOList().size() > 0) {
			fil1.crossSeerBodyguard(seerOptional, bodyguardOptional);
		}
		if(table2.getMediumCOList().size() > 0 && table2.getBodyguardCOList().size() > 0) {
			fil1.crossMediumBodyguard(mediumOptional, bodyguardOptional);
		}
		
		System.out.println();
		for(int i = 0; i < table2.getSeerCOList().size(); i++) {
			System.out.println("占い師 >> " + table2.getSeerCOList().get(i).getSeerCOpl().getName() + "視点");
			System.out.println();
			create1.printTable(seerOptional.get(i), table2);
			System.out.println(tar.getName() + "非" + role + "視点、" + table2.getSeerCOList().get(i).getSeerCOpl().getName() + "が真の占い師の可能性はあるか？ : " + !fil1.isBankruptcy(seerOptional.get(i)));
			System.out.println(); System.out.println();
		}
		
		for(int i = 0; i < table2.getMediumCOList().size(); i++) {
			System.out.println("霊能者 >> " + table2.getMediumCOList().get(i).getMediumCOpl().getName() + "視点");
			System.out.println();
			create1.printTable(mediumOptional.get(i), table2);
			System.out.println(tar.getName() + "非" + role + "視点、" + table2.getMediumCOList().get(i).getMediumCOpl().getName() + "が真の霊能者の可能性はあるか？ : " + !fil1.isBankruptcy(mediumOptional.get(i)));
			System.out.println(); System.out.println();
		}
		
		for(int i = 0; i < table2.getBodyguardCOList().size(); i++) {
			System.out.println("狩人 >> " + table2.getBodyguardCOList().get(i).getBodyguardCOpl().getName() + "視点");
			System.out.println();
			create1.printTable(bodyguardOptional.get(i), table2);
			System.out.println(tar.getName() + "非" + role + "視点、" + table2.getBodyguardCOList().get(i).getBodyguardCOpl().getName() + "が真の狩人の可能性はあるか？ : " + !fil1.isBankruptcy(bodyguardOptional.get(i)));
			System.out.println(); System.out.println();
		}
		
		for(int i = table2.getSeerCOList().size(); i < table2.getSeerHideList().size() + table2.getSeerCOList().size(); i++) {
			System.out.println("占い師 >> " + table2.getSeerHideList().get(i - table2.getSeerCOList().size()).getSeerCOpl().getName() + "視点 (潜伏死亡)");
			System.out.println();
			create1.printTable(seerData.get(i), table2);
			System.out.println(tar.getName() + "非" + role + "視点、" + table2.getSeerHideList().get(i - table2.getSeerCOList().size()).getSeerCOpl().getName() + "が真の占い師(潜伏死亡)の可能性はあるか？ : " + !fil1.isBankruptcy(seerOptional.get(i)));
			System.out.println(); System.out.println();
		}
		
		for(int i = table2.getMediumCOList().size(); i < table2.getMediumHideList().size() + table2.getMediumCOList().size(); i++) {
			System.out.println("霊能者 >> " + table2.getMediumHideList().get(i - table2.getMediumCOList().size()).getMediumCOpl().getName() + "視点 (潜伏死亡)");
			System.out.println();
			create1.printTable(mediumData.get(i), table2);
			System.out.println(tar.getName() + "非" + role + "視点、" + table2.getMediumHideList().get(i - table2.getMediumCOList().size()).getMediumCOpl().getName() + "が真の霊能者(潜伏死亡)の可能性はあるか？ : " + !fil1.isBankruptcy(mediumOptional.get(i)));
			System.out.println(); System.out.println();
		}
		
		for(int i = table2.getBodyguardCOList().size(); i < table2.getBodyguardHideList().size() + table2.getBodyguardCOList().size(); i++) {
			System.out.println("狩人 >> " + table2.getBodyguardHideList().get(i - table2.getBodyguardCOList().size()).getBodyguardCOpl().getName() + "視点 (潜伏死亡)");
			System.out.println();
			create1.printTable(bodyguardData.get(i), table2);
			System.out.println(tar.getName() + "非" + role + "視点、" + table2.getBodyguardHideList().get(i - table2.getBodyguardCOList().size()).getBodyguardCOpl().getName() + "が真の狩人(潜伏死亡)の可能性はあるか？ : " + !fil1.isBankruptcy(bodyguardOptional.get(i)));
			System.out.println(); System.out.println();
		}
		
		latestSeerOptional = new ArrayList<>();
		for(String[][] seer : seerOptional) {
			latestSeerOptional.add(fil1.copyData(seer));
		}
		latestMediumOptional = new ArrayList<>();
		for(String[][] medium : mediumOptional) {
			latestMediumOptional.add(fil1.copyData(medium));
		}
		latestBodyguardOptional = new ArrayList<>();
		for(String[][] bodyguard : bodyguardOptional) {
			latestBodyguardOptional.add(fil1.copyData(bodyguard));
		}
		
		System.out.println();
		System.out.println(tar.getName() + "非" + role + "視点");
		create1.printTable(dataOptional, table2);
		System.out.println(tar.getName() + "非" + role + "視点が存在するか？ : " + !fil1.isBankruptcy(dataOptional));
		System.out.println();
		return fil1.copyData(dataOptional);
	}
	
	// 作為的に役職候補を削除(表示なし)
	public final String[][] nonPrintOptionalDelete(String name, String role, String[][] tmp, table table2) {
		int id = -1;
		for(player pl : table1.getPlayerList()) {
			if(pl.getName().equals(name)) {
				id = pl.getId();
			}
		}
		if(id == -1) {
			System.out.println("そのプレイヤー名は存在しません。 : " + name);
			return tmp;
		}
		return nonPrintOptionalDelete(id, role, tmp, table2);
	}
	public final String[][] nonPrintOptionalDelete(int id, String role, String[][] tmp, table table2) {
		if(!isArrange) {
			System.err.println("まずはarrangementメソッドかnonPrintArrangementメソッドを実行してください");
			System.exit(1);
		}
		String roleStr = "";
		if(role.equals("人狼") || role.equals("妖狐")) {
			roleStr = role.substring(1);
		}
		else {
			roleStr = role.substring(0,1);
		}
		player tar = new player("×", -1);
		for(player pl : table2.getPlayerList()) {
			if(pl.getId() == id) {
				tar = pl;
			}
		}
		if(tar.getId() == -1) {
			System.err.println("playerを認識出来ませんでした");
			System.exit(1);
		}
		String[][] dataOptional = fil1.copyData(tmp);
		dataOptional = fil1.addDeleteCondition(dataOptional, read.getPlayer(id, table2.getPlayerList()), roleStr);
		String[][] tmpOptional = create1.initData();
		List<String[][]> seerOptional = new ArrayList<>();
		List<String[][]> mediumOptional = new ArrayList<>();
		List<String[][]> bodyguardOptional = new ArrayList<>();
		
		dataOptional = fil1.totalPrimarySort(dataOptional);
		while(!fil1.isSameData(tmpOptional, dataOptional)) {
			tmpOptional = fil1.copyData(dataOptional);
			if(table2.getSeerCOList().size() > 0) {
				seerOptional = fil1.seerCOPositioning(dataOptional);
				dataOptional = fil1.combineData(dataOptional, seerOptional, "占い師");
			}
			if(table2.getMediumCOList().size() > 0) {
				mediumOptional = fil1.mediumCOPositioning(dataOptional);
				dataOptional = fil1.combineData(dataOptional, mediumOptional, "霊能者");
			}
			if(table2.getBodyguardCOList().size() > 0) {
				bodyguardOptional = fil1.bodyguardCOPositioning(dataOptional);
				dataOptional = fil1.combineData(data, bodyguardOptional, "狩人");
			}
			roleSort(dataOptional, seerOptional, mediumOptional, bodyguardOptional, table2);
			tmpOptional = fil1.totalSort(dataOptional);
		}
		if(table2.getSeerCOList().size() > 0) {
			seerOptional = fil1.seerCOPositioning(dataOptional);
		}
		if(table2.getMediumCOList().size() > 0) {
			mediumOptional = fil1.mediumCOPositioning(dataOptional);
		}
		if(table2.getBodyguardCOList().size() > 0) {
			bodyguardOptional = fil1.bodyguardCOPositioning(dataOptional);
		}
		if(table2.getSeerCOList().size() > 0) {
			dataOptional = fil1.combineData(dataOptional, seerOptional, "占い師");
		}
		if(table2.getMediumCOList().size() > 0) {
			dataOptional = fil1.combineData(dataOptional, mediumOptional, "霊能者");
		}
		if(table2.getBodyguardCOList().size() > 0) {
			dataOptional = fil1.combineData(data, bodyguardOptional, "狩人");
		}
		if(table2.getMediumCOList().size() > 0 && table2.getSeerCOList().size() > 0) {
			fil1.crossSeerMedium(seerOptional, mediumOptional);
		}
		if(table2.getBodyguardCOList().size() > 0 && table2.getSeerCOList().size() > 0) {
			fil1.crossSeerBodyguard(seerOptional, bodyguardOptional);
		}
		if(table2.getMediumCOList().size() > 0 && table2.getBodyguardCOList().size() > 0) {
			fil1.crossMediumBodyguard(mediumOptional, bodyguardOptional);
		}
		
		latestSeerOptional = new ArrayList<>();
		for(String[][] seer : seerOptional) {
			latestSeerOptional.add(fil1.copyData(seer));
		}
		latestMediumOptional = new ArrayList<>();
		for(String[][] medium : mediumOptional) {
			latestMediumOptional.add(fil1.copyData(medium));
		}
		latestBodyguardOptional = new ArrayList<>();
		for(String[][] bodyguard : bodyguardOptional) {
			latestBodyguardOptional.add(fil1.copyData(bodyguard));
		}
		
		return fil1.copyData(dataOptional);
	}
	
	// 作為的に役職を指定(全体表示のみ)
	public final String[][] totalOnlyOptional(String name, String role, String[][] tmp, table table2) {
		int id = -1;
		for(player pl : table1.getPlayerList()) {
			if(pl.getName().equals(name)) {
				id = pl.getId();
			}
		}
		if(id == -1) {
			System.out.println("そのプレイヤー名は存在しません。 : " + name);
			return tmp;
		}
		return totalOnlyOptional(id, role, tmp, table2);
	}
	public final String[][] totalOnlyOptional(int id, String role, String[][] tmp, table table2) {
		String[][] data = nonPrintOptional(id, role, tmp, table2);
		player tar = new player("×", -1);
		for(player pl : table2.getPlayerList()) {
			if(pl.getId() == id) {
				tar = pl;
			}
		}
		if(tar.getId() == -1) {
			System.err.println("playerを認識出来ませんでした");
			System.exit(1);
		}
		System.out.println("\n【" + tar.getName() + "===>" + role + "視点】\n");
		create1.printTable(data, table2);
		System.out.println(tar.getName() + role + "視点が存在するか？ : " + !fil1.isBankruptcy(data));
		System.out.println();
		return data;
	}
	
	// 作為的に役職候補を削除(全体表示のみ)
	public final String[][] totalOnlyOptionalDelete(String name, String role, String[][] tmp, table table2) {
		int id = -1;
		for(player pl : table1.getPlayerList()) {
			if(pl.getName().equals(name)) {
				id = pl.getId();
			}
		}
		if(id == -1) {
			System.out.println("そのプレイヤー名は存在しません。 : " + name);
			return tmp;
		}
		return totalOnlyOptionalDelete(id, role, tmp, table2);
	}
	public final String[][] totalOnlyOptionalDelete(int id, String role, String[][] tmp, table table2) {
		String[][] data = nonPrintOptionalDelete(id, role, tmp, table2);
		player tar = new player("×", -1);
		for(player pl : table2.getPlayerList()) {
			if(pl.getId() == id) {
				tar = pl;
			}
		}
		if(tar.getId() == -1) {
			System.err.println("playerを認識出来ませんでした");
			System.exit(1);
		}
		System.out.println("\n【" + tar.getName() + "===>非" + role + "視点】\n");
		create1.printTable(data, table2);
		System.out.println(tar.getName() + "非" + role + "視点が存在するか？ : " + !fil1.isBankruptcy(data));
		System.out.println();
		return data;
	}
	
	// 作為的に役職候補を絞る()
	public final String[][] nonPrintOptional(int id, String[] roles, String[][] tmp, table table2) {
		boolean[] isContain = {false, false, false, false, false, false, false, false, false, false};
		for(int i = 0; i < isContain.length; i++) {
			for(String role : roles) {
				if(role.equals(roleName[i])) {
					isContain[i] = true;
					break;
				}
			}
		}
		String[][] cp = fil1.copyData(tmp);
		for(int i = 0; i < isContain.length; i++) {
			if(!isContain[i]) {
				cp = nonPrintOptionalDelete(id, roleName[i], cp, table2);
			}
		}
		return tmp;
	}
	
	public final void printData(String[][] data) {
		create1.printTable(data, table1);
	}
	
	/** データのコピー */
	public final String[][] copyData(String[][] data) {
		return fil1.copyData(data);
	}
	
	/** データが破綻しているか？ */
	public final boolean isBankruptcy(String[][] data) {
		return fil1.isBankruptcy(data);
	}
	
	final private boolean isSameDataList(List<String[][]> data1, List<String[][]> data2) {
		if(data1.size() != data2.size()) {
			return false;
		}
		for(int i = 0; i < data1.size(); i++) {
			if(!fil1.isSameData(data1.get(i), data2.get(i))) {
				return false;
			}
		}
		return true;
	}
	
	// state関係
	public final void printStatus(String[][] data, table table2) {
		boardState state = new boardState(table2, fil1, data);
		state.printStatus();
	}
	
	/**
	 * ステータスを返す
	 * <small><br>cast:配役、pretend:騙り、hidden:潜伏、a:生存(alive)<br>
	 * R:役職 [Rw:人狼(Role werewolf)、Rf:妖狐(Role foxspirit)]<br>
	 * S:陣営 [Swf:人外(Side werewolf/foxspirit)、Sv:村人陣営(Side villager)]</small>
	 * @param data
	 * @return
	 */
	public final Map<String, Integer> getTotalState(String[][] data) {
		return new boardState(table1, fil1, data).getTotalState();
	}
	
	/**
	 * 確定人外プレイヤーを返す
	 * @param data
	 * @return
	 */
	public final List<Integer> getDisitionAliveSwfList(String[][] data) {
		List<player> Swf = new boardState(table1, fil1, data).getDisitionAliveNVSList();
		List<Integer> SwfId = new ArrayList<>();
		for(player pl : Swf) {
			SwfId.add(pl.getId());
		}
		return SwfId;
	}
	
	/**
	 * 確定村人陣営プレイヤーを返す
	 * @param data
	 * @return
	 */
	public final List<Integer> getDisitionAliveSvList(String[][] data) {
		List<player> Sv = new boardState(table1, fil1, data).getDisitionAliveVSList();
		List<Integer> SvId = new ArrayList<>();
		for(player pl : Sv) {
			SvId.add(pl.getId());
		}
		return SvId;
	}
	
	/**
	 * 確定人狼プレイヤーを返す
	 * @param data
	 * @return
	 */
	public final List<Integer> getDisitionAliveRwList(String[][] data) {
		List<player> Rw = new boardState(table1, fil1, data).getDisitionAliveWolfList();
		List<Integer> RwId = new ArrayList<>();
		for(player pl : Rw) {
			RwId.add(pl.getId());
		}
		return RwId;
	}
	
	/**
	 * 確白プレイヤーを返す
	 * @param data
	 * @return
	 */
	public final List<Integer> getDisitionAliveNRwList(String[][] data) {
		List<player> NRw = new boardState(table1, fil1, data).getDisitionAliveNotWolfList();
		List<Integer> NRwId = new ArrayList<>();
		for(player pl : NRw) {
			NRwId.add(pl.getId());
		}
		return NRwId;
	}
	
	
	final private void roleSort(String[][] data, List<String[][]> seer, List<String[][]> medium, List<String[][]> bodyguard, table table2) {
		if(table2.getSeerCOList().size() > 0) {
			seer = fil1.seerCOPositioning(data);
		}
		if(table2.getMediumCOList().size() > 0) {
			medium = fil1.mediumCOPositioning(data);
		}
		if(table2.getBodyguardCOList().size() > 0) {
			bodyguard = fil1.bodyguardCOPositioning(data);
		}
		if(table2.getSeerCOList().size() > 0) {
			data = fil1.combineData(data, seer, "占い師");
		}
		if(table2.getMediumCOList().size() > 0) {
			data = fil1.combineData(data, medium, "霊能者");
		}
		if(table2.getBodyguardCOList().size() > 0) {
			data = fil1.combineData(data, bodyguard, "狩人");
		}
		if(table2.getMediumCOList().size() > 0 && table2.getSeerCOList().size() > 0) {
			fil1.crossSeerMedium(seer, medium);
		}
		if(table2.getBodyguardCOList().size() > 0 && table2.getSeerCOList().size() > 0) {
			fil1.crossSeerBodyguard(seer, bodyguard);
		}
		if(table2.getMediumCOList().size() > 0 && table2.getBodyguardCOList().size() > 0) {
			fil1.crossMediumBodyguard(medium, bodyguard);
		}
		data = fil1.totalSort(data);
	}
	
	public final void pattern(String[][] data) {
		
		List<Patterns> patterns = new ArrayList<>();
		System.out.println("-----------------------------------------------------------");
		s = 1;
		undisitionPlayer = undisitionNum(data);
		//System.out.println(undisitionPlayer);
		tmp(patterns, data, table1);
		for(int i = 0; i < patterns.size(); i++) {
			patterns.get(i).print(i + 1);
		}
		System.out.println("組み合わせパターン数 : " + patterns.size());
	}
	
	public final void pattern(String[][] data, table table2) {
		
		List<Patterns> patterns = new ArrayList<>();
		System.out.println("-----------------------------------------------------------");
		s = 1;
		undisitionPlayer = undisitionNum(data);
		//System.out.println(undisitionPlayer);
		tmp(patterns, data, table2);
		for(int i = 0; i < patterns.size(); i++) {
			patterns.get(i).print(i + 1);
		}
		System.out.println("組み合わせパターン数 : " + patterns.size());
	}
	
	public final String[][] patternSort(String[][] data) {
		List<Patterns> patterns = new ArrayList<>();
		s = 1;
		undisitionPlayer = undisitionNum(data);
		System.out.println(patterns.size());
		tmp(patterns, data, table1);
		System.out.println(patterns.size());
		for(int i = 0; i < table1.getPlayerList().size(); i++) {
			for(int j = 0; j < table1.getRoleCom().getTotal(); j++) {
				if(data[i][j].equals("  ") || data[i][j].equals("●") || data[i][j].equals("△")) {
					System.out.println("[" + i + ", " + j + "]");
					pointDisition(patterns, data, i, j);
				}
			}
		}
		return data;
	}
	
	final private void pointDisition(List<Patterns> patterns, String[][] data, int player, int rolelabel) {
		boolean allTrue = true;
		boolean allFalse = true;
		for(Patterns pat : patterns) {
			String[][] tmp = pat.getPatternData();
			String pos = tmp[player][rolelabel];
			if(!pos.equals("○")) {
				allTrue = false;
			}
			if(!pos.equals("×")) {
				allFalse = false;
			}
		}
		if(allTrue && patterns.size() > 0) {
			data[player][rolelabel] = "○";
			//System.out.println("true  [" + player + ", " + rolelabel + "]");
		}
		if(allFalse && patterns.size() > 0) {
			data[player][rolelabel] = "×";
			//System.out.println("false  [" + player + ", " + rolelabel + "]");
		}
	}
	
	final private int undisitionNum(String[][] data) {
		int count = 0;
		boolean cir;
		for(int i = 0; i < table1.getPlayerList().size(); i++) {
			cir = false;
			for(int j = 0; j < table1.getRoleCom().getTotal(); j++) {
				if(data[i][j].equals("○")) {
					cir = true;
					break;
				}
			}
			if(!cir) {
				count++;
			}
		}
		
		return count;
	}
	
	final private void tmp(List<Patterns> patterns, String[][] data, table table2) {
		String[][] tmpData = fil1.copyData(data);
		boolean full = true;
		//System.out.println("============== " + s + " ==============");
		if(undisitionPlayer == 0) {
			Patterns p = new Patterns(table2, data, fil1);
			patterns.add(p);
			return;
		}
		
		for(int i = 0; i < table2.getPlayerList().size(); i++) {
			String tmpLabel = "";
			for(int j = 0; j < table2.getRoleCom().getTotal(); j++) {
				if(data[i][j].equals("  ") || data[i][j].equals("●") || data[i][j].equals("△")) {
					full = false;
					//System.out.println("[" + i + ", " + j + "]" + s);
					if(!tmpLabel.equals(table2.getRoleCom().getRoleLabel().get(j))) {
						String[][] tmpData2 = nonPrintOptional(table2.getPlayerList().get(i).getId(), table2.getRoleCom().getRoleLabel().get(j), tmpData, table2);
						tmpLabel = table2.getRoleCom().getRoleLabel().get(j);
						if(!fil1.isBankruptcy(tmpData2) && s < undisitionPlayer) {
							//System.out.println("tmpData2");
							//create1.printTable(tmpData2);
							if(!isFull(tmpData2)) {
								s++;
								tmp(patterns, tmpData2, table2);
								s--;
							}
							else {
								//System.out.println("++++++++ " + s + " +++++++");
								//create1.printTable(tmpData2);
								//patterns.add(new Patterns(table1, tmpData));
								Patterns p = new Patterns(table2, tmpData2, fil1);
								patterns.add(p);
							}
							//System.out.println(i + "," + j);
						}
					}
				}
			}
			if(!full) {
				break;
			}
		}
		/*
		if(!fil1.isBankruptcy(tmpData) && full) {
			System.out.println("++++++++ " + s + " +++++++");
			//create1.printTable(tmpData);
			//patterns.add(new Patterns(table1, tmpData));
			Patterns p = new Patterns(table1, tmpData);
			patterns.add(p);
			return;
		}
		*/
	}
	
	final private boolean isFull(String[][] data) {
		boolean full = false;
		for(int i = 0; i < table1.getPlayerList().size(); i++) {
			full = false;
			for(int j = 0; j < table1.getRoleCom().getTotal(); j++) {
				if(data[i][j].equals("○")) {
					full = true;
					break;
				}
			}
			if(!full) {
				return false;
			}
		}
		return true;
	}
	
	public final String[][] getPastData(final int day, final boolean isNight) {
		List<table> tables = read.getDayState();
		int rank = (day - 1) * 2;
		if(isNight) {
			rank = rank - 1;
		}
		output output2;
		if(rank < 0) {
			System.err.println(day + "日目は存在しません");
			output2 = new output(read, tables.get(0));
		}
		else if(rank >= tables.size()) {
			if(isNight) {
				System.err.println(day + "日目の夜は存在しません");
			}
			else {
				System.err.println(day + "日目の昼は存在しません");
			}
			output2 = new output(read, tables.get(tables.size() - 1));
		}
		else {
			if(isNight) {
				System.out.println("-------------------- " + day + "日目夜時点 ----------------------\n");
			}
			else {
				System.out.println("-------------------- " + day + "日目昼時点 ----------------------\n");
			}
			output2 = new output(read, tables.get(rank));
		}
		String[][] past = output2.nonPrintArrangement();
		create1.printTable(past, tables.get(rank));
		
		return past;
	}
	
	public final int getPastAliveRank(final int day, final boolean isNight) {
		if(isNight) {
			System.out.println("\n\n================" + day + "日目夜時点================\n");
		}
		else {
			System.out.println("\n\n================" + day + "日目昼時点================\n");
		}
		int r = (day) * 2;
		if(isNight) {
			r = r - 1;
		}
		return r;
	}
}

/** <div lang="ja"><big>Patterns</big><br>このクラスは盤面に合わせてあり得るプレイヤーと役職の組み合わせを出力させるクラスです。</div><br>
 * 
 * @author 49
 *
 */
final class Patterns {
	private player[] villager;
	private player[] seer;
	private player[] medium;
	private player[] bodyguard;
	private player[] freemason;
	private player[] werewolf;
	private player[] fanatic;
	private player[] foxspirit;
	private player[] immoralist;
	private player[] toxic;
	private final String[][] data;
	private final filtering fil1;
	private final table table1;
	
	Patterns(table table1, String[][] data, filtering fil1) {
		villager = new player[table1.getRoleCom().getRole()[0]];
		seer = new player[table1.getRoleCom().getRole()[1]];
		medium = new player[table1.getRoleCom().getRole()[2]];
		bodyguard = new player[table1.getRoleCom().getRole()[3]];
		freemason = new player[table1.getRoleCom().getRole()[4]];
		werewolf = new player[table1.getRoleCom().getRole()[5]];
		fanatic = new player[table1.getRoleCom().getRole()[6]];
		foxspirit = new player[table1.getRoleCom().getRole()[7]];
		immoralist = new player[table1.getRoleCom().getRole()[8]];
		toxic = new player[table1.getRoleCom().getRole()[9]];
		
		
		this.table1 = table1;
		this.fil1 = fil1;
		this.data = fil1.copyData(data);
		
		int tmpIndex = 0;
		List<player> inPlayer = new ArrayList<>();
		
		for(int j = 0; j < table1.getRoleCom().getTotal(); j++) {
			if(j > 0) {
				if(!table1.getRoleCom().getRoleLabel().get(j).equals(table1.getRoleCom().getRoleLabel().get(j - 1))) {
					//System.out.println(table1.getRoleCom().getRoleLabel().get(j) + ", " + table1.getRoleCom().getRoleLabel().get(j - 1));
					tmpIndex = 0;
				}
				//System.out.println(table1.getRoleCom().getRoleLabel().get(j) + "........" + tmpIndex);
			}
			//System.out.println(j + "***" + tmpIndex);
			for(int i = 0; i < table1.getPlayerList().size(); i++) {
				player pl = table1.getPlayerList().get(i);
				if(data[i][j].equals("○") && !inPlayer.contains(pl)) {
					//System.out.println(i + " : " + table1.getPlayerList().get(i).getName());
					if(table1.getRoleCom().getRoleLabel().get(j).equals("村")) {
						villager[tmpIndex] = pl;
						inPlayer.add(pl);
						break;
					}
					else if(table1.getRoleCom().getRoleLabel().get(j).equals("占")) {
						seer[tmpIndex] = pl;
						inPlayer.add(pl);
						break;
					}
					else if(table1.getRoleCom().getRoleLabel().get(j).equals("霊")) {
						medium[tmpIndex] = pl;
						inPlayer.add(pl);
						break;
					}
					else if(table1.getRoleCom().getRoleLabel().get(j).equals("狩")) {
						bodyguard[tmpIndex] = pl;
						inPlayer.add(pl);
						break;
					}
					else if(table1.getRoleCom().getRoleLabel().get(j).equals("共")) {
						freemason[tmpIndex] = pl;
						inPlayer.add(pl);
						break;
					}
					else if(table1.getRoleCom().getRoleLabel().get(j).equals("狼")) {
						werewolf[tmpIndex] = pl;
						inPlayer.add(pl);
						break;
					}
					else if(table1.getRoleCom().getRoleLabel().get(j).equals("狂")) {
						fanatic[tmpIndex] = pl;
						inPlayer.add(pl);
						break;
					}
					else if(table1.getRoleCom().getRoleLabel().get(j).equals("狐")) {
						foxspirit[tmpIndex] = pl;
						inPlayer.add(pl);
						break;
					}
					else if(table1.getRoleCom().getRoleLabel().get(j).equals("背")) {
						immoralist[tmpIndex] = pl;
						inPlayer.add(pl);
						break;
					}
					else {
						toxic[tmpIndex] = pl;
						inPlayer.add(pl);
						break;
					}
				}
			}
			tmpIndex++;
		}
		
		
	}
	
	final void print(int id) {
		System.out.print("パターン" + id);
		printVict();
		System.out.println();
		System.out.println();
		if(villager.length != 0) {
			System.out.print("村人　  " + villager.length + " : ");
			for(player pl : villager) {
				System.out.print(pl.getName());
				if(pl == table1.getLack()) {
					System.out.print("《初》");
				}
				System.out.print("[" + table1.getCOrole(pl) + "]  ");
			}
			System.out.println();
		}
		if(seer.length != 0) {
			System.out.print("占い師  " + seer.length + " : ");
			for(player pl : seer) {
				System.out.print(pl.getName());
				if(pl == table1.getLack()) {
					System.out.print("《初》");
				}
				System.out.print("[" + table1.getCOrole(pl) + "]  ");
			}
			System.out.println();
		}
		if(medium.length != 0) {
			System.out.print("霊能者  " + medium.length + " : ");
			for(player pl : medium) {
				System.out.print(pl.getName());
				if(pl == table1.getLack()) {
					System.out.print("《初》");
				}
				System.out.print("[" + table1.getCOrole(pl) + "]  ");
			}
			System.out.println();
		}
		if(bodyguard.length != 0) {
			System.out.print("狩人　  " + bodyguard.length + " : ");
			for(player pl : bodyguard) {
				System.out.print(pl.getName());
				if(pl == table1.getLack()) {
					System.out.print("《初》");
				}
				System.out.print("[" + table1.getCOrole(pl) + "]  ");
			}
			System.out.println();
		}
		if(freemason.length != 0) {
			System.out.print("共有者  " + freemason.length + " : ");
			for(player pl : freemason) {
				System.out.print(pl.getName());
				if(pl == table1.getLack()) {
					System.out.print("《初》");
				}
				System.out.print("[" + table1.getCOrole(pl) + "]  ");
			}
			System.out.println();
		}
		System.out.print("人狼　  " + werewolf.length + " : ");
		for(player pl : werewolf) {
			System.out.print(pl.getName());
			if(pl == table1.getLack()) {
				System.out.print("《初》");
			}
			System.out.print("[" + table1.getCOrole(pl) + "]  ");
		}
		System.out.println();
		if(fanatic.length != 0) {
			System.out.print("狂信者  " + fanatic.length + " : ");
			for(player pl : fanatic) {
				System.out.print(pl.getName());
				if(pl == table1.getLack()) {
					System.out.print("《初》");
				}
				System.out.print("[" + table1.getCOrole(pl) + "]  ");
			}
			System.out.println();
		}
		if(foxspirit.length != 0) {
			System.out.print("妖狐　  " + foxspirit.length + " : ");
			for(player pl : foxspirit) {
				System.out.print(pl.getName());
				if(pl == table1.getLack()) {
					System.out.print("《初》");
				}
				System.out.print("[" + table1.getCOrole(pl) + "]  ");
			}
			System.out.println();
		}
		if(immoralist.length != 0) {
			System.out.print("背徳者  " + immoralist.length + " : ");
			for(player pl : immoralist) {
				System.out.print(pl.getName());
				if(pl == table1.getLack()) {
					System.out.print("《初》");
				}
				System.out.print("[" + table1.getCOrole(pl) + "]  ");
			}
			System.out.println();
		}
		if(toxic.length != 0) {
			System.out.print("埋毒者  " + immoralist.length + " : ");
			for(player pl : toxic) {
				System.out.print(pl.getName());
				if(pl == table1.getLack()) {
					System.out.print("《初》");
				}
				System.out.print("[" + table1.getCOrole(pl) + "]  ");
			}
			System.out.println();
		}
		System.out.println();
		System.out.println();
		System.out.println();
	}
	
	final void printVict() {
		List<Integer> deadRank = table1.deadRank();
		List<player> aliveList = new ArrayList<>();;
		int aliveRank = 0;
		for(Integer i : deadRank) {
			if(i > aliveRank) {
				aliveRank = i;
			}
		}
		for(int i = 0; i < table1.getPlayerList().size(); i++) {
			if(deadRank.get(i) == aliveRank) {
				aliveList.add(table1.getPlayerList().get(i));
			}
		}
		int wolves = 0, foxes = 0, humans = 0, seers = 0, bodyguards = 0, immoralists = 0, other = 0;
		for(int i = 0; i < werewolf.length; i++) {
			if(aliveList.contains(werewolf[i])) {
				wolves++;
			}
		}
		for(int i = 0; i < foxspirit.length; i++) {
			if(aliveList.contains(foxspirit[i])) {
				foxes++;
			}
		}
		for(int i = 0; i < seer.length; i++) {
			if(aliveList.contains(seer[i])) {
				seers++;
			}
		}
		for(int i = 0; i < bodyguard.length; i++) {
			if(aliveList.contains(bodyguard[i])) {
				bodyguards++;
			}
		}
		for(int i = 0; i < immoralist.length; i++) {
			if(aliveList.contains(immoralist[i])) {
				immoralists++;
			}
		}
		humans = aliveList.size() - wolves - foxes;
		other = humans - seers - bodyguards - immoralists;
		
		if(wolves > 0 && wolves < humans) {
			int alive = fil1.getAliveRank();
			System.out.print(" 【未決着】");
			if(foxes == 0) {
				//System.out.print("(妖狐陣営負け確定)");
			}
			if(!table1.getSelfAttack() && !table1.getNoAttack() && alive % 2 == 0) {
				if(other + seers + bodyguards <= 1) {
					System.out.print("(村人陣営負け確定)");
				}
				else if(other == 2 && seers == 0 && bodyguards == 0) {
					System.out.print("(村人陣営負け確定)");
				}
				else if(bodyguards == 0 && other + seers == 2 && seers >= 1 && foxes == 1 && immoralists == 0) {
					System.out.print("(村人陣営負け確定)");
				}
			}
			else if(!table1.getSelfAttack() && alive % 2 == 0) {
				if(other + seers + bodyguards <= 1) {
					System.out.print("(村人陣営負け確定)");
				}
			}
			else if(alive % 2 == 0) {
				if(other + seers + bodyguards <= 1 && seers == 0) {
					System.out.print("(村人陣営負け確定)");
				}
				else if(other == 0 && seers == 1 && bodyguards == 0 && (immoralists < 2 || wolves + foxes + immoralists <= 4)) {
					System.out.print("(村人陣営負け確定)");
				}
			}
		}
		else if(foxes > 0) {
			System.out.print(" 【妖狐陣営の勝利】");
		}
		else if(wolves == 0) {
			System.out.print(" 【村人陣営の勝利】");
		}
		else {
			System.out.print(" 【人狼陣営の勝利】");
		}
	}
	
	final String[][] getPatternData() {
		return fil1.copyData(data);
	}
	
}


final class boardState {
	final private static int pretenableRole = 3; //騙り可能役職数 / 占い師、霊能者、狩人
	final private table table1;
	private filtering fil1;
	final private String[][] data;
	final private int originalNonVillSide;      // 配役人外数
	final private int originalWerewolf;         // 配役人狼数
	final private int originalWolfSide;         // 配役人狼陣営数
	final private int minPretendNonVillSide;    // 騙り最小人外数
	final private int maxHiddenNonVillSide;     // 潜伏最大人外数
	final private int leaveCountExpelled;       // 残り吊り縄数
	final private int maxAliveNonVillSide;      // 生存最大人外数
	final private int maxAliveWerewolf;         // 生存最大人狼数
	//private int minAliveWerewolf;         // 生存最小人狼数
	final private int maxAliveFoxspirit;        // 生存最大妖狐数
	final private int minAliveFoxspirit;        // 生存最小妖狐数
	final private int disitionAliveNonVillSide; // 生存確定人外数
	final private int disitionAliveWerewolf;    // 生存確定人狼数
	final private int disitionAliveVillSide;    // 生存確定村人陣営数
	
	public boardState(table table1, filtering fil1, String[][] data) {
		this.table1 = table1;
		this.fil1 = fil1;
		String[][] tmpData = new String[table1.getPlayerList().size()][table1.getRoleCom().getTotal()];
		for(int i = 0; i < table1.getPlayerList().size(); i++) {
			for(int j = 0; j < table1.getRoleCom().getTotal(); j++) {
				tmpData[i][j] = data[i][j];
			}
		}
		this.data = tmpData;
		//* 配役人外数
		originalNonVillSide = fil1.callRoleNum(Role.werewolf) + fil1.callRoleNum(Role.fanatic) + fil1.callRoleNum(Role.foxspirit) + fil1.callRoleNum(Role.immoralist);
		//* 配役人狼数
		originalWerewolf = fil1.callRoleNum(Role.werewolf);
		//* 配役人狼陣営数
		originalWolfSide = fil1.callRoleNum(Role.werewolf) + fil1.callRoleNum(Role.fanatic);
		
		//騙り人外数
		int tmpPretend = 0;
		//確定人外数、確定生存人外数、確定生存人狼数
		int tmpDisitionAliveNonVillSide = 0, tmpDisitionAliveWerewolf = 0;
		for(player pl : table1.getPlayerList()) {
			if(fil1.isDisitionNonVillagerSide(data, pl)) {
				if(table1.getAlivePl().contains(pl)) {
					tmpDisitionAliveNonVillSide++;
					if(fil1.plToRoletype(data, pl).equals(Role.werewolf)) {
						tmpDisitionAliveWerewolf++;
					}
				}
			}
		}
		//* 確定生存人外数
		disitionAliveNonVillSide = tmpDisitionAliveNonVillSide;
		//* 確定生存人狼数
		disitionAliveWerewolf = tmpDisitionAliveWerewolf;
		
		if(table1.getSeerCOList().size() > 0) {
			tmpPretend += table1.getSeerCOList().size() - 1;
		}
		if(table1.getMediumCOList().size() > 0) {
			tmpPretend += table1.getMediumCOList().size() - 1;
		}
		if(table1.getBodyguardCOList().size() > 0) {
			tmpPretend += table1.getBodyguardCOList().size() - 1;
		}
		if(table1.getToxicCOList().size() > 0) {
			tmpPretend += table1.getToxicCOList().size() - 1;
		}
		
		//* 最小騙り人外数
		minPretendNonVillSide = tmpPretend;
		//* 最大潜伏人外数
		maxHiddenNonVillSide = originalNonVillSide - minPretendNonVillSide;
		//* 残り吊り縄数
		leaveCountExpelled = (table1.getAlivePl().size() - 1) / 2;
		
		//妖狐確定死亡の場合
		if(fil1.getAliveRank() > fil1.getMaxRank(tmpData, Role.foxspirit)) {
			maxAliveFoxspirit = 0;
		}
		else {
			maxAliveFoxspirit = 1;
		}
		
		//妖狐確定生存の場合
		if(fil1.getAliveRank() == fil1.getMinRank(tmpData, Role.foxspirit) && fil1.callRoleNum(Role.foxspirit) > 0) {
			minAliveFoxspirit = 1;
		}
		else {
			minAliveFoxspirit = 0;
		}
		
		
		//役職別に(候補別の生存最大人外数)の最大値を格納
		int[] roleMaxNVS = new int[pretenableRole];
		//役職別に(候補別の生存最大人狼数)の最大値を格納
		int[] roleMaxWolf = new int[pretenableRole];
		
		//CO無視状態での最大生存人狼数
		int totalMaxWolf = originalWerewolf;
		if(table1.getAlivePl().size() - minAliveFoxspirit < 2 * originalWerewolf) {
			totalMaxWolf = (table1.getAlivePl().size() - minAliveFoxspirit) / 2;
		}
		
		//CO無視状態での最大生存人外数 == 配役白人外数 + 最大生存人狼数
		int totalMaxNVS = (originalNonVillSide - originalWerewolf) + totalMaxWolf;
		if(maxAliveFoxspirit == 0) {
			totalMaxNVS = (originalWolfSide - originalWerewolf) + totalMaxWolf;
		}
		
		roleMaxWolf[0] = totalMaxWolf;
		roleMaxWolf[1] = totalMaxWolf;
		roleMaxWolf[2] = totalMaxWolf;
		roleMaxNVS[0] = totalMaxNVS;
		roleMaxNVS[1] = totalMaxNVS;
		roleMaxNVS[2] = totalMaxNVS;
		
		//(占い師候補別の生存最大人外数)の最大値
		if(table1.getSeerCOList().size() > 0) {
			List<String[][]> seerList = fil1.seerCOPositioning(tmpData);
			roleMaxNVS[0] = 0;
			roleMaxWolf[0] = 0;
			for(String[][] seer : seerList) {
				int seerPosiNVS = getPerMaxAliveNVS(seer, totalMaxNVS);
				int seerPosiWolf = getPerMaxAliveWolf(seer, totalMaxWolf);
				
				if(roleMaxNVS[0] < seerPosiNVS) {
					roleMaxNVS[0] = seerPosiNVS;
				}
				if(roleMaxWolf[0] < seerPosiWolf) {
					roleMaxWolf[0] = seerPosiWolf;
				}
			}
		}
		
		//(霊能者候補別の生存最大人外数)の最大値
		if(table1.getMediumCOList().size() > 0) {
			List<String[][]> mediumList = fil1.mediumCOPositioning(tmpData);
			roleMaxNVS[1] = 0;
			roleMaxWolf[1] = 0;
			for(String[][] medium : mediumList) {
				int mediumPosiNVS = getPerMaxAliveNVS(medium, totalMaxNVS);
				int mediumPosiWolf = getPerMaxAliveWolf(medium, totalMaxWolf);
				
				if(roleMaxNVS[1] < mediumPosiNVS) {
					roleMaxNVS[1] = mediumPosiNVS;
				}
				if(roleMaxWolf[1] < mediumPosiWolf) {
					roleMaxWolf[1] = mediumPosiWolf;
				}
			}
		}
		
		//(狩人候補別の生存最大人外数)の最大値
		if(table1.getBodyguardCOList().size() > 0) {
			List<String[][]> bodyguardList = fil1.bodyguardCOPositioning(tmpData);
			roleMaxNVS[2] = 0;
			roleMaxWolf[2] = 0;
			for(String[][] bodyguard : bodyguardList) {
				int bodyguardPosiNVS = getPerMaxAliveNVS(bodyguard, totalMaxNVS);
				int bodyguardPosiWolf = getPerMaxAliveWolf(bodyguard, totalMaxWolf);
				
				if(roleMaxNVS[2] < bodyguardPosiNVS) {
					roleMaxNVS[2] = bodyguardPosiNVS;
				}
				if(roleMaxWolf[2] < bodyguardPosiWolf) {
					roleMaxWolf[2] = bodyguardPosiWolf;
				}
			}
		}
		
		
		
		int tmpMaxAliveNVS = totalMaxNVS, tmpMaxAliveWolf = totalMaxWolf;
		for(int i = 0; i < pretenableRole; i++) {
			//System.out.println(roleMaxNVS[i]);
			if(tmpMaxAliveNVS > roleMaxNVS[i]) {
				tmpMaxAliveNVS = roleMaxNVS[i];
			}
			if(tmpMaxAliveWolf > roleMaxWolf[i]) {
				tmpMaxAliveWolf = roleMaxWolf[i];
			}
		}
		
		int tmpMinAliveVillSide = 0;
		for(player pl : table1.getPlayerList()) {
			if(table1.getAlivePl().contains(pl)) {
				if(fil1.isDisitionVillagerSide(data, pl)) {
					tmpMinAliveVillSide++;
				}
			}
		}
		// 人外の可能性がある生存プレイヤー数と最大生存人外数の小さい方を取る
		if(table1.getAlivePl().size() - tmpMinAliveVillSide < tmpMaxAliveNVS) {
			tmpMaxAliveNVS = table1.getAlivePl().size() - tmpMinAliveVillSide;
		}
		
		//* 最大生存人外数
		maxAliveNonVillSide = tmpMaxAliveNVS;
		//* 最大生存人狼数
		maxAliveWerewolf = tmpMaxAliveWolf;
		
		disitionAliveVillSide = tmpMinAliveVillSide;
	}
	
	final private int minDeadWerewolfSide(String[][] data) {
		int underFoxMaxRank = 0, overFoxMaxRankDead = 0, underFoxMaxWolfSide = 0;
		for(player pl : table1.getPlayerList()) {
			if(table1.deadRankPlayer(pl) <= fil1.getMaxRank(data, Role.foxspirit)) {
				if(fil1.isDisitionNonVillagerSide(data, pl)) {
					underFoxMaxRank++;
					if(fil1.stat(data, pl, Role.foxspirit, "×") || fil1.callRoleNum(Role.foxspirit) == 0) {
						if(fil1.stat(data, pl, Role.immoralist, "×") || fil1.callRoleNum(Role.immoralist) == 0) {
							underFoxMaxWolfSide++;
						}
					}
				}
			}
			else if(table1.deadRankPlayer(pl) < fil1.getAliveRank()) {
				if(fil1.isDisitionNonVillagerSide(data, pl)) {
					overFoxMaxRankDead++;
				}
			}
		}
		//System.out.println(underFoxMaxRank + " : " + underFoxMaxWolfSide + " : " + overFoxMaxRankDead + " ");
		if(underFoxMaxRank - fil1.callRoleNum(Role.foxspirit) - fil1.callRoleNum(Role.immoralist) > underFoxMaxWolfSide) {
			underFoxMaxWolfSide = underFoxMaxRank - fil1.callRoleNum(Role.foxspirit) - fil1.callRoleNum(Role.immoralist);
		}
		//System.out.println(underFoxMaxWolfSide + " : " + overFoxMaxRankDead + " ");
		
		return underFoxMaxWolfSide + overFoxMaxRankDead;
	}
	
	
	final private int getPerMaxAliveNVS(String[][] data, int totalMaxNVS) {
		int PosiNVS = 0, PosiAliveVS = 0;
		
		for(player pl : table1.getPlayerList()) {
			//死亡者の確定人外数
			if(fil1.isDisitionNonVillagerSide(data, pl)) {
				if(!table1.getAlivePl().contains(pl)) {
					PosiNVS++;
				}
			}
			//生存者の確定村人陣営数
			else if(fil1.isDisitionVillagerSide(data, pl)) {
				if(table1.getAlivePl().contains(pl)) {
					PosiAliveVS++;
				}
			}
		}
		//配役人外数から死亡者の確定人外数を引く
		PosiNVS = originalNonVillSide - PosiNVS;
		
		//妖狐確定死亡の場合、最大生存人外数は最大生存人狼陣営数以下になる
		if(maxAliveFoxspirit == 0) {
			PosiNVS = originalWolfSide - minDeadWerewolfSide(data);
		}
		//視点別の最大生存人外数は全体でのその人数を越さない
		if(PosiNVS > totalMaxNVS) {
			PosiNVS = totalMaxNVS;
		}
		//最大生存人外数は、(生存者数-生存確定村人陣営数)以下である
		if(PosiNVS > table1.getAlivePl().size() - PosiAliveVS) {
			PosiNVS = table1.getAlivePl().size() - PosiAliveVS;
		}
		
		return PosiNVS;
	}
	
	final private int getPerMaxAliveWolf(String[][] data, int totalMaxWolf) {
		int PosiWolf = 0, PosiAlivewhite = 0;
		
		for(player pl : table1.getPlayerList()) {
			//死亡者の確定人狼数
			if(fil1.plToRoletype(data, pl).equals(Role.werewolf)) {
				if(!table1.getAlivePl().contains(pl)) {
					PosiWolf++;
				}
			}
			//生存者の確白人数
			else if(fil1.stat(data, pl, Role.werewolf, "×")) {
				if(table1.getAlivePl().contains(pl)) {
					PosiAlivewhite++;
				}
			}
		}
		//配役人狼数から死亡者の確定人狼数を引く
		PosiWolf = originalWerewolf - PosiWolf;
		
		//視点別の最大生存人狼数は全体でのその人数を越さない
		if(PosiWolf > totalMaxWolf) {
			PosiWolf = totalMaxWolf;
		}
		//最大生存人狼数は、(生存者数-生存確白人数)以下である
		if(PosiWolf > table1.getAlivePl().size() - PosiAlivewhite) {
			PosiWolf = table1.getAlivePl().size() - PosiAlivewhite;
		}
		
		return PosiWolf;
	}
	
	public final int getOriginalNVS() {
		return originalNonVillSide;
	}
	
	public final int getOriginalWolf() {
		return originalWerewolf;
	}
	
	public final int getMinPretend() {
		return minPretendNonVillSide;
	}
	
	public final int getMaxHidden() {
		return maxHiddenNonVillSide;
	}
	
	public final int getLeaveExpelled() {
		return leaveCountExpelled;
	}
	
	public final int getMaxAliveNVS() {
		return maxAliveNonVillSide;
	}
	
	public final int getMaxAliveWolf() {
		return maxAliveWerewolf;
	}
	
	public final int getMaxAliveFox() {
		return maxAliveFoxspirit;
	}
		
	public final int getMinAliveFox() {
		return minAliveFoxspirit;
	}
		
	public final int getDisitionAliveNVS() {
		return disitionAliveNonVillSide;
	}
		
	public final int getDisitionAliveWolf() {
		return disitionAliveWerewolf;
	}
		
	public final int getDisitionAliveVS() {
		return disitionAliveVillSide;
	}
	
	public final List<player> getDisitionAliveNVSList() {
		List<player> nvs = new ArrayList<>();
		for(player pl : table1.getPlayerList()) {
			if(fil1.isDisitionNonVillagerSide(data, pl)) {
				if(table1.getAlivePl().contains(pl)) {
					nvs.add(pl);
				}
			}
		}
		return nvs;
	}

	public final List<player> getDisitionAliveWolfList() {
		List<player> wolf = new ArrayList<>();
		for(player pl : table1.getPlayerList()) {
			if(fil1.plToRoletype(data, pl).equals(Role.werewolf)) {
				if(table1.getAlivePl().contains(pl)) {
					wolf.add(pl);
					
				}
			}
		}
		return wolf;
	}

	public final List<player> getDisitionAliveVSList() {
		List<player> vs = new ArrayList<>();
		for(player pl : table1.getPlayerList()) {
			if(fil1.isDisitionVillagerSide(data, pl)) {
				if(table1.getAlivePl().contains(pl)) {
					vs.add(pl);
				}
			}
		}
		return vs;
	}
	
	public final List<player> getDisitionAliveNotWolfList() {
		List<player> nw = new ArrayList<>();
		for(player pl : table1.getPlayerList()) {
			if(fil1.stat(data, pl, Role.werewolf, "×")) {
				if(table1.getAlivePl().contains(pl)) {
					nw.add(pl);
				}
			}
		}
		return nw;
	}

	public final void printStatus() {
		System.out.println("配役人外数 : " + getOriginalNVS());
		System.out.println("配役人狼数 : " + getOriginalWolf());
		System.out.println("騙り最小人外数 : " + getMinPretend());
		System.out.println("潜伏最大人外数 : " + getMaxHidden());
		System.out.println("残り吊り縄数 : " + getLeaveExpelled());
		System.out.println("生存最大人外数 : " + getMaxAliveNVS());
		System.out.println("生存最大人狼数 : " + getMaxAliveWolf());
		if(fil1.callRoleNum(Role.foxspirit) > 0) {
			System.out.println("生存最大妖狐数 : " + getMaxAliveFox());
			System.out.println("生存最小妖狐数 : " + getMinAliveFox());
		}
		System.out.print("生存確定人外数 : " + getDisitionAliveNVS());
		if(getDisitionAliveNVS() > 0) {
			System.out.print(" / ");
			for(player pl : getDisitionAliveNVSList()) {
				System.out.print(pl.getName() + " ");
			}
		}
		System.out.println();
		System.out.print("生存確定人狼数 : " + getDisitionAliveWolf());
		if(getDisitionAliveWolf() > 0) {
			System.out.print(" / ");
			for(player pl : getDisitionAliveWolfList()) {
				System.out.print(pl.getName() + " ");
			}
		}
		System.out.println();
		System.out.print("生存確定村人陣営数 : " + getDisitionAliveVS());
		if(getDisitionAliveVS() > 0) {
			System.out.print(" / ");
			for(player pl : getDisitionAliveVSList()) {
				System.out.print(pl.getName() + " ");
			}	
		}
		System.out.println("\n");
	}
	
	public final Map<String, Integer> getTotalState() {
		Map<String, Integer> state = new HashMap<>();
		state.put("cast-Swf", originalNonVillSide);
		state.put("cast-Rw", originalWerewolf);
		state.put("cast-Sw", originalWolfSide);
		state.put("min-pretend-Swf", minPretendNonVillSide);
		state.put("max-hidden-Swf", maxHiddenNonVillSide);
		state.put("count-expelled", leaveCountExpelled);
		state.put("max-a-Swf", maxAliveNonVillSide);
		state.put("max-a-Rw", maxAliveWerewolf);
		state.put("max-a-Rf", maxAliveFoxspirit);
		state.put("min-a-Rf", minAliveFoxspirit);
		state.put("disi-a-Swf", disitionAliveNonVillSide);
		state.put("disi-a-Rw", disitionAliveWerewolf);
		state.put("disi-a-Sv", disitionAliveVillSide);
		return state;
	}
}

