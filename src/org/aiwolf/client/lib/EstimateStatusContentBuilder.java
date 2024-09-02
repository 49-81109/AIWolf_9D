/**
 * EstimateContentBuilder.java
 * 
 * Copyright (c) 2016 人狼知能プロジェクト
 */
package org.aiwolf.client.lib;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Status;

/**
 * <div lang="ja">状態推測発話ビルダークラス</div>
 * 
 * <div lang="en">Builder class for the utterance of a estimation.</div>
 * 
 * @author otsuki
 *
 */
public class EstimateStatusContentBuilder extends ContentBuilder {

	/**
	 * <div lang="ja">状態推測のためのEstimateContentBuilderを構築する(主語の指定なし)</div>
	 *
	 * <div lang="en">Constructs an EstimateContentBuilder to utter an estimation (with no subject specified).</div>
	 * 
	 * @param role
	 *            <div lang="ja">被推測役職</div>
	 * 
	 *            <div lang="en">The role estimated.</div>
	 * @param status
	 *            <div lang="ja">被推測生存状態</div>
	 * 
	 *            <div lang="en">The status estimated.</div>
	 */
	public EstimateStatusContentBuilder(Role role, Status status) {
		this(Content.UNSPEC, role, status);
	}

	/**
	 * <div lang="ja">状態推測のためのEstimateContentBuilderを構築する</div>
	 *
	 * <div lang="en">Constructs an EstimateContentBuilder to utter an estimation.</div>
	 * 
	 * @param subject
	 *            <div lang="ja">推測をするエージェント</div>
	 * 
	 *            <div lang="en">The agent who estimates.</div>
	 * @param role
	 *            <div lang="ja">被推測役職</div>
	 * 
	 *            <div lang="en">The role estimated.</div>
	 * @param status
	 *            <div lang="ja">被推測生存状態</div>
	 * 
	 *            <div lang="en">The status estimated.</div>
	 */
	public EstimateStatusContentBuilder(Agent subject, Role role, Status status) {
		topic = Topic.ESTIMATESTATUS;
		this.subject = subject;
		this.role = role;
		this.status = status;
	}

}
