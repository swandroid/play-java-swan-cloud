package optimization.algorithm.gmooa;


import controllers.OptimizationController;
import optimization.algorithm.ClusterGraph;
import optimization.algorithm.algorithms.Dijkstra;
import optimization.algorithm.helperThreads.DijkstraRunnable;
import optimization.algorithm.helperThreads.EvaluateIndividualThread;
import optimization.algorithm.helperThreads.EvaluatePathThread;
import optimization.algorithm.helperThreads.FillDijkstraPathThread;
import optimization.algortihmsConditions.AlgorithmSettings;
import optimization.algortihmsConditions.MutationOptions;
import optimization.algortihmsConditions.OptimizationMethods;
import optimization.core.GeneticAlgorithm;
import optimization.core.population.Individual;
import optimization.core.population.Paths;
import optimization.core.population.Population;
import optimization.hibernateModels.Edge;
import optimization.hibernateModels.Graph;
import optimization.hibernateModels.Vertex;
import org.apache.commons.collections.CollectionUtils;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.*;

import static optimization.algortihmsConditions.AlgorithmSettings.populationSize;

/**
 * Created by Maria Efthymiadou on Jun, 2019
 */
public class GenericMultiObjectiveAlgorithm  extends GenericMultiObjectiveAlgorithmBase implements GeneticAlgorithm {

    private long deadline;
    private ConcurrentLinkedQueue<ClusterGraph> workQueue = new ConcurrentLinkedQueue<>();
    private ConcurrentHashMap<String,Paths> createdPaths;
    private HashMap<String,HashMap<Integer,Edge>> connectionsMap = new HashMap<>();
    private String userID;
    private Individual superior;
    private Individual elitist;
    
    public GenericMultiObjectiveAlgorithm(Individual elitist, HashMap<Integer,Graph> graphSet
            , HashMap<String,Boolean> increaseObjectives, Vertex origin, Vertex destination, Vertex originCluster
            , Vertex destinationCluster, Integer numberOfIndividuals, OptimizationMethods method, long deadline
            , String userID, List<Vertex> previousPath){
        
        super(graphSet, increaseObjectives, origin, destination, originCluster, destinationCluster, numberOfIndividuals
                , method,deadline,previousPath);
        this.deadline = deadline;
        this.sensorValues.putAll(OptimizationController.optimizationControllerData.sensorValues);
        this.maxSensorValues.putAll(OptimizationController.optimizationControllerData.maxSensorValues);
        this.method = OptimizationMethods.RANDOM_ACYCLIC_WALK;
        this.userID = userID;
        this.minSensorValues.putAll(OptimizationController.optimizationControllerData.minSensorValues);
        this.elitist = elitist;
    }

    public Individual GenericMultiObjectiveAlgorithmRun(){
//        System.out.println("Time in sec until the next intersection "+(deadline-System.currentTimeMillis())/1000);
        if(elitist!=null){
            elitist.setId(-1);
        }
        if (origin.getCluster().equals(destination.getCluster())) {
            Individual individual = runDijkstra();
            while(deadline - System.currentTimeMillis()> AlgorithmSettings.deadlineSingleDijkstra){
                if(elitist!=null){
                    sensorValues.putAll(OptimizationController.optimizationControllerData.sensorValues);
                    individual.setFitness(fitness(individual.getEdgePath(),maxSensorValues,sensorValues,minSensorValues
                            ,increaseObjectives)/individual.getEdgePath().size());
                    elitist.setFitnessMap(fitnessMap(elitist.getEdgePath(),sensorValues,increaseObjectives));
                    elitist.setFitness(fitness(elitist.getEdgePath(),maxSensorValues,sensorValues,minSensorValues
                            ,increaseObjectives)/elitist.getEdgePath().size());
                    if (individual.getFitness() <=elitist.getFitness()) {
                        superior = individual;
                        OptimizationController.optimizationControllerData.elit.put(userID,superior.copy());
                    }
                    else{
                        superior = elitist;
                        OptimizationController.optimizationControllerData.elit.put(userID,superior.copy());
                    }
                }
                individual = runDijkstra();
            }
        } else {
            connectionsMap = filterConnections();
            population = createInitialPopulation();
            evaluate(population);
            if (population.getPopulation().size() > populationSize) {
                population = cropPopulation(populationSize);
                createdPaths = renewCreatedPaths();
            }
            Individual mutant;
            long mutationTime = deadline - System.currentTimeMillis();
                while ((mutationTime - AlgorithmSettings.mutationTime>0)&&(population!=null)) {
                    if ((AlgorithmSettings.mutationOption == MutationOptions.RANDOM)||(superior==null)) {
                        mutant = mutation(population.getPopulation()
                                                      .get(new Random().nextInt(population.getPopulation().size())));
                    } else {
                        mutant = mutation(getBestIndividual(population));
                    }
                    if(mutant!=null) {
                        boolean exist = exists(mutant);
                        if (!exist){
                            population.getPopulation().add(mutant);
                            if (population.getPopulation().size() > populationSize) {
                                population = cropPopulation(populationSize);
                                createdPaths = renewCreatedPaths();
                            }
                        }
                    }
                    evaluate(population);
                    mutationTime = deadline-System.currentTimeMillis();
                }
                while(deadline-System.currentTimeMillis()>0)
                {
                    evaluate(population);
                }
            }
        return superior;
    }
    
    private  ConcurrentHashMap<String,Paths> renewCreatedPaths() {
        ConcurrentHashMap<String,Paths> newPaths = new ConcurrentHashMap<>();
        for(Individual individual:population.getPopulation()){
            if(!origin.getId().equals(individual.getChosenConnections().get(0).getOrigin().getId())){
                if(!newPaths.keySet().contains(origin.getId()+"."+individual.getChosenConnections().get(0).getOrigin().getId()))
                    newPaths.put(origin.getId()+"."+individual.getChosenConnections().get(0).getOrigin().getId()
                            ,createdPaths.get(origin.getId()+"."+individual.getChosenConnections().get(0).getOrigin().getId()));
            }
            for(int i=0;i<individual.getChosenConnections().size()-1;i++){
                if(!individual.getChosenConnections().get(i).getDestination().getId()
                            .equals(individual.getChosenConnections().get(i+1).getOrigin().getId())) {
                    if (!newPaths.keySet().contains(individual.getChosenConnections().get(i).getDestination().getId()
                                                            + "." + individual.getChosenConnections().get(i+1).getOrigin().getId()))
                        newPaths.put(individual.getChosenConnections().get(i).getDestination().getId() + "."
                                             + individual.getChosenConnections().get(i+1).getOrigin().getId()
                                , createdPaths.get(individual.getChosenConnections().get(i).getDestination().getId()
                                                           + "." + individual.getChosenConnections().get(i+1).getOrigin().getId()));
                }
            }
            if(!individual.getChosenConnections().get(individual.getChosenConnections().size()-1).getDestination().getId()
                        .equals(destination.getId())){
                newPaths.put(individual.getChosenConnections().get(individual.getChosenConnections().size()-1)
                                     .getDestination().getId() +"."+(destination.getId()),
                        createdPaths.get(individual.getChosenConnections().get(individual.getChosenConnections().size()-1)
                                                 .getDestination().getId() +"."+(destination.getId())));
            }
        }
        return newPaths;
    }

    private Individual getBestIndividual(Population population) {
        return population.getPopulation().get(0);
    }

    private boolean exists(Individual individualToCheck){
        for (Individual individual: population.getPopulation()) {
            if(individualToCheck.getChosenConnections().size()==individual.getChosenConnections().size()){
                if(equalIndividualsDijkstra(individual,individualToCheck)){
                    return true;
                }
            }
        }
        return false;
    }
    
    @Override
    public Population createInitialPopulation(){
        endPointsInConnection();
        ArrayList<List<Vertex>> skeletonPaths = createSkeletonPaths(userID);
        individualInfo = new ArrayList<>();
        for(int i = 0; i< skeletonPaths.size(); i++){
            individualInfo.add(new Individual(skeletonPaths.get(i)));
        }
        individualInfo =chooseRandomConnections(individualInfo,previousPath,connectionsMap);
        Iterator<Individual> individualIterator = individualInfo.iterator();
        while(individualIterator.hasNext()) {
            Individual individual = individualIterator.next();
            if (duplicated(individual.getChosenConnections())) {
                individualIterator.remove();
            }
        }
        int i=0;
        for(Individual individual:individualInfo){
            i=i+1;
            individual.setId(i);
        }
        createCommonDijkstraPairs(individualInfo);
        createdPaths = runCommonDijkstraPairs();
        population = fillDijkstraPopulation(createdPaths);
        if(population!=null)
            population.setTimestamp(new Timestamp(System.currentTimeMillis()));
        return population;

    }
    
    @Override
    public List<Individual> crossover(Individual individual, Individual individual1) {
        return null;
    }
    
    private Population cropPopulation(int populationSize) {
        Collections.sort(population.getPopulation(), Comparator.comparing(Individual::getFitness));
        return new Population(population.getPopulation().subList(0,populationSize));
    }

    private boolean duplicated(List<Edge> chosenConnections) {
        int i=0;
        for(Individual individual:individualInfo){
            if(CollectionUtils.isEqualCollection(chosenConnections,individual.getChosenConnections()))
                i++;
        }
        return i > 1;
    }
    
    private ConcurrentHashMap<String,Paths>  runCommonDijkstraPairs() {
        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        CompletionService<Paths> executorCompletionService = new ExecutorCompletionService<>(executorService);
        List<Future<Paths>> list = new ArrayList<>();
        for(ClusterGraph clusterGraph:workQueue){
            Future<Paths> future = executorCompletionService.submit(new DijkstraRunnable(clusterGraph,increaseObjectives,
                    clusterGraph.getOrigin(),clusterGraph.getDestination(),sensorValues,maxSensorValues,minSensorValues,previousPath));
            list.add(future);
        }
        ConcurrentHashMap<String,Paths> dijkstraPaths = new ConcurrentHashMap<>();
        for (Future<Paths> fut : list) {
            try {
                Paths paths = fut.get();
                if(paths!=null) {
                    dijkstraPaths.put(paths.getPathID(), paths);
                }else{

                    System.out.println("GenericMultiObjectiveAlgorithm: The Dijkstra path is null");
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        executorService.shutdown();
        return dijkstraPaths;
    }

    /**
     * the method puts the unique pairs for the dijkstra in the workQueue
     * */
    private void createCommonDijkstraPairs(List<Individual> individualInfo) {
        for(Individual individual:individualInfo){
            for(Integer integer:individual.getClustersMap().keySet()){
                    workQueue.add(individual.getClustersMap().get(integer));
            }
        }
    }

    
    @Override
    public void evaluate(Population population) {
        if(population==null)
            superior=elitist;
        else
            superior = evaluate();
        if(deadline-System.currentTimeMillis()>0)
            OptimizationController.optimizationControllerData.elit.put(userID,superior.copy());
        elitist = superior;
    }

    private Individual evaluate(){
        addFitnessToPopulation();
        Collections.sort(population.getPopulation(), Comparator.comparing(Individual::getFitness));
        if(elitist!=null){
            if(elitist.getFitness()<population.getPopulation().get(0).getFitness())
                return elitist;
        }
        return population.getPopulation().get(0);
    }
    
    public double fitness(List<Edge> edgeList, HashMap<String,Double> maxSensorValues, HashMap<String,Double> sensorValues
            , HashMap<String,Double> minSensorValues, HashMap<String,Boolean> objectives){
        double fitness=0;
        for(Edge edge:edgeList){
            fitness+=fitness(edge,maxSensorValues,sensorValues,minSensorValues,objectives);
        }
        return fitness;
    }


    @Override
    public Individual mutation(Individual individual) {
        boolean individualOriginInConnection = false,individualDestinationInConnection = false;
        Individual mutatedIndividual;
        if(origin.getId().equals(individual.getChosenConnections().get(0).getOrigin().getId())){
            individualOriginInConnection = true;
        }
        if(destination.getId().equals(individual.getChosenConnections().get(individual.getChosenConnections().size()-1)
                                              .getDestination().getId())){
            individualDestinationInConnection = true;
        }
        if((individual.getChosenConnections().size()==1)&&(individualOriginInConnection)&&(individualDestinationInConnection)){
            return null;
        }else{
            mutatedIndividual = externalMutation(individual);
            return mutatedIndividual;
        }
    }

    private Individual externalMutation(Individual individual){
        int random = new Random().nextInt(individual.getChosenConnections().size());
        Edge previousConnection = individual.getChosenConnections().get(random);
        HashMap<Integer, Edge> connection = connectionsMap.get(String.valueOf(
                previousConnection.getOrigin().getCluster())+"." +String.valueOf(previousConnection.getDestination().getCluster()));
        List<Edge> connections = new ArrayList<>(connection.values());
        ListIterator<Edge> iter = connections.listIterator();
        while(iter.hasNext()){
            if(iter.next().getEdge_id().equals(previousConnection.getEdge_id())){
                iter.remove();
            }
        }
        if(connections.size()==0)
            return null;
        Edge newChosenConnection = connections.get(new Random().nextInt(connections.size()));
        List<Vertex> path,path1;
        List<Edge> edgePath, edgePath1;
        int position=0;
        for(int m=0;m<individual.getChosenConnections().size();m++){
            if(previousConnection.getEdge_id().equals(individual.getChosenConnections().get(m).getEdge_id()))
                position = m;
        }
        if(individual.getChosenConnections().size()==1){
            addPath(newChosenConnection);
            addEndPath(newChosenConnection);
        }else{
            if(position==0){
                addPath(newChosenConnection);
                if(!newChosenConnection.getDestination().getId().equals(individual.getChosenConnections().get(1).getOrigin().getId())){
                    if(!createdPaths.containsKey(newChosenConnection.getDestination().getId()+"."+individual.getChosenConnections().get(1).getOrigin().getId())){
                        Dijkstra dijkstra1 = new Dijkstra(graphSet.get(newChosenConnection.getDestination().getCluster()),
                                increaseObjectives,sensorValues,maxSensorValues,minSensorValues,previousPath);
                        dijkstra1.execute(newChosenConnection.getDestination());
                        path1 = dijkstra1.getPath(individual.getChosenConnections().get(1).getOrigin());
                        edgePath1=dijkstra1.getEdgesPath(path1,graphSet.get(newChosenConnection.getDestination().getCluster()).getEdges());
                        Paths paths1 = new Paths(path1,edgePath1);
                        paths1.setFitness(dijkstra1.getFitness(edgePath1));
                        paths1.setPathID(newChosenConnection.getDestination().getId()+"."+individual.getChosenConnections().get(1).getOrigin().getId());
                        createdPaths.put(newChosenConnection.getDestination().getId()+"."+individual.getChosenConnections().get(1).getOrigin().getId(),paths1);
                    }
                }
                
            }else if(position==individual.getChosenConnections().size()-1){
                if(!individual.getChosenConnections().get(individual.getChosenConnections().size()-2).getDestination().getId().equals(newChosenConnection.getOrigin().getId())){
                    if (!createdPaths.containsKey(individual.getChosenConnections().get(individual.getChosenConnections().size() - 2).getDestination().getId() + "." + newChosenConnection.getOrigin().getId())) {
                        Dijkstra dijkstra = new Dijkstra(graphSet.get(newChosenConnection.getOrigin().getCluster()), increaseObjectives
                                , sensorValues, maxSensorValues, minSensorValues, previousPath);
                        dijkstra.execute(individual.getChosenConnections().get(individual.getChosenConnections().size() - 2).getDestination());
                        path = dijkstra.getPath(newChosenConnection.getOrigin());
                        edgePath = dijkstra.getEdgesPath(path,graphSet.get(newChosenConnection.getOrigin().getCluster()).getEdges());
                        Paths paths = new Paths(path, edgePath);
                        paths.setFitness(dijkstra.getFitness(edgePath));
                        paths.setPathID(individual.getChosenConnections().get(individual.getChosenConnections().size() - 2).getDestination().getId() + "." + newChosenConnection.getOrigin().getId());
                        createdPaths.put(individual.getChosenConnections().get(individual.getChosenConnections().size() - 2).getDestination().getId() + "." + newChosenConnection.getOrigin().getId(), paths);
                        edgePath.add(newChosenConnection);
                    }
                }
                addEndPath(newChosenConnection);
            }
            else {
    
                if (!createdPaths.containsKey(individual.getChosenConnections().get(position - 1).getDestination().getId()
                                                      + "." + newChosenConnection.getOrigin().getId())) {
                    if (!individual.getChosenConnections().get(position - 1).getDestination().getId()
                                 .equals(newChosenConnection.getOrigin().getId())) {
                        Dijkstra dijkstra = new Dijkstra(graphSet.get(newChosenConnection.getOrigin().getCluster())
                                , increaseObjectives, sensorValues, maxSensorValues, minSensorValues, previousPath);
                        dijkstra.execute(individual.getChosenConnections().get(position - 1).getDestination());
                        path = dijkstra.getPath(newChosenConnection.getOrigin());
                        edgePath = dijkstra.getEdgesPath(path,graphSet.get(newChosenConnection.getOrigin().getCluster()).getEdges());
                        Paths paths = new Paths(path, edgePath);
                        paths.setFitness(dijkstra.getFitness(edgePath));
                        paths.setPathID(individual.getChosenConnections().get(position - 1).getDestination().getId()
                                                + "." + newChosenConnection.getOrigin().getId());
                        createdPaths.put(individual.getChosenConnections().get(position - 1).getDestination().getId()
                                                 + "." + newChosenConnection.getOrigin().getId(), paths);
    
                    }
                }
                if (!createdPaths.containsKey(newChosenConnection.getDestination().getId() + "."
                                                      + individual.getChosenConnections().get(position + 1).getOrigin().getId())) {
                    if (!newChosenConnection.getDestination().getId().equals(individual.getChosenConnections()
                                                                                     .get(position + 1).getOrigin().getId())) {
                        Dijkstra dijkstra1 = new Dijkstra(graphSet.get(newChosenConnection.getDestination().getCluster()),
                                increaseObjectives, sensorValues, maxSensorValues, minSensorValues, previousPath);
                        dijkstra1.execute(newChosenConnection.getDestination());
                        path1 = dijkstra1.getPath(individual.getChosenConnections().get(position + 1).getOrigin());
                        edgePath1 = dijkstra1.getEdgesPath(path1,graphSet.get(newChosenConnection.getDestination().getCluster()).getEdges());
                        Paths paths1 = new Paths(path1, edgePath1);
                        paths1.setFitness(dijkstra1.getFitness(edgePath1));
                        paths1.setPathID(newChosenConnection.getDestination().getId() + "."
                                                 + individual.getChosenConnections().get(position + 1).getOrigin().getId());
                        createdPaths.put(newChosenConnection.getDestination().getId() + "."
                                                 + individual.getChosenConnections().get(position + 1).getOrigin().getId(), paths1);
                    }
                }
    
            }
        }
    
        Individual mutated = new Individual();
        List<Edge> mutatedConnections = new ArrayList<>();
        mutated.setChosenConnections(mutatedConnections);
        for(int k=0;k<individual.getChosenConnections().size();k++){
            if(k==random)
                mutatedConnections.add(newChosenConnection);
            else
                mutatedConnections.add(individual.getChosenConnections().get(k));
        }
        mutated.setChosenConnections(mutatedConnections);
        mutated.setOrigin(individual.getOrigin());
        mutated.setDestination(individual.getDestination());
        ExecutorService execService = Executors.newFixedThreadPool(1);
        List<Future<Individual>> futures = new ArrayList<>();
        for (int i = 0; i < 1; i++) {
            futures.add(execService.submit(new FillDijkstraPathThread(mutated, createdPaths, origin, destination
                    ,this.maxSensorValues,this.sensorValues,this.minSensorValues,this.increaseObjectives)));
        }
        for (Future<Individual> future : futures) {
            try {
                try {
                    mutated=future.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        execService.shutdown();
        mutated.setId(-2);
        return mutated;
    }
    
    private void addEndPath(Edge newChosenConnection) {
        List<Vertex> path1;
        List<Edge> edgePath1;
        if(!newChosenConnection.getDestination().getId().equals(destination.getId())){
            if(!createdPaths.containsKey(newChosenConnection.getDestination().getId()+"."+destination.getId())){
                Dijkstra dijkstra1 = new Dijkstra(graphSet.get(newChosenConnection.getDestination().getCluster()),
                        increaseObjectives,sensorValues,maxSensorValues,minSensorValues,previousPath);
                dijkstra1.execute(newChosenConnection.getDestination());
                path1 = dijkstra1.getPath(destination);
                edgePath1=dijkstra1.getEdgesPath(path1,graphSet.get(newChosenConnection.getDestination().getCluster()).getEdges());
                Paths paths1 = new Paths(path1,edgePath1);
                paths1.setFitness(dijkstra1.getFitness(edgePath1));
                paths1.setPathID(newChosenConnection.getDestination().getId()+"."+destination.getId());
                createdPaths.put(newChosenConnection.getDestination().getId()+"."+destination.getId(),paths1);
            }
        }
    }
    
    private void addPath(Edge newChosenConnection) {
        List<Vertex> path;
        List<Edge> edgePath;
        if(!origin.getId().equals(newChosenConnection.getOrigin().getId())){
            if(!createdPaths.containsKey(origin.getId()+"."+newChosenConnection.getOrigin().getId())){
                Dijkstra dijkstra = new Dijkstra(graphSet.get(origin.getCluster()),increaseObjectives
                        ,sensorValues,maxSensorValues,minSensorValues,previousPath);
                dijkstra.execute(origin);
                path= dijkstra.getPath(newChosenConnection.getOrigin());
                edgePath = dijkstra.getEdgesPath(path,graphSet.get(origin.getCluster()).getEdges());
                Paths paths = new Paths(path,edgePath);
                paths.setFitness(dijkstra.getFitness(edgePath));
                paths.setPathID(origin.getId()+"."+newChosenConnection.getOrigin().getId());
                createdPaths.put(origin.getId()+"."+newChosenConnection.getOrigin().getId(),paths);
                edgePath.add(newChosenConnection);
            }
        }
    }
    
    private Population fillDijkstraPopulation(Map<String,Paths> unmap){
        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        CompletionService<Individual> executorCompletionService = new ExecutorCompletionService<>(executorService);

        List<Future<Individual>> list=new ArrayList<>();
        int i=0;
        for(Individual individual:individualInfo){
            individual.setId(i);
            Future<Individual> fillthread = executorCompletionService.submit(new FillDijkstraPathThread(individual,
                    unmap, origin, destination,this.maxSensorValues,this.sensorValues,this.minSensorValues,this.increaseObjectives));
            list.add(fillthread);
            i++;
        }
        List<Individual> safeList  = addIndividualConnections(list);
        executorService.shutdown();
        if(safeList.size()>0){
            return new Population(safeList);
        }else return null;
    }
    
    private void addFitnessToPopulation() {
        HashMap<String, Double> map = new HashMap<>(OptimizationController.optimizationControllerData.sensorValues);
        evaluatePaths(map);
        evaluatePopulation(map);
        if(elitist!=null){
            elitist.setFitnessMap(fitnessMap(elitist.getEdgePath(),map,increaseObjectives));
            elitist.setFitness(fitness(elitist.getEdgePath(),maxSensorValues,map,minSensorValues,increaseObjectives)
                                       /elitist.getEdgePath().size());
        }
    }
    
    private  void evaluatePopulation(HashMap<String, Double> map) {
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        List<Future> tasks = new ArrayList();
        for(Individual individual:population.getPopulation()){
            Future future = executorService.submit(new EvaluateIndividualThread(individual,createdPaths ,
                    origin,destination,maxSensorValues,map,minSensorValues,increaseObjectives));
            tasks.add(future);
        }

        for (Future fut : tasks) {
            try {
                fut.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        executorService.shutdown();
    }

    private void evaluatePaths(HashMap<String, Double> map) {
        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        List<Future<Paths>> tasks = new ArrayList();
        for(String  string:createdPaths.keySet()){
            Future<Paths> future = executorService.submit(new EvaluatePathThread(createdPaths.get(string), map
                    ,increaseObjectives,maxSensorValues,minSensorValues));
            tasks.add(future);
        }

        for (Future<Paths> fut : tasks) {
            try {
                Paths paths = fut.get();
                if(paths!=null){
                    createdPaths.put(paths.getPathID(),paths);
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        executorService.shutdown();
    }
    
    private boolean equalIndividualsDijkstra(Individual individual, Individual individualToCheck) {
        for(int i=0;i<individual.getChosenConnections().size();i++){
            if(!individual.getChosenConnections().get(i).getEdge_id().equals(individualToCheck.getChosenConnections().get(i).getEdge_id())){
                return false;
            }
        }
        return true;
    }

}
