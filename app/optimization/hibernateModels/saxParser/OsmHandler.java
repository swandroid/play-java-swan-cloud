package optimization.hibernateModels.saxParser;

import com.vividsolutions.jts.geom.Coordinate;
import optimization.hibernateModels.Edge;
import optimization.hibernateModels.Vertex;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Maria Efthymiadou on Jun, 2019
 */

public class OsmHandler extends DefaultHandler {


    List<Edge> edges = new ArrayList<Edge>();
    List<Vertex> vertexes = new ArrayList<Vertex>();
    private Vertex currentVertex;
    private Edge currentEdge;
    private List<Vertex> ways = new ArrayList<>();
    private List<String> waysRef = new ArrayList<>();

    public HashMap<String, HashMap<Vertex, Vertex>> getEdgesMap() {
        return edgesMap;
    }

    private HashMap<String,HashMap<Vertex,Vertex>> edgesMap = new HashMap<>();

    public List<Edge> getEdges(){
        return  edges;
    }

    public List<Vertex> getVertexes(){
        return vertexes;
    }

    int waysin=0, waysout=0;

    @Override
    public void startElement(String uri, String localName, String qName,
                             Attributes attrs){
        if ("way".equals(qName)) {
            ways.clear();
            waysRef.clear();
            String key = attrs.getValue("id");
           // System.out.println("key: "+key);
            Edge edge = new Edge();
            edge.setEdge_id(key);
            currentEdge =edge;
            waysin++;
        }
        if ("nd".equals(qName)) {
            String key = attrs.getValue("ref");
            //System.out.println("ref: "+key);
            for(Vertex v: vertexes){
                if(v.getId().equals(key)){
                    //System.out.println("found v.origin:"+v.getCoordinate().toString());
                    //System.out.println("adding to ways:"+v.getID());
                    ways.add(v);
                }
            }
            waysRef.add(key);
        }

        if ("node".equals(qName)) {
            String key = attrs.getValue("id");
            //System.out.println("inside node his id:  "+ key);
            String nodeLat = attrs.getValue("lat");
            String nodeLon = attrs.getValue("lon");
            Coordinate locationCoordinate = new Coordinate(Double.parseDouble(nodeLat),Double.parseDouble(nodeLon));
            Vertex vertex  = new Vertex(key,null,locationCoordinate);
            currentVertex = vertex;
            vertexes.add(currentVertex);
            //System.out.println("new vertex: "+currentVertex);


        }


    }

    @Override
    public void endElement(String uri, String localName, String qName){
        if(qName.equals("node")){
            if(currentVertex!=null) {
                //vertexes.add(currentVertex);
                //System.out.println("adding vertex:  "+currentVertex.getId().toString()+ "   "+currentVertex.getCoordinate().x);
                currentVertex = null;
            }
        }
        if(qName.equals("way")){
            if((currentEdge!=null) && (!ways.isEmpty())){
                int i;
               // for(i=0;i<ways.size()-1;i++){
                    //System.out.println("ways: "+ways.get(i).getID());
                    //System.out.println("waysRef:"+waysRef.get(i));
                //}

                HashMap<Vertex,Vertex> nodes = new HashMap<>();
                for(i=0;i<ways.size()-1;i++){
                       // System.out.println("ways: "+ways.get(i));
                    //int r = 1+(int)Math.random()*8;
                    Vertex v1 = ways.get(i);
                    Vertex v2 = ways.get(i+1);
                    String s1 = waysRef.get(i);
                    String s2 = waysRef.get(i+1);

                    System.out.println(ways.size()+ "              currentEdge  "+currentEdge.getEdge_id());

                    edges.add(new Edge(currentEdge.getEdge_id()+s1+s2,v1,v2,i+1));
                    edges.add(new Edge(currentEdge.getEdge_id()+s2+s1,v2,v1,i+1));
                    nodes.put(v1,v2);
                    edgesMap.put(currentEdge.getEdge_id(),nodes);


                    //System.out.println("origin: "+ways.get(i)+ "  destination:  "+ways.get(i+1)+" , "+String.valueOf(i+1));
                    //System.out.println("origin: "+ways.get(i+1)+ "  destination:  "+ways.get(i)+" , "+String.valueOf(i+1));
                }
                //System.out.println("currentEdge:  "+currentEdge.getEdgeId().toString()+"   ways:  "+ways.toString());
                currentEdge=null;
                ways.clear();
                waysRef.clear();
            }
            waysout++;
        }

    }
}


