package com.sos.ui5.battle.sos.simulator;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonValue;

import org.apache.commons.io.IOUtils;

public class JsonUtil {
	public static JsonArray unitStats;
	public static JsonArray survivorSkillMain;
	public static JsonArray survivorSlgSkill;
	public static JsonArray survivorSlgEffect;
	public static JsonArray survivorRankMain;
	public static JsonArray survivors;
	public static JsonArray survivorTag;
	public static JsonArray benefits;
	public static JsonArray benefitCalc;
	public static JsonArray survivorEquipment;
	public static JsonArray survivorEquipmentSuit;
	public static JsonArray battleWounded;
	public static JsonArray survivorSkillType;
	public static JsonArray survivorEquipmentGroup;
	public static JsonArray npcMonster;
	public static JsonArray itemlist;
	public static JsonArray city_buff_main;

	public static JsonObject jsonById(JsonArray json, String key, String val) {
		return (JsonObject) (json).stream().filter(e -> {
			JsonObject o = (JsonObject) e;
			return o.get(key) instanceof JsonString ? o.getString(key).equals(val) : o.get(key).toString().equals(val);
		}).findAny().orElse(null);
	}

	static {
		try {
			battleWounded = (JsonArray) getAsset("battle_wounded.json");
			benefits = (JsonArray) getAsset("benefits.json");
			benefitCalc = (JsonArray) getAsset("benefit_calc.json");
			city_buff_main = (JsonArray) getAsset("city_buff_main.json");
			itemlist = (JsonArray) getAsset("itemlist.json");
			npcMonster = (JsonArray) getAsset("npc_monster.json");
			survivorSkillMain = (JsonArray) getAsset("survivor_skill_main.json");
			survivorSlgSkill = (JsonArray) getAsset("survivor_slg_skill.json");
			survivorSlgEffect = (JsonArray) getAsset("survivor_slg_effect.json");
			survivorSkillType = (JsonArray) getAsset("survivor_skill_type.json");
			survivors = (JsonArray) getAsset("survivor.json");
			survivorTag = (JsonArray) getAsset("survivor_tag.json");
			survivorRankMain = (JsonArray) getAsset("survivor_rank_main.json");
			survivorEquipment = (JsonArray) getAsset("survivor_equipment.json");
			survivorEquipmentSuit = (JsonArray) getAsset("survivor_equipment_suit.json");
			survivorEquipmentGroup = (JsonArray) getAsset("survivor_equipment_group.json");
			unitStats = (JsonArray) getAsset("unit_stats.json");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static List<Map<String, Object>> units() {
		List<Map<String, Object>> lst = new ArrayList<Map<String, Object>>();

		for (JsonValue u : unitStats) {
			JsonObject ju = (JsonObject) u;
			Map<String, Object> unit = new HashMap<String, Object>();
			unit.put("id", ju.getInt("InternalId"));
			unit.put("name", toUnitName(ju.getString("Id")));
			unit.put("tier", toTier(ju.getInt("Tier"), ju.getInt("UnlockStar")));
			// if (ju.getInt("UnlockStar") == 0 || ju.getInt("Tier") >= 10) {
			lst.add(unit);
			// }
		}

		return lst.stream().sorted((o1, o2) -> ((String) o2.get("name")).compareTo((String) o1.get("name")))
				.collect(Collectors.toList());
	}

	private static String toTier(int tier, int star) {
		return "T" + tier + (star == 0 ? "" : " " + star + "*");
	}

	public static String toUnitName(String val) {
		if (val.contains("boss")) {
			return "plot_gallery_hunter_prey_subtitle_2";
		} else if (val.contains("infantry")) {
			return "troop_type_infantry_name";
		} else if (val.contains("ranged")) {
			return "troop_type_ranged_name";
		} else if (val.contains("cavalry")) {
			return "troop_type_cavalry_name";
		}

		return "troop_type_infantry_name";
	}

	public static List<Map<String, Object>> heros() {
		List<Map<String, Object>> lst = new ArrayList<Map<String, Object>>();

		for (JsonValue u : survivorRankMain) {
			JsonObject ju = (JsonObject) u;
			Map<String, Object> unit = new HashMap<String, Object>();

			JsonObject survivor = jsonById(survivors, "InternalId", "" + ju.getInt("SurvivorType"));

			unit.put("id", ju.getInt("InternalId"));
			unit.put("hero", survivor.getInt("InternalId"));
			unit.put("name", survivor.getString("Name"));
			unit.put("profession", survivor.getString("Profession"));
			String rank = "" + ju.getInt("Rank") + "." + (ju.getInt("RankSmall") + 1);
			unit.put("rank", rank);
			unit.put("name", survivor.getString("Name"));
			unit.put("rankName", "survivor_rank_" + rank.replace(".", "_"));
			unit.put("image", "img/" + survivor.getString("ImageSmall") + ".png");
			if (survivor.getInt("WhatWeek") < 30) {
				lst.add(unit);
			}
		}

		return lst.stream()
				.sorted(
						(o1, o2) -> ((String) o1.get("name") + o1.get("rank")).compareTo((String) o2.get("name") + o2.get("rank")))
				.collect(Collectors.toList());
	}

	public static List<Map<String, Object>> skills() {
		List<Map<String, Object>> lst = new ArrayList<Map<String, Object>>();

		for (JsonValue s : survivors) {
			JsonObject survivor = (JsonObject) s;
			for (String key : survivor.getJsonObject("SkillConfig").keySet()) {
				JsonObject skillType = jsonById(survivorSkillType, "InternalId", key);
				if (skillType.getInt("Type") == 1) {
					// Trouver les skills main corespondant
					for (JsonValue u : survivorSkillMain) {
						JsonObject skillMain = (JsonObject) u;
						if (skillMain.getInt("SkillType") == Integer.parseInt(key)) {
							Map<String, Object> skill = new HashMap<String, Object>();
							skill.put("id", skillMain.getInt("InternalId"));
							skill.put("name", skillType.getString("Name"));
							skill.put("hero", survivor.getInt("InternalId"));
							skill.put("lvl", skillMain.getInt("Level"));
							skill.put("image", "img/" + skillType.getString("Icon") + ".png");
							skill.put("skillNo", survivor.getJsonObject("SkillConfig").getInt(key));
							lst.add(skill);
						}
					}
				}
			}
		}

		return lst.stream().sorted((o1, o2) -> ((Integer) o1.get("lvl")).compareTo((Integer) o2.get("lvl")))
				.collect(Collectors.toList());
	}

	public static List<Map<String, Object>> equipment() {
		List<Map<String, Object>> lst = new ArrayList<Map<String, Object>>();

		for (JsonValue u : survivorEquipment) {
			JsonObject ju = (JsonObject) u;
			Map<String, Object> equipment = new HashMap<String, Object>();

			JsonObject equipmentType = jsonById(survivorEquipmentGroup, "InternalId",
					"" + ju.getInt("SurvivorEquipmentGroupNew"));

			equipment.put("id", ju.getInt("InternalId"));
			equipment.put("name", equipmentType.getString("EquipmentName"));
			equipment.put("profession", equipmentType.getString("Profession"));
			equipment.put("part", equipmentType.getString("Part"));
			equipment.put("lvl", "" + equipmentType.getInt("SurvivorEquipmentGroupLevel") + "." + (ju.getInt("Star")));
			equipment.put("image", "img/" + ju.getString("Icon") + ".png");
			lst.add(equipment);
		}

		return lst.stream().sorted((o1, o2) -> ((String) o1.get("name") + (String) o1.get("name") + o1.get("lvl"))
				.compareTo((String) o2.get("name") + (String) o2.get("name") + o2.get("lvl"))).collect(Collectors.toList());
	}

	public static List<Map<String, Object>> benefits() {
		List<Map<String, Object>> lst = new ArrayList<Map<String, Object>>();

		for (JsonValue u : benefits) {
			JsonObject ju = (JsonObject) u;
			Map<String, Object> benefit = new HashMap<String, Object>();

			benefit.put("id", ju.getInt("InternalId"));
			benefit.put("name", ju.getString("LocKey"));
			lst.add(benefit);
		}

		return lst;
	}

	public static SimulationFighter npcMonsert(int id) {
		SimulationFighter fighter = new SimulationFighter();

		JsonObject json = jsonById(npcMonster, "InternalId", "" + id);

		fighter.setMonster(true);
		fighter.setStatsIncluded(false);

		for (JsonValue benefit : json.getJsonArray("Benefit")) {
			JsonObject b = (JsonObject) benefit;
			fighter.getBoost().put(b.getInt("benefit_id"), b.getJsonNumber("benefit_value").doubleValue());
		}

		for (String key : json.getJsonObject("Troops").keySet()) {
			fighter.getTroops().add(new SimulationTroop(Integer.parseInt(key), json.getJsonObject("Troops").getInt(key)));
		}

		return fighter;
	}

	public static List<Map<String, Object>> templateNpc() {
		List<Map<String, Object>> lst = new ArrayList<>();

		for (JsonValue jsonValue : npcMonster) {
			JsonObject npc = (JsonObject) jsonValue;
			String id = npc.getString("Id");
			String name = null;
			String lvl = null;
			if (id.contains("world_monster_")) {
				// infecté
				name = "id_wildness_infected";
				lvl = id.replace("world_monster_", "");
			} else if (id.contains("npc_monster_alliance_boss_")) {
				// Piège
				name = "id_alliance_portal";
				String[] t = id.replace("npc_monster_alliance_boss_", "").split("_");
				lvl = "" + ((Integer.parseInt(t[0])-1) * 6 + Integer.parseInt(t[1]));
			} else if (npc.getString("Name").contains("id_gve_camp")) {
				// infecté boss
				name = "id_gve_camp";
				lvl = id.replace("npc_monster_gve_boss_", "");
			}

			if (name != null) {
				Map<String, Object> map = new HashMap<>();
				map.put("id", npc.getInt("InternalId"));
				map.put("name", name);
				map.put("type", "npc");
				map.put("lvl", lvl);
				lst.add(map);
			}
		}

		return lst;
	}

	/**
	 * Les buffs
	 * 
	 * @return
	 */
	private static final List<Integer> extraBenefits = Arrays.asList(300433, 300088, 300086, 300434, 300435, 300421,
			300409, 300412, 300410, 300417, 300436, 300416, 300423, 300411, 300415, 300418, 300360, 300422, 300424);

	public static List<Map<String, Object>> buff() {
		List<Map<String, Object>> lst = new ArrayList<>();

		for (JsonValue jsonValue : benefits) {
			JsonObject benefit = (JsonObject) jsonValue;
			if (extraBenefits.contains(benefit.getInt("InternalId"))) {
				Map<String, Object> map = new HashMap<>();
				map.put("id", benefit.getInt("InternalId"));
				map.put("name", benefit.getString("LocKey"));
				map.put("value", null);
				lst.add(map);
			}
		}
		return lst;
	}

	public static JsonValue getAsset(String fileName) throws Exception {
		byte[] data = IOUtils.toByteArray(JsonUtil.class.getClassLoader().getResourceAsStream("asset/" + fileName));
		JsonReader reader = Json.createReader(new ByteArrayInputStream(data));
		return reader.read();
	}
}
