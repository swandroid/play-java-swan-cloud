package optimization.algorithm.gmooa;

import controllers.OptimizationController;
import optimization.algorithm.algorithms.Dijkstra;
import optimization.algorithm.algorithms.RandomAcyclic;
import optimization.algorithm.algorithms.RandomWalk;
import optimization.algorithm.helperThreads.RandomConnectionsRunnable;
import optimization.algortihmsConditions.AlgorithmSettings;
import optimization.algortihmsConditions.OptimizationMethods;
import optimization.core.GeneticAlgorithm;
import optimization.core.fitness.Fitness;
import optimization.core.population.Individual;
import optimization.core.population.Paths;
import optimization.core.population.Population;
import optimization.hibernateModels.Edge;
import optimization.hibernateModels.Graph;
import optimization.hibernateModels.Vertex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by Maria Efthymiadou on Jun, 2019
 */
public class GenericMultiObjectiveAlgorithmBase extends Fitness implements GeneticAlgorithm {
    protected HashMap<String,Boolean> increaseObjectives;
    protected Vertex origin;
    protected Vertex destination;
    protected HashMap<Integer, Graph> graphSet;
    protected Integer numberOfIndividuals;
    protected long deadline;
    protected Vertex originCluster;
    protected Vertex destinationCluster;
    protected ArrayList<List<Vertex>> skeletonPaths;
    protected Population population;
//    protected ConcurrentLinkedQueue<ClusterGraph> workQueue = new ConcurrentLinkedQueue<>();
    protected OptimizationMethods method;
    protected List<Individual> individualInfo;
    protected HashMap<String,Double> sensorValues = new HashMap<>();
    protected HashMap<String,Double> maxSensorValues = new HashMap<>();
    protected HashMap<String,Double> minSensorValues = new HashMap<>();
    protected int originInConnectionEdge = 0;
    protected int destinationInConnectionEdge = 0;
    protected List<Vertex> previousPath;

    public GenericMultiObjectiveAlgorithmBase(HashMap<Integer,Graph> graphSet, HashMap<String,Boolean> increaseObjectives
            , Vertex origin, Vertex destination, Vertex originCluster, Vertex destinationCluster
            , Integer numberOfIndividuals, OptimizationMethods method, long deadline, List<Vertex> previousPath){
        this.increaseObjectives = increaseObjectives;
        this.origin = origin;
        this.destination = destination;
        this.graphSet = graphSet;
        this.numberOfIndividuals = numberOfIndividuals;
        this.deadline = deadline;
        this.originCluster = originCluster;
        this.destinationCluster = destinationCluster;
        this.method = method;
        this.sensorValues = sensorValues;
        this.maxSensorValues = maxSensorValues;
        this.minSensorValues = minSensorValues;
        this.previousPath = previousPath;

    }

    protected Individual runDijkstra(){
        Individual individualDijkstra = new Individual();
        if(origin.getId().equals(destination.getId())){
//	        System.out.println("origin "+origin.getId()+" destination "+destination.getId());
	        individualDijkstra.setPath(null);
            individualDijkstra.setEdgePath(null);
            individualDijkstra.setFitness(0);
            individualDijkstra.setFitnessMap(null);
            return individualDijkstra;
        }
        Dijkstra dijkstra = new Dijkstra(graphSet.get(origin.getCluster()), increaseObjectives, sensorValues
                , maxSensorValues,minSensorValues,previousPath);
        dijkstra.execute(origin);
        if (dijkstra.getPath(destination) == null) {
            System.out.println("Dijkstra path is null");
            return null;
        } else {
            individualDijkstra = new Individual();
            individualDijkstra.setPath(dijkstra.getPath(destination));
            individualDijkstra.setEdgePath(dijkstra.getEdgesPath(individualDijkstra.getPath(),graphSet.get(origin.getCluster()).getEdges()));
            individualDijkstra.setFitnessMap(fitnessMap(individualDijkstra.getEdgePath(),sensorValues,increaseObjectives));
            individualDijkstra.setFitness(fitness(individualDijkstra.getFitnessMap(),individualDijkstra.getEdgePath().size()
                    ,maxSensorValues,minSensorValues,increaseObjectives));
            return individualDijkstra;
        }
    }

    protected ArrayList<List<Vertex>> createSkeletonPaths(String userID){
        Integer individuals=0;
        skeletonPaths=new ArrayList<>();
        Graph skeletonGraph;
        if(AlgorithmSettings.skeletonOption.equals(AlgorithmSettings.skeletonOption.USER_SKELETON))
        {
            skeletonGraph = OptimizationController.optimizationControllerData.activeUsersData.get(userID)
                                    .getUserSkeletonGraph();
        }
        else{
            skeletonGraph = new Graph(OptimizationController.optimizationControllerData.skeleton.getVertexes()
                    ,OptimizationController.optimizationControllerData.skeleton.getEdges());
        }
        if(method.equals(OptimizationMethods.RANDOM_ACYCLIC_WALK)){
            while(individuals<numberOfIndividuals) {

                RandomAcyclic randomAcyclic = new RandomAcyclic(skeletonGraph);
                randomAcyclic.execute(originCluster, destinationCluster);
                skeletonPaths.add(randomAcyclic.getPath());
                individuals++;
            }
//            System.out.println("FINISH createSkeletonPaths");
        }
        if(method.equals(OptimizationMethods.RANDOM_WALK)){
            while(individuals<this.numberOfIndividuals) {
                RandomWalk randomWalk = new RandomWalk(skeletonGraph);
                randomWalk.execute(originCluster, destinationCluster);
                skeletonPaths.add(randomWalk.getPath());
                individuals++;
                if (individuals > this.numberOfIndividuals - 1)
                    break;
            }
        }
        return skeletonPaths;
    }

    protected List<Individual> chooseRandomConnections(List<Individual> individualInfo, List<Vertex> previousPath
            , HashMap<String,HashMap<Integer,Edge>> connections) {
        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        CompletionService<Paths> executorCompletionService = new ExecutorCompletionService<>(executorService);
        List<Future<Individual>> list = new ArrayList<>();
        for(Individual individual:individualInfo) {
            Future<Individual> future = executorCompletionService.submit(new RandomConnectionsRunnable(individual
                    , origin, destination,graphSet,previousPath,connections));
            list.add(future);
        }
    
        List<Individual> safeList  = addIndividualConnections(list);
        executorService.shutdown();
        return safeList;
    }
    
    protected List<Individual>  addIndividualConnections(List<Future<Individual>> list) {
        List<Individual> unsafeList = new ArrayList<>();
        List<Individual> safeList = Collections.synchronizedList(unsafeList);
        for (Future<Individual> fut : list) {
            try {
                Individual individual = fut.get();
                if(individual!=null) {
                    safeList.add(individual);
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        return safeList;
    }
    
    
    public void endPointsInConnection(){
        HashMap<String, HashMap<Integer, Edge>> connections = OptimizationController.optimizationControllerData.connectionsEdgeHashMap;
        for(String string:connections.keySet()) {
            if (string.contains(originCluster.getId())){
                for (Integer integer : connections.get(string).keySet()) {
                    if (connections.get(string).get(integer).getOrigin().getId().equals(origin.getId())) {
                        originInConnectionEdge = 1;
                    }
                }
            }
            if (string.contains(destinationCluster.getId())){
                for (Integer integer : connections.get(string).keySet()) {
                    if (connections.get(string).get(integer).getDestination().getId().equals(destination.getId())) {
                        destinationInConnectionEdge = 1;
                    }
                }
            }
        }
    }



    @Override
    public Population createInitialPopulation(){
        return null;
    }

    @Override
    public List<Individual> crossover(Individual individual, Individual individual1) {
        return null;
    }

    @Override
    public Individual mutation(Individual individual) {
        return null;
    }

    @Override
    public void evaluate(Population population) {

    }
    
    protected HashMap<String, HashMap<Integer, Edge>> filterConnections() {
        HashMap<String, HashMap<Integer, Edge>> connectionEdges = new HashMap<>();
        if((previousPath==null)||(previousPath.isEmpty())){
            return OptimizationController.optimizationControllerData.connectionsEdgeHashMap;
        }else{
            for(String string:OptimizationController.optimizationControllerData.connectionsEdgeHashMap.keySet()){
                HashMap<Integer,Edge> internalMap = new HashMap<>();
                for(Integer integer:OptimizationController.optimizationControllerData.connectionsEdgeHashMap
                                            .get(string).keySet()){
                    if(!previousPath.contains(OptimizationController.optimizationControllerData.connectionsEdgeHashMap
                                                      .get(string).get(integer).getOrigin())&& (!previousPath
                                         .contains(OptimizationController.optimizationControllerData.connectionsEdgeHashMap
                                                           .get(string).get(integer).getDestination()))){
                        internalMap.put(integer,OptimizationController.optimizationControllerData.connectionsEdgeHashMap
                                                        .get(string).get(integer));
                    }
                }
                connectionEdges.put(string,internalMap);
            }
            return connectionEdges;
        }
    }
    
    public static List<Integer> containEdge(Edge vertex, List<Edge> vertexPath) {
        int k=0;
        List<Integer> list = new ArrayList<>();
        for(int j=0;j<vertexPath.size();j++){
            if(vertex.getEdge_id().equals(vertexPath.get(j).getEdge_id())){
                k++;
                if(k==0)
                    list.add(j);
                if(k>1){
                    list.add(j);
                    return list;
                }
            }
        }
        return list;
    }

}
