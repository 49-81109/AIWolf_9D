package org.aiwolf.ui.res;

import java.net.URL;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.swing.ImageIcon;

import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.Operator;
import org.aiwolf.client.lib.TalkType;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Judge;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.data.Status;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.data.Team;
import org.aiwolf.common.data.Vote;
import org.aiwolf.common.util.BidiMap;






public class JapaneseResource implements AIWolfResource {

	protected String[][] agentResourceAry = {
			{ "不幸なタットン", "img/Male_01.png" },
			{ "学生デイクル", "img/Male_02.png" },
			{ "御曹司ランダル", "img/Male_03.png" },
			{ "用心棒ダコタ", "img/Male_04.png" },
			{ "調査員ピース", "img/Male_05.png" },
			{ "教師ジョアキム", "img/Male_06.png" },
			{ "絵描きロデリック", "img/Male_07.png" },
			{ "村長イワン", "img/Male_08.png" },
			{ "嘘つきジャック", "img/Male_09.png" },
			{ "牧童クゥ", "img/Male_10.png" },
			{ "探偵トーリイ", "img/Male_11.png" },
			{ "露天商クリフ", "img/Male_12.png" },
			{ "雑貨屋ノラ", "img/Female_01.png" },
			{ "魔女ターニャ", "img/Female_02.png" },
			{ "老婆キノ", "img/Female_03.png" },
			{ "洗濯屋メアリ", "img/Female_04.png" },
			{ "村娘カサリナ", "img/Female_05.png" },
			{ "優等生デニース", "img/Female_06.png" },
			{ "記者キャサリン", "img/Female_07.png" },
			{ "不機嫌エイミー", "img/Female_08.png" },
			{ "浜辺のフラン", "img/Female_09.png" },
			{ "令嬢ブレンダ", "img/Female_10.png" },
			{ "力持ちメリーズ", "img/Female_11.png" },
			{ "眼鏡マリンダ", "img/Female_12.png" },
	};
	//
	// String[][] agentResourceAry = {
	// {"麦藁帽のオリバー", "img/00_body.png"},
	// {"学生デイクル", "img/01_body.png"},
	// {"変人レノックス", "img/02_body.png"},
	// {"医者の卵トレイス", "img/03_body.png"},
	// {"水商売ディラニイ", "img/04_body.png"},
	// {"パジャマジェリコ", "img/05_body.png"},
	// {"王女サフィラ", "img/06_body.png"},
	// {"団長バークレイ", "img/07_body.png"},
	// {"浮浪者ロデリック", "img/08_body.png"},
	// {"掃除人モンティ", "img/09_body.png"},
	// {"元軍人リンシイ", "img/10_body.png"},
	// {"用心棒ダコタ", "img/11_body.png"},
	// {"黒ずきんマーシャ", "img/12_body.png"},
	// {"囚人キプリング", "img/13_body.png"},
	// {"料理人チャズ", "img/14_body.png"},
	// {"探偵トーリイ", "img/15_body.png"},
	// {"不幸なタットン", "img/16_body.png"},
	// {"眼鏡マリンダ", "img/17_body.png"},
	// {"御曹司ランダル", "img/18_body.png"},
	// {"包帯の子ニッキー", "img/19_body.png"},
	// {"盗賊レギナルド", "img/20_body.png"},
	// {"怠け者ミッチェル", "img/21_body.png"},
	// {"詩人ルシアス", "img/22_body.png"},
	// {"教師ジョアキム", "img/23_body.png"},
	// {"村娘カサリナ", "img/24_body.png"},
	// {"堅物クレア", "img/25_body.png"},
	// {"曲芸師テッド", "img/26_body.png"},
	// {"ゴロツキガルダ", "img/27_body.png"},
	// {"牧童クゥ", "img/28_body.png"},
	// {"モノマネ師ピンク", "img/29_body.png"},
	// {"男前少年トム", "img/30_body.png"},
	// {"露天商クリフ", "img/31_body.png"},
	// {"酔払いアレクセイ", "img/32_body.png"},
	// {"不機嫌エイミー", "img/33_body.png"},
	// {"嘘つきジャック", "img/34_body.png"},
	// {"記者キャサリン", "img/35_body.png"},
	// {"令嬢ブレンダ", "img/36_body.png"},
	// {"芸人ピエール", "img/37_body.png"},
	// {"孤児ショコラ", "img/38_body.png"},
	// {"旅人ザク", "img/39_body.png"},
	// {"双子の兄エメット", "img/40_body.png"},
	// {"双子の弟ミメット", "img/41_body.png"},
	// {"女学生サーラ", "img/42_body.png"},
	// {"ボスアンジェラ", "img/43_body.png"},
	// {"老婆キノ", "img/44_body.png"},
	// {"看板娘モニカ", "img/45_body.png"},
	// {"優等生デニース", "img/46_body.png"},
	// {"教徒カミュ", "img/47_body.png"},
	// {"修道女ユーリエ", "img/48_body.png"},
	// {"雑貨屋ノラ", "img/49_body.png"},
	// {"踊り子バルバラ", "img/50_body.png"},
	// {"双子のチェルシー", "img/51_body.png"},
	// {"双子のチェリオ", "img/52_body.png"},
	// {"猫仙人グエン", "img/53_body.png"},
	// {"闇商人ライザ", "img/54_body.png"},
	// {"赤面症ベス", "img/55_body.png"},
	// {"剣士ウォーカー", "img/56_body.png"},
	// {"洗濯屋メアリ", "img/57_body.png"},
	// {"聖者マキリス", "img/58_body.png"},
	// {"処刑人カナビス", "img/59_body.png"},
	// {"悪童ハッチ", "img/60_body.png"},
	// {"植物好きマーブル", "img/61_body.png"},
	// {"調査員ピース", "img/62_body.png"},
	// {"天才医師バラキン", "img/63_body.png"},
	// {"家庭教師メリーズ", "img/64_body.png"},
	// {"浜辺の少女フラン", "img/65_body.png"},
	// {"作家ヒグラシ", "img/66_body.png"},
	// {"村長イワン", "img/67_body.png"},
	// {"食道楽タオ", "img/68_body.png"},
	// {"アウトロージーマ", "img/69_body.png"},
	// {"魔女ターニャ", "img/70_body.png"},
	// {"遊び人ジョニー", "img/71_body.png"},
	// {"旅人リンドバーグ", "img/72_body.png"},
	// };
	//
	//

	protected List<String[]> agentResourceList;

	protected BidiMap<Agent, String> bidiMap;

	public JapaneseResource() {
		agentResourceList = Arrays.asList(agentResourceAry);
		long seed = Calendar.getInstance().getTimeInMillis() / (1000 * 60 * 60);
		Collections.shuffle(agentResourceList, new Random(seed));

		bidiMap = new BidiMap<>();
	}

	@Override
	public void setName(int i, String name) {
		agentResourceList.get(i)[0] = name;
		// System.out.println(name);
	}

	@Override
	public String convertAgent(Agent agent) {
		// return agent.toString();
		if (agent == null) {
			return "";
		}
		else if(agent == Content.ANY){
			return "誰か";
		}
		int agentIdx = agent.getAgentIdx();
		String name = agentResourceList.get(agentIdx)[0];
		bidiMap.put(agent, name);
		return name;
	}

	@Override
	public String convertRole(Role role) {
		switch (role) {
		case BODYGUARD:
			return "狩人";
		case FREEMASON:
			return "共有者";
		case MEDIUM:
			return "霊媒師";
		case POSSESSED:
			return "狂人";
		case SEER:
			return "占い師";
		case VILLAGER:
			return "村人";
		case WEREWOLF:
			return "人狼";
		default:
			return null;
		}
	}

	@Override
	public String convertSpecies(Species species) {
		switch (species) {
		case HUMAN:
			return "人間";
		case WEREWOLF:
			return "人狼";
		default:
			return null;
		}
	}

	@Override
	public String convertTeam(Team team) {
		switch (team) {
		case VILLAGER:
			return "村人側";
		case WEREWOLF:
			return "人狼側";
		default:
			return null;
		}
	}

	@Override
	public String convertStatus(Status status) {
		if (status == null) {
			return null;
		}
		switch (status) {
		case ALIVE:
			return "生存";
		case DEAD:
			return "死亡";
		default:
			return null;
		}
	}

	@Override
	public String convertTopic(Topic topic) {
		switch (topic) {
		case AGREE:
			return "同意";
		case ATTACK:
			return "襲撃";
		case COMINGOUT:
			return "カミングアウト";
		case DISAGREE:
			return "反対";
		case DIVINED:
			return "占い結果";
		case ESTIMATE:
			return "予想";
		case GUARDED:
			return "護衛先";
		case IDENTIFIED:
			return "霊媒結果";
		case OVER:
			return "発言終了";
		case SKIP:
			return "スキップ";
		case VOTE:
			return "投票先";
		default:
			return "";
		}

	}

	@Override
	public String convertTalkType(TalkType talkType) {
		switch (talkType) {
		case TALK:
			return "意見";
		case WHISPER:
			return "囁き";
		}
		return "";
	}

	@Override
	public Agent convertToAgent(String name) {
		return bidiMap.getKey(name);
	}

	// @Override
	// public String convertWhisper(Talk whisper) {
	// Content utterance = new Content(whisper.getText());
	// Topic topic = utterance.getTopic();
	// if(topic == Topic.AGREE){
	// return String.format("%d日の%s(%03d)に賛成する", utterance.getTalkDay(), convert(utterance.getTalkType()), utterance.getTalkID());
	// }
	// else if(topic == Topic.COMINGOUT){
	// return String.format("私は%sと名乗り出る", convert(utterance.getRole()));
	// }
	// else if(topic == Topic.DISAGREE){
	// return String.format("%d日の%s(%03d)に反対する", utterance.getTalkDay(), convert(utterance.getTalkType()), utterance.getTalkID());
	// }
	// else if(topic == Topic.DIVINED){
	// return String.format("%sを占った結果%sだったことにする", convert(utterance.getTarget()), convert(utterance.getResult()));
	// }
	// else if(topic == Topic.ESTIMATE){
	// return String.format("%sは%sだと思う．", convert(utterance.getTarget()), convert(utterance.getRole()));
	// }
	// else if(topic == Topic.GUARDED){
	// return String.format("%sを護衛したことにする", convert(utterance.getTarget()));
	// }
	// else if(topic == Topic.IDENTIFIED){
	// return String.format("%sは%sだったと言う", convert(utterance.getTarget()), convert(utterance.getResult()));
	// }
	// else if(topic == Topic.VOTE){
	// return String.format("%sに投票する", convert(utterance.getTarget()));
	// }
	// else if(topic == Topic.ATTACK){
	// return String.format("%sを襲撃する", convert(utterance.getTarget()));
	// }
	// return whisper.getText();
	// }

	@Override
	public String convertTalk(Talk talk) {
		if (talk.isSkip()) {
			return "様子を見ている";
		}
		else if(talk.isOver()){
			return "特に話すことはない";
		}
		try {
			Content contents = new Content(talk.getAgent() + " " + Content.stripSubject(talk.getText()));
			return contentToText(contents.getSubject(), contents, TalkType.TALK);
		} catch (Exception e) {
			e.printStackTrace();
			return talk.getText();
		}
	}

	@Override
	public String convertWhisper(Talk whisper) {
		if (whisper.isSkip()) {
			return "様子を見ている";
		}
		else if(whisper.isOver()){
			return "特に話すことはない";
		}
		try {
			Content contents = new Content(whisper.getAgent() + " " + Content.stripSubject(whisper.getText()));
			return contentToText(contents.getSubject(), contents, TalkType.WHISPER);
		} catch (Exception e) {
			e.printStackTrace();
			return whisper.getText();
		}
	}

	/**
	 * Convert content to text
	 * @param baseContents
	 */
	protected String contentToText(Agent talker, Content baseContents, TalkType talkType) {
		Agent subject = baseContents.getSubject();
		Operator operator = baseContents.getOperator();
		Topic topic = baseContents.getTopic();
		if (operator == null) {
			return topicToText(talker, baseContents, topic, talkType);
		}
		else {
			if (operator == Operator.REQUEST) {
				StringBuffer buf = new StringBuffer();
				if (subject != talker) {
					buf.append(convertAgent(subject) + "->");
				}
				for (Content content : baseContents.getContentList()) {
						buf.append(String.format("要望：%s ", contentToText(talker, content, talkType)));						
					// if(content.getSubject() != null){
					// buf.append(String.format("%sに要望： %s ", convert(content.getSubject()), contentToText(content)));
					// }
					// else{
					// }
				}
				return buf.toString();
			}
			else if (operator == Operator.BECAUSE) {
				StringBuffer buf = new StringBuffer();
				if (subject != talker) {
					buf.append(convertAgent(subject) + "->");
				}
				List<Content> contentList = baseContents.getContentList();
				if (contentList.size() == 2) {
					Content reason = contentList.get(0);
					Content action = contentList.get(1);
					buf.append(String.format("%s， だから %s", contentToText(talker, reason, talkType), contentToText(talker, action, talkType)));
				}
				else {
					return baseContents.getText();
				}

				// for(Content content:baseContents.getContentList()){
				// buf.append(String.format("理由： %s ", contentToText(content, talkType)));
				// }
				return buf.toString();
			}
			else if (operator == Operator.INQUIRE) {
				StringBuffer buf = new StringBuffer();
				if (subject != talker) {
					buf.append(convertAgent(subject) + "->");
				}
				for (Content content : baseContents.getContentList()) {
					buf.append(String.format("照会：%s ", contentToText(talker, content, talkType)));
				}
				return buf.toString();

			}
			else if (operator == Operator.AND) {
				StringBuffer buf = new StringBuffer();
				if (subject != talker) {
					buf.append(convertAgent(subject) + "->");
				}
				String and = "";
				for (Content content : baseContents.getContentList()) {
					buf.append(String.format("%s%s ", and, contentToText(talker, content, talkType)));
					and = "かつ ";
				}
				return buf.toString();
			}
			else if (operator == Operator.OR) {
				StringBuffer buf = new StringBuffer();
				if (subject != talker) {
					buf.append(convertAgent(subject) + "->");
				}
				String and = "";
				for (Content content : baseContents.getContentList()) {
					buf.append(String.format("%s%s ", and, contentToText(talker, content, talkType)));
					and = "または ";
				}
				return buf.toString();
			}
			else if (operator == Operator.XOR) {
				StringBuffer buf = new StringBuffer();
				if (subject != talker) {
					buf.append(convertAgent(subject) + "->");
				}
				String and = "";
				for (Content content : baseContents.getContentList()) {
					buf.append(String.format("%s%s ", and, contentToText(talker, content, talkType)));
					and = "か ";
				}
				if (baseContents.getContentList().size() > 1) {
					buf.append("のどちらか");
				}
				return buf.toString();
			}
			else if (operator == Operator.NOT) {
				StringBuffer buf = new StringBuffer();
				if (subject != talker) {
					buf.append(convertAgent(subject) + "->");
				}
				for (Content content : baseContents.getContentList()) {
					buf.append(String.format("%s ", contentToText(talker, content, talkType)));
				}
				buf.append("ではない ");
				return buf.toString();
			}
			else if (operator == Operator.DAY) {
				if (subject == talker) {
					return "day" + baseContents.getDay() + "に " + contentToText(talker, baseContents.getContentList().get(0), talkType);
				}else {
					return convertAgent(subject) + "->day" + baseContents.getDay() + "に " + contentToText(talker, baseContents.getContentList().get(0), talkType);
				}
			}

			// System.out.println(operator);
		}
		return baseContents.getText();

	}

	/**
	 *
	 * @param contents
	 * @param topic
	 * @return
	 */
	protected String topicToText(Agent talker, Content contents, Topic topic, TalkType talkType) {
		Agent subject = contents.getSubject();
		if (topic == Topic.ATTACK) {
			if (subject == talker) {
				return String.format("%sを襲撃する", convertAgent(contents.getTarget()));

			} else {
				return String.format("%sが%sを襲撃する", convertAgent(subject), convertAgent(contents.getTarget()));
			}
		}
		else if (topic == Topic.ATTACKED) {
			if (subject == talker) {
				return String.format("%sを襲撃した", convertAgent(contents.getTarget()));
			} else {
				return String.format("%sが%sを襲撃した", convertAgent(subject), convertAgent(contents.getTarget()));
			}
		}
		else if (topic == Topic.AGREE) {
			if (subject == talker) {
				return String.format("%d日の意見(%03d)に同意", contents.getTalkDay(), contents.getTalkID());
			} else {
				return String.format("%sが%d日の意見(%03d)に同意", convertAgent(subject), contents.getTalkDay(), contents.getTalkID());
			}
		}
		else if (topic == Topic.COMINGOUT) {
			if (talkType == TalkType.TALK) {
				if (subject == talker) {
					if (contents.getTarget() == talker) {
						return String.format("【私は%s】です", convertRole(contents.getRole()));

					}
					else {
						return String.format("【%sは%s】です", convertAgent(contents.getTarget()), convertRole(contents.getRole()));
					}
				}
				else {
					if (subject == contents.getTarget()) {
						return String.format("【%sは%sとCO】した", convertAgent(subject), convertRole(contents.getRole()));
					}
					else {
						return String.format("%sが【%sは%sとCO】した", convertAgent(subject), convertAgent(contents.getTarget()), convertRole(contents.getRole()));
					}
				}
			}
			else if (talkType == TalkType.WHISPER) {
				if (subject == talker) {
					if (contents.getTarget() == talker) {
						return String.format("私は%sと名乗る", convertRole(contents.getRole()));

					} 
					else {
						return String.format("%sは%sだと言う", convertAgent(contents.getTarget()), convertRole(contents.getRole()));
					}
				} 
				else {
					if (subject == contents.getTarget()) {
						return String.format("%sは%sとCOした", convertAgent(subject), convertRole(contents.getRole()));
					}
					else {
						return String.format("%sが%sは%sとCOした", convertAgent(subject), convertAgent(contents.getTarget()), convertRole(contents.getRole()));
					}
				}
			}
		}
		else if (topic == Topic.DISAGREE) {
			if (subject == talker) {
				return String.format("%d日の意見(%03d)に反対", contents.getTalkDay(), contents.getTalkID());
			} else {
				return String.format("%sが%d日の意見(%03d)に反対", convertAgent(subject), contents.getTalkDay(), contents.getTalkID());
			}
		}
		else if (topic == Topic.DIVINED) {
			if (talkType == TalkType.TALK) {
				if (subject == talker) {
					return String.format("占い結果：【%sは%s】だった", convertAgent(contents.getTarget()), convertSpecies(contents.getResult()));
				}
				else {
					return String.format("%sの占い結果：【%sは%s】だった", convertAgent(subject), convertAgent(contents.getTarget()), convertSpecies(contents.getResult()));
				}
			} 
			else if (talkType == TalkType.WHISPER) {
				if (subject == talker) {
					return String.format("占い結果：%sは%sだったことにする", convertAgent(contents.getTarget()), convertSpecies(contents.getResult()));
				}
				else {
					return String.format("%sの占い結果：%sは%sだったことにする", convertAgent(subject), convertAgent(contents.getTarget()), convertSpecies(contents.getResult()));
				}
			}
		}
		else if (topic == Topic.DIVINATION) {
			if (talkType == TalkType.TALK) {
				if (subject == talker) {
					return String.format("%sを占う", convertAgent(contents.getTarget()));
				} 
				else {
					return String.format("%sが%sを占う", convertAgent(subject), convertAgent(contents.getTarget()));
				}
			}
			else if (talkType == TalkType.WHISPER) {
				if (subject == talker) {
					return String.format("%sを占うことにする", convertAgent(contents.getTarget()));
				} 
				else {
					return String.format("%sが%sを占うことにする", convertAgent(subject), convertAgent(contents.getTarget()));
				}
			}
		} 
		else if (topic == Topic.ESTIMATE) {
			if (subject == talker) {
				return String.format("%sは%sだと思う", convertAgent(contents.getTarget()), convertRole(contents.getRole()));
			} 
			else {
				return String.format("%sが%sは%sだと思っている", convertAgent(subject), convertAgent(contents.getTarget()), convertRole(contents.getRole()));
			}
		}
		else if (topic == Topic.GUARDED) {
			if (talkType == TalkType.TALK) {
				if (subject == talker) {
					return String.format("【%sを護衛】した", convertAgent(contents.getTarget()));
				} 
				else {
					return String.format("%sが【%sを護衛】した", convertAgent(subject), convertAgent(contents.getTarget()));
				}
			}
			else if (talkType == TalkType.WHISPER) {
				if (subject == talker) {
					return String.format("%sを護衛したことにする", convertAgent(contents.getTarget()));
				} 
				else {
					return String.format("%sが【%sを護衛】したことにする", convertAgent(subject), convertAgent(contents.getTarget()));
				}
			}
		} 
		else if (topic == Topic.GUARD) {
			if (talkType == TalkType.TALK) {
				if (subject == talker) {
					return String.format("%sを護衛する", convertAgent(contents.getTarget()));
				} 
				else {
					return String.format("%sが%sを護衛する", convertAgent(subject), convertAgent(contents.getTarget()));
				}
			} 
			else if (talkType == TalkType.WHISPER) {
				if (subject == talker) {
					return String.format("%sを護衛することにする", convertAgent(contents.getTarget()));
				} 
				else {
					return String.format("%sが%sを護衛することにする", convertAgent(subject), convertAgent(contents.getTarget()));
				}
			}
		} 
		else if (topic == Topic.IDENTIFIED) {
			if (talkType == TalkType.TALK) {
				if (subject == talker) {
					return String.format("霊媒結果：【%sは%s】だった", convertAgent(contents.getTarget()), convertSpecies(contents.getResult()));
				} 
				else {
					return String.format("%sの霊媒結果：【%sは%s】だった", convertAgent(subject), convertAgent(contents.getTarget()), convertSpecies(contents.getResult()));
				}
			}
			else if (talkType == TalkType.WHISPER) {
				if (subject == talker) {
					return String.format("霊媒結果：%sは%sだったことにする", convertAgent(contents.getTarget()), convertSpecies(contents.getResult()));
				} 
				else {
					return String.format("%sの霊媒結果：%sは%sだったことにする", convertAgent(subject), convertAgent(contents.getTarget()), convertSpecies(contents.getResult()));
				}
			}
		} 
		else if (topic == Topic.VOTE) {
			if (talkType == TalkType.TALK) {
				if (subject == talker) {
					return String.format("%sに投票する", convertAgent(contents.getTarget()));
				}
				else {
					return String.format("%sが%sに投票する", convertAgent(subject), convertAgent(contents.getTarget()));
				}
			}
			else if (talkType == TalkType.WHISPER) {
				if (subject == talker) {
					return String.format("%sに投票することにする", convertAgent(contents.getTarget()));
				} 
				else {
					return String.format("%sが%sに投票することにする", convertAgent(subject), convertAgent(contents.getTarget()));
				}
			}
		}
		else if (topic == Topic.VOTED) {
			if (subject == talker) {
				return String.format("%sに投票した", convertAgent(contents.getTarget()));
			} else {
				return String.format("%sが%sに投票した", convertAgent(subject), convertAgent(contents.getTarget()));
			}
		}
		return contents.getText();
	}

	@Override
	public String convertVote(Vote vote) {
		return "投票：" + convertAgent(vote.getTarget()) + "\t<-" + convertAgent(vote.getAgent());

	}

	@Override
	public String convertAttackVote(Vote vote) {
		return "襲撃：" + convertAgent(vote.getTarget()) + "\t<-" + convertAgent(vote.getAgent());

	}

	@Override
	public String convertMedium(Judge mediumResult) {
		return String.format("霊媒の結果%sは%sだった", convertAgent(mediumResult.getTarget()), convertSpecies(mediumResult.getResult()));
	}

	@Override
	public String convertDivined(Judge divineResult) {
		return String.format("占いの結果%sは%sだった", convertAgent(divineResult.getTarget()), convertSpecies(divineResult.getResult()));
	}

	@Override
	public String convertGuarded(Agent guardedAgent) {
		return String.format("%sを守った", convertAgent(guardedAgent));
	}

	@Override
	public String convertAttacked(Agent attackedAgent) {
		if (attackedAgent != null) {
			return String.format("%sが襲撃された", convertAgent(attackedAgent));
		} 
		else {
			return String.format("誰も襲撃されなかった");
		}
	}

	@Override
	public String convertExecuted(Agent exectedAgent) {
		return String.format("%sを追放した", convertAgent(exectedAgent));
	}

	@Override
	public String convertDead(Agent agent) {
		if (agent == null) {
			return "誰も死ななかった";
		}
		return String.format("%sが死んでいた", convertAgent(agent));
	}

	@Override
	public String convertTurn(int turn) {
		return "第" + turn + "ターン";
	}

	@Override
	public String aliveRemain(int agents) {
		return String.format("%d人生存", agents);
	}

	@Override
	public ImageIcon getImageIcon(Agent agent) {
		String imageUrl = agentResourceList.get(agent.getAgentIdx())[1];
		// System.out.println(imageUrl+"\t"+agentResourceList.get(agent.getAgentIdx())[0]);
		URL url = getClass().getClassLoader().getResource(imageUrl);
		ImageIcon icon = new ImageIcon(url);

		return icon;
	}

	@Override
	public String convertWinner(Team winner) {
		if (winner == Team.VILLAGER) {
			return "村に光が差し込んだ！すべての人狼を退治することに成功した！\n村人の勝利だ！";
		}
		else {
			return "村は深い闇に包まれた．村人はすべて人狼達の胃袋に収まり，\n人狼はまた新たな犠牲者を求めて村を去って行った．\n人狼の勝利だ！";
		}
	}

	public String convertWinner(Team winner, Team yourTeam) {
		String result = convertWinner(winner) + "\n";
		if (winner == yourTeam) {
			return result + "あなたは勝利しました";
		}
		else {
			return result + "あなたは敗北しました";
		}
	}

	@Override
	public String getFirstText(Agent agent, Role role) {
		if (role != null && agent != null) {
			Team team = role.getTeam();
			return String.format("あなたは %sです．与えられた役割は%sです．\n%sに所属しますので，%sの勝利に向けて行動してください．", convertAgent(agent), convertRole(role), convertTeam(team), convertTeam(team));
		} 
		else {
			return "";
		}
	}

	@Override
	public String getRoleInformation(Map<Role, Integer> roleCounter) {
		Role[] roleAry = new Role[] { Role.VILLAGER, Role.WEREWOLF, Role.SEER, Role.MEDIUM, Role.BODYGUARD, Role.POSSESSED, Role.FREEMASON };

		StringBuffer buf = new StringBuffer();
		buf.append("この村には\n");
		String separator = "";
		for (Role role : roleAry) {
			if (roleCounter.get(role) == 0) {
				continue;
			}
			buf.append(separator);
			buf.append(convertRole(role) + "が" + roleCounter.get(role) + "人");
			separator = "，";
		}
		buf.append("がいるらしい");
		return buf.toString();
	}

	@Override
	public String convertAttackedDay(int day) {
		return day + "日目に襲撃";
	}

	@Override
	public String convertExecutedDay(int day) {
		return day + "日目に追放";
	}

	@Override
	public String dayStart(int day) {
		if (day == 0) {
			return "村人による人狼対策会議が始まった．\n今日は，追放の投票及び襲撃は行われない．\n占い師は占うことができる．";
		}
		return day + "日目の朝が来た";
	}

	@Override
	public String convertText(String text) {
		return text;
	}

	@Override
	public String convertDeadDay(int day) {
		return day + "日目に死亡";
	}

	@Override
	public String convertCursedDay(int day) {
		return day + "日目に呪殺";
	}

	@Override
	public String convertVote() {
		return "投票";
	}

}
