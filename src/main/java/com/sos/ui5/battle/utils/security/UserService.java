package com.sos.ui5.battle.utils.security;

import java.util.List;

import javax.persistence.TypedQuery;
import javax.servlet.http.HttpServletRequest;

import com.sos.ui5.battle.model.Groups;
import com.sos.ui5.battle.model.Parameter;
import com.sos.ui5.battle.model.ParameterKey;
import com.sos.ui5.battle.model.Role;
import com.sos.ui5.battle.model.User;
import com.sos.ui5.battle.utils.EMUtils;
import com.sos.ui5.battle.web.RequestThreadLocal;

public class UserService {
	private static String USER = "user";

	/**
	 * Renvoie l'utilisateur connecté
	 * 
	 * @return l'utilisateur actuel ou vide
	 */
	public static User getUser() {
		return getUser(RequestThreadLocal.getCurrentRequest());
	}

	public static User getUser(HttpServletRequest request) {
		if (request == null) {
			return null;
		}
		return (User) request.getSession(true).getAttribute(USER);
	}

	/**
	 * Fixe l'utilisateur dans la session
	 * 
	 * @param user l'utilisateur
	 */
	public static void setUser(User user) {
		RequestThreadLocal.getCurrentRequest().getSession(true).setAttribute(USER, user);
	}

	/**
	 * Identification de l'utilisateur
	 * 
	 * @param code
	 * @param password
	 * @return succes ou pas
	 * @throws Exception
	 */
	public static boolean login(String code, String password) throws Exception {
		return EMUtils.call(em -> {
			boolean success = false;
			List<User> lst = em.createQuery("select o from User o join fetch o.group where o.code=:code", User.class)
					.setParameter("code", code.trim()).getResultList();
			if (lst.size() > 0) {
				User user = lst.get(0);

				if (user.login(code, password)) {
					success = true;
					UserService.setUser(user);
				}
			}
			return success;
		});
	}

	/**
	 * Identification de l'utilisateur via oAuth
	 * 
	 * @param mail
	 * @param anem
	 * @return succes ou pas
	 * @throws Exception
	 */
	public static boolean loginOAuth(String mail, String name, String avatar) throws Exception {
		try {
			return EMUtils.call(em -> {
				TypedQuery<User> query = em.createQuery("select o from User o join fetch o.group where o.code=:code",
						User.class);
				List<User> lst = query.setParameter("code", mail).getResultList();
				if (lst.size() == 0) {
					if ("true".equals(Parameter.get(ParameterKey.AUTO_CREATE_DISCORD))) {
						User user = new User();
						user.setCode(mail);
						user.setName(name);
						user.setGroup(em.find(Groups.class, 2l));
						user.setAvatar(avatar);
						em.persist(user);
						UserService.setUser(user);
					} else {
						return false;
					}
					return true;
				} else {
					User user = lst.get(0);
					user.setAvatar(avatar);
					UserService.setUser(user);
					return true;
				}
			});
		} catch (Exception ex) {
			//No database, we force the access
			User user = new User();
			user.setCode(mail);
			user.setName(name);
			Groups group = new Groups();
			group.setName("user");
			group.setRoles(Role.user+","+Role.battle);
			user.setGroup(group);
			user.setAvatar(avatar);
			UserService.setUser(user);
			return true;
		}
	}

	/**
	 * isUserInRole
	 * 
	 * @param role role
	 * @return true si oui
	 */
	public static boolean isUserInRole(Role role) {
		return isUserInRole(role.toString());
	}

	/**
	 * isUserInRole
	 * 
	 * @param role role
	 * @return true si oui
	 */
	public static boolean isUserInRole(String role) {
		return RequestThreadLocal.getCurrentRequest().isUserInRole(role);
	}
}