package com.sos.ui5.battle.model;

public enum Role {
	USER, ADMIN, BATTLE;

	// Pour l'annotation @RolesAllowed
	public static final String user = "USER";
	public static final String admin = "ADMIN";
	public static final String alliance = "ALLIANCE";
	public static final String battle = "BATTLE";
	public static final String files = "FILES";
}