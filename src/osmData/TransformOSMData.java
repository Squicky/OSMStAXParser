package osmData;

import java.util.regex.Pattern;


/**
 * This class transforms osm highway Types to maxSpeed values
 * @author Norbert Goebel
 * @author Daniel Sathees Elmo
 *
 */
public class TransformOSMData {
	
	//means of transport constants
	public static final int DEFAULT=0x00;
	public static final int CAR=0x01;
	public static final int TRAM=0x02;	
	
	//split pattern to detect numbers of lanes
	static final Pattern splitPattern = Pattern.compile("\\,|\\;");
	
	/**
	 * Don't let anyone create instances of this class
	 */
	private TransformOSMData(){}
	
	private static String [] highwayTypes = {"motorway","motorway_link","motorway_junction","trunk","trunk_link",
		"primary","primary_link","primary_trunk","secondary","secondary_link",
		"tertiary","tertiary_link","unclassified","unsurfaced","track",
		"residential","living_street","service","road","raceway",
		"xxx","xxx","xxx","xxx","xxx",
		"xxx","xxx","xxx","xxx","xxx", //intentionally left blank
		"steps","bridleway","cycleway","footway","pedestrian",
		"bus_guideway","path","xxx","xxx","xxx",
		"xxx","xxx","xxx","xxx","xxx",
		"xxx","xxx","xxx","xxx","xxx",};
	//highwaySpeeds should always be > 0 !!!
	private static int [] highwaySpeeds = {130,80,80,100,80,
		100,70,70,70,60,
		50,30,50,25,50,
		30,10,10,0,0,
		1,1,1,1,1,
		1,1,1,1,1,
		0,0,5,0,0,
		5,0,1,1,1,
		1,1,1,1,1,
		1,1,1,1,1};
	
	/**
	 * Returns a value that shows if a car may drive on this way.
	 * 
	 * @param highwayType
	 * @param motorcar
	 * @param id
	 * @return 0 = not allowed, 1 = restricted, 2 = allowed
	 */
	public static int carPermission(int highwayType, String motorcar,long id){
		// highway types usually intended for car
		if (highwayType>=0 && highwayType<=29)
		{
			if (motorcar.equals("") || motorcar.equals("yes") || motorcar.equals("designated") || motorcar.equals("official")) 
				return 2;
			else if (motorcar.equals("private") || motorcar.equals("permissive") || motorcar.equals("unknown") ||
					motorcar.equals("restricted") || motorcar.equals("destination") || motorcar.equals("customer") ||
					motorcar.equals("delivery") || motorcar.equals("agricultural") || motorcar.equals("forestry")){
				return 1;
			}
			else if (motorcar.equals("no"))
				return 0;
			else {
				System.out.println("Illegal motorcar/highway combination in way-id:"+id+
						" highway="+highwayTypes[highwayType]+" motorcar="+motorcar);
				return 0;
			}
		}
		// motor-driven vehicles can't drive on steps!
		else if (highwayType==30)
		{
			if (motorcar.equals("") || motorcar.equals("no"))
				return 0;
			else
			{
				System.out.println("Illegal motorcar/highway combination in way-id:"+id+
						" highway="+highwayTypes[highwayType]+" motorcar="+motorcar);
				return 0;
			}	
		}
		// highway types not designed for cars primarily
		else if (highwayType >= 31 && highwayType <=49)
		{
			if (motorcar.equals("") || motorcar.equals("no"))
				return 0;
			else if (motorcar.equals("yes") || motorcar.equals("designated") || motorcar.equals("official"))
				return 2;
			else if  (motorcar.equals("private") || motorcar.equals("permissive") || motorcar.equals("unknown") ||
					motorcar.equals("restricted") || motorcar.equals("destination") || motorcar.equals("customer") ||
					motorcar.equals("delivery") || motorcar.equals("agricultural") || motorcar.equals("forestry"))
				return 1;
			else 
			{
				System.out.println("Unhandled motorcar/highway combination in way-id:"+id+
						" highway="+highwayTypes[highwayType]+" motorcar="+motorcar);
				return 0;
			}
		}
		//else
		return 0;
	}
	
	
	public static int maxSpeed(int highwayType){
		if (highwayType == -1){
			//System.out.println("Ouch, unknown highwayType in maxspeed conversion");
			return 10;
		}
		else if (highwayType >=0 && highwayType < highwaySpeeds.length)
			return highwaySpeeds[highwayType];
		else {
			//System.out.println("Ouch, unknown highwayType in maxspeed conversion");
			return 10;
		}
	}
	
	
	public static int highwayType(String highway){
		for (int i=0;i<highwayTypes.length;i++){
			if (highwayTypes[i].equals(highway))
				return i;
		}
		//System.out.println("Warning, unknown highway type: "+highway);
		return -1;
	}
	
	/**
	 * this method sums the count of all lanes described in the lanes key
	 * @param lanes value as string
	 * @return numbers of lanes
	 */
	public static int getNrOfLanes(String lanes){
		
		//delete space characters
		lanes = lanes.replace(" ", "");
		
		//split lanes value
		String tokens[] = splitPattern.split(lanes);
		
		//count numbers of lanes
		Integer nrOfLanes=0;
		
		//sum all lanes
		for (String token : tokens){
			//try to read numbers of lanes in token
			try{
				nrOfLanes += Integer.parseInt(token);
			}
			//otherwise count as one lane
			catch (NumberFormatException e){
				nrOfLanes++;
			}
		}
		
		//there must be at least one lane!
		return (nrOfLanes > 0) ? nrOfLanes : 1;
	}
}
