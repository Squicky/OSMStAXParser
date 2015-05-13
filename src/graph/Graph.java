package graph;

import java.io.DataOutputStream;
import java.io.IOException;

//there still is some room for improvement in making the datastructure more memoryeffective
// for example one could save the streetnames in a hashtable so each name has to be saved only once
// downside: serializing and deserializing will get much more complicated

/**
 * This Class represents the RoutingGraph
 * @author Norbert Goebel
 *
 */
public class Graph {
	
	private Node[] nodes;
	private Edge[] edges;
	
	//minimal and maximal Latitudes and Longitudes in this map
	//this is needed to build the can zones to just overlap this map area
	private double minLat;
	private double maxLat;
	private double minLon;
	private double maxLon;
	private double safety = 0.001; //the safety margin for the intervals
	
	/**
	 * Creates an empty Graph with nodes Nodes and edges Edges
	 */
	public Graph(int nodes, int edges){
		this.nodes = new Node[nodes];
		this.edges = new Edge[edges];
		this.minLat = Double.MAX_VALUE;
		this.minLon = Double.MAX_VALUE;
		this.maxLat = -Double.MAX_VALUE;
		this.maxLon = -Double.MAX_VALUE;
	}

	/**
	 * This function adds an edge to the graph.
	 * @param startNode
	 * @param edgeIndex
	 * @param edge
	 */
	public void addEdge(int startNode, int edgeIndex, Edge edge){
		edges[edgeIndex]= edge;
		nodes[startNode].addEdge(edgeIndex);
	}
	
	/**
	 * Adds a node to the graph as long as the node is not already present in the 
	 * nodes Array
	 * @param index
	 * @param latitude
	 * @param longitude
	 */
	public void addNode(int index, long id, double longitude, double latitude){
		//only add the node if it has not already been added to the graph
		if (nodes[index]==null)
		{
			nodes[index]= new Node(id, longitude,latitude);
			
			//check if we have new min or max long/lat for this graph
			if (longitude < minLon)
				minLon = longitude;
			else if (longitude + safety > maxLon)
				maxLon = longitude + safety;
			if (latitude < minLat)
				minLat = latitude;
			else if (latitude + safety > maxLat)
				maxLat = latitude + safety;
		}
	}
	
	/**
	 * Adds a node to the graph as long as the node is not already present in the 
	 * nodes Array
	 * @param index
	 * @param latitude
	 * @param longitude
	 */
	/*
	public void addNode(int index, double longitude, double latitude){
		//only add the node if it has not already been added to the graph
		if (nodes[index]==null)
		{
			nodes[index]= new Node(longitude,latitude);
			
			//check if we have new min or max long/lat for this graph
			if (longitude < minLon)
				minLon = longitude;
			else if (longitude + safety > maxLon)
				maxLon = longitude + safety;
			if (latitude < minLat)
				minLat = latitude;
			else if (latitude + safety > maxLat)
				maxLat = latitude + safety;
		}
	}
*/
	/**
	 * Prints max/min longitude/latitude for debugging
	 */
	public void printMinMax(){
		System.out.println("Vorher: "+minLat+" "+maxLat+" "+minLon+" "+maxLon);
		double minLa = Double.MAX_VALUE;
		double maxLa = Double.MIN_VALUE;
		double minLo = Double.MAX_VALUE;
		double maxLo = Double.MIN_VALUE;
		double temp;
		
		for (int i=0; i<nodes.length; i++)
		{
			temp = nodes[i].getLatitude();
			if (temp < minLa)
				minLa = temp;
			if (temp > maxLa)
				maxLa = temp;
			temp = nodes[i].getLongitude();
			if (temp < minLo)
				minLo = temp;
			if (temp > maxLo)
				maxLo = temp;
		}
		System.out.println("Nachher: "+minLa+" "+maxLa+" "+minLo+" "+maxLo);
	}
	
/*	
	public void normalize(double from, double to){
		for (int i=0; i<nodes.length; i++)
			nodes[i].normalize(minLat,maxLat,minLon,maxLon,from,to);
		minLat = from;
		maxLat = to;
		minLon = from;
		maxLon = to;
	}
*/
	
	public int nodeCount(){
		return nodes.length;
	}
	
	public int edgeCount(){
		return edges.length;
	}
	
	public Edge getEdge(int edge) throws IndexOutOfBoundsException{
		return (Edge) edges[edge];
	}
	
	public Node getNode(int index) throws IndexOutOfBoundsException{
			return nodes[index];
	}

	/**
	 * This function serializes the graph and all its nodes + edges
	 * @param dos
	 * @param large = will betweenNodes be stored too?
	 * @throws IOException
	 */
	public void toDataStream(DataOutputStream dos, boolean large) throws IOException 
	{
		//first we store if this is a routing.graph.small=0 or routing.graph.large=1
		if (large)
		{			dos.writeInt(1); System.out.println(1); }
		else
			dos.writeInt(0);
		
		//first we export the dimensions of the graph
		dos.writeInt(nodes.length); System.out.println(nodes.length);
		dos.writeInt(edges.length); System.out.println(edges.length);
		
		//now we export the min/max coordinates used in this graph
		dos.writeDouble(this.minLat); System.out.println(this.minLat);
		dos.writeDouble(this.maxLat); System.out.println(this.maxLat);
		dos.writeDouble(this.minLon); System.out.println(this.minLon);
		
		int s = dos.size();
		dos.writeDouble(this.maxLon); System.out.println(this.maxLon);
		s = dos.size();
		
		//now we export the nodes list
		for (int i=0;i<nodes.length;i++){
			nodes[i].toDataStream(dos);
		}
		
		//export all edges
		for (int i=0;i<edges.length;i++){
			edges[i].toDataStream(dos,large);
		}
	}

	/**
	 * Set the bounds the OSM File tells us
	 * @param minlon
	 * @param maxlon
	 * @param minlat
	 * @param maxlat
	 */
	public void setDimensions(double minlon, double maxlon, double minlat, double maxlat)
	{
		this.minLat = minlat;
		this.minLon = minlon;
		this.maxLat = maxlat;
		this.maxLon = maxlon;
	}
}
