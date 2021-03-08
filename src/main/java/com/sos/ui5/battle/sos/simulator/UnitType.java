package com.sos.ui5.battle.sos.simulator;

public enum UnitType {
	INFANTRY, CAVALRY, RANGED;
	
	public static UnitType decodeString(String val) {
		for (UnitType unitType : UnitType.values()) {
		  if (val.toUpperCase().equals(unitType.toString())) {
		  	return unitType;
		  }
		}
		return null;
	}
}
