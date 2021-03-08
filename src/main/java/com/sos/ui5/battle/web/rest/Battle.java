package com.sos.ui5.battle.web.rest;

import java.util.List;
import java.util.Map;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.sos.ui5.battle.model.Role;
import com.sos.ui5.battle.model.Troop;
import com.sos.ui5.battle.model.User;
import com.sos.ui5.battle.sos.simulator.BattleType;
import com.sos.ui5.battle.sos.simulator.Fight;
import com.sos.ui5.battle.sos.simulator.JsonUtil;
import com.sos.ui5.battle.sos.simulator.Simulation;
import com.sos.ui5.battle.sos.simulator.SimulationFighter;

@Path("/battle")
@RolesAllowed(Role.battle)
public class Battle {

	@GET
	@Path("units")
	@Produces({ MediaType.APPLICATION_JSON })
	public List<Map<String, Object>> units() throws Exception {
		return JsonUtil.units();
	}

	@GET
	@Path("heros")
	@Produces({ MediaType.APPLICATION_JSON })
	public List<Map<String, Object>> heros() throws Exception {
		return JsonUtil.heros();
	}

	@GET
	@Path("skills")
	@Produces({ MediaType.APPLICATION_JSON })
	public List<Map<String, Object>> skills() throws Exception {
		return JsonUtil.skills();
	}

	@GET
	@Path("equipment")
	@Produces({ MediaType.APPLICATION_JSON })
	public List<Map<String, Object>> equipment() throws Exception {
		return JsonUtil.equipment();
	}

	@GET
	@Path("simulation")
	@Produces({ MediaType.APPLICATION_JSON })
	public Simulation simulation() throws Exception {
		// Simulation par defaut
		Simulation simulation = new Simulation();
		simulation.setBattleType(BattleType.BATTLE_MONSTER_ATTACK);
		simulation.setMaxRound(9999);
		simulation.setAttacker(Troop.load(1l));
		simulation.setAttacked(JsonUtil.npcMonsert(3200058));

		return simulation;
	}

	@GET
	@Path("benefits")
	@Produces({ MediaType.APPLICATION_JSON })
	public List<Map<String, Object>> benefits() throws Exception {
		return JsonUtil.benefits();
	}

	@POST
	@Path("battle")
	@Produces({ MediaType.APPLICATION_JSON })
	public Fight battle(Simulation simulation) throws Exception {
		User.addCpt();
		return simulation.calc();
	}

	@POST
	@Path("saveFighter")
	@Produces({ MediaType.APPLICATION_JSON })
	public boolean saveFighter(SimulationFighter fighter, @QueryParam("name") String name) throws Exception {
		Troop.save(name, fighter);
		return true;
	}

	@GET
	@Path("template")
	@Produces({ MediaType.APPLICATION_JSON })
	public SimulationFighter template(@QueryParam("id") Integer id, @QueryParam("type") String type) throws Exception {
		if ("npc".equals(type)) {
			return JsonUtil.npcMonsert(id);
		}
		if ("human".equals(type)) {
			return Troop.load(id);
		}
		return null;
	}

	@GET
	@Path("templateList")
	@Produces({ MediaType.APPLICATION_JSON })
	public List<Map<String, Object>> templateList() throws Exception {
		List<Map<String, Object>> lst = Troop.getList();
		lst.addAll(JsonUtil.templateNpc());
		return lst;
	}

	@POST
	@Path("optimize")
	@Produces({ MediaType.APPLICATION_JSON })
	public SimulationFighter optimize(Simulation simulation) throws Exception {
		simulation.optimize();
		User.addCpt();
		return simulation.getAttacker();
	}

	@GET
	@Path("buffs")
	@Produces({ MediaType.APPLICATION_JSON })
	public List<Map<String, Object>> buffs() throws Exception {
		return JsonUtil.buff();
	}

}