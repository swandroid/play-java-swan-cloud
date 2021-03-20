package optimization.core.fitness;

import java.util.HashMap;
import java.util.Set;

/**
 * Created by Maria Efthymiadou on Jun, 2019
 */

public class FitnessMap {
	private HashMap<String,Double> values = new HashMap<>();
	private double totalFitness;
	
	public FitnessMap(){
	
	}
	
	public FitnessMap(HashMap<String,Double> values){
		this.values = values;
		
	}
	public FitnessMap(HashMap<String,Double> values,double totalFitness){
		this.values = values;
		this.totalFitness = totalFitness;
		
	}
	
	public double getTotalFitness() {
		return totalFitness;
	}
	
	public void setTotalFitness(double totalFitness) {
		this.totalFitness = totalFitness;
	}
	
	public HashMap<String, Double> getValues() {
		return values;
	}
	
	public void setValues(HashMap<String, Double> values) {
		this.values = values;
	}
	
	public void init(Set<String> objectives){
		for(String string: objectives){
			this.values.put(string, (double) 0);
		}
	}
}
