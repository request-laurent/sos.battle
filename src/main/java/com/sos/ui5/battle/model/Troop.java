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

import com.sos.ui5.battle.sos.simulator.SimulationFighter;
import com.sos.ui5.battle.utils.EMUtils;
import com.sos.ui5.battle.utils.JpaSimulationFighterConverterJson;
import com.sos.ui5.battle.utils.security.UserService;

@Cacheable
@Entity
@Table(name="troop")
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
			
			List<Troop> lst = em.createQuery("select o from Troop o where o.user.id=:userId and o.name=:name", Troop.class).setParameter("userId", user.getId()).setParameter("name", name)
					.getResultList();
			if (lst.size()>0) {
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
		List<Map<String, Object>> lst = new ArrayList<Map<String,Object>>();

		EMUtils.call(em -> {
			User user = UserService.getUser();
			
			List<Troop> troops= em.createQuery("select o from Troop o where o.user.id=:userId or o.user.id is null", Troop.class).setParameter("userId", user.getId()).getResultList();
			for (Troop troop: troops) {
				Map<String, Object> map = new HashMap<>();
				map.put("id", troop.getId());
				map.put("name", troop.getUser()==null? "npc_name_survivor":"event_showdown_tutorial_player_nametag");
				map.put("type", "human");
				map.put("lvl", troop.getName());
				lst.add(map);
			}
			
			return null;
		});
		
		return lst;
	}

	public static SimulationFighter load(long id) throws Exception {
		return EMUtils.call(em -> {
			User user = UserService.getUser();
			
			List<Troop> lst = em.createQuery("select o from Troop o where o.id=:id and (o.user.id=:userId or o.user.id is null)", Troop.class).setParameter("userId", user.getId()).setParameter("id", id)
					.getResultList();
			if (lst.size()>0) {
				return lst.get(0).getFighter();
			}
			return null;
		});
	}
	
	public static SimulationFighter load2(long id) throws Exception {
		return EMUtils.call(em -> {
			List<Troop> lst = em.createQuery("select o from Troop o where o.id=:id", Troop.class).setParameter("id", id)
					.getResultList();
			if (lst.size()>0) {
				return lst.get(0).getFighter();
			}
			return null;
		});
	}


}