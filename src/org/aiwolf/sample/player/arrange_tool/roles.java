package org.aiwolf.sample.player.arrange_tool;

public class roles {
	final private static String[] rolestr = {"村", "占", "霊", "狩", "共", "狼", "狂", "狐", "背", "埋"};
	final private static String[] rolefullstr = {"村人", "占い師", "霊能者", "狩人","共有者", "人狼", "狂信者", "妖狐", "背徳者", "埋毒者"};
	final static Role[] roletype = {Role.villager, Role.seer, Role.medium, Role.bodyguard, Role.freemason, Role.werewolf, Role.fanatic, Role.foxspirit, Role.immoralist, Role.toxic};
	
	public static Role getRoletype(String r) {
		for(int i = 0; i < roleCount(); i++) {
			if(r.equals(rolestr[i]) || r.equals(rolefullstr[i])) {
				return roletype[i];
			}
		}
		return Role.other;
	}
	
	public static int roleCount() {
		return roletype.length;
	}
	
	public static String roleName(Role role, boolean isfull) {
		for(int i = 0; i < roleCount(); i++) {
			if(role == roletype[i]) {
				if(isfull) {
					return rolefullstr[i];
				}
				else {
					return rolestr[i];
				}
			}
		}
		return "";
	}
	
	
}