/**
 * DeclaredContentBuilder.java
 * 
 *
 */
package org.aiwolf.client.lib;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;

/**
 * <div lang="ja">断言発話ビルダークラス</div>
 * 
 * <div lang="en">Builder class for the utterance of a declared.</div>
 * 
 * @author 
 *
 */
public class DeclaredContentBuilder extends ContentBuilder {

	/**
	 * <div lang="ja">断言のためのDeclaredContentBuilderを構築する(主語の指定なし)</div>
	 *
	 * <div lang="en">Constructs an DeclaredContentBuilder to utter an declared (with no subject specified).</div>
	 * 
	 * @param target
	 *            <div lang="ja">被断言エージェント</div>
	 * 
	 *            <div lang="en">The agent declared.</div>
	 * @param role
	 *            <div lang="ja">被断言役職</div>
	 * 
	 *            <div lang="en">The role declared.</div>
	 */
	public DeclaredContentBuilder(Agent target, Role role) {
		this(Content.UNSPEC, target, role);
	}

	/**
	 * <div lang="ja">断言のためのDeclaredContentBuilderを構築する</div>
	 *
	 * <div lang="en">Constructs an DeclaredContentBuilder to utter an declared.</div>
	 * 
	 * @param subject
	 *            <div lang="ja">断言をするエージェント</div>
	 * 
	 *            <div lang="en">The agent who declared.</div>
	 * @param target
	 *            <div lang="ja">被断言エージェント</div>
	 * 
	 *            <div lang="en">The agent declared.</div>
	 * @param role
	 *            <div lang="ja">被断言役職</div>
	 * 
	 *            <div lang="en">The role declared.</div>
	 */
	public DeclaredContentBuilder(Agent subject, Agent target, Role role) {
		topic = Topic.DECLARED;
		this.subject = subject;
		this.target = target;
		this.role = role;
	}

}
