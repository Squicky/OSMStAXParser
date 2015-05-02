package osmData;

/**
 * Storage for OSMNode data
 * @author Norbert Goebel
 *
 */
public class OSMNode {
	private double longitude; //needs to be double to reflect osm data exactly else 
							  //lat=51.1527874 get to be lat=51.1527876 ...
	private double latitude;
	private int outerNodeCount = 0;
	private int innerNodeCount= 0;
	
	public OSMNode(double longitude, double latitude) {
		super();
		this.longitude = longitude;
		this.latitude = latitude;
	}
	
	public void resetCounters(){
		innerNodeCount=0;
		outerNodeCount=0;
	}
	
	public double getLongitute() {
		return longitude;
	}
	
	public double getLatitude() {
		return latitude;
	}

	/**
	 * @return the outerNodeCount
	 */
	public int getOuterNodeCount() {
		return outerNodeCount;
	}

	/**
	 * @return the innterNodeCount
	 */
	public int getInnerNodeCount() {
		return innerNodeCount;
	}
	
	public void increaseInnerNodeCount(){
		innerNodeCount++;
	}
	
	public void increaseOuterNodeCount(){
		outerNodeCount++;
	}
}
