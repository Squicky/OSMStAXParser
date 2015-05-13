package graph;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

//import queue.Queue;

/**
 * This Class represents the nodes in the routinggraph
 * @author Norbert Goebel
 *
 */
public class Node {
	private double longitude; //needs to be double to reflect osm data exactly else 
	  						  //lat=51.1527874 get to be lat=51.1527876 ...
	private double latitude;
	
	private long id;
	
	private Vector<Integer> edges = new Vector<Integer>();
	
	/**
	 * Constructor for the Nodes
	 * @param longitude
	 * @param latitude
	 */
	public Node(long id, double longitude, double latitude) {
		super();
		this.longitude = longitude;
		this.latitude = latitude;
		this.id = id;
	}
	
	/**
	 * Add an edge starting at this node to the edgesQueue
	 * @param edge
	 */
	public void addEdge(int edge){
		edges.add(new Integer(edge));
	}

/*	
	public void normalize(double minlat, double maxlat, double minlon, double maxlon, double from, double to){
		longitude = from + (longitude - minlon) / (maxlon - minlon) * (to - from);
		latitude = from + (latitude - minlat) / (maxlat - minlat) * (to - from);
	}
*/
	
	/**
	 * @return the longitude
	 */
	public double getLongitude() {
		return longitude;
	}

	/**
	 * @return the latitude
	 */
	public double getLatitude() {
		return latitude;
	}

	/**
	 * @return the edges
	 */
	public Vector<Integer> getEdges() {
		return edges;
	}
	
	public int[] getEdgeIndexArray(){
		int[] array = new int[edges.size()];
		for(int i=0; i<edges.size();i++)
			array[i] = ((Integer)edges.elementAt(i)).intValue();
		
		return array;
	}
	
	/**
	 * This function serializes a node
	 * @param dos
	 * @throws IOException
	 */
	public void toDataStream(DataOutputStream dos) throws IOException 
	{
		//first we write long und lat
		int s = dos.size();
		dos.writeLong(id); System.out.println(id);
		s = dos.size();
		dos.writeDouble(longitude); System.out.println(longitude);
		dos.writeDouble(latitude); System.out.println(latitude);
		
		//now we write how many edges start at this node
		dos.writeInt(edges.size()); System.out.println(edges.size());
		
		//now we write the indexArray of the edges
		for(int i=0; i<edges.size();i++) {
			dos.writeInt(((Integer)edges.elementAt(i)).intValue());
			System.out.println(((Integer)edges.elementAt(i)).intValue());			
		}
	}
	
}
