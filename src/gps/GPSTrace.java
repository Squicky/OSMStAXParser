package gps;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.regex.Pattern;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import tools.MyFile;

/**
 * This class contains info about an GPS trace
 * @author Daniel Sathees Elmo
 *
 */
public class GPSTrace {
	
	//boundary values of GPS trace
	private double minGPSLat = Double.MAX_VALUE;
	private double minGPSLon = Double.MAX_VALUE;
	private double maxGPSLat = -Double.MAX_VALUE;
	private double maxGPSLon = -Double.MAX_VALUE;
	
	private long refTimeStamp=0; 		//time stamp of GPS trace file
	private long nrOfGPSPoints=0;	//numbers of GPS points in file
	
	//filename of GPS trace
	private MyFile gpsTracefile;
	
	//pattern for gps point: timestamp, latitude, longitude -> digit(s),digit(s).digit(s),digit(s).digit(s)
	static private final Pattern gpsPattern = Pattern.compile("-?\\d+(,-?\\d+.\\d+){2}"); 
	static private final Pattern gpsSplitPattern = Pattern.compile(",");
	
	// pattern for date strings in GPX files (e.g. "2012-10-02T16:17:16Z"), we have to split at '-', 'T' and 'Z' Position
	static private final Pattern gpxDateSplitPattern = Pattern.compile("[-TZ]");
	static private final int GPX_STRING_DATE_PARTS = 4; // we must have 4 parts after splitting: 1.Year 2.Month 3.Day 4.Time(HH:MM:ss) 
	
	/**
	 * constructor needs path of GPS trace file
	 * @param filePath file path of GPS trace file (text file or GPX file format)
	 * @exception FileNotFoundException if GPS trace file can't be found
	 * @exception IOException if reading file occurs an error
	 * @exception NumberFormatException  if a number can't be read
	 */
	
	public GPSTrace(String filePath) throws Exception {
		// access file and save name
		gpsTracefile = new MyFile(filePath);
		
    	// TEXT file
    	if (filePath.toLowerCase().endsWith(".txt")) 
    		saveGPSTraceInfoFromTextFile(filePath);
    	// GPX XML file
    	else if (filePath.toLowerCase().endsWith(".gpx")) 
    		saveGPSTraceInfoFromGPXFile(filePath);
		// otherwise throw exception
    	else throw new Exception("Not valid GPS file extension!");
	}
	
	/**
	 * gets info out of a GPX file (boundary, numbers of GPS Points & reference timestamp)
	 * and saves it
	 * @param filePath
	 * @throws Exception
	 */
	public void saveGPSTraceInfoFromGPXFile(String filePath) throws Exception{
		// variables
    	boolean isInsideMetadata = false;
    	
    	// try initialize stream reader with XML file
    	InputStream inputStream = new FileInputStream(filePath);
    	XMLStreamReader parser;
    	try {
			parser = XMLInputFactory.newInstance().createXMLStreamReader(inputStream);
		} catch (XMLStreamException e) {
			System.err.println("XML parser couldn't be created (file: " + filePath + ")");
			throw e;
		}
    	
		// read latitude / longitude of each track point
		double lat = 0;
		double lon = 0;
   
    	// get time stamp and bounds
    loop:
    	while (parser.hasNext()) {
    		switch (parser.getEventType()) {
    			case XMLStreamConstants.START_ELEMENT:
    				
    				// notice that we entered metadata info
    				if (parser.getLocalName().equals("metadata"))
    					isInsideMetadata = true;
    				// read reference time stamp inside metadata
    				else if (parser.getLocalName().equals("time") && isInsideMetadata)
    					refTimeStamp = readGPXTimeStamp(parser);
    				// track point tag reached, set flag
    				else if (parser.getLocalName().equals("trkpt")) {
						// read latitude and longitude
						for (int i=0; i < parser.getAttributeCount(); i++) {
							if (parser.getAttributeLocalName(i).equals("lat"))
								lat = Double.parseDouble(parser.getAttributeValue(i));
							else if (parser.getAttributeLocalName(i).equals("lon"))
								lon = Double.parseDouble(parser.getAttributeValue(i));
						}
						// adjust boundary
						if (lat<minGPSLat) minGPSLat=lat;
						if (lon<minGPSLon) minGPSLon=lon;
						if (lat>maxGPSLat) maxGPSLat=lat;
						if (lon>maxGPSLon) maxGPSLon=lon;
						// count numbers of GPS points
						nrOfGPSPoints++;
					}
    				break;
    				
    			// leave loop after track, reset flag after metadata block
    			case XMLStreamConstants.END_ELEMENT:
    				if (parser.getLocalName().equals("trk"))
    					break loop;
    				else if (parser.getLocalName().equals("metadate"))
    					isInsideMetadata = false;
    		}
    		// get next event
    		parser.next();
    	}
		// release resources
		parser.close();
		
		
	  	System.out.println("GPS-trace boundary min(Lon,Lat),max(Lon,Lat) : ("+
					minGPSLon+", "+minGPSLat+"),("+maxGPSLon+", "+maxGPSLat+")");
		
    }
	
    /**
     * if time tag is reached, this method will extract timestamp value in milliseconds
     * @param parser
     * @return
     * @throws Exception
     */
    private static long readGPXTimeStamp(XMLStreamReader parser) throws Exception {
    	// get next tag, ignore white spaces an comments
    	while (parser.hasNext()) {
    		// next content must be characters
    		if (parser.getEventType() == XMLStreamConstants.CHARACTERS)
    			return dateInGPXToMilli(parser.getText());
    		else if ((parser.getEventType() == XMLStreamConstants.END_ELEMENT) && (parser.getLocalName().equals("time")))
    			break;
    		// get next element
    		parser.next();
    	}
    	// throw error exception
    	throw new Exception("No time character stream available inside time tag");
    }
    
    /**
     * convert date string out of GPX files to milliseconds since 1.January.1970
     * @param gpxDateString 
     * @return
     * @throws Exception
     */
    private static long dateInGPXToMilli(String gpxDateString) throws Exception {
    	String dateString;
    	
    	// apply split pattern
		String dateStringParts[] = gpxDateSplitPattern.split(gpxDateString);
		
		// check correct amount of split parts
		if (dateStringParts.length == GPX_STRING_DATE_PARTS) {
			// rebuild compatible date string for parsing
			dateString = dateStringParts[2] + "." + dateStringParts[1] + "." + dateStringParts[0] + " " + dateStringParts[3]; 
		}
		// otherwise throw exception cause we've got a wrong formated GPX date string
		else throw new Exception("GPX date string doesn't match to format YYYY-MM-DDTHH:MM:ssZ");
		
		// create date
		DateFormat dateFormatter = DateFormat.getDateTimeInstance();
		Date date = dateFormatter.parse(dateString);
		
		// return date in milliseconds since 1.January.1970
		return date.getTime();
    }
	
    /**
	 * gets info out of a GPS text file (boundary, numbers of GPS Points & reference timestamp)
	 * @param filePath
	 * @throws Exception
	 */
	public void saveGPSTraceInfoFromTextFile(String filePath) throws Exception{
		try{
			//read file via buffered Reader due to better performance
			FileReader fReader = new FileReader(gpsTracefile);
			BufferedReader bReader = new BufferedReader(fReader);

			//read first line
			String line = bReader.readLine();
			
			//line must be "#n" with n = Number Of GPS Points in file
			if(line.matches("#\\d+"))
				nrOfGPSPoints = Long.parseLong(line.substring(1));
			else			
				System.out.println("Numbers of GPS Point information couldn't be read");
			
			//read second line
			line=bReader.readLine();
			
			//line must contain reference timestamp, ignore case sensitivity
			if(line.matches("(?i)#all Tstamps substracted by \\d+"))
				refTimeStamp = Long.parseLong(line.substring(28));
			else			
				System.out.println("Numbers of GPS Point information couldn't be read");
			
			//read third line, ignore though it contains information about gps information syntax
			bReader.readLine();
			
			//store read data
			double lat=0.0;
			double lon=0.0;
			String[] gpsData;
			
			while((line = bReader.readLine()) != null){
				//readed line must confirm to pattern
				if (gpsPattern.matcher(line).matches() || line.startsWith("2014-")){
					gpsData = gpsSplitPattern.split(line);
					
					//ignore first field (timestamp), read latitude/longitude
					lat=Double.parseDouble(gpsData[1]);
					lon=Double.parseDouble(gpsData[2]);
					
					//adjust boundary
					if (lat<minGPSLat) minGPSLat=lat;
					if (lon<minGPSLon) minGPSLon=lon;
					if (lat>maxGPSLat) maxGPSLat=lat;
					if (lon>maxGPSLon) maxGPSLon=lon;
				}
				else
					System.out.println(line+" doesn't match gps information pattern!");
			}
			
			// close reader
			bReader.close();
			fReader.close();
			
			/*
		  	System.out.println("GPS-trace boundary min(Lon,Lat),max(Lon,Lat) : ("+
						minGPSLon+", "+minGPSLat+"),("+maxGPSLon+", "+maxGPSLat+")");
			*/
			
		}
		catch(FileNotFoundException e){
			System.out.println("GPS-trace file not found!");
			throw e;
		}
		catch(IOException e){
			System.out.println("Error while reading GPS-trace file!");
			throw e;
		}
		catch(NumberFormatException e){
			System.out.println("Error reading number!");
			throw e;
		}
	}
	
	/**
	 * @return time stamp of GPS trace
	 */
	public long getRefTimeStamp(){
		return refTimeStamp;
	}
	
	/**
	 * @return count of all GPS points
	 */ 
	public long getNrOfGPSPoints(){
		return nrOfGPSPoints;
	}
	
	/**
	 * @return  minimum GPS Point Latitude
	 */ 
	public double getMinGPSLat(){
		return minGPSLat;
	}
	
	/**
	 * @return  minimum GPS Point Longitude
	 */
	public double getMinGPSLon(){
		return minGPSLon;
	}
	
	/**
	 * @return maximum GPS Point Latitude
	 */
	public double getMaxGPSLat(){
		return maxGPSLat;
	}
	
	/**
	 * @return  maximum GPS Point Longitude
	 */
	public double getMaxGPSLon(){
		return maxGPSLon;
	}
	
	/**
	 * @return name of GPS trace file
	 */
	public String getFileName(){
		return gpsTracefile.getName();
	}
	
	/**
	 * @return name of GPS trace file without extension
	 */
	public String getFileNameWithoutEx(){
		return gpsTracefile.getNameWithoutExt();
	}
}
