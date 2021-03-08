package com.sos.ui5.battle.web;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class StatisticsService {
	private String code;
	private long dureeTotal = 0;
	private long nombre = 0;
	private long erreurs = 0;
	private long min = 0;
	private long max = 0;
	private static final Map<String, StatisticsService> stats = new HashMap<String, StatisticsService>();
	private Map<Integer, ExceptionInfo> exceptions = new HashMap<Integer, ExceptionInfo>();

	public static class ExceptionInfo {
		String message;
		String type;
		String stack;
		Date date;
		int nb = 0;
		
		public String getMessage() {
			return message;
		}
		public void setMessage(String message) {
			this.message = message;
		}
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
		public String getStack() {
			return stack;
		}
		public void setStack(String stack) {
			this.stack = stack;
		}
		public Date getDate() {
			return date;
		}
		public void setDate(Date date) {
			this.date = date;
		}
		public int getNb() {
			return nb;
		}
		public void setNb(int nb) {
			this.nb = nb;
		}
	}


	public static Map<String, StatisticsService> getStats() {
		return stats;
	}

	public static synchronized void add(String code, long debut, Throwable ex) {
		StatisticsService statistiques = stats.get(code);
		if (statistiques == null) {
			statistiques = new StatisticsService();
			stats.put(code, statistiques);
		}

		long duree = System.currentTimeMillis() - debut;

		statistiques.setCode(code);
		statistiques.setDureeTotal(statistiques.getDureeTotal() + duree);

		if (duree < statistiques.getMin() || statistiques.getMin() == 0) {
			statistiques.setMin(duree);
		}

		if (duree > statistiques.getMax() || statistiques.getMax() == 0) {
			statistiques.setMax(duree);
		}

		statistiques.setNombre(statistiques.getNombre() + 1);
		if (ex != null) {
			statistiques.setErreurs(statistiques.getErreurs() + 1);

			String message = ex.getMessage();

			StringWriter sw = new StringWriter();
			ex.printStackTrace(new PrintWriter(sw));
			

			int hash = sw.toString().hashCode();

			ExceptionInfo exceptionInfo = statistiques.exceptions.get(hash);
			if (exceptionInfo == null) {
				exceptionInfo = new ExceptionInfo();
				exceptionInfo.stack = sw.toString();
				exceptionInfo.type = ex.getClass().getName();
				statistiques.exceptions.put(hash, exceptionInfo);
			}
			exceptionInfo.date = new Date();
			exceptionInfo.message = message;
			exceptionInfo.nb += 1;
		}
	}
	
	public static Collection<StatisticsService> build() {
		return StatisticsService.getStats().values();
	}
	
	public static Collection<ExceptionInfo> getStack(String code) {
		return stats.get(code).exceptions.values();
	}


	public void setDureeTotal(long dureeTotal) {
		this.dureeTotal = dureeTotal;
	}

	public long getDureeTotal() {
		return dureeTotal;
	}

	public void setNombre(long nombre) {
		this.nombre = nombre;
	}

	public long getNombre() {
		return nombre;
	}

	public void setErreurs(long erreurs) {
		this.erreurs = erreurs;
	}

	public long getErreurs() {
		return erreurs;
	}

	public void setMax(long max) {
		this.max = max;
	}

	public long getMax() {
		return max;
	}

	public void setMin(long min) {
		this.min = min;
	}

	public long getMin() {
		return min;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public static void clear() {
		stats.clear();
	}
}