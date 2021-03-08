package com.sos.ui5.battle.sos.simulator;

import javax.json.JsonObject;

public class Skill {
	private UnitType unitType;
	private int roundFreq;

	private double value;
	private int effect;
	private int effectTarget;
	private int roundLag;
	private UnitType trigger = null;
	private UnitType target = null;
	private int order = 1;
	private int effectRelation;

	private double protect;

	private int lastRound = -1;

	public Skill(UnitType unitType, int roundFreq) {
		this.unitType = unitType;
		this.roundFreq = roundFreq;
	}

	public void fromJson(JsonObject json) {
		this.value = json.getJsonNumber("EffectAttr").doubleValue();
		this.effect = json.getInt("EffectResult");
		this.roundLag = json.getInt("EffectLag");
		this.effectTarget = json.getInt("EffectTarget");
		this.target = UnitType.decodeString(json.getString("EffectTargetData"));
		this.trigger = UnitType.decodeString(json.getString("EffectTriggerData"));
		this.order = json.getInt("EffectLag");
		this.effectRelation= json.getInt("EffectRelation");
	}

	public static double damage(Fighter fighter, UnitType unitType, UnitType vs, int round) {
		double coef = 1;

		for (Skill skill : fighter.getSkillsDetails()) {
			if (skill.condition(fighter, unitType, vs, false, round, skill)) {
				switch (skill.getEffect()) {
				case 101:
					if (unitType == skill.getUnitType()) {
						if (skill.getLastRound() == round) {
							coef = coef - 1;
							// Uniquement l'ajout
						}
						// Extra damage => S'ajoute aux domage existant
						coef = coef * (1+skill.getValue() / 100.0);
						skill.setLastRound(round);
					}
					break;
				case 201:
					// damage bonus
					coef = coef + skill.getValue() / 100.0;
					break;
				case 221:
					// damage bonus moyen sur toutes les unités
					coef = coef + (skill.getValue() / 3) / 100.0;
					break;
				case 301:
					// damage bonus
					coef = coef + skill.getValue() / 100.0;
					break;
				case 211:
					// damage bonus => Le 1.4 ???
					if (unitType == skill.getUnitType()) {
						coef = coef + skill.getValue()*1.4 / 100.0;
					}
					break;
				}
			}
		}
		return coef;
	}

	public static double defense(Fighter fighter, UnitType unitType, UnitType vs, int round) {
		double coef = 0;

		for (Skill skill : fighter.getSkillsDetails()) {
			if (skill.condition(fighter, unitType, vs, true, round, skill)) {
				switch (skill.getEffect()) {
				case 202:
				case 302:
					// Augmentation de la defense					
					//if (skill.getEffectTarget() != 0) {
						//TODO Quand effectTarget est à 0 parfois cele ne l'applique pas (Zach), certainement lié à EffectTarget
						coef = coef + skill.getValue() / 100.0;
					//}
					break;
				}
			}
		}
		return coef;
	}

	public static int protect(Fighter fighter, UnitType unitType, UnitType vs, int round, int dead) {
		double protect = 0;
		double remainDead = dead;
		for (Skill skill : fighter.getSkillsDetails()) {
			if (skill.condition(fighter, unitType, vs, true, round, skill)) {
				switch (skill.getEffect()) {
				case 801:
					// Augmentation de la defense
					if (round != skill.getLastRound()) {
						skill.setProtect(dead * skill.getValue() / 100);
						skill.setLastRound(round);
					}
					if (remainDead > 0 && skill.getProtect() > 0) {
						if (skill.getProtect() > remainDead) {
							protect = +remainDead;
							skill.setProtect(skill.getProtect() - remainDead);
						} else {
							protect = skill.getProtect();
							skill.setProtect(0);
						}
						remainDead = remainDead - protect;
					}
					break;
				case 901:
					if (round != skill.getLastRound()) {
						skill.setProtect(dead * skill.getValue() / 100);
						skill.setLastRound(round);
					}
					if (remainDead > 0 && skill.getProtect() > 0) {
						if (skill.getProtect() > remainDead) {
							protect = +remainDead;
							skill.setProtect(skill.getProtect() - remainDead);
						} else {
							protect = skill.getProtect();
							skill.setProtect(0);
						}
						remainDead = remainDead - protect;
					}
					break;
				}
			}
		}
		return (int) protect;
	}

	private boolean condition(Fighter fighter, UnitType unitType, UnitType vs, boolean isDefense, int round, Skill skill) {
//		if (round==5 && this.getRoundFreq()==5 && skill.getEffect()==202) {
//			System.out.println("5");
//		}
		
		//Toutes les unités sauf celle du skill
		if (skill.getEffectTarget()==31 && skill.getUnitType() == unitType) {
			return false;
		}
		
		//Si l'unité est décimée, alors le hero n'a plus d'effet
		if (fighter.getRound().get(round-1).get(skill.getUnitType()).getNbUnit()<=0 && skill.getEffectRelation()==2) {
			return false;
		}
		
		// Vérification du round
		if ((round - this.getRoundLag()) > 0 && (round - this.getRoundLag()) % this.getRoundFreq() == 0) {
			// Vérification de l'unité adverse
			if (this.getTarget() == null || this.getTarget() ==  (skill.getEffectTarget() == 20 ? vs : unitType)) {
				// Vérification de l'unité source
				if (this.getTrigger() == null || this.getTrigger() == vs) {
					// Vérification encore vivant
					if (fighter.getRound().get(round - 1).get(unitType).getNbUnit() > 0) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public static boolean needContinue(Fighter fighter, UnitType unitType, UnitType vs, int round) {
		for (Skill skill : fighter.getSkillsDetails()) {
			if (skill.condition(fighter, unitType, vs, false, round, skill)) {
				if (skill.getEffectTarget() == 40 && skill.getEffect() == 101 && skill.getUnitType() == unitType) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return unitType + "\t " + roundFreq + "\t" + roundLag + "\t" + value + "\t" + effect + "\t" + effectTarget + "\t"
				+ (target == null ? "" : target);
	}

	public UnitType getUnitType() {
		return unitType;
	}

	public void setUnitType(UnitType unitType) {
		this.unitType = unitType;
	}

	public int getRoundFreq() {
		return roundFreq;
	}

	public void setRoundFreq(int roundFreq) {
		this.roundFreq = roundFreq;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public int getEffect() {
		return effect;
	}

	public void setEffect(int effect) {
		this.effect = effect;
	}

	public int getRoundLag() {
		return roundLag;
	}

	public void setRoundLag(int roundLag) {
		this.roundLag = roundLag;
	}

	public int getEffectTarget() {
		return effectTarget;
	}

	public void setEffectTarget(int effectTarget) {
		this.effectTarget = effectTarget;
	}

	public int getLastRound() {
		return lastRound;
	}

	public void setLastRound(int lastRound) {
		this.lastRound = lastRound;
	}

	public UnitType getVs() {
		return target;
	}

	public void setVs(UnitType vs) {
		this.target = vs;
	}

	public double getProtect() {
		return protect;
	}

	public void setProtect(double protect) {
		this.protect = protect;
	}

	public UnitType getTrigger() {
		return trigger;
	}

	public void setTrigger(UnitType trigger) {
		this.trigger = trigger;
	}

	public UnitType getTarget() {
		return target;
	}

	public void setTarget(UnitType target) {
		this.target = target;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public int getEffectRelation() {
		return effectRelation;
	}

	public void setEffectRelation(int effectRelation) {
		this.effectRelation = effectRelation;
	}
}