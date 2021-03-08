package com.sos.ui5.battle.sos.simulator;

import java.util.ArrayList;
import java.util.List;

import com.sos.ui5.battle.utils.Tuple;

import flanagan.math.*;

public class MinimisationSOS implements MinimisationFunction {
	private Simulation simulation;
	private int max;
	private List<Tuple<Integer, Integer>> lst;

	public MinimisationSOS(Simulation simulation, int max, List<Tuple<Integer, Integer>> lst) {
		this.simulation = simulation;
		this.max = max;
		this.lst = lst;
	}

	// evaluation function
	public double function(double[] params) {
		// Vérification de ne pas dépasser les bornes
		int sum = 0;
		for (double d : params) {
			sum += d;
			if (d < 1) {
				return 2;
			}
		}

		if (sum > max) {
			return 2;
		}

		// int hun = max - inf - cav;
		int pos = 0;
		String log = "";
		simulation.getAttacker().getTroops().clear();
		for (double d : params) {
			int val = (int) Math.round(d);
			simulation.getAttacker().getTroops().add(new SimulationTroop(lst.get(pos++).getX(), val));
			log = log + val + " ";
		}
		simulation.getAttacker().getTroops().add(new SimulationTroop(lst.get(pos++).getX(), max - sum));
		log = log + (max - sum) + " ";

		try {
			Fight fight = simulation.calc();
			Double score = (Double) fight.getResult().get(0).get("score");
			//Si on est sur le piege ou si le score est trop proche de 100% on utilise le nombre de mort à la place
			if (score>=0.99) {
				score = 1.0*(Integer)fight.getResult().get(2).get("total");
			} else if (score<=-0.99) {
				score = 1.0*(Integer)fight.getResult().get(1).get("total");
			}
			
			return -score;
		} catch (Exception e) {
			return 2;
		}
	}

	public static Integer[] minimise(Simulation simulation, List<Tuple<Integer, Integer>> lst) {
		// Create instance of Minimisation
		Minimisation min = new Minimisation();

		int max = 0;
		for (Tuple<Integer, Integer> t : lst) {
			max += t.getY();
		}

		MinimisationSOS funct = new MinimisationSOS(simulation, max, lst);
		
		double[] start = new double[lst.size()-1];
		double[] step = new double[lst.size()-1];
		
		for (int i=0; i<lst.size()-1; i++) {
			start[i] = max / lst.size();
			step[i] =max / (lst.size()+1);
		}
		
		// Nelder and Mead minimisation procedure
		min.setNmax(500);
		min.setNrestartsMax(4);
		min.nelderMead(funct, start, step, 0.0001);
		
		int sum = max;
		List<Integer> ret = new ArrayList<Integer>();
		for (double d : min.getParamValues()) {
			int val = (int) Math.round(d);
			sum -= val>sum?sum:val;
			ret.add(val);
		}		
		ret.add(sum);

		return ret.toArray(new Integer[ret.size()]);
	}

}