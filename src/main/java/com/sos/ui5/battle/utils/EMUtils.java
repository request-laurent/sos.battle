package com.sos.ui5.battle.utils;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class EMUtils {
	private static final String APP = EMUtils.class.getName().split("\\.")[3];
	private static EntityManagerFactory emf = null;

	public static EntityManager getEntityManager() {
		if (emf == null) {
			Map<String, String> properties = new HashMap<>();
			if (System.getenv("MYSQLCONNSTR_" + APP + "_url") != null) {
				properties.put("javax.persistence.jdbc.url", System.getenv("MYSQLCONNSTR_" + APP + "_url"));
				properties.put("javax.persistence.jdbc.user", System.getenv("MYSQLCONNSTR_" + APP + "_user"));
				properties.put("javax.persistence.jdbc.password", System.getenv("MYSQLCONNSTR_" + APP + "_password"));
			}

			emf = Persistence.createEntityManagerFactory("main", properties);
		}
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		return em;
	}

	public static void closeEntityManager(EntityManager em, boolean roolback) {
		if (roolback) {
			if (em != null) {
				if (em.getTransaction().isActive()) {
					em.getTransaction().setRollbackOnly();
				}
				closeEntityManager(em);
			}
		}
	}

	public static void closeEntityManager(EntityManager em) {
		if (em != null) {
			if (em.isOpen()) {
				if (em.getTransaction().isActive()) {
					if (em.getTransaction().getRollbackOnly()) {
						em.getTransaction().rollback();
					} else {
						em.getTransaction().commit();
					}
				}
				em.clear();
				em.close();
			}
		}
	}

	/*
	 * KFO - Pour tenter de ne plus perdre les connexions lors d'un HotDeploy
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable {
		try {
			emf.close(); // close open connection
			emf = null;
		} finally {
			super.finalize();
		}
	}

	public static void destroy() {
		if (emf != null) {
			emf.close();
		}
	}

	public static <E> E call(final CallDelegate<E> delegate) throws Exception {
		E object = null;
		EntityManager em = EMUtils.getEntityManager();
		try {
			object = delegate.call(em);
			EMUtils.closeEntityManager(em);
		} catch (Exception ex) {
			EMUtils.closeEntityManager(em, true);
			throw ex;
		}
		return object;
	}

	public static void main(String[] args) {
		getEntityManager();
	}

}