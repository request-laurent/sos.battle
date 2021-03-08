package com.sos.ui5.battle.model;

public enum ParameterKey {
	AUTO_CREATE_DISCORD, VERSION;

	private ParameterKey() {
	}

	public String getValue() {
		return this.toString().toLowerCase();
	}
}
