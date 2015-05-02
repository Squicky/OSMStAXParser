package osmData;

/**
 * Calculate the accurate distance between two coordinates given in Longitude/Latitude
 * @author Norbert Goebel
 *
 */
public final class Distance {
	
	 /**
	 * Don't let anyone instantiate this class.
	 */
	private Distance(){}
	
	/**
	 * Calculate the distance betwwen two point with lat/lon coordinates in degrees
	 * 
	 * @return distance in meters
	 */
	//calculate the distance between two points with lat/lon coordinates in degrees
	public static int dist(double lat1, double lon1, double lat2, double lon2){
		
		//First version:
		//Approximation using 
//	    sqrt(x * x + y * y)
//
//	    where x = 69.1 * (lat2 - lat1)
//	    and y = 69.1 * (lon2 - lon1) * cos(lat1/57.3) 
		
		//Second version:
//		Calculation with the Haversine formula:
//
//			r = radius of the earth in km
//			deltalat = lat2 - lat1
//			deltalong = long2-long1
//			a = sin^2(deltalat/2) + cos(lat1)*cos(lat2)*sin^2(deltalong/2)
//			c = 2*atan2(sqrt(a),sqrt(1-a))
//			d = R*c

//			(Note that angles need to be in radians to pass to trig functions).
			int r = 6371000; //Radius of the earth in m
			double dLat2=(lat1-lat2)/360*Math.PI; //dlat/2 
			double dLon2=(lon1-lon2)/360*Math.PI; //dlon/2

			double sindlat2 = Math.sin(dLat2);
			double sindlon2 = Math.sin(dLon2);
			double a = sindlat2 * sindlat2 + Math.cos(lat1/180*Math.PI) * Math.cos(lat2/180*Math.PI) * sindlon2 * sindlon2;  
			double dist = r * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
			
		return (int) dist;
	}
}
