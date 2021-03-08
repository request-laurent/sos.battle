package com.sos.ui5.battle.web.rest;

import java.io.Reader;
import java.net.URI;
import java.net.URL;

import javax.json.Json;
import javax.json.JsonObject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;

import com.sos.ui5.battle.utils.security.UserService;

@Path("/discord")
public class Discord {
	private static final String CLIENT_ID = "773858599197343765";
	private static final String CLIENT_SECRET = "4LKF7GakIJ90XHSapP9jDSlkBlr_wodn";
	private static final String DISCORD_API = "https://discord.com/api/";

	@GET
	@Produces({ MediaType.TEXT_HTML })
	public Response oAuth2(@QueryParam("code") String code, @Context HttpServletRequest request) throws Exception {
		// Récupération de l'url du serveur
		URL url = new URL(
				(request.getHeader("X-Forwarded-Proto") != null ? request.getHeader("X-Forwarded-Proto") : request.getScheme())
						+ "://" + request.getServerName()
						+ ("http".equals(request.getScheme()) && request.getServerPort() == 80
								|| "https".equals(request.getScheme()) && request.getServerPort() == 443 ? ""
										: ":" + request.getServerPort())
						+ request.getRequestURI());

		Form form = new Form();
		form.param("client_id", CLIENT_ID);
		form.param("client_secret", CLIENT_SECRET);
		form.param("grant_type", "authorization_code");
		form.param("code", code);
		form.param("redirect_uri", url.toString());
		form.param("scope", "identify");

		Response response = ClientBuilder.newClient().target(DISCORD_API + "/oauth2/token").request()
				.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED));

		if (response.getStatusInfo().getFamily() != Family.SUCCESSFUL) {
			throw new Exception(response.readEntity(String.class));
		}
		JsonObject json = Json.createReader(response.readEntity(Reader.class)).readObject();
		String accessToken = json.getString("access_token");
		String tokenType = json.getString("token_type");

		response = ClientBuilder.newClient().target(DISCORD_API + "users/@me").request()
				.header("authorization", tokenType + " " + accessToken).get();

		json = Json.createReader(response.readEntity(Reader.class)).readObject();

		String img = "";
		try {
			img = json.containsKey("avatar")
					? "https://cdn.discordapp.com/avatars/" + json.getString("id") + "/" + json.getString("avatar")
							+ ".png?size=32"
					: "";
		} catch (Exception ex) {
		}

		boolean success = UserService.loginOAuth(json.getString("username") + "#" + json.getString("discriminator"),
				json.getString("username"), img);
		if (success) {
			String returnURL = (String) request.getSession(true).getAttribute("returnURL");
			if (returnURL != null) {
				request.getSession(true).removeAttribute("returnURL");
				return Response.temporaryRedirect(new URI(returnURL)).build();
			} else {
				return Response.temporaryRedirect(new URL(url, "../").toURI()).build();
			}
		} else {
			return Response.temporaryRedirect(new URL(url, "../login.html").toURI()).build();
		}
	}
}