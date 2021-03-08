package com.sos.ui5.battle.sos.simulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Fight {
	private Fighter attacker = new Fighter();
	private Fighter attacked = new Fighter();
	private BattleType battleType;
	private int maxRound = 9999;

	public Fight(BattleType battleType, int maxRound) {
		this.battleType = battleType;
		attacker.setBattleType(battleType);
		attacked.setBattleType(battleType.getOponent());
		this.maxRound = maxRound;
	}

	private void round() {
		int armyMin = this.attacker.getSumArmy() < this.attacked.getSumArmy() ? this.attacker.getSumArmy()
				: this.attacked.getSumArmy();

		boolean end = false;
		int round = 0;
		while (!end) {
			round(this.attacker, this.attacked, armyMin, round);
			round(this.attacked, this.attacker, armyMin, round);

			boolean end1 = calcNbUnit(this.attacker, this.attacked, round);
			boolean end2 = calcNbUnit(this.attacked, this.attacker, round);

			end = end1 || end2;

			round++;
			if (round > maxRound) {
				end = true;
			}
		}

	}

	private boolean calcNbUnit(Fighter fighter, Fighter opponent, int round) {
		int totalUnit = 0;
		for (UnitType unitType1 : UnitType.values()) {
			int unit = fighter.getRound().get(round).get(unitType1).getNbUnit();

			for (UnitType unitType2 : UnitType.values()) {
				int deadUnit = opponent.getRound().get(round).get(unitType2).getRoundDetails().get(unitType1).getDead();

				if (unit < deadUnit) {
					// End for this unit
					deadUnit = unit;
					opponent.getRound().get(round).get(unitType2).getRoundDetails().get(unitType1).setDead(deadUnit);
					if (deadUnit == 0) {
						opponent.getRound().get(round).get(unitType2).getRoundDetails().get(unitType1).setDamage(0);
					}
				}
				unit -= deadUnit;

			}
			totalUnit += unit;

			fighter.getRound().get(round).get(unitType1).setNbUnit(unit);
		}
		return totalUnit == 0;
	}

	private void round(Fighter fighter, Fighter opponent, int armyMin, int round) {
		HashMap<UnitType, RoundUnit> line = new HashMap<UnitType, RoundUnit>();
		fighter.getRound().add(line);

		for (UnitType unitType : UnitType.values()) {
			RoundUnit roundUnit = new RoundUnit();
			line.put(unitType, roundUnit);
			round(unitType, fighter, opponent, armyMin, round, roundUnit);
		}
	}

	private void round(UnitType unitType, Fighter fighter, Fighter opponent, int armyMin, int round,
			RoundUnit roundUnit) {
		if (round == 0) {
			Integer nb = fighter.getTroopsByType().get(unitType);
			roundUnit.setNbUnit(nb);
			roundUnit.setRound(round);
			for (UnitType vs : UnitType.values()) {
				RoundDetail roundDetail = new RoundDetail();
				roundUnit.getRoundDetails().put(vs, roundDetail);
			}
		} else {
			RoundUnit roundP = fighter.getRound().get(round - 1).get(unitType);
			int nbUnit = roundP.getNbUnit();
			roundUnit.setNbUnit(nbUnit);
			roundUnit.setRound(round);

			boolean continueBattle = true;

			UnitType[] types = UnitType.values();
			if (round % 20 == 0) {
				// All the 20 rounds bikers attacks hunters
				if (unitType == UnitType.CAVALRY && fighter.hasBikers()) {
					types = new UnitType[] { UnitType.RANGED, UnitType.INFANTRY, UnitType.CAVALRY };
				}
				if (unitType == UnitType.RANGED && fighter.hasSnipers()) {
					types = new UnitType[] { UnitType.CAVALRY, UnitType.INFANTRY, UnitType.RANGED };
				}
			}

			for (UnitType vs : types) {
				RoundDetail roundDetail = new RoundDetail();
				roundUnit.getRoundDetails().put(vs, roundDetail);

				// Previous value => calculate the troops engaged
				double army = Math.pow(nbUnit, 0.5) * Math.pow(armyMin, 0.5);

				// We check fight or not
				if (continueBattle && opponent.getDefenseByType().get(vs) > 0 && army > 0
						&& opponent.getRound().get(round - 1).get(vs).getNbUnit() > 0) {

					double attack = fighter.getAttackByType().get(unitType);
					double defense = opponent.getDefenseByType().get(vs);

					double coefAttack = Skill.damage(fighter, unitType, vs, round);
					double coefDefense = Skill.defense(opponent, vs, unitType, round);

//					System.out.println("" + round + "\t" + unitType + "\t" + vs + "\t" + army + "\t" + attack + "\t" + defense
//							+ "\t" + coefAttack + "\t" + coefDefense);

					attack = attack * coefAttack;
					defense = defense / (1 - coefDefense);

					int dead = calcDead(round, army, attack, defense);
					double protect = Skill.protect(opponent, unitType, vs, round, dead);
					attack = attack * (dead - protect) / dead;

					roundDetail.setDead(calcDead(round, army, attack, defense));


					// Damage = nb soldat + attack
					roundDetail.setDamage(army * attack * (1 - coefDefense));
					continueBattle = Skill.needContinue(fighter, unitType, vs, round);
				}
			}
		}
	}

	public int calcDead(int round, double army, double attack, double defense) {
		// Dead = roundup((nb soldat * attack / defense oposant / 100)
		double deadValue = army * attack / defense / 100.0;
		// Usure de 0,01% qui s'ajoute
		deadValue = deadValue - deadValue * 0.0001 * round;
		return (int) Math.ceil(deadValue);
	}

	public void calc() throws Exception {
		this.attacker.calc(attacked);
		this.attacked.calc(attacker);

		// Simulation des rounds
		this.round();
	}

	public Fighter getAttacker() {
		return attacker;
	}

	public Fighter getAttacked() {
		return attacked;
	}

	public BattleType getBattleType() {
		return battleType;
	}

	public void setBattleType(BattleType battleType) {
		this.battleType = battleType;
	}

	public Map<String, Object> getResult(boolean attacker) {
		return attacker ? getAttacker().getResult() : getAttacked().getResult();
	}

	public List<Map<String, Object>> getResult() {
		List<Map<String, Object>> ret = new ArrayList<>();
		Map<String, Object> total = new HashMap<>();
		ret.add(total);
		Map<String, Object> r1 = this.getAttacker().getResult();
		ret.add(r1);
		Map<String, Object> r2 = this.getAttacked().getResult();
		ret.add(r2);

		total.put("success", Boolean.valueOf((Integer) r1.get("end") > 0));
		double score = ((Integer) r1.get("end") > 0) ? ((Integer) r1.get("end") * 1.0 / (Integer) r1.get("start"))
				: -((Integer) r2.get("end") * 1.0 / (Integer) r2.get("start"));
		total.put("score", score);

		return ret;
	}

	public List<Map<String, Object>> getStats() {
		List<Map<String, Object>> ret = new ArrayList<>();

		Set<Integer> set = new HashSet<>();
		for (Integer boost : getAttacker().getUsedBoost().keySet()) {
			Map<String, Object> map = new HashMap<>();
			ret.add(map);
			map.put("stat", boost);
			map.put("attacker", getAttacker().getUsedBoost().get(boost));
			map.put("attacked", getAttacked().getUsedBoost().get(boost));
			set.add(boost);
		}
		for (Integer boost : getAttacked().getUsedBoost().keySet()) {
			if (!set.contains(boost)) {
				Map<String, Object> map = new HashMap<>();
				ret.add(map);
				map.put("stat", boost);
				map.put("attacker", getAttacker().getUsedBoost().get(boost));
				map.put("attacked", getAttacked().getUsedBoost().get(boost));
			}
		}

		return ret.stream().sorted((o1, o2) -> ((Integer) o1.get("stat")).compareTo((Integer) o2.get("stat")))
				.collect(Collectors.toList());
	}
}
