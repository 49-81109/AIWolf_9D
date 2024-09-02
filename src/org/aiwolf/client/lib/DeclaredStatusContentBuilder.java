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
 * <div lang="ja">状態断定発話ビルダークラス</div>
 * 
 * <div lang="en">Builder class for the utterance of a declared.</div>
 * 
 * @author otsuki
 *
 */
public class DeclaredStatusContentBuilder extends ContentBuilder {

	/**
	 * <div lang="ja">状態断定のためのDeclaredContentBuilderを構築する(主語の指定なし)</div>
	 *
	 * <div lang="en">Constructs an DeclaredContentBuilder to utter an declared (with no subject specified).</div>
	 * 
	 * @param role
	 *            <div lang="ja">被断定役職</div>
	 * 
	 *            <div lang="en">The role declared.</div>
	 * @param status
	 *            <div lang="ja">被断定生存状態</div>
	 * 
	 *            <div lang="en">The status declared.</div>
	 */
	public DeclaredStatusContentBuilder(Role role, Status status) {
		this(Content.UNSPEC, role, status);
	}

	/**
	 * <div lang="ja">状態断定のためのDeclaredContentBuilderを構築する</div>
	 *
	 * <div lang="en">Constructs an DeclaredContentBuilder to utter an declared.</div>
	 * 
	 * @param subject
	 *            <div lang="ja">断定をするエージェント</div>
	 * 
	 *            <div lang="en">The agent who declared.</div>
	 * @param role
	 *            <div lang="ja">被断定役職</div>
	 * 
	 *            <div lang="en">The role declared.</div>
	 * @param status
	 *            <div lang="ja">被断定生存状態</div>
	 * 
	 *            <div lang="en">The status declared.</div>
	 */
	public DeclaredStatusContentBuilder(Agent subject, Role role, Status status) {
		topic = Topic.DECLAREDSTATUS;
		this.subject = subject;
		this.role = role;
		this.status = status;
	}

}
