package optimization;

/**
 * Created by Maria Efthymiadou on Jun, 2019
 */

public class TimeStampedRoute {
    private String route;
    private double fitness;
    private int duplicates = 0;
    private int mutants = 0;


    public TimeStampedRoute(String route,double fitness) {
        this.route = route;
        this.fitness = fitness;
    }
    
    public TimeStampedRoute(String route,double fitness,int duplicates) {
        this.route = route;
        this.fitness = fitness;
        this.duplicates = duplicates;
    }


    public double getFitness() {
        return fitness;
    }

    public String getRoute() {
        return route;
    }
    
    
    public int getDuplicates() {
        return duplicates;
    }
    
    public void setDuplicates(int duplicates) {
        this.duplicates = duplicates;
    }
    
    public int getMutants() {
        return mutants;
    }
    
    public void setMutants(int mutants) {
        this.mutants = mutants;
    }
    
}
