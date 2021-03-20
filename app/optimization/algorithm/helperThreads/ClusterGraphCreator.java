package optimization.algorithm.helperThreads;

import optimization.algorithm.ClusterGraph;
import optimization.hibernateModels.Edge;
import optimization.hibernateModels.Graph;
import optimization.hibernateModels.Vertex;

import java.util.HashMap;
import java.util.List;


/**
 * Created by Maria Efthymiadou on Jun, 2019
 */


public class ClusterGraphCreator {
	protected HashMap<Integer, ClusterGraph> createClusterGraphs(List<Edge> randomConnectionsList, int originInConnectionEdge
			, Vertex origin, Vertex destination, HashMap<Integer, Graph> graphSet) {
		List<Edge> individualrandomlist = randomConnectionsList;
		int k=0;
		for(Edge edge:individualrandomlist){
			k++;
			if(k==individualrandomlist.size()-1){
				k=0;
			}
		}
		
		HashMap<Integer,ClusterGraph> maps=new HashMap<>();
		int m=1;
		if(originInConnectionEdge==0) {
			ClusterGraph clusterGraphOrigin =new ClusterGraph(origin.getCluster(),graphSet.get(origin.getCluster()),origin);
			clusterGraphOrigin.setDestination(individualrandomlist.get(0).getOrigin());
			maps.put(1,clusterGraphOrigin);
			m=2;
		}
		for(int i=0;i<individualrandomlist.size()-1;i++){
			if(!individualrandomlist.get(i).getDestination().getId().equals(individualrandomlist.get(i+1).getOrigin().getId())){
				ClusterGraph clusterGraph = new ClusterGraph();
				clusterGraph.setCluster(individualrandomlist.get(i).getDestination().getCluster());
				clusterGraph.setOrigin(individualrandomlist.get(i).getDestination());
				clusterGraph.setDestination(individualrandomlist.get(i+1).getOrigin());
				clusterGraph.setGraph(graphSet.get(individualrandomlist.get(i).getDestination().getCluster()));
				maps.put(i+m,clusterGraph);
			}
		}
		if(!destination.getId().equals(individualrandomlist.get(individualrandomlist.size()-1).getDestination().getId())){
			ClusterGraph clusterGraphDestination =new ClusterGraph(destination.getCluster(),graphSet.get(destination.getCluster()));
			clusterGraphDestination.setOrigin(individualrandomlist.get(individualrandomlist.size()-1).getDestination());
			clusterGraphDestination.setDestination(destination);
			maps.put(maps.size()+1,clusterGraphDestination);
		}
		return maps;
	}
	
}
