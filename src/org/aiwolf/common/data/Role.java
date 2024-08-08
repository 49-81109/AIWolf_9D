/**
 * Role.java
 * 
 * Copyright (c) 2014 人狼知能プロジェクト
 */
package org.aiwolf.common.data;

/**
 * 
 * <div lang="ja">プレイヤーの役職の列挙です。</div>
 * <div lang="ja">村人/VILLAGER、占い師/SEER、霊能者/MEDIUM、狩人/BODYGUARD、共有者/FREEMASON、埋毒者/TOXIC、人狼/WEREWOLF、狂人/POSSESSED、狂信者/FANATIC、妖狐/FOX、背徳者/IMMORALIST、役職不定/ANY</div>
 * <br><div lang="en">Roles of Player.</div>
 * 
 * @author tori and otsuki
 */
public enum Role {

	/**
	 * <div lang="ja">村人です。</div>
	 * 
	 * <div lang="en">The role Villager.</div>
	 */
	VILLAGER(Team.VILLAGER, Species.HUMAN),
	
	/**
	 * <div lang="ja">占い師です。</div>
	 * 
	 * <div lang="en">The role Seer.</div>
	 */
	SEER(Team.VILLAGER, Species.HUMAN),
	
	/**
	 * <div lang="ja">霊能者です。</div>
	 * 
	 * <div lang="en">The role Medium.</div>
	 */
	MEDIUM(Team.VILLAGER, Species.HUMAN),

	/**
	 * <div lang="ja">狩人です。</div>
	 * 
	 * <div lang="en">The role Bodyguard.</div>
	 */
	BODYGUARD(Team.VILLAGER, Species.HUMAN),

	/**
	 * <div lang="ja">共有者です。</div>
	 * 
	 * <div lang="en">The role Freemason.</div>
	 */
	FREEMASON(Team.VILLAGER, Species.HUMAN),

	/**
	 * <div lang="ja">埋毒者です。</div>
	 * 
	 * <div lang="en">The role Toxic.</div>
	 */
	TOXIC(Team.VILLAGER, Species.HUMAN),

	/**
	 * <div lang="ja">人狼です。</div>
	 * 
	 * <div lang="en">The role WereWolf.</div>
	 */
	WEREWOLF(Team.WEREWOLF, Species.WEREWOLF),

	/**
	 * <div lang="ja">狂人です。</div>
	 * 
	 * <div lang="en">The role Possessed.</div>
	 */
	POSSESSED(Team.WEREWOLF, Species.HUMAN),

	/**
	 * <div lang="ja">狂信者です。</div>
	 * 
	 * <div lang="en">The role Fanatic.</div>
	 */
	FANATIC(Team.WEREWOLF, Species.HUMAN),
	
	/**
	 * <div lang="ja">妖狐です。</div>
	 * 
	 * <div lang="en">The role Fox.</div>
	 */
	FOX(Team.OTHERS, Species.HUMAN),

	/**
	 * <div lang="ja">背徳者です。</div>
	 * 
	 * <div lang="en">The role Immoralist.</div>
	 */
	IMMORALIST(Team.OTHERS, Species.HUMAN),
	
	
	/**
	 * <div lang="ja">役職不定</div>
	 * 
	 * <div lang="en">An arbitrary role.</div>
	 */
	ANY(Team.ANY, Species.ANY);

	private Team teamType;
	private Species species;

	private Role(Team teamType, Species species) {
		this.teamType = teamType;
		this.species = species;
	}

	/**
	 * <div lang="ja">役職の属している陣営を返します。</div>
	 * 
	 * <div lang="en"><!--Insert English Document--></div>
	 * 
	 * @return
	 * 
	 * 		<div lang="ja">役職の属している陣営</div>
	 * 
	 *         <div lang="en">Team type</div>
	 */
	public Team getTeam() {
		return teamType;
	}

	/**
	 * <div lang="ja">役職の属している種族を返します。</div>
	 * 
	 * <div lang="en"><!--Insert English Document--></div>
	 * 
	 * @return
	 * 
	 * 		<div lang="ja">役職の属している種族</div>
	 * 
	 *         <div lang="en">Species</div>
	 */
	public Species getSpecies() {
		return species;
	}
}
