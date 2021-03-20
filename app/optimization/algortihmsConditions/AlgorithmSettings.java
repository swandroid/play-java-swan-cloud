package optimization.algortihmsConditions;

/**
 * Created by Maria Efthymiadou on Jun, 2019
 */
public class AlgorithmSettings {
    
    //the weights of the objectives used in the fitness method
    public static double SOUND_WEIGHT = 0.3;
    public static double AIR_WEIGHT = 0.3;
    public static double DISTANCE_WEIGHT = 0.4;
    
    //the number of individuals allowed in every new population generation
    public static int populationSize = 50;
    
    //the maximum number of the initial population size
    public static int maxInitialPopulationSize = 50;
    
    //the angle as input of the DirectedRandom algorithm
    public static float ANGLE = 120;
    
    //the minimum required time for the algorithm to produce a path. It depends on the number of clusters and their size
    public static long minimumAlgorithmRunTime = 10000;
    
    //the time in ms that the SensorValuesModifier changes the values
    public static int time = 1000;
    
    //the maximum mutation time required by the algorithm. It depends on the number of clusters, their size and the used
    // algorithm (GMOODA or GMOODRA)
    public static long mutationTime = 2000;

    //the maximum time in ms dedicated to the mutation for the GMODRA algorithm.
    public static long mutationDeadline = 4000;
    
    //the files contain the sound and air data for sensors
    public static String soundSensorValuesFile = "";
    public static String airSensorValuesFile = "";
    
    public static final double _eQuatorialEarthRadius = 6378.1370D;
    public static final double _d2r = (Math.PI / 180D);
    
    //this time is added to the calculated time for the production of a single individual. If the user available time is
    //does not exceed it, the elitist individual is send to the user
    public static long timeThreshold = 3000;
    
    //the option to use the skeleton graph of the user for future reuse
    public static SkeletonGraphOptions skeletonOption = SkeletonGraphOptions.USER_SKELETON;
    
    //the mutation is performed to the fittest individual
    public static MutationOptions mutationOption = MutationOptions.FITTEST;
    
    //the time dedicated to run a single dijkstra. This should be adapted based on the size of the graph
    public static long deadlineSingleDijkstra = 900;
    
    //the time to evaluate the existing population.
    public static long deadlineEvaluationPopulation = 50;
    
    //the geentic algorithm to use for the production of the navigation path
    public static GeneticAlgorithmOption geneticAlgorithmOption = GeneticAlgorithmOption.GENETICMULTIOBJECTIVEALGORITM;
    
    
    
}