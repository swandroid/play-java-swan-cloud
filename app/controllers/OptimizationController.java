package controllers;


import com.fasterxml.jackson.databind.JsonNode;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import engine.ExpressionManager;
import interdroid.swancore.swansong.ExpressionFactory;
import interdroid.swancore.swansong.ExpressionParseException;
import optimization.ClusterCreator;
import optimization.OptimizationControllerData;
import optimization.SensorValuesModifier;
import optimization.algorithm.UserOptimizationData;
import optimization.algorithm.helperThreads.OptimizationThread;
import optimization.algortihmsConditions.AlgorithmSettings;
import optimization.algortihmsConditions.PopulationSizeCalculator;
import optimization.core.population.Individual;
import optimization.hibernateModels.Cluster;
import optimization.hibernateModels.Edge;
import optimization.hibernateModels.Graph;
import optimization.hibernateModels.Vertex;
import optimization.hibernateModels.hibernateUtils.HibernateUtil;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import play.api.Play;
import play.libs.ws.WSClient;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Maria Efthymiadou on 15/01/19.
 */
public class OptimizationController extends Controller{


    @Inject
    WSClient ws;
    
    public static OptimizationControllerData optimizationControllerData;
    
    public Result startUserNavigation() throws ExpressionParseException {
        JsonNode json = request().body().asJson();
        startNavigation(json);
        return ok();
    }
    
    public void startNavigation(JsonNode json) throws ExpressionParseException {
        HashMap<String,String> userData = ExpressionFactory.parseOptimizationJsonExpression(json);
        if(!optimizationControllerData.active.containsKey(userData.get("id"))){
            ws = Play.current().injector().instanceOf(WSClient.class);
            
            optimizationControllerData.activeUsersWs.put(userData.get("id"),ws);
            optimizationControllerData.userEvaluationRoute.put(userData.get("id"),new ArrayList<>());
            optimizationControllerData.userNewData.put(userData.get("id"),true);
            HashMap<String,Boolean> sensors= getSensors(userData.get("sensors"));
            Coordinate coordinate = new Coordinate(Double.parseDouble(userData.get("originLatitude")),Double.parseDouble(userData.get("originLongitude")));
            Coordinate coordinateD = new Coordinate(Double.parseDouble(userData.get("destinationLatitude")),Double.parseDouble(userData.get("destinationLongitude")));
            Vertex origin = findVertexByCoordinate(coordinate);
            Vertex destination = findVertexByCoordinate(coordinateD);
            List<Vertex> userRouteList = new ArrayList<>();
            userRouteList.add(origin);
            optimizationControllerData.userRoute.put(userData.get("id"),userRouteList);
            optimizationControllerData.active.put(userData.get("id"),origin);
            optimizationControllerData.activeUsersWs.put(userData.get("id"),ws);
            UserOptimizationData newUser = new UserOptimizationData(origin,destination,null,sensors,Long.parseLong(userData.get("deadline")),userData.get("token"),userData.get("id"));
            Graph graph = createUserSkeleton(origin,destination);
            newUser.setUserSkeletonGraph(graph);
            System.out.println("The user location is "+origin.getId()+ " with coordinates: "+origin.getCoordinate().toString());
            optimizationControllerData.activeUsersData.put(userData.get("id"),newUser);
            String convertedExpression = convertExpression("cloud@optimization:result");
            Runnable r = new OptimizationThread(userData.get("id"),convertedExpression);
            Thread thread = new Thread(r);
            optimizationControllerData.activeUsersThreads.put(userData.get("id"),thread);
            thread.start();
        }
    }

    public Result updateUserInfoNavigation(){
        JsonNode json = request().body().asJson();
        updateUserData(json);
        return ok();
    }
    
    private static Individual adaptElitist(Vertex origin, Individual elitist) {
        int position=-1;
        for(int i=0;i<elitist.getPath().size();i++){
            if(elitist.getPath().get(i).getId().equals(origin.getId())){
                position=i;
                break;
            }
        }
         if(position != -1) {
            List<Vertex> vertexList = new ArrayList<>();
            for(int i=position;i<elitist.getPath().size();i++)
                vertexList.add(elitist.getPath().get(i));
            List<Edge> edgeList = new ArrayList<>();
        
            for(int i=position;i<elitist.getEdgePath().size();i++)
                edgeList.add(elitist.getEdgePath().get(i));
             elitist.setPath(vertexList);
             elitist.setEdgePath(edgeList);
             elitist.setOrigin(origin);
        }
        return elitist;
    }
    
    public static void updateUserData(JsonNode json){
    
        HashMap<String,String> jsonData = ExpressionFactory.parseOptimizationJsonUpdateExpression(json);
        Coordinate coordinate = new Coordinate(Double.parseDouble(jsonData.get("originLatitude")),Double.parseDouble(jsonData.get("originLongitude")));
        Vertex origin = findVertexByCoordinate(coordinate);
        UserOptimizationData user = OptimizationController.optimizationControllerData.activeUsersData.get(jsonData.get("id"));
        if(!origin.getId().equals(optimizationControllerData.active.get(jsonData.get("id")).getId())){
            System.out.println(" Updated user origin "+origin.getId());
            if(user.getOrigin().getCluster()!=origin.getCluster()){
                user.setUserSkeletonGraph(createUserSkeleton(origin,user.getDestination()));
            }
            user.setOrigin(origin);
            System.out.println("The time until the user reaches the next intersection is "+json.get("deadline"));
            user.setOptimizationTime(Long.parseLong(jsonData.get("deadline"))+System.currentTimeMillis());
            //todo add method in case the user does not follow the path. The addresses this case
            Individual elitist = user.getElitist().copy();
            elitist = adaptElitist(origin,elitist);
            if(elitist!=null){
                OptimizationController.optimizationControllerData.activeUsersData.get(jsonData.get("id")).setElitist(elitist);
                OptimizationController.optimizationControllerData.elit.put(jsonData.get("id"),elitist);
            }
            optimizationControllerData.userRoute.get(jsonData.get("id")).add(origin);
            user.setElitist(OptimizationController.optimizationControllerData.activeUsersData.get(jsonData.get("id")).getElitist());
            user.setElitist(elitist);
            if((isUserTimeSufficient(user.getOptimizationTime(),user.getUserSkeletonGraph().getVertexes().size()))&&(elitist!=null)){
                OptimizationController.optimizationControllerData.activeUsersData.put(jsonData.get("id"),user);
                OptimizationController.optimizationControllerData.userNewData.put(jsonData.get("id"),true);
                return;
            }
            OptimizationController.optimizationControllerData.activeUsersData.get(jsonData.get("id")).setElitist(elitist);
            OptimizationController.optimizationControllerData.elit.put(jsonData.get("id"),elitist);
            System.out.println("Not enough time to produce a new path.");
            return;
        }
    }
    
    //the method controls if there is efficient time to produce new solutions.
    public static boolean isUserTimeSufficient(long userTime,int size){
        PopulationSizeCalculator populationSizeCalculator = new PopulationSizeCalculator();
        if(userTime>=(populationSizeCalculator.retrieveSingleIndividualCreationTimeDRA(size)+ AlgorithmSettings.timeThreshold)){
            return true;
        }
        return false;
    }

    public Result terminateUserNavigation(){
        JsonNode json = request().body().asJson();
        terminate(json);
        return ok();
    }
    
    public static void terminate(JsonNode json) {
        System.out.println("Terminate the navigation with id  "+json.toString());
        String id = json.findPath("id").asText();
        ExpressionManager.unregisterExpression(id);
        if(optimizationControllerData.activeUsersThreads.containsKey(id)){
            optimizationControllerData.activeUsersThreads.get(id).interrupt();
            optimizationControllerData.activeUsersThreads.remove(id);
        }
        optimizationControllerData.active.remove(id);
        optimizationControllerData.userEvaluationRoute.remove(id);
        optimizationControllerData.activeUsersData.remove(id);
        optimizationControllerData.activeUsersWs.remove(id);
        optimizationControllerData.userRoute.remove(id);
        optimizationControllerData.userNewData.remove(id);

    }
    
    public static HashMap<String, Boolean> getSensors(String sensorsString){
        HashMap<String,Boolean> increaseObjectives = new HashMap<>();
        if(sensorsString!=null){
            List<String> sensors = Arrays.asList(sensorsString.split("\\+"));
            for(String string:sensors){
                if(string.split("/")[1].equals(true)){
                    increaseObjectives.put(string.split("/")[0],true);
                }else {
                    increaseObjectives.put(string.split("/")[0],false);
                }
            }
            return increaseObjectives;
        }
        System.out.println("Sensor Types are null");
        return null;
    }
    
    
    public static Vertex findVertexByCoordinate(Coordinate coordinate){
        Vertex vertex=null;
        int distance = Integer.MAX_VALUE;
        for(Vertex v: optimizationControllerData.DublinCityGraph.getVertexes()){
            if((HaversineInM(coordinate.x,coordinate.y,v.getCoordinate().x,v.getCoordinate().y))<distance){
                distance=HaversineInM(coordinate.x,coordinate.y,v.getCoordinate().x,v.getCoordinate().y);
                vertex = v;
            }
        }
        return vertex;
    }
    
    public static int HaversineInM(double lat1, double long1, double lat2, double long2) {
        return (int) (1000D * HaversineInKM(lat1, long1, long2,lat2));
    }
    
    public static double HaversineInKM(double lat1, double long1, double lat2, double long2) {
        double dlong = (long2 - long1) * AlgorithmSettings._d2r;
        double dlat = (lat2 - lat1) * AlgorithmSettings._d2r;
        double a = Math.pow(Math.sin(dlat / 2D), 2D) + Math.cos(lat1 * AlgorithmSettings._d2r)
                                                               * Math.cos(lat2 * AlgorithmSettings._d2r)
                                                               * Math.pow(Math.sin(dlong / 2D), 2D);
        double c = 2D * Math.atan2(Math.sqrt(a), Math.sqrt(1D - a));
        double d = AlgorithmSettings._eQuatorialEarthRadius * c;
        
        return d;
    }
    
    public static String convertExpression(String expression) {
        String convertedExpression = null;
        String strippedExpression = expression.replaceAll("[\\[\\]()]","");
        if (strippedExpression.startsWith("cloud")) {
            convertedExpression = expression.replace("cloud", "self");
        }
        else if(strippedExpression.contains("http")){
            String[] split_expression = expression.split("@",2);
            if(split_expression.length>1){
                convertedExpression = "self@"+ split_expression[1];
            }
        }
        return convertedExpression;
    }
    
    public static Graph createUserSkeleton(Vertex origin, Vertex destination){
        
        List<Coordinate> coordinates = cornerCoordinates(origin.getCoordinate(),destination.getCoordinate());
        Graph userGraph = new Graph();
        Set<String> vertexes = new HashSet<>();
        for(Integer cluster: OptimizationController.optimizationControllerData.clusterMap.keySet()){
            if(clusterRelatedToUser(OptimizationController.optimizationControllerData.clusterMap.get(cluster),coordinates)){
                vertexes.add(String.valueOf(cluster));
            }
        }
        if(!vertexes.contains(String.valueOf(origin.getCluster()))){
            vertexes.add(String.valueOf(origin.getCluster()));
        }
        if(!vertexes.contains(String.valueOf(destination.getCluster()))){
            vertexes.add(String.valueOf(destination.getCluster()));
        }
        for(String vertex:vertexes){
            userGraph.addVertex(findVertexFromKeletonGraph(vertex));
        }
        System.out.println(" user skeleton graph ");
        for(Vertex vertex:userGraph.getVertexes()){
            System.out.print(" "+vertex.getId());
        }
        
        for(String vertex:vertexes){
            for(String vertex1:vertexes){
                if(!vertex.equals(vertex1)){
                    Edge skeletonEdge = skeletonEdge(vertex,vertex1);
                    if(skeletonEdge!=null){
                        userGraph.getEdges().add(skeletonEdge);
                    }
                }
            }
        }
        return userGraph;
    }
    
    public static HashMap<Integer,Graph> getLocalGraphSet() {
        HashMap<Integer,Graph> graphs = new HashMap<>();
        List<Integer> clusters = numberOfClusters();
        System.out.println("  number of clusters: "+clusters.toString());
        int i;
        for(i=0;i<clusters.size();i++){
            if(clusters.get(i)!=null) {
                Set<Edge> edgeSet = getLocalClusterEdges(clusters.get(i));
                Set<Vertex> vertexSet = getLocalClusterVertexes(clusters.get(i));
                Graph graph = new Graph(String.valueOf(clusters.get(i)), vertexSet, edgeSet);
                graphs.put(clusters.get(i),graph);
            }
        }
        return graphs;
    }
    
    
    /**
     * these 2 methods to change to create the sub-graphs
     * **/
    private static Set<Vertex> getLocalClusterVertexes(Integer cluster) {
        Set<Vertex> vertexSet = new HashSet<>();
        for(Vertex vertex: optimizationControllerData.DublinCityGraph.getVertexes()){
            if(vertex.getCluster()==cluster)
                vertexSet.add(vertex);
        }
        return vertexSet;
    }
    
    public static Set<Edge> getLocalClusterEdges(Integer edgeCluster) {
        Set<Edge> edgeSet = new HashSet<>();
        for(Edge edge: optimizationControllerData.DublinCityGraph.getEdges()){
            if(edge.getCluster()==edgeCluster)
                edgeSet.add(edge);
        }
        return edgeSet;
    }
    
    private static Edge skeletonEdge(String vertex, String vertex1) {
        for(Edge edge: optimizationControllerData.skeleton.getEdges()){
            if(edge.getEdge_id().equals(vertex+"."+vertex1)){
                return edge;
            }
        }
        System.out.println(" Could not find the edge with origin "+vertex+ " and destination "+vertex1);
        return null;
    }
    
    
    private static Vertex findVertexFromKeletonGraph(String vertexId) {
        for(Vertex vertex: OptimizationController.optimizationControllerData.skeleton.getVertexes()){
            if(vertex.getId().equals(vertexId))
                return vertex;
        }
        System.out.println(" Could not find the vertex with id "+vertexId);
        return null;
    }
    
    private static boolean clusterRelatedToUser(Cluster cluster, List<Coordinate> coordinates) {
        if(isInBox(coordinates.get(0).x,coordinates.get(1).x,coordinates.get(0).y,coordinates.get(1).y,cluster.getEast())){
            return true;
        }if(isInBox(coordinates.get(0).x,coordinates.get(1).x,coordinates.get(0).y,coordinates.get(1).y,cluster.getNorth())){
            return true;
        }
        if(isInBox(coordinates.get(0).x,coordinates.get(1).x,coordinates.get(0).y,coordinates.get(1).y,cluster.getSouth())){
            return true;
        }
        if(isInBox(coordinates.get(0).x,coordinates.get(1).x,coordinates.get(0).y,coordinates.get(1).y,cluster.getWest())){
            return true;
        }
        return false;
    }
    
    
    public static boolean isInBox(double minX, double maxX, double minY, double maxY, Coordinate p){
        return minX <= p.x && p.x <= maxX && minY <= p.y && p.y <= maxY;
    }
    
    /**initialize the sensor values (from file) in a hashmap and the modifier to alter the values */
    public void initializeSensorValues(){
        optimizationControllerData.maxSensorValues.put("air",371.0);
        optimizationControllerData.maxSensorValues.put("sound",56.0);
        optimizationControllerData.minSensorValues.put("air",1.0);
        optimizationControllerData.minSensorValues.put("sound",48.0);
        findMaxMinEdgeDistance();
//        OptimizationController.sensorValues = createStaticValues();
        OptimizationController.optimizationControllerData.dynamicSensorValues = createDynamicValues();
        optimizationControllerData.sensorValuesModifier = new SensorValuesModifier();
    }
    
    private void findMaxMinEdgeDistance() {
        double minDistance = Double.MAX_VALUE;
        double maxDistance = Double.MIN_VALUE;
        double averageDistance = 0;
        for(Edge edge: optimizationControllerData.DublinCityGraph.getEdges()){
            if((edge.getDistance()<minDistance)&&(edge.getDistance()>0))
                minDistance=edge.getDistance();
            if(edge.getDistance()>maxDistance)
                maxDistance=edge.getDistance();
            
            averageDistance+=edge.getDistance();
            
        }
        optimizationControllerData.maxSensorValues.put("distance",maxDistance);
        optimizationControllerData.minSensorValues.put("distance",minDistance);
        optimizationControllerData.historicalObjectiveValues.put("distance",averageDistance/ optimizationControllerData.DublinCityGraph.getEdges().size());
        System.out.println("max Sensor Distance : "+ BigDecimal.valueOf(maxDistance));
        System.out.println(" min Sensor distance: "+ BigDecimal.valueOf(minDistance));
        
    }

    private ConcurrentHashMap<String,Double> createStaticValues(){
        ConcurrentHashMap<String,Double> airData = createValuesFromStaticFile(AlgorithmSettings.airSensorValuesFile,"air");
        ConcurrentHashMap<String,Double> soundData = createValuesFromStaticFile(AlgorithmSettings.soundSensorValuesFile,"sound");
        airData.putAll(soundData);
        return airData;
    }

    private ConcurrentHashMap<String,List<Double>> createDynamicValues(){
        ConcurrentHashMap<String,List<Double>> airData = createValuesFromDynamicFile(AlgorithmSettings.airSensorValuesFile,"air");
        ConcurrentHashMap<String,List<Double>> soundData = createValuesFromDynamicFile(AlgorithmSettings.soundSensorValuesFile,"sound");
        airData.putAll(soundData);
        ConcurrentHashMap<String,Double> values = new ConcurrentHashMap<>();
        for(String string: airData.keySet()){
            values.put(string,airData.get(string).get(0));
        }
        for(String string: soundData.keySet()){
            values.put(string,airData.get(string).get(0));
        }
        OptimizationController.optimizationControllerData.sensorValues=values;
        return airData;
    }

    private ConcurrentHashMap<String,List<Double>> createValuesFromDynamicFile(String fileName,String objective){
        File file = new File(fileName);
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";
        List<Double> values;
            ConcurrentHashMap<String,List<Double>> sensorValues = new ConcurrentHashMap<>();
            try {
                br = new BufferedReader(new FileReader(file));
                while ((line = br.readLine()) != null) {
                    String[] sensorData = line.split(cvsSplitBy);
                    values = new ArrayList<>();
                    values.add(Double.parseDouble(sensorData[1]));
                    values.add(Double.parseDouble(sensorData[2]));
                    values.add(Double.parseDouble(sensorData[3]));
                    values.add(Double.parseDouble(sensorData[4]));
                    String osmid = sensorData[0].split("\\.")[0];
                    sensorValues.put(osmid+objective,values);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return sensorValues;

    }

    private ConcurrentHashMap<String,Double> createValuesFromStaticFile(String fileName,String objective){
        File file = new File(fileName);
        String line = "";
        String cvsSplitBy = ",";
        ConcurrentHashMap<String,Double> sensorValues = new ConcurrentHashMap<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            while ((line = br.readLine()) != null) {
                String[] sensorData = line.split(cvsSplitBy);
                sensorValues.put(sensorData[0]+objective,Double.parseDouble(sensorData[1]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sensorValues;
    }
    
    private static List<Coordinate> cornerCoordinates(Coordinate origin, Coordinate destination) {
        double minX,minY,maxX,maxY;
        if(origin.x<=destination.x) {
            if (origin.y <= destination.y) {
                minX = origin.x;
                minY = origin.y;
                maxX = destination.x;
                maxY= destination.y;
            } else {
                minX = origin.x;
                maxY = origin.y;
                maxX = destination.x;
                minY = destination.y;
            }

        }else {
            if (origin.y <= destination.y){
                minX = destination.x;
                maxY = destination.y;
                maxX = origin.x;
                minY = origin.y;

            }
            else {
                minX = destination.x;
                maxY = origin.y;
                maxX = origin.x;
                minY = destination.y;
            }
        }
        return Arrays.asList(new Coordinate(minX,minY),new Coordinate(maxX,maxY));
    }


    private static HashMap<Integer,Cluster> createClusterMap() {
        List<Cluster> clusters = getClusters();
        HashMap<Integer,Cluster> clusterMap = new HashMap<>();
        for(Cluster cluster:clusters){
            clusterMap.put(Integer.parseInt(cluster.getCluster_id()),cluster);
            System.out.println("clusterMap cluster "+clusterMap.get(Integer.parseInt(cluster.getCluster_id())).getCluster_id());
        }
        return clusterMap;
    }
    
    private static HashMap<Integer,List<String>> getClusterSensors() {
        List<String> sensorsMap;
        HashMap<Integer,List<String>> clusterSensorsMap = new HashMap<>();
        for(Integer integer: optimizationControllerData.graphMap.keySet()){
            sensorsMap = new ArrayList<>();
            for(Edge edge: optimizationControllerData.graphMap.get(integer).getEdges()){
                sensorsMap.add(edge.getEdge_id());
            }
            clusterSensorsMap.put(integer,sensorsMap);
        }
        sensorsMap =  new ArrayList<>();
        for(Edge edge:getClusterConnectionEdges()){
            sensorsMap.add(edge.getEdge_id());
        }
        clusterSensorsMap.put(0,sensorsMap);
        return clusterSensorsMap;
    }

    
    public Result init(){
        
        optimizationControllerData =  new OptimizationControllerData();
        optimizationControllerData.clusterConnections= getClusterConnectionEdges();
        optimizationControllerData.graphMap=getLocalGraphSet();
        optimizationControllerData.connectionsEdgeHashMap = getConnectionEdges();
        optimizationControllerData.clusterSensors  = getClusterSensors();
        optimizationControllerData.skeleton = createSkeleton();
        optimizationControllerData.clusterMap = createClusterMap();
        initUserMinMaxValues();
        initializeSensorValues();
        optimizationControllerData.userRoute = new ConcurrentHashMap<>();
        optimizationControllerData.timer.schedule(optimizationControllerData.sensorValuesModifier, new Date(), AlgorithmSettings.time);

        return ok("");
    }

    
    private void initUserMinMaxValues() {
        optimizationControllerData.maxSensorValues.put("distance",Double.MIN_VALUE);
        optimizationControllerData.maxSensorValues.put("sound",Double.MIN_VALUE);
        optimizationControllerData.maxSensorValues.put("air",Double.MIN_VALUE);
        optimizationControllerData.minSensorValues.put("distance",Double.MAX_VALUE);
        optimizationControllerData.minSensorValues.put("sound",Double.MAX_VALUE);
        optimizationControllerData.minSensorValues.put("air",Double.MAX_VALUE);
    }
    
    public static Graph createSkeleton() {
        Graph graph = new Graph();
        Set<Edge> graphEdges = new HashSet<>();
        Set<Vertex> graphVertexes = new HashSet<>();
        Set<Edge> connectionEdges = OptimizationController.optimizationControllerData.clusterConnections;
//        for(Edge edge1:connectionEdges){
//            System.out.println("edge "+edge1.getEdge_id()+ " origin "+edge1.getOrigin().getId()+ " destination "+edge1.getDestination().getId()
//                                       + " "+edge1.getOrigin().getCluster()+" "+edge1.getDestination().getCluster());
//        }
        for(Edge edge:connectionEdges){
            if(graphEdges!=null){
                if(!containsId(graphEdges,edge.getOrigin().getCluster()+"."+edge.getDestination().getCluster())){
                    Vertex v1 = new Vertex(edge.getOrigin().getCluster().toString());
                    Vertex v2 = new Vertex(edge.getDestination().getCluster().toString());
                    graphEdges.add(new Edge(edge.getOrigin().getCluster().toString()+"."+
                                                    edge.getDestination().getCluster().toString(),
                            v1, v2,1));
                    graphVertexes.add(v1);
                    graphVertexes.add(v2);
                    
                }
            }else {
                Vertex v1 = new Vertex(edge.getOrigin().getCluster().toString());
                Vertex v2 = new Vertex(edge.getDestination().getCluster().toString());
                graphEdges.add(new Edge(edge.getOrigin().getCluster().toString()+"."+
                                                edge.getDestination().getCluster().toString(),
                        v1, v2,1));
                graphVertexes.add(v1);
                graphVertexes.add(v2);
            }
        }
        graph.setEdges(graphEdges);
        graph.setVertexes(graphVertexes);
//        for(Vertex vertex:graph.getVertexes())
//            System.out.println("vertex id "+vertex.getId());
//        for(Edge edge:graph.getEdges())
//            System.out.println("edge id "+edge.getEdge_id());
        
        return graph;
    }
    
    public static boolean containsId(Set<Edge> graphEdges, String id){
        for(Edge edge:graphEdges){
            if(edge.getEdge_id().equals(id))
                return true;
        }
        return false;
    }
    
    
    public static Set<Edge> getClusterConnectionEdges() {
        Set<Edge> clusterConnectionEdges = new HashSet<>();
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        String hql = "FROM Edge WHERE cluster = 0";
        Query query = session.createQuery(hql);
        List<Edge> edges = query.list();
        session.close();
        clusterConnectionEdges.addAll(edges);
        System.out.println("The number of connections between the clusters is "+clusterConnectionEdges.size());
        boolean update = false;
        for(Edge edge:clusterConnectionEdges){
            Vertex origin = getVertex(edge.getOrigin().getId());
            Vertex destination = getVertex(edge.getDestination().getId());
            if(!edge.getDestination().getCluster().equals(destination.getCluster())){
                update=true;
                edge.getDestination().setCluster(destination.getCluster());
            }
            if(!edge.getOrigin().getCluster().equals(origin.getCluster())){
                update=true;
                edge.getOrigin().setCluster(origin.getCluster());
            }
            if(edge.getOrigin().getCluster()==edge.getDestination().getCluster()) {
                edge.setCluster(origin.getCluster());
                update=true;
            }
            if(update){
                System.out.println("IT NEEDS UPDATE");
            }
            update=false;
        }
        return clusterConnectionEdges;
    }
    
    public static Vertex getVertex(String vertexId){
        
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        String hql = "FROM Vertex v WHERE v.vertex_id = :vertexId";
        Query query = session.createQuery(hql);
        query.setParameter("vertexId",vertexId);
        Vertex vertex = (Vertex) query.uniqueResult();
        session.close();
        return vertex;
    }
    
    
    public static List<Cluster> getClusters(){
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        Criteria cr = session.createCriteria(Cluster.class);
        for(Object cluster:cr.list()){
            Cluster cl = (Cluster)cluster;
        }
        return cr.list();
    }
    
    public static Graph getGraph(){
        Graph graph = new Graph();
        graph.setVertexes(getVertexesSet());
        graph.setEdges(getEdgesSet());
        return graph;
    }
    
    public static Set<Vertex> getVertexesSet() {
        
        Session session = HibernateUtil.getSessionFactory().openSession();
        List vertexes = session.createQuery("FROM Vertex").list();
        session.close();
        Set<Vertex> set = new HashSet<Vertex>(vertexes);
        return set;
    }
    
    
    public static Set<Edge> getEdgesSet() {
        
        Session session = HibernateUtil.getSessionFactory().openSession();
        List edges = session.createQuery("FROM Edge").list();
        session.close();
        Set<Edge> set = new HashSet<Edge>(edges);
        return set;
    }
    
    public static HashMap<String,HashMap<Integer,Edge>> getConnectionEdges() {
        Set<Edge> connections = OptimizationController.optimizationControllerData.clusterConnections;
        HashMap<String,HashMap<Integer,Edge>> connectionsMap =  new HashMap<>();
        String clusters = null;
        for(Edge edge:connections){
            clusters = edge.getOrigin().getCluster()+"."+edge.getDestination().getCluster();
            if(!connectionsMap.containsKey(clusters)){
                HashMap<Integer,Edge> map = new HashMap<>();
                map.put(1,edge);
                connectionsMap.put(clusters,map);
            }else{
                connectionsMap.get(clusters).put(connectionsMap.get(clusters).size()+1,edge);
            }
        }
        return connectionsMap;
    }
    
    
    public static List<Integer> numberOfClusters(){
        Session session = HibernateUtil.getSessionFactory().openSession();
        String hql = "select distinct cluster from Edge";
        Query query = session.createQuery(hql);
        List<Integer> list= query.list();
        for (Iterator<Integer> iter = list.listIterator(); iter.hasNext(); ) {
            Integer a = iter.next();
            if (a.equals(0)) {
                iter.remove();
            }
        }
        session.close();
        return list;
    }
    
    
    public Result fillDatabaseFromGraphMl(){
        ClusterCreator clustersCreator = new ClusterCreator();
        return ok("The database is filled");
    }
    
//    public static Graph minimizeGraph(double originX, double originY , double destinationX, double destinationY, double distance){
//        Graph graph = new Graph();
//        Set<Edge> edgeSet = new HashSet<>();
//        Set<Edge> edges = getEdgesSet();
//        Set<Vertex> vertexSet = new HashSet<>();
//        double minY;
//        double minX;
//        double maxY;
//        double maxX;
//        if(originY<=destinationY){
//            minY=originY;
//            maxY=destinationY;
//        }
//        else {
//            minY=destinationY;
//            maxY=originY;
//        }
//
//        if(originX<=destinationX) {
//            minX = originX;
//            maxX = destinationX;
//        }
//        else {
//            minX=destinationX;
//            maxX = originX;
//        }
//
//        double earth = 6378.137;  //radius of the earth in kilometer
//        double pi = Math.PI;
//        double m = (1 / ((2 * pi / 360) * earth)) / 1000;  //1 meter in degree
//        double new_latitude = maxX + (distance * m);
//        double new_min_latitude = minX - (distance * m);
//        double new_longitude = maxY + (distance * m) / Math.cos(new_latitude * (pi / 180));
//        double new_min_longitude = minY - (distance * m) / Math.cos(new_min_latitude * (pi / 180));
//        int k=0;
//        Coordinate origin = new Coordinate(new_min_latitude,new_min_longitude);
//        Coordinate destination = new Coordinate(new_latitude,new_longitude);
//        Envelope box = new Envelope(origin,destination);
//        List<Long> areas = new ArrayList<>();
//        if(box==null) {
//            System.out.println("box is null");
//            return null;
//        }
//
//        for(Edge e: edges){
//            Vertex ov = getVertex(e.getOrigin().getId());
//            Vertex dv = getVertex(e.getDestination().getId());
//            if(box.contains(ov.getCoordinate())&&(box.contains(dv.getCoordinate())))
//            {
//                System.out.println("edge is contained");
//                edgeSet.add(e);
//                // areas.add(Long.parseLong(e.getArea_id()));
//                vertexSet.add(ov);
//                vertexSet.add(dv);
//            }else {
//                //System.out.println("edge is not contained");
//                k++;
//            }
//        }
//        System.out.println("The Minimized graph has edgeSet.size("+edgeSet.size());
//        graph.setVertexes(vertexSet);
//        graph.setEdges(edgeSet);
//        return graph;
//    }
//
//    public static Graph minimizeGraph(Coordinate origin, Coordinate destination){
//        Graph graph = new Graph();
//        Set<Edge> edgeSet = new HashSet<>();
//        Set<Edge> edges = getEdgesSet();
//        Set<Vertex> vertexSet = new HashSet<>();
//        List<String> vertexList = new ArrayList<>();
//        Envelope envelope = new Envelope(origin.x,destination.x,origin.y,destination.y);
//        List<Long> areas = new ArrayList<>();
//        for(Edge e: edges){
//            if(envelope==null){
//                System.out.println("The envelope box is null");
//            }else{
//                System.out.println("The envelope box is NOT null");
//                if(e.getOrigin().getCoordinate()!=null)
//                    System.out.println("e.getOrigin().getCoordinate() is NOT null");
//                else
//                    System.out.println("e.getOrigin().getCoordinate() is null");
//                if(origin!=null)
//                    System.out.println("userCoordinate is NOT null");
//                Vertex ov = getVertex(e.getOrigin().getId());
//                Vertex dv = getVertex(e.getDestination().getId());
//                if(envelope.contains(ov.getCoordinate())&&(envelope.contains(dv.getCoordinate()))
//                           ||(origin.equals(e.getDestination()))||(origin.equals(e.getOrigin()))
//                           ||(destination.equals(e.getOrigin()))||(destination.equals(e.getDestination()))) {
//                    edgeSet.add(e);
//                    vertexList.add(e.getOrigin().getId());
//                    vertexList.add(e.getDestination().getId());
//                    vertexSet.add(ov);
//                    vertexSet.add(dv);
//                }
//            }
//        }
//        System.out.println("The Minimized graph has edgeSet.size()"+edgeSet.size());
//        graph.setVertexes(vertexSet);
//        graph.setEdges(edgeSet);
//        return graph;
//    }
    
    
}
