package optimization.algorithm.gmooa;

import controllers.OptimizationController;
import optimization.algorithm.ClusterGraph;
import optimization.algorithm.helperThreads.DirectedRandomCallable;
import optimization.algorithm.helperThreads.EvaluateIndividualInterClusterThread;
import optimization.algorithm.helperThreads.FillDirectedPathThread;
import optimization.algorithm.helperThreads.MutationTask;
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

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.*;

import static java.lang.System.exit;
import static optimization.algortihmsConditions.AlgorithmSettings.populationSize;

/**
 * Created by Maria Efthymiadou on Jun, 2019
 */
public class GenericMultiObjectiveDirectedAlgorithm extends GenericMultiObjectiveAlgorithmBase implements GeneticAlgorithm {
    
    
    private ConcurrentLinkedQueue<ClusterGraph> workQueue = new ConcurrentLinkedQueue<>();
    private ConcurrentHashMap<String,Paths> createdPaths;
    private HashMap<String,HashMap<Integer,Edge>> connectionsMap = new HashMap<>();
    private String userID;
    private Individual superior;
    private Individual elitist;
    private long deadline;
    private int mutationCount = 1;
    
    
    public GenericMultiObjectiveDirectedAlgorithm(Individual elitist, HashMap<Integer,Graph> graphSet, HashMap<String,Boolean> increaseObjectives, Vertex origin,
                                                  Vertex destination, Vertex originCluster, Vertex destinationCluster,
                                                  Integer numberOfIndividuals, OptimizationMethods method, long deadline, String userID, List<Vertex> previousPath){
        super(graphSet, increaseObjectives, origin, destination, originCluster, destinationCluster, numberOfIndividuals, method,deadline,previousPath);
        this.deadline = deadline;
        this.sensorValues.putAll(OptimizationController.optimizationControllerData.sensorValues);
        this.maxSensorValues.putAll(OptimizationController.optimizationControllerData.maxSensorValues);
        this.userID = userID;
        this.minSensorValues.putAll(OptimizationController.optimizationControllerData.minSensorValues);
        this.elitist = elitist;

    }

    private Population fillDirectedPopulation(){
        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        CompletionService<Individual> executorCompletionService = new ExecutorCompletionService<>(executorService);
        List<Future<Individual>> list=new ArrayList<>();
        int i=0;

        for(Individual individual:individualInfo){
            individual.setId(i);
            Future<Individual> fillThread = executorCompletionService.submit(new FillDirectedPathThread(individual, createdPaths, origin, destination));
            list.add(fillThread);
            i++;
        }
        List<Individual> unsafeList = new ArrayList<>();
        List<Individual> safeList = Collections.synchronizedList(unsafeList);
        for (Future<Individual> fut : list) {
            try {
                Individual individual = fut.get();
                if(individual==null) {
                    exit(1);
                }
                else
                    safeList.add(individual);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        executorService.shutdown();
        return new Population(safeList);
    }

    public Individual GenericMultiObjectiveDirectedAlgorithmRun(){
//        System.out.println("deadline "+ deadline);
//        System.out.println("Time in sec until the next intersection "+(deadline-System.currentTimeMillis())/1000);
        if(elitist!=null){
            elitist.setId(-1);
        }
        long mutationTime;
        if (origin.getCluster().equals(destination.getCluster())) {
            createInterClusterInitialPopulation();
            if(population.getPopulation().size()>0) {
                evaluate(population);
                if (population.getPopulation().size() > populationSize)
                    population = cropPopulation(populationSize);
                Individual mutant;
                mutationTime = deadline - System.currentTimeMillis();
                while ((mutationTime - AlgorithmSettings.mutationTime > 0) && (population != null)) {
                    if ((AlgorithmSettings.mutationOption == MutationOptions.RANDOM) || (superior == null)) {
                        mutant = internalInterClusterMutation(population.getPopulation()
                                                                      .get(new Random().nextInt(population.getPopulation().size())));
                    } else {
                        mutant = internalInterClusterMutation(getBestIndividual(population));
                    }
                    if (mutant != null) {
                        population.getPopulation().add(mutant);
                    }
                    evaluate(population);
                    if (mutant != null) {
                        if (population.getPopulation().size() > populationSize) {
                            population = cropPopulation(populationSize);
                        }
                    }
                    mutationTime = deadline - System.currentTimeMillis();
                }
                while (deadline - System.currentTimeMillis() > AlgorithmSettings.deadlineEvaluationPopulation) {
                    evaluate(population);
                }
            }
        } else {
            connectionsMap = filterConnections();
            population = createInitialPopulation();
            evaluate(population);
            if (population.getPopulation().size() > populationSize) {
                population = cropPopulation(populationSize);
            }
            Individual mutant;
            while ((deadline-System.currentTimeMillis()- AlgorithmSettings.mutationTime>0)&&(population!=null)){
                if (AlgorithmSettings.mutationOption == MutationOptions.RANDOM) {
                    mutant = mutation(population.getPopulation().get(new Random().nextInt(population.getPopulation().size())));
                } else {
                    mutant = mutation(getBestIndividual(population));
                }
                if (mutant!=null) {
                    population.getPopulation().add(mutant);
                    if(population.getPopulation().size()>populationSize){
                        population = cropPopulation(populationSize);
                    }
                }
                evaluate(population);
            }
            while(deadline-System.currentTimeMillis()>AlgorithmSettings.deadlineEvaluationPopulation)
            {
                evaluate(population);
            }
        }
        return superior;
    }
    
    private Population cropPopulation(int populationSize) {
        Collections.sort(population.getPopulation(), Comparator.comparing(Individual::getFitness));
        return new Population(population.getPopulation().subList(0,populationSize));
    }

    private void evaluateIndividuals(HashMap<String, Double> map) {
        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        List<Future> tasks = new ArrayList();
        for(Individual individual:population.getPopulation()){
            Future future = executorService
                                    .submit(new EvaluateIndividualInterClusterThread(individual, map,increaseObjectives
                                            ,maxSensorValues,minSensorValues));
            tasks.add(future);
        }
        for (Future fut : tasks) {
            try {
                if(fut.get()!=null) {
                    System.out.println("Future is null");
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        executorService.shutdown();
    }
    
    private void createInterClusterInitialPopulation() {
        individualInfo = new ArrayList<>();
        for(int i=0;i<numberOfIndividuals;i++){
            ClusterGraph clusterGraph = new ClusterGraph(i,graphSet.get(origin.getCluster()),origin,destination);
            clusterGraph.setId(String.valueOf(i));
            workQueue.add(clusterGraph);
        }
        for(int i=0;i<workQueue.size();i++){
            individualInfo.add(new Individual(i));
        }
        createIndividuals(deadline-AlgorithmSettings.mutationTime);
        population.setTimestamp(new Timestamp(System.currentTimeMillis()));
    }
    
    
    private void createIndividuals(long time) {
        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        CompletionService<Paths> executorCompletionService = new ExecutorCompletionService<>(executorService);
            List<Future<Paths>> list = new ArrayList<>();
            for(ClusterGraph clusterGraph:workQueue){
                Future<Paths> future = executorCompletionService.submit(new DirectedRandomCallable(clusterGraph
                        ,increaseObjectives,clusterGraph.getOrigin(),clusterGraph.getDestination(),sensorValues
                        ,maxSensorValues,previousPath));
                list.add(future);
            }
            ConcurrentHashMap<String,Paths> clusterPath = new ConcurrentHashMap<>();
            int i=0;
            for (Future<Paths> fut : list) {
                try {
                    Paths paths = fut.get();
                    if(paths!=null) {
                        synchronized (this){
                            int size = clusterPath.size();
                            paths.setPathID(String.valueOf(size));
                            clusterPath.put(String.valueOf(size), paths);
                            i++;
                            Individual individual = new Individual(i);
                            individual.setPath(paths.getVertexPath());
                            individual.setEdgePath(paths.getEdgePath());
                            individual.setId(i);
                            i++;
                            if(System.currentTimeMillis()>time+100)
                                population.getPopulation().add(individual);
                        }
                    }
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
            executorService.shutdown();
    }
    
    @Override
    public Population createInitialPopulation(){
        endPointsInConnection();
        ArrayList<List<Vertex>> skeletonPaths = createSkeletonPaths(userID);
        individualInfo = new ArrayList<>();
        for(int i = 0; i< skeletonPaths.size(); i++)
            individualInfo.add(new Individual(skeletonPaths.get(i)));
        
        individualInfo =chooseRandomConnections(individualInfo,previousPath,connectionsMap);
        int i=0;
        for(Individual individual:individualInfo){
            individual.setId(i++);
        }
        createClusterPairs(individualInfo);
        createdPaths = runClusterPairs();
        population=fillDirectedPopulation();
        population.setTimestamp(new Timestamp(System.currentTimeMillis()));
        return population;
    }
    
    private List<Edge> checkForEdgeCycle(List<Edge> edgePath) {
        Iterator<Edge> edgePathIterator = edgePath.iterator();
        while (edgePathIterator.hasNext()) {
            List<Integer> list = containEdge(edgePathIterator.next(),edgePath);
            if(list.size()>1){
                removeCycle(edgePath,list);
            }
        }
        return edgePath;
    }

    private List<Edge> removeCycle(List<Edge> edgePath, List<Integer> list) {
        List<Edge> edgeList = new ArrayList<>();
        edgeList.addAll(edgePath.subList(0,list.get(0)));
        edgeList.addAll(edgePath.subList(list.get(1),edgePath.size()));
        return edgeList;
    }

    @Override
    public List<Individual> crossover(Individual individual, Individual individual1) {
        return null;
    }

    private ConcurrentHashMap<String,Paths> runClusterPairs() {
        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        CompletionService<Paths> executorCompletionService = new ExecutorCompletionService<>(executorService);
        List<Future<Paths>> list = new ArrayList<>();
        for(ClusterGraph clusterGraph:workQueue){
            Future<Paths> future = executorCompletionService.submit(new DirectedRandomCallable(clusterGraph,increaseObjectives,
                    clusterGraph.getOrigin(),clusterGraph.getDestination(),sensorValues,maxSensorValues,previousPath));
            list.add(future);

        }
        ConcurrentHashMap<String,Paths> clusterPath = new ConcurrentHashMap<>();
        for (Future<Paths> fut : list) {
            try {
                Paths paths = fut.get();
                if(paths==null) {
                    System.out.println("The path is null");
                    exit(1);
                }
                else {
                    clusterPath.put(paths.getPathID(), paths);
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        executorService.shutdown();
        return clusterPath;

    }

    @Override
    public Individual mutation(Individual individual) {
        boolean individualOriginInConnection = false,individualDestinationInConnection = false;
        Individual mutatedIndividual;
        if(origin.getId().equals(individual.getChosenConnections().get(0).getOrigin().getId())){
            individualOriginInConnection = true;
        }
        if(destination.getId().equals(individual.getChosenConnections().get(individual.getChosenConnections().size()-1).getDestination().getId())){
            individualDestinationInConnection = true;
        }
        if((individual.getChosenConnections().size()==1)&&(individualOriginInConnection)&&(individualDestinationInConnection)){
            //if the origin and the destination belong to the single connection we do not perform genetic operation
            return null;
        }else{
            mutatedIndividual = internalMutation(individual);
            return mutatedIndividual;
        }
    }

    private void createClusterPairs(List<Individual> individualInfo) {
        for(Individual individual:individualInfo){
            for(Integer integer:individual.getClustersMap().keySet()){
                individual.getClustersMap().get(integer).setId(String.valueOf(individual.getId())
                        +"."+individual.getClustersMap().get(integer).getOrigin().getId()+"."
                        +individual.getClustersMap().get(integer).getDestination().getId());
                    workQueue.add(individual.getClustersMap().get(integer));
           }
        }
    }

    @Override
    public void evaluate(Population population) {
        sensorValues = new HashMap<>(OptimizationController.optimizationControllerData.sensorValues);
        if(population==null)
            superior=elitist;
        else
            superior = evaluate();
        if(deadline-System.currentTimeMillis()>0)
            OptimizationController.optimizationControllerData.elit.put(userID,superior.copy());
        elitist = superior;
    }

    private Individual evaluate(){
        evaluateIndividuals(sensorValues);
        if(elitist!=null){
            elitist.setFitnessMap(fitnessMap(elitist.getEdgePath(),sensorValues,increaseObjectives));
            elitist.setFitness(fitness(elitist.getEdgePath(),maxSensorValues,sensorValues,minSensorValues,increaseObjectives)
                                       /elitist.getEdgePath().size());
        }
        Collections.sort(population.getPopulation(), Comparator.comparing(Individual::getFitness));
        if(elitist!=null){
            if(elitist.getFitness()<population.getPopulation().get(0).getFitness())
                return elitist;
        }
        return population.getPopulation().get(0);
    }

    private Individual getBestIndividual(Population population) {
        return population.getPopulation().get(0);
    }
    
    private Individual mutationExecution(int interCluster, int chosenCluster, Individual individual){
        Individual mutant =null;
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        MutationTask mutationTask;
        try {
            mutationTask = new MutationTask(interCluster,chosenCluster,origin,destination,previousPath
                        ,increaseObjectives,graphSet,individual,createdPaths);
            Future<Individual> future = executorService.submit(mutationTask);
            mutant = future.get(AlgorithmSettings.mutationDeadline,TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            return null;
        }
        executorService.shutdown();
        mutationCount++;
        mutant.setId(-mutationCount);
        return mutant;
    }

    private Individual internalInterClusterMutation(Individual individual){
        Integer chosenCluster = origin.getCluster();
        return mutationExecution(-1,chosenCluster,individual);
    }

    private Individual internalMutation(Individual individual){
        List<Integer> clusters = getClusters(individual);
        Integer chosenCluster = chooseCluster(clusters);
        if(originInConnectionEdge==1)
            return mutationExecution(1,chosenCluster,individual);
        else
            return mutationExecution(0,chosenCluster,individual);
    }
    
    private List<Integer> getClusters(Individual individual) {

        List<Integer> clusters = new ArrayList<>();
        clusters.addAll(individual.getClustersMap().keySet());
        return clusters;
    }
    
    private Integer chooseCluster(List<Integer> betweenClusters) {
        Integer random = new Random().nextInt(betweenClusters.size());
        return betweenClusters.get(random);
    }

}
