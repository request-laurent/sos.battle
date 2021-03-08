package com.sos.ui5.battle.sos.simulator;

import java.util.HashMap;
import java.util.Map;

public class RoundUnit {
	int nbUnit = 0;	
	int round;
	Map<UnitType, RoundDetail> roundDetails = new HashMap<UnitType, RoundDetail>();
	
	public int getNbUnit() {
		return nbUnit;
	}
	public void setNbUnit(int nbUnit) {
		this.nbUnit = nbUnit;
	}
	public Map<UnitType, RoundDetail> getRoundDetails() {
		return roundDetails;
	}
	public void setRoundDetails(Map<UnitType, RoundDetail> roundDetails) {
		this.roundDetails = roundDetails;
	}
	public int getRound() {
		return round;
	}
	public void setRound(int round) {
		this.round = round;
	} 


}
