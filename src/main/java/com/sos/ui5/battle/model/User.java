package com.sos.ui5.battle.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sos.ui5.battle.utils.EMUtils;
import com.sos.ui5.battle.utils.security.SHA;
import com.sos.ui5.battle.utils.security.UserService;

@Cacheable
@Entity
@Table(name = "user")
public class User implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final String PREFIX = "ZNc2TY:";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true)
	private String code;

	private String name;

	@Column(length = 1000)
	private String password;

	private String avatar;

	@ManyToOne(fetch = FetchType.LAZY)
	private Groups group;

	private Integer cpt;

	public Groups getGroup() {
		return group;
	}

	public void setGroup(Groups group) {
		this.group = group;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return la liste des roles de l'utilisateurs
	 */
	@JsonIgnore
	public List<String> getRoles() {
		if (getGroup() == null || getGroup().getRoles() == null) {
			return new ArrayList<String>();
		}
		return Arrays.asList(getGroup().getRoles().split(","));
	}

	public static String encryptPassword(String name, String password) throws Exception {
		return SHA.getHash(PREFIX + name + ":" + password);
	}

	/**
	 * Vérifie l'identification
	 * 
	 * @param name
	 * @param password
	 * @return true si c'est bon
	 * @throws Exception
	 */
	public boolean login(String name, String password) throws Exception {
		if (getPassword() != null) {
			return getPassword().trim().equals(User.encryptPassword(name.trim(), password.trim()));
			// return true;
		} else {
			return false;
		}
	}

	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}

	public Integer getCpt() {
		return cpt;
	}

	public void setCpt(Integer cpt) {
		this.cpt = cpt;
	}

	public static void addCpt() throws Exception {
		EMUtils.call(em -> {
			User user = UserService.getUser();
			user = em.find(User.class, user.getId());
			user.setCpt((user.getCpt() == null ? 0 : user.getCpt()) + 1);
			return null;
		});
	}

}