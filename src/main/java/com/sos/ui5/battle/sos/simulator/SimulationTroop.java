package com.sos.ui5.battle.sos.simulator;

import java.io.Serializable;

public class SimulationTroop implements Serializable {
	private static final long serialVersionUID = 8564245612973415222L;
	private Integer id;
	private Integer number;
	
	public SimulationTroop() {
	}
	
	public SimulationTroop(Integer id, Integer number) {
		super();
		this.id = id;
		this.number = number;
	}	
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getNumber() {
		return number;
	}
	public void setNumber(Integer number) {
		this.number = number;
	}

}
