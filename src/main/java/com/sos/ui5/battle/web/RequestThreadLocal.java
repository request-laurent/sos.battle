package com.sos.ui5.battle.web;

import javax.servlet.http.HttpServletRequest;

/**
 * Permet de conserver la request HTTP tout le long de l'appel
 * utilisé pour : la sécurité, i18n ...
 */
public class RequestThreadLocal {
	private static ThreadLocal<HttpServletRequest> perThreadRequest = new ThreadLocal<HttpServletRequest>();

	public static HttpServletRequest getCurrentRequest() {
		return perThreadRequest.get();
	}

	public static void setCurrentRequest(HttpServletRequest request) {
		perThreadRequest.set(request);
	}

}
