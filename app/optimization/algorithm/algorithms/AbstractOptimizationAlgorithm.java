package optimization.algorithm.algorithms;

import optimization.core.fitness.Fitness;
import optimization.hibernateModels.Edge;
import optimization.hibernateModels.Vertex;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created by Maria Efthymiadou on Jun, 2019
 */
public abstract class AbstractOptimizationAlgorithm extends Fitness {
	
	protected Set<Edge> removeEdge(Set<Edge> removedEdges, Set<Edge> edges, Vertex next, Vertex last){
		Iterator<Edge> edge = edges.iterator();
		while (edge.hasNext()) {
			Edge e = edge.next();
			if (((e.getDestination().getId().equals(last.getId())) && (e.getOrigin().getId().equals(next.getId())))
					    ||((e.getDestination().getId().equals(next.getId())) && (e.getOrigin().getId().equals(last.getId())))) {
				edge.remove();
				removedEdges.add(e);
			}
		}
		return removedEdges;
	}
	
	protected Vertex chooseRandom(List<Vertex> randomList) {
        if(randomList.size() == 1)
            return randomList.get(0);
        else {
            java.util.Random rand = new java.util.Random();
            int position = rand.nextInt((randomList.size()));
            return  randomList.get(position);
        }
    }
	
	public LinkedList<Edge> getEdgesPath(List<Vertex> path, Set<Edge> edges) {
		LinkedList<Edge> edgePath = new LinkedList<>();
		Edge edge;
		if(path != null) {
			for (int i = 0; i<path.size()-1; i++) {
				edge = findEdge(path.get(i), path.get(i + 1),edges);
				edgePath.add(edge);
			}
		}
		return edgePath;
	}
	
	private Edge findEdge(Vertex vertex, Vertex vertex1, Set<Edge> edges) {
        for(Edge edge:edges){
            if(edge.getOrigin().getId().equals(vertex.getId())&&(edge.getDestination().getId().equals(vertex1.getId()))){
                return edge;
            }
        }
        return null;
    }
}
