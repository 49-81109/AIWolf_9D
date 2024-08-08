package org.aiwolf.sample.player.arrange_tool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** <div lang="ja"><big>filtering</big><br>このクラスは盤面に合わせて条件フィルタを掛けるクラスです。</div><br>
 * 
 * @author 49
 *
 */
final public class filtering {                                                                                                                                                                                                            
	final private table table1;
	private String[][] tmptotal;
	
	filtering(final table table1) {
		this.table1 = table1;
	}
	
	/** <div lang="ja"><big>追放者と犠牲者についてのフィルタ</big></div><br>
	 * ※これは初期フィルタです。役職別に結果のフィルタをかけた後はこのフィルタをかけないでください。<br>
	 * @param data 全体データ
	 * @return data <br>
	 * @see
	 * ・欠けプレイヤーの欠け除外役職の可能性を削除します。<br>
	 * ・噛みなし設定で初日に呪殺が発生した場合<br>
	 * 　・欠けプレイヤー以外の初日の犠牲者の <b>妖狐</b> と <b>背徳者</b> 以外の候補を削除します。<br>
	 * 　・欠けプレイヤー以外の初日の犠牲者の人数が <b>(妖狐+背徳者)の配役人数 - 1</b> 人の場合、欠けプレイヤーの <b>妖狐</b> と <b>背徳者</b> 以外の候補を削除します。<br>
	 * 　・欠けプレイヤーの初日占いがなしの場合、欠けプレイヤーの  <b>占い師</b> の候補を削除します。<br>
	 * ・犠牲者の <b>妖狐</b> の候補をいったん削除します。(後で占い師の結果によって候補を復活させます)<br>
	 * ・犠牲者が1人の場合、犠牲者の <b>埋毒者</b> の候補を削除します。<br>
	 * ・犠牲者が1人の場合、設定が 自噛み禁止 なら犠牲者の <b>人狼</b> の候補を削除します。<br>
	 * ・追放者の <b>埋毒者</b> の候補をいったん削除します。(後でランダム毒殺があった場合に復活させます)<br>
	 */
	final String[][] remWolfFox_victims(String[][] data) {
		//全ての犠牲者を格納
		List<player> allVictims = new ArrayList<>();
		for(List<player> dayVic : table1.getVictims()) {
			for(player victim : dayVic) {
				if(victim.getId() != -1) {
					allVictims.add(victim);
				}
			}
		}
		//System.out.println(getAliveRank());
		/*
		List<Integer> ra = table1.deadRank();
		for(int i = 0; i < table1.getPlayerList().size(); i++) {
			System.out.println(table1.getPlayerList().get(i).getName() + " -> " + ra.get(i));
		}
		//*/
		
		for(player pl : table1.getPlayerList()) {
			if(table1.getLack().getId() != -1) {
				List<Role> out = table1.outLackRoletype();
				Role[] leave = new Role[out.size()];
				for(int j = 0; j < out.size(); j++) {
					leave[j] = out.get(j);
				}
				InsertData(data, pl, leave, "×", pl == table1.getLack());
			}
			//欠けありで初日呪殺発生時
			if(table1.getLack().getId() != -1 && table1.getFirstVictim().size() > 0) {
				//初日犠牲者以外の犠牲者の数が妖狐+背徳者の配役数未満の場合
				if(table1.getFirstVictim().size() < callRoleNum(Role.foxspirit) + callRoleNum(Role.immoralist)) {
					//欠けは妖狐か背徳者確定
					Role[] leave = {Role.foxspirit, Role.immoralist};
					NotInsertData(data, table1.getLack(), leave, "×", true);
				}
				else {
					Role[] leave = {Role.foxspirit, Role.immoralist};
					InsertData(data, table1.getLack(), leave, "×", true );
				}
				//占い師欠けの場合に初日占いがなかったら
				InsertData(data, table1.getLack(), Role.seer, "×", !table1.getSeerLackFirstDivine());
			}
			
			//初日噛みなしで初日呪殺が発生した場合
			for(player cu : table1.getPlayerList()) {
				if(table1.getFirstVictim().size() > 0) {
					if(table1.getFirstVictim().contains(cu)) {
						Role[] leave = {Role.foxspirit, Role.immoralist};
						NotInsertData(data, cu, leave, "×", true);
					}
					else {
						Role[] leave = {Role.foxspirit, Role.immoralist};
						InsertData(data, cu, leave, "×", table1.getLack() != cu);
					}
				}
			}
			//犠牲者の妖狐の箇所に×を挿入
			InsertData(data, pl, Role.foxspirit, "×", allVictims.contains(pl));
			
			//犠牲者が1人のときその埋毒者と人狼(自噛みなし)否定
			for(int j = 0; j < table1.getVictims().size(); j++) {
				InsertData(data, pl, Role.toxic, "×", table1.getVictims().get(j).size() <= 1 && table1.getVictims().get(j).contains(pl));
				InsertData(data, pl, Role.werewolf, "×", table1.getVictims().get(j).size() <= 1 && table1.getVictims().get(j).contains(pl) && !table1.getSelfAttack());
			}
			//自噛みなし、かつ埋毒者がいない場合は人狼は否定
			if(!table1.getSelfAttack() && callRoleNum(Role.toxic) == 0) {
				InsertData(data, pl, Role.werewolf, "×", allVictims.contains(pl));
			}
			
			//いったん追放者の埋毒者の可能性の削除
			InsertData(data, pl, Role.toxic, "×", table1.getExpelleds().contains(pl));
		}
		return data;
	}
	
	/** <div lang="ja"><big>COについてのフィルタ</big></div><br>
	 * ここでは役職のCOのみを反映させます。能力行使の結果は反映されていません。<br>
	 * ※これは初期フィルタです。役職別に結果のフィルタをかけた後はこのフィルタをかけないでください。<br>
	 * @param data 全体データ
	 * @return data
	 * @see
	 * ・占い師、霊能者。狩人。埋毒者について次の処理を行います。<br>
	 * 　・COしている役職に ● のマークをつけます。<br>
	 * 　・なんらかの役職をCOしているプレイヤーについて、 <b>そのCO役職以外の村人陣営の役職</b> の候補を削除します。<br>
	 * 　・各役職の最初にCOがあった日付時点での死亡者について、その役職の可能性があるならば △ のマークをつけます。(これは乗っ取られている可能性があるという意味です)<br>
	 * 　・各役職の最初にCOがあった日付時点での生存者について、その役職のCOをしていなければ <b>その役職</b> の候補を削除します。<br>
	 * 　・COしているプレイヤーが1人で乗っ取りがないなら、そのCOしているプレイヤーの <b>COしている役職</b> を確定させます。<br>
	 * 
	 */
	final String[][] COFilter(String[][] data) {
		//System.out.println(getAliveRank());
		for(player pl : table1.getPlayerList()) {
			
			// filter seer CO
			for(seerCO seer : table1.getSeerCOList()) {
				if(seer.getSeerCOpl() == pl) {
					//占い師の箇所に無条件に●を挿入
					InsertData(data, pl, Role.seer, "●", true);
					//占い師以外の村人陣営役職の箇所に無条件に×を挿入
					Role[] elseVillageSide = {Role.villager, Role.medium, Role.bodyguard, Role.freemason, Role.toxic};
					InsertData(data, pl, elseVillageSide, "×", true);
				}
			}
			// filter medium CO
			for(mediumCO medium : table1.getMediumCOList()) {
				if(medium.getMediumCOpl() == pl) {
					//霊能者の箇所に無条件に●を挿入
					InsertData(data, pl, Role.medium, "●", true);
					//霊能者以外の村人陣営役職の箇所に無条件に×を挿入
					Role[] elseVillageSide = {Role.villager, Role.seer, Role.bodyguard, Role.freemason, Role.toxic};
					InsertData(data, pl, elseVillageSide, "×", true);
				}
			}
			// filter bodyguard CO
			for(bodyguardCO bodyguard : table1.getBodyguardCOList()) {
				if(bodyguard.getBodyguardCOpl() == pl) {
					//狩人の箇所に無条件に●を挿入
					InsertData(data, pl, Role.bodyguard, "●", true);
					//狩人以外の村人陣営役職の箇所に無条件に×を挿入
					Role[] elseVillageSide = {Role.villager, Role.seer, Role.medium, Role.freemason, Role.toxic};
					InsertData(data, pl, elseVillageSide, "×", true);
				}
			}
			// filter toxic CO
			for(toxicCO toxic : table1.getToxicCOList()) {
				if(toxic.getToxicCOpl() == pl) {
					//埋毒者の箇所に無条件に●を挿入
					InsertData(data, pl, Role.toxic, "●", true);
					//埋毒者以外の村人陣営役職の箇所に無条件に×を挿入
					Role[] elseVillageSide = {Role.villager, Role.medium, Role.seer, Role.bodyguard, Role.freemason};
					InsertData(data, pl, elseVillageSide, "×", true);
				}
			}
			for(seerCO seer : table1.getSeerHideList()) {
				if(seer.getSeerCOpl() == pl) {
					//占い師の箇所に△を挿入
					InsertData(data, pl, Role.seer, "△", !stat(data, pl, Role.seer, "×"));
					if(stat(data, pl, Role.seer, "×")) {
						table1.deleteSeerHide(pl);
					}
				}
			}
			for(mediumCO medium : table1.getMediumHideList()) {
				if(medium.getMediumCOpl() == pl) {
					//霊能者の箇所に△を挿入
					InsertData(data, pl, Role.medium, "△", !stat(data, pl, Role.medium, "×"));
					if(stat(data, pl, Role.medium, "×")) {
						table1.deleteMediumHide(pl);
					}
				}
			}
			for(bodyguardCO bodyguard : table1.getBodyguardHideList()) {
				if(bodyguard.getBodyguardCOpl() == pl) {
					//狩人の箇所に△を挿入
					InsertData(data, pl, Role.bodyguard, "△", !stat(data, pl, Role.bodyguard, "×"));
					if(stat(data, pl, Role.bodyguard, "×")) {
						table1.deleteBodyguardHide(pl);
					}
				}
			}
			for(toxicCO toxic : table1.getToxicHideList()) {
				if(toxic.getToxicCOpl() == pl) {
					//埋毒者の箇所に△を挿入
					InsertData(data, pl, Role.toxic, "△", !stat(data, pl, Role.toxic, "×"));
					if(stat(data, pl, Role.toxic, "×")) {
						table1.deleteToxicHide(pl);
					}
				}
			}
			// filter freemason CO
			for(player freemason : table1.getFreemasonCOList().getFreemasonCOpl()) {
				if(freemason == pl) {
					//共有者の箇所に無条件に○を挿入
					InsertData(data, pl, Role.freemason, "○", true);
					//共有者以外の箇所に無条件に×を挿入
					NotInsertData(data, pl, Role.freemason, "×", true);
				}
			}
		}
		if(table1.getSeerCOList().size() > 0) {
			for(player pl : table1.getPlayerList()) {
				//占い師COしてない生存プレイヤーの占い師の箇所に×を挿入
				InsertData(data, pl, Role.seer, "×", !stat(data, pl, Role.seer, "●") && !stat(data, pl, Role.seer, "△"));
				//占い師が1確しているときその占い師の箇所に○を挿入
				InsertData(data, pl, Role.seer, "○", stat(data, pl, Role.seer, "●") && table1.getSeerCOList().size() == 1 && table1.getSeerHideList().size() == 0);
			}
		}
		if(table1.getMediumCOList().size() > 0) {
			for(player pl : table1.getPlayerList()) {
				//霊能者COしてないプレイヤーの霊能者の箇所に×を挿入
				InsertData(data, pl, Role.medium, "×", !stat(data, pl, Role.medium, "●") && !stat(data, pl, Role.medium, "△"));
				//霊能者が1確しているときその霊能者の箇所に○を挿入
				InsertData(data, pl, Role.medium, "○", stat(data, pl, Role.medium, "●") && table1.getMediumCOList().size() == 1 && table1.getMediumHideList().size() == 0);
			}
		}
		if(table1.getBodyguardCOList().size() > 0) {
			for(player pl : table1.getPlayerList()) {
				//狩人COしてないプレイヤーの狩人の箇所に×を挿入
				InsertData(data, pl, Role.bodyguard, "×", !stat(data, pl, Role.bodyguard, "●") && !stat(data, pl, Role.bodyguard, "△"));
				//狩人が1確しているときその狩人の箇所に○を挿入
				InsertData(data, pl, Role.bodyguard, "○", stat(data, pl, Role.bodyguard, "●") && table1.getBodyguardCOList().size() == 1 && table1.getBodyguardHideList().size() == 0);
			}
		}
		if(table1.getToxicCOList().size() > 0) {
			for(player pl : table1.getPlayerList()) {
				//埋毒者COしてないプレイヤーの埋毒者の箇所に×を挿入
				InsertData(data, pl, Role.toxic, "×", !stat(data, pl, Role.toxic, "●") && !stat(data, pl, Role.toxic, "△"));
				//埋毒者が1確しているときその埋毒者の箇所に○を挿入
				InsertData(data, pl, Role.toxic, "○", stat(data, pl, Role.toxic, "●") && table1.getToxicCOList().size() == 1 && table1.getToxicHideList().size() == 0);
			}
		}
		
		boolean[] isDisitionLack = {false, false, false};
		
		if(table1.outHideRoletype().contains(Role.seer)) {
			if(table1.getSeerCOList().size() == 0) {
				isDisitionLack[0] = true;
			}
		}
		if(table1.outHideRoletype().contains(Role.medium)) {
			if(table1.getMediumCOList().size() == 0) {
				isDisitionLack[0] = true;
			}
		}
		if(table1.outHideRoletype().contains(Role.bodyguard)) {
			if(table1.getBodyguardCOList().size() == 0) {
				isDisitionLack[0] = true;
			}
		}
		if((isDisitionLack[0] && !isDisitionLack[1] && !isDisitionLack[2])) {
			for(player pl : table1.getPlayerList()) {
				InsertData(data, pl, Role.seer, "×", table1.deadRankPlayer(pl) > 1);
			}
		}
		if((!isDisitionLack[0] && isDisitionLack[1] && !isDisitionLack[2])) {
			for(player pl : table1.getPlayerList()) {
				InsertData(data, pl, Role.medium, "×", table1.deadRankPlayer(pl) > 1);
			}
		}
		if((!isDisitionLack[0] && !isDisitionLack[1] && isDisitionLack[2])) {
			for(player pl : table1.getPlayerList()) {
				InsertData(data, pl, Role.bodyguard, "×", table1.deadRankPlayer(pl) > 1);
			}
		}
		
		data = allNonVillagerSideCO(data, false);
		data = dataDisitionSort(data);
		//data = more2Victims(data);
		return data;
	}
	
	/** <div lang="ja"><big>追放時に道連れがあった場合についてのフィルタ</big></div><br>
	 * 追放時に道連れが起きるのは、「埋毒者が追放されてランダムで1人を毒殺した場合」 と 「妖狐が追放されて背徳者が後追いで死亡した場合」 の2通りです。<br>
	 * ※これは初期フィルタです。役職別に結果のフィルタをかけた後はこのフィルタをかけないでください。<br>
	 * @param data 全体データ
	 * @return data
	 * @see
	 * ・ランダム毒殺での道連れが起きた場合、その日の追放者の <b>埋毒者</b> を確定させます。<br>
	 * ・後追いでの道連れが起きた場合、後追い死亡のプレイヤーの <b>背徳者</b> を確定させます。<br>
	 * ・ランダム毒殺での道連れが起きなかった場合、その日の追放者の <b>埋毒者</b> の候補を削除します。(COについてのフィルタで埋毒者の可能性が復活している可能性があるためもう1度削除します)<br>
	 * ・ランダム毒殺での道連れが起きず、後追いでの道連れのみが起きた場合、その日の追放者の <b>妖狐</b> を確定させます。<br>
	 * ・ランダム毒殺での道連れと後追いでの道連れの両方が起きた場合、その日のランダム毒殺対象の <b>妖狐</b> を確定させます。<br>
	 */
	
	
	//道連れがあったときの処理
	final String[][] suicideHappened(String[][] data) {
		//System.out.println(table1.getSuicideDay() + ", " + table1.getPoisonedDay());
		if(table1.getSuicideDay() != 0) {
			player foxspiritPl = table1.getExpelleds().get(table1.getSuicideDay());
			for(player pl : table1.getPlayerList()) {
				if(table1.getSuicideDay() == table1.getPoisonedDay()) {
					//ランダム毒殺と道連れがあった日の毒殺対象は妖狐確定
					foxspiritPl = table1.getPoisoned();
					InsertData(data, pl, Role.foxspirit, "○", pl == foxspiritPl);
				}
				else {
					//道連れのみがあった日の追放者は妖狐確定
					InsertData(data, pl, Role.foxspirit, "○", pl == foxspiritPl);
				}
				//道連れになったプレイヤーは背徳者確定
				InsertData(data, pl, Role.immoralist, "○", table1.getSuicides().contains(pl));
			}
		}
		if(table1.getPoisonedDay() != 0) {
			player toxicPl = table1.getExpelleds().get(table1.getPoisonedDay());
			for(player pl : table1.getPlayerList()) {
				//ランダム毒殺があった日の追放者は埋毒者確定
				InsertData(data, pl, Role.toxic, "○", pl == toxicPl);
			}
		}
		
		// ランダム毒殺がなかった日の追放者は埋毒者ではない
		for(player pl : table1.getPlayerList()) {
			//追放者の埋毒者の可能性の削除
			for(int j = 0; j < table1.getExpelleds().size(); j++) {
				InsertData(data, pl, Role.toxic, "×", table1.getExpelleds().get(j) == pl && table1.getPoisonedDay() != j);
			}
		}
		
		data = dataDisitionSort(data);
		tmptotal = copyData(data);
		return data;
	}
	
	/** <div lang="ja"><big>占い師自称者の個別視点のデータを取得</big></div><br>
	 * 
	 * @param data 全体データ
	 * @return 各占い師視点での個別データのリスト
	 * @see
	 * 
	 */
	
	//占い師CO別に整理
	
	final List<String[][]> seerCOPositioning(String[][] data) {
		//System.out.println("4. 占い師CO別に整理");
		//createTable tab = new createTable(table1.getPlayerList(), table1.getRoleCom());
		//tab.printTable(data);
		List<String[][]> seerCOdata = new ArrayList<>();
		tmptotal = copyData(data);
		for(seerCO seer : table1.getSeerCOList()) {
			//System.out.println("占い師 ===> " + table1.getSeerCOList().get(i).getSeerCOpl().getName());
			String[][] tmp = copyData(data);
			//tab.printTable(tmp);
			for(player pl : table1.getPlayerList()) {
				//占い師該当プレイヤーの場合
				if(pl == seer.getSeerCOpl()) {
					//占い師の箇所に無条件に○を挿入
					InsertData(tmp, pl, Role.seer, "○", true);
					//占い師以外の箇所に無条件に×を挿入
					NotInsertData(tmp, pl, Role.seer, "×", true);
					//占い結果を反映
					tmp = seerResult(seer, tmp, tmptotal, false);
				}
				//該当しない場合
				else {
					//占い師の箇所に無条件に×を挿入
					InsertData(tmp, pl, Role.seer, "×", true);
				}
			}
			//データ整理
			//createTable tab = new createTable(table1.getPlayerList(), table1.getRoleCom());
			//System.out.println("占い師 ===> " + table1.getSeerCOList().get(i).getSeerCOpl().getName());
			//System.out.println("****tmp");
			//tab.printTable(tmp);
			tmp = totalSort(tmp);
			  //System.out.println("****tmp");
			   //tab.printTable(tmp);
			//各占い師視点に追加
			seerCOdata.add(tmp);
		}
		//占い師乗っ取りの場合
		for(seerCO seer : table1.getSeerHideList()) {
			String[][] tmp = copyData(data);
			for(player pl : table1.getPlayerList()) {
				if(pl == seer.getSeerCOpl()) {
					InsertData(tmp, pl, Role.seer, "○", true);
					NotInsertData(tmp, pl, Role.seer, "×", true);
				}
				else {
					InsertData(tmp, pl, Role.seer, "×", true);
				}
			}
			tmp = seerResult(seer, tmp, tmptotal, table1.getSeerCOList().size() > 0);
			//System.out.println("***" + table1.getSeerHideList().get(i).getSeerCOpl().getName());
			//tab.printTable(tmp);
			tmp = totalSort(tmp);
			//tab.printTable(tmp);
			seerCOdata.add(tmp);
		}
		//createTable tab = new createTable(table1.getPlayerList(), table1.getRoleCom());
		
		//System.out.println("****data");
		//tab.printTable(data);
		
		//妖狐呪殺判定の反映
		data = tmptotal;
		//System.out.println("***tmptotal");
		//tab.printTable(tmptotal);
		//tab.printTable(data);
		return seerCOdata;
	}
	
	/** <div lang="ja"><big>霊能者自称者の個別視点のデータを取得</big></div><br>
	 * 
	 * @param data 全体データ
	 * @return 各霊能者視点での個別データのリスト
	 */
	
	//霊能者CO別に整理
	final List<String[][]> mediumCOPositioning(String[][] data) {
		//System.out.println("5. 霊能者CO別に整理");
		List<String[][]> mediumCOdata = new ArrayList<>();
		for(mediumCO medium : table1.getMediumCOList()) {
			//System.out.println("霊能者 ===> " + table1.getMediumCOList().get(i).getMediumCOpl().getName());
			String[][] tmp = copyData(data);
			for(player pl : table1.getPlayerList()) {
				if(pl == medium.getMediumCOpl()) {
					//霊能者の箇所に無条件に○を挿入
					InsertData(tmp, pl, Role.medium, "○", true);
					//霊能者以外の箇所に無条件に×を挿入
					NotInsertData(tmp, pl, Role.medium, "×", true);
					//霊能結果を反映
					tmp = mediumResult(medium, tmp, false);
				}
				else {
					//霊能者の箇所に無条件に×を挿入
					InsertData(tmp, pl, Role.medium, "×", true);
				}
			}
			//データ整理
			tmp = totalSort(tmp);
			  //createTable tab = new createTable(table1.getPlayerList(), table1.getRoleCom());
			  //tab.printTable(tmp);
			//各霊能者視点に追加
			mediumCOdata.add(tmp);
		}
		//乗っ取られ
		for(mediumCO medium : table1.getMediumHideList()) {
			//System.out.println("霊能者 ===> " + table1.getMediumCOList().get(i).getMediumCOpl().getName());
			String[][] tmp = copyData(data);
			for(player pl : table1.getPlayerList()) {
				if(pl == medium.getMediumCOpl()) {
					InsertData(tmp, pl, Role.medium, "○", true);
					NotInsertData(tmp, pl, Role.medium, "×", true);
					tmp = mediumResult(medium, tmp, table1.getMediumCOList().size() > 0);
				}
				else {
					InsertData(tmp, pl, Role.medium, "×", true);
				}
			}
			mediumCOdata.add(tmp);
		}
		
		return mediumCOdata;
	}
	
	/** <div lang="ja"><big>狩人自称者の個別視点のデータを取得</big></div><br>
	 * 
	 * @param data 全体データ
	 * @return 各狩人視点での個別データのリスト
	 */
	
	//狩人CO別に整理
	final List<String[][]> bodyguardCOPositioning(String[][] data) {
		//System.out.println("6. 狩人CO別に整理");
		data = tmptotal;
		List<String[][]> bodyguardCOdata = new ArrayList<>();
		for(bodyguardCO bodyguard : table1.getBodyguardCOList()) {
			//System.out.println("狩人 ===> " + table1.getBodyguardCOList().get(i).getBodyguardCOpl().getName());
			String[][] tmp = copyData(data);
			for(player pl : table1.getPlayerList()) {
				if(pl == bodyguard.getBodyguardCOpl()) {
					//狩人の箇所に無条件に○を挿入
					InsertData(tmp, pl, Role.bodyguard, "○", true);
					//狩人以外の箇所に無条件に×を挿入
					NotInsertData(tmp, pl, Role.bodyguard, "×", true);
					//護衛先を反映
					tmp = bodyguardResult(bodyguard, tmp, false);
				}
				else {
					//狩人の箇所に無条件に×を挿入
					InsertData(tmp, pl, Role.bodyguard, "×", true);
				}
			}
			tmp = totalSort(tmp);
			bodyguardCOdata.add(tmp);
		}
		//乗っ取られ
		for(bodyguardCO bodyguard : table1.getBodyguardHideList()) {
			//System.out.println("狩人 ===> " + table1.getBodyguardCOList().get(i).getBodyguardCOpl().getName());
			String[][] tmp = copyData(data);
			for(player pl : table1.getPlayerList()) {
				if(pl == bodyguard.getBodyguardCOpl()) {
					InsertData(tmp, pl, Role.bodyguard, "○", true);
					NotInsertData(tmp, pl, Role.bodyguard, "×", true);
					tmp = bodyguardResult(bodyguard, tmp, table1.getBodyguardCOList().size() > 0);
				}
				else {
					InsertData(tmp, pl, Role.bodyguard, "×", true);
				}
			}
			bodyguardCOdata.add(tmp);
		}
		return bodyguardCOdata;
	}
	
	/** データのコピー
	 * 
	 * @param data
	 * @return
	 */
	
	//データのコピー
	final String[][] copyData (String[][] data) {
		String[][] tmp1 = new String[data.length][];
		for(int i = 0; i < data.length; i++) {
			String[] tmp2 = new String[data[i].length];
			for(int j = 0; j < data[i].length; j++) {
				tmp2[j] = data[i][j];
			}
			tmp1[i] = tmp2;
		}
		return tmp1;
	}
	
	/** <div lang="ja"><big>占い師の占い結果の反映</big></div><br>
	 * 占い結果に合わせて役職候補を絞ります。<br>
	 * @param seer 占い師のCOや占い結果などの情報
	 * @param data 占い師個別視点でのデータ
	 * @param allData 全体データ
	 * @param isHide 乗っ取られかどうか
	 * @return 占い師個別視点でのデータ
	 * @see
	 * 占い結果が●(黒)なら、占い対象の <b>人狼</b> を確定させます。<br>
	 * 占い結果が○(白)なら、占い対象の <b>人狼</b> の候補を削除します。<br>
	 * 占い結果が○(白)で占い対象が生存していたら、占い対象の <b>妖狐</b> の候補を削除します。<br>
	 * 占い結果が○(白)で占い対象がその日の犠牲者に含まれ、占い対象を初めて占った場合、占い対象の <b>妖狐</b> の候補を復活させます。(全体データでも復活させます)<br>
	 * 占い結果が○(白)でその日の犠牲者が占い対象を含む2人の場合、占い対象の <b>妖狐</b> と <b>埋毒者</b> 以外の候補を削除します。<br>
	 * 占い結果が○(白)でその日の犠牲者が占い対象を含む3人以上の場合、占い対象の <b>妖狐</b> を確定させます。<br>
	 * 噛みなし設定で初日に呪殺死亡が出た場合、初日の占い対象の <b>妖狐</b> を確定させます。<br>
	 * <br><i>遺言呪殺の処理</i><br>
	 * 犠牲者が自身を含む2人で自身ではない犠牲者を占い対象にしてなかったら、自身ではない犠牲者の <b>妖狐</b> を確定させます。<br>
	 * 犠牲者が自身を含む3人以上の場合、自身以外のその日の犠牲者の <b>妖狐</b> と <b>背徳者</b> 以外の候補を削除します。<br>
	 * 犠牲者が自身を含む2人以上の場合、その日の犠牲者以外の <b>妖狐</b> の候補を削除します。<br><br>
	 * 犠牲者が2人でその日以前に自身がすでに死亡、またはどちらも占い対象にしていなかった場合、その日の犠牲者の <b>人狼</b> と <b>埋毒者</b> 以外の候補を削除します。<br>
	 */
	
	//占い師の結果反映
	final String[][] seerResult(seerCO seer, String[][] data, String[][] allData, boolean isHide) {
		/*
		createTable tab = new createTable(table1.getPlayerList(), table1.getRoleCom());
		tab.printTable(data);
		//*/
		
		// 最初にその日の占い先でない犠牲者は妖狐ではない
		for(int i = 0; i < table1.getVictims().size(); i++) {
			if(i < seer.getDivineList().size()) {
				if(!table1.getVictims().get(i).contains(seer.getDivineList().get(i).getPl())) {
					for(player pl : table1.getPlayerList()) {
						InsertData(data, pl, Role.foxspirit, "×", table1.getVictims().get(i).contains(pl));
					}
				}
			}
			else {
				for(player pl : table1.getPlayerList()) {
					InsertData(data, pl, Role.foxspirit, "×", table1.getVictims().get(i).contains(pl));
				}
			}
		}
		
		
		
		for(int i = 0; i < seer.getDivineList().size(); i++) {
			//●黒結果の場合
			if(seer.getDivineList().get(i).getIsWerewolf()) {
				for(player pl : table1.getPlayerList()) {
					InsertData(data, pl, Role.werewolf, "○", !stat(data, pl, Role.werewolf, "×") && seer.getDivineList().get(i).getPl() == pl);
					NotInsertData(data, pl, Role.werewolf, "×", seer.getDivineList().get(i).getPl() == pl);
				}
			}
			//○白結果の場合
			else {
				for(player pl : table1.getPlayerList()) {
					if(seer.getDivineList().get(i).getPl() == pl) {
						//人狼の可能性の削除
						InsertData(data, pl, Role.werewolf, "×", true);
						//占い対象がその日の犠牲者に含まれない場合妖狐の可能性の削除
						InsertData(data, pl, Role.foxspirit, "×", !table1.getVictims().get(i).contains(seer.getDivineList().get(i).getPl()));
						
						if(table1.getVictims().get(i).contains(seer.getDivineList().get(i).getPl())) {
							List<player> alreadyDivined = new ArrayList<>();
							for(int l = 0; l < i; l++) {
								alreadyDivined.add(seer.getDivineList().get(l).getPl());
							}
							//死亡白先の妖狐の可能性の復活
							if(callRoleNum(Role.foxspirit) > 0) {
								InsertData(data, pl, Role.foxspirit, "  ", stat(data, pl, Role.foxspirit, "×") && !alreadyDivined.contains(seer.getDivineList().get(i).getPl()));
								InsertData(allData, pl, Role.foxspirit, "  ", stat(allData, pl, Role.foxspirit, "×") && !alreadyDivined.contains(seer.getDivineList().get(i).getPl()));
								//埋毒者のいない村でその日の犠牲者が2人以上なら呪殺確定
								if(table1.getVictims().get(i).size() > 1 && callRoleNum(Role.toxic) == 0) {
									InsertData(data, pl, Role.foxspirit, "○", !alreadyDivined.contains(seer.getDivineList().get(i).getPl()));
								}
								
								//埋毒者のいる村でその日の犠牲者が2人の場合
								if(table1.getVictims().get(i).size() == 2 && callRoleNum(Role.toxic) > 0) {
									//占ってない方の犠牲者
									player NotDivinedVictim = new player("×", -1);
									for(player part : table1.getPlayerList()) {
										//System.out.println("[1]" + table1.getPlayerList().get(k).getName() + table1.getVictims().get(i).contains(table1.getPlayerList().get(k)));
										//System.out.println("[2]" + table1.getPlayerList().get(k).getName() + ", " + (seer.getDivineList().get(i).getPl() != table1.getPlayerList().get(j)));
										if(table1.getVictims().get(i).contains(part) && seer.getDivineList().get(i).getPl() != part) {
											NotDivinedVictim = part;
											break;
										}
									}
									//System.out.println(seer.getSeerCOpl().getName() + ", " + NotDivinedVictim);
									
									//白先が妖狐否定されている場合、占ってない方の犠牲者が人狼確定の場合、その白先は埋毒者確定
									if((stat(data, pl, Role.foxspirit, "×") && seer.getDivineList().get(i).getPl() == pl)
											|| stat(data, NotDivinedVictim, Role.werewolf, "○")) {
										InsertData(data, pl, Role.toxic, "○", !stat(data, pl, Role.toxic, "×") && seer.getDivineList().get(i).getPl() == pl);
										//白先でないほうの犠牲者は人狼確定
										InsertData(data, NotDivinedVictim, Role.werewolf, "○", !stat(data, NotDivinedVictim, Role.werewolf, "×"));
									}
									
									//白先が埋毒者否定されている場合、占ってない方の犠牲者が人狼でないことが確定の場合、その白先は妖狐確定
									else if((stat(data, pl, Role.toxic, "×") && seer.getDivineList().get(i).getPl() == pl)
											|| stat(data, NotDivinedVictim, Role.werewolf, "×")) {
										
										InsertData(data, pl, Role.foxspirit, "○", !stat(data, pl, Role.foxspirit, "×") && seer.getDivineList().get(i).getPl() == pl);
									}
									
									//それ以外、その白先は埋毒者か妖狐
									else {
										Role[] leave = {Role.foxspirit, Role.toxic};
										NotInsertData(data, pl, leave, "×", seer.getDivineList().get(i).getPl() == pl);
									}
								}
								//その日の犠牲者が3人以上の場合、呪殺確定
								if(table1.getVictims().get(i).size() > 2) {
									InsertData(data, pl, Role.foxspirit, "○", !alreadyDivined.contains(seer.getDivineList().get(i).getPl()));
								}
							}
						}
					}
				}
			}
		}
		//初日呪殺の処理
		if(table1.getFirstVictim().size() > 0) {
			for(player vic : table1.getFirstVictim()) {
				if(seer.getDivineList().size() > 1) {
					if(vic == seer.getDivineList().get(1).getPl()) {
						for(player pl : table1.getPlayerList()) {
							InsertData(data, pl, Role.foxspirit, "○", pl == seer.getDivineList().get(1).getPl() && !seer.getDivineList().get(1).getIsWerewolf());
						}
					}
					else {
						for(player pl : table1.getPlayerList()) {
							InsertData(data, pl, Role.foxspirit, "×", pl == vic);
						}
					}
				}
			}
		}
		
		//遺言呪殺の処理
		int Deadtime = table1.deadRankPlayer(seer.getSeerCOpl());
		List<player> divineTarget = seer.getDivineTarget();
		if((Deadtime + 1) / 2 < table1.getVictims().size()) {
			// 占い師が死亡した日に2人以上犠牲者が出たとき
			if(Deadtime % 2 == 1 && table1.getVictims().get((Deadtime + 1) / 2).size() > 1) {
				for(player pl : table1.getPlayerList()) {
					//その日の犠牲者について
					if(table1.getVictims().get((Deadtime + 1) / 2).contains(pl)) {
						//占い師でない犠牲者
						if(pl != seer.getSeerCOpl()) {
							//System.out.println(table1.getPlayerList().get(j).getName());
							//死亡白先の妖狐の可能性の復活
							if(callRoleNum(Role.foxspirit) > 0) {
								InsertData(data, pl, Role.foxspirit, "  ", stat(data, pl, Role.foxspirit, "×") && !divineTarget.contains(pl));
								InsertData(allData, pl, Role.foxspirit, "  ", stat(allData, pl, Role.foxspirit, "×") && !divineTarget.contains(pl));
								if(table1.getVictims().get((Deadtime + 1) / 2).size() == 2) {
									InsertData(data, pl, Role.foxspirit, "○", !divineTarget.contains(pl));
								}
								// 自身以外のその日の犠牲者は妖狐か背徳者
								Role[] leave = {Role.foxspirit, Role.immoralist};
								NotInsertData(data, pl, leave, "×", true);
							}
						}
					}
					//複数死体の中に含まれないプレイヤーは妖狐否定
					InsertData(data, pl, Role.foxspirit, "×", Deadtime != table1.deadRankPlayer(pl));
				}
			}
		}
		
		//犠牲者が2人いたときにその日にどちらの犠牲者も占ってない場合
		for(int i = 0; i < table1.getVictims().size(); i++) {
			if(table1.getVictims().get(i).size() == 2 && callRoleNum(Role.toxic) > 0) {
				
				if(seer.getDivineList().size() <= i) {
					//その犠牲者以外は埋毒者ではない
					for(player pl : table1.getPlayerList()) {
						InsertData(data, pl, Role.toxic, "×", !table1.getVictims().get(i).contains(pl));
						disitionWerewolfContainVic(data, i, pl, !table1.getVictims().get(i).contains(pl), -1, false);
					}
					Role[] leave = {Role.werewolf, Role.toxic};
					//その犠牲者は埋毒者か人狼確定
					for(player pl : table1.getPlayerList()) {
						if(table1.getVictims().get(i).contains(pl)) {
							NotInsertData(data, pl, leave, "×", true);
							
							//また片方が人狼ならもう片方は埋毒者 (埋毒者は1人しか入れられない)
							for(player part : table1.getPlayerList()) {
								if(table1.getVictims().get(i).contains(part) && pl != part) {
									InsertData(data, pl, Role.toxic, "○", !stat(data, pl, Role.toxic, "×") && stat(data, part, Role.werewolf, "○"));
									break;
								}
							}
							
						}
					}
				}
				else {
					//その犠牲者以外は埋毒者ではない
					for(player pl : table1.getPlayerList()) {
						InsertData(data, pl, Role.toxic, "×", !table1.getVictims().get(i).contains(pl) && !table1.getVictims().get(i).contains(seer.getDivineList().get(i).getPl()));
						disitionWerewolfContainVic(data, i, pl, !table1.getVictims().get(i).contains(seer.getDivineList().get(i).getPl()), -1, false);
					}
					if(!table1.getVictims().get(i).contains(seer.getDivineList().get(i).getPl())) {
						Role[] leave = {Role.werewolf, Role.toxic};
						//その犠牲者は埋毒者か人狼確定
						for(player pl : table1.getPlayerList()) {
							if(table1.getVictims().get(i).contains(pl)) {
								NotInsertData(data, pl, leave, "×", true);
								
								//また片方が人狼ならもう片方は埋毒者 (埋毒者は1人しか入れられない)
								for(player part : table1.getPlayerList()) {
									if(table1.getVictims().get(i).contains(part) && pl != part) {
										InsertData(data, pl, Role.toxic, "○", !stat(data, pl, Role.toxic, "×") && stat(data, part, Role.werewolf, "○"));
										break;
									}
								}
								
							}
						}
					}
				}
			}
		}
		
		data = allNonVillagerSideCO(data, isHide);
		//System.out.println("input");
		//tab.printTable(data);
		//table1.deleteSeerHide(null);
		return data;
	}
	
	/** <div lang="ja"><big>霊能者の霊能結果の反映</big></div><br>
	 * 霊能結果に合わせて役職候補を絞ります。<br>
	 * @param medium 霊能者のCOや占い結果などの情報
	 * @param data 霊能者個別視点でのデータ
	 * @param isHide 乗っ取られかどうか
	 * @return 霊能者個別視点でのデータ
	 * @see
	 * 霊能結果が●(黒)なら、前日の追放者の <b>人狼</b> を確定させます。<br>
	 * 霊能結果が○(白)なら、前日の追放者の <b>人狼</b> の候補を削除します。<br>
	 */
	
	//霊能者の結果反映
	final String[][] mediumResult(mediumCO medium, String[][] data, boolean isHide) {
		for(int i = 0; i < medium.getSenseList().size(); i++) {
			//●黒結果の場合
			if(medium.getSenseList().get(i).getIsWerewolf()) {
				for(player pl : table1.getPlayerList()) {
					InsertData(data, pl, Role.werewolf, "○", !stat(data, pl, Role.werewolf, "×") && medium.getSenseList().get(i).getPl() == pl);
					NotInsertData(data, pl, Role.werewolf, "×", medium.getSenseList().get(i).getPl() == pl);
				}
			}
			//○白結果の場合
			else {
				for(player pl : table1.getPlayerList()) {
					//人狼の可能性の削除
					InsertData(data, pl, Role.werewolf, "×", !stat(data, pl, Role.werewolf, "×") && medium.getSenseList().get(i).getPl() == pl);
				}
			}
		}
		
		data = allNonVillagerSideCO(data, isHide);
		
		return data;
	}
	
	/** <div lang="ja"><big>狩人の護衛先の反映</big></div><br>
	 * 護衛先に合わせて役職候補を絞ります。
	 * @param bodyguard 狩人のCOや占い結果などの情報
	 * @param data 狩人個別視点でのデータ
	 * @param isHide 乗っ取られかどうか
	 * @return 狩人個別視点でのデータ
	 * @see
	 * 犠牲者がいなかった場合、妖狐が確定で死亡しているか妖狐が配役に存在せず、噛みなしと自噛みの設定が両方禁止なら、その日の護衛先の <b>人狼</b> の候補を削除します。<br>
	 * 犠牲者がその日の護衛先の1人のみの場合、その日の護衛先の <b>妖狐</b> を確定させます。(貫通呪殺)<br>
	 * 犠牲者の中にその日の護衛先を含む2人以上の場合、その日の護衛先の <b>妖狐</b> と <b>背徳者</b>  と <b>人狼</b> 以外の候補を削除します。<br>
	 * 犠牲者の中にその日の護衛先を含む2人以上で埋毒者が配役に存在しない場合、その日の護衛先の <b>妖狐</b> と <b>背徳者</b> 以外の候補を削除します。<br>
	 */
	
	//狩人の護衛反映(占い師の結果を先に反映させること)
	final String[][] bodyguardResult(bodyguardCO bodyguard, String[][] data, boolean isHide) {
		int FoxspiritMaxRank = getMaxRank(data, Role.foxspirit);
		int aliveRank = getAliveRank();
		
		for(int i = 1; i <= aliveRank; i = i + 2) {
			//妖狐確定死亡時
			if(FoxspiritMaxRank < i && table1.getVictims().size() > (i + 1) / 2) {
				//もし犠牲者なしなら
				if(table1.getVictims().get((i + 1) / 2).get(0).getId() == -1) {
					for(player pl : table1.getPlayerList()) {
						//その日の護衛先は噛みなし禁止、かつ自噛み禁止なら人狼ではない
						if(bodyguard.getGuardList().size() > (i + 1) / 2) {
							InsertData(data, pl, Role.werewolf, "×", pl == bodyguard.getGuardList().get((i + 1) / 2) && !table1.getNoAttack() && !table1.getSelfAttack());
						}
					}
				}
			}
			//System.out.println(bodyguard.getGuardList().get((i + 1) / 2).getName() + "護衛");
			
			//もし護衛先が貫通で死亡していた場合
			if(bodyguard.getGuardList().size() > ((i + 1) / 2) && table1.getVictims().size() > (i + 1) / 2) {
				//System.out.println(bodyguard.getGuardList().get((i + 1) / 2).getName() + "護衛");
				if(table1.getVictims().get((i + 1) / 2).contains(bodyguard.getGuardList().get((i + 1) / 2)) && table1.getVictims().get((i + 1) / 2).get(0).getId() != -1) {
					
					//1死体ならその位置は妖狐(貫通呪殺)
					if(table1.getVictims().get((i + 1) / 2).size() == 1) {
						for(player pl : table1.getPlayerList()) {
							InsertData(data, pl, Role.foxspirit, "○", pl == bodyguard.getGuardList().get((i + 1) / 2) && !stat(data, pl, Role.foxspirit, "×"));
							NotInsertData(data, pl, Role.foxspirit, "×", pl == bodyguard.getGuardList().get((i + 1) / 2));
							
						}
					}
					//そうでないならその位置は妖狐か背徳者か人狼(貫通呪殺か貫通道連れか埋毒者噛みの反撃)
					else {
						for(player pl : table1.getPlayerList()) {
							if(pl == bodyguard.getGuardList().get((i + 1) / 2)) {
								
								if(table1.getRoleCom().getRole()[9] > 0) {
									Role[] leave = {Role.foxspirit, Role.immoralist, Role.werewolf};
									NotInsertData(data, pl, leave, "×", true);
								}
								//埋毒者がいないならその位置は妖狐か背徳者
								else {
									Role[] leave = {Role.foxspirit, Role.immoralist};
									NotInsertData(data, pl, leave, "×", true);
								}
							}
						}
					}
				}
			}
		}
		
		data = allNonVillagerSideCO(data, isHide);
		
		return data;
	}
	
	/** <div lang="ja"><big>役職確定の場合の他候補の削除</big></div><br>
	 * 役職が確定している場合、そのプレイヤーの他の役職の候補を削除し、その役職が配役人数分確定していた場合確定してないプレイヤーのその役職の候補を削除します。<br>
	 * 
	 * @param data 全体データ
	 * @return data
	 */
	
	//確定役職の他の候補の削除
	final String[][] dataDisitionSort(String[][] data) {
		// 縦方向の整理
		for(int i = 0; i < 10; i++) {
			int col = 0;
			for(int j = 0; j < i; j++) {
				col += table1.getRoleCom().getRole()[j];
			}
			//System.out.println(col);
			if(col < table1.getPlayerList().size()) {
				int disitionNum = 0;
				for(int j = 0; j < table1.getPlayerList().size(); j++) {
					if(data[j][col].equals("○")) {
						disitionNum++;
					}
				}
				if(disitionNum >= table1.getRoleCom().getRole()[i]) {
					for(int j = 0; j < table1.getPlayerList().size(); j++) {
						for(int k = col; k < col + table1.getRoleCom().getRole()[i]; k++) {
							if(!data[j][k].equals("○")) {
								data[j][k] = "×";
							}
						}
					}
				}
			}
		}
		
		// 横方向の整理
		for(int i = 0; i < table1.getPlayerList().size(); i++) {
			for(int j = 0; j < table1.getRoleCom().getTotal(); j++) {
				if(data[i][j].equals("○")) {
					for(int k = 0; k < table1.getRoleCom().getTotal(); k++) {
						if(!data[i][k].equals("○")) {
							data[i][k] = "×";
						}
					}
					break;
				}
			}
		}
		return data;
	}
	
	/** <div lang="ja"><big>役職候補が1つに定まった場合の確定処理</big></div><br>
	 * 各プレイヤーについてそのプレイヤーの役職候補が1つしかない場合、各役職についてその役職の該当プレイヤー候補が配役人数分しかなかった場合、そのプレイヤーと役職を確定させます。<br>
	 * 
	 * @param data 全体データ
	 * @return data
	 */
	
	//役職候補が1つしかないところは確定
	final String[][] leaveRoleDisition(String[][] data) {
		for(int i = 0; i < table1.getPlayerList().size(); i++) {
			String roleSelect = "";
			boolean leave1 = true;
			for(int j = 0; j < table1.getRoleCom().getTotal(); j++) {
				if(!data[i][j].equals("×")) {
					if(roleSelect.equals("")) {
						roleSelect = table1.getRoleCom().getRoleLabel().get(j);
					}
					else if(!roleSelect.equals(table1.getRoleCom().getRoleLabel().get(j))){
						leave1 = false;
						roleSelect = table1.getRoleCom().getRoleLabel().get(j);
					}
				}
			}
			if(!roleSelect.equals("") && leave1) {
				for(int j = 0; j < table1.getRoleCom().getTotal(); j++) {
					if(!data[i][j].equals("×")) {
						data[i][j] = "○";
					}
				}
			}
		}
		
		String strRole = "村";
		int numRole = 0;
		for(int i = 0; i < table1.getRoleCom().getTotal(); i++) {
			if(!strRole.equals(table1.getRoleCom().getRoleLabel().get(i))) {
				strRole = table1.getRoleCom().getRoleLabel().get(i);
				numRole++;
				while(table1.getRoleCom().getRole()[numRole] == 0) {
					numRole++;
				}
			}
			int disiCount = 0;
			if(table1.getRoleCom().getRole()[numRole] > 0) {
				for(int j = 0; j < table1.getPlayerList().size(); j++) {
					if(!data[j][i].equals("×")) {
						disiCount++;
					}
				}
				if(disiCount == table1.getRoleCom().getRole()[numRole]) {
					for(int j = 0; j < table1.getPlayerList().size(); j++) {
						if(!data[j][i].equals("×")) {
							data[j][i] = "○";
						}
					}
				}
			}
		}
		
		return data;
	}
	
	/** <div lang="ja"><big>生存人狼数の調整</big></div><br>
	 * ゲームの途中で人狼数が0になったり、人間数以上になったりなど、ゲーム決着がついた状態になってしまう候補を削除します。<br>
	 * @param data 全体データ
	 * @return data
	 */
	
	//人狼の生存数が途中で0になったり半数占めたりしないように調整
	final String[][] disitionWerewolfAtLeast(String[][] data) {
		List<Integer> deadList = table1.deadRank();
		int WerewolfMaxRank = getMaxRank(data, Role.werewolf);
		int aliveRank = getAliveRank();
		int maxCount = table1.sameRankSelect(WerewolfMaxRank, Role.werewolf, data);
		//System.out.println(WerewolfMaxRank + "," + aliveRank + "," + maxCount);
		if(maxCount == 1) {
			/*/ ?
			for(int i = 0; i < table1.getPlayerList().size(); i++) {
				InsertData(data, i, "狼", "×", !data[i][call1RoleIndex("狼")].equals("○") && table1.deadRankPlayer(table1.getPlayerList().get(i)) < aliveRank - 1);
				
			}
			//*/
		}
		
		int werewolfDisitionNum = getDisitionNum(data, Role.werewolf);
		int werewolfDisitionMaxRank = getDisitionMaxRank(data, Role.werewolf);
		List<Integer> werewolfDisitionList = getDisitionRankList(data, Role.werewolf);
		int FoxspiritMinRank = getMinRank(data, Role.foxspirit);
		
		//確定していない人狼の数が1の場合
		if(werewolfDisitionNum == table1.getRoleCom().getRole()[5] - 1) {
			//人狼が確定しているプレイヤーがすべて死亡している場合
			if(werewolfDisitionMaxRank < aliveRank - 1) {
				for(player pl : table1.getPlayerList()) {
					InsertData(data, pl, Role.werewolf, "×", table1.deadRankPlayer(pl) < aliveRank - 1 && !stat(data, pl, Role.werewolf, "○"));
				}
			}
			for(int i = 1; i < aliveRank; i++) {
				//生存人数
				int aliveNum = countRank(deadList, i);
				//確定人狼の生存数
				int werewolfDisition = countRank(werewolfDisitionList, i);
				//妖狐確定生存の場合
				if(i <= FoxspiritMinRank && callRoleNum(Role.foxspirit) > 0) {
					if(2 * (werewolfDisition + 1) + 1 >= aliveNum) {
						for(player pl : table1.getPlayerList()) {
							InsertData(data, pl, Role.werewolf, "×", table1.deadRankPlayer(pl) >= i && !stat(data, pl, Role.werewolf, "○"));
						}
					}
				}
				else {
					if(2 * (werewolfDisition + 1) >= aliveNum) {
						for(player pl : table1.getPlayerList()) {
							InsertData(data, pl, Role.werewolf, "×", table1.deadRankPlayer(pl) >= i && !stat(data, pl, Role.werewolf, "○"));
						}
					}
				}
			}
		}
		
		// 整理段階の前の日まで
		for(int i = 1; i < aliveRank; i++) {
			//生存人数
			int aliveNum = countRank(deadList, i);
			//確定人狼の生存数
			int werewolfDisition = countRank(werewolfDisitionList, i);
			// 確定で1人は生存
			if(werewolfDisition == 0) {
				werewolfDisition = 1;
			}
			// 確定人狼の人数がそれ以外の数より1小さい場合
			if(aliveNum == 2 * werewolfDisition + 1) {
				// 生存者は妖狐ではない (妖狐なら飽和している)
				if(callRoleNum(Role.foxspirit) > 0) {
					for(player pl : table1.getPlayerList()) {
						InsertData(data, pl, Role.foxspirit, "×", table1.deadRankPlayer(pl) >= i);
					}
				}
			}
		}
		
		return data;
	}
	
	/** <div lang="ja"><big>妖狐死亡かつ背徳者生存の候補の削除</big></div><br>
	 * 各日数について妖狐が死亡していて背徳者が生存している組み合わせを削除します。<br>
	 * @param data 全体データ
	 * @return data
	 */
	
	//生存者のうち妖狐死亡かつ背徳者生存の削除
	final String[][] remFoxspiritDeadImmoralistAlive(String[][] data) {
		//背徳者がいない場合は即returnする。
		if(callRoleNum(Role.immoralist) == 0) {
			return data;
		}
		//List<Integer> deadList = table1.deadRank();
		
		/*
		for(int i = 0; i < deadList.size(); i++) {
			System.out.println(table1.getPlayerList().get(i).getName() + " : " + deadList.get(i));
		}
		*/
		
		// 妖狐が否定されてないプレイヤーのうち最大のdeadRank値を求める
		int FoxspiritMaxRank = getMaxRank(data, Role.foxspirit);
		
		// 背徳者が1人以上確定で生存している時点での生存者のうち最小のdeadRank値を求める
		// ます、背徳者が否定されてないプレイヤーのうち (背徳者の配役人数)番目に小さい deadRank値を求める
		int ImmoralistMinRank = getMinRank(data, Role.immoralist, callRoleNum(Role.immoralist));
		int ImmoralistDisitionMax = getDisitionMaxRank(data, Role.immoralist);
		// もし、背徳者が確定しているプレイヤーがいた場合はその確定したプレイヤーのうち最大のdeadRank値になる
		if(ImmoralistDisitionMax > ImmoralistMinRank) {
			ImmoralistMinRank = ImmoralistDisitionMax;
		}
		
		// ImmoralistMinRank値未満のプレイヤーのうち背徳者が否定されてないプレイヤーの人数を記録
		int ImmoralistUnderCount = 0;
		for(int i = 0; i < ImmoralistMinRank; i++) {
			ImmoralistUnderCount += table1.sameRankSelect(i, Role.immoralist, data);
		}
		//System.out.println(ImmoralistUnderCount + ", " + ImmoralistMinRank);
		
		int maxCount = table1.sameRankSelect(FoxspiritMaxRank, Role.foxspirit, data);
		//System.out.println("--------+" + maxCount);
		int minCount = table1.sameRankSelect(ImmoralistMinRank, Role.immoralist, data);
		//System.out.println("--------*" + minCount);
		
		
		for(player pl : table1.getPlayerList()) {
			
			//背徳者が1人以上確定生存している場合、そのときの死亡者は妖狐ではない
			InsertData(data, pl, Role.foxspirit, "×", table1.deadRankPlayer(pl) < ImmoralistMinRank);
			
			//妖狐が確定で死亡している場合、そのときの生存者は背徳者ではない
			InsertData(data, pl, Role.immoralist, "×", table1.deadRankPlayer(pl) > FoxspiritMaxRank);
			
			if(minCount == callRoleNum(Role.immoralist) - ImmoralistUnderCount && table1.deadRankPlayer(pl) == ImmoralistMinRank) {
				InsertData(data, pl, Role.foxspirit, "×", !stat(data, pl, Role.immoralist, "×"));
			}
			if(maxCount == callRoleNum(Role.foxspirit) && table1.deadRankPlayer(pl) == FoxspiritMaxRank) {
				InsertData(data, pl, Role.immoralist, "×", !stat(data, pl, Role.foxspirit, "×"));
			}
		}
		
		return data;
	}
	
	int sa = 0;
	
	/** <div lang="ja"><big>犠牲者が2人以上出た場合の処理</big></div><br>
	 * 
	 * @param data 全体データ
	 * @return data
	 * @see
	 * 犠牲者が2人以上出た日が2回あった場合、片方は呪殺でもう片方は埋毒者噛みが確定しているので犠牲者が2人以上出た日の犠牲者以外の <b>妖狐</b> と <b>埋毒者</b> の候補を削除します。<br>
	 * 犠牲者が2人以上いて埋毒者が配役にいない場合、その日の犠牲者以外の <b>妖狐</b> の候補を削除します。<br>
	 * 犠牲者が3人以上いる場合、その日の犠牲者以外の <b>妖狐</b> の候補を削除します。<br>
	 * 犠牲者が <b>(妖狐+背徳者)の配役人数 + 2</b> 人の場合、その日の犠牲者以外の <b>妖狐</b> と <b>背徳者</b>  と <b>埋毒者</b> の候補を削除します。<br>
	 * 犠牲者が2人以上いて犠牲者のうち1人が妖狐、背徳者、人狼、埋毒者のすべてが否定されていたら、その日の犠牲者すべての <b>埋毒者</b> の候補を削除します。<br>
	 */
	//複数死体でた場合の処理
	final String[][] more2Victims(String[][] data) {
		//createTable tab = new createTable(table1.getPlayerList(), table1.getRoleCom());
		//System.out.println("more2---top");
		int more2day = -1;
		
		for(int i = 0; i < table1.getVictims().size(); i++) {
			//System.out.println("more2---loop_top");
			
			//もし2犠牲以上の日が2日あった場合
			if(more2day != -1 && table1.getVictims().get(i).size() > 1) {
				//System.out.println("*More2Times : " + more2day + ", " + i);
				table1.deleteSeerHide(2 * more2day - 1);
				for(player pl : table1.getPlayerList()) {
					//1回は埋毒者噛みで1回は呪殺なので、その2回の犠牲者に含まれない人は妖狐と埋毒者は否定
					if(!table1.getVictims().get(i).contains(pl) && !table1.getVictims().get(more2day).contains(pl)) {
						InsertData(data, pl, Role.foxspirit, "×", true);
						InsertData(data, pl, Role.toxic, "×", true);
						
					}
					disitionWerewolfContainVic(data, i, pl, true, more2day, true);
				}
			}
			
			//複数死体がでたときに埋毒者が居なかったら呪殺確定
			if(table1.getVictims().get(i).size() > 1 && callRoleNum(Role.toxic) == 0) {
				data = curseDisition(data, i);
				more2day = i;
				break;
			}
			//3犠牲以上の場合、呪殺確定
			else if(table1.getVictims().get(i).size() > 2) {
				data = curseDisition(data, i);
				table1.deleteSeerHide(2 * i - 1);
				more2day = i;
				boolean isToxicContainVic = false;
				for(player pl : table1.getPlayerList()) {
					if(table1.getVictims().get(i).contains(pl)) {
						if(!stat(data, pl, Role.toxic, "×")) {
							isToxicContainVic = true;
						}
					}
				}
				if(!isToxicContainVic) {
					for(player pl : table1.getPlayerList()) {
						disitionImmoralistContainVic(data, i, pl, true, -1, false);
					}
				}
				
			}
			//2犠牲の場合
			else if(table1.getVictims().get(i).size() == 2) {
				player vic1 = new player("", -1), vic2 = new player("", -1);
				for(player pl : table1.getPlayerList()) {
					if(table1.getVictims().get(i).get(0) == pl) {
						vic1 = pl;
					}
					if(table1.getVictims().get(i).get(1) == pl) {
						vic2 = pl;
					}
				}
				//犠牲者のうち1人が埋毒者と人狼の両方が否定できる場合や、犠牲者の両方が埋毒者か人狼が否定できる場合呪殺確定
				if((stat(data, vic1, Role.toxic, "×") && stat(data, vic2, Role.toxic, "×"))
					|| (stat(data, vic1, Role.werewolf, "×") && stat(data, vic2, Role.werewolf, "×"))
					|| (stat(data, vic1, Role.toxic, "×") && stat(data, vic1, Role.werewolf, "×"))
					|| (stat(data, vic2, Role.toxic, "×") && stat(data, vic2, Role.werewolf, "×"))) {
					data = curseDisition(data, i);
				}
				more2day = i;
			}
			//(妖狐+背徳者)の配役人数+2人の犠牲者がでたとき、その犠牲者の中には埋毒者が含まれる
			if(table1.getVictims().get(i).size() > 1 + callRoleNum(Role.foxspirit) + callRoleNum(Role.immoralist)) {
				for(player pl : table1.getPlayerList()) {
					if(!table1.getVictims().get(i).contains(pl)) {
						InsertData(data, pl, Role.toxic, "×", true);
					}
				}
			}
			
			//複数犠牲者のうち1人が妖狐、背徳者、人狼、埋毒者のすべてが否定できるとき、その犠牲者に埋毒者は含まれない
			//もし複数犠牲がでたときに犠牲者以外の埋毒者が否定できた場合、その犠牲者は人狼、埋毒者、妖狐、背徳者以外は否定
			boolean isToxicInVic = true, isNotToxicOutVic = true;
			if(table1.getVictims().get(i).size() > 1) {
				for(player pl : table1.getPlayerList()) {
					if(table1.getVictims().get(i).contains(pl)) {
						if(callRoleNum(Role.toxic) > 0 && callRoleNum(Role.immoralist) > 0) {
							if(stat(data, pl, Role.foxspirit, "×") && stat(data, pl, Role.immoralist, "×") && stat(data, pl, Role.werewolf, "×") && stat(data, pl, Role.toxic, "×")) {
								isToxicInVic = false;
							}
						}
					}
					else {
						if(callRoleNum(Role.toxic) > 0) {
							if(!stat(data, pl, Role.toxic, "×")) {
								isNotToxicOutVic = false;
							}
						}
					}
				}
				if(!isToxicInVic) {
					for(player pl : table1.getPlayerList()) {
						if(table1.getVictims().get(i).contains(pl)) {
							InsertData(data, pl, Role.toxic, "×", true);
						}
					}
				}
				if(isNotToxicOutVic) {
					for(player pl : table1.getPlayerList()) {
						if(table1.getVictims().get(i).contains(pl)) {
							Role[] leave = {Role.foxspirit, Role.immoralist, Role.werewolf, Role.toxic};
							NotInsertData(data, pl, leave, "×", true);
						}
					}
					for(player pl : table1.getPlayerList()) {
						disitionWerewolfContainVic(data, i, pl, true, -1, false);
					}
				}
			}
		}
		//sa++;
		return data;
	}
	
	/** <div lang="ja"><big>呪殺確定の処理</big></div><br>
	 * 呪殺が確定している場合の処理
	 * @param data 全体データ
	 * @param i 呪殺が起きた日数
	 * @return data
	 * @see
	 * 
	 */
	
	//* 呪殺確定処理
	final private String[][] curseDisition(String[][] data, int i) {
		///List<Integer> rank = table1.deadRank();
		//table1.deleteSeerHide(2 * i - 1);
		for(player pl : table1.getPlayerList()) {
			//複数死体の中に含まれてないプレイヤーについて
			if(!table1.getVictims().get(i).contains(pl)) {
				
				//そのプレイヤーは妖狐ではない
				InsertData(data, pl, Role.foxspirit, "×", true);
				
				//もし犠牲者数が(背徳者+埋毒者)配役数+2なら、そのプレイヤーは背徳者ではない
				InsertData(data, pl, Role.immoralist, "×", table1.getVictims().get(i).size() - 2 == callRoleNum(Role.immoralist) + callRoleNum(Role.toxic));
				
				boolean containToxic = false;
				for(player vic : table1.getPlayerList()) {
					if(table1.getVictims().get(i).contains(vic)) {
						if(callRoleNum(Role.toxic) > 0) {
							if(!stat(data, vic, Role.toxic, "×")) {
								containToxic = true;
								break;
							}
						}
					}
				}
				//もし呪殺された日の犠牲者に埋毒者が含まれていない場合
				if(!containToxic) {
					//犠牲者数が背徳者配役数+2なら、そのプレイヤーは背徳者ではない
					InsertData(data, pl, Role.immoralist, "×", table1.getVictims().get(i).size() - 2 == callRoleNum(Role.immoralist));
				}
			}
			//呪殺での複数死体が出たときの生存者は背徳者ではない
			InsertData(data, pl, Role.immoralist, "×", table1.deadRankPlayer(pl) > 2 * i - 1);
			InsertData(data, pl, Role.seer, "×", table1.deadRankPlayer(pl) < 2 * i - 1);
		}
		return data;
	}
	
	/** <div lang="ja"><big>犠牲者がいない場合の処理</big></div><br>
	 * この処理は噛みなし禁止の場合のみ実行されます。<br>
	 * 噛みなし禁止の場合、犠牲者がいなかった日の時点で確定で妖狐か狩人が生存しています。
	 * 犠牲者がいなかった日の生存者のうち、(妖狐か狩人)の可能性のあるプレイヤーが1人しかいなかったらそのプレイヤーは妖狐か狩人であることが確定します。<br>
	 * @param data 全体データ
	 * @return data
	 * @see
	 */
	
	//犠牲者なしの日は狩人か妖狐が生存(噛みなし禁止のときのみ)
	final String[][] noVictim(String[][] data) {
		//噛みなしありなら即returnする
		if(table1.getNoAttack()) {
			return data;
		}
		
		//List<Integer> deadList = table1.deadRank();
		int FoxspiritMaxRank = getMaxRank(data, Role.foxspirit);
		int BodyguardMaxRank = getMaxRank(data, Role.bodyguard);
		
		for(int i = 2; i < table1.getVictims().size(); i++) {
			//犠牲者なしのとき
			if(table1.getVictims().get(i).get(0).getId() == -1) {
				//もしその日時点で妖狐が確定死亡、または妖狐がいない場合
				if(FoxspiritMaxRank < 2 * i - 1 || callRoleNum(Role.foxspirit) == 0) {
					for(player pl : table1.getPlayerList()) {
						//System.out.println(FoxspiritMaxRank + "++++");
						//その日までの死亡者は狩人ではない
						InsertData(data, pl, Role.bodyguard, "×", table1.deadRankPlayer(pl) < 2 * i - 1);
					}
				}
				//もしその日時点で狩人が確定死亡、または狩人がいない場合
				if(BodyguardMaxRank < 2 * i - 1 || callRoleNum(Role.bodyguard) == 0) {
					for(player pl : table1.getPlayerList()) {
						//その日までの死亡者は妖狐ではない
						InsertData(data, pl, Role.foxspirit, "×", table1.deadRankPlayer(pl) < 2 * i - 1);
					}
				}
				
				List<player> Fox_BodyguardList = new ArrayList<>();
				for(player pl : table1.getPlayerList()) {
					if(table1.deadRankPlayer(pl) > 2 * i - 1) {
						if(!stat(data, pl, Role.foxspirit, "×") || !stat(data, pl, Role.bodyguard, "×")) {
							Fox_BodyguardList.add(pl);
						}
					}
				}
				//もし、犠牲者なしの日の生存者のうち狩人または妖狐であるプレイヤーが1人しかいない場合
				if(Fox_BodyguardList.size() == 1) {
					//そのプレイヤーは妖狐か狩人
					Role[] leave = {Role.foxspirit, Role.bodyguard};
					NotInsertData(data, Fox_BodyguardList.get(0), leave, "×", true);
				}
			}
		}
		
		return data;
	}
	
	/** <div lang="ja"><big>全人外露出の処理</big></div><br>
	 * <b>騙り人外数+潜伏している確定人外数</b> が人外の配役人数の場合、潜伏している人外が確定していないプレイヤーの村人陣営が確定します。<br>
	 * @param data 全体データ
	 * @param isHide 確定で潜伏で乗っ取られている役職があるか？
	 * @return data
	 */
	
	// 全人外露出の処理
	final String[][] allNonVillagerSideCO(String[][] data, boolean isHide) {
		
		// すべてのCO数の合計
		int totalCO = table1.getSeerCOList().size() + table1.getMediumCOList().size() + table1.getBodyguardCOList().size() + table1.getToxicCOList().size();
		
		// 人外数
		int nonVillagerSide = callRoleNum(Role.werewolf) + callRoleNum(Role.fanatic) + callRoleNum(Role.foxspirit) + callRoleNum(Role.immoralist);
		
		// 騙り数
		int pretend = totalCO;
		if(table1.getSeerCOList().size() > 0) {
			pretend = pretend - callRoleNum(Role.seer);
		}
		if(table1.getMediumCOList().size() > 0) {
			pretend = pretend - callRoleNum(Role.medium);
		}
		if(table1.getBodyguardCOList().size() > 0) {
			pretend = pretend - callRoleNum(Role.bodyguard);
		}
		if(table1.getToxicCOList().size() > 0) {
			pretend = pretend - callRoleNum(Role.toxic);
		}
		if(isHide) {
			pretend++;
		}
		
		// 非COプレイヤー
		List<player> nonCO = getNonCOPlayers();
		List<player> nonCONonVillagerSide = new ArrayList<>();
		
		//潜伏人外のプレイヤーを格納
		for(player pl : table1.getPlayerList()) {
			if(nonCO.contains(pl)) {
				if(isDisitionNonVillagerSide(data, pl)) {
					nonCONonVillagerSide.add(pl);
				}
			}
		}
		
		// 全人外露出のとき、潜伏している非確定人外は確定で村人陣営、乗っ取りもない
		if(pretend + nonCONonVillagerSide.size() == nonVillagerSide) {
			for(player pl : table1.getPlayerList()) {
				if(nonCO.contains(pl) && !nonCONonVillagerSide.contains(pl)) {
					InsertData(data, pl, Role.werewolf, "×", true);
					InsertData(data, pl, Role.fanatic, "×", true);
					InsertData(data, pl, Role.foxspirit, "×", true);
					InsertData(data, pl, Role.immoralist, "×", true);
					InsertData(data, pl, Role.seer, "×", stat(data, pl, Role.seer, "△"));
					InsertData(data, pl, Role.medium, "×", stat(data, pl, Role.medium ,"△"));
					InsertData(data, pl, Role.bodyguard, "×", stat(data, pl, Role.bodyguard, "△"));
					InsertData(data, pl, Role.toxic, "×", stat(data, pl, Role.toxic, "△"));
				}
			}
		}
		
		// 確定人外数
		
		int nonVillagerSideDisition = 0;
		List<player> nonVillagerSideDisitionList = new ArrayList<>();
		
		for(player pl : table1.getPlayerList()) {
			if(isDisitionNonVillagerSide(data, pl)) {
				nonVillagerSideDisition++;
				nonVillagerSideDisitionList.add(pl);
			}
		}
		/*
		for(int i = 0; i < table1.getPlayerList().size(); i++) {
			if(data[i][call1RoleIndex("狩")].equals("○") && !isBankruptcy(data)) {
				System.out.println(table1.getPlayerList().get(i).getName() + ">>>" + nonVillagerSideDisition);
				for(player pl : nonVillagerSideDisitionList) {
					System.out.println(pl.getName());
				}
				System.out.println();
			}
		}
		//*/
		//System.out.println(nonVillagerSideDisition + ", " + nonVillagerSide);
		if(nonVillagerSideDisition >= nonVillagerSide) {
			for(player pl : table1.getPlayerList()) {
				if(!nonVillagerSideDisitionList.contains(pl)) {
					//System.out.println("Disition VillagerSide : " + table1.getPlayerList().get(i).getName());
					InsertData(data, pl, Role.werewolf, "×", true);
					InsertData(data, pl, Role.fanatic, "×", true);
					InsertData(data, pl, Role.foxspirit, "×", true);
					InsertData(data, pl, Role.immoralist, "×", true);
				}
			}
		}
		
		return data;
	}
	
	/** COしてないプレイヤーのリスト
	 * 
	 * @return
	 */
	
	//COしてないプレイヤーの一覧
	final private List<player> getNonCOPlayers() {
		List<player> nonCOPlayer = new ArrayList<>();
		for(player pl : table1.getPlayerList()) {
			boolean isCO = false;
			for(seerCO seer : table1.getSeerCOList()) {
				if(seer.getSeerCOpl() == pl) {
					isCO = true;
				}
			}
			for(mediumCO medium : table1.getMediumCOList()) {
				if(medium.getMediumCOpl() == pl) {
					isCO = true;
				}
			}
			for(bodyguardCO bodyguard : table1.getBodyguardCOList()) {
				if(bodyguard.getBodyguardCOpl() == pl) {
					isCO = true;
				}
			}
			for(toxicCO toxic : table1.getToxicCOList()) {
				if(toxic.getToxicCOpl() == pl) {
					isCO = true;
				}
			}
			if(table1.getFreemasonCOList().getFreemasonCOpl().contains(pl)) {
				isCO = true;
			}
			if(!isCO) {
				nonCOPlayer.add(pl);
			}
		}
		return nonCOPlayer;
	}
	
	/** <div lang="ja"><big>占い師と霊能者の結果の相互判定</big></div><br>
	 * 各占い師と各霊能者の結果を組み合わせて矛盾があるかどうかを判断して、矛盾があった場合は互いに相手の人外が確定します。<br>
	 * また占い師視点で霊能者プレイヤーが、霊能者視点で占い師プレイヤーが確定した場合、その確定プレイヤーの結果を自身のデータに反映させます。<br>
	 * @param seerData 占い師視点の個別データのリスト
	 * @param mediumData 霊能者視点の個別データのリスト
	 */
	
	//各占い師視点、霊能者視点でそれぞれ結果があっているか？
	final void crossSeerMedium(List<String[][]> seerData, List<String[][]> mediumData) {
		//createTable tab = new createTable(table1.getPlayerList(), table1.getRoleCom());
		
		for(String[][] seer : seerData) {
			for(String[][] medium : mediumData) {
				/*
				System.out.println("------");
				tab.printTable(seer);
				tab.printTable(medium);
				System.out.println("++++++" + is2Possible(seer, medium));
				//*/
				//選択した占い師と霊能者の結果が矛盾している場合
				if(!is2Possible(seer, medium)) {
					//System.out.println("******");
					for(player pl : table1.getPlayerList()) {
						InsertData(medium, pl, Role.seer, "×", stat(seer, pl, Role.seer, "○"));
						InsertData(seer, pl, Role.medium, "×", stat(medium, pl, Role.medium, "○"));
					}
					//tab.printTable(seer);
				}
			}
		}
		
		for(String[][] seer : seerData) {
			for(String[][] medium : mediumData) {
				//選択した占い師と霊能者の結果が矛盾している場合
				if(!is2Possible(seer, medium)) {
					for(player pl : table1.getPlayerList()) {
						//System.out.println("====");
						//tab.printTable(seer);
						if(stat(seer, pl, Role.seer, "○")) {
							seer = totalSort(seer);
						}
						if(stat(medium, pl, Role.medium, "○")) {
							medium = totalSort(medium);
						}
					}
				}
			}
		}
		
		for(String[][] seer : seerData) {
			for(player pl : table1.getPlayerList()) {
				if(stat(seer, pl, Role.seer, "○")) {
					// 占い師視点で霊能者が確定している場合
					for(player tar : table1.getPlayerList()) {
						if(stat(seer, tar, Role.medium, "○")) {
							//System.out.println("***" + table1.getPlayerList().get(i).getName() + "->" + table1.getPlayerList().get(j).getName());
							for(String[][] medium : mediumData) {
								// 霊能者視点の情報が占い師にも通用する
								if(stat(medium, tar, Role.medium, "○")) {
									InsertCopyData(seer, medium);
									break;
								}
							}
						}
					}
					break;
				}
			}
		}
		
		for(String[][] medium : mediumData) {
			for(player pl : table1.getPlayerList()) {
				if(stat(medium, pl, Role.medium, "○")) {
					// 霊能者視点で占い師が確定している場合
					for(player tar : table1.getPlayerList()) {
						if(stat(medium, tar, Role.seer, "○")) {
							//System.out.println("***" + table1.getPlayerList().get(i).getName() + "->" + table1.getPlayerList().get(j).getName());
							for(String[][] seer : seerData) {
								// 占い師視点の情報が霊能者にも通用する
								if(stat(seer, tar, Role.seer, "○")) {
									InsertCopyData(medium, seer);
									break;
								}
							}
						}
					}
					break;
				}
			}
		}
		
		for(String[][] seer : seerData) {
			seer = totalSort(seer);
		}
		for(String[][] medium : mediumData) {
			medium = totalSort(medium);
		}
		
	}
	
	/** <div lang="ja"><big>占い師と狩人の結果の相互判定</big></div><br>
	 * 各占い師と各狩人の結果を組み合わせて矛盾があるかどうかを判断して、矛盾があった場合は互いに相手の人外が確定します。<br>
	 * また占い師視点で狩人プレイヤーが、狩人視点で占い師プレイヤーが確定した場合、その確定プレイヤーの結果を自身のデータに反映させます。<br>
	 * @param seerData 占い師視点の個別データのリスト
	 * @param bodyguardData 狩人視点の個別データのリスト
	 */
	
	//各占い師視点、狩人視点でそれぞれ結果があっているか？
	final void crossSeerBodyguard(List<String[][]> seerData, List<String[][]> bodyguardData) {
		for(String[][] seer : seerData) {
			for(String[][] bodyguard : bodyguardData) {
				//選択した占い師と狩人の結果が矛盾している場合
				if(!is2Possible(seer, bodyguard)) {
					for(player pl : table1.getPlayerList()) {
						InsertData(bodyguard, pl, Role.seer, "×", stat(seer, pl, Role.seer, "○"));
						InsertData(seer, pl, Role.bodyguard, "×", stat(bodyguard, pl, Role.bodyguard, "○"));
					}
				}
			}
		}
		
		for(String[][] seer : seerData) {
			for(String[][] bodyguard : bodyguardData) {
				//選択した占い師と狩人の結果が矛盾している場合
				if(!is2Possible(seer, bodyguard)) {
					for(player pl : table1.getPlayerList()) {
						if(stat(seer, pl, Role.seer, "○")) {
							seer = totalSort(seer);
						}
						if(stat(bodyguard, pl, Role.bodyguard, "○")) {
							bodyguard = totalSort(bodyguard);
						}
					}
				}
			}
		}
		
		for(String[][] seer : seerData) {
			for(player pl : table1.getPlayerList()) {
				if(stat(seer, pl, Role.seer, "○")) {
					// 占い師視点で狩人が確定している場合
					for(player tar : table1.getPlayerList()) {
						if(stat(seer, tar, Role.bodyguard, "○")) {
							for(String[][] bodyguard : bodyguardData) {
								// 狩人視点の情報が占い師にも通用する
								if(stat(bodyguard, tar, Role.bodyguard, "○")) {
									InsertCopyData(seer, bodyguard);
									break;
								}
							}
						}
					}
					break;
				}
			}
		}
		
		for(String[][] bodyguard : bodyguardData) {
			for(player pl : table1.getPlayerList()) {
				if(stat(bodyguard, pl, Role.bodyguard, "○")) {
					// 狩人視点で占い師が確定している場合
					for(player tar : table1.getPlayerList()) {
						if(stat(bodyguard, tar, Role.seer, "○")) {
							//System.out.println("***" + table1.getPlayerList().get(i).getName() + "->" + table1.getPlayerList().get(j).getName());
							for(String[][] seer : seerData) {
								// 占い師視点の情報が狩人にも通用する
								if(stat(seer, tar, Role.seer, "○")) {
									InsertCopyData(bodyguard, seer);
									break;
								}
							}
						}
					}
					break;
				}
			}
		}
		
		for(String[][] seer : seerData) {
			seer = totalSort(seer);
		}
		for(String[][] bodyguard : bodyguardData) {
			bodyguard = totalSort(bodyguard);
		}
		
	}
	
	/** <div lang="ja"><big>霊能者と狩人の結果の相互判定</big></div><br>
	 * 各霊能者と各狩人の結果を組み合わせて矛盾があるかどうかを判断して、矛盾があった場合は互いに相手の人外が確定します。<br>
	 * また霊能者視点で狩人プレイヤーが、狩人視点で霊能者プレイヤーが確定した場合、その確定プレイヤーの結果を自身のデータに反映させます。<br>
	 * @param mediumData 霊能者視点の個別データのリスト
	 * @param bodyguardData 狩人視点の個別データのリスト
	 */
	
	//各霊能者視点、狩人視点でそれぞれ結果があっているか？
	final void crossMediumBodyguard(List<String[][]> mediumData, List<String[][]> bodyguardData) {
		for(String[][] medium : mediumData) {
			for(String[][] bodyguard : bodyguardData) {
				//選択した霊能者と狩人の結果が矛盾している場合
				if(!is2Possible(medium, bodyguard)) {
					for(player pl : table1.getPlayerList()) {
						InsertData(bodyguard, pl, Role.medium, "×", stat(medium, pl, Role.medium, "○"));
						InsertData(medium, pl, Role.bodyguard, "×", stat(bodyguard, pl, Role.bodyguard, "○"));
					}
				}
			}
		}
		for(String[][] medium : mediumData) {
			for(String[][] bodyguard : bodyguardData) {
				//選択した霊能者と狩人の結果が矛盾している場合
				if(!is2Possible(medium, bodyguard)) {
					for(player pl : table1.getPlayerList()) {
						if(stat(medium, pl, Role.medium, "○")) {
							medium = totalSort(medium);
						}
						if(stat(bodyguard, pl, Role.bodyguard, "○")) {
							bodyguard = totalSort(bodyguard);
						}
					}
				}
			}
		}
		
		for(String[][] medium : mediumData) {
			//createTable tab = new createTable(table1.getPlayerList(), table1.getRoleCom());
			
			for(player pl : table1.getPlayerList()) {
				if(stat(medium, pl, Role.medium, "○")) {
					// 霊能者視点で狩人が確定している場合
					for(player tar : table1.getPlayerList()) {
						if(stat(medium, tar, Role.bodyguard, "○")) {
							for(String[][] bodyguard : bodyguardData) {
								// 狩人視点の情報が霊能者にも通用する
								if(stat(bodyguard, tar, Role.bodyguard, "○")) {
									//System.out.println("霊 : " + table1.getPlayerList().get(i).getName() + ", 狩 : " + table1.getPlayerList().get(j).getName());
									//tab.printTable(bodyguard);
									InsertCopyData(medium, bodyguard);
									break;
								}
							}
						}
					}
					break;
				}
			}
		}
		/*
		for(String[][] medium : mediumData) {
			createTable tab = new createTable(table1.getPlayerList(), table1.getRoleCom());
			
			for(int i = 0; i < table1.getPlayerList().size(); i++) {
				if(medium[i][call1RoleIndex("霊")].equals("○")) {
					System.out.println("===霊 : " + table1.getPlayerList().get(i).getName());
					tab.printTable(medium);
				}
			}
		}
		//*/
		
		for(String[][] bodyguard : bodyguardData) {
			for(player pl : table1.getPlayerList()) {
				if(stat(bodyguard, pl, Role.bodyguard, "○")) {
					// 狩人視点で霊能者が確定している場合
					for(player tar : table1.getPlayerList()) {
						if(stat(bodyguard, tar, Role.medium, "○")) {
							//System.out.println("***" + table1.getPlayerList().get(i).getName() + "->" + table1.getPlayerList().get(j).getName());
							for(String[][] medium : mediumData) {
								// 霊能者視点の情報が狩人にも通用する
								if(stat(medium, tar, Role.medium, "○")) {
									InsertCopyData(bodyguard, medium);
									break;
								}
							}
						}
					}
					break;
				}
			}
		}
		
		for(String[][] medium : mediumData) {
			medium = totalSort(medium);
		}
		for(String[][] bodyguard : bodyguardData) {
			bodyguard = totalSort(bodyguard);
		}
	}
	
	/** <div lang="ja"><big>データが破綻しているか？</big></div><br>
	 * 各プレイヤーについて該当役職が存在しない場合、各役職について該当プレイヤーが配役人数分存在しない場合、確定判定が2重になっている場合は破綻していると判定します。<br>
	 * @param data
	 * @return
	 */
	
	//破綻しているか？
	final boolean isBankruptcy(String[][] data) {
		boolean noRole = false;
		boolean noPlayer = false;
		//縦がすべて×のとき
		for(int i = 0; i < table1.getRoleCom().getTotal(); i++) {
			noRole = true;
			for(int j = 0; j < table1.getPlayerList().size(); j++) {
				if(!data[j][i].equals("×")) {
					noRole = false;
					break;
				}
			}
			if(noRole) {
				//System.out.println("縦がすべて× : " + table1.getRoleCom().getRoleLabel().get(i));
				return true;
			}
		}
		//横がすべて×のとき
		for(int i = 0; i < table1.getPlayerList().size(); i++) {
			noPlayer = true;
			for(int j = 0; j < table1.getRoleCom().getTotal(); j++) {
				if(!data[i][j].equals("×")) {
					noPlayer = false;
					break;
				}
			}
			if(noPlayer) {
				//System.out.println("横がすべて× : " + table1.getPlayerList().get(i).getName());
				return true;
			}
		}
		//1プレイヤーに複数役職○がついているとき
		for(int i = 0; i < table1.getPlayerList().size(); i++) {
			boolean disiRole = false;
			String strRole = "";
			for(int j = 0; j < table1.getRoleCom().getTotal(); j++) {
				if(data[i][j].equals("○")) {
					if(disiRole && !strRole.equals(table1.getRoleCom().getRoleLabel().get(j))) {
						//System.out.println("複数役職○ : " + table1.getPlayerList().get(i).getName());
						return true;
					}
					else {
						disiRole = true;
						strRole = table1.getRoleCom().getRoleLabel().get(j);
					}
				}
			}
		}
		//String[] role = {"村", "占", "霊", "狩", "共", "狼", "狂", "狐", "背"};
		String strRole = "村";
		int numRole = 0;
		for(int i = 0; i < table1.getRoleCom().getTotal(); i++) {
			if(!strRole.equals(table1.getRoleCom().getRoleLabel().get(i))) {
				strRole = table1.getRoleCom().getRoleLabel().get(i);
				numRole++;
				while(table1.getRoleCom().getRole()[numRole] == 0) {
					numRole++;
				}
			}
			//System.out.print(role[numRole] + " ");
			int disiCount = 0;
			int notCount = 0;
			if(table1.getRoleCom().getRole()[numRole] > 0) {
				for(int j = 0; j < table1.getPlayerList().size(); j++) {
					if(data[j][i].equals("○")) {
						disiCount++;
					}
					if(data[j][i].equals("×")) {
						notCount++;
					}
					if(disiCount > table1.getRoleCom().getRole()[numRole]) {
						//System.out.println("overplayer:" + disiCount + ", numrole:" + numRole);
						return true;
					}
					if(notCount > table1.getPlayerList().size() - table1.getRoleCom().getRole()[numRole]) {
						//System.out.println(table1.getRoleCom().getRoleLabel().get(i) + "," + disiCount + "," + notCount + "," + table1.getRoleCom().getRole()[numRole]);
						//return true;
					}
				}
			}
			//System.out.println(table1.getRoleCom().getRoleLabel().get(i) + "," + disiCount + "," + notCount);
		}
		
		
		List<Integer> deadList = table1.deadRank();
		int aliveRank = getAliveRank();
		
		int werewolfDisitionNum = getDisitionNum(data, Role.werewolf), werewolfDisitionMaxRank = getDisitionMaxRank(data, Role.werewolf);
		List<Integer> werewolfDisitionList = getDisitionRankList(data, Role.werewolf);
		
		int FoxspiritMinRank = getMinRank(data, Role.foxspirit);
		
		if(werewolfDisitionNum == callRoleNum(Role.werewolf)) {
			//System.out.println(werewolfDisitionNum);
			if(werewolfDisitionMaxRank < aliveRank - 1) {
				for(player pl : table1.getPlayerList()) {
					InsertData(data, pl, Role.werewolf, "×", table1.deadRankPlayer(pl) < aliveRank - 1 && !stat(data, pl, Role.werewolf, "○"));
				}
				//System.out.println("true1");
				return true;
			}
			
			for(int i = 1; i < aliveRank; i++) {
				int aliveNum = countRank(deadList, i);
				int werewolfDisition = countRank(werewolfDisitionList, i);
				if(i <= FoxspiritMinRank && callRoleNum(Role.foxspirit) > 0) {
					if(2 * werewolfDisition + 1 >= aliveNum) {
						for(player pl : table1.getPlayerList()) {
							InsertData(data, pl, Role.werewolf, "×", table1.deadRankPlayer(pl) >= i && !stat(data, pl, Role.werewolf, "○"));
						}
						//System.out.println("true2");
						return true;
					}
				}
				else {
					if(2 * werewolfDisition >= aliveNum) {
						for(player pl : table1.getPlayerList()) {
							InsertData(data, pl, Role.werewolf, "×", table1.deadRankPlayer(pl) >= i && !stat(data, pl, Role.werewolf, "○"));
						}
						//System.out.println("true3");
						return true;
					}
				}
			}
		}
		
		return false;
	}
	
	/** <div lang="ja"><big>各役職の個別データの統合</big></div><br>
	 * 各役職視点での個別データを比較してすべての役職自称者視点で確定する情報を全体データに反映させます。<br>
	 * またその役職の該当プレイヤーが確定している場合、そのプレイヤー視点のデータを全体データに反映させます。<br>
	 * @param data 全体データ
	 * @param posData 役職視点の個別データのリスト
	 * @param datatype 役職
	 * @return data
	 */
	
	//まとまったデータの結合 (または)
	final String[][] combineData(String[][] data, List<String[][]> posData, String datatype) {
		//System.out.println();
		//System.out.println("データを結合");
		//System.out.println("****data****   " + posData.size());
		data = tmptotal;
		//createTable tab = new createTable(table1.getPlayerList(), table1.getRoleCom());
		//System.out.println("****data****");
		//tab.printTable(data);
		List<String[][]> possible = new ArrayList<>();
		for(int i = 0; i < posData.size(); i++) {
			//System.out.println("****data****" + i + "   " + posData.size() + ", " + is2Possible(data, posData.get(i)));
			//tab.printTable(posData.get(i));
			if(is2Possible(data, posData.get(i))) {
				//System.out.println(i);
				possible.add(posData.get(i));
			}
			else {
				for(int j = 0; j < table1.getPlayerList().size(); j++) {
					for(int k = 0; k < table1.getRoleCom().getTotal(); k++) {
						if(table1.getRoleCom().getRoleLabel().get(k).equals(datatype.substring(0,1))) {
							
							if(datatype.substring(0,1).equals("占")) {
								//System.out.println(table1.getSeerCOList().get(i).getSeerCOpl().getName());
								//System.out.println(table1.getPlayerList().get(j).getName());
								if(i < table1.getSeerCOList().size()) {
									if(table1.getSeerCOList().get(i).getSeerCOpl() == table1.getPlayerList().get(j)) {
										data[j][k] = "×";
									}
								}
							}
							else if(datatype.substring(0,1).equals("霊")) {
								if(i < table1.getMediumCOList().size()) {
									if(table1.getMediumCOList().get(i).getMediumCOpl() == table1.getPlayerList().get(j)) {
										data[j][k] = "×";
									}
								}
							}
							else if(datatype.substring(0,1).equals("狩")) {
								if(i < table1.getBodyguardCOList().size()) {
									if(table1.getBodyguardCOList().get(i).getBodyguardCOpl() == table1.getPlayerList().get(j)) {
										data[j][k] = "×";
									}
								}
							}
						}
					}
					//System.out.println(datatype.substring(0,1) + j);
				}
			}
		}
		posData = possible;
		/*
		System.out.println("****data2****");
		tab.printTable(data);
		System.out.println("---------");
		for(int k = 0; k < posData.size(); k++) {
			tab.printTable(posData.get(k));
		}
		System.out.println("---------");
		//*/
		
		for(int i = 0; i < table1.getPlayerList().size(); i++) {
			for(int j = 0; j < table1.getRoleCom().getTotal(); j++) {
				if(!data[i][j].equals("●")) {
					boolean noExist = true;
					boolean Disition = true;
					for(int k = 0; k < posData.size(); k++) {
						if(!posData.get(k)[i][j].equals("×")) {
							noExist = false;
						}
						if(!posData.get(k)[i][j].equals("○")) {
							Disition = false;
						}
					}
					if(i == 0) {
						//System.out.println("combine " + j);
						//tab.printTable(data);
					}
					//System.out.println(i + ", noExist:" + noExist + ", Disition:" + Disition);
					if(noExist) {
						data[i][j] = "×";
						/*
						if(datatype.equals("占い師")) {
							System.out.println("× ---(" + i + ", " + j + ")");
						}
						//*/
					}
					else if(Disition) {
						data[i][j] = "○";
					}
				}
			}
		}
		//System.out.println("combine" + datatype);
		//tab.printTable(data);
		
		//対象役職が1確しているなら、全体データに1確役職者の情報を反映
		for(player pl : table1.getPlayerList()) {
			if(data[callPlTabIndex(pl)][call1RoleIndex(roles.getRoletype(datatype))].equals("○")) {
				for(int i = 0; i < posData.size(); i++) {
					if(posData.get(i)[callPlTabIndex(pl)][call1RoleIndex(roles.getRoletype(datatype))].equals("○")) {
						InsertCopyData(data, posData.get(i));
					}
				}
			}
		}
		data = totalSort(data);
		return data;
	}
	final String[][] combineData(String[][] data, List<String[][]> posData, Role datatype) {
		//System.out.println();
		//System.out.println("データを結合");
		//System.out.println("****data****   " + posData.size());
		data = tmptotal;
		//createTable tab = new createTable(table1.getPlayerList(), table1.getRoleCom());
		//System.out.println("****data****");
		//tab.printTable(data);
		List<String[][]> possible = new ArrayList<>();
		for(int i = 0; i < posData.size(); i++) {
			//System.out.println("****data****" + i + "   " + posData.size() + ", " + is2Possible(data, posData.get(i)));
			//tab.printTable(posData.get(i));
			if(is2Possible(data, posData.get(i))) {
				//System.out.println(i);
				possible.add(posData.get(i));
			}
			else {
				for(int j = 0; j < table1.getPlayerList().size(); j++) {
					for(int k = 0; k < table1.getRoleCom().getTotal(); k++) {
						if(table1.getRoleCom().getRoleLabel().get(k).equals(roles.roleName(datatype, false))) {
							
							if(datatype == Role.seer) {
								//System.out.println(table1.getSeerCOList().get(i).getSeerCOpl().getName());
								//System.out.println(table1.getPlayerList().get(j).getName());
								if(i < table1.getSeerCOList().size()) {
									if(table1.getSeerCOList().get(i).getSeerCOpl() == table1.getPlayerList().get(j)) {
										data[j][k] = "×";
									}
								}
							}
							else if(datatype == Role.medium) {
								if(i < table1.getMediumCOList().size()) {
									if(table1.getMediumCOList().get(i).getMediumCOpl() == table1.getPlayerList().get(j)) {
										data[j][k] = "×";
									}
								}
							}
							else if(datatype == Role.bodyguard) {
								if(i < table1.getBodyguardCOList().size()) {
									if(table1.getBodyguardCOList().get(i).getBodyguardCOpl() == table1.getPlayerList().get(j)) {
										data[j][k] = "×";
									}
								}
							}
						}
					}
					//System.out.println(datatype.substring(0,1) + j);
				}
			}
		}
		posData = possible;
		/*
		System.out.println("****data2****");
		tab.printTable(data);
		System.out.println("---------");
		for(int k = 0; k < posData.size(); k++) {
			tab.printTable(posData.get(k));
		}
		System.out.println("---------");
		//*/
		
		for(int i = 0; i < table1.getPlayerList().size(); i++) {
			for(int j = 0; j < table1.getRoleCom().getTotal(); j++) {
				if(!data[i][j].equals("●")) {
					boolean noExist = true;
					boolean Disition = true;
					for(int k = 0; k < posData.size(); k++) {
						if(!posData.get(k)[i][j].equals("×")) {
							noExist = false;
						}
						if(!posData.get(k)[i][j].equals("○")) {
							Disition = false;
						}
					}
					if(i == 0) {
						//System.out.println("combine " + j);
						//tab.printTable(data);
					}
					//System.out.println(i + ", noExist:" + noExist + ", Disition:" + Disition);
					if(noExist) {
						data[i][j] = "×";
						/*
						if(datatype.equals("占い師")) {
							System.out.println("× ---(" + i + ", " + j + ")");
						}
						//*/
					}
					else if(Disition) {
						data[i][j] = "○";
					}
				}
			}
		}
		//System.out.println("combine" + datatype);
		//tab.printTable(data);
		
		//対象役職が1確しているなら、全体データに1確役職者の情報を反映
		for(player pl : table1.getPlayerList()) {
			if(stat(data, pl, datatype, "○")) {
				for(int i = 0; i < posData.size(); i++) {
					if(stat(posData.get(i), pl, datatype, "○")) {
						InsertCopyData(data, posData.get(i));
					}
				}
			}
		}
		data = totalSort(data);
		return data;
	}
	
	//2つのデータが同じかどうか？
	
	/** 2つのデータが同じかどうかを表します。
	 * 
	 * @param data1
	 * @param data2
	 * @return
	 */
	final boolean isSameData(String[][] data1, String[][] data2) {
		for(int i = 0; i < table1.getPlayerList().size(); i++) {
			for(int j = 0; j < table1.getRoleCom().getTotal(); j++) {
				if(!data1[i][j].equals(data2[i][j])) {
					return false;
				}
			}
		}
		return true;
	}
	
	/** 2つのデータを同時に満たすデータを作成し、そのデータが破綻せず組み合わせとして存在するかを返します。
	 * 
	 * @param data1
	 * @param data2
	 * @return
	 */
	
	//2つのデータが同時に満たす可能性があるか？
	final boolean is2Possible(String[][] data1, String[][] data2) {
		String[][] data3 = copyData(data2);
		//createTable tab = new createTable(table1.getPlayerList(), table1.getRoleCom());
		//String[][] tmp = tab.initData();
		/*
		System.out.println("================data1===============");
		tab.printTable(data1);
		System.out.println("================data2===============");
		tab.printTable(data2);
		System.out.println("================data3===============");
		tab.printTable(data3);
		//*/
		
		for(int i = 0; i < table1.getPlayerList().size(); i++) {
			for(int j = 0; j < table1.getRoleCom().getTotal(); j++) {
				if(data1[i][j].equals("×")) {
					data3[i][j] = "×";
				}
			}
		}
		/*
		System.out.println("================data2===============");
		tab.printTable(data2);
		System.out.println("================data3===============");
		tab.printTable(data3);
		//*/
		//System.out.println("破綻？" + isBankruptcy(data3));
		if(isBankruptcy(data3)) {
			return false;
		}
		return true;
	}
	
	/** 2つのデータを同時に満たすデータを作成して返します。
	 * 
	 * @param data1
	 * @param data2
	 * @return data data1とdata2を統合したデータ
	 */
	
	//2つのデータを同時に満たすもの
	final String[][] fill2data(String[][] data1, String[][] data2) {
		String[][] data3 = copyData(data2);
		
		for(int i = 0; i < table1.getPlayerList().size(); i++) {
			for(int j = 0; j < table1.getRoleCom().getTotal(); j++) {
				if(data1[i][j].equals("×")) {
					data3[i][j] = "×";
				}
				if(data1[i][j].equals("○")) {
					data3[i][j] = "○";
				}
			}
		}
		/*
		System.out.println("================data2===============");
		tab.printTable(data2);
		System.out.println("================data3===============");
		tab.printTable(data3);
		//*/
		//System.out.println("破綻？" + isBankruptcy(data3));
		
		return data3;
	}
	
	/** <div lang="ja"><big>データの総合フィルタ</big></div><br>
	 * 次のフィルタを行います。<br>
	 * ・犠牲者が2人以上出た場合の処理<br>
	 * ・役職確定の場合の他候補の削除<br>
	 * ・妖狐死亡かつ背徳者生存の候補の削除<br>
	 * ・役職候補が1つに定まった場合の確定処理<br>
	 * ・生存人狼数の調整<br>
	 * ・犠牲者がいない場合の処理<br>
	 * ・全人外露出の処理<br>
	 * 
	 * @param data
	 * @return
	 */
	
	//整理実行
	//dataから盤面上で整理
	final String[][] totalSort(String[][] data) {
		createTable tab = new createTable(table1.getPlayerList(), table1.getRoleCom());
		String[][] tmp = tab.initData();
		//System.out.println("input");
		//tab.printTable(data);
		//System.out.println("確定役職の他の候補の削除");
		//System.out.println("生存者のうち妖狐死亡かつ背徳者生存の削除");
		//System.out.println("役職候補が1つしかないところは確定");
		//int s = 0;
		while(!isSameData(tmp, data)) {
			//tab.printTable(tmp);
			tmp = copyData(data);
			//tab.printTable(tmp);
			  //System.out.println("more2_");
			data = more2Victims(data);
			  //System.out.println("more2");
			//tab.printTable(data);
			  /*
			  if(s == 0)
			  	tab.printTable(data);
			  //*/
			data = dataDisitionSort(data);
			  //System.out.println("dataDisitionSort");
			  //tab.printTable(data);
			data = remFoxspiritDeadImmoralistAlive(data);
			  //System.out.println("remFoxspiritDeadImmoralistAlive");
			  //tab.printTable(data);
			data = leaveRoleDisition(data);
			  //System.out.println("leaveRoleDisition");
			  //tab.printTable(data);
			  //tab.printTable(tmp);
			data = disitionWerewolfAtLeast(data);
			  //System.out.println("disitionWerewolfAtLeast");
			  //tab.printTable(data);
			data = noVictim(data);
			  //System.out.println("noVictim");
			  //tab.printTable(data);
			//s++;
			data = allNonVillagerSideCO(data, false);
			sa++;
		}
		/*
		System.out.println("********* return ***********");
		tab.printTable(data);
		System.out.println("****************************");
		//*/
		return data;
	}
	
	/** <div lang="ja"><big>初期段階の総合フィルタ</big></div><br>
	 * 各役職の結果反映前に行うフィルタです。データの統合フィルタと違って「犠牲者が2人以上出た場合の処理」は行われていません。<br>
	 * ・役職確定の場合の他候補の削除<br>
	 * ・妖狐死亡かつ背徳者生存の候補の削除<br>
	 * ・役職候補が1つに定まった場合の確定処理<br>
	 * ・生存人狼数の調整<br>
	 * ・犠牲者がいない場合の処理<br>
	 * ・全人外露出の処理<br>
	 * @param data
	 * @return
	 */
	
	final String[][] totalPrimarySort(String[][] data) {
		createTable tab = new createTable(table1.getPlayerList(), table1.getRoleCom());
		String[][] tmp = tab.initData();
		//int s = 0;
		while(!isSameData(tmp, data)) {
			//tab.printTable(tmp);
			tmp = copyData(data);
			//tab.printTable(tmp);
			data = dataDisitionSort(data);
			  //System.out.println("dataDisitionSort");
			  //tab.printTable(data);
			data = remFoxspiritDeadImmoralistAlive(data);
			  //System.out.println("remFoxspiritDeadImmoralistAlive");
			  //tab.printTable(data);
			data = leaveRoleDisition(data);
			  //System.out.println("leaveRoleDisition");
			  //tab.printTable(data);
			  //tab.printTable(tmp);
			data = disitionWerewolfAtLeast(data);
			  //System.out.println("disitionWerewolfAtLeast");
			  //tab.printTable(data);
			data = noVictim(data);
			  //System.out.println("noVictim");
			  //tab.printTable(data);
			//s++;
			data = allNonVillagerSideCO(data, false);
		}
		return data;
	}
	
	/** <div lang="ja"><big>作為的に役職指定を追加</big></div><br>
	 * 
	 * @param data 全体データ
	 * @param pl 役職を指定するプレイヤー
	 * @param roleStr 指定役職
	 * @return
	 */
	
	//作為的に役職を指定する場合
	final String[][] addCondition(String[][] data, player pl, String roleStr) {
		for(int i = 0; i < table1.getPlayerList().size(); i++) {
			if(table1.getPlayerList().get(i) == pl) {
				for(int j = 0; j < table1.getRoleCom().getTotal(); j++) {
					if(table1.getRoleCom().getRoleLabel().get(j).equals(roleStr)) {
						data[i][j] = "○";
					}
				}
			}
		}
		//createTable tab = new createTable(table1.getPlayerList(), table1.getRoleCom());
		/*
		System.out.println(sa  + "**************************");
		if(sa == 98)
			tab.printTable(data);
		data = totalSort(data);
		//*/
		//System.out.println(sa  + "**************************");
		//List<String[][]> seerData = seerCOPositioning(data);
		//data = combineData(data, seerData);
		
		return data;
	}
	final String[][] addCondition(String[][] data, player pl, Role role) {
		for(int i = 0; i < table1.getPlayerList().size(); i++) {
			if(table1.getPlayerList().get(i) == pl) {
				for(int j = 0; j < table1.getRoleCom().getTotal(); j++) {
					if(table1.getRoleCom().getRoleLabel().get(j).equals(roles.roleName(role, false))) {
						data[i][j] = "○";
					}
				}
			}
		}
		return data;
	}
	
	/** <div lang="ja"><big>作為的に役職候補を削除</big></div><br>
	 * 
	 * @param data 全体データ
	 * @param pl 役職候補を削除するプレイヤー
	 * @param roleStr 削除役職
	 * @return
	 */
	
	//作為的に役職候補を削除する場合
	final String[][] addDeleteCondition(String[][] data, player pl, String roleStr) {
		for(int i = 0; i < table1.getPlayerList().size(); i++) {
			if(table1.getPlayerList().get(i) == pl) {
				for(int j = 0; j < table1.getRoleCom().getTotal(); j++) {
					if(table1.getRoleCom().getRoleLabel().get(j).equals(roleStr)) {
						data[i][j] = "×";
					}
				}
			}
		}
		
		return data;
	}
	final String[][] addDeleteCondition(String[][] data, player pl, Role role) {
		for(int i = 0; i < table1.getPlayerList().size(); i++) {
			if(table1.getPlayerList().get(i) == pl) {
				for(int j = 0; j < table1.getRoleCom().getTotal(); j++) {
					if(table1.getRoleCom().getRoleLabel().get(j).equals(roles.roleName(role, false))) {
						data[i][j] = "×";
					}
				}
			}
		}
		
		return data;
	}
	
	//** 役職指定呼び出し(複数人含む)
	
	/** <div lang="ja"><big>指定役職のデータラベルのインデックスのリストの呼び出し</big></div><br>
	 * 
	 * @param role
	 * @return 該当インデックスのリスト
	 */
	final private List<Integer> callRoleIndex(Role role) {
		List<Integer> ind = new ArrayList<>();
		for(int i = 0; i < table1.getRoleCom().getTotal(); i++) {
			if(table1.getRoleCom().getRoleLabel().get(i).equals(roles.roleName(role, false))) {
				ind.add(i);
			}
		}
		return ind;
	}
		

	/** <div lang="ja"><big>指定役職のデータラベルの最初のインデックスの呼び出し</big></div><br>
	 * ラベルが見つからなかった場合は <b>-1</b> が返されます。<br>
	 * @param role
	 * @return 役職の該当する最初のインデックス
	 */
	final private int call1RoleIndex(Role role) {
		for(int i = 0; i < table1.getRoleCom().getTotal(); i++) {
			if(table1.getRoleCom().getRoleLabel().get(i).equals(roles.roleName(role, false))) {
				return i;
			}
		}
		return -1;
	}
		

	/** <div lang="ja"><big>指定プレイヤーのデータラベルのインデックスの呼び出し</big></div><br>
	 * 
	 * @param pl
	 * @return 該当するインデックス
	 */
	final private int callPlTabIndex(player pl) {
		for(int i = 0; i < table1.getPlayerList().size(); i++) {
			if(table1.getPlayerList().get(i) == pl) {
				return i;
			}
		}
		return -1;
	}
		
	//* 役職が存在するか？
	
	//配役人数の取得

	/** <div lang="ja"><big>指定役職の配役人数の取得</big></div><br>
	 * 
	 * @param role
	 * @return その役職の配役人数
	 */
	final int callRoleNum(Role role) {
		for(int i = 0; i < roles.roleCount(); i++) {
			if(roles.roletype[i] == role) {
				return table1.getRoleCom().getRole()[i];
			}
		}
		return 0;
	}
		
	
	//* 対象役職以外のデータ挿入 ins

	/** <div lang="ja"><big>データ挿入 【指定役職以外すべて】</big></div><br>
	 * 
	 * @param data 挿入対象データ
	 * @param pl 挿入対象プレイヤー
	 * @param role 挿入除外役職
	 * @param ins 挿入するマーク
	 * @param condition 挿入条件
	 */
	final private void NotInsertData(String[][] data, player pl, Role role, String ins, boolean condition) {
		if(callPlTabIndex(pl) == -1 || callRoleNum(role) == 0) {
			return;
		}
		if(condition) {
			for(Role rol : roles.roletype) {
				if(rol != role) {
					InsertData(data, pl, rol, ins, true);
				}
			}
		}
	}
		

	/** <div lang="ja"><big>データ挿入 【指定役職(複数)以外すべて】</big></div><br>
	 * 
	 * @param data 挿入対象データ
	 * @param pl 挿入対象プレイヤー
	 * @param role 挿入除外役職のリスト
	 * @param ins 挿入するマーク
	 * @param condition 挿入条件
	 */
	final private void NotInsertData(String[][] data, player pl, Role[] role, String ins, boolean condition) {
		if(callPlTabIndex(pl) == -1) {
			return;
		}
		boolean[] nocontain = {true,true,true,true,true,true,true,true,true,true};
		if(condition) {
			for(int i = 0; i < role.length; i++) {
				for(int j = 0; j < roles.roleCount(); j++) {
					if(roles.roletype[j] == role[i]) {
						nocontain[j] = false;
					}
				}
			}
			for(int i = 0; i < roles.roleCount(); i++) {
				if(nocontain[i]) {
					InsertData(data, pl, roles.roletype[i], ins, true);
				}
			}
		}
	}
		
	//* 対象役職のデータ挿入 ins

	/** <div lang="ja"><big>データ挿入 【役職指定】</big></div><br>
	 * 
	 * @param data 挿入対象データ
	 * @param pl 挿入対象プレイヤー
	 * @param role 挿入指定役職
	 * @param ins 挿入するマーク
	 * @param condition 挿入条件
	 */
	final private void InsertData(String[][] data, player pl, Role role, String ins, boolean condition) {
		if(callPlTabIndex(pl) == -1 || callRoleNum(role) == 0) {
			return;
		}
		for(int i = 0; i < callRoleIndex(role).size(); i++) {
			if(condition) {
				data[callPlTabIndex(pl)][callRoleIndex(role).get(i)] = ins;
			}
		}
	}
	
	/** <div lang="ja"><big>データ挿入 【役職指定(複数)】</big></div><br>
	 * 
	 * @param data 挿入対象データ
	 * @param pl 挿入対象プレイヤー
	 * @param role 挿入指定役職のリスト
	 * @param ins 挿入するマーク
	 * @param condition 挿入条件
	 */
	final private void InsertData(String[][] data, player pl, Role[] role, String ins, boolean condition) {
		if(callPlTabIndex(pl) == -1) {
			return;
		}
		for(Role rolestr : role) {
			if(callRoleNum(rolestr) > 0) {
				for(int i = 0; i < callRoleIndex(rolestr).size(); i++) {
					if(condition) {
						data[callPlTabIndex(pl)][callRoleIndex(rolestr).get(i)] = ins;
					}
				}
			}
		}
	}
	
	
	/** <div lang="ja"><big>データにベースデータの情報を反映</big></div><br>
	 * 
	 * @param data 反映させたいデータ
	 * @param base 反映したい情報があるベースデータ
	 */
	final private void InsertCopyData(String[][] data, String[][] base) {
		for(int i = 0; i < table1.getPlayerList().size(); i++) {
			for(int j = 0; j < table1.getRoleCom().getTotal(); j++) {
				if(base[i][j].equals("×")) {
					data[i][j] = "×";
				}
				if(base[i][j].equals("○") && !data[i][j].equals("×")) {
					data[i][j] = "○";
				}
			}
		}
	}
	
	/** <div lang="ja"><big>指定役職の最大deadRank値の取得</big></div><br>
	 * deadRank値は、死亡した順番を表したものです。<br>
	 * 1日目の犠牲者は <b>1</b> 、1日目の追放者は <b>2</b> 、2日目の犠牲者は <b>3</b> ...と続いていきます。<br>
	 * 追放によりランダム毒殺や後追いなどの道連れが起きた場合、その道連れ対象のdeadRank値はその日の追放者と同じ値になります。<br>
	 * @param data 
	 * @param role 指定役職
	 * @return 最大deadRank値
	 */
	final int getMaxRank(String[][] data, Role role) {
		List<Integer> deadList = table1.deadRank();
		int r = 0;
		if(callRoleNum(role) == 0) {
			return r;
		}
		for(int i = 0; i < table1.getPlayerList().size(); i++) {
			if(!data[i][call1RoleIndex(role)].equals("×") && deadList.get(i) > r) {
				r = deadList.get(i); 
			}
		}
		return r;
	}
	
	//* 最小DeadRank値の取得
	
	/** <div lang="ja"><big>指定役職の最小deadRank値の取得</big></div><br>
	 * deadRank値は、死亡した順番を表したものです。<br>
	 * 1日目の犠牲者は <b>1</b> 、1日目の追放者は <b>2</b> 、2日目の犠牲者は <b>3</b> ...と続いていきます。<br>
	 * 追放によりランダム毒殺や後追いなどの道連れが起きた場合、その道連れ対象のdeadRank値はその日の追放者と同じ値になります。<br>
	 * @param data 
	 * @param role 指定役職
	 * @return 最小deadRank値
	 */
	final int getMinRank(String[][] data, Role role) {
		List<Integer> deadList = table1.deadRank();
		int r = getAliveRank();
		if(callRoleNum(role) == 0) {
			return r;
		}
		for(int i = 0; i < table1.getPlayerList().size(); i++) {
			if(!data[i][call1RoleIndex(role)].equals("×") && deadList.get(i) < r) {
				r = deadList.get(i); 
			}
		}
		return r;
	}
	
	/** <div lang="ja"><big>指定役職のrank番目に小さいdeadRank値の取得</big></div><br>
	 * deadRank値は、死亡した順番を表したものです。<br>
	 * 1日目の犠牲者は <b>1</b> 、1日目の追放者は <b>2</b> 、2日目の犠牲者は <b>3</b> ...と続いていきます。<br>
	 * 追放によりランダム毒殺や後追いなどの道連れが起きた場合、その道連れ対象のdeadRank値はその日の追放者と同じ値になります。<br>
	 * @param data 
	 * @param role 指定役職
	 * @param rank 
	 * @return rank番目に小さいdeadRank値
	 */
	final private int getMinRank(String[][] data, Role role, int rank) {
		List<Integer> deadList = table1.deadRank();
		int r = getAliveRank();
		if(callRoleNum(role) == 0) {
			return r;
		}
		int[] deadListArray = new int[deadList.size()];
		for(int i = 0; i < deadList.size(); i++) {
			deadListArray[i] = deadList.get(i);
		}
		
		for(int i = 0; i < deadList.size(); i++) {
			if(data[i][call1RoleIndex(role)].equals("×")) {
				deadListArray[i] = getAliveRank();
			}
		}
		Arrays.sort(deadListArray);
		Collections.sort(deadList);
		/*
		for(Integer i : deadListArray) {
			System.out.print(i + ", ");
		}
		//*/
		if(rank > 0 && rank <= deadList.size()) {
			r = deadListArray[rank - 1];
		}
		//System.out.println("[" + r + ", " + rank);
		return r;
	}
	
	
	/** <div lang="ja"><big>生存者のdeadRank値の取得</big></div><br>
	 * deadRank値は、死亡した順番を表したものです。<br>
	 * 1日目の犠牲者は <b>1</b> 、1日目の追放者は <b>2</b> 、2日目の犠牲者は <b>3</b> ...と続いていきます。<br>
	 * 追放によりランダム毒殺や後追いなどの道連れが起きた場合、その道連れ対象のdeadRank値はその日の追放者と同じ値になります。<br>
	 * @return 生存者のdeadRank値(すべてのプレイヤーでの最大deadRank値)
	 */
	final int getAliveRank() {
		List<Integer> deadList = table1.deadRank();
		int aliveRank = 0;
		for(Integer i : deadList) {
			if(aliveRank < i) {
				aliveRank = i;
			}
		}
		return aliveRank;
	}
	
	//* 確定人数の取得
	/** <div lang="ja"><big>指定役職のうちその役職が確定しているプレイヤーの人数の取得</big></div><br>
	 * 
	 * @param data
	 * @param role
	 * @return
	 */
	final private int getDisitionNum(String[][] data, Role role) {
		int DisitionNum = 0;
		for(int i = 0; i < table1.getPlayerList().size(); i++) {
			if(data[i][call1RoleIndex(role)].equals("○")) {
				DisitionNum++;
			}
		}
		return DisitionNum;
	}
	
	/** <div lang="ja"><big>指定役職のうちその役職が確定しているプレイヤーの最大deadRank値の取得</big></div><br>
	 * deadRank値は、死亡した順番を表したものです。<br>
	 * 1日目の犠牲者は <b>1</b> 、1日目の追放者は <b>2</b> 、2日目の犠牲者は <b>3</b> ...と続いていきます。<br>
	 * 追放によりランダム毒殺や後追いなどの道連れが起きた場合、その道連れ対象のdeadRank値はその日の追放者と同じ値になります。<br>
	 * @param data
	 * @param role
	 * @return
	 */
	final private int getDisitionMaxRank(String[][] data, Role role) {
		List<Integer> deadList = table1.deadRank();
		int DisitionMaxRank = 0;
		if(callRoleNum(role) == 0) {
			return DisitionMaxRank;
		}
		for(int i = 0; i < table1.getPlayerList().size(); i++) {
			if(data[i][call1RoleIndex(role)].equals("○")) {
				if(deadList.get(i) > DisitionMaxRank) {
					DisitionMaxRank = deadList.get(i);
				}
			}
		}
		return DisitionMaxRank;
	}
	
	/** <div lang="ja"><big>指定役職のうちその役職が確定しているプレイヤーのdeadRank値のリストの取得</big></div><br>
	 * deadRank値は、死亡した順番を表したものです。<br>
	 * 1日目の犠牲者は <b>1</b> 、1日目の追放者は <b>2</b> 、2日目の犠牲者は <b>3</b> ...と続いていきます。<br>
	 * 追放によりランダム毒殺や後追いなどの道連れが起きた場合、その道連れ対象のdeadRank値はその日の追放者と同じ値になります。<br>
	 * @param data
	 * @param role
	 * @return
	 */
	final private List<Integer> getDisitionRankList(String[][] data, Role role) {
		List<Integer> deadList = table1.deadRank();
		List<Integer> DisitionList = new ArrayList<>();
		if(callRoleNum(role) == 0) {
			return DisitionList;
		}
		for(int i = 0; i < table1.getPlayerList().size(); i++) {
			if(data[i][call1RoleIndex(role)].equals("○")) {
				DisitionList.add(deadList.get(i));
			}
		}
		return DisitionList;
	}
	
	/** <div lang="ja"><big>deadRank値がborder以上のプレイヤーの人数の取得</big></div><br>
	 * deadRank値は、死亡した順番を表したものです。<br>
	 * 1日目の犠牲者は <b>1</b> 、1日目の追放者は <b>2</b> 、2日目の犠牲者は <b>3</b> ...と続いていきます。<br>
	 * 追放によりランダム毒殺や後追いなどの道連れが起きた場合、その道連れ対象のdeadRank値はその日の追放者と同じ値になります。<br>
	 * @param rankList
	 * @param border
	 * @return
	 */
	
	// rankListに含まれるうちの生存人数を取得
	final int countRank(List<Integer> rankList, int border) {
		int count = 0;
		for(Integer rank : rankList) {
			if(rank >= border) {
				count++;
			}
		}
		return count;
	}
	
	/** <div lang="ja"><big>確定人外かどうかの判定</big></div><br>
	 * plで取得したプレイヤーがdataの情報において確定人外かどうかを判定します。<br>
	 * ※データが破綻している場合は正確に判定しない場合があります。<br>
	 * @param data
	 * @param pl
	 * @return
	 */
	
	// プレイヤーが確定人外か？
	final boolean isDisitionNonVillagerSide(String[][] data, player pl) {
		if(callRoleNum(Role.villager) > 0) {
			if(!stat(data, pl, Role.villager, "×")) {
				return false;
			}
		}
		if(callRoleNum(Role.seer) > 0) {
			if(!stat(data, pl, Role.seer, "×")) {
				return false;
			}
		}
		if(callRoleNum(Role.medium) > 0) {
			if(!stat(data, pl, Role.medium, "×")) {
				return false;
			}
		}
		if(callRoleNum(Role.bodyguard) > 0) {
			if(!stat(data, pl, Role.bodyguard, "×")) {
				return false;
			}
		}
		if(callRoleNum(Role.freemason) > 0) {
			if(!stat(data, pl, Role.freemason, "×")) {
				return false;
			}
		}
		if(callRoleNum(Role.toxic) > 0) {
			if(!stat(data, pl, Role.toxic, "×")) {
				return false;
			}
		}
		return true;
	}
	
	/** <div lang="ja"><big>確定村人陣営かどうかの判定</big></div><br>
	 * plで取得したプレイヤーがdataの情報において確定で村人陣営かどうかを判定します。<br>
	 * ※データが破綻している場合は正確に判定しない場合があります。<br>
	 * @param data
	 * @param pl
	 * @return
	 */
	
	// プレイヤーが確定村人陣営か？
	final boolean isDisitionVillagerSide(String[][] data, player pl) {
		if(callRoleNum(Role.werewolf) > 0) {
			if(!stat(data, pl, Role.werewolf, "×")) {
				return false;
			}
		}
		if(callRoleNum(Role.fanatic) > 0) {
			if(!stat(data, pl, Role.fanatic, "×")) {
				return false;
			}
		}
		if(callRoleNum(Role.foxspirit) > 0) {
			if(!stat(data, pl, Role.foxspirit, "×")) {
				return false;
			}
		}
		if(callRoleNum(Role.immoralist) > 0) {
			if(!stat(data, pl, Role.immoralist, "×")) {
				return false;
			}
		}
		return true;
	}
	
	/** <div lang="ja"><big>dataにおいて指定箇所のマークが一致しているかどうかの判定</big></div><br>
	 * 
	 * @param data 
	 * @param pl 指定プレイヤー
	 * @param role 指定役職
	 * @param status マーク
	 * @return
	 */
	final boolean stat(String[][] data, player pl, Role role, String status) {
		if(call1RoleIndex(role) != -1) {
			return data[callPlTabIndex(pl)][call1RoleIndex(role)].equals(status);
		}
		return false;
	}
	
	
	// もしプレイヤーの役職が確定していたらその役職名を返す(確定してない場合は空の文字列が返される)
	/** <div lang="ja"><big>該当プレイヤーについてもし役職が確定していたらその役職を取得</big></div><br>
	 * 確定していなかったらother(役職不定)が返されます。<br>
	 * @param data
	 * @param pl
	 * @return
	 */
	final Role plToRoletype(String[][] data, player pl) {
		for(int i = 0; i < roles.roleCount(); i++) {
			if(table1.getRoleCom().getRole()[i] > 0) {
				if(data[callPlTabIndex(pl)][call1RoleIndex(roles.roletype[i])].equals("○")) {
					return roles.roletype[i];
				}
			}
		}
		return Role.other;
	}
	
	/** <div lang="ja"><big>該当プレイヤーについての役職候補のリストの取得</big></div><br>
	 * 
	 * @param data
	 * @param pl
	 * @return
	 */
	final List<Role> plToRoletypeCandidate(String[][] data, player pl) {
		List<Role> candidate = new ArrayList<>();
		for(int i = 0; i < roles.roleCount(); i++) {
			if(table1.getRoleCom().getRole()[i] > 0) {
				if(!data[callPlTabIndex(pl)][call1RoleIndex(roles.roletype[i])].equals("×")) {
					candidate.add(roles.roletype[i]);
				}
			}
		}
		return candidate;
	}
	
	/** <div lang="ja"><big>指定役職についてその役職が確定しているプレイヤーのリストの取得</big></div><br>
	 * 
	 * @param data
	 * @param role
	 * @return
	 */
	final List<player> roleToPl(String[][] data, Role role) {
		List<player> tmp = new ArrayList<>();
		for(player pl : table1.getPlayerList()) {
			if(data[callPlTabIndex(pl)][call1RoleIndex(role)].equals("○")) {
				tmp.add(pl);
			}
		}
		return tmp;
	}
	
	/** <div lang="ja"><big>指定役職のプレイヤー候補のリストの取得</big></div><br>
	 * 
	 * @param data
	 * @param role
	 * @return
	 */
	final List<player> roleToPlCandidate(String[][] data, Role role) {
		List<player> tmp = new ArrayList<>();
		for(player pl : table1.getPlayerList()) {
			if(!data[callPlTabIndex(pl)][call1RoleIndex(role)].equals("×")) {
				tmp.add(pl);
			}
		}
		return tmp;
	}
	
	/** <div lang="ja"><big>犠牲者の中に確定で人狼が含まれる場合の処理</big></div><br>
	 * 犠牲者が2人以上出たときにその中に確定で人狼が含まれている場合の人狼の人数調整を行います。<br>
	 * day2 については、犠牲者が2人以上の日が2回あってどちらかの日の犠牲者が埋毒者と人狼であることが確定している場合に使います。そうでない場合は <b>-1</b> が格納されます。<br>
	 * @param data 
	 * @param day 犠牲者が2人以上出た日の日数
	 * @param pl 注目するプレイヤー
	 * @param insertCondition 挿入条件
	 * @param day2 犠牲者が2人以上出た日が2回あった場合の2回目の日数
	 * @param is2Time 犠牲者が2人以上出た日が2回あったか？
	 */
	final private void disitionWerewolfContainVic(String[][] data, int day, player pl, boolean insertCondition, int day2, boolean is2Time) {
		if(insertCondition) {
			int werewolfDisitionNum = getDisitionNum(data, Role.werewolf);
			
			//複数犠牲者のうち人狼が確定しているプレイヤーが存在するか？
			boolean werewolfDisitionContainVic = false;
			for(player pl2 : table1.getPlayerList()) {
				if(table1.getVictims().get(day).contains(pl2)) {
					if(stat(data, pl2, Role.werewolf, "○")) {
						werewolfDisitionContainVic = true;
					}
				}
				else if(is2Time) {
					if(table1.getVictims().get(day2).contains(pl2)) {
						if(stat(data, pl2, Role.werewolf, "○")) {
							werewolfDisitionContainVic = true;
						}
					}
				}
			}
			
			if(callRoleNum(Role.werewolf) == werewolfDisitionNum + 1) {
				//もし複数犠牲者に含まれない人狼が(人狼配役数 - 1)人分確定していたら、複数犠牲者に含まれない人狼が確定していない位置は人狼ではない
				if(!werewolfDisitionContainVic && !table1.getVictims().get(day).contains(pl)) {
					if(is2Time) {
						if(!table1.getVictims().get(day2).contains(pl)) {
							InsertData(data, pl, Role.werewolf, "×", !stat(data, pl, Role.werewolf, "○"));
						}
					}
					else {
						InsertData(data, pl, Role.werewolf, "×", !stat(data, pl, Role.werewolf, "○"));
					}
				}
			}
			if(callRoleNum(Role.werewolf) == werewolfDisitionNum + 2) {
				//もし複数犠牲者に含まれない人狼が(人狼配役数 - 2)人分確定していてその後ゲームが続いていたら、複数犠牲者に含まれない人狼が確定していない死亡者位置は人狼ではない (ゲームが続いているなら人狼は1人以上生存している)
				//List<Integer> deadList = table1.deadRank();
				int werewolfDisitionMaxRank = getDisitionMaxRank(data, Role.werewolf);
				if(2 * day > werewolfDisitionMaxRank) {
					if(!werewolfDisitionContainVic && !table1.getVictims().get(day).contains(pl)) {
						if(is2Time) {
							if(!table1.getVictims().get(day2).contains(pl)) {
								InsertData(data, pl, Role.werewolf, "×", !stat(data, pl, Role.werewolf, "○") && table1.deadRankPlayer(pl) < 2 * day - 1 && getAliveRank() > 2 * day);
							}
						}
						else {
							InsertData(data, pl, Role.werewolf, "×", !stat(data, pl, Role.werewolf, "○") && table1.deadRankPlayer(pl) < 2 * day - 1 && getAliveRank() > 2 * day);
						}
					}
				}
			}
		}
	}
	
	//* 犠牲者の中に背徳者が確定で含まれているときの処理 (jはplに対応する表のインデックス)
	
	/** <div lang="ja"><big>犠牲者の中に確定で背徳者が含まれる場合の処理</big></div><br>
	 * 犠牲者が2人以上出たときにその中に確定で背徳者が含まれている場合の背徳者の人数調整を行います。<br>
	 * day2 については、犠牲者が2人以上の日が2回あった場合に使います。そうでない場合は <b>-1</b> が格納されます。<br>
	 * @param data 
	 * @param day 犠牲者が2人以上出た日の日数
	 * @param pl 注目するプレイヤー
	 * @param insertCondition 挿入条件
	 * @param day2 犠牲者が2人以上出た日が2回あった場合の2回目の日数
	 * @param is2Time 犠牲者が2人以上出た日が2回あったか？
	 */
	final private void disitionImmoralistContainVic(String[][] data, int day, player pl, boolean insertCondition, int day2, boolean is2Time) {
		if(insertCondition) {
			int immoralistDisitionNum = getDisitionNum(data, Role.immoralist);
			
			//複数犠牲者のうち背徳者が確定しているプレイヤーが存在するか？
			boolean immoralistDisitionContainVic = false;
			for(player pl2 : table1.getPlayerList()) {
				if(table1.getVictims().get(day).contains(pl2)) {
					if(stat(data, pl2, Role.immoralist, "○")) {
						immoralistDisitionContainVic = true;
					}
				}
				else if(is2Time) {
					if(table1.getVictims().get(day2).contains(pl2)) {
						if(stat(data, pl2, Role.immoralist, "○")) {
							immoralistDisitionContainVic = true;
						}
					}
				}
			}
			
			if(callRoleNum(Role.immoralist) == immoralistDisitionNum + 1) {
				//もし複数犠牲者に含まれない背徳者が(背徳者配役数 - 1)人分確定していたら、複数犠牲者に含まれない背徳者が確定していない位置は背徳者ではない
				if(!immoralistDisitionContainVic && !table1.getVictims().get(day).contains(pl)) {
					if(is2Time) {
						if(!table1.getVictims().get(day2).contains(pl)) {
							InsertData(data, pl, Role.immoralist, "×", !stat(data, pl, Role.immoralist, "○"));
						}
					}
					else {
						InsertData(data, pl, Role.immoralist, "×", !stat(data, pl, Role.immoralist, "○"));
					}
				}
			}
		}
	}
	
	
	
	
}