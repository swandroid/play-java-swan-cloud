package optimization.algorithm.helperThreads;

import optimization.algorithm.algorithms.DirectedRandom;
import optimization.algortihmsConditions.AlgorithmSettings;
import optimization.core.population.Individual;
import optimization.core.population.Paths;
import optimization.hibernateModels.Edge;
import optimization.hibernateModels.Graph;
import optimization.hibernateModels.Vertex;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import static optimization.algorithm.gmooa.GenericMultiObjectiveAlgorithmBase.containEdge;

/**
 * Created by Maria Efthymiadou on Jun, 2019
 */

public class MutationTask extends ClusterGraphCreator implements Callable {
	
	private int originInConnectionEdge;
	private Integer clusterForMutation;
	private Individual individual;
	private ConcurrentHashMap<String, Paths> createdPaths;
	private HashMap<String, Boolean> increaseObjectives;
	private HashMap<Integer, Graph> graphSet;
	private Vertex origin;
	private Vertex destination;
	private List<Vertex> previousPath;

	public MutationTask(int originInConnectionEdge, Integer clusterForMutation, Vertex origin, Vertex destination
			, List<Vertex> previousPath, HashMap<String, Boolean> increaseObjectives, HashMap<Integer, Graph> graphSet
			, Individual individual, ConcurrentHashMap<String, Paths> createdPaths) {
		this.origin = origin;
		this.destination = destination;
		this.previousPath = previousPath;
		this.graphSet = graphSet;
		this.increaseObjectives = increaseObjectives;
		this.createdPaths = createdPaths;
		this.individual = individual;
		this.clusterForMutation = clusterForMutation;
		this.originInConnectionEdge = originInConnectionEdge;
	}
	
	@Override
	public Individual call(){
		if(originInConnectionEdge==-1)
			return mutationA();
		else return mutationB();
//		return mutationC();
	}
	
	private Individual mutationA(){
		Vertex vertex = null;
		boolean vertexEqual=true;
		while(vertexEqual){
			vertex = chooseRandomClusterVertex(graphSet.get(clusterForMutation).getVertexes());
			if(!vertex.getId().equals(origin.getId())&&(!vertex.getId().equals(destination.getId())))
				vertexEqual = false;
		}
		Paths pathsMutation1 = runDirectedCluster(graphSet.get(clusterForMutation),origin,vertex);

		Paths pathsMutation2 = runDirectedCluster(graphSet.get(clusterForMutation),vertex,destination);

		Individual mutated = new Individual();
		Paths paths = createPath(individual,pathsMutation1,pathsMutation2,-1,-1);
		mutated.setPath(paths.getVertexPath());
		mutated.setEdgePath(paths.getEdgePath());
		mutated.setChosenConnections(individual.getChosenConnections());
		createdPaths.put(String.valueOf(createdPaths.size()),paths);
		return mutated;
		
	}
	
	private Individual mutationB(){
		Vertex vertex = null;
		boolean isVertexEqual=true;
		while(isVertexEqual){
			vertex = chooseRandomClusterVertex(individual.getClustersMap().get(clusterForMutation).getGraph().getVertexes());
			if(!vertex.getId().equals(origin.getId())&&(!vertex.getId().equals(destination.getId())))
				isVertexEqual = false;
		}
		Paths pathsMutation1 = runDirectedCluster(individual.getClustersMap().get(clusterForMutation).getGraph(),
				individual.getClustersMap().get(clusterForMutation).getOrigin(),vertex);
		
		Paths pathsMutation2 = runDirectedCluster(individual.getClustersMap().get(clusterForMutation).getGraph(),
				vertex,individual.getClustersMap().get(clusterForMutation).getDestination());
		
		Individual mutated = new Individual();
		int originPosition = 0, destinationPosition=0;
		for(int i=0;i<individual.getPath().size();i++){
			if(individual.getPath().get(i).getId().equals(individual.getClustersMap().get(clusterForMutation).getOrigin().getId())){
				originPosition= i;
			}
			if(individual.getPath().get(i).getId().equals(individual.getClustersMap().get(clusterForMutation).getDestination().getId())){
				destinationPosition=i;
			}
		}
		
		Paths paths = createPath(individual,pathsMutation1,pathsMutation2,originPosition,destinationPosition);
		mutated.setPath(paths.getVertexPath());
		mutated.setEdgePath(paths.getEdgePath());
		mutated.setChosenConnections(individual.getChosenConnections());
		mutated.setClustersMap(createClusterGraphs(mutated.getChosenConnections(),originInConnectionEdge,origin,destination,graphSet));
		createdPaths.put(String.valueOf(createdPaths.size()),paths);
		
		return mutated;
	}
	
	
	private Individual mutationC(){
		Vertex vertex = null;
		boolean notFound = true;
		
		while(notFound){
			vertex = findNeighbor(previousPath);
			if(vertex!=null)
				notFound = false;
		}
		
		Paths pathsMutation1 = runDirectedCluster(graphSet.get(vertex.getCluster()),origin,vertex);
		
		Paths pathsMutation2 = runDirectedCluster(graphSet.get(vertex.getCluster()),vertex,destination);
		
		Individual mutated = new Individual();
		Paths paths = createPath(individual,pathsMutation1,pathsMutation2,-1,-1);
		
		mutated.setPath(paths.getVertexPath());
		mutated.setEdgePath(paths.getEdgePath());
		mutated.setChosenConnections(individual.getChosenConnections());
		mutated.setId(-2);
		createdPaths.put(String.valueOf(createdPaths.size()),paths);
		return mutated;
		
	}
	
	private Vertex findNeighbor(List<Vertex> previousPath) {
		boolean notFound = true;
		Vertex vertex = null;
		Edge edgeForMutation = null;
		while(notFound){
			edgeForMutation = individual.getEdgePath().get(new Random().nextInt(individual.getEdgePath().size()-2) + 1);
			if(!previousPath.contains(edgeForMutation.getOrigin().getId())){
				notFound = false;
			}
		}
		List<Vertex> list = new ArrayList<>();
		for(Edge edge:graphSet.get(vertex.getCluster()).getEdges()){
			if(edge.getDestination().getId().equals(edgeForMutation.getOrigin().getId())&&(edge.getCluster()!=0)) {
					list.add(edge.getOrigin());
			}
			if((edge.getOrigin().getId().equals(vertex.getId()))&&(edge.getCluster()!=0)){
					list.add(edge.getDestination());
			}
		}
		if(list.isEmpty())
			return null;
		else
			return list.get(new Random().nextInt(list.size()));
	}
	
	private Paths createPath(Individual individual, Paths paths1,
	                         Paths paths2, int originPosition, int destinationPosition) {
		List<Vertex> path = new ArrayList<>();
		List<Edge> edgePath = new ArrayList<>();
		if((originPosition==-1)&&(destinationPosition==-1)){
			path.addAll(paths1.getVertexPath().subList(0,paths1.getVertexPath().size()-1));
			path.addAll(paths2.getVertexPath());
			edgePath.addAll(paths1.getEdgePath());
			edgePath.addAll(paths2.getEdgePath());
		}else {
			for(int i=0;i<originPosition;i++){
				edgePath.add(individual.getEdgePath().get(i));
			}
			
			for(int i=0;i<paths1.getEdgePath().size();i++){
				edgePath.add(paths1.getEdgePath().get(i));
			}
			
			for(int i=0;i<paths2.getEdgePath().size();i++){
				edgePath.add(paths2.getEdgePath().get(i));
			}
			
			for(int i=destinationPosition;i<individual.getEdgePath().size();i++){
				edgePath.add(individual.getEdgePath().get(i));
			}
		}
		edgePath = checkForEdgeCycle(edgePath);
		for(Edge edge:edgePath){
			path.add(edge.getOrigin());
		}
		path.add(edgePath.get(edgePath.size()-1).getDestination());
		return new Paths(path,edgePath,0);
	}
	
	private List<Edge> checkForEdgeCycle(List<Edge> edgePath) {
		Iterator<Edge> edgePathIterator = edgePath.iterator();
		while (edgePathIterator.hasNext()) {
			List<Integer> list = containEdge(edgePathIterator.next(),edgePath);
			if(list.size()>1){
				edgePath=removeCycle(edgePath,list);
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
	
	private Paths runDirectedCluster(Graph graph, Vertex origin, Vertex destination) {
		DirectedRandom directedRandom = new DirectedRandom(graph, AlgorithmSettings.ANGLE,previousPath);
		directedRandom.execute(origin,destination);
		List<Vertex> path1 = new ArrayList<>();
		path1.addAll(directedRandom.getPath());
		List<Edge> edgePath = new ArrayList<>();
		edgePath.addAll(directedRandom.getEdgesPath(directedRandom.getPath(),graph.getEdges()));
		Paths paths = new Paths(path1,edgePath);
		paths.setPathID(origin.getId()+"."+destination.getId());
		return paths;
	}
	
	private Vertex chooseRandomClusterVertex(Set<Vertex> graphVertexes) {
		int random = new Random().nextInt(graphVertexes.size());
		int i=0;
		Vertex randomVertex = null;
		for(Vertex vertex:graphVertexes){
			if(i==random){
				randomVertex= vertex;
				break;
			}
			i++;
		}
		return randomVertex;
	}
}
