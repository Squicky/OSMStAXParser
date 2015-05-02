package tools;

/**
 * Calculate the accurate distance between two coordinates given in Longitude/Latitude
 * @author Norbert Goebel
 * @author Daniel Sathees Elmo
 *
 */
public final class Tools {
	
	 /**
	 * Don't let anyone instantiate this class.
	 */
	private Tools(){}
	
	/**
	 * Calculate the distance betwwen two point with lat/lon coordinates in degrees
	 * 
	 * @return distance in meters
	 */
	//calculate the distance between two points with lat/lon coordinates in degrees
	public static int distance(double lat1, double lon1, double lat2, double lon2){
		
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
	
	/**
	* normalizes Longitude if value is not between -180.0 and 180.0 degrees
	* 
	* @param 	longitude value to normalize as String
	* @return 	normalized longitude between -180.0 and 180.0 degrees
	* @throws	NumberFormatException
	*/
	public static double normalizeLon(String longitude) throws NumberFormatException
	{
		//try to read lon as string and normalize to get value between -180.0,180.0
		return normalizeLon(Double.parseDouble(longitude));
		
	}
	
	/**
	* normalizes Latitude if value is not between -90.0 and 90.0 degrees
	* 
	* @param	latitude value to normalize as String
	* @return 	normalized latitude between -90.0 and 90.0 degrees
	* @throws	NumberFormatException
	*/
	public static double normalizeLat(String latitude) throws NumberFormatException
	{
		//try to read lon as string and normalize to get value between -90.0,90.0
		return normalizeLat(Double.parseDouble(latitude));
	}
	
	/**
	* normalizes Longitude if value is not between -180.0 and 180.0 degrees
	* 
	* @param 	longitude value to normalize
	* @return 	normalized longitude between -180.0 and 180.0 degrees
	*/
	public static double normalizeLon(double longitude) throws NumberFormatException
	{
		//normalize to get value between -180.0,180.0
		return normalizeDeg(longitude, -180.0, 180.0);
	}
	
	/**
	* normalizes Latitude if value is not between -90.0 and 90.0 degrees
	* 
	* @param	latitude value to normalize
	* @return 	normalized latitude between -90.0 and 90.0 degrees
	*/
	public static double normalizeLat(double latitude) throws NumberFormatException
	{
		//normalize to get value between -90.0,90.0
		return normalizeDeg(latitude, -90.0, 90.0);
	}
	
	/**
	 * normalizes degree values to an given range
	 * @param value 		degree value to normalize
	 * @param rangeStart 	begin of interval in degrees
	 * @param rangeEnd 		end of interval in degrees
	 * @return 				normalized value in degrees
	 * @throws NumberFormatException
	 */
	private static double normalizeDeg(double value, double rangeStart, double  rangeEnd) throws NumberFormatException
	{			
		//calcuate offset position in interval
		double erg = value % rangeEnd;
		
		//from which intervall-border do we start
		//after passing (a) period(s)
		double rangeBorder = (erg > 0 || (erg == 0.0 && value < 0)) ? rangeStart : rangeEnd;
		
		//do we have to start at the beginning of our range?
		if ((int)(value / rangeEnd) % 2 != 0) erg+=rangeBorder;
		
		//clean if we get -0.0
		return (erg==-0.0) ? 0.0 : erg;
		
	}
	
	/**
	 * checks if a string value can be read as double value
	 * @param value 		string value to check
	 * @return 				boolean true or false
	 */
	public static boolean isDouble(String value)
	{
		try{
			Double.parseDouble(value);
			return true;
		}catch(NumberFormatException e){
			return false;
		}
	}
}
