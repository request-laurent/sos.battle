package com.sos.ui5.battle.web.rest;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.sos.ui5.battle.model.Parameter;
import com.sos.ui5.battle.model.Role;

@Path("/parameter")
@RolesAllowed(Role.admin)
public class ParameterService {

	/**
	 * Permet de mettre à jour la liste des parametres
	 * 
	 * @return Rien
	 * @throws Exception
	 */
	@GET
	@Path("refresh")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response refresh() throws Exception {
		Parameter.getMaps();
		
		return Response.ok().build();
	}
}
