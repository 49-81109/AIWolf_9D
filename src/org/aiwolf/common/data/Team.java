/**
 * Team.java
 * 
 * Copyright (c) 2014 人狼知能プロジェクト
 */
package org.aiwolf.common.data;

/**
 * <div lang="ja">陣営の列挙です。</div>
 * <div lang="ja">村人陣営/VILLAGER、人狼陣営/WEREWOLF、第3陣営(妖狐陣営)/OTHERS、陣営不定/ANY</div><br>
 * <div lang="en">Teams.</div>
 * 
 * @author tori and otsuki
 */
public enum Team {
	/**
	 * <div lang="ja">村人陣営</div>
	 * 
	 * <div lang="en">The team of Villager.</div>
	 */
	VILLAGER,
	/**
	 * <div lang="ja">人狼陣営</div>
	 * 
	 * <div lang="en">The team of WereWolf.</div>
	 */
	WEREWOLF,

	/**
	 * <div lang="ja">第三陣営(妖狐陣営)</div>
	 * 
	 * <div lang="en">The third team.</div>
	 */
	OTHERS,

	/**
	 * <div lang="ja">陣営不定</div>
	 * 
	 * <div lang="en">An arbitrary team.</div>
	 */
	ANY,

}
