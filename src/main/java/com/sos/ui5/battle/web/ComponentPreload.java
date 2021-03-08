package com.sos.ui5.battle.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

/**
 * Fabrication automatique du ComponentPreload
 */
public class ComponentPreload extends HttpServlet {
	private static final long serialVersionUID = -4588572496573730768L;
	private boolean first = true;
	private PrintWriter w;
	private String namespace;
	private List<String> exclusions = new ArrayList<>();
	
	@Override
	public void init() throws ServletException {
		super.init();
		namespace = getServletConfig().getInitParameter("resourceroots");
		
		String val = getServletConfig().getInitParameter("exclusions");
		if (val != null) {
			exclusions = Arrays.asList(val.split(",")); 
		}
	}
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.addHeader("content-type", "application/javascript");
		
		// Vérifier l'hote
		if (exclusions.contains(request.getServerName())) {
			response.getWriter().write("/* empty : server exculsion */");
		} else {
			if (namespace == null) {
				throw new ServletException("resourceroots is mandatory");
			}

			first = true;
			w = response.getWriter();

			w.append("jQuery.sap.registerPreloadedModules({\n");
			w.append("	\"version\": \"2.0\",\n");
			w.append("	\"name\": \"" + namespace + "/Component-preload\",\n");
			w.append("	\"modules\": {\n");

			try {
				scan("/");
			} catch (Exception e) {
				throw new ServletException(e);
			}

			w.append("\n		}\n");
			w.append("});\n");
		}
	}

	private void scan(String file) throws Exception {
		if (file.endsWith("/")) {
			for (String path : getServletContext().getResourcePaths(file)) {
				scan(path);
			}
		} else {
			if (!file.endsWith("/Component-preload.js") && file.matches(
			    "(?i:^.*\\.(js|fragment\\.html|fragment\\.json|fragment\\.xml|view\\.html|view\\.json|view\\.xml|properties)$)")) {

				if (first) {
					first = false;
				} else {
					w.append(",\n");
				}

				w.append("		\"");
				w.append(namespace + file);
				w.append("\": ");
				w.append(quote(new String(IOUtils.toByteArray(getServletContext().getResourceAsStream(file)), "utf-8")));
			}
		}
	}

	public static String quote(String string) {
		if (string == null || string.length() == 0) {
			return "\"\"";
		}

		char c = 0;
		int i;
		int len = string.length();
		StringBuilder sb = new StringBuilder(len + 4);
		String t;

		sb.append('"');
		for (i = 0; i < len; i += 1) {
			c = string.charAt(i);
			switch (c) {
			case '\\':
			case '"':
				sb.append('\\');
				sb.append(c);
				break;
			case '/':
				sb.append('\\');
				sb.append(c);
				break;
			case '\b':
				sb.append("\\b");
				break;
			case '\t':
				sb.append("\\t");
				break;
			case '\n':
				sb.append("\\n");
				break;
			case '\f':
				sb.append("\\f");
				break;
			case '\r':
				sb.append("\\r");
				break;
			default:
				if (c < ' ') {
					t = "000" + Integer.toHexString(c);
					sb.append("\\u" + t.substring(t.length() - 4));
				} else {
					sb.append(c);
				}
			}
		}
		sb.append('"');
		return sb.toString();
	}

}
