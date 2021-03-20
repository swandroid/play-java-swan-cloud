package optimization;

import com.opencsv.CSVWriter;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import com.tinkerpop.blueprints.util.io.graphml.GraphMLReader;
import com.vividsolutions.jts.geom.Coordinate;
import controllers.OptimizationController;
import optimization.hibernateModels.Cluster;
import optimization.hibernateModels.Edge;
import optimization.hibernateModels.Graph;
import optimization.hibernateModels.Vertex;
import optimization.hibernateModels.hibernateUtils.HibernateUtil;
import optimization.locationUtils.GeoLocation;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Query;
import org.hibernate.Session;
import tyrex.util.ArraySet;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static controllers.OptimizationController.getEdgesSet;
import static controllers.OptimizationController.getVertexesSet;
import static java.lang.System.exit;

/**
 * Created by Maria Efthymiadou on Jun, 2019
 */

public class ClusterCreator {
	
	static String BASE_FOLDER = "" ;
	String graphFile =BASE_FOLDER+"city.graphml";
	//graphFile =BASE_FOLDER+"city.graphml";
	Graph graph;
	String writeToFile = BASE_FOLDER+"Centroids.txt";
	HashMap<Integer,Vertex> clusterCentroid = new HashMap<>();
	static Set<Edge>  setEdges = getEdgesSet();
	Set<Vertex> blackListAll = new ArraySet();
	
	HashMap<Integer, Graph> graphSet;
	
	public ClusterCreator() {
		//run the python script to download the city.graphml file
		parseGraphmlFile();
		createVertexClusterFile();
		//run python script to create the file finalFile.csv
		cvsParserUpdateCluster(BASE_FOLDER+"Database/finalFile.csv");
		correctClusters();
		setClusterBounderyCoordinates();
	}
	
	/** parses the graphml file and stores the vertexes and the edges in the database*/
	
	public void parseGraphmlFile(){
	
//		String XML_FILE = BASE_FOLDER+"graph.graphml";
		
		Set<Vertex> nodesSet = new HashSet<>();
		Set<Edge> edgesSet = new HashSet<>();
		String XML_FILE = graphFile;
		TinkerGraph tinkerGraph = new TinkerGraph();
		GraphMLReader reader = new GraphMLReader(tinkerGraph);
		InputStream is = null;
		try {
			is = new BufferedInputStream(new FileInputStream(XML_FILE));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		try {
			reader.inputGraph(is);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Iterable<com.tinkerpop.blueprints.Vertex> vertices = tinkerGraph.getVertices();
		Iterable<com.tinkerpop.blueprints.Edge> edges = tinkerGraph.getEdges();
		int i = 0, k = 0;
		for (com.tinkerpop.blueprints.Vertex v : vertices) {
			String lat =v.getProperty("x");
			String longi = v.getProperty("y");
			
			System.out.println("v.Id() = "+v.getId()+ " lat= "+lat+ "  longi  "+longi);
			Vertex vertex =new Vertex((String)v.getId(),null,new Coordinate(Double.parseDouble(lat),Double.parseDouble(longi)),0);
			i++;
			nodesSet.add(vertex);
		}
		
		for (com.tinkerpop.blueprints.Edge v : edges) {
			//System.out.println(v.getId() + ", " +v.getVertex(Direction.IN) +"  "+v.getVertex(Direction.OUT));
			String d2 = v.getProperty("osmid");
			String length = v.getProperty("length");
			String name = v.getProperty("name");
			System.out.println("osmid= "+d2+" length= "+length+ " name= "+name);
			Vertex source = null;
			Vertex destination = null;
			for(Vertex vertex:nodesSet){
				if(vertex.getId().equals(v.getVertex(Direction.IN).getId())){
					source = vertex;
					System.out.println("DIRECTION.IN = "+v.getVertex(Direction.IN).getId());
				}
				if(vertex.getId().equals((v.getVertex(Direction.OUT).getId()))){
					destination = vertex;
					System.out.println("DIRECTION.OUT = "+v.getVertex(Direction.OUT).getId());
				}
			}
			if((source!=null)&&(destination!=null)){
				Coordinate middlepoint = new Coordinate();
				Edge edget = new Edge((String)v.getId(),source,destination,(float)1,0);
				edget.setDistance(Double.parseDouble(v.getProperty("length")));
				k++;
				edgesSet.add(edget);
			}else
			{
				System.out.println("the edge with id: "+v.getId()+" has null origin or destination");
			}
		}
		graph = new Graph(nodesSet, edgesSet);
		setMiddlePoint();
		System.out.println("START FILLING DATABASE");
		fillDatabase();
		System.out.println("The database is succesfully filled");
	}
	
	private void setMiddlePoint() {
			for(Edge edge:graph.getEdges()){
				GeoLocation origin= GeoLocation.fromDegrees(edge.getOrigin().getCoordinate().x, edge.getOrigin().getCoordinate().y);
				GeoLocation destination = GeoLocation.fromDegrees(edge.getDestination().getCoordinate().x, edge.getDestination().getCoordinate().y);
				GeoLocation middle = origin.findMiddleFromDegrees(destination);
				edge.setMiddlePoint(new Coordinate(middle.getLatitudeInDegrees(),middle.getLongitudeInDegrees()));
			}

	}
	
	public void fillDatabase(){
		if(graph!=null) {
			Session session = HibernateUtil.getSessionFactory().openSession();
			session.beginTransaction();
			int n = 0;
			System.out.println("the size of the graph vertexes is "+graph.getVertexes().size());
			Iterator<Vertex> iter = graph.getVertexes().iterator();
			while (n < graph.getVertexes().size() && iter.hasNext()) {
				Vertex t = iter.next();
				session.save(t);
				session.flush();
				session.clear();
				n++;
			}
			session.close();
			fillDatabaseEdges();
		}else System.out.println("The Graph is null");
	}
	
	public void fillDatabaseEdges(){
		Session session = HibernateUtil.getSessionFactory().openSession();
		session.beginTransaction();
		Vertex origin,destination;
		for(Edge edge: graph.getEdges()){
			origin = OptimizationController.getVertex(edge.getOrigin().getId());
			destination = OptimizationController.getVertex(edge.getDestination().getId());
			edge.setOrigin(origin);
			edge.setDestination(destination);
			session.save(edge);
			session.flush();
			session.clear();
		}
		session.close();
	}
	
	/** it creates the file tha is used for the python script to cluster the vertexes*/
	
	public void createVertexClusterFile(){
		Set<Vertex> vertexSet = getVertexesSet();
		final String STRING_ARRAY_SAMPLE = BASE_FOLDER+"VertexFile.csv";
		try (
				Writer writer = Files.newBufferedWriter(Paths.get(STRING_ARRAY_SAMPLE));
				CSVWriter csvWriter = new CSVWriter(writer,
						CSVWriter.DEFAULT_SEPARATOR,
						CSVWriter.NO_QUOTE_CHARACTER,
						CSVWriter.DEFAULT_ESCAPE_CHARACTER,
						CSVWriter.DEFAULT_LINE_END);
		) {
			String[] headerRecord = {"osmid", "lat", "lon"};
			csvWriter.writeNext(headerRecord);
			for(Vertex vertex:vertexSet){
				csvWriter.writeNext(new String[]{vertex.getId(), String.valueOf(vertex.getCoordinate().x),
						String.valueOf(vertex.getCoordinate().y)});
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/** it updates the cluster of the vertexes and the edges* */
	public static void cvsParserUpdateCluster(String csvFile){
		
		String line = "";
		String cvsSplitBy = ",";
		Vertex vertex;
		try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
			System.out.println("START PARSING CLUSTER FILE");
			while ((line = br.readLine()) != null) {
				String[] row = line.split(cvsSplitBy);
				try{
					System.out.println("cluster: "+row[0]+"  ,osm_id " + row[3] + " , lat=" + row[1] + " , long="+row[2]);
					String cluster = StringUtils.deleteWhitespace(row[0]);
					int clusterInt = Integer.parseInt(cluster) +1;
					vertex = OptimizationController.getVertex(StringUtils.deleteWhitespace(row[3]));
					vertex.setCluster(clusterInt);
					updateObject(vertex);
				}catch(NumberFormatException ex){
					ex.printStackTrace();
					System.out.println("class:  "+row[1].getClass().toString());
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		updateEdges();
	}
	
	public static void updateEdges(){
		Set<Edge> edgeSet = getEdgesSet();
		Vertex origin,destination;
		for(Edge edge:edgeSet){
			origin = OptimizationController.getVertex(edge.getOrigin().getId());
			destination = OptimizationController.getVertex(edge.getDestination().getId());
			edge.setDestination(destination);
			edge.setOrigin(origin);
			if(origin.getCluster().equals(destination.getCluster()))
				edge.setCluster(origin.getCluster());
			else
				edge.setCluster(0);
			
			updateObject(edge);
		}
		
		
	}
	
	
	/** it corrects the cluster of the vertexes (and related edges) so that they are connected in it.
	 * */
	public void correctClusters() {
		graphSet = OptimizationController.getLocalGraphSet();
		createClusterCentroid();
		getBlackList();
		System.out.println("Blacklist initially "+blackListAll.size());
        while(blackListAll.size()!=0) {
            for(Vertex vertex:blackListAll){
                System.out.println(" Black Vertex "+vertex.getId()+" cluster "+vertex.getCluster());
                if(findCluster(vertex, blackListAll)){
	                setEdges = getEdgesSet();
                    getBlackList().remove(vertex);
                    System.out.println("Blacklist:  "+blackListAll.size());
                }
            }
        }
		System.out.println("The clusters are corrected.");
	}
	
	private boolean findCluster(Vertex vertex, Set<Vertex> black) {
		List<Edge> edgeOriginList =new ArrayList<>();
		List<Edge> edgeDestinationList = new ArrayList<>();
		List<Edge> edgeOList = new ArrayList<>();
		for(Edge edge:setEdges){
			if(edge.getOrigin().getId().equals(vertex.getId())){
				edgeOriginList.add(edge);
				if(!containsVertex(black,edge.getDestination())){
					edgeOList.add(edge);
				}
			}
			if(edge.getDestination().getId().equals(vertex.getId())){
				edgeDestinationList.add(edge);
			}
		}
		if(edgeDestinationList.size()!=edgeOriginList.size()){
			System.out.println("destination and origin list have different sizes. It should not happen");
			System.exit(0);
		}
		while (edgeOList.size()>0){
			int newCluster = chooseRandomCluster(edgeOList);
			System.out.println("vertex cluster BEFORE update : "+OptimizationController.getVertex(vertex.getId()).getCluster());
			int oldCluster = vertex.getCluster();
			vertex.setCluster(newCluster);
			graphSet.get(oldCluster).getVertexes().remove(vertex);
			graphSet.get(newCluster).getVertexes().add(vertex);

			updateCluster(vertex,newCluster);
			int edgeOldCluster;
			for(Edge e: edgeOriginList){
				edgeOldCluster=e.getCluster();
				e.setOrigin(vertex);
				Vertex destination = OptimizationController.getVertex(e.getDestination().getId());
				e.setDestination(destination);
				if(e.getOrigin().getCluster().equals(destination.getCluster())) {
					e.setCluster(newCluster);
					graphSet.get(e.getOrigin().getCluster()).getEdges().add(e);
				}else{
					e.setCluster(0);
				}
				if(edgeOldCluster>0){
					graphSet.get(edgeOldCluster).getEdges().remove(e);
				}
				updateObject(e);
			}
			
			for(Edge e: edgeDestinationList){
				e.setDestination(vertex);
				Vertex origin = OptimizationController.getVertex(e.getOrigin().getId());
				e.setOrigin(origin);
				edgeOldCluster=e.getCluster();
				if(origin.getCluster().equals(e.getDestination().getCluster())){
					e.setCluster(newCluster);
					graphSet.get(e.getOrigin().getCluster()).getEdges().add(e);
				}else{
					e.setCluster(0);
				}
				if(edgeOldCluster>0){
					graphSet.get(edgeOldCluster).getEdges().remove(e);
				}
				updateObject(e);
				
			}
			System.out.println("vertex cluster AFTER update : "+OptimizationController.getVertex(vertex.getId()).getCluster());
			edgeOList.clear();
			return true;
		}
		return false;
	}
	
	private int chooseRandomCluster(List<Edge> edgeOList) {
		int index =new  Random().nextInt(edgeOList.size());
		int cluster = edgeOList.get(index).getDestination().getCluster();
		return cluster;
	}
	
	private boolean containsVertex(Set<Vertex> black, Vertex node) {
		for(Vertex vertex:black){
			if(vertex.getId().equals(node.getId())){
				return true;
			}
		}
		return false;
	}
	
	private void createClusterCentroid() {
		createClusterCentroidFile();
		try {
			readFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(clusterCentroid.isEmpty()){
			System.out.println("The clusters centroids are empty");
			exit(1);
		}
	}
	
	public void readFile() throws IOException {
		File file = new File(writeToFile);
		BufferedReader br = new BufferedReader(new FileReader(file));
		String st;
		int i=1;
		while ((st = br.readLine()) != null){
			Vertex vertex = OptimizationController.getVertex(String.valueOf(StringUtils.deleteWhitespace(st)));
			clusterCentroid.put(i, vertex);
			i++;
		}
	}
	
	
	private void createClusterCentroidFile() {
		List<Vertex> centroids = new ArrayList<>();
		
		for(int i=1;i<=getNumberOfClusters().size();i++){
			List<Vertex> vertexList = getVertexes(i);
			double avgX=0,avgY=0;
			for(Vertex vertex:vertexList){
				avgX = avgX + vertex.getCoordinate().x;
				avgY = avgY + vertex.getCoordinate().y;
			}
			Coordinate coordinate = new Coordinate(avgX/vertexList.size(),avgY/vertexList.size());
			Vertex centroid = null;
			double distance = Double.MAX_VALUE;
			for(Vertex vertex:vertexList){
				if(vertex.getCoordinate().distance(coordinate)<distance){
					distance=vertex.getCoordinate().distance(coordinate);
					centroid=vertex;
				}
			}
			System.out.print("the centroid for cluster "+i+ " is the vertex with id: "+centroid.getId()+
					                 "  with coordinate x: "+centroid.getCoordinate().x+ "  and coordinate y: "+centroid.getCoordinate().y);
			centroids.add(centroid);
		}
		try {
			writeFile(centroids);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public List<Integer> getNumberOfClusters(){
		Session session = HibernateUtil.getSessionFactory().openSession();
		session.beginTransaction();
		String hql = "Select distinct cluster from Vertex";
		Query query = session.createQuery(hql);
		List<Integer> clusters =  query.list();
		session.close();
		return clusters;
	}
	
	public void writeFile(List<Vertex> centroids) throws IOException {
		File file = new File(writeToFile);
		FileOutputStream fos = new FileOutputStream(file);
		
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
		
		for (Vertex vertex:centroids) {
			bw.write(vertex.getId());
			bw.newLine();
		}
		
		bw.close();
	}
	
	public Set<Vertex> getBlackList(){
		int l=0;
		blackListAll = new ArraySet();
		for(Integer graph:graphSet.keySet()) {
			DijkstraAlgorithm dijkstraAlgorithm = new DijkstraAlgorithm(graphSet.get(graph), "");
			dijkstraAlgorithm.execute(clusterCentroid.get(graph));
			int k = 0;
			List<Vertex> blackList = new ArrayList<>();
			for (Vertex vertex : graphSet.get(graph).getVertexes()) {
				LinkedList<Vertex> path = dijkstraAlgorithm.getPath(vertex);
				if(!vertex.getId().equals(clusterCentroid.get(vertex.getCluster()).getId())) {
					if (path == null) {
						k++;
						blackList.add(vertex);
						blackListAll.add(vertex);
					}
				}
				
			}
			System.out.println(" number of vertexes in graph : "+graph+"  are  "+graphSet.get(graph).getVertexes().size()+" k = "+k );
			l=l+k;
		}
		return blackListAll;
	}
	
	public List<Vertex> getVertexes(Integer cluster){
		Session session = HibernateUtil.getSessionFactory().openSession();
		session.beginTransaction();
		String hql = "FROM Vertex v WHERE v.cluster = :cluster";
		Query query = session.createQuery(hql);
		query.setParameter("cluster",cluster);
		List<Vertex> vertices =  query.list();
		session.close();
		return vertices;
	}
	
	
	/** create the cluster objects and their boundaries that are used in the skeleton graph*/
	
	public void setClusterBounderyCoordinates(){
		HashMap<Integer,Graph> map = OptimizationController.getLocalGraphSet();
		for(Integer cluster:map.keySet()){
			findClusterEdgeCoordinates(String.valueOf(cluster),map.get(cluster));
//            Set<Sensor> clusterSensors = new HashSet<>();
//            for(Edge edge:map.get(cluster).getEdges()){
//                clusterSensors.add(edge.getSensor());
//            }
		
		}
	}
	
	private static void findClusterEdgeCoordinates(String s, Graph graph) {
		Coordinate random = findRandomGraphCoordinate(graph.getVertexes());
		Coordinate east=random;
		Coordinate west=random;
		Coordinate north=random;
		Coordinate south=random;
		for(Vertex vertex:graph.getVertexes()){
			if(vertex.getCoordinate().x<west.x){
				west=vertex.getCoordinate(); }
			if(vertex.getCoordinate().x>east.x){
				east=vertex.getCoordinate();
			}
			if(vertex.getCoordinate().y>north.y){
				north=vertex.getCoordinate();
			}
			if(vertex.getCoordinate().y<south.y){
				south=vertex.getCoordinate();
			}
		}
		updateClusterEdgeCoordinates(s,east,west,north,south);
	}
	
	private static Coordinate findRandomGraphCoordinate(Set<Vertex> vertexes) {
		Iterator iter = vertexes.iterator();
		return ((Vertex)iter.next()).getCoordinate();
	}
	
	private static void updateClusterEdgeCoordinates(String s, Coordinate east, Coordinate west, Coordinate north, Coordinate south) {
		System.out.println("create cluster "+s+ "  east "+east.toString()+ " west "+west.toString()
				                   +" north "+north.toString()+ " south  "+south.toString());
		Cluster cluster = new Cluster();
		cluster.setCluster_id(s);
		cluster.setEast(east);
		cluster.setWest(west);
		cluster.setNorth(north);
		cluster.setSouth(south);
		saveDatabaseObject(cluster);
		
	}
	
	private static void saveDatabaseObject(Object object) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		session.beginTransaction();
		try{
			session.save(object);
			session.getTransaction().commit();
		}catch (Exception e){
			e.printStackTrace();
		}
		session.close();
	}
	
	public static void updateObject(Object object){
		Session session = HibernateUtil.getSessionFactory().openSession();
		org.hibernate.Transaction tr =session.beginTransaction();
		session.saveOrUpdate(object);
		tr.commit();
		session.close();
	}
	
	
	public static void updateCluster(Vertex vertex, Integer cluster){
		Session session = HibernateUtil.getSessionFactory().openSession();
		org.hibernate.Transaction tr =session.beginTransaction();
		System.out.println(cluster.toString());
		System.out.println(vertex.getId());
		vertex.setCluster(cluster);
		session.saveOrUpdate(vertex);
		tr.commit();
		session.close();
	}
	
	
}
