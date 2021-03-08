package com.sos.ui5.battle.web.rest;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.security.RolesAllowed;
import javax.persistence.EntityManager;
import javax.persistence.Parameter;
import javax.persistence.Query;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sos.ui5.battle.model.Dummy;
import com.sos.ui5.battle.model.Role;
import com.sos.ui5.battle.utils.EMUtils;

@Path("/data")
@RolesAllowed(Role.user)
public class Data {
	/**
	 * Classe contenant les parametres pour une requette JPA
	 */
	static class Select {
		String where;
		String orderBy;
		String className;
		String fetch;
		Map<String, Object> params;

		public String getWhere() {
			return where;
		}

		public void setWhere(String where) {
			this.where = where;
		}

		public String getOrderBy() {
			return orderBy;
		}

		public void setOrderBy(String orderBy) {
			this.orderBy = orderBy;
		}

		public String getFetch() {
			return fetch;
		}

		public void setFetch(String fetch) {
			this.fetch = fetch;
		}

		public Map<String, Object> getParams() {
			return params;
		}

		public void setParams(Map<String, Object> params) {
			this.params = params;
		}

		public String getClassName() {
			return className;
		}

		public void setClassName(String className) {
			this.className = className;
		}
	}

	/**
	 * Classe contenant les parametres pour une requette JPA
	 */
	static class Find {
		String className;
		String fetch;
		Object key;

		public String getClassName() {
			return className;
		}

		public void setClassName(String className) {
			this.className = className;
		}

		public String getFetch() {
			return fetch;
		}

		public void setFetch(String fetch) {
			this.fetch = fetch;
		}

		public Object getKey() {
			return key;
		}

		public void setKey(Object key) {
			this.key = key;
		}
	}
	
	/**
	 * Récupération d'une liste de données avec critères
	 * 
	 * @param filter Le filtre avec ses conditions
	 * @return Une liste d'objet
	 * @throws Exception
	 */
	@POST
	@Path("select")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response select(Select filter) throws Exception {
		List<?> ret = EMUtils.call(em -> {
			return getSelect(em, filter);
		});

		return Response.ok(ret).build();
	}	

	/**
	 * Récupération de plusieurs listes de données avec critères
	 * 
	 * @param filter Les filtres avec ses conditions
	 * @return Une liste d'objet
	 * @throws Exception
	 */
	@POST
	@Path("selects")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response selects(Map<String, Select> filters) throws Exception {
		Map<String, List<?>> ret = EMUtils.call(em -> {
			Map<String, List<?>> map = new HashMap<>();
			for (String code : filters.keySet()) {
				Select filter = filters.get(code);
				map.put(code, getSelect(em, filter));
			}

			return map;
		});

		return Response.ok(ret).build();
	}

	private List<?> getSelect(EntityManager em, Select filter) {
		String cond = "";
		if (filter.where != null && filter.where.length() > 0) {
			cond = " where " + filter.where;
		}

		String order = "";
		if (filter.orderBy != null && filter.orderBy.length() > 0) {
			order = " order by " + filter.orderBy;
		}

		String fetch = "";
		if (filter.fetch != null) {
			fetch = " " + filter.fetch;
		}

		Query query = em.createQuery("select distinct o from " + filter.className + " o" + fetch + cond + order);
		if (filter.params != null) {
			for (String key : filter.params.keySet()) {
				query.setParameter(key, convertType(query.getParameter(key), filter.params.get(key)));
			}
		}

		return query.getResultList();
	}
	/**
	 * Récupération d'un objet
	 * 
	 * @param filter Le filtre avec ses conditions
	 * @return Une liste d'objet
	 * @throws Exception
	 */
	@POST
	@Path("find")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response find(Find find) throws Exception {
		Object ret = EMUtils.call(em -> {
			String fetch = "";
			if (find.getFetch() != null) {
				fetch = " " + find.getFetch();
			}

			Query query = em.createQuery("select o from " + find.getClassName() + " o" + fetch + " where o.id=:id");
			query.setParameter("id", convertType(query.getParameter("id"), find.getKey()));

			List<?> lst = query.getResultList();
			if (lst.size() == 0) {
				return null;
			} else {
				return lst.get(0);
			}
		});

		return Response.ok(ret).build();
	}

	/**
	 * Permet de créer / modifier un objet json
	 * 
	 * @param classeName Le nom de la classe
	 * @param json L'objet en json
	 * @return L'objet enregistré
	 * @throws Exception
	 */
	@POST
	@Path("save")
	@RolesAllowed(Role.admin)
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response save(@QueryParam("className") String className, String json) throws Exception {
		Object object = new ObjectMapper().readValue(json, getFullClassName(className));

		Object ret = EMUtils.call(em -> {
			return em.merge(object);
		});

		return Response.ok(ret).build();
	}

	private Class<?> getFullClassName(String className) throws ClassNotFoundException {
		return Class.forName(Dummy.class.getPackage().getName() + "." + className);
	}

	/**
	 * Suppression d'un objet
	 * 
	 * @param classeName Le nom de la classe
	 * @param key La clé
	 * @return rien
	 * @throws Exception en cas de problème
	 */
	@DELETE
	@Path("delete")
	@RolesAllowed(Role.admin)
	@Produces({ MediaType.APPLICATION_JSON })
	public Response delete(@QueryParam("className") String className, @QueryParam("key") Long key) throws Exception {
		EMUtils.call(em -> {
			em.remove(em.find(getFullClassName(className), key));
			return null;
		});

		return Response.ok().build();
	}

	/**
	 * Cette methode permet de convertir les types simples JSON vers son équivalent
	 * javas
	 * 
	 * @param parameter La parametre de la requette
	 * @param value la valeur à convertir
	 * @return la valeur convertie
	 */
	private Object convertType(Parameter<?> parameter, Object value) {
		if (value == null || parameter.getParameterType() == null
				|| parameter.getParameterType().equals(value.getClass())) {
			return value;
		}

		if (parameter.getParameterType().equals(Long.class)) {
			if (value instanceof Integer) {
				return ((Integer) value).longValue();
			}
			if (value instanceof String) {
				return new Long((String) value);
			}
		} else if (parameter.getParameterType().equals(Date.class)) {
			if (value instanceof Long) {
				return new Date(((Long) value));
			}

		}

		return null;
	}

}
