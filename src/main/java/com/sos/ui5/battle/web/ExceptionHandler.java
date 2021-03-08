package com.sos.ui5.battle.web;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.sos.ui5.battle.model.Role;
import com.sos.ui5.battle.utils.security.UserService;

/**
 * Cette classe permet de surcharger le traitement des erreurs de JAX-RS avec un
 * retour JSON pour un traitement côté client
 */
@Provider
public class ExceptionHandler implements ExceptionMapper<Throwable> {

	@Override
	public Response toResponse(Throwable exception) {
		Map<String, String> map = new HashMap<>();

		int status = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();

		// Le message d'erreur
		map.put("message",
				exception.getMessage() == null ? exception.getClass().getName().toString() : exception.getMessage());

		// Le détail => la stack trace
		StringWriter sw = new StringWriter();
		exception.printStackTrace(new PrintWriter(sw));
		// Si l'on n'est pas identifié, pas de détail
		if (UserService.getUser() != null && UserService.isUserInRole(Role.ADMIN)) {
			map.put("details", sw.toString());
		}

		// Si c'est une WebApplicationException, on conserve son statut
		if (exception instanceof WebApplicationException) {
			WebApplicationException webApplicationException = (WebApplicationException) exception;
			status = webApplicationException.getResponse().getStatus();
		}

		if (status == 500) {
			try {
				String url = RequestThreadLocal.getCurrentRequest().getRequestURI()
						.substring(RequestThreadLocal.getCurrentRequest().getContextPath().length());
				if (url.startsWith("/rest/")) {
					StatisticsService.add(url.substring(6), System.currentTimeMillis(), exception);
				}
			} catch (Exception ex) {
			}
		}

		return Response.status(status).type(MediaType.APPLICATION_JSON).entity(map).build();
	}

}
