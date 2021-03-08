package com.sos.ui5.battle.web.rest;

import java.util.Collection;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.sos.ui5.battle.model.Role;
import com.sos.ui5.battle.web.StatisticsService;

@Path("/statistics")
@RolesAllowed(Role.admin)
public class Statistics {

	@GET
	@Path("all")
	@Produces({ MediaType.APPLICATION_JSON })
	public Collection<StatisticsService> all() {
		return StatisticsService.build();
	}

	@GET
	@Path("clear")
	public Response clear() {
		StatisticsService.clear();
		return Response.ok().build();
	}

	@GET
	@Path("stack")
	@Produces({ MediaType.APPLICATION_JSON })
	public Collection<StatisticsService.ExceptionInfo> stack(@QueryParam("code") String code) {
		return  StatisticsService.getStack(code);
	}
	
}