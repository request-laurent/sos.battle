package com.sos.ui5.battle.sos.simulator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimulationFighter implements Serializable {
	private static final long serialVersionUID = 6443019763547270365L;
	private Map<Integer, Double> boost = new HashMap<>();
	private List<SimulationTroop> troops = new ArrayList<>(); 
	private List<SimulationHero> heroes = new ArrayList<SimulationHero>();
	private List<Integer> skillsJoiner = new ArrayList<Integer>();
	private boolean isMonster = false;
	private boolean statsIncluded = false;
	private List<Map<String, Object>> buffs = new ArrayList<Map<String, Object>>();
	
	public SimulationFighter() {
		super();
	}
	
	public Map<Integer, Double> getBoost() {
		return boost;
	}
	public void setBoost(Map<Integer, Double> boost) {
		this.boost = boost;
	}
	public boolean isMonster() {
		return isMonster;
	}
	public void setMonster(boolean isMonster) {
		this.isMonster = isMonster;
	}
	public List<SimulationHero> getHeroes() {
		return heroes;
	}
	public void setHeroes(List<SimulationHero> heroes) {
		this.heroes = heroes;
	}
	public List<SimulationTroop> getTroops() {
		return troops;
	}
	public void setTroops(List<SimulationTroop> troops) {
		this.troops = troops;
	}

	public List<Integer> getSkillsJoiner() {
		return skillsJoiner;
	}
	public void setSkillsJoiner(List<Integer> skillsJoiner) {
		this.skillsJoiner = skillsJoiner;
	}

	public boolean isStatsIncluded() {
		return statsIncluded;
	}

	public void setStatsIncluded(boolean statsIncluded) {
		this.statsIncluded = statsIncluded;
	}

	public  List<Map<String, Object>> getBuffs() {
		return buffs;
	}

	public void setBuffs( List<Map<String, Object>> buffs) {
		this.buffs = buffs;
	}

}
