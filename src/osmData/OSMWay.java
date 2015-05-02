package osmData;

import java.util.NoSuchElementException;
import java.util.Vector;

/**
 * Container for OSMWay Data
 * 
 * @author Norbert Goebel
 * @author Daniel Sathees Elmo
 *
 */

public class OSMWay {

	private long id;
	private int maxSpeed; // as taken from osm data or -1 if not given in osm data
	private boolean oneWay; //is this way unidirectional ?
	private int carPermission; //0 = notallowed, 1 = restricted, 2 = allowed
	private int lanes=1;	//how many lanes does this way have?
	private int highwayType;
	private String name="";
	private int meansOfTransport = TransformOSMData.DEFAULT;
	
	//save all part ways belongs to an OSM way with its specific id
	private Vector<Vector<Long>> osmPartWays = new Vector<Vector<Long>>(); 
	
	public OSMWay(OSMWay oldOSMWay, Vector<Long> osmNodes){
		super();
		this.osmPartWays.add(osmNodes) ;
		this.id = oldOSMWay.getId();
		this.highwayType = oldOSMWay.getHighwayType();
		this.oneWay = oldOSMWay.isOneWay();
		this.lanes = oldOSMWay.getLanes();
		this.maxSpeed = oldOSMWay.getOsmMaxSpeed();
		this.carPermission = oldOSMWay.getCarPermission();
		this.name = oldOSMWay.getName();
		this.meansOfTransport = oldOSMWay.getMeansOfTransport();
	}
		
	public OSMWay(long id, Vector<Vector<Long>> osmPartWays, int maxSpeed, boolean oneWay,
			String lanes, String motorcar, String highway, String name, final int meansOfTransport) {
		super();
		this.id = id;
		this.osmPartWays = osmPartWays;
		this.oneWay = oneWay;
		this.maxSpeed = maxSpeed;
		
		//try to get numbers of lanes in this way
		try{
			this.lanes = Integer.valueOf(lanes);
		}
		//otherwise call converting method cause some idiot didn't entered a valid integer number
		catch (NumberFormatException e){
			System.err.println(lanes);
			this.lanes = TransformOSMData.getNrOfLanes(lanes);
		}
		
		this.name = name;
		this.meansOfTransport = meansOfTransport;
		
		//if (!name.isEmpty()) System.out.println(name);
				
		//transform Highway string to integer
		highwayType=TransformOSMData.highwayType(highway);
		
		//check carPermission
		carPermission = TransformOSMData.carPermission(highwayType, motorcar, id);
		
		//check car permission to set flag
		if (carPermission!=0)
			this.meansOfTransport |= TransformOSMData.CAR;
	}
	
	/**
	 * Counts all nodes 
	 * @return	Returns the number of Nodes in all part ways	 
	 */
	public int getNumberOfAllOSMNodes(){
		int nodesNumbers=0;
		// sum all nodes of all partWays
		for (Vector<Long> osmWay : osmPartWays)
			nodesNumbers += osmWay.size();
		
		return nodesNumbers;
	}
	
	/**
	 * Returns the number of Nodes in a part way
	 * @param	index of part way	
	 * @return 	nodes count of part way
	 */
	public int getNumberOfOSMNodes(int wayIndex){
		try{
			return osmPartWays.elementAt(wayIndex).size();
		}catch(ArrayIndexOutOfBoundsException e){
			return 0;
		}	
	}

	/**
	 * get the ID of the start node of a part way
	 * @param wayIndex index of part way
	 * @return Long nodeID
	 */
	public Long getStart(int wayIndex){
		try{
			return (Long) osmPartWays.elementAt(wayIndex).firstElement();
		}catch(IndexOutOfBoundsException | NoSuchElementException e){
			return new Long(0);
		}	
	}
	
	/**
	 * get the ID of the start node of the first part way
	 * @return Long nodeID
	 */
	public Long getStart(){
		return getStart(0);
	}
	
	/**
	 * get the ID of the target node of this way
	 * @param wayIndex index of part way
	 * @return Long nodeID
	 */
	public Long getTarget(int wayIndex){
		try{
			return (Long) osmPartWays.elementAt(wayIndex).lastElement();
		}catch(IndexOutOfBoundsException | NoSuchElementException e){
			return new Long(0);
		}	
	}
	
	/**
	 * get the ID of the target node of the first part way
	 * @return Long nodeID
	 */
	public Long getTarget(){
		return getTarget(0);
	}
	
	/**
	 * Adds node id of a way point into the first partWay
	 * @param nodeID OSM ID of node
	 * @return boolean if adding was successful
	 */
	public boolean addOSMNode(long nodeID){
		return addOSMNode(nodeID, 0);
	}
	
	/**
	 * Adds node id of a way point into a part way
	 * @param nodeID OSM ID of node
	 * @param wayIndex index of part way
	 * @return boolean if adding was successful
	 */
	public boolean addOSMNode(long nodeID, int wayIndex){
		try{
			osmPartWays.elementAt(wayIndex).addElement(new Long(nodeID));
			return true;
		}catch(ArrayIndexOutOfBoundsException e){
			return false;
		}
	}
	
	/**
	 * Get vector of (Long) nodeIDs of a part way
	 * @return	vector<Long>
	 */
	public Vector<Long> getOSMPartWay(int wayIndex){
		try{
			return osmPartWays.elementAt(wayIndex);
		}catch(ArrayIndexOutOfBoundsException e){
			return null;
		}
	}
	
	/**
	 * Get vector of (Long) nodeIDs of first part way
	 * @return	vector<Long>
	 */
	public Vector<Long> getOSMPartWay(){
		return getOSMPartWay(0);
	}
	
	/**
	 * Get vector with all part ways
	 * @return	vector of vector<Long> with nodeIDs
	 */
	public Vector<Vector<Long>> getOSMPartWays(){
		return osmPartWays;
	}
	
	/**
	 * get count of part ways
	 * @return
	 */
	public int getNumbersOfOSMPartWays(){
		return osmPartWays.size();
	}
	
	/**
	 * get way id
	 * @return the id of this way in OSMdata
	 */
	public long getId() {
		return id;
	}

	/**
	 * get max speed of this way
	 * @return the maxSpeed (this is either the osmMaxSpeed or one "calculated" by roadcondition)
	 */
	public int getMaxSpeed() {
		if (maxSpeed>0) 
			return maxSpeed;
		else 
			return TransformOSMData.maxSpeed(highwayType);
	}
	
	/**
	 * get max speed stored in OSM file
	 * @return the osmMaxSpeed (will be -1 if not set in OSM Data!)
	 */
	public int getOsmMaxSpeed() {
		return maxSpeed;
	}

	/**
	 * is this a one way road?
	 * @return the oneWay
	 */
	public boolean isOneWay() {
		return oneWay;
	}
	
	/**
	 * Tell me if a car is allowed to use this road
	 * 0 = not allowed
	 * 1 = restricted
	 * 2 = allowed
	 * @return the carPermission
	 */
	public int getCarPermission() {
		return carPermission;
	}
	
	/**
	 * get all means of tranport as bits set
	 * @return
	 */
	public int getMeansOfTransport()
	{
		return meansOfTransport;
	}
	
	/**
	 * checks if this way is permitted for given means of transport
	 * @param 	transportFlag	OSMWay.CAR, .TRAM, .ILLEGAL
	 * @return 	boolean
	 */
	public boolean getMeansOfTransportPermission(final int transportFlag)
	{
		//check if bit for this means of transport is set
		return ((meansOfTransport & transportFlag) != 0);
	}
	
	/** 
	 * Get the number of lanes of this way
	 * @return the lanes
	 */
	public int getLanes() {
		return lanes;
	}

	/**
	 * Returns an integer representing the highwayType
	 * @return the highwayType
	 */
	public int getHighwayType() {
		return highwayType;
	}

	/**
	 * Return the name of this street (if set in OSM Data)
	 * @return the name
	 */
	public String getName() {
		return name;
	}
}
