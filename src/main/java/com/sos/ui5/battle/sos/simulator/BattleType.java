package com.sos.ui5.battle.sos.simulator;

public enum BattleType {
	// capital
	BATTLE_WONDER("battle_wonder", "battle_wonder"),
	// city
	BATTLE_CITY_ATTACK("battle_city_attack", "battle_city_defence"), BATTLE_CITY_DEFENCE("battle_city_defence",
			"battle_city_attack"),
	// city
	BATTLE_CITY_ATTACK_RALLY("battle_city_attack_rally",
			"battle_city_defence_rally"), BATTLE_CITY_DEFENCE_RALLY("battle_city_defence_rally", "battle_city_attack_rally"),
	// Deamon
	BATTLE_GVE_RALLY("battle_gve_rally", "battle_simulation"),
	// gather
	BATTLE_GATHER_ATTACK("battle_gather_attack", "battle_gather_defence"), BATTLE_GATHER_DEFENCE("battle_gather_defence",
			"battle_gather_attack"),
	// Infected
	BATTLE_MONSTER_ATTACK("battle_monster_attack", "battle_simulation"),
	// Trap
	BATTLE_ALLIANCE_BOSS("battle_alliance_boss", "battle_simulation"),
	// reservoir
	BATTLE_AVA_PVP("battle_ava_pvp", "battle_ava_pvp"),
	// reservoir
	BATTLE_AVA_PVP_RALLY("battle_ava_pvp_rally", "battle_simulation"), BATTLE_SIMULATION("battle_simulation",
			"battle_simulation");

	private String code;
	private String invert;

	private BattleType(String code, String invert) {
		this.code = code;
		this.invert = invert;
	}

	public String getCode() {
		return code;
	}

	public static BattleType byCode(String code) {
		for (BattleType battleType : BattleType.values()) {
			if (battleType.getCode().equals(code)) {
				return battleType;
			}
		}
		return null;
	}


	public BattleType getOponent() {
		return byCode(invert);
	}
}
