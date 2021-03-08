package com.sos.ui5.battle.web.rest;

import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.sos.ui5.battle.model.Groups;
import com.sos.ui5.battle.model.Role;
import com.sos.ui5.battle.model.User;
import com.sos.ui5.battle.utils.EMUtils;
import com.sos.ui5.battle.utils.security.UserService;

@Path("/security")
public class Security {
	/**
	 * Pour éviter les pertes de sessions + reload automatique de l'application
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@GET
	@Path("ping")
	public Response ping(@Context HttpServletRequest request) throws Exception {
		if(request.isRequestedSessionIdValid()) {
			return Response.ok().build();
		}
		return Response.status(Response.Status.UNAUTHORIZED).build();
	}
	
	
	/**
	 * Permet de mettre à jour l'utilisateur
	 * 
	 * @param user L'utilisateur
	 * @return rien
	 * @throws Exception
	 */
	@POST
	@Path("saveUser")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	@RolesAllowed(Role.admin)
	public Response saveUser(User user) throws Exception {
		EMUtils.call(em -> {
			boolean changePwd = true;
			if (user.getId() == null) {
				em.persist(user);
			} else {
				User old = em.find(User.class, user.getId());
				if (Objects.equals(old.getPassword(), user.getPassword())) {
					changePwd = false;
				}
			}

			if (changePwd) {
				user.setPassword(User.encryptPassword(user.getCode(), user.getPassword()));
			}

			em.merge(user);
			
			return null;
		});

		return Response.ok().build();
	}

	/**
	 * Récupération d'un groupe
	 * 
	 * @param id La clé du groupe
	 * @return Le groupe
	 * @throws Exception
	 */
	@GET
	@Path("findGroup")
	@Produces({ MediaType.APPLICATION_JSON })
	@RolesAllowed(Role.admin)
	public Response findGroup(@QueryParam("id") Long id) throws Exception {
		Groups group;
		if (id == null) {
			group = new Groups();
			group.setRoles("");
		} else {
			group = EMUtils.call(em -> {
				return em.find(Groups.class, id);
			});
		}

		List<String> selected = Arrays.asList(group.getRoles().split(","));

		// Ajout des roles
		JsonObjectBuilder json = Json.createObjectBuilder();
		if (group.getId() != null) {
			json.add("id", group.getId());
		}
		json.add("name", group.getName() == null ? "" : group.getName());
		JsonArrayBuilder roles = Json.createArrayBuilder();
		for (Role role : Role.values()) {
			roles.add(
					Json.createObjectBuilder().add("name", role.toString()).add("selected", selected.contains(role.toString())));
		}
		json.add("roles", roles);

		return Response.ok(json.build().toString()).build();
	}

	/**
	 * Permet de mettre à jour le group
	 * 
	 * @param group le group
	 * @return rien
	 * @throws Exception
	 */
	@POST
	@Path("saveGroup")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	@RolesAllowed(Role.admin)
	public Response saveGroup(String json) throws Exception {
		Groups groups = new Groups();

		JsonObject j = Json.createReader(new StringReader(json)).readObject();
		groups.setName(j.getString("name"));
		if (j.get("id") != null) {
			groups.setId(new Integer(j.getInt("id")).longValue());
		}

		JsonArray jArray = j.getJsonArray("roles");
		groups.setRoles(jArray.getValuesAs(JsonObject.class).stream().filter(n -> n.getBoolean("selected"))
				.map(n -> n.getString("name")).collect(Collectors.joining(",")));

		EMUtils.call(em -> {
			em.merge(groups);
			return null;
		});
		
		return Response.ok().build();
	}

	/**
	 * Identification de l'utilisateur
	 * 
	 * @param name nom
	 * @param password mot de passe
	 * @return rien
	 * @throws Exception
	 */
	@POST
	@Path("login")
	@Produces({ MediaType.TEXT_PLAIN })
	public String login(@FormParam("user") String name, @FormParam("password") String password, @Context HttpServletRequest request) throws Exception {
		if (UserService.login(name, password)) {
			return (String) request.getSession(true).getAttribute("returnURL");
		} else {
			throw new ForbiddenException();
		}
	}

	/**
	 * Se deconecter => supprime la session de l'utilisateur
	 * @return ok
	 * @throws Exception
	 */
	@POST
	@Path("logout")
	public Response logout() throws Exception {
		UserService.setUser(null);

		return Response.ok().build();
	}

	/**
	 * Est ce que l'utilisateur est identifié
	 * 
	 * @return rien
	 * @throws Exception
	 */
	@GET
	@Path("getUser")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getUser(@QueryParam("url") String url, @Context HttpServletRequest request) throws Exception {
		//on conserve l'url
		request.getSession(true).setAttribute("returnURL", url);
		
		User user = UserService.getUser();
		if (user == null) {
			//Pas identifié, il faut se loguer
			throw new ForbiddenException();
		}

		List<String> selected = Arrays.asList(user.getGroup().getRoles().split(","));

		// Ajout des roles
		JsonObjectBuilder json = Json.createObjectBuilder();
		json.add("code", user.getCode());
		json.add("name", user.getName());
		json.add("group", user.getGroup().getName());
		json.add("avatar", user.getAvatar()==null?"":user.getAvatar());
		JsonObjectBuilder roles = Json.createObjectBuilder();
		for (Role role : Role.values()) {
			roles.add(role.toString(), selected.contains(role.toString()));
		}
		json.add("roles", roles);
		
		return Response.ok(json.build().toString()).build();
	}

}