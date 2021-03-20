package optimization;

import optimization.hibernateModels.Edge;
import optimization.hibernateModels.Graph;
import optimization.hibernateModels.Vertex;

import java.util.*;

/**
 * Created by Maria Efthymiadou on Jun, 2019
 */

public class DijkstraAlgorithm {
	private final List<Vertex> nodes;
	private final List<Edge> edges;
	private Set<Vertex> settledNodes;
	private Set<Vertex> unSettledNodes;
	private Map<Vertex, Vertex> predecessors;
	private Map<Vertex, Double> distance;
	
	public DijkstraAlgorithm(Graph graph, String objective) {
		this.nodes = new ArrayList<Vertex>(graph.getVertexes());
		this.edges = new ArrayList<Edge>(graph.getEdges());
	}
	
	public void execute(Vertex source) {
		settledNodes = new HashSet<Vertex>();
		unSettledNodes = new HashSet<Vertex>();
		distance = new HashMap<Vertex, Double>();
		predecessors = new HashMap<Vertex, Vertex>();
		distance.put(source, (double)0);
		unSettledNodes.add(source);
		while (unSettledNodes.size() > 0) {
			Vertex node = getMinimum(unSettledNodes);
			settledNodes.add(node);
			unSettledNodes.remove(node);
			findMinimalDistances(node);
		}
	}
	
	private void findMinimalDistances(Vertex node) {
		List<Vertex> adjacentNodes = getNeighbors(node);
		for (Vertex target : adjacentNodes) {
			if (getShortestDistance(target) > getShortestDistance(node)+ getDistance(node, target)) {
				distance.put(target, getShortestDistance(node) + getDistance(node, target));
				predecessors.put(target, node);
				unSettledNodes.add(target);
			}
		}
	}
	
	private double getDistance(Vertex node, Vertex target) {
		for (Edge edge : edges) {
			if (edge.getOrigin().equals(node)
					    && edge.getDestination().equals(target)) {
					double distance = calculateDistance(edge);
					return distance;
			}
		}
		throw new RuntimeException("Should not happen");
	}
	
	private double calculateDistance(Edge edge) {
		double distance;
		distance = edge.getOrigin().getCoordinate().distance(edge.getDestination().getCoordinate());
		
		return distance;
	}
	
	private List<Vertex> getNeighbors(Vertex node) {
		List<Vertex> neighbors = new ArrayList<Vertex>();
		for (Edge edge : edges) {
			if (edge.getOrigin().equals(node)
					    && !isSettled(edge.getDestination())) {
				neighbors.add(edge.getDestination());
			}
		}
		return neighbors;
	}
	
	private Vertex getMinimum(Set<Vertex> vertexes) {
		Vertex minimum = null;
		for (Vertex vertex : vertexes) {
			if (minimum == null) {
				minimum = vertex;
			} else {
				if (getShortestDistance(vertex) < getShortestDistance(minimum)) {
					minimum = vertex;
				}
			}
		}
		return minimum;
	}
	
	private boolean isSettled(Vertex vertex) {
		boolean settled = settledNodes.contains(vertex);
		return settled;
	}
	
	private Double getShortestDistance(Vertex destination) {
		Double d = distance.get(destination);
		if (d == null) {
			return Double.MAX_VALUE;
		} else {
			return d;
		}
	}
	
	public LinkedList<Vertex> getPath(Vertex target) {
		LinkedList<Vertex> path = new LinkedList<Vertex>();
		Vertex step = target;
		// check if a path exists
		if (predecessors.get(step) == null) {
			return null;
		}
		path.add(step);
		while (predecessors.get(step) != null) {
			step = predecessors.get(step);
			path.add(step);
		}
		// Put it into the correct order
		Collections.reverse(path);
		if(path==null)
		{
			System.out.println("DIJKSTRA PATH IS NULL: ");
		}
		
		return path;
	}
	
	public LinkedList<Edge> getEdgesPath(LinkedList<Vertex> path) {
		LinkedList<Edge> edgePath = new LinkedList<Edge>();
		Edge edge;
		if(path!=null) {
			for (int i=0; i<path.size()-1; i++) {
				edge = findEdge(path.get(i), path.get(i + 1));
				if(edge==null)
					System.out.print("NULL");
				edgePath.add(edge);
			}
			
		}
		return edgePath;
	}
	
	private Edge findEdge(Vertex vertex, Vertex vertex1) {
		for(Edge edge:this.edges){
			if(edge.getOrigin().getId().equals(vertex.getId())&&(edge.getDestination().getId().equals(vertex1.getId()))){
				return edge;
			}
		}
		return null;
	}
	
}
