/**
 * DeclaredNotContentBuilder.java
 * 
 *
 */
package org.aiwolf.client.lib;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;

/**
 * <div lang="ja">否定断言発話ビルダークラス</div>
 * 
 * <div lang="en">Builder class for the utterance of a declared not.</div>
 * 
 * @author 
 *
 */
public class DeclaredNotContentBuilder extends ContentBuilder {

	/**
	 * <div lang="ja">否定断言のためのDeclaredNotContentBuilderを構築する(主語の指定なし)</div>
	 *
	 * <div lang="en">Constructs an DeclaredContentBuilder to utter an declared not (with no subject specified).</div>
	 * 
	 * @param target
	 *            <div lang="ja">被断言エージェント</div>
	 * 
	 *            <div lang="en">The agent declared.</div>
	 * @param role
	 *            <div lang="ja">被否定断言役職</div>
	 * 
	 *            <div lang="en">The role declared not.</div>
	 */
	public DeclaredNotContentBuilder(Agent target, Role role) {
		this(Content.UNSPEC, target, role);
	}

	/**
	 * <div lang="ja">断言のためのDeclaredNotContentBuilderを構築する</div>
	 *
	 * <div lang="en">Constructs an DeclaredContentBuilder to utter an declared not.</div>
	 * 
	 * @param subject
	 *            <div lang="ja">否定断言をするエージェント</div>
	 * 
	 *            <div lang="en">The agent who declared.</div>
	 * @param target
	 *            <div lang="ja">被断言エージェント</div>
	 * 
	 *            <div lang="en">The agent declared.</div>
	 * @param role
	 *            <div lang="ja">被否定断言役職</div>
	 * 
	 *            <div lang="en">The role declared not.</div>
	 */
	public DeclaredNotContentBuilder(Agent subject, Agent target, Role role) {
		topic = Topic.DECLAREDNOT;
		this.subject = subject;
		this.target = target;
		this.role = role;
	}

}
