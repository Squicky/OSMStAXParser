package boundary;

import java.util.Hashtable;
import osmData.OSMNode;
import osmData.OSMWay;
import osmData.TransformOSMData;
import tools.Tools;
import gps.GPSTrace;

/**
 * This class contains info about a boundary need to be extracted
 * @author Daniel Sathees Elmo
 *
 */
public class Boundary{
	
	//boundary values, maximum elongation by default
	private double minLat = -Double.MAX_VALUE;
	private double minLon = -Double.MAX_VALUE;
	private double maxLat = Double.MAX_VALUE;
	private double maxLon = Double.MAX_VALUE;
	
	//means of transport for this boundary
	private int meansOfTransport = TransformOSMData.DEFAULT;
	
	//name of boundary
	private String boundaryName="";
	
	//hash tables to store OSM nodes & ways 
	private Hashtable<Long, OSMNode> nodeList = new Hashtable<Long, OSMNode>();
	private Hashtable<Long, OSMWay> osmWays = new Hashtable<Long, OSMWay>();
	
	/**
	 * constructor needs an gps trace, the means of transport and a name for this boundary
	 * @param gpsTrace
	 * @param meansOfTransport
	 * @param boundaryName
	 * @throws NumberFormatException
	 */
	public Boundary(GPSTrace gpsTrace, int meansOfTransport, String boundaryName) throws NumberFormatException{
		//call other constructor
		this(gpsTrace.getMinGPSLat(), gpsTrace.getMinGPSLat(), gpsTrace.getMinGPSLat(), gpsTrace.getMinGPSLat(), 
			meansOfTransport, boundaryName);
		
	}
	
	/**
	 * constructor needs boundary values as String, the means of transport and a name for this boundary
	 * @param minLat
	 * @param minLon
	 * @param maxLat
	 * @param maxLon
	 * @param meansOfTransport
	 * @param boundaryName
	 * @throws NumberFormatException
	 */
	public Boundary(String minLat, String minLon, String maxLat, String maxLon, int meansOfTransport, String boundaryName) throws NumberFormatException
	{
		this(Double.parseDouble(minLat), Double.parseDouble(minLon), Double.parseDouble(maxLat), Double.parseDouble(maxLon),
			meansOfTransport, boundaryName);
	}
	
	/**
	 * constructor needs boundary, the means of transport and a name for this boundary
	 * @param minLat
	 * @param minLon
	 * @param maxLat
	 * @param maxLon
	 * @param meansOfTransport
	 * @param boundaryName
	 * @throws NumberFormatException
	 */
	public Boundary(double minLat, double minLon, double maxLat, double maxLon, int meansOfTransport, String boundaryName) throws NumberFormatException
	{
		//save name and vehicle
		this(meansOfTransport, boundaryName);
		//save boundary
		this.minLat = Tools.normalizeLat(minLat);
		this.minLon = Tools.normalizeLon(minLon);
		this.maxLat = Tools.normalizeLat(maxLat);
		this.maxLon = Tools.normalizeLat(maxLon);
	}
	
	/**
	 * constructor needs boundary, a buffer value for cutting, the means of transport and a name for this boundary
	 * @param minLat
	 * @param minLon
	 * @param maxLat
	 * @param maxLon
	 * @param buffer
	 * @param meansOfTransport
	 * @param boundaryName
	 * @throws NumberFormatException
	 */
	public Boundary(double minLat, double minLon, double maxLat, double maxLon, double buffer, int meansOfTransport, String boundaryName) throws NumberFormatException
	{
		// call another constructor in consideration of map cutting buffer value
		this((minLat - buffer) , (minLon - buffer), (maxLat + buffer), (maxLon + buffer), meansOfTransport, boundaryName);
	}
	
	
	
	/**
	 * constructor just to set the means of transport and a boundary name
	 * @param meansOfTransport
	 * @param boundaryName
	 */
	public Boundary(int meansOfTransport, String boundaryName)
	{
		//notice vehicle
		this.meansOfTransport = meansOfTransport;
		//save name of boundary
		this.boundaryName = boundaryName;
	}
	
	/**
	 * @return min latitude
	 */
	public double getMinLat(){
		return minLat;
	}
	
	/**
	 * @return min longitude
	 */
	public double getMinLon(){
		return minLon;
	}
	
	/**
	 * @return max latitude
	 */
	public double getMaxLat(){
		return maxLat;
	}
	
	/**
	 * @return max longitude
	 */
	public double getMaxLon(){
		return maxLon;
	}
	
	/**
	 * set min latitude
	 * @param minLat
	 * @throws NumberFormatException
	 */
	public void setMinLat(double minLat)throws NumberFormatException{
		this.minLat = Tools.normalizeLat(minLat);
	}
	
	/**
	 * set min longitude
	 * @param minLon
	 * @throws NumberFormatException
	 */
	public void setMinLon(double minLon)throws NumberFormatException{
		this.minLon = Tools.normalizeLon(minLon);
	}
	
	/**
	 * set max latitude
	 * @param maxLat
	 * @throws NumberFormatException
	 */
	public void setMaxLat(double maxLat)throws NumberFormatException{
		this.maxLat = Tools.normalizeLat(maxLat);
	}
	
	/**
	 * set max longitude
	 * @param maxLon
	 * @throws NumberFormatException
	 */
	public void setMaxLon(double maxLon)throws NumberFormatException{
		this.maxLon = Tools.normalizeLon(maxLon);
	}
	
	/**
	 * @return the means of tranport
	 */
	public int getMeansOfTransport(){
		return meansOfTransport;
	}
	
	/**
	 * set means of transport
	 * @param meansOfTransport
	 */
	public void setMeansOfTransport(int meansOfTransport){
		this.meansOfTransport = meansOfTransport;
	}
	
	/**
	 * @return the name of boundary
	 */
	public String getBoundaryName(){
		return boundaryName;
	}
	
	/**
	 * set name for boundary
	 * @param boundaryName
	 */
	public void setBoundaryName(String boundaryName){
		this.boundaryName = boundaryName;
	}
	
	/**
	 * @return the node list as hash table
	 */
	public Hashtable<Long, OSMNode> getNodeList(){
		return nodeList;
	}
	
	/**
	 * set node list
	 * @param nodeList
	 */
	public void setNodeList(Hashtable<Long, OSMNode> nodeList){
		this.nodeList = nodeList;
	}
	
	/**
	 * @return the list of OSM ways
	 */
	public Hashtable<Long, OSMWay> getOSMWays(){
		return osmWays;
	}
	
	/**
	 * set list of OSM ways
	 * @param osmWays
	 */
	public void setOSMWays(Hashtable<Long, OSMWay> osmWays){
		this.osmWays = osmWays;
	}
}
