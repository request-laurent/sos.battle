package com.sos.ui5.battle.sos.simulator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Fighter implements Serializable {
	public Fighter() {
		super();
	}

	private static final long serialVersionUID = 2572365371199883881L;
	private static final Boolean DEBUG = false;

	private boolean isMonster = false;
	private BattleType battleType;

	// Stats id boost/valeur
	@JsonIgnore
	private Map<Integer, Double> boost = new HashMap<>();
	// Stats utilisées
	@JsonIgnore
	private Map<Integer, Double> usedBoost = new HashMap<>();
	// Toupes id unit/nombre
	@JsonIgnore
	private Map<Integer, Integer> troops = new HashMap<>();

	// Toupes type/nombre
	@JsonIgnore
	private Map<UnitType, Integer> troopsByType = new HashMap<>();

	// Attaque id unit/valeur
	@JsonIgnore
	private Map<Integer, Double> attackByTroop = new HashMap<>();
	// Defense id unit/valeur
	@JsonIgnore
	private Map<Integer, Double> defenseByTroop = new HashMap<>();

	// Attaque type/valeur
	@JsonIgnore
	private Map<UnitType, Double> attackByType = new HashMap<>();
	// Attaque type/valeur
	@JsonIgnore
	private Map<UnitType, Double> defenseByType = new HashMap<>();

	// Skills id
	@JsonIgnore
	private Map<Integer, List<Integer>> skills = new HashMap<>();
	// Skills Object
	@JsonIgnore
	private List<Skill> skillsDetails = new ArrayList<>();

	private List<Map<UnitType, RoundUnit>> round = new ArrayList<Map<UnitType, RoundUnit>>();

	public Map<UnitType, Double> getAttackByType() {
		return attackByType;
	}

	public Map<UnitType, Double> getDefenseByType() {
		return defenseByType;
	}

	public void calc(Fighter opponent) throws Exception {
		calcSkills();
		for (Integer troopId : troops.keySet()) {
			calcByTroops(troopId, opponent);
		}
		calcByType();
	}

	private void calcSkills() throws Exception {
		for (Integer survivorId : skills.keySet()) {
			// On recherche le Survivor
			JsonObject survivor = JsonUtil.jsonById(JsonUtil.survivors, "InternalId", survivorId.toString());
			// Récupération du type
			UnitType unitType = null;
			for (JsonValue professionTag : survivor.getJsonArray("ProfessionTag")) {
				unitType = toUnitType(
						JsonUtil.jsonById(JsonUtil.survivorTag, "InternalId", professionTag.toString()).getString("Id"));
				if (unitType != null) {
					break;
				}
			}

			if (unitType == null) {
				throw new Exception("Unitype not found for " + survivor.getString("Id"));
			}

			for (Integer skillId : skills.get(survivorId)) {
				if (skillId != null) {
					// On recherche le skill new
					JsonObject survivorSkillMain = JsonUtil.jsonById(JsonUtil.survivorSkillMain, "InternalId",
							skillId.toString());
					int skillNew = survivorSkillMain.getInt("SkillNew");
					// On récupère la liste des skills associés
					JsonObject survivorSlgSkill = JsonUtil.jsonById(JsonUtil.survivorSlgSkill, "InternalId", "" + skillNew);
					for (JsonValue skillEffectId : survivorSlgSkill.getJsonArray("SkillEffect_ID")) {
						// Récupération de l'effet
						JsonObject survivoSlgEffect = JsonUtil.jsonById(JsonUtil.survivorSlgEffect, "InternalId",
								"" + skillEffectId);

						Skill skill = new Skill(unitType, survivorSlgSkill.getInt("SkillStart"));
						skill.fromJson(survivoSlgEffect);
						skillsDetails.add(skill);
						if (DEBUG) {
							System.out.println(survivor.getString("Id") + "\t" + skillEffectId + "\t" + skill.toString() + "\t"
									+ survivorSkillMain.getString("Id"));
						}
					}
				}
			}

			skillsDetails = skillsDetails.stream().sorted((o1, o2) -> {
				// Le 101 à la fin
				if (o1.getEffect() == 101) {
					return 1;
				} else if (o2.getEffect() == 101) {
					return -1;
				}
				return Integer.compare(o1.getOrder(), o2.getOrder());
			}).collect(Collectors.toList());

			// System.out.println(skillsDetails);

		}
	}

	private void calcByType() {
		calcByType(UnitType.INFANTRY);
		calcByType(UnitType.CAVALRY);
		calcByType(UnitType.RANGED);
	}

	private void calcByType(UnitType unitType) {
		int nb = 0;
		// Quand il y a plusieurs troop du meme type on prend la puissance pondéré
		double totalAttack = 0;
		double totalDefense = 0;
		for (Integer troopId : troops.keySet()) {
			JsonObject unit = JsonUtil.jsonById(JsonUtil.unitStats, "InternalId", "" + troopId);
			if (unitType == toUnitType(unit.getString("Id"))) {
				totalAttack += troops.get(troopId) * attackByTroop.get(troopId);
				totalDefense += troops.get(troopId) * defenseByTroop.get(troopId);
				nb += troops.get(troopId);
			}
		}

		double attack = 0;
		double defense = 0;
		for (Integer troopId : troops.keySet()) {
			JsonObject unit = JsonUtil.jsonById(JsonUtil.unitStats, "InternalId", "" + troopId);
			if (unitType == toUnitType(unit.getString("Id"))) {
				if (attack == 0) {
					attack = 1;
					defense = 1;
				}
				attack *= Math.pow(attackByTroop.get(troopId), troops.get(troopId) * attackByTroop.get(troopId) / totalAttack);
				defense *= Math.pow(defenseByTroop.get(troopId),
						troops.get(troopId) * defenseByTroop.get(troopId) / totalDefense);
			}
		}

		attackByType.put(unitType, attack);
		defenseByType.put(unitType, defense);
		troopsByType.put(unitType, nb);
	}

	private void calcByTroops(Integer troopId, Fighter opponent) {
		// Récupération de la class
		JsonObject unit = JsonUtil.jsonById(JsonUtil.unitStats, "InternalId", "" + troopId);

		double attackRet = 0;
		double defenseRet = 0;

		double attack = unit.getInt("Attack");
		double damage = unit.getInt("Damage");

		double health = unit.getInt("Health");
		double defense = unit.getInt("Defense");

		UnitType unitType = toUnitType(unit.getString("Id"));

		double troopDefense = 0;
		double troopLethality = 0;
		double troopHealth = 0;
		double troopAttack = 0;

		if (unitType == UnitType.INFANTRY) {
			troopAttack += beneficeCalc("calc_infantry_commando_attack", opponent);
			troopLethality += beneficeCalc("calc_infantry_commando_damage", opponent);
			troopHealth += beneficeCalc("calc_infantry_commando_health", opponent);
			troopDefense += beneficeCalc("calc_infantry_commando_defense", opponent);
		} else if (unitType == UnitType.RANGED) {
			troopAttack += beneficeCalc("calc_ranged_artilleryman_attack", opponent);
			troopLethality += beneficeCalc("calc_ranged_artilleryman_damage", opponent);
			troopHealth += beneficeCalc("calc_ranged_artilleryman_health", opponent);
			troopDefense += beneficeCalc("calc_ranged_artilleryman_defense", opponent);
		} else if (unitType == UnitType.CAVALRY) {
			troopAttack += beneficeCalc("calc_cavalry_velociraptor_rider_attack", opponent);
			troopLethality += beneficeCalc("calc_cavalry_velociraptor_rider_damage", opponent);
			troopHealth += beneficeCalc("calc_cavalry_velociraptor_rider_health", opponent);
			troopDefense += beneficeCalc("calc_cavalry_velociraptor_rider_defense", opponent);
		}

		attackRet = attack * (1 + troopAttack) * damage * (1 + troopLethality) / 100.0;
		defenseRet = health * (1 + troopHealth) * defense * (1 + troopDefense) / 100.0;

		attackByTroop.put(troopId, attackRet);
		defenseByTroop.put(troopId, defenseRet);
	}

	private double beneficeCalc(String id, Fighter opponent) {
		JsonObject benefitCalc = JsonUtil.jsonById(JsonUtil.benefitCalc, "Id", id);

		double base = 0;

		base += this.beneficeCalc(benefitCalc, "PercentParam");
		base += opponent.beneficeCalc(benefitCalc, "OppPercentParam");

		return base;
	}

	public double beneficeCalc(JsonObject benefitCalc, String key) {
		double base = 0;
		for (JsonValue benefit : benefitCalc.getJsonArray(key)) {
			int benefitId = ((JsonNumber) benefit).intValue();
			Double val = boost.get(benefitId);
			if (val != null) {
				usedBoost.put(benefitId, val);
				if (DEBUG) {
					// System.out.println("Boost " + id + " / " + benefit + " =>" + val);
				}
				base += val;
			}
		}
		return base;
	}

	private UnitType toUnitType(String id) {
		if (id.toUpperCase().contains(UnitType.INFANTRY.toString())) {
			return UnitType.INFANTRY;
		} else if (id.toUpperCase().contains(UnitType.CAVALRY.toString())) {
			return UnitType.CAVALRY;
		} else if (id.toUpperCase().contains(UnitType.RANGED.toString())) {
			return UnitType.RANGED;
		}
		// System.err.println("Troupes inconnues");
		return null;
	}

	// private double getDouble(Map<String, Double> map, String key) {
	// Double ret = map.get(key);
	// return ret == null ? 0 : ret;
	// }

	public int getSumArmy() {
		return troopsByType.values().stream().reduce(0, (a, b) -> (a == null ? 0 : a) + (b == null ? 0 : b));
	}

	public Map<UnitType, Integer> getTroopsByType() {
		return troopsByType;
	}

	public List<Map<UnitType, RoundUnit>> getRound() {
		return round;
	}

	public boolean hasBikers() {
		for (Integer id : troops.keySet()) {
			JsonObject unit = JsonUtil.jsonById(JsonUtil.unitStats, "InternalId", "" + id);
			if (unit.getInt("TroopClass") == 1400005) {
				return true;
			}
		}
		return false;
	}

	public boolean hasSnipers() {
		for (Integer id : troops.keySet()) {
			JsonObject unit = JsonUtil.jsonById(JsonUtil.unitStats, "InternalId", "" + id);
			if (unit.getInt("TroopClass") == 1400004) {
				return true;
			}
		}
		return false;
	}

	public boolean isMonster() {
		return isMonster;
	}

	public void setMonster(boolean isMonster) {
		this.isMonster = isMonster;
	}

	public Map<Integer, List<Integer>> getSkills() {
		return skills;
	}

	public void setSkills(Map<Integer, List<Integer>> skills) {
		this.skills = skills;
	}

	public Map<Integer, Integer> getTroops() {
		return troops;
	}

	public void setTroops(Map<Integer, Integer> troops) {
		this.troops = troops;
	}

	public Map<Integer, Double> getBoost() {
		return boost;
	}

	public void setBoost(Map<Integer, Double> boost) {
		this.boost = boost;
	}

	public List<Skill> getSkillsDetails() {
		return skillsDetails;
	}

	public void setSkillsDetails(List<Skill> skillsDetails) {
		this.skillsDetails = skillsDetails;
	}

	public Map<String, Object> getResult() {
		int start = getRound().get(0).get(UnitType.INFANTRY).getNbUnit()
				+ getRound().get(0).get(UnitType.CAVALRY).getNbUnit() + getRound().get(0).get(UnitType.RANGED).getNbUnit();
		int last = getRound().size() - 1;
		int end = getRound().get(last).get(UnitType.INFANTRY).getNbUnit()
				+ getRound().get(last).get(UnitType.CAVALRY).getNbUnit()
				+ getRound().get(last).get(UnitType.RANGED).getNbUnit();
		int total = start - end;

		JsonObject battleWounded = JsonUtil.jsonById(JsonUtil.battleWounded, "Id", getBattleType().getCode());
		double woundRate = battleWounded.getJsonNumber("WoundRate").doubleValue();
		double minorWoundRate = battleWounded.getJsonNumber("MinorWoundRate").doubleValue();

		int minorWound = (int) Math.round(minorWoundRate * total);
		int wound;
		int dead;
		if (woundRate + minorWoundRate == 1) {
			wound = total - minorWound;
			dead = 0;
		} else {
			wound = (int) Math.round(woundRate * total);
			dead = total - wound - minorWound;
		}

		Map<String, Object> ret = new HashMap<>();
		ret.put("start", start);
		ret.put("end", end);
		ret.put("total", total);
		ret.put("dead", dead);
		ret.put("wound", wound);
		ret.put("minorWound", minorWound);
		ret.put("startInf", getRound().get(0).get(UnitType.INFANTRY).getNbUnit());
		ret.put("startCav", getRound().get(0).get(UnitType.CAVALRY).getNbUnit());
		ret.put("startRan", getRound().get(0).get(UnitType.RANGED).getNbUnit());
		ret.put("endInf", getRound().get(last).get(UnitType.INFANTRY).getNbUnit());
		ret.put("endCav", getRound().get(last).get(UnitType.CAVALRY).getNbUnit());
		ret.put("endRan", getRound().get(last).get(UnitType.RANGED).getNbUnit());

		return ret;
	}

	public Map<Integer, Double> getUsedBoost() {
		return usedBoost;
	}

	public BattleType getBattleType() {
		return battleType;
	}

	public void setBattleType(BattleType battleType) {
		this.battleType = battleType;
	}

	public void addBoost(Integer key, Double val) {
		// Vérification si le boost s'applique ici
		JsonObject benefit = JsonUtil.jsonById(JsonUtil.benefits, "InternalId", "" + key);
		if (benefit.getJsonArray("BattleType").stream().anyMatch(v -> {
			String code = ((JsonString) v).getString();
			return "all".equals(code) || getBattleType().getCode().equals(code);
		})) {
			Double old = getBoost().get(key);
			if (old != null) {
				getBoost().put(key, old.doubleValue() + val);
			} else {
				getBoost().put(key, val);
			}
		} else {
			// System.err.println("Boost rejeté : " + key);
		}
	}

	public void addJoinerSkill(Integer skillId) {
		JsonObject survivorSkillMain = JsonUtil.jsonById(JsonUtil.survivorSkillMain, "InternalId", skillId.toString());
		for (JsonValue obj : survivorSkillMain.getJsonArray("Benefits")) {
			JsonObject benefits = (JsonObject) obj;
			// Seuls les skills de défense sont utilisés
			if (benefits.getInt("benefit_id") == 300662 || benefits.getInt("benefit_id") == 300869
					|| benefits.getInt("benefit_id") == 300781) {
				double value = benefits.getJsonNumber("benefit_value").doubleValue();
				// On force en defense de troupe / 9
				Skill skill = new Skill(UnitType.INFANTRY, 1);
				skill.setValue(0.36 * (value * 100));
				skill.setEffect(302);
				skill.setRoundLag(0);
				skill.setEffectTarget(30);
				skill.setTarget(null);
				skill.setTrigger(null);
				skillsDetails.add(skill);
			}
		}
	}

	public void addBuff(Integer buff) {
		JsonObject cityBuff = JsonUtil.jsonById(JsonUtil.city_buff_main, "InternalId", buff.toString());
		for (String key : cityBuff.getJsonObject("Benefit").keySet()) {
			addBoost(Integer.parseInt(key), cityBuff.getJsonObject("Benefit").getJsonNumber(key).doubleValue());
		}
	}

}
