package org.aiwolf.sample.player.arrange_tool;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

final public class readData {
	final table table1;
	private boolean selfAttack = false;
	private boolean noAttack = false;
	private boolean firstDivine = true;
	private boolean firstAttack = false;
	private boolean seerLackFirstDivine = true;
	private boolean[] posHide = {true, true, true};
	private player lackPl;
	private player notPlayer;
	private int rank = 0;
	private boolean[] LackRole;
	private int[] rolesNum;
	
 	public readData(String readfile) {
		String data = read(readfile);
		table table1 = input(data);
		this.table1 = table1;
		boolean err = isCorrect();
		if(err) {
			//System.exit(1);
		}
		//table1.printStatus();
	}
	
	final table getTable() {
		return table1;
	}
	
	public final createTable getCreateTable() {
		return new createTable(table1.getPlayerList(), table1.getRoleCom());
	}
	
	private final String read(String text) {
		 String dataStr = "";
		 try {
			 String fname = "src/org/aiwolf/sample/player/arrange_tool/data/" + text + ".txt";
	        //Fileクラスに読み込むファイルを指定する
	        //File file = new File(text + ".txt");
			 File file = new File(fname);
			 
	        //ファイルが存在するか確認する
	        if(file.exists()) {
	            //FileReaderクラスのオブジェクトを生成する
	            FileReader filereader = new FileReader(file);
	                
	            //filereaderクラスのreadメソッドでファイルを1文字ずつ読み込む
	            int data;
	            while((data = filereader.read()) != -1) {
	                //System.out.print((char) data);
	                dataStr = dataStr + (char)data;
	            }
	            //ファイルクローズ
	            filereader.close();
	        } 
	        else {
	            System.out.print("ファイルは存在しません");
	        }
	    }
		catch (IOException e) {
	        e.printStackTrace();
	    }
		 
		 return dataStr;
	}
	
	private final table input(String data) {
		String tmp = data;
		player notPl = new player("×", -1);         //空のプレイヤーをセット
		notPlayer = notPl;
		List<player> players = new ArrayList<>();       //
		players.add(notPl);                             //
		lackPl = notPl;                                 //
		List<List<player>> victims = new ArrayList<>(); //
		List<player> victim0 = new ArrayList<>();       //
		victim0.add(notPl);  victims.add(victim0);      //
		List<player> expelleds = new ArrayList<>();     //
		expelleds.add(notPl);
		int suicideDay = 0;
		List<player> suicides = new ArrayList<>();
		int poisonedDay = 0;
		List<player> poisoned = new ArrayList<>();
		poisoned.add(notPl);
		boolean[] Lack = {true, true, true, true, true, true, true, true, true, true};
		boolean isWriteLack = false;
		List<player> firstVictim = new ArrayList<>();
		List<seerCO> seerCOList = new ArrayList<>();
		List<mediumCO> mediumCOList = new ArrayList<>();
		List<bodyguardCO> bodyguardCOList = new ArrayList<>();
		List<player> freemason = new ArrayList<>();
		List<toxicCO> toxicCOList = new ArrayList<>();
		int[] roles = {0,0,0,0,0,0,0,0,0,0};
		while(tmp.length() != 0) {
		    while(tmp.startsWith(" ") || tmp.startsWith("\n") || tmp.startsWith("\t") || tmp.startsWith("　")) {
		    	tmp = tmp.substring(1);
		    }
		    //System.out.println("^[" + tmp.charAt(0) + "]");
			String label = "";
			while(!tmp.startsWith("=") && tmp.length() != 0) {
				label = label + tmp.charAt(0);
				tmp = tmp.substring(1);
			}
			label = label.trim();
			//System.out.println("label : " + label);
			if(label.matches("^//.*")) {
				//System.out.println("^^^^^^ " + label);
				while(!tmp.startsWith("\n") && tmp.length() != 0) {
					tmp = tmp.substring(1);
				}
			}
			//配役の読み込み
			else if(label.equals("role")) {
				tmp = inputRole(tmp, roles);
				/*
				System.out.print("role = [");
				for(int i = 0; i < 9; i++) {
					System.out.print(roles[i] + ", ");
				}
				System.out.println("]");
				*/
			}
			else if(label.equals("player")) {
				tmp = inputPlayer(tmp, players);
				/*
				System.out.print("player = [");
				for(int i = 0 ; i < players.size(); i++) {
					System.out.print(players.get(i).getName() + " : " + players.get(i).getId());
					if(i < players.size() - 1) {
						System.out.print(", ");
					}
				}
				System.out.println(" ]");
				*/
			}
			else if(label.equals("selfAttack")) {
				tmp = inputselfAttack(tmp);
			}
			else if(label.equals("noAttack")) {
				tmp = inputnoAttack(tmp);
			}
			else if(label.equals("firstDivine")) {
				tmp = inputfirstDivine(tmp);
			}
			else if(label.equals("firstAttack")) {
				tmp = inputfirstAttack(tmp);
			}
			else if(label.equals("firstVictim")) {
				//この機能は初日噛みなしで初日呪殺が起きたときに使う。
				tmp = inputfirstVictim(tmp, firstVictim, players);
			}
			else if(label.equals("seerLackFirstDivine")) {
				//占い師が欠けていた場合にその欠けた占い師が初日に占いを行っているか？
				tmp = inputseerLackFirstDivine(tmp);
			}
			else if(label.equals("lackPlayer")) {
				tmp = inputlackPlayer(tmp, players);
			}
			else if(label.equals("outLackRole")) {
				tmp = inputOutLackRole(tmp, Lack);
				//System.out.println("++++");
				isWriteLack = true;
			}
			else if(label.equals("outHideRole")) {
				tmp = inputOutHideRole(tmp, posHide);
			}
			else if(label.equals("victim")) {
				if(!firstAttack) {
					victims.add(victim0);
				}
				tmp = inputVictims(tmp, victims, players);
				/*
				System.out.print("victims = [");
				for(int i = 2 ; i < victims.size(); i++) {
					System.out.print(" {");
					for(int j = 0; j < victims.get(i).size(); j++) {
						System.out.print(victims.get(i).get(j).getName());
						if(j < victims.get(i).size() - 1) {
							System.out.print(", ");
						}
					}
					System.out.print("}");
					if(i < victims.size() - 1) {
						System.out.print(",");
					}
				}
				System.out.println(" ]");
				*/
			}
			else if(label.equals("expelled")) {
				tmp = inputExpelleds(tmp, expelleds, players);
				/*
				System.out.print("expelleds = [ ");
				for(int i = 1; i < expelleds.size(); i++) {
					System.out.print(expelleds.get(i).getName());
					if(i < expelleds.size() - 1) {
						System.out.print(", ");
					}
				}
				System.out.println(" ]");
				*/
			}
			else if(label.equals("suicide")) {
				tmp = inputSuicides(tmp, suicides, players);
				/*
				System.out.print("suicides = [ ");
				for(int i = 0; i < suicides.size(); i++) {
					System.out.print(suicides.get(i).getName());
					if(i < suicides.size() - 1) {
						System.out.print(", ");
					}
				}
				System.out.println(" ]");
				*/
			}
			else if(label.equals("suicideDay")) {
				List<Integer> suicideBox = new ArrayList<>();
				tmp = inputSuicideDay(tmp, suicideBox);
				suicideDay = suicideBox.get(0);
				//System.out.println(suicideDay);
				
			}
			else if(label.equals("poisoned")) {
				tmp = inputPoisoned(tmp, poisoned, players);
			}
			else if(label.equals("poisonedDay")) {
				List<Integer> poisonedBox = new ArrayList<>();
				tmp = inputPoisonedDay(tmp, poisonedBox);
				poisonedDay =  poisonedBox.get(0);
			}
			else if(label.equals("seerCO")) {
				tmp = inputSeerCO(tmp, seerCOList, players, firstDivine);
			}
			else if(label.equals("mediumCO")) {
				tmp = inputMediumCO(tmp, mediumCOList, players, expelleds);
			}
			else if(label.equals("bodyguardCO")) {
				tmp = inputBodyguardCO(tmp, bodyguardCOList, players, firstAttack);
			}
			else if(label.equals("freemasonCO")) {
				tmp = inputFreemasonCO(tmp, freemason, players);
				
			}
			else if(label.equals("toxicCO")) {
				tmp = inputToxicCO(tmp, toxicCOList, players);
				
			}
			else if(label.equals("") && tmp.length() == 0) {
				break;
			}
			else {
				System.err.println("labelが適切ではありません : " + label);
				System.exit(1);
			}
			if(tmp.length() != 0) {
				tmp = tmp.substring(1);
			}
		}
		freemasonCO freemasonCOList = new freemasonCO(freemason);
		players.remove(0);
		if(!isWriteLack) {
			//System.out.println("*****");
			Lack[7] = false;
		}
		if(firstAttack && lackPl.getId() != -1) {
			System.err.println("初日噛みと欠けを両方ありにはできません。");
			System.exit(1);
		}
		if(firstAttack && firstVictim.size() > 0) {
			System.err.println("初日噛みありでfirstVictimを設定することはできません。");
			System.exit(1);
		}
		if(Lack[7] && roles[8] > 0) {
			System.err.println("背徳者がいる場合は妖狐を欠け除外対象役職にする必要があります。");
			System.exit(1);
		}
		
		//System.out.println(lackPl.getId());
		/*
		for(int i = 0; i < 9; i++) {
			System.out.println(Lack[i] + " " + i);
		}
		//*/
		
		rank = victims.size() + expelleds.size();
		LackRole = Lack;
		rolesNum = roles;
		//System.out.println("****" + rank);
		
		table table1 = new table(players, selfAttack, noAttack, firstDivine, firstAttack, firstVictim, lackPl, Lack, posHide, seerLackFirstDivine, victims, expelleds, suicideDay, suicides, poisonedDay, poisoned, seerCOList, mediumCOList, bodyguardCOList, freemasonCOList, toxicCOList, true, new roleCombination(roles));
		return table1;
	}
	
	private final String inputRole(String tmp, int[] roles) {
		// =, [ をとる
	    tmp = tmp.substring(1);
	    if(!tmp.startsWith("[")) {
	    	System.err.println("labelの値が[で始まっていません : " + tmp.charAt(0));
	    	System.exit(1);
	    }
	    tmp = tmp.substring(1);
	    
	    
		while(!tmp.startsWith(";")) {
			String label = "";
			while(!tmp.startsWith(":")) {
				label = label + tmp.charAt(0);
				tmp = tmp.substring(1);
			}
			tmp = tmp.substring(1);
			String num = "";
			//System.out.println(tmp);
			while(tmp.startsWith("1") || tmp.startsWith("2") || tmp.startsWith("3")
					|| tmp.startsWith("4") || tmp.startsWith("5") || tmp.startsWith("6")
					|| tmp.startsWith("7") || tmp.startsWith("8") || tmp.startsWith("9") || tmp.startsWith("0")) {
				num = num + tmp.charAt(0);
				tmp = tmp.substring(1);
			}
			//System.out.println("num:" + num + "charAt:" + tmp.charAt(0));
			if(num.equals("") || !(tmp.charAt(0) + "").equals(",") && !(tmp.charAt(0) + "").equals("]")) {
				System.err.println("*roleが正しく設定されてません  " + "num:" + num + ", charAt:" + tmp.charAt(0));
            	System.exit(1);
			}
			//System.out.println(label);
			if(label.equals("villager")) {
				roles[0] = Integer.parseInt(num);
			}
			else if(label.equals("seer")) {
				roles[1] = Integer.parseInt(num);
			}
            else if(label.equals("medium")) {
            	roles[2] = Integer.parseInt(num);
			}
            else if(label.equals("bodyguard")) {
            	roles[3] = Integer.parseInt(num);
			}
            else if(label.equals("freemason")) {
            	roles[4] = Integer.parseInt(num);
			}
            else if(label.equals("werewolf")) {
            	roles[5] = Integer.parseInt(num);
			}
            else if(label.equals("fanatic")) {
            	roles[6] = Integer.parseInt(num);
			}
            else if(label.equals("foxspirit")) {
            	roles[7] = Integer.parseInt(num);
			}
            else if(label.equals("immoralist")) {
            	roles[8] = Integer.parseInt(num);
			}
            else if(label.equals("toxic")) {
            	roles[9] = Integer.parseInt(num);
            }
            else {
            	System.err.println("roleが正しく設定されてません  role :" + label);
            	System.exit(1);
            }
			tmp = tmp.substring(1);
		}
		return tmp;
	}
	
	private final String inputOutLackRole(String tmp, boolean[] lack) {
		// =, [ をとる
	    tmp = tmp.substring(1);
	    if(!tmp.startsWith("[")) {
	    	System.err.println("labelの値が[で始まっていません : " + tmp.charAt(0));
	    	System.exit(1);
	    }
	    tmp = tmp.substring(1);
	    while(!tmp.startsWith(";")) {
	    	String label = "";
	    	while(!tmp.startsWith("]") && !tmp.startsWith(",")) {
				label = label + tmp.charAt(0);
				tmp = tmp.substring(1);
			}
			
			final String[] rolestr = {"villager", "seer", "medium", "boodyguard", "freemason", "werewolf", "fanatic", "foxspirit", "immoralist", "toxic"};
			boolean cor = false;
			for(int i = 0; i < 10; i++) {
				if(label.equals(rolestr[i])) {
					lack[i] = false;
					cor = true;
				}
			}
			if(!cor && (!tmp.startsWith("]") || !label.equals(""))) {
				System.err.println("roleLackが正しく設定されてません  roleLack :" + label);
            	System.exit(1);
			}
			tmp = tmp.substring(1);
	    }
		return tmp;
	}
	
	private final String inputOutHideRole(String tmp, boolean[] hide) {
		// =, [ をとる
	    tmp = tmp.substring(1);
	    if(!tmp.startsWith("[")) {
	    	System.err.println("labelの値が[で始まっていません : " + tmp.charAt(0));
	    	System.exit(1);
	    }
	    tmp = tmp.substring(1);
	    while(!tmp.startsWith(";")) {
	    	String label = "";
	    	while(!tmp.startsWith("]") && !tmp.startsWith(",")) {
				label = label + tmp.charAt(0);
				tmp = tmp.substring(1);
			}
			
			final String[] rolestr = {"seer", "medium", "boodyguard"};
			boolean cor = false;
			for(int i = 0; i < 3; i++) {
				if(label.equals(rolestr[i])) {
					hide[i] = false;
					cor = true;
				}
			}
			if(!cor && (!tmp.startsWith("]") || !label.equals(""))) {
				System.err.println("roleHideが正しく設定されてません  roleHide :" + label);
            	System.exit(1);
			}
			tmp = tmp.substring(1);
	    }
		return tmp;
	}
	
	final player getPlayer(int id, List<player> playerList) {
		for(player pl : playerList) {
			if(pl.getId() == id) {
				return pl;
			}
		}
		System.err.println("該当playerがみつかりません getPlayer()" + id);
		System.exit(1);
		return null;
	}
	
	private final String inputPlayer(String tmp, List<player> playerList) {
		// =, [ をとる
	    tmp = tmp.substring(1);
	    if(!tmp.startsWith("[")) {
	    	System.err.println("labelの値が[で始まっていません : " + tmp.charAt(0));
	    	System.exit(1);
	    }
	    tmp = tmp.substring(1);
	    
	    
	    while(!tmp.startsWith(";")) {
			String label = "";
			while(!tmp.startsWith(":")) {
				label = label + tmp.charAt(0);
				tmp = tmp.substring(1);
			}
			tmp = tmp.substring(1);
			String id = "";
			//System.out.println("+++" + tmp.charAt(0));
			while(tmp.startsWith("1") || tmp.startsWith("2") || tmp.startsWith("3")
					|| tmp.startsWith("4") || tmp.startsWith("5") || tmp.startsWith("6")
					|| tmp.startsWith("7") || tmp.startsWith("8") || tmp.startsWith("9") || tmp.startsWith("0")) {
				id = id + tmp.charAt(0);
				tmp = tmp.substring(1);
			}
			//System.out.println(label);
			//System.out.println("id:" + id + "charAt:" + tmp.charAt(0));
			/*
			if(id.equals("") || !(tmp.charAt(0) + "").equals("-")) {
				System.err.println("*playerが正しく設定されてません");
            	System.exit(1);
			}
			tmp = tmp.substring(1);
			String num = "";
			while(tmp.startsWith("1") || tmp.startsWith("2") || tmp.startsWith("3")
					|| tmp.startsWith("4") || tmp.startsWith("5") || tmp.startsWith("6")
					|| tmp.startsWith("7") || tmp.startsWith("8") || tmp.startsWith("9") || tmp.startsWith("0")) {
				num = num + tmp.charAt(0);
				tmp = tmp.substring(1);
			}
			*/
			//System.out.println("num:" + num + "charAt:" + tmp.charAt(0));
			if(id.equals("") || !(tmp.charAt(0) + "").equals(",") && !(tmp.charAt(0) + "").equals("]")) {
				System.err.println("*playerが正しく設定されてません" + tmp.charAt(0));
            	System.exit(1);
			}
			playerList.add(new player(label, Integer.parseInt(id)));
			tmp = tmp.substring(1);
	    }
		return tmp;
	}
	
	private final String inputselfAttack(String tmp) {
		tmp = tmp.substring(1);
		String self = "";
		while(!tmp.startsWith(";")) {
			self = self + tmp.charAt(0);
			tmp = tmp.substring(1);
		}
		if(self.equals("true")) {
			selfAttack = true;
		}
		else if(self.equals("false")) {
			selfAttack = false;
		}
		else {
			System.err.println("正しく設定されてません" + "selfAttack:" + self + "  charAt:" + tmp.charAt(0));
        	System.exit(1);
		}
		tmp = tmp.substring(1);
		return tmp;
	}
	
	private final String inputnoAttack(String tmp) {
		tmp = tmp.substring(1);
		String self = "";
		while(!tmp.startsWith(";")) {
			self = self + tmp.charAt(0);
			tmp = tmp.substring(1);
		}
		if(self.equals("true")) {
			noAttack = true;
		}
		else if(self.equals("false")) {
			noAttack = false;
		}
		else {
			System.err.println("正しく設定されてません" + "noAttack:" + self + "  charAt:" + tmp.charAt(0));
        	System.exit(1);
		}
		tmp = tmp.substring(1);
		return tmp;
	}
	
	private final String inputfirstDivine(String tmp) {
		tmp = tmp.substring(1);
		String self = "";
		while(!tmp.startsWith(";")) {
			self = self + tmp.charAt(0);
			tmp = tmp.substring(1);
		}
		if(self.equals("true")) {
			firstDivine = true;
		}
		else if(self.equals("false")) {
			firstDivine = false;
		}
		else {
			System.err.println("正しく設定されてません" + "firstDivine:" + self + "  charAt:" + tmp.charAt(0));
        	System.exit(1);
		}
		tmp = tmp.substring(1);
		return tmp;
	}
	
	private final String inputfirstAttack(String tmp) {
		tmp = tmp.substring(1);
		String self = "";
		while(!tmp.startsWith(";")) {
			self = self + tmp.charAt(0);
			tmp = tmp.substring(1);
		}
		if(self.equals("true")) {
			firstAttack = true;
		}
		else if(self.equals("false")) {
			firstAttack = false;
		}
		else {
			System.err.println("正しく設定されてません" + "roleLack:" + self + "  charAt:" + tmp.charAt(0));
        	System.exit(1);
		}
		tmp = tmp.substring(1);
		return tmp;
	}
	
	private final String inputseerLackFirstDivine(String tmp) {
		tmp = tmp.substring(1);
		String self = "";
		while(!tmp.startsWith(";")) {
			self = self + tmp.charAt(0);
			tmp = tmp.substring(1);
		}
		if(self.equals("true")) {
			seerLackFirstDivine = true;
		}
		else if(self.equals("false")) {
			seerLackFirstDivine = false;
		}
		else {
			System.err.println("正しく設定されてません" + "seerLackFirstDivine:" + self + "  charAt:" + tmp.charAt(0));
        	System.exit(1);
		}
		tmp = tmp.substring(1);
		return tmp;
	}
	
	private final String inputfirstVictim(String tmp, List<player> firstVictim, List<player> playerList) {
		if(firstAttack) {
			System.err.println("初日噛みあり設定ではこの機能は使えません。");
        	System.exit(1);
		}
		// =, [ をとる
	    tmp = tmp.substring(1);
	    if(!tmp.startsWith("[")) {
	    	System.err.println("labelの値が[で始まっていません : " + tmp.charAt(0));
	    	System.exit(1);
	    }
	    tmp = tmp.substring(1);
	    
	    while(!tmp.startsWith(";")) {
	    	String id = "";
			while(tmp.startsWith("1") || tmp.startsWith("2") || tmp.startsWith("3")
					|| tmp.startsWith("4") || tmp.startsWith("5") || tmp.startsWith("6")
					|| tmp.startsWith("7") || tmp.startsWith("8") || tmp.startsWith("9") || tmp.startsWith("0")) {
				id = id + tmp.charAt(0);
				tmp = tmp.substring(1);
			}
			//System.out.println("id:" + id + "   charAt:" + tmp.charAt(0));
			if(!(tmp.charAt(0) + "").equals(",") && !(tmp.charAt(0) + "").equals("]")) {
				System.err.println("firstVictimが正しく設定されてません" + "id:" + id + "  charAt:" + tmp.charAt(0));
            	System.exit(1);
			}
			firstVictim.add(getPlayer(Integer.parseInt(id), playerList));
			tmp = tmp.substring(1);
	    }
		
		return tmp;
	}
	
	private final String inputlackPlayer(String tmp, List<player> playerList) {
		// =をとる
	    tmp = tmp.substring(1);
	    String id = "";
	    while(tmp.startsWith("1") || tmp.startsWith("2") || tmp.startsWith("3")
				|| tmp.startsWith("4") || tmp.startsWith("5") || tmp.startsWith("6")
				|| tmp.startsWith("7") || tmp.startsWith("8") || tmp.startsWith("9") || tmp.startsWith("0")) {
			id = id + tmp.charAt(0);
			tmp = tmp.substring(1);
		}
	    if(!(tmp.charAt(0) + "").equals(";") || id.equals("")) {
	    	System.err.println("欠けプレイヤーが正しく設定されてません" + "id:" + id + "  charAt:" + tmp.charAt(0));
        	System.exit(1);
	    }
	    lackPl = getPlayer(Integer.parseInt(id), playerList);
		return tmp;
	}
	
	private final String inputVictims(String tmp, List<List<player>> victimList, List<player> playerList) {
		// =, [ をとる
	    tmp = tmp.substring(1);
	    if(!tmp.startsWith("[")) {
	    	System.err.println("labelの値が[で始まっていません : " + tmp.charAt(0));
	    	System.exit(1);
	    }
	    tmp = tmp.substring(1);
	    if(tmp.startsWith("]")) {
	    	tmp = tmp.substring(1);
	    	return tmp;
	    }
	    
	    while(!tmp.startsWith(";")) {
	    	if(!tmp.startsWith("{") && !tmp.startsWith("]")) {
		    	System.err.println("犠牲者の値が{で始まっていません : " + tmp.charAt(0));
		    	System.exit(1);
		    }
	    	if(tmp.startsWith("]")) {
	    		tmp = tmp.substring(1);
	    		if(tmp.startsWith(";")) {
	    			return tmp;
	    		}
	    		else {
	    			System.err.println("犠牲者の値が不適切です : " + tmp.charAt(0));
			    	System.exit(1);
	    		}
	    	}
	    	List<player> dayVic = new ArrayList<>();
	    	while(!tmp.startsWith("}")) {
	    		tmp = tmp.substring(1);
				String id = "";
				//System.out.println("+++" + tmp.charAt(0));
				while(tmp.startsWith("1") || tmp.startsWith("2") || tmp.startsWith("3")
						|| tmp.startsWith("4") || tmp.startsWith("5") || tmp.startsWith("6")
						|| tmp.startsWith("7") || tmp.startsWith("8") || tmp.startsWith("9") || tmp.startsWith("0")) {
					id = id + tmp.charAt(0);
					tmp = tmp.substring(1);
				}
				//System.out.println("id:" + id + "   charAt:" + tmp.charAt(0));
				if(!(tmp.charAt(0) + "").equals("}") && !(tmp.charAt(0) + "").equals(",")) {
					System.err.println("victimが正しく設定されてません" + "id:" + id + "  charAt:" + tmp.charAt(0));
	            	System.exit(1);
				}
				if(!id.equals("")) {
					dayVic.add(getPlayer(Integer.parseInt(id), playerList));
				}
				if((tmp.charAt(0) + "").equals("}") && dayVic.size() == 0) {
					dayVic.add(playerList.get(0));
				}
				//tmp = tmp.substring(1);
	    	}
	    	victimList.add(dayVic);
	    	tmp = tmp.substring(1);
	    	if(!tmp.startsWith(",") && !tmp.startsWith("]")) {
		    	System.err.println("犠牲者の値が,か]で始まっていません : " + tmp.charAt(0));
		    	System.exit(1);
		    }
	    	tmp = tmp.substring(1);
	    	//System.out.println("   charAt:" + tmp.charAt(0));
	    }
	    
		return tmp;
	}
	
	private final String inputExpelleds(String tmp, List<player> expelledList, List<player> playerList) {
		// =, [ をとる
	    tmp = tmp.substring(1);
	    if(!tmp.startsWith("[")) {
	    	System.err.println("labelの値が[で始まっていません : " + tmp.charAt(0));
	    	System.exit(1);
	    }
	    tmp = tmp.substring(1);
	    if(tmp.startsWith("]")) {
	    	tmp = tmp.substring(1);
	    	return tmp;
	    }
	    
	    while(!tmp.startsWith(";")) {
	    	String id = "";
	    	if(tmp.startsWith("-")) {
	    		id = "-1";
	    		tmp = tmp.substring(1);
	    	}
	    	else {
	    		while(tmp.startsWith("1") || tmp.startsWith("2") || tmp.startsWith("3")
						|| tmp.startsWith("4") || tmp.startsWith("5") || tmp.startsWith("6")
						|| tmp.startsWith("7") || tmp.startsWith("8") || tmp.startsWith("9") || tmp.startsWith("0")) {
					id = id + tmp.charAt(0);
					tmp = tmp.substring(1);
				}
	    	}
			//System.out.println("id:" + id + "   charAt:" + tmp.charAt(0));
			if(!(tmp.charAt(0) + "").equals(",") && !(tmp.charAt(0) + "").equals("]")) {
				System.err.println("expelledが正しく設定されてません" + "id:" + id + "  charAt:" + tmp.charAt(0));
            	System.exit(1);
			}
			expelledList.add(getPlayer(Integer.parseInt(id), playerList));
			tmp = tmp.substring(1);
	    }
		
		return tmp;
	}
	
	private final String inputSuicides(String tmp, List<player> suicideList, List<player> playerList) {
		// =, [ をとる
	    tmp = tmp.substring(1);
	    if(!tmp.startsWith("[")) {
	    	System.err.println("labelの値が[で始まっていません : " + tmp.charAt(0));
	    	System.exit(1);
	    }
	    tmp = tmp.substring(1);
	    
	    while(!tmp.startsWith(";")) {
	    	String id = "";
			while(tmp.startsWith("1") || tmp.startsWith("2") || tmp.startsWith("3")
					|| tmp.startsWith("4") || tmp.startsWith("5") || tmp.startsWith("6")
					|| tmp.startsWith("7") || tmp.startsWith("8") || tmp.startsWith("9") || tmp.startsWith("0")) {
				id = id + tmp.charAt(0);
				tmp = tmp.substring(1);
			}
			//System.out.println("id:" + id + "   charAt:" + tmp.charAt(0));
			if(!(tmp.charAt(0) + "").equals(",") && !(tmp.charAt(0) + "").equals("]")) {
				System.err.println("suicideが正しく設定されてません" + "id:" + id + "  charAt:" + tmp.charAt(0));
            	System.exit(1);
			}
			if(!id.equals("")) {
				suicideList.add(getPlayer(Integer.parseInt(id), playerList));
			}
			tmp = tmp.substring(1);
	    }
		
		return tmp;
	}
	
	private final String inputSuicideDay(String tmp, List<Integer> suicideBox) {
		// =をとる
	    tmp = tmp.substring(1);
    	String day = "";
		while(tmp.startsWith("1") || tmp.startsWith("2") || tmp.startsWith("3")
				|| tmp.startsWith("4") || tmp.startsWith("5") || tmp.startsWith("6")
				|| tmp.startsWith("7") || tmp.startsWith("8") || tmp.startsWith("9") || tmp.startsWith("0")) {
			day = day + tmp.charAt(0);
			tmp = tmp.substring(1);
		}
		//System.out.println("id:" + day + "   charAt:" + tmp.charAt(0));
		if(!day.equals("") && !(tmp.charAt(0) + "").equals(";")) {
			System.err.println("suicideDayが正しく設定されてません" + "id:" + day + "  charAt:" + tmp.charAt(0));
        	System.exit(1);
		}
		suicideBox.add(Integer.parseInt(day));
		return tmp;
	}
	
	private final String inputPoisoned(String tmp, List<player> poisonedList, List<player> playerList) {
		// =をとる
	    tmp = tmp.substring(1);
    	String id = "";
		while(tmp.startsWith("1") || tmp.startsWith("2") || tmp.startsWith("3")
				|| tmp.startsWith("4") || tmp.startsWith("5") || tmp.startsWith("6")
				|| tmp.startsWith("7") || tmp.startsWith("8") || tmp.startsWith("9") || tmp.startsWith("0")) {
			id = id + tmp.charAt(0);
			tmp = tmp.substring(1);
		}
		if(!id.equals("") && !(tmp.charAt(0) + "").equals(";")) {
			System.err.println("poisonedListが正しく設定されてません" + "id:" + id + "  charAt:" + tmp.charAt(0));
        	System.exit(1);
		}
		if(!id.equals("")) {
			poisonedList.add(getPlayer(Integer.parseInt(id), playerList));
		}
		return tmp;
	}
	
	private final String inputPoisonedDay(String tmp, List<Integer> poisonBox) {
		// =をとる
	    tmp = tmp.substring(1);
    	String day = "";
		while(tmp.startsWith("1") || tmp.startsWith("2") || tmp.startsWith("3")
				|| tmp.startsWith("4") || tmp.startsWith("5") || tmp.startsWith("6")
				|| tmp.startsWith("7") || tmp.startsWith("8") || tmp.startsWith("9") || tmp.startsWith("0")) {
			day = day + tmp.charAt(0);
			tmp = tmp.substring(1);
		}
		//System.out.println("id:" + day + "   charAt:" + tmp.charAt(0));
		if(!day.equals("") && !(tmp.charAt(0) + "").equals(";")) {
			System.err.println("suicideDayが正しく設定されてません" + "id:" + day + "  charAt:" + tmp.charAt(0));
        	System.exit(1);
		}
		poisonBox.add(Integer.parseInt(day));
		return tmp;
	}
	
	private final String inputSeerCO(String tmp, List<seerCO> seerList, List<player> playerList, boolean firstDivine) {
		// =, [ をとる
	    tmp = tmp.substring(1);
	    if(!tmp.startsWith("[")) {
	    	System.err.println("labelの値が[で始まっていません : " + tmp.charAt(0));
	    	System.exit(1);
	    }
	    tmp = tmp.substring(1);
	    String id = "";
		while(tmp.startsWith("1") || tmp.startsWith("2") || tmp.startsWith("3")
				|| tmp.startsWith("4") || tmp.startsWith("5") || tmp.startsWith("6")
				|| tmp.startsWith("7") || tmp.startsWith("8") || tmp.startsWith("9") || tmp.startsWith("0")) {
			id = id + tmp.charAt(0);
			tmp = tmp.substring(1);
		}
		//System.out.println("id:" + id + "   charAt:" + tmp.charAt(0));
		if(id.equals("") || !(tmp.charAt(0) + "").equals(",")) {
			System.err.println("seerCO-playerが正しく設定されてません" + "id:" + id + "  charAt:" + tmp.charAt(0));
        	System.exit(1);
		}
		
		player seerCOplayer = getPlayer(Integer.parseInt(id), playerList);
		tmp = tmp.substring(1);
		if(!tmp.startsWith("{")) {
	    	System.err.println("seerCOListが{で始まっていません : " + tmp.charAt(0));
	    	System.exit(1);
	    }
		List<result> seerResult = new ArrayList<>();
		seerResult.add(new result(playerList.get(0), false));
		if(!firstDivine) {
			seerResult.add(new result(playerList.get(0), false));
		}
		while(!tmp.startsWith("}")) {
			tmp = tmp.substring(1);
			id = "";
			player tar;
			String isWolf = "";
			// 欠損値の処理
			if(tmp.startsWith("-")) {
	    		id = "-1";
	    		isWolf = "false";
	    		tmp = tmp.substring(1);
	    	}
			else {
				while(tmp.startsWith("1") || tmp.startsWith("2") || tmp.startsWith("3")
						|| tmp.startsWith("4") || tmp.startsWith("5") || tmp.startsWith("6")
						|| tmp.startsWith("7") || tmp.startsWith("8") || tmp.startsWith("9") || tmp.startsWith("0")) {
					id = id + tmp.charAt(0);
					tmp = tmp.substring(1);
				}
				//System.out.println("id:" + id + "   charAt:" + tmp.charAt(0));
				if(id.equals("") || !(tmp.charAt(0) + "").equals(":")) {
					System.err.println("占い対象が正しく設定されてません" + "id:" + id + "  charAt:" + tmp.charAt(0));
	            	System.exit(1);
				}
				tmp = tmp.substring(1);
			}
			tar = getPlayer(Integer.parseInt(id), playerList);
			while(!tmp.startsWith(",") && !tmp.startsWith("}")) {
				isWolf = isWolf + tmp.charAt(0);
				tmp = tmp.substring(1);
			}
			if(isWolf.equals("true")) {
				seerResult.add(new result(tar, true));
			}
			else if(isWolf.equals("false")) {
				seerResult.add(new result(tar, false));
			}
			else {
				System.err.println("占い結果が正しく設定されてません" + "isWolf:" + isWolf + "  charAt:" + tmp.charAt(0));
            	System.exit(1);
			}
		}
		tmp = tmp.substring(1);
		if(!tmp.startsWith(",")) {
	    	System.err.println("seerCO-playerが正しく設定されてません : " + tmp.charAt(0));
	    	System.exit(1);
	    }
		tmp = tmp.substring(1);
		String day = "";
		while(tmp.startsWith("1") || tmp.startsWith("2") || tmp.startsWith("3")
				|| tmp.startsWith("4") || tmp.startsWith("5") || tmp.startsWith("6")
				|| tmp.startsWith("7") || tmp.startsWith("8") || tmp.startsWith("9") || tmp.startsWith("0")) {
			day = day + tmp.charAt(0);
			tmp = tmp.substring(1);
		}
		if(day.equals("") || !tmp.startsWith("]")) {
			System.err.println("seerCO-playerが正しく設定されてません " + "day:" + day + "  charAt:" + tmp.charAt(0));
	    	System.exit(1);
		}
		
		seerList.add(new seerCO(seerCOplayer, seerResult, Integer.parseInt(day)));
		tmp = tmp.substring(1);
		
		return tmp;
	}
	
	private final String inputMediumCO(String tmp, List<mediumCO> mediumList, List<player> playerList, List<player> expelledList) {
		// =, [ をとる
	    tmp = tmp.substring(1);
	    if(!tmp.startsWith("[")) {
	    	System.err.println("labelの値が[で始まっていません : " + tmp.charAt(0));
	    	System.exit(1);
	    }
	    tmp = tmp.substring(1);
	    String id = "";
		while(tmp.startsWith("1") || tmp.startsWith("2") || tmp.startsWith("3")
				|| tmp.startsWith("4") || tmp.startsWith("5") || tmp.startsWith("6")
				|| tmp.startsWith("7") || tmp.startsWith("8") || tmp.startsWith("9") || tmp.startsWith("0")) {
			id = id + tmp.charAt(0);
			tmp = tmp.substring(1);
		}
		//System.out.println("id:" + id + "   charAt:" + tmp.charAt(0));
		if(id.equals("") || !(tmp.charAt(0) + "").equals(",")) {
			System.err.println("mediumCO-playerが正しく設定されてません" + "id:" + id + "  charAt:" + tmp.charAt(0));
        	System.exit(1);
		}
		
		player mediumCOplayer = getPlayer(Integer.parseInt(id), playerList);
		tmp = tmp.substring(1);
		if(!tmp.startsWith("{")) {
	    	System.err.println("mediumCOListが{で始まっていません : " + tmp.charAt(0));
	    	System.exit(1);
	    }
		List<result> mediumResult = new ArrayList<>();
		mediumResult.add(new result(playerList.get(0), false));
		int count = 1;
		while(!tmp.startsWith("}")) {
			tmp = tmp.substring(1);
			
			String isWolf = "";
			while(!tmp.startsWith(",") && !tmp.startsWith("}")) {
				isWolf = isWolf + tmp.charAt(0);
				tmp = tmp.substring(1);
			}
			if(isWolf.equals("true")) {
				mediumResult.add(new result(expelledList.get(count), true));
			}
			else if(isWolf.equals("false")) {
				mediumResult.add(new result(expelledList.get(count), false));
			}
			// 欠損値の処理
			else if(isWolf.equals("-")) {
				mediumResult.add(new result(new player("-", -2), false));
			}
			else if(tmp.startsWith("}")) {
				break;
			}
			else {
				System.err.println("霊能結果が正しく設定されてません" + "isWolf:" + isWolf + "  charAt:" + tmp.charAt(0));
            	System.exit(1);
			}
			count++;
		}
		tmp = tmp.substring(1);
		if(!tmp.startsWith(",")) {
	    	System.err.println("mediumCO-playerが正しく設定されてません : " + tmp.charAt(0));
	    	System.exit(1);
	    }
		tmp = tmp.substring(1);
		String day = "";
		while(tmp.startsWith("1") || tmp.startsWith("2") || tmp.startsWith("3")
				|| tmp.startsWith("4") || tmp.startsWith("5") || tmp.startsWith("6")
				|| tmp.startsWith("7") || tmp.startsWith("8") || tmp.startsWith("9") || tmp.startsWith("0")) {
			day = day + tmp.charAt(0);
			tmp = tmp.substring(1);
		}
		if(day.equals("") || !tmp.startsWith("]")) {
			System.err.println("mediumCO-playerが正しく設定されてません " + "day:" + day + "  charAt:" + tmp.charAt(0));
	    	System.exit(1);
		}
		
		mediumList.add(new mediumCO(mediumCOplayer, mediumResult, Integer.parseInt(day)));
		tmp = tmp.substring(1);
		
		return tmp;
	}
	
	private final String inputBodyguardCO(String tmp, List<bodyguardCO> bodyguardList, List<player> playerList, boolean firstAttack) {
		// =, [ をとる
	    tmp = tmp.substring(1);
	    if(!tmp.startsWith("[")) {
	    	System.err.println("labelの値が[で始まっていません : " + tmp.charAt(0));
	    	System.exit(1);
	    }
	    tmp = tmp.substring(1);
	    String id = "";
	    while(tmp.startsWith("1") || tmp.startsWith("2") || tmp.startsWith("3")
				|| tmp.startsWith("4") || tmp.startsWith("5") || tmp.startsWith("6")
				|| tmp.startsWith("7") || tmp.startsWith("8") || tmp.startsWith("9") || tmp.startsWith("0")) {
			id = id + tmp.charAt(0);
			tmp = tmp.substring(1);
		}
		//System.out.println("id:" + id + "   charAt:" + tmp.charAt(0));
		if(id.equals("") || !(tmp.charAt(0) + "").equals(",")) {
			System.err.println("bodyguardCO-playerが正しく設定されてません" + "id:" + id + "  charAt:" + tmp.charAt(0));
        	System.exit(1);
		}
		
		player bodyguardCOplayer = getPlayer(Integer.parseInt(id), playerList);
		tmp = tmp.substring(1);
		if(!tmp.startsWith("{")) {
	    	System.err.println("bodyguardCOListが{で始まっていません : " + tmp.charAt(0));
	    	System.exit(1);
	    }
		List<player> guard = new ArrayList<>();
		guard.add(playerList.get(0)); 
		if(!firstAttack) {
			guard.add(playerList.get(0));
		}
		while(!tmp.startsWith("}")) {
			tmp = tmp.substring(1);
			id = "";
			if(tmp.startsWith("-")) {
	    		id = "-1";
	    		tmp = tmp.substring(1);
	    	}
			else {
				while(tmp.startsWith("1") || tmp.startsWith("2") || tmp.startsWith("3")
						|| tmp.startsWith("4") || tmp.startsWith("5") || tmp.startsWith("6")
						|| tmp.startsWith("7") || tmp.startsWith("8") || tmp.startsWith("9") || tmp.startsWith("0")) {
					id = id + tmp.charAt(0);
					tmp = tmp.substring(1);
				}
			}
			//System.out.println("id:" + id + "   charAt:" + tmp.charAt(0));
			
			if(!tmp.startsWith(",") && !tmp.startsWith("}")) {
				System.err.println("護衛先が正しく設定されてません  charAt:" + tmp.charAt(0));
            	System.exit(1);
			}
			if(!id.equals("")) {
				guard.add(getPlayer(Integer.parseInt(id), playerList));
			}
		}
		tmp = tmp.substring(1);
		if(!tmp.startsWith(",")) {
	    	System.err.println("bodyguardCO-playerが正しく設定されてません : " + tmp.charAt(0));
	    	System.exit(1);
	    }
		tmp = tmp.substring(1);
		String day = "";
		while(tmp.startsWith("1") || tmp.startsWith("2") || tmp.startsWith("3")
				|| tmp.startsWith("4") || tmp.startsWith("5") || tmp.startsWith("6")
				|| tmp.startsWith("7") || tmp.startsWith("8") || tmp.startsWith("9") || tmp.startsWith("0")) {
			day = day + tmp.charAt(0);
			tmp = tmp.substring(1);
		}
		if(day.equals("") || !tmp.startsWith("]")) {
			System.err.println("bodyguardCO-playerが正しく設定されてません " + "day:" + day + "  charAt:" + tmp.charAt(0));
	    	System.exit(1);
		}
		
		bodyguardList.add(new bodyguardCO(bodyguardCOplayer, guard, Integer.parseInt(day)));
		tmp = tmp.substring(1);
		//System.out.println("***" + bodyguardList.size() + ", " + guard.size());
		return tmp;
	}
	
	private final String inputFreemasonCO(String tmp, List<player> freemason, List<player> playerList) {
		// =, [ をとる
	    tmp = tmp.substring(1);
	    if(!tmp.startsWith("[")) {
	    	System.err.println("labelの値が[で始まっていません : " + tmp.charAt(0));
	    	System.exit(1);
	    }
	    tmp = tmp.substring(1);
	    
	    while(!tmp.startsWith(";")) {
	    	String id = "";
			while(tmp.startsWith("1") || tmp.startsWith("2") || tmp.startsWith("3")
					|| tmp.startsWith("4") || tmp.startsWith("5") || tmp.startsWith("6")
					|| tmp.startsWith("7") || tmp.startsWith("8") || tmp.startsWith("9") || tmp.startsWith("0")) {
				id = id + tmp.charAt(0);
				tmp = tmp.substring(1);
			}
			//System.out.println("id:" + id + "   charAt:" + tmp.charAt(0));
			if(!(tmp.charAt(0) + "").equals(",") && !(tmp.charAt(0) + "").equals("]")) {
				System.err.println("freemasonが正しく設定されてません" + "id:" + id + "  charAt:" + tmp.charAt(0));
            	System.exit(1);
			}
			freemason.add(getPlayer(Integer.parseInt(id), playerList));
			tmp = tmp.substring(1);
	    }
		
		return tmp;
	}
	
	private final String inputToxicCO(String tmp, List<toxicCO> toxicList, List<player> playerList) {
		// =, [ をとる
	    tmp = tmp.substring(1);
	    if(!tmp.startsWith("[")) {
	    	System.err.println("labelの値が[で始まっていません : " + tmp.charAt(0));
	    	System.exit(1);
	    }
	    tmp = tmp.substring(1);
	    String id = "";
		while(tmp.startsWith("1") || tmp.startsWith("2") || tmp.startsWith("3")
				|| tmp.startsWith("4") || tmp.startsWith("5") || tmp.startsWith("6")
				|| tmp.startsWith("7") || tmp.startsWith("8") || tmp.startsWith("9") || tmp.startsWith("0")) {
			id = id + tmp.charAt(0);
			tmp = tmp.substring(1);
		}
		//System.out.println("id:" + id + "   charAt:" + tmp.charAt(0));
		if(id.equals("") || !(tmp.charAt(0) + "").equals(",")) {
			System.err.println("toxicCO-playerが正しく設定されてません" + "id:" + id + "  charAt:" + tmp.charAt(0));
        	System.exit(1);
		}
		
		player toxicCOplayer = getPlayer(Integer.parseInt(id), playerList);
		
		if(!tmp.startsWith(",")) {
	    	System.err.println("toxicCO-playerが正しく設定されてません : " + tmp.charAt(0));
	    	System.exit(1);
	    }
		tmp = tmp.substring(1);
		String day = "";
		while(tmp.startsWith("1") || tmp.startsWith("2") || tmp.startsWith("3")
				|| tmp.startsWith("4") || tmp.startsWith("5") || tmp.startsWith("6")
				|| tmp.startsWith("7") || tmp.startsWith("8") || tmp.startsWith("9") || tmp.startsWith("0")) {
			day = day + tmp.charAt(0);
			tmp = tmp.substring(1);
		}
		if(day.equals("") || !tmp.startsWith("]")) {
			System.err.println("toxicCO-playerが正しく設定されてません " + "day:" + day + "  charAt:" + tmp.charAt(0));
	    	System.exit(1);
		}
		
		toxicList.add(new toxicCO(toxicCOplayer, Integer.parseInt(day)));
		tmp = tmp.substring(1);
		
		return tmp;
	}
	
	public final void printRoleCombination() {
		final String[] rolename = {"村人", "占い師", "霊能者", "狩人", "共有者", "人狼", "狂信者", "妖狐", "背徳者", "埋毒者"};
		String roleList = "";
		for(int j = 0; j < 10; j++) {
			
			if(table1.getRoleCom().getRole()[j] != 0) {
				roleList = roleList + rolename[j] + table1.getRoleCom().getRole()[j];
			}
		}
		System.out.println("\n" + roleList);
		System.out.println();
	}
	
	public final void printPlayer() {	
		String playerList = "";
		for(player pl : table1.getPlayerList()) {
			playerList = playerList + pl.getName() + ":" + pl.getId() + "  ";
		}
		System.out.println("\n" + playerList);
		System.out.println();
	}
	
	public final boolean isCorrect() {
		boolean hasMinus = false;
		final String[] rolename = {"村人", "占い師", "霊能者", "狩人", "共有者", "人狼", "狂信者", "妖狐", "背徳者", "埋毒者"};
		
		// 1. 配役とプレイヤー数が一致しているか？
		if(table1.getPlayerList().size() != table1.getRoleCom().getTotal()) {
			System.err.println("配役人数とプレイヤー人数が一致していません。配役数:" + table1.getPlayerList().size() + "  プレイヤー数:" + table1.getRoleCom().getTotal());
			hasMinus = true;
			printRoleCombination();
		}
		
		// 2. 配役設定が不適切な場合
		int wolves = table1.getRoleCom().getRole()[5];
		int foxes = table1.getRoleCom().getRole()[7];
		int humans = table1.getPlayerList().size() - wolves - foxes;
		// 2-0. 配役数に負の数が含まれる場合
		for(int i = 0; i < table1.getRoleCom().getRole().length; i++) {
			if(table1.getRoleCom().getRole()[i] < 0) {
				System.err.println("配役数に負の数が含まれています。" + rolename[i] + " : " + table1.getRoleCom().getRole()[i]);
				hasMinus = true;
				printRoleCombination();
			}
		}
		if(hasMinus) {
			//System.exit(1);
		}
		// 2-1. 人狼数が人間数以上のエラー
		if(wolves >= humans) {
			System.err.println("人狼数が人間数以上います。人狼数:" + wolves + "   人間数:" + humans);
			hasMinus = true;
			printRoleCombination();
		}
		// 2-2. 人狼数が0のエラー
		if(wolves == 0) {
			System.err.println("人狼を1人以上配役に含めてください。");
			hasMinus = true;
			printRoleCombination();
		}
		// 2-3. 妖狐数が0で背徳者数が1以上のエラー
		if(foxes == 0 && table1.getRoleCom().getRole()[8] > 0) {
			System.err.println("妖狐がいない配役で背徳者を入れることはできません。");
			hasMinus = true;
			printRoleCombination();
		}
		if(hasMinus) {
			//System.exit(1);
		}
		// 2-4. 次の役職は1人までしか入れられない。占い師、霊能者、狩人、妖狐
		if(table1.getRoleCom().getRole()[1] > 1) {
			System.err.println("占い師は1人までしか配役に入れられません。");
			hasMinus = true;
			printRoleCombination();
		}
		if(table1.getRoleCom().getRole()[2] > 1) {
			System.err.println("霊能者は1人までしか配役に入れられません。");
			hasMinus = true;
			printRoleCombination();
		}
		if(table1.getRoleCom().getRole()[3] > 1) {
			System.err.println("狩人は1人までしか配役に入れられません。");
			hasMinus = true;
			printRoleCombination();
		}
		if(table1.getRoleCom().getRole()[7] > 1) {
			System.err.println("妖狐は1人までしか配役に入れられません。");
			hasMinus = true;
			printRoleCombination();
		}
		if(table1.getRoleCom().getRole()[9] > 1) {
			System.err.println("埋毒者は1人までしか配役に入れられません。");
			hasMinus = true;
			printRoleCombination();
		}
		if(hasMinus) {
			//System.exit(1);
		}
		
		// 3. プレイヤーidが負の数、またはかぶりのエラー
		List<Integer> idList = new LinkedList<>();
		List<String> nameList = new LinkedList<>();
		for(player pl : table1.getPlayerList()) {
			for(String name : nameList) {
				if(pl.getName().equals(name)) {
					System.err.println("プレイヤーの名前が重複しています。" + pl.getName());
					hasMinus = true;
					printPlayer();
				}
			}
			if(idList.contains(pl.getId())) {
				System.err.println("プレイヤーのidが重複しています。" + pl.getName() + " id:" + pl.getId());
				hasMinus = true;
				printPlayer();
			}
			else if(pl.getName().equals("×")) {
				System.err.println("プレイヤーの名前を×にはできません。");
				hasMinus = true;
				printPlayer();
			}
			else {
				idList.add(pl.getId());
				nameList.add(pl.getName());
			}
			if(pl.getId() < 0) {
				System.err.println("プレイヤーidは負の数にできません。" + pl.getName() + " id:" + pl.getId());
				hasMinus = true;
				printPlayer();
			}
		}
		if(hasMinus) {
			//System.exit(1);
		}
		
		// 4. すべてのリストで該当しないプレイヤーが含まれるエラー
		for(player pl : table1.getExpelleds()) {
			if(!table1.getPlayerList().contains(pl) && pl.getId() != -1) {
				System.err.println("追放者リストに存在しないプレイヤーがいます。" + pl.getName());
				hasMinus = true;
			}
		}
		for(List<player> list : table1.getVictims()) {
			for(player pl : list) {
				if(!table1.getPlayerList().contains(pl) && pl.getId() != -1) {
					System.err.println("犠牲者リストに存在しないプレイヤーがいます。" + pl.getName());
					hasMinus = true;
				}
			}
		}
		for(player pl : table1.getSuicides()) {
			if(!table1.getPlayerList().contains(pl) && pl.getId() != -1) {
				System.err.println("道連れリストに存在しないプレイヤーがいます。" + pl.getName());
				hasMinus = true;
			}
		}
		for(seerCO seer : table1.getSeerCOList()) {
			if(!table1.getPlayerList().contains(seer.getSeerCOpl())) {
				System.err.println("占い師COプレイヤーに存在しないプレイヤーがいます。" + seer.getSeerCOpl().getName());
				hasMinus = true;
			}
			for(int i = 0; i < seer.getDivineList().size(); i++) {
				if(!table1.getPlayerList().contains(seer.getDivineList().get(i).getPl()) && seer.getDivineList().get(i).getPl().getId() != -1) {
					System.err.println(seer.getSeerCOpl().getName() + "の占い対象に存在しないプレイヤーがいます。" + seer.getDivineList().get(i).getPl().getName());
					hasMinus = true;
				}
			}
		}
		for(mediumCO medium : table1.getMediumCOList()) {
			if(!table1.getPlayerList().contains(medium.getMediumCOpl())) {
				System.err.println("霊能者COプレイヤーに存在しないプレイヤーがいます。" + medium.getMediumCOpl().getName());
				hasMinus = true;
			}
		}
		for(bodyguardCO bodyguard : table1.getBodyguardCOList()) {
			if(!table1.getPlayerList().contains(bodyguard.getBodyguardCOpl())) {
				System.err.println("狩人COプレイヤーに存在しないプレイヤーがいます。" + bodyguard.getBodyguardCOpl().getName());
				hasMinus = true;
			}
			for(int i = 0; i < bodyguard.getGuardList().size(); i++) {
				if(!table1.getPlayerList().contains(bodyguard.getGuardList().get(i)) && bodyguard.getGuardList().get(i).getId() != -1) {
					System.err.println(bodyguard.getBodyguardCOpl().getName() + "の護衛対象に存在しないプレイヤーがいます。" + bodyguard.getGuardList().get(i).getName());
					hasMinus = true;
				}
			}
		}
		for(player freemason : table1.getFreemasonCOList().getFreemasonCOpl()) {
			if(!table1.getPlayerList().contains(freemason)) {
				System.err.println("共有者COプレイヤーに存在しないプレイヤーがいます。" + freemason.getName());
				hasMinus = true;
			}
		}
		if(hasMinus) {
			//System.exit(1);
		}
		
		// 5. 日数のエラー
		// 5-1. 道連れ
		List<Integer> deadRank = table1.deadRank();
		int aliveRank = 0;
		for(Integer rank : deadRank) {
			if(aliveRank < rank) {
				aliveRank = rank;
			}
		}
		if(table1.getSuicides().size() != 0) {
			if(2 * table1.getSuicideDay() > aliveRank || table1.getSuicideDay() < 1) {
				System.err.println("道連れの日数が不適切です。日数:" + table1.getSuicideDay());
				System.exit(1);
			}
		}
		// 5-2. CO
		for(seerCO seer : table1.getSeerCOList()) {
			if(2 * seer.getCOday() - 1 > aliveRank || seer.getCOday() < 1) {
				System.err.println(seer.getSeerCOpl().getName() + "の占い師のCO日数が不適切です。日数:" + seer.getCOday());
				hasMinus = true;
			}
		}
		for(mediumCO medium : table1.getMediumCOList()) {
			if(2 * medium.getCOday() - 1 > aliveRank || medium.getCOday() < 1) {
				System.err.println(medium.getMediumCOpl().getName() + "の霊能者のCO日数が不適切です。日数:" + medium.getCOday());
				hasMinus = true;
			}
		}
		for(bodyguardCO bodyguard : table1.getBodyguardCOList()) {
			if(2 * bodyguard.getCOday() - 1 > aliveRank || bodyguard.getCOday() < 1) {
				System.err.println(bodyguard.getBodyguardCOpl().getName() + "の狩人のCO日数が不適切です。日数:" + bodyguard.getCOday());
			}
		}
		if(hasMinus) {
			//System.exit(1);
		}
		
		// 6. 死亡者の重複のエラー
		List<player> deadPlayer = new ArrayList<>();
		for(player pl : table1.getExpelleds()) {
			if(deadPlayer.contains(pl)) {
				System.err.println("死亡者が重複しています。:" + pl.getName());
				hasMinus = true;
			}
			else if(pl.getId() != -1) {
				deadPlayer.add(pl);
			}
		}
		for(List<player> list : table1.getVictims()) {
			for(player pl : list) {
				if(deadPlayer.contains(pl)) {
					System.err.println("死亡者が重複しています。:" + pl.getName());
					hasMinus = true;
				}
				else if(pl.getId() != -1) {
					deadPlayer.add(pl);
				}
			}
		}
		for(player pl : table1.getSuicides()) {
			if(deadPlayer.contains(pl)) {
				System.err.println("死亡者が重複しています。:" + pl.getName());
				hasMinus = true;
			}
			else if(pl.getId() != -1) {
				deadPlayer.add(pl);
			}
		}
		if(hasMinus) {
			//System.exit(1);
		}
		
		// 7. COプレイヤーの重複エラー
		List<player> COplayer = new ArrayList<>();
		for(seerCO seer: table1.getSeerCOList()) {
			if(COplayer.contains(seer.getSeerCOpl())) {
				System.err.println("COが重複しています。:" + seer.getSeerCOpl().getName());
				hasMinus = true;
			}
			else {
				COplayer.add(seer.getSeerCOpl());
			}
		}
		for(mediumCO medium : table1.getMediumCOList()) {
			if(COplayer.contains(medium.getMediumCOpl())) {
				System.err.println("COが重複しています。:" + medium.getMediumCOpl().getName());
				hasMinus = true;
			}
			else {
				COplayer.add(medium.getMediumCOpl());
			}
		}
		for(bodyguardCO bodyguard : table1.getBodyguardCOList()) {
			if(COplayer.contains(bodyguard.getBodyguardCOpl())) {
				System.err.println("COが重複しています。:" + bodyguard.getBodyguardCOpl().getName());
				hasMinus = true;
			}
			else {
				COplayer.add(bodyguard.getBodyguardCOpl());
			}
		}
		for(player freemason : table1.getFreemasonCOList().getFreemasonCOpl()) {
			if(COplayer.contains(freemason)) {
				System.err.println("COが重複しています。:" + freemason.getName());
				hasMinus = true;
			}
			else {
				COplayer.add(freemason);
			}
		}
		if(hasMinus) {
			//System.exit(1);
		}
		
		// 8. 死亡者の能力行使/未来の能力行使のエラー
		for(seerCO seer : table1.getSeerCOList()) {
			if(2 * (seer.getDivineList().size() - 1) - 1 >= table1.deadRankPlayer(seer.getSeerCOpl())) {
				System.err.println(seer.getSeerCOpl().getName() + "の占い師の占い結果の数が不適切です。");
				hasMinus = true;
			}
		}
		for(mediumCO medium : table1.getMediumCOList()) {
			if(2 * (medium.getSenseList().size()) - 1 >= table1.deadRankPlayer(medium.getMediumCOpl())) {
				System.err.println(medium.getMediumCOpl().getName() + "の霊能者の霊能結果の数が不適切です。");
				hasMinus = true;
			}
		}
		for(bodyguardCO bodyguard : table1.getBodyguardCOList()) {
			if(2 * (bodyguard.getGuardList().size() - 1) - 1 >= table1.deadRankPlayer(bodyguard.getBodyguardCOpl())) {
				System.err.println(bodyguard.getBodyguardCOpl().getName() + "の狩人の護衛対象選択の数が不適切です。");
				hasMinus = true;
			}
		}
		if(hasMinus) {
			//System.exit(1);
		}
		
		// 9. 死亡者への能力行使対象のエラー
		for(seerCO seer : table1.getSeerCOList()) {
			for(int day = 1; day < seer.getDivineList().size(); day++) {
				if(seer.getDivineList().get(day).getPl().getId() != -1) {
					if(2 * day - 2 >= table1.deadRankPlayer(seer.getDivineList().get(day).getPl())) {
						System.err.println(seer.getSeerCOpl().getName() + "の占い師の占い対象はすでに死亡しています。" + day + "日目:" + seer.getDivineList().get(day).getPl().getName());
						hasMinus = true;
					}
				}
			}
		}
		for(bodyguardCO bodyguard : table1.getBodyguardCOList()) {
			for(int day = 2; day < bodyguard.getGuardList().size(); day++) {
				if(bodyguard.getGuardList().get(day).getId() != -1) {
					 if(2 * day - 2 >= table1.deadRankPlayer(bodyguard.getGuardList().get(day))) {
						 System.err.println(bodyguard.getBodyguardCOpl().getName() + "の狩人の護衛対象はすでに死亡しています。" + day + "日目:" + bodyguard.getGuardList().get(day).getName());
						 hasMinus = true;
					 }
				}
			}
		}
		if(hasMinus) {
			//System.exit(1);
		}
		
		// 10. 能力行使対象が自分のエラー(占い師、狩人)
		for(seerCO seer : table1.getSeerCOList()) {
			for(result tar : seer.getDivineList()) {
				if(tar.getPl() == seer.getSeerCOpl()) {
					System.err.println("自分を占い対象にはできません。 占い師 : " + seer.getSeerCOpl().getName());
					hasMinus = true;
				}
			}
		}
		for(bodyguardCO bodyguard : table1.getBodyguardCOList()) {
			for(player guard : bodyguard.getGuardList()) {
				if(guard == bodyguard.getBodyguardCOpl()) {
					System.err.println("自分を護衛対象にはできません。 狩人 : " + bodyguard.getBodyguardCOpl().getName());
					hasMinus = true;
				}
			}
		}
		if(hasMinus) {
			//System.exit(1);
		}
		
		// 11. 占い師の2回目の占い結果が違うエラー
		for(seerCO seer : table1.getSeerCOList()) {
			for(result tar : seer.getDivineList()) {
				for(result tar2 : seer.getDivineList()) {
					if(tar.getPl() == tar2.getPl() && tar.getIsWerewolf() != tar2.getIsWerewolf()) {
						System.err.println(seer.getSeerCOpl().getName() + "の占い結果に相違が出ています。 : " + tar.getPl().getName());
						hasMinus = true;
					}
				}
			}
		}
		
		// 12. 配役にない役職のCOがあるエラー
		if(table1.getRoleCom().getRole()[1] == 0 && table1.getSeerCOList().size() > 0) {
			System.err.println("占い師が配役に含まれていないのにCOがあります。");
			hasMinus = true;
		}
		if(table1.getRoleCom().getRole()[2] == 0 && table1.getMediumCOList().size() > 0) {
			System.err.println("霊能者が配役に含まれていないのにCOがあります。");
			hasMinus = true;
		}
		if(table1.getRoleCom().getRole()[3] == 0 && table1.getBodyguardCOList().size() > 0) {
			System.err.println("狩人が配役に含まれていないのにCOがあります。");
			hasMinus = true;
		}
		if(table1.getRoleCom().getRole()[4] == 0 && table1.getFreemasonCOList().getFreemasonCOpl().size() > 0) {
			System.err.println("共有者が配役に含まれていないのにCOがあります。");
			hasMinus = true;
		}
		// 13. 共有者が配役人数より多くCOがある場合
		else if(table1.getRoleCom().getRole()[4] < table1.getFreemasonCOList().getFreemasonCOpl().size()) {
			System.err.println("共有者のCOが配役人数を超えてます。");
			hasMinus = true;
		}
		
		if(table1.getFirstVictim().size() > 0)  {
			
		}
		
		//14. 犠牲者と追放者を比較して数が合わないエラー
		if(table1.getExpelleds().size() > table1.getVictims().size() || table1.getExpelleds().size() + 1 < table1.getVictims().size()) {
			System.err.println("追放者と日ごとの犠牲者の数が不適切です。");
			hasMinus = true;
		}
		
		return hasMinus;
	}
	
	public final player getNotPl() {
		return notPlayer;
	}
	
	public final List<table> getDayState() {
		List<table> tableDayList = new ArrayList<>();
		
		// 半日ごとに情報を格納
		for(int ra = 0; ra < rank - 2; ra++) {
			
			// 犠牲者
			List<List<player>> tmpVic = new ArrayList<>();
			for(int i = 0; i <= ((ra)/2 + 1); i++) {
				tmpVic.add(table1.getVictims().get(i));
			}
			
			// 追放者
			List<player> tmpExpe = new ArrayList<>();
			if(ra > 0) {
				for(int i = 0; i <= ((ra - 1)/2 + 1); i++) {
					tmpExpe.add(table1.getExpelleds().get(i));
				}
			}
			else {
				tmpExpe.add(table1.getExpelleds().get(0));
			}
			
			List<seerCO> tmpSeer = new ArrayList<>();
			List<mediumCO> tmpMedium = new ArrayList<>();
			List<bodyguardCO> tmpBodyguard = new ArrayList<>();
			List<toxicCO> tmpToxic = new ArrayList<>();
			
			// 占い師CO
			for(seerCO seer : table1.getSeerCOList()) {
				if(seer.getCOday() <= (ra/2 + 1)) {
					List<result> tmpRes = new ArrayList<>();
					for(int i = 0; i <= (ra/2 + 1); i++) {
						if(seer.getDivineList().size() > i) {
							tmpRes.add(seer.getDivineList().get(i));
						}
					}
					tmpSeer.add(new seerCO(seer.getSeerCOpl(), tmpRes, seer.getCOday()));
				}
			}
			// 霊能者CO
			for(mediumCO medium : table1.getMediumCOList()) {
				if(medium.getCOday() <= (ra/2 + 1)) {
					List<result> tmpRes = new ArrayList<>();
					for(int i = 0; i <= (ra/2); i++) {
						if(medium.getSenseList().size() > i) {
							tmpRes.add(medium.getSenseList().get(i));
						}
					}
					tmpMedium.add(new mediumCO(medium.getMediumCOpl(), tmpRes, medium.getCOday()));
				}
			}
			// 狩人CO
			for(bodyguardCO bodyguard : table1.getBodyguardCOList()) {
				if(bodyguard.getCOday() <= (ra/2 + 1)) {
					List<player> tmpRes = new ArrayList<>();
					for(int i = 0; i <= (ra/2 + 1); i++) {
						if(bodyguard.getGuardList().size() > i) { 
							tmpRes.add(bodyguard.getGuardList().get(i));
						}
					}
					tmpBodyguard.add(new bodyguardCO(bodyguard.getBodyguardCOpl(), tmpRes, bodyguard.getCOday()));
				}
			}
			// 埋毒者CO
			for(toxicCO toxic : table1.getToxicCOList()) {
				if(toxic.getCOday() <= (ra/2 + 1)) {
					tmpToxic.add(new toxicCO(toxic.getToxicCOpl(), toxic.getCOday()));
				}
			}
			
			int suicideDay = 0;
			List<player> suicide = new ArrayList<>();
			if(ra > 0) {
				if(table1.getSuicideDay() < ((ra + 1)/2 + 1)) {
					suicideDay = table1.getSuicideDay();
					suicide = table1.getSuicides();
				}
			}
			int poisonedDay = 0;
			List<player> poisoned = new ArrayList<>();
			poisoned.add(notPlayer);
			if(ra > 0) {
				if(table1.getPoisonedDay() < ((ra + 1)/2 + 1)) {
					poisonedDay = table1.getPoisonedDay();
					poisoned.add(table1.getPoisoned());
				}
			}
			
			table tmpTable = new table(table1.getPlayerList(), table1.getSelfAttack(), table1.getNoAttack(),
					table1.getFirstDivine(), table1.getFirstAttack(), table1.getFirstVictim(), table1.getLack(), 
					LackRole, posHide, seerLackFirstDivine, tmpVic, tmpExpe, suicideDay, suicide, poisonedDay, poisoned, 
					tmpSeer, tmpMedium, tmpBodyguard, table1.getFreemasonCOList(), tmpToxic, true,  new roleCombination(rolesNum));
			
			tableDayList.add(tmpTable);
		}
		
		
		return tableDayList;
	}
}
