package com.sos.ui5.battle.web;

import java.io.IOException;
import java.security.Principal;
import java.util.Base64;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import com.sos.ui5.battle.model.User;
import com.sos.ui5.battle.utils.security.UserService;

/**
 * Filtre permettant : - Positionner le contexte de l'utilisateur : i18n -
 * Sécurisation des requettes - Cache control privé par défaut - stats
 */
public class ContextFilter implements Filter {
	/**
	 * Wrapper de la requette pour la sécurité
	 *
	 */
	static class UserRoleRequestWrapper extends HttpServletRequestWrapper {
		String user;
		List<String> roles = null;
		HttpServletRequest realRequest;

		public UserRoleRequestWrapper(User user, HttpServletRequest request) {
			super(request);
			this.user = user.getCode();
			this.roles = user.getRoles();
			this.realRequest = request;
		}

		@Override
		public boolean isUserInRole(String role) {
			if (roles == null) {
				return false;
			}
			return roles.contains(role);
		}

		@Override
		public Principal getUserPrincipal() {
			if (this.user == null) {
				return null;
			}

			return new Principal() {
				@Override
				public String getName() {
					return user;
				}
			};
		}
	}

	public void init(FilterConfig filterConfig) {
		// init
	}

	public void destroy() {
		// destroy
	}

	public void doFilter(ServletRequest req, ServletResponse res, FilterChain filterChain)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;

		// On met sur le thread local la requette pour l'utiliser partout
		RequestThreadLocal.setCurrentRequest(request);
		
		// Ne rien mettre en cache par defaut
		response.setHeader("Cache-Control", "private, max-age=0");

		request = login(request);

		//Ajout des stats
		long time = System.currentTimeMillis();
		String url =request.getRequestURI().substring(request.getContextPath().length());
		try {
			try {
				filterChain.doFilter(request, response);
			} finally {
				RequestThreadLocal.setCurrentRequest(null);
			}
			addStat(url, time, null);
		} catch (Exception ex) {
			addStat(url, time, ex);
			throw ex;
		}
	}

	/**
	 * On regarde si l'on est identifié, dans ce cas là, on passer sur une requette identifiée
	 * @param request la requette d'rigine
	 * @return la requette identifiée ou pas
	 */
	private HttpServletRequest login(HttpServletRequest request) {
		User user = UserService.getUser(request);

		if (user != null) {
			// Si l'utilisateur est connecté, on passe sur une requette identifiée
			request = new UserRoleRequestWrapper(user, request);
			RequestThreadLocal.setCurrentRequest(request);
		} else {
			// L'utilisateur n'est pas connecté, on regarde si l'on est en mode Authorization basic
			String auth = request.getHeader("Authorization");
			if (auth != null && auth.toLowerCase().startsWith("basic ")) {
				try {
					auth = new String (Base64.getDecoder().decode(auth.substring(6)));
					if (UserService.login(auth.substring(0, auth.indexOf(":")), auth.substring(auth.indexOf(":") + 1))) {
						user = UserService.getUser(request);
						request = new UserRoleRequestWrapper(user, request);
						RequestThreadLocal.setCurrentRequest(request);
					}
				} catch (Exception ex) {
					// On n'est pas identifié dans ce cas là, on continue avec la requette standard
				}
			}
		}
		return request;
	}

	private void addStat(String url, long time, Exception ex) {
		if (url.startsWith("/rest/")) {
			StatisticsService.add(url.substring(6), time, ex);
		}
	}
}