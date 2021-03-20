package optimization.algorithm.helperThreads;

import controllers.OptimizationController;
import optimization.core.population.Individual;
import optimization.hibernateModels.Edge;
import optimization.hibernateModels.Graph;
import optimization.hibernateModels.Vertex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;

/**
 * Created by Maria Efthymiadou on Jun, 2019
 */

public class RandomConnectionsRunnable extends ClusterGraphCreator implements Callable {

    private final Vertex destination;
    private final Vertex origin;
    private Individual individual;
    private int originInConnectionEdge;
    private int destinationInConnectionEdge;
    private HashMap<Integer,Graph> graphSet;
    private List<Vertex> previousPath;
    private HashMap<String,HashMap<Integer,Edge>> connectionEdges;
    
    public RandomConnectionsRunnable(Individual individual, Vertex origin, Vertex destination, HashMap<Integer, Graph> graphSet, List<Vertex> previousPath, HashMap<String,HashMap<Integer,Edge>> connectionEdges) {
        
            this.individual=individual;
        this.origin = origin;
        this.destination = destination;
        this.graphSet = graphSet;
        this.previousPath = previousPath;
        this.connectionEdges = connectionEdges;
        

    }
    
    private List<Edge> chooseRandomConnections(List<Vertex> path, HashMap<String, HashMap<Integer, Edge>> connectionEdges) {
        List<Edge> connectionEdgesLinkedList = new ArrayList<>();
        if(originInConnectionEdge==1){
            List<Edge> filteredRandomConnectionsOrigin = new ArrayList<>();
            for(Integer integer:connectionEdges.get(path.get(0) + "." + path.get(1)).keySet()){
                if(connectionEdges.get(path.get(0) + "." + path.get(1)).get(integer).getOrigin().getId().equals(origin.getId())){
                        filteredRandomConnectionsOrigin.add(connectionEdges.get(path.get(0) + "." + path.get(1)).get(integer));
                }
            }
            if(filteredRandomConnectionsOrigin.isEmpty()){
                int random = new Random().nextInt((connectionEdges.get(path.get(0)+"."+path.get(1))).size())+1;
                Edge edge = connectionEdges.get(path.get(0)+"."+path.get(1)).get(random);
                connectionEdgesLinkedList.add(edge);
                originInConnectionEdge=0;
            }
            else{
                connectionEdgesLinkedList.add(filteredRandomConnectionsOrigin.get(new Random().nextInt(filteredRandomConnectionsOrigin.size())));
            }
        }else{
            int random = new Random().nextInt((connectionEdges.get(path.get(0)+"."+path.get(1))).size())+1;
            Edge edge = connectionEdges.get(path.get(0)+"."+path.get(1)).get(random);
            connectionEdgesLinkedList.add(edge);
        }
        if(path.size()==2){
            return connectionEdgesLinkedList;
        }
        for(int i=1;i<path.size()-2;i++){
            int random = new Random().nextInt((connectionEdges.get(path.get(i)+"."+path.get(i+1))).size())+1;
            Edge edge = connectionEdges.get(path.get(i)+"."+path.get(i+1)).get(random);
            connectionEdgesLinkedList.add(edge);
        }
        if(destinationInConnectionEdge==1){
            List<Edge> filteredRandomConnectionsDestination = new ArrayList<>();
            for(Integer integer:connectionEdges.get(path.get(path.size()-2) + "." + path.get(path.size()-1)).keySet()){
                if(connectionEdges.get(path.get(path.size()-2) + "." + path.get(path.size()-1)).get(integer).getDestination().getId().equals(destination.getId())){
                    filteredRandomConnectionsDestination.add(connectionEdges.get(path.get(0) + "." + path.get(1)).get(integer));
                }
            }
            if(filteredRandomConnectionsDestination.isEmpty()){
                int random = new Random().nextInt((connectionEdges.get(path.get(path.size()-2)+"."+path.get(path.size()-1))).size())+1;
                Edge edge = connectionEdges.get(path.get(path.size()-2)+"."+path.get(path.size()-1)).get(random);
                connectionEdgesLinkedList.add(edge);
                destinationInConnectionEdge=0;
            }else{
                connectionEdgesLinkedList.add(filteredRandomConnectionsDestination.get(new Random().nextInt(filteredRandomConnectionsDestination.size())));
            }
        }else{
            int random = new Random().nextInt((connectionEdges.get(path.get(path.size()-2)+"."+path.get(path.size()-1))).size())+1;
            Edge edge = connectionEdges.get(path.get(path.size()-2)+"."+path.get(path.size()-1)).get(random);
            connectionEdgesLinkedList.add(edge);

        }
        return connectionEdgesLinkedList;
    }
    
    private HashMap<String,HashMap<Integer,Edge>> removeConnectionsInPath(HashMap<String, HashMap<Integer, Edge>> connectionEdges, List<Vertex> previousPath) {
        HashMap<String, HashMap<Integer, Edge>> newConnectionEdges = new HashMap<>();
        int i=0;
        for(Vertex vertex:previousPath)
            System.out.println("previouspath: vertex "+vertex.getId());
        for(String string:connectionEdges.keySet()){
            HashMap<Integer,Edge> internalMap = new HashMap<>();
            for(Integer integer:connectionEdges.get(string).keySet()){
                if(!previousPath.contains(connectionEdges.get(string).get(integer).getOrigin())&&(!previousPath.contains(connectionEdges.get(string).get(integer).getDestination()))){
                    internalMap.put(i,connectionEdges.get(string).get(integer));
//                    System.out.println("getOrigin().getId() "+connectionEdges.get(string).get(integer).getOrigin().getId()+" getDestination.getId() "+connectionEdges.get(string).get(integer).getDestination().getId());
                    i++;
                }else
                    System.out.println("IT CONTAINS IS getOrigin().getId() "+connectionEdges.get(string).get(integer).getOrigin().getId()+" getDestination.getId() "+connectionEdges.get(string).get(integer).getDestination().getId());
    
            }
            i=0;
            newConnectionEdges.put(string,internalMap);
        }
        //return newConnectionEdges;
        return connectionEdges;
    }
    
    @Override
    public Individual call(){
        List<Edge> randomConnectionsList;
        randomConnectionsList = chooseRandomConnections(individual.getSkeleton(), OptimizationController.optimizationControllerData.connectionsEdgeHashMap);
        individual.setChosenConnections(randomConnectionsList);
        individual.setClustersMap(createClusterGraphs(randomConnectionsList,originInConnectionEdge,origin,destination,graphSet));
        return individual;
    }
    
    
    
}
