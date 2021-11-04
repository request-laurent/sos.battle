package com.sos.ui5.battle.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sos.ui5.battle.sos.simulator.SimulationFighter;
import com.sos.ui5.battle.utils.EMUtils;
import com.sos.ui5.battle.utils.JpaSimulationFighterConverterJson;
import com.sos.ui5.battle.utils.security.UserService;

@Cacheable
@Entity
@Table(name = "troop")
public class Troop implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;

	@Column(length = 100000)
	@Convert(converter = JpaSimulationFighterConverterJson.class)
	private SimulationFighter fighter;

	@ManyToOne(fetch = FetchType.LAZY)
	private User user;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public SimulationFighter getFighter() {
		return fighter;
	}

	public void setFighter(SimulationFighter fighter) {
		this.fighter = fighter;
	}

	public static void save(String name, SimulationFighter fighter) throws Exception {
		EMUtils.call(em -> {
			User user = UserService.getUser();

			if (user.getGroup().getId() == null )  {
				em.persist(user.getGroup());
			}
			if (user.getId() == null) {
				em.persist(user);
			}

			List<Troop> lst = em.createQuery("select o from Troop o where o.user.id=:userId and o.name=:name", Troop.class)
					.setParameter("userId", user.getId()).setParameter("name", name).getResultList();
			if (lst.size() > 0) {
				lst.get(0).setFighter(fighter);
			} else {
				Troop troop = new Troop();
				troop.setUser(user);
				troop.setFighter(fighter);
				troop.setName(name);
				em.persist(troop);
			}
			return null;
		});
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public static List<Map<String, Object>> getList() throws Exception {
		List<Map<String, Object>> lst = new ArrayList<Map<String, Object>>();

		EMUtils.call(em -> {
			User user = UserService.getUser();

			List<Troop> troops = em
					.createQuery("select o from Troop o where o.user.id=:userId or o.user.id is null", Troop.class)
					.setParameter("userId", user.getId()).getResultList();
			for (Troop troop : troops) {
				Map<String, Object> map = new HashMap<>();
				map.put("id", troop.getId());
				map.put("name", troop.getUser() == null ? "npc_name_survivor" : "event_showdown_tutorial_player_nametag");
				map.put("type", "human");
				map.put("lvl", troop.getName());
				lst.add(map);
			}

			return null;
		});

		return lst;
	}

	public static SimulationFighter load(long id) throws Exception {
		try {
			return EMUtils.call(em -> {
				User user = UserService.getUser();

				List<Troop> lst = em
						.createQuery("select o from Troop o where o.id=:id and (o.user.id=:userId or o.user.id is null)",
								Troop.class)
						.setParameter("userId", user.getId()).setParameter("id", id).getResultList();
				if (lst.size() > 0) {
					return lst.get(0).getFighter();
				}
				return null;
			});
		} catch (Exception ex) {
			//Default is database is not available
			return new ObjectMapper().readValue(
					"{\"boost\":{\"300002\":0.3,\"300004\":0.195,\"300006\":0.22,\"300008\":0.19,\"300014\":1.2336,\"300016\":0.9961,\"300018\":1.3531,\"300020\":1.4181,\"300026\":1.0911,\"300028\":1.1386,\"300030\":1.4181,\"300032\":1.3531,\"300038\":1.0436,\"300040\":1.0911,\"300042\":1.2234,\"300044\":1.1584},\"troops\":[{\"id\":1300010,\"number\":36333},{\"id\":1300022,\"number\":36333},{\"id\":1300034,\"number\":36333}],\"heroes\":[{\"id\":26400086,\"skills\":[25500170,25500180,25500190],\"equipment\":[27300043,27300044,27300045],\"selected\":true},{\"id\":26400140,\"skills\":[25500382,25500392,25500402],\"equipment\":[27300034,27300035,27300036],\"selected\":true},{\"id\":26400046,\"skills\":[25500080,25500089,25500098],\"equipment\":[27300052,27300053,27300054],\"selected\":false},{\"id\":26400149,\"skills\":[25500413,25500418,25500423],\"equipment\":[27300052,27300053,27300054],\"selected\":true}],\"monster\":false}",
					new TypeReference<SimulationFighter>() {
					});
		}
	}

	public static SimulationFighter load2(long id) throws Exception {
		return EMUtils.call(em -> {
			List<Troop> lst = em.createQuery("select o from Troop o where o.id=:id", Troop.class).setParameter("id", id)
					.getResultList();
			if (lst.size() > 0) {
				return lst.get(0).getFighter();
			}
			return null;
		});
	}

}