package org.aiwolf.sample.player.arrange_tool;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;



final class table {
	final private String[] RoleFullStr = {"村人", "占い師", "霊能者", "狩人","共有者", "人狼", "狂信者", "妖狐", "背徳者", "埋毒者"};
	final private String[] RoleCOFullStr = {"占い師", "霊能者", "狩人"};
	final private List<player> plList;                 //プレイヤーリスト
    final private List<List<player>> victims;          //犠牲者
    final private List<player> expelleds;              //追放者
    final private int suicideDay;                      //妖狐追放による道連れ発生日数
    final private List<player> suicides;               //妖狐追放による道連れ対象
    final private int poisonedDay;                     //埋毒者追放によるランダム毒殺発生日数
    final private player poisoned;                     //埋毒者追放によるランダム毒殺対象
    final private List<seerCO> seerCOList;             //占い師CO情報
    final private List<mediumCO> mediumCOList;         //霊能者CO情報
    final private List<bodyguardCO> bodyguardCOList;   //狩人CO情報
    final private freemasonCO freemasonCOList;         //共有者CO情報
    final private List<toxicCO> toxicCOList;           //埋毒者CO情報
    private List<seerCO> seerHide;
    private List<mediumCO> mediumHide;
    private List<bodyguardCO> bodyguardHide;
    private List<toxicCO> toxicHide;
    final private boolean isOver;                      //ゲームの終了
    final private boolean selfAttack;                  //自噛み
    final private boolean noAttack;                    //噛みなし
    final private boolean firstDivine;                 //初日占い
    final private boolean firstAttack;                 //初日噛み
    final private boolean seerLackFirstDivine;
    final private List<player> firstVictim;            //初日呪殺死亡(初日噛みなしの場合)
    final private player lackPlayer;                   //欠けプレイヤー
    final private List<String> outLackRole;            //
    final private List<String> outHideRole;            //
    final private roleCombination roleCom;             //
    
    table(final List<player> plList, final boolean selfAttack, final boolean noAttack, final boolean firstDivine,
    		final boolean firstAttack, final List<player> firstVictim, final player lackPl, final boolean[] Lack, final boolean[] Hide, final boolean seerLackFirstDivine, final List<List<player>> victims, final List<player> expelleds,
    		final int suicideDay, final List<player> suicides, final int poisonedDay, final List<player> poisoned,
    		final List<seerCO> seerCOList, final List<mediumCO> mediumCOList, final List<bodyguardCO> bodyguardCOList,
    		final freemasonCO freemasonCOList, final List<toxicCO> toxicCOList,
    		final boolean isOver, final roleCombination roleCom) {
    	//今参照になっているが後で編集しておくこと
    	this.plList = plList;
    	List<List<player>> tmp = new ArrayList<>();
    	for(int i = 0; i < victims.size(); i++) {
    		List<player> tmp2 = new ArrayList<>();
    		for(int j = 0; j < victims.get(i).size(); j++) {
    			tmp2.add(victims.get(i).get(j));
    		}
    		tmp.add(tmp2);
    	}
    	this.selfAttack = selfAttack;
    	this.noAttack = noAttack;
    	this.firstDivine = firstDivine;
    	this.firstAttack = firstAttack;
    	this.firstVictim = firstVictim;
    	this.lackPlayer = lackPl;
    	this.seerLackFirstDivine = seerLackFirstDivine;
    	List<String> lackRole = new ArrayList<>();
    	List<String> nohideRole = new ArrayList<>();
    	for(int i = 0; i < Lack.length; i++) {
    		if(!Lack[i]) {
    			lackRole.add(RoleFullStr[i]);
    		}
    	}
    	for(int i = 0; i < Hide.length; i++) {
    		if(!Hide[i]) {
    			nohideRole.add(RoleCOFullStr[i]);
    		}
    	}
    	outLackRole = lackRole;
    	outHideRole = nohideRole;
    	this.victims = tmp;
    	this.expelleds = expelleds;
    	this.suicideDay = suicideDay;
    	this.suicides = suicides;
    	this.poisonedDay = poisonedDay;
    	if(poisoned.size() > 1) {
    		this.poisoned = poisoned.get(1);
    	}
    	else {
    		this.poisoned = poisoned.get(0);
    	}
    	this.seerCOList = seerCOList;
    	int seerFastCOday = 999999;
    	for(int j = 0; j < seerCOList.size(); j++) {
    		if(seerFastCOday > seerCOList.get(j).getCOday()) {
    			seerFastCOday = seerCOList.get(j).getCOday();
    		}
    	}
    	this.mediumCOList = mediumCOList;
    	int mediumFastCOday = 999999;
    	for(int j = 0; j < mediumCOList.size(); j++) {
    		if(mediumFastCOday > mediumCOList.get(j).getCOday()) {
    			mediumFastCOday = mediumCOList.get(j).getCOday();
    		}
    	}
    	this.bodyguardCOList = bodyguardCOList;
    	int bodyguardFastCOday = 999999;
    	for(int j = 0; j < bodyguardCOList.size(); j++) {
    		if(bodyguardFastCOday > bodyguardCOList.get(j).getCOday()) {
    			bodyguardFastCOday = bodyguardCOList.get(j).getCOday();
    		}
    	}
    	this.freemasonCOList = freemasonCOList;
    	this.isOver = isOver;
    	this.roleCom = roleCom;
    	
    	this.toxicCOList = toxicCOList;
    	int toxicFastCOday = 999999;
    	for(int j = 0; j < toxicCOList.size(); j++) {
    		if(toxicFastCOday > toxicCOList.get(j).getCOday()) {
    			toxicFastCOday = toxicCOList.get(j).getCOday();
    		}
    	}
    	
    	List<Integer> rank = deadRank();
    	seerHide = new ArrayList<>();
    	mediumHide = new ArrayList<>();
    	bodyguardHide = new ArrayList<>();
    	toxicHide = new ArrayList<>();
    	for(int i = 0; i < plList.size(); i++) {
    		if(rank.get(i) < 2 * seerFastCOday && seerCOList.size() > 0 && !freemasonCOList.getFreemasonCOpl().contains(plList.get(i))) {
    			seerHide.add(new seerCO(plList.get(i), new ArrayList<result>(), -1));
    		}
    		if(rank.get(i) < 2 * mediumFastCOday && mediumCOList.size() > 0 && !freemasonCOList.getFreemasonCOpl().contains(plList.get(i))) {
    			mediumHide.add(new mediumCO(plList.get(i), new ArrayList<result>(), -1));
    		}
    		if(rank.get(i) < 2 * bodyguardFastCOday && bodyguardCOList.size() > 0 && !freemasonCOList.getFreemasonCOpl().contains(plList.get(i))) {
    			bodyguardHide.add(new bodyguardCO(plList.get(i), new ArrayList<player>(), -1));
    		}
    		if(rank.get(i) < 2 * toxicFastCOday && toxicCOList.size() > 0 && !freemasonCOList.getFreemasonCOpl().contains(plList.get(i))) {
    			for(int j = 1; j <= toxicFastCOday; j++) {
    				if(victims.get(j).size() > 1) {
    					if(victims.get(j).contains(plList.get(i))) {
    						toxicHide.add(new toxicCO(plList.get(i), -1));
    					}
    				}
    			}
    		}
    	}
    }
    
    //プレイヤー一覧の取得
    final List<player> getPlayerList() {
    	List<player> tmp = new ArrayList<>();
    	for(int i = 0; i < plList.size(); i++) {
    		tmp.add(plList.get(i));
    	}
    	return tmp;
    }
    
    //番号からプレイヤーの取得
    final player getIdToPlayer(final int id) {
    	for(player pl : plList) {
    		if(pl.getId() == id) {
    			return pl;
    		}
    	}
    	return new player("×", -1);
    }
    
    //犠牲者の取得
    final List<List<player>> getVictims() {
    	List<List<player>> tmp = new ArrayList<>();
        for(int i = 0; i < victims.size(); i++) {
            List<player> tmp2 = new ArrayList<>();
            for(int j = 0; j < victims.get(i).size(); j++) {
            	tmp2.add(victims.get(i).get(j));
            }
            tmp.add(tmp2);
        }
        return tmp;
    }
    
    //追放者の取得
    final List<player> getExpelleds() {
    	List<player> tmp = new ArrayList<>();
        for(int i = 0; i < expelleds.size(); i++) {
            tmp.add(expelleds.get(i));
        }
        return tmp;
    }
    
    //道連れがおきた日数の取得
    final int getSuicideDay() {
    	return suicideDay;
    }
    
    //道連れになったプレイヤーの取得
    final List<player> getSuicides() {
    	List<player> tmp = new ArrayList<>();
        for(int i = 0; i < suicides.size(); i++) {
            tmp.add(suicides.get(i));
        }
        return tmp;
    }
    
    //ランダム毒殺がおきた日数の取得
    final int getPoisonedDay() {
    	return poisonedDay;
    }
    
    //ランダム毒殺対象の取得
    final player getPoisoned() {
    	return poisoned;
    }
    
    //生存者の取得
    final List<player> getAlivePl() {
    	List<player> dead = new ArrayList<>();
    	List<player> alive = new ArrayList<>();
    	if(lackPlayer.getId() != -1) {
    		dead.add(lackPlayer);
    	}
    	for(List<player> dayVic : victims) {
    		for(player vic : dayVic) {
    			dead.add(vic);
    		}
    	}
    	for(player expe : expelleds) {
    		dead.add(expe);
    	}
    	if(poisoned.getId() != -1) {
    		dead.add(poisoned);
    	}
    	for(player sui : suicides) {
    		dead.add(sui);
    	}
    	for(player vic : firstVictim) {
    		dead.add(vic);
    	}
    	for(player pl : plList) {
    		if(!dead.contains(pl)) {
    			alive.add(pl);
    		}
    	}
    	return alive;
    }
    
    //占い師CO情報の取得
    final List<seerCO> getSeerCOList() {
    	List<seerCO> tmp = new ArrayList<>();
        for(int i = 0; i < seerCOList.size(); i++) {
            tmp.add(seerCOList.get(i));
        }
        return tmp;
    }
    
    //霊能者CO情報の取得
    final List<mediumCO> getMediumCOList() {
    	List<mediumCO> tmp = new ArrayList<>();
        for(int i = 0; i < mediumCOList.size(); i++) {
            tmp.add(mediumCOList.get(i));
        }
        return tmp;
    }
    
    //狩人CO情報の取得
    final List<bodyguardCO> getBodyguardCOList() {
    	List<bodyguardCO> tmp = new ArrayList<>();
        for(int i = 0; i < bodyguardCOList.size(); i++) {
            tmp.add(bodyguardCOList.get(i));
        }
        return tmp;
    }
    
    //共有者COの取得
    final freemasonCO getFreemasonCOList() {
    	return freemasonCOList;
    }
    
    //埋毒者CO情報の取得
    final List<toxicCO> getToxicCOList() {
    	List<toxicCO> tmp = new ArrayList<>();
        for(int i = 0; i < toxicCOList.size(); i++) {
            tmp.add(toxicCOList.get(i));
        }
        return tmp;
    }
    
    //占い師潜伏死亡の可能性リスト
    final List<seerCO> getSeerHideList() {
    	List<seerCO> tmp = new ArrayList<>();
        for(int i = 0; i < seerHide.size(); i++) {
            tmp.add(seerHide.get(i));
        }
        return tmp;
    }
    
    //特定playerの占い師潜伏死亡の可能性の削除
    final void deleteSeerHide(player pl) {
    	for(int i = 0; i < seerHide.size(); i++) {
    		if(seerHide.get(i).getSeerCOpl() == pl) {
    			seerHide.remove(i);
    		}
    	}
    }
    
    //rank未満の占い師潜伏死亡の可能性の削除
    final void deleteSeerHide(int rank) {
    	/*
    	for(int i = 0; i < seerHide.size(); i++) {
    		if(rank > deadRankPlayer(seerHide.get(i).getSeerCOpl())) {
    			seerHide.remove(i);
    		}
    	}
    	//*/
    	List<seerCO> tmp = new ArrayList<>();
    	seerCO empty = new seerCO(new player("×", 0), new ArrayList<result>(), -3);
    	for(int i = 0; i < seerHide.size(); i++) {
    		tmp.add(seerHide.get(i));
    	}
    	for(int i = 0; i < tmp.size(); i++) {
    		if(rank > deadRankPlayer(tmp.get(i).getSeerCOpl())) {
    			tmp.remove(i);
    			tmp.add(i, empty);
    		}
    	}
    	List<seerCO> tmp2 = new ArrayList<>();
    	
    	for(seerCO seer : tmp) {
    		if(seer.getCOday() != -3) {
    			tmp2.add(seer);
    		}
    	}
    	
    	seerHide = tmp2;
    	//System.out.println("del_al");
    }
    
    //霊能者潜伏死亡の可能性リスト
    final List<mediumCO> getMediumHideList() {
    	List<mediumCO> tmp = new ArrayList<>();
        for(int i = 0; i < mediumHide.size(); i++) {
            tmp.add(mediumHide.get(i));
        }
        return tmp;
    }
    
    //特定playerの霊能者潜伏死亡の可能性の削除
    final void deleteMediumHide(player pl) {
    	for(int i = 0; i < mediumHide.size(); i++) {
    		if(mediumHide.get(i).getMediumCOpl() == pl) {
    			mediumHide.remove(i);
    		}
    	}
    }
    
    //狩人潜伏死亡の可能性リスト
    final List<bodyguardCO> getBodyguardHideList() {
    	List<bodyguardCO> tmp = new ArrayList<>();
        for(int i = 0; i < bodyguardHide.size(); i++) {
            tmp.add(bodyguardHide.get(i));
        }
        return tmp;
    }
    
    //特定playerの狩人潜伏死亡の可能性の削除
    final void deleteBodyguardHide(player pl) {
    	for(int i = 0; i < bodyguardHide.size(); i++) {
    		if(bodyguardHide.get(i).getBodyguardCOpl() == pl) {
    			bodyguardHide.remove(i);
    		}
    	}
    }
    
    //rank未満の狩人潜伏死亡の可能性の削除
    final void deleteBodyguardHide(int rank) {
    	List<bodyguardCO> tmp = new ArrayList<>();
    	bodyguardCO empty = new bodyguardCO(new player("×", 0), new ArrayList<player>(), -3);
    	for(int i = 0; i < bodyguardHide.size(); i++) {
    		tmp.add(bodyguardHide.get(i));
    	}
    	for(int i = 0; i < tmp.size(); i++) {
    		if(rank > deadRankPlayer(tmp.get(i).getBodyguardCOpl())) {
    			tmp.remove(i);
    			tmp.add(i, empty);
    		}
    	}
    	List<bodyguardCO> tmp2 = new ArrayList<>();
    	
    	for(bodyguardCO bodyguard : tmp) {
    		if(bodyguard.getCOday() != -3) {
    			tmp2.add(bodyguard);
    		}
    	}
    	
    	bodyguardHide = tmp2;
    	
    }
    
    //埋毒者潜伏死亡の可能性リスト
    final List<toxicCO> getToxicHideList() {
    	List<toxicCO> tmp = new ArrayList<>();
        for(int i = 0; i < toxicHide.size(); i++) {
            tmp.add(toxicHide.get(i));
        }
        return tmp;
    }
    
    //特定playerの埋毒者潜伏死亡の可能性の削除
    final void deleteToxicHide(player pl) {
    	for(int i = 0; i < toxicHide.size(); i++) {
    		if(toxicHide.get(i).getToxicCOpl() == pl) {
    			toxicHide.remove(i);
    		}
    	}
    }
    
    //配役情報の取得
    final roleCombination getRoleCom() {
    	return roleCom;
    }
    
    //ゲームが終了しているか
    final boolean getIsOver() {
    	return isOver;
    }
    
    //自噛み可能か？
    final boolean getSelfAttack() {
    	return selfAttack;
    }
    
    //噛みなし可能か？
    final boolean getNoAttack() {
    	return noAttack;
    }
    
    //初日占いありか？
    final boolean getFirstDivine() {
    	return firstDivine;
    }
    
    //初日噛みありか？
    final boolean getFirstAttack() {
    	return firstAttack;
    }
    
    //初日呪殺死亡(初日噛みなし)のリスト
    final List<player> getFirstVictim() {
    	List<player> out = new ArrayList<>();
    	for(player pl : firstVictim) {
    		out.add(pl);
    	}
    	return out;
    }
    
    //欠けプレイヤーの取得
    final player getLack() {
    	return lackPlayer;
    }
    
    //欠け除外役職一覧の取得
    final List<String> outLackRole() {
    	List<String> out = new ArrayList<>();
    	for(String r : outLackRole) {
    		out.add(r);
    	}
    	return out;
    }
    final List<Role> outLackRoletype() {
    	List<Role> out = new ArrayList<>();
    	for(String r : outLackRole) {
    		out.add(roles.getRoletype(r));
    	}
    	return out;
    }
    
    //潜伏除外役職一覧の取得
    final List<String> outHideRole() {
    	List<String> out = new ArrayList<>();
    	for(String r : outHideRole) {
    		out.add(r);
    	}
    	return out;
    }
    final List<Role> outHideRoletype() {
    	List<Role> out = new ArrayList<>();
    	for(String r : outHideRole) {
    		out.add(roles.getRoletype(r));
    	}
    	return out;
    }
    
    //占い師欠けのときに初日占いしているか？
    final boolean getSeerLackFirstDivine() {
    	return seerLackFirstDivine;
    }
    
    //deadRank(死亡した順番)の取得
    final List<Integer> deadRank() {
		List<Integer> tmp = new ArrayList<>();
		for(int i = 0; i < getPlayerList().size(); i++) {
			player pl = getPlayerList().get(i);
			int deadtime = getVictims().size() + getExpelleds().size() - 1;
			//System.out.println("deadtime : " + getExpelleds().size());
			if(firstVictim.contains(pl)) {
				deadtime = 1;
			}
			if(lackPlayer == pl) {
				deadtime = 1;
			}
			for(int j = 0; j < getVictims().size(); j++) {
				if(getVictims().get(j).contains(pl)) {
					deadtime = 2 * j - 1;
				}
			}
			for(int j = 0; j < getExpelleds().size(); j++) {
				if(getExpelleds().get(j) == pl) {
					deadtime = 2 * j;
				}
				if(suicideDay == j && suicides.size() != 0) {
					if(getSuicides().contains(pl)) {
						deadtime = 2 * j;
					}
				}
				if(poisonedDay == j) {
					if(poisoned == pl) {
						deadtime = 2 * j;
					}
				}
			}
			tmp.add(deadtime);
		}
		return tmp;
	}
    
    //特定playerのdeadRank値の取得
    final int deadRankPlayer(player pl) {
    	List<Integer> dead = deadRank();
    	for(int i = 0; i < getPlayerList().size(); i++) {
    		if(getPlayerList().get(i) == pl) {
    			return dead.get(i);
    		}
    	}
    	return 0;
    }
    
    //CO役職の取得
    final String getCOrole(player pl) {
    	for(seerCO seer : seerCOList) {
    		if(seer.getSeerCOpl() == pl) {
    			return "占";
    		}
    	}
    	for(mediumCO medium : mediumCOList) {
    		if(medium.getMediumCOpl() == pl) {
    			return "霊";
    		}
    	}
    	for(bodyguardCO bodyguard : bodyguardCOList) {
    		if(bodyguard.getBodyguardCOpl() == pl) {
    			return "狩";
    		}
    	}
    	if(freemasonCOList.getFreemasonCOpl().contains(pl)) {
    		return "共";
    	}
    	return "";
    }
    
    //特定日数の昼の生存プレイヤー一覧の取得
    final List<player> dayAliveList(int day) {
    	List<player> alive = new ArrayList<>();
    	List<Integer> rank = deadRank();
    	for(int i = 0; i < getPlayerList().size(); i++) {
    		if(2 * day <= rank.get(i)) {
    			alive.add(getPlayerList().get(i));
    		}
    	}
    	return alive;
    }
    
    //特定日数の夜の生存プレイヤー一覧の取得
    final List<player> nightAliveList(int day) {
    	List<player> alive = new ArrayList<>();
    	List<Integer> rank = deadRank();
    	for(int i = 0; i < getPlayerList().size(); i++) {
    		if(2 * day - 1 <= rank.get(i)) {
    			alive.add(getPlayerList().get(i));
    		}
    	}
    	return alive;
    }
    
    //同時死亡に対象役職は何人いるか？
    final int sameRankSelect(final int rank, final String roleStr, final String[][] data) {
    	List<Integer> tmp = deadRank();
    	int count = 0;
    	for(int i = 0; i < tmp.size(); i++) {
    		if(tmp.get(i) == rank) {
    			//System.out.println(rank + getPlayerList().get(i).getName() + ", " + roleStr);
    			for(int j = 0; j < getRoleCom().getTotal(); j++) {
    				if(getRoleCom().getRoleLabel().get(j).equals(roleStr)) {
    					if(!data[i][j].equals("×")) {
    						count++;
    						//System.out.println(getPlayerList().get(i).getName() + rank);
    						break;
    					}
    				}
    			}
    		}
    	}
    	//System.out.println(count + "**" + rank);
    	return count;
    }
    final int sameRankSelect(final int rank, final Role role, final String[][] data) {
    	List<Integer> tmp = deadRank();
    	int count = 0;
    	for(int i = 0; i < tmp.size(); i++) {
    		if(tmp.get(i) == rank) {
    			//System.out.println(rank + getPlayerList().get(i).getName() + ", " + roleStr);
    			for(int j = 0; j < getRoleCom().getTotal(); j++) {
    				if(getRoleCom().getRoleLabel().get(j).equals(roles.roleName(role, false))) {
    					if(!data[i][j].equals("×")) {
    						count++;
    						//System.out.println(getPlayerList().get(i).getName() + rank);
    						break;
    					}
    				}
    			}
    		}
    	}
    	//System.out.println(count + "**" + rank);
    	return count;
    }
    
    //入力データの出力
    final void printStatus() {
    	System.out.print("配役 : ");
    	int num = 0;
    	for(int i = 0; i < 10; i++) {
    		num += roleCom.getRole()[i];
    		if(roleCom.getRole()[i] > 0) {
    			System.out.print(RoleFullStr[i] + ":" +  roleCom.getRole()[i] + " ");
    		}
    	}
    	System.out.print(" (" + num + "人)");
    	
    	System.out.println();
    	System.out.print("player : ");
    	for(int i = 0; i < plList.size(); i++) {
    		System.out.print(plList.get(i).getName());
    		if(lackPlayer == plList.get(i)) {
    			System.out.print("《初》");
    		}
    		if(i < plList.size() - 1) {
    			System.out.print(", ");
    		}
    	}
    	System.out.println("   ");
    	System.out.println();
    	System.out.print("自噛み");
    	if(selfAttack) {
    		System.out.print("可能, ");
    	}
    	else {
    		System.out.print("禁止, ");
    	}
    	System.out.print("噛みなし");
    	if(noAttack) {
    		System.out.print("可能, ");
    	}
    	else {
    		System.out.print("禁止, ");
    	}
    	System.out.print("初日占い");
    	if(firstDivine) {
    		System.out.print("あり, ");
    	}
    	else {
    		System.out.print("なし, ");
    	}
    	if(lackPlayer.getId() == -1) {
    		System.out.print("初日噛み");
        	if(firstAttack) {
        		System.out.print("あり");
        	}
        	else {
        		System.out.print("なし");
        	}
    	}
    	if(!firstAttack) {
    		if(lackPlayer.getId() == -1) {
    			System.out.print(", ");
    		}
    		System.out.print("欠け");
        	if(lackPlayer.getId() != -1) {
        		System.out.print("あり (欠け除外対象役職 : ");
        		if(outLackRole.size() > 0) {
        			for(int i = 0; i < outLackRole.size(); i++) {
        				System.out.print(outLackRole.get(i));
        				if(i < outLackRole.size() - 1) {
        					System.out.print(", ");
        				}
        			}
        		}
        		else {
        			System.out.print("なし");
        		}
        		System.out.print(" / 占い師欠けの初日占い : ");
        		if(seerLackFirstDivine) {
        			System.out.print("あり)");
        		}
        		else {
        			System.out.print("なし)");
        		}
        	}
        	else {
        		System.out.print("なし");
        	}
    	}
    	if(firstAttack || lackPlayer.getId() != -1) {
    		System.out.println();
    		System.out.print("潜伏除外対象役職 : ");
    		if(outHideRole.size() > 0) {
    			for(int i = 0; i < outHideRole.size(); i++) {
    				System.out.print(outHideRole.get(i));
    				if(i < outHideRole.size() - 1) {
    					System.out.print(", ");
    				}
    			}
    		}
    		else {
    			System.out.print("なし");
    		}
    	}
    	System.out.println();
    	System.out.println();
    	
    	for(int i = 0; i < seerCOList.size(); i++) {
    		System.out.print("[占い師CO] " + seerCOList.get(i).getSeerCOpl().getName());
    		int firstDivineDay = 1;
    		if(!firstDivine) {
    			firstDivineDay = 2;
    		}
    		for(int j = firstDivineDay; j < seerCOList.get(i).getDivineList().size(); j++) {
    			System.out.print("→");
    			System.out.print(seerCOList.get(i).getDivineList().get(j).getPl().getName());
    			if(seerCOList.get(i).getDivineList().get(j).getPl().getId() != -1) {
    				if(seerCOList.get(i).getDivineList().get(j).getIsWerewolf()) {
        				System.out.print("●【" + j + "d】");
        			}
        			else {
        				System.out.print("○【" + j + "d】");
        			}
    			}
    			else {
    				System.out.print("【" + j + "d】");
    			}
    		}
    		System.out.println(" (" + seerCOList.get(i).getCOday() + "日目にCO)");
    	}
    	for(int i = 0; i < mediumCOList.size(); i++) {
    		System.out.print("[霊能者CO] " + mediumCOList.get(i).getMediumCOpl().getName());
    		for(int j = 1; j < mediumCOList.get(i).getSenseList().size(); j++) {
    			System.out.print("→");
    			if(expelleds.get(j).getId() != -1) {
    				System.out.print(expelleds.get(j).getName());
    				if(mediumCOList.get(i).getSenseList().get(j).getPl().getId() != -2) {
    					if(mediumCOList.get(i).getSenseList().get(j).getIsWerewolf()) {
            				System.out.print("●【" + j + "d吊】");
            			}
            			else {
            				System.out.print("○【" + j + "d吊】");
            			}
    				}
    				else {
    					System.out.print("-【" + j + "d吊】");
    				}
    			}
    			else {
    				System.out.print("×【" + j + "d吊】");
    			}
    		}
    		System.out.println(" (" + mediumCOList.get(i).getCOday() + "日目にCO)");
    	}
    	for(int i = 0; i < bodyguardCOList.size(); i++) {
    		System.out.print("[狩人CO] " + bodyguardCOList.get(i).getBodyguardCOpl().getName());
    		if(firstAttack) {
    			System.out.print("→");
    			System.out.print(bodyguardCOList.get(i).getGuardList().get(1).getName());
    			System.out.print("護衛【1d】");
    		}
    		for(int j = 2; j < bodyguardCOList.get(i).getGuardList().size(); j++) {
    			System.out.print("→");
    			System.out.print(bodyguardCOList.get(i).getGuardList().get(j).getName());
    			System.out.print("護衛【" + j + "d】");
    		}
    		System.out.println(" (" + bodyguardCOList.get(i).getCOday() + "日目にCO)");
    	}
    	if(freemasonCOList.getFreemasonCOpl().size() > 0) {
        	System.out.print("[共有者CO] ");
        	for(int i = 0; i < freemasonCOList.getFreemasonCOpl().size(); i++) {
        		System.out.print(freemasonCOList.getFreemasonCOpl().get(i).getName());
        		if(i < freemasonCOList.getFreemasonCOpl().size() - 1) {
        			System.out.print("=");
        		}
        	}
        	System.out.println();
    	}
    	for(int i = 0; i < toxicCOList.size(); i++) {
    		System.out.print("[埋毒者CO] " + toxicCOList.get(i).getToxicCOpl().getName());
    		System.out.println(" (" + toxicCOList.get(i).getCOday() + "日目にCO)");
    	}
    	System.out.println();
    	if(lackPlayer.getId() != -1) {
    		System.out.print("[初日犠牲者(欠け)] " + lackPlayer.getName());
    		System.out.println();
    		System.out.println();
    		
    	}
    	List<player> deads = new ArrayList<>();
    	System.out.print("[吊り]");
    	for(int i = 1; i < expelleds.size(); i++) {
    		System.out.print("→");
    		System.out.print(expelleds.get(i).getName() + "【" + i + "d】");
    		if(expelleds.get(i).getId() != -1) {
    			deads.add(expelleds.get(i));
    		}
    		if(poisonedDay == i && poisoned.getId() != -1) {
    			System.out.print("/");
    			System.out.print(poisoned.getName());
    			System.out.print("(ランダム毒殺)");
    			deads.add(poisoned);
    		}
    		if(suicideDay == i) {
    			System.out.print("/");
    			for(int j = 0; j < suicides.size(); j++) {
    				System.out.print(suicides.get(j).getName());
    				deads.add(suicides.get(j));
    				if(j < suicides.size() - 1) {
    					System.out.print(",");
    				}
    			}
    			System.out.print("(後追い)");
    		}
    	}
    	//System.out.println(deads.size());
    	System.out.println();
    	System.out.print("[犠牲]");
    	
    	for(int i = 1; i < victims.size(); i++) {
    		if(i == 1 && !firstAttack && firstVictim.size() == 0 && lackPlayer.getId() == -1) {
    			continue;
    		}
    		if(i == 1 && !firstAttack) {
    			System.out.print("→");
    			if(lackPlayer.getId() != -1) {
    				System.out.print(lackPlayer.getName() + "《初》");
    				deads.add(lackPlayer);
    				if(firstVictim.size() > 0) {
    					System.out.print(", ");
    				}
    			}
    			for(int j = 0; j < firstVictim.size(); j++) {
        			System.out.print(firstVictim.get(j).getName());
        			if(firstVictim.get(j).getId() != -1) {
        				deads.add(firstVictim.get(j));
        			}
        			if(j < firstVictim.size() - 1) {
        				System.out.print(", ");
        			}
        		}
    			System.out.print("【" + i + "d】");
    			continue;
    		}
    		System.out.print("→");
    		for(int j = 0; j < victims.get(i).size(); j++) {
    			System.out.print(victims.get(i).get(j).getName());
    			if(victims.get(i).get(j).getId() != -1) {
    				deads.add(victims.get(i).get(j));
    			}
    			if(j < victims.get(i).size() - 1) {
    				System.out.print(", ");
    			}
    		}
    		System.out.print("【" + i + "d】");
    	}
    	System.out.println();
    	System.out.print("[生存]→");
    	int c = 0;
    	for(player pl : getPlayerList()) {
    		if(!deads.contains(pl)) {
    			System.out.print(pl.getName());
    			c++;
    			if(c < getPlayerList().size() - deads.size()) {
        			System.out.print(", ");
        		}
    		}
    	}
    	System.out.println("  " + (getPlayerList().size() - deads.size()) + "人");
    }
    
}

final class createTable {
	final private List<player> plList;
	final private roleCombination roleCom;
	createTable(final List<player> plList, final roleCombination roleCom) {
		List<player> tmp = new ArrayList<>();
		for(int i = 0; i < plList.size(); i++) {
			tmp.add(plList.get(i));
		}
		this.plList = tmp;
		this.roleCom = roleCom;
	}
	
	final String[][] initData() {
		String[][] tmp1 = new String[plList.size()][];
		for(int i = 0; i < plList.size(); i++) {
			String[] tmp2 = new String[roleCom.getTotal()];
			for(int j = 0; j < roleCom.getTotal(); j++) {
				tmp2[j] = "  ";
			}
			tmp1[i] = tmp2;
		}
		return tmp1;
	}
	
	final void printTable(final String[][] data) {
		
		int max_str = 0;
		for(int i = 0; i < plList.size(); i++) {
			if(plList.get(i).getStrSize() > max_str) {
				max_str = plList.get(i).getStrSize();
			}
		}
		
		System.out.print("  ");
		for(int i = 0; i < max_str; i++) {
			System.out.print(" ");
		}
		for(int i = 0; i < roleCom.getTotal(); i++) {
			System.out.print(" | " + roleCom.getRoleLabel().get(i));
		}
		System.out.println(" |");
		System.out.print("---");
		for(int i = 0; i < max_str; i++) {
			System.out.print("-");
		}
		System.out.print("|");
		for(int i = 0; i < roleCom.getTotal(); i++) {
			System.out.print("----|");
		}
		System.out.println();
		for(int i = 0; i < plList.size(); i++) {
			for(int j = 0; j < max_str + 2 - plList.get(i).getStrSize(); j++) {
				System.out.print(" ");
			}
			System.out.print(plList.get(i).getName() + " |");
			for(int j = 0; j < roleCom.getTotal(); j++) {
				System.out.print(" " + data[i][j] + " |");
			}
			System.out.println();
		}
		System.out.print("----");
		for(int i = 0; i < max_str; i++) {
			System.out.print("-");
		}
		for(int i = 0; i < roleCom.getTotal(); i++) {
			System.out.print("-----");
		}
		System.out.println();
	}
	
	final void printTable(final String[][] data, table table1) {
		
		int max_str = 0;
		for(int i = 0; i < plList.size(); i++) {
			if(plList.get(i).getStrSize() > max_str) {
				max_str = plList.get(i).getStrSize();
			}
		}
		
		System.out.print("  ");
		for(int i = 0; i < max_str; i++) {
			System.out.print(" ");
		}
		for(int i = 0; i < roleCom.getTotal(); i++) {
			System.out.print(" | " + roleCom.getRoleLabel().get(i));
		}
		System.out.println(" |");
		System.out.print("---");
		for(int i = 0; i < max_str; i++) {
			System.out.print("-");
		}
		System.out.print("|");
		for(int i = 0; i < roleCom.getTotal(); i++) {
			System.out.print("----|");
		}
		System.out.print("|-----|------|");
		for(seerCO seer : table1.getSeerCOList()) {
			System.out.print("-----");
		}
		System.out.print("|");
		for(mediumCO medium : table1.getMediumCOList()) {
			System.out.print("-----");
		}
		System.out.println("|");
		for(int i = 0; i < plList.size(); i++) {
			for(int j = 0; j < max_str + 2 - plList.get(i).getStrSize(); j++) {
				System.out.print(" ");
			}
			System.out.print(plList.get(i).getName() + " |");
			for(int j = 0; j < roleCom.getTotal(); j++) {
				System.out.print(" " + data[i][j] + " |");
			}
			System.out.print("|");
			String state = "  -  ";
			for(int j = 0; j < table1.getVictims().size(); j++) {
				if(table1.getVictims().get(j).contains(plList.get(i))) {
					state = " " + j + "▲ ";
				}
			}
			for(int j = 0; j < table1.getExpelleds().size(); j++) {
				if(table1.getExpelleds().get(j) == plList.get(i)) {
					state = " " + j + "▼ ";
				}
			}
			if(table1.getPoisoned() == plList.get(i)) {
				state = " " + table1.getPoisonedDay() + "◆ ";
			}
			if(table1.getSuicides().contains(plList.get(i))) {
				state = " " + table1.getSuicideDay() + "★ ";
			}
			if(table1.getFirstVictim().contains(plList.get(i))) {
				state = " 1▲ ";
			}
			if(table1.getLack() == plList.get(i)) {
				state = "[1初]";
			}
			System.out.print(state + "|");
			state = "      ";
			for(seerCO seer : table1.getSeerCOList()) {
				if(seer.getSeerCOpl() == plList.get(i)) {
					state = " 占co ";
				}
			}
			for(mediumCO medium : table1.getMediumCOList()) {
				if(medium.getMediumCOpl() == plList.get(i)) {
					state = " 霊co ";
				}
			}
			for(bodyguardCO bodyguard : table1.getBodyguardCOList()) {
				if(bodyguard.getBodyguardCOpl() == plList.get(i)) {
					state = " 狩co ";
				}
			}
			if(table1.getFreemasonCOList().getFreemasonCOpl().contains(plList.get(i))) {
				state = " 共co ";
			}
			for(toxicCO toxic : table1.getToxicCOList()) {
				if(toxic.getToxicCOpl() == plList.get(i)) {
					state = " 埋co ";
				}
			}
			System.out.print(state + "|");
			int n = 0;
			final String[] alp = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n"};
			for(seerCO seer : table1.getSeerCOList()) {
				state = "  -  ";
				if(seer.getSeerCOpl() == plList.get(i)) {
					state = "[占" + alp[n] + "]";
				}
				for(int j = 0; j < seer.getDivineList().size(); j++) {
					if(seer.getDivineList().get(j).getPl() == plList.get(i)) {
						state = " " + j;
						if(seer.getDivineList().get(j).getIsWerewolf()) {
							state = state + "● ";
						}
						else {
							state = state + "○ ";
						}
					}
				}
				System.out.print(state);
				n++;
			}
			System.out.print("|");
			n = 0;
			for(mediumCO medium : table1.getMediumCOList()) {
				state = "  -  ";
				if(medium.getMediumCOpl() == plList.get(i)) {
					state = "[霊" + alp[n] + "]";
				}
				for(int j = 0; j < medium.getSenseList().size(); j++) {
					if(table1.getExpelleds().get(j) == plList.get(i)) {
						state = " " + j;
						if(medium.getSenseList().get(j).getIsWerewolf()) {
							state = state + "● ";
						}
						else {
							state = state + "○ ";
						}
					}
				}
				System.out.print(state);
				n++;
			}
			System.out.println("|");
		}
		System.out.print("----");
		for(int i = 0; i < max_str; i++) {
			System.out.print("-");
		}
		for(int i = 0; i < roleCom.getTotal(); i++) {
			System.out.print("-----");
		}
		System.out.print("--------------");
		for(seerCO seer : table1.getSeerCOList()) {
			System.out.print("-----");
		}
		for(mediumCO medium : table1.getMediumCOList()) {
			System.out.print("-----");
		}
		System.out.println("--");
	}
	
}


final class playerList {
	final private List<player> plList;
	playerList(final List<player> plList) {
		List<player> tmp = new ArrayList<>();
		for(int i = 0; i < plList.size(); i++) {
			tmp.add(plList.get(i));
		}
		this.plList = tmp;
	}
	
	final List<player> getPlList() {
		List<player> tmp = new ArrayList<>();
		for(int i = 0; i < plList.size(); i++) {
			tmp.add(plList.get(i));
		}
		return tmp;
	}
}


final class roleCombination {
    final private int[] combination;
    final private int totalNum;
    roleCombination(final int[] roles) {
        int[] tmp = new int[10];
        int total = 0;
        for(int i = 0; i < roles.length; i++) {
            tmp[i] = roles[i];
            total += roles[i];
        }
        combination = tmp;
        totalNum = total;
    }
    final int[] getRole() {
    	int[] tmp = new int[10];
    	for(int i = 0; i < combination.length; i++) {
            tmp[i] = combination[i];
        }
    	return tmp;
    }
    final int getTotal() {
    	return totalNum;
    }
    final List<String> getRoleLabel() {
    	List<String> roleLabel = new ArrayList<>();
    	for(int i = 0; i < combination[0]; i++) {
    		 roleLabel.add("村");
    	}
    	for(int i = 0; i < combination[1]; i++) {
    		roleLabel.add("占");
   	    }
    	for(int i = 0; i < combination[2]; i++) {
    		roleLabel.add("霊");
   	    }
    	for(int i = 0; i < combination[3]; i++) {
    		roleLabel.add("狩");
   		 }
    	for(int i = 0; i < combination[4]; i++) {
      		 roleLabel.add("共");
         }
    	for(int i = 0; i < combination[5]; i++) {
     		 roleLabel.add("狼");
        }
    	for(int i = 0; i < combination[6]; i++) {
     		 roleLabel.add("狂");
        }
    	for(int i = 0; i < combination[7]; i++) {
     		 roleLabel.add("狐");
        }
    	for(int i = 0; i < combination[8]; i++) {
     		 roleLabel.add("背");
        }
    	for(int i = 0; i < combination[9]; i++) {
    		 roleLabel.add("埋");
       }
    	return roleLabel;
    }
}



final class player {
    final private String name;
    final private int id;
    player(final String name, final int id) {
        this.name = name;
        this.id = id;
    }
    
    final String getName() {
    	return name;
    }
    
    final int getId() {
    	return id;
    }
    
    final int getStrSize() {
    	return (name.length() + name.getBytes(StandardCharsets.UTF_8).length) / 2;
    }
    
}

//結果
final class result {
    final private player pl;
    final private boolean isWerewolf;
    result(final player pl, final boolean isWerewolf) {
        this.pl = pl;
        this.isWerewolf = isWerewolf;
    }
    final player getPl() {
    	return pl;
    }
    final boolean getIsWerewolf() {
    	return isWerewolf;
    }
}

//占い師co
final class seerCO {
    final private player COplayer;
    final private List<result> divined;
    final private int COday;
    seerCO(final player COplayer, final List<result> divineds, final int COday) {
        this.COplayer = COplayer;
        List<result> tmp = new ArrayList<>();
        for(int i = 0; i < divineds.size(); i++) {
            tmp.add(divineds.get(i));
        }
        divined = tmp;
        this.COday = COday;
    }
    final player getSeerCOpl() {
    	return COplayer;
    }
    final int getCOday() {
    	return COday;
    }
    final List<result> getDivineList() {
    	List<result> tmp = new ArrayList<>();
        for(int i = 0; i < divined.size(); i++) {
            tmp.add(divined.get(i));
        }
        return tmp;
    }
    
    final List<player> getDivineTarget() {
    	List<player> tmp = new ArrayList<>();
    	for(int i = 0; i < divined.size(); i++) {
            tmp.add(divined.get(i).getPl());
        }
    	return tmp;
    }
    
}

//霊能者co
final class mediumCO {
    final private player COplayer;
    final private List<result> sense;
    final private int COday;
    mediumCO(final player COplayer, final List<result> senses, final int COday) {
        this.COplayer = COplayer;
        List<result> tmp = new ArrayList<>();
        for(int i = 0; i < senses.size(); i++) {
            tmp.add(senses.get(i));
        }
        sense = tmp;
        this.COday = COday;
    }
    final player getMediumCOpl() {
    	return COplayer;
    }
    final int getCOday() {
    	return COday;
    }
    final List<result> getSenseList() {
    	List<result> tmp = new ArrayList<>();
        for(int i = 0; i < sense.size(); i++) {
            tmp.add(sense.get(i));
        }
        return tmp;
    }
}

//狩人co
final class bodyguardCO {
    final private player COplayer;
    final private List<player> guard;
    final private int COday;
    bodyguardCO(final player COplayer, final List<player> guards, final int COday) {
        this.COplayer = COplayer;
        List<player> tmp = new ArrayList<>();
        for(int i = 0; i < guards.size(); i++) {
            tmp.add(guards.get(i));
        }
        guard = tmp;
        this.COday = COday;
    }
    final player getBodyguardCOpl() {
    	return COplayer;
    }
    final int getCOday() {
    	return COday;
    }
    final List<player> getGuardList() {
    	List<player> tmp = new ArrayList<>();
        for(int i = 0; i < guard.size(); i++) {
            tmp.add(guard.get(i));
        }
        return tmp;
    }
}

//共有者co(騙りなし)
final class freemasonCO {
    final private List<player> COplayer;
    freemasonCO(final List<player> COplayer) {
        List<player> tmp = new ArrayList<>();
        for(int i = 0; i < COplayer.size(); i++) {
            tmp.add(COplayer.get(i));
        }
        this.COplayer = tmp;
    }
    final List<player> getFreemasonCOpl() {
    	List<player> tmp = new ArrayList<>();
        for(int i = 0; i < COplayer.size(); i++) {
            tmp.add(COplayer.get(i));
        }
        return tmp;
    }
}

//埋毒者co
final class toxicCO {
	final private player COplayer;
	final private int COday;
	toxicCO(final player COplayer, final int COday) {
        this.COplayer = COplayer;
        this.COday = COday;
    }
	final player getToxicCOpl() {
    	return COplayer;
    }
	final int getCOday() {
    	return COday;
    }
}
