package com.sos.ui5.battle.sos.simulator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;

import com.sos.ui5.battle.utils.Tuple;

public class Simulation {
	private BattleType battleType;
	private int maxRound = 9999;
	private SimulationFighter attacker = new SimulationFighter();
	private SimulationFighter attacked = new SimulationFighter();

	public Simulation() {
		super();
	}

	public BattleType getBattleType() {
		return battleType;
	}

	public void setBattleType(BattleType battleType) {
		this.battleType = battleType;
	}

	public Fight calc() throws Exception {
		Fight fight = new Fight(this.getBattleType(), this.getMaxRound());

		calcFighter(fight.getAttacker(), this.getAttacker());
		calcFighter(fight.getAttacked(), this.getAttacked());

		fight.calc();

		return fight;
	}

	private void calcFighter(Fighter fighter, SimulationFighter simulationFighter) {
		// Boost
		for (Integer key : simulationFighter.getBoost().keySet()) {
			fighter.addBoost(key, simulationFighter.getBoost().get(key));
		}

		// Troops
		fighter.getTroops().clear();
		for (SimulationTroop troop : simulationFighter.getTroops()) {
			Integer val = fighter.getTroops().get(troop.getId());
			fighter.getTroops().put(troop.getId(), (val==null?0:val)+ troop.getNumber());
		}

		List<Integer> equipmentGroup = new ArrayList<>();
		// Heros
		for (SimulationHero simulationHero : simulationFighter.getHeroes()) {
			if (simulationHero.isSelected()) {
				JsonObject survivorRankMain = JsonUtil.jsonById(JsonUtil.survivorRankMain, "InternalId",
						"" + simulationHero.getId());
				int survivorId = survivorRankMain.getInt("SurvivorType");

				// Skills
				fighter.getSkills().put(survivorId, new ArrayList<Integer>(simulationHero.getSkills()));

				if (!simulationFighter.isStatsIncluded()) {
					// Ajout des skills du heros
					addBenefits(fighter, survivorRankMain.getJsonArray("BenefitTroop"));

					// Ajout des boost de l'équipement
					for (Integer equipmentId : simulationHero.getEquipment()) {
						if (equipmentId != null) {
							JsonObject survivorEquipment = JsonUtil.jsonById(JsonUtil.survivorEquipment, "InternalId",
									"" + equipmentId);
							equipmentGroup.add(survivorEquipment.getInt("SurvivorEquipmentGroup"));
							addBenefits(fighter, survivorEquipment.getJsonArray("TroopBenefits"));
						}
					}
				}
			}
		}

		if (!simulationFighter.isStatsIncluded()) {
			// Les bonus des groupe d'équipement (bonus quand 3 équipements)
			for (JsonValue j : JsonUtil.survivorEquipmentSuit) {
				JsonObject survivorEquipmentSuit = (JsonObject) j;
				int cpt = 0;
				cpt += equipmentGroup.contains(survivorEquipmentSuit.getInt("SurvivorEquipmentGroup_1")) ? 1 : 0;
				cpt += equipmentGroup.contains(survivorEquipmentSuit.getInt("SurvivorEquipmentGroup_2")) ? 1 : 0;
				cpt += equipmentGroup.contains(survivorEquipmentSuit.getInt("SurvivorEquipmentGroup_3")) ? 1 : 0;
				if (cpt >= 2) {
					addBenefits(fighter, survivorEquipmentSuit.getJsonArray("TroopBenefits_1"));
				}
				if (cpt >= 3) {
					addBenefits(fighter, survivorEquipmentSuit.getJsonArray("TroopBenefits_2"));
				}
			}
		}

		// Les skills des joiners
		for (Integer skillId : simulationFighter.getSkillsJoiner()) {
			fighter.addJoinerSkill(skillId);
		}
		
		//Les buffs
		for (Map<String, Object> buff: simulationFighter.getBuffs()) {
			if (buff.get("id")!=null) {
				fighter.addBoost(Integer.parseInt((String)buff.get("id")), (Double)buff.get("value"));
			}
		}
	}

	public void addBenefits(Fighter fighter, JsonArray benefits) {
		for (JsonValue benefitTroop : benefits) {
			JsonObject benefitTroopJ = (JsonObject) benefitTroop;
			// Double val = fighter.getBoost().get(benefitTroopJ.getInt("benefit_id"));
			// val = (val == null ? 0.0 : val) +
			// benefitTroopJ.getJsonNumber("benefit_value").doubleValue();
			Double val = benefitTroopJ.getJsonNumber("benefit_value").doubleValue();

			Integer key = benefitTroopJ.getInt("benefit_id");
			fighter.addBoost(key, val);
		}
	}

	public SimulationFighter getAttacker() {
		return attacker;
	}

	public void setAttacker(SimulationFighter attacker) {
		this.attacker = attacker;
	}

	public SimulationFighter getAttacked() {
		return attacked;
	}

	public void setAttacked(SimulationFighter attacked) {
		this.attacked = attacked;
	}

	public Simulation optimize() throws Exception {
		// Reset des heros
		for (SimulationHero simulationHero : getAttacker().getHeroes()) {
			simulationHero.setSelected(false);
		}

		// Recherche des meilleurs héros
		optimizeHero("zealots", getAttacker().getHeroes());
		optimizeHero("assault", getAttacker().getHeroes());
		optimizeHero("sniper", getAttacker().getHeroes());

		// Recherche du meilleur ratio
		List<Tuple<Integer, Integer>> lst = new ArrayList<>();

		maxTroop("infantry", lst);
		maxTroop("cavalry", lst);
		maxTroop("ranged", lst);

		if (lst.size() > 1) {
			Integer[] troop = MinimisationSOS.minimise(this, lst);
			this.getAttacker().getTroops().clear();
			for (int i = 0; i < lst.size(); i++) {
				this.getAttacker().getTroops().add(new SimulationTroop(lst.get(i).getX(), troop[i]));
			}
		}

		return this;
	}

	private void maxTroop(String type, List<Tuple<Integer, Integer>> lst) {
		int sum = 0;
		int max = 0;
		Integer id = null;
		for (SimulationTroop simulationTroop : getAttacker().getTroops()) {
			JsonObject unitStats = JsonUtil.jsonById(JsonUtil.unitStats, "InternalId", "" + simulationTroop.getId());
			if (unitStats.getString("Id").contains(type)) {
				sum += simulationTroop.getNumber();
				if (unitStats.getInt("BattlePower") > max) {
					max = unitStats.getInt("BattlePower");
					id = simulationTroop.getId();
				}
			}
		}
		if (id != null) {
			lst.add(new Tuple<Integer, Integer>(id, sum));
		}
	}

	private void optimizeHero(String type, List<SimulationHero> heroes) throws Exception {
		double scoreMax = -2;
		SimulationHero hero = null;

		for (SimulationHero simulationHero : heroes) {
			JsonObject survivorRankMain = JsonUtil.jsonById(JsonUtil.survivorRankMain, "InternalId",
					"" + simulationHero.getId());
			JsonObject survivor = JsonUtil.jsonById(JsonUtil.survivors, "InternalId",
					"" + survivorRankMain.getInt("SurvivorType"));
			if (survivor.getString("Profession").equals(type)) {
				simulationHero.setSelected(true);
				Fight fight = new Fight(this.getBattleType(), this.getMaxRound());
				calcFighter(fight.getAttacked(), this.getAttacked());
				calcFighter(fight.getAttacker(), this.getAttacker());
				fight.calc();

				Double score = (Double) fight.getResult().get(0).get("score");
				// Si on est sur le piege ou si le score est trop proche de 100% on utilise le
				// nombre de mort à la place
				if (score >= 0.99) {
					score = 1.0 * (Integer) fight.getResult().get(2).get("total");
				} else if (score <= -0.99) {
					score = 1.0 * (Integer) fight.getResult().get(1).get("total");
				}
				if (score > scoreMax) {
					scoreMax = score;
					hero = simulationHero;
				}
				simulationHero.setSelected(false);
			}
		}

		if (hero != null) {
			hero.setSelected(true);
		}
	}

	public int getMaxRound() {
		return maxRound;
	}

	public void setMaxRound(int maxRound) {
		this.maxRound = maxRound;
	}
}
