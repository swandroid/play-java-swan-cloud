package optimization;

import optimization.algorithm.UserOptimizationData;
import optimization.core.population.Individual;
import optimization.hibernateModels.Cluster;
import optimization.hibernateModels.Edge;
import optimization.hibernateModels.Graph;
import optimization.hibernateModels.Vertex;
import play.libs.ws.WSClient;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static controllers.OptimizationController.getGraph;

/**
 * Created by Maria Efthymiadou on Jun, 2019
 */

public class OptimizationControllerData {
	
	/**It stores maximum and minimum Sensor values that are used during normalization*/
	public static ConcurrentHashMap<String,Double> maxSensorValues = new ConcurrentHashMap<>();
	public static ConcurrentHashMap<String,Double> minSensorValues = new ConcurrentHashMap<>();
	
	/**It contains the average objective values which can be also used for performing a penalty in the fitness function*/
	public static HashMap<String, Double> historicalObjectiveValues = new HashMap();
	
	/**It stores information for the evaluation process of the user route*/
	public static HashMap<String,List<TimeStampedRoute>> userEvaluationRoute = new HashMap<>();
	
	/**It stores the dynamic sensor values necessary for the experiments*/
	public static ConcurrentHashMap<String,List<Double>> dynamicSensorValues = null;
	
	/**It is used to change the sensor values*/
	public static AtomicInteger listPosition= new AtomicInteger(0);
	
	/**It is used to change the sensor values*/
	public static SensorValuesModifier sensorValuesModifier;
	
	/**It is used to time the sensor modification*/
	public static Timer timer = new Timer();
	
	
	/**It contains all the users and their data*/
	public static HashMap<String, UserOptimizationData> activeUsersData = new HashMap<>();
	
	/**It contains the users ws necessary for disseminating the path through the framework.*/
	public static HashMap<String,WSClient> activeUsersWs = new HashMap<>();
	
	/**It contains the elitist solution*/
	public static ConcurrentHashMap<String,Individual> elit = new ConcurrentHashMap<>();
	
	/**The Graph of DublinCity*/
	public static Graph DublinCityGraph = getGraph();
	
	/**It stores the graph of each cluster in a map*/
	public static HashMap<Integer, Graph> graphMap;
	
	/**The connection edges between the clusters that do not belong to a subgraph*/
	public static Set<Edge> clusterConnections;
	
	/**The connectionsEdgeHashMap include the edges that connect the clusters. The difference with the above is that
	 * we categorize them in every 2 clusters they connect (clusterA, clusterB). The key is the clusterAId.clusterBId */
	public static HashMap<String,HashMap<Integer,Edge>> connectionsEdgeHashMap;
	
	/**The sensors per cluster. They do not include the sensors for the clusterConnections*/
	public static HashMap<Integer,List<String>> clusterSensors;
	
	/**The graph has the cluster ids as vertexes and an edge if there is a connection between 2 clusters.
	 *  It is used to create the initial population*/
	public static Graph skeleton;
	
	/**The individual maps with the sensor ids and the relevant values*/
	public static ConcurrentHashMap<String, Double> sound = new ConcurrentHashMap<>();
	public static ConcurrentHashMap<String, Double> air = new ConcurrentHashMap<>();
	
	/**The map stores all the objective+sensorId types as keys*/
	public static ConcurrentHashMap<String,Double> sensorValues= new ConcurrentHashMap<>();
	
	/**Required by the unregister to stop all the user related threads */
	public static HashMap<String,Thread> activeUsersThreads = new HashMap<>();
	
	public static ConcurrentHashMap<String, Vertex> active=new ConcurrentHashMap<>();
	
	/**It contains all the nodes the user already been. We use it in the algorithms to avoid creating a path with a cycle*/
	public static ConcurrentHashMap<String, List<Vertex>> userRoute=new ConcurrentHashMap<String, List<Vertex>>();
	
	public static HashMap<Integer, Cluster> clusterMap;
	
	/**It contains a boolean that notifies the sensor about an update on the user location*/
	public static ConcurrentHashMap<String, Boolean> userNewData = new ConcurrentHashMap<>();
	
}
