package optimization.core.population;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Maria Efthymiadou on Jun, 2019
 */

public class Population {
    private HashMap<String,Double> objectivesMaximumValues;
    private HashMap<String,Double> objectivesMinimumValues;
    private List<Individual> population;
    private Timestamp timestamp;

    public List<Individual> getPopulation() {
        return population;
    }

    public Population(){
    }

    public Population(List<Individual> population){
        this.population = population;
    }

    public HashMap<String, Double> getObjectivesMaximumValues() {
        return objectivesMaximumValues;
    }

    public void setObjectivesMaximumValues(HashMap<String, Double> objectivesMaximumValues) {
        this.objectivesMaximumValues = objectivesMaximumValues;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public HashMap<String, Double> getObjectivesMinimumValues() {
        return objectivesMinimumValues;
    }

    public void setObjectivesMinimumValues(HashMap<String, Double> objectivesMinimumValues) {
        this.objectivesMinimumValues = objectivesMinimumValues;
    }
}
