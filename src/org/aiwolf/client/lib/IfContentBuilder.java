/**
 * IfContentBuilder.java
 * 
 * 
 */
package org.aiwolf.client.lib;

import java.util.ArrayList;
import java.util.Arrays;

import org.aiwolf.common.data.Agent;

/**
 * <div lang="ja">仮定発話ビルダークラス</div>
 * 
 * <div lang="en">Builder class for the utterance of a condition.</div>
 * 
 * @author 
 *
 */
public class IfContentBuilder extends ContentBuilder {

	/**
	 * <div lang="ja">行動の仮定条件を述べるためのIfContentBuilderを構築する(主語の指定なし)</div>
	 *
	 * <div lang="en">Constructs a IfContentBuilder to express the condition for an action (with no subject specified).</div>
	 * 
	 * @param reason
	 *            <div lang="ja">仮定を表す{@code Content}</div>
	 *
	 *            <div lang="en">{@code Content} representing the condition.</div>
	 * @param action
	 *            <div lang="ja">行動を表す{@code Content}</div>
	 *
	 *            <div lang="en">{@code Content} representing the action.</div>
	 */
	public IfContentBuilder(Content condition, Content action) {
		this(Content.UNSPEC, condition, action);
	}

	/**
	 * <div lang="ja">行動の仮定条件を述べるためのIfContentBuilderを構築する</div>
	 *
	 * <div lang="en">Constructs a IfContentBuilder to express the condition for an action.</div>
	 * 
	 * @param subject
	 *            <div lang="ja">仮定を述べるエージェント</div>
	 *
	 *            <div lang="en">The agent who expresses the condition.</div>
	 * @param reason
	 *            <div lang="ja">仮定を表す{@code Content}</div>
	 *
	 *            <div lang="en">{@code Content} representing the condition.</div>
	 * @param action
	 *            <div lang="ja">行動を表す{@code Content}</div>
	 *
	 *            <div lang="en">{@code Content} representing the action.</div>
	 */
	public IfContentBuilder(Agent subject, Content condition, Content action) {
		topic = Topic.OPERATOR;
		operator = Operator.IF;
		this.subject = subject;
		contentList = new ArrayList<>(Arrays.asList(condition, action));
	}

}
