package com.sos.ui5.battle.sos.simulator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SimulationHero implements Serializable {
	private static final long serialVersionUID = -7792526933801515859L;
	private Integer id;
	private List<Integer> skills = new ArrayList<Integer>();
	private List<Integer> equipment = new ArrayList<Integer>();
	private boolean selected = false;

	public SimulationHero() {
		super();
	}

	public SimulationHero(int id, List<Integer> skills, List<Integer> equipment) {
		this.id = id;
		this.skills = skills;
		this.equipment = equipment;
		this.selected = true;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public List<Integer> getSkills() {
		return skills;
	}

	public void setSkills(List<Integer> skills) {
		this.skills = skills;
	}

	public List<Integer> getEquipment() {
		return equipment;
	}

	public void setEquipment(List<Integer> equipment) {
		this.equipment = equipment;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

}
