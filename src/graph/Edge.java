package graph;

import java.io.DataOutputStream;
import java.io.IOException;


/**
 * This Class represents the edges in the routingGraph
 * @author Norbert Goebel
 *
 */
public class Edge {//extends QueueNode{

	private int osmMaxSpeed; // as taken from osm data or guessed by the parser
	private int carPermission; //0 = notallowed, 1 = restricted, 2 = allowed
	private int lanes=0;	//how many lanes does this way have?
	private int highwayType;
	private String name="";
	private int targetNode;
	private int length; //in meters
	private int[] betweenDistFromStart;
	private double[] betweenLat;
	private double[] betweenLon;
	
	/**
	 * Constructor for a new Edge
	 * @param osmMaxSpeed
	 * @param carPermission
	 * @param lanes
	 * @param highwayType
	 * @param name
	 * @param targetNode
	 * @param length
	 */
	public Edge(int osmMaxSpeed, int carPermission, int lanes, int highwayType,
			String name, int targetNode, int length, int[] betweenDist, double[] betweenLon, double[] betweenLat) {
		super();
		this.osmMaxSpeed = osmMaxSpeed;
		this.carPermission = carPermission;
		this.lanes = lanes;
		this.highwayType = highwayType;
		this.name = name;
		this.targetNode = targetNode;
		this.length = length;
		this.betweenDistFromStart=betweenDist;
		this.betweenLon = betweenLon;
		this.betweenLat = betweenLat;
	}

	/**
	 * @return the osmMaxSpeed
	 */
	public int getOsmMaxSpeed() {
		return osmMaxSpeed;
	}

	/**
	 * @return the carPermission
	 */
	public int getCarPermission() {
		return carPermission;
	}

	/**
	 * @return the lanes
	 */
	public int getLanes() {
		return lanes;
	}

	/**
	 * @return the highwayType
	 */
	public int getHighwayType() {
		return highwayType;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the target
	 */
	public int getTargetNode() {
		return targetNode;
	}

	/**
	 * @return the length
	 */
	public int getLength() {
		return length;
	}
	
	/**
	 * This function serializes an edge
	 * @param dos
	 * @param large = write betweenNodes and distances too
	 * @throws IOException
	 */
	public void toDataStream(DataOutputStream dos, boolean large) throws IOException 
	{
		dos.writeInt(osmMaxSpeed); 
		dos.writeInt(carPermission);
		dos.writeInt(lanes);
		dos.writeInt(highwayType);
		dos.writeUTF(name);
		dos.writeInt(targetNode);
		dos.writeInt(length);
		
		if (large)
		{
			dos.writeInt(betweenLat.length);
			for (int i=0; i< betweenLat.length; i++)
			{
				dos.writeDouble(betweenLon[i]);
				dos.writeDouble(betweenLat[i]);
				dos.writeInt(betweenDistFromStart[i]);
			}
		}
	}
}
