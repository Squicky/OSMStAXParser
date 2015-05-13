package osmstaxparser;
import gps.GPSTrace;
import graph.Edge;
import graph.Graph;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import boundary.Boundary;

import osmData.OSMNode;
import osmData.OSMWay;
import osmData.TransformOSMData;
import tools.MyFile;
import tools.Tools;

/**
 * 
 * This class implements a StAX Parser for OSM XLM Files.
 * The result will be saved to a file called routing.graph
 * 
 * @author Norbert Goebel
 * @author Daniel Sathees Elmo
 *
 */

public class OSMStAXParser {
	
	//map boundary buffer
	static final double MAP_BUFFER = 0.0025;
	
	//definition in Open Street Map
	static final int MIN_WAYSIZE=2;
	static final int MAX_WAYSIZE=2000;
	static final int MAX_PARTWAYS=MAX_WAYSIZE-1;
	
	//arguments reading offset constants
	static final int BOUNDARY_OFFSET=4;
	static final int GPS_TRACE_OFFSET=1;
	
	//XML classes
	static XMLInputFactory factory = XMLInputFactory.newInstance(); 
	static XMLStreamReader parser;
	
	//file class to access XML file
	static MyFile xmlFile;
	
	//OSM XML file info
	static String osmVersion="";
	static String osmGenerator="";
	
	//Vector to save all boundaries user entered, counter for unnamed boundaries
	static Vector<Boundary> boundaries = new Vector<Boundary>();
	static int boundaryCounter = 0;
	
	//OSM informations
	static String primType=""; //notice which element (data primitive) we handle (e.g. node, way, relation, member etc...)
	static Long id=new Long(0); //this is intentionally a Long and not a long as hash table needs objects as keys.
	static double latitude=0;
	static double longitude=0;
	static boolean oneWay=false;
	static boolean error=false;
	static boolean wayNotNeeded = false; //if we don't need this way (like areas or service routes)
	static String lanes="1";
	static int maxSpeed=-1;
	static String motorcar="";
	static String highway="";
	static String name="";
	static String lastkey=""; 
	static int meansOfTransport=TransformOSMData.DEFAULT;
	
	//we use vectors to save way nodes, part ways and routing ways
	static Vector<Long> wayNodes = new Vector<Long>(MAX_WAYSIZE);
	static Vector<OSMWay> routingWays = new Vector<OSMWay>();
	
	//spacer to check XML formation
	static StringBuilder spacer = new StringBuilder();

	//store bounds of OSM-file
	static double osmMinLat=  -Double.MAX_VALUE; 
	static double osmMinLon=  -Double.MAX_VALUE;
	static double osmMaxLat=  Double.MAX_VALUE;
	static double osmMaxLon=  Double.MAX_VALUE;
		
	//graph properties
	static long totalStreetLength=0;
	static int edges=0;
	static int nodes=0;
	
	//variables to measure runtime performance
	static long progStartTime=0;
	static long sectionStartTime=0;
	static long calculationTime=0;
	static long writingTime=0;
	static long runtimeLength=0;
	static double membefore=0;
	static double memafter=0;
	
	/**
	 * calculates memUsage in MB
	 * @return
	 */
	public static double memUsage(){
		Runtime rt = Runtime.getRuntime();
        double mem = (rt.totalMemory() - rt.freeMemory()) / (1024.0 * 1024.0);
        return mem = Math.round(mem * 100) / 100.0;
	}
	
	/**
	 * Handles OSM Nodes
	 */
	public static void nodeHandler(){
		//mark that we handle a node
		primType="node";
		
		//read node data
		for ( int i = 0; i < parser.getAttributeCount(); i++ ) {
	    	  if (parser.getAttributeLocalName(i)=="id")
	    		  id=Long.valueOf(parser.getAttributeValue(i));
	    	  else if (parser.getAttributeLocalName(i)=="lat")
	    		  latitude=Double.valueOf(parser.getAttributeValue(i)).doubleValue();
	    	  else if (parser.getAttributeLocalName(i)=="lon")
	    		  longitude=Double.valueOf(parser.getAttributeValue(i)).doubleValue();
	    	  /*
	    	  else
	    		  //should never be called
	    		  printUnknownAttribute("node", i);
	  		  */
	    }
		
		//add node for each boundary, if node is included
		for(Boundary boundary : boundaries)
		{
			//if boundary contains node, put node to hash table 
			if (longitude >= boundary.getMinLon() && longitude <= boundary.getMaxLon() &&
				latitude >= boundary.getMinLat() && latitude <= boundary.getMaxLat())
				boundary.getNodeList().put(id, new OSMNode(id, longitude,latitude));
		}
	}
	
	/**
	 * Handles OSM ways
	 */
	public static void wayHandler(){
		/*
		System.out.println(nodeVector.size()+" Nodes in "+
						((float)(System.currentTimeMillis()-progStartTime) / 1000.0) +" s eingelesen!");
		System.exit(0);
		*/
		
		//mark that we handle a way, assume we need this way!
		primType="way";
		wayNotNeeded=false;
		
		//first we clear all leftover values:
  	  	id= new Long(0);
  	  	latitude=0;
  	  	longitude=0;
  	  	oneWay=false;
  	  	lanes="1";
  	  	motorcar="";
  	  	highway="";
  	  	name="";
  	  	lastkey="";
  	  	maxSpeed=-1; //set to -1 to check if it really was set in OSM database
  	  	meansOfTransport=TransformOSMData.DEFAULT;
  	  	
  	  	//initialize new vectors to store part ways and their nodes
  	  	wayNodes = new Vector<Long>(MAX_WAYSIZE);
  	  	//partWays = new Vector<Vector<Long>>(MAX_PARTWAYS);
  	  	
  	  	//now we check all Attributes of the way element
  	  	for ( int i = 0; i < parser.getAttributeCount(); i++ ) {
  	  		if (parser.getAttributeLocalName(i).equals("id"))
  	  			id=Long.valueOf(parser.getAttributeValue(i));
  	  		/*
  	  		else if (parser.getAttributeLocalName(i).equals("timestamp"))
  	  			//ignore
  	  			;
  	  		else if (parser.getAttributeLocalName(i).equals("created_by"))
	    		//ignore
  	  			;
  	  		else if (parser.getAttributeLocalName(i).equals("visible"))
  	  			//ignore
  	  			;
  	  		else if (parser.getAttributeLocalName(i).equals("user"))
  	  			//ignore
  	  			;
	    	else
	    		//should never be called
	    		printUnknownAttribute("way", i);
  	  		*/
  	  	}
	}
	
	/**
	 * Handles XML References
	 */
	public static void referenceHandler(){
		//safe all nodes that belongs to a way
		for ( int i = 0; i < parser.getAttributeCount(); i++ ) {
			if (parser.getAttributeLocalName(i)=="ref")
    	  	  	wayNodes.add(Long.valueOf(parser.getAttributeValue(i)));
			else 
				//should never be called
				printUnknownAttribute("nd", i);
		}
	}
	
	/**
	 * Save OSM Way
	 */
	public static void addWay()
	{	
		//save ways for each boundary, if they cross those
		for (Boundary boundary : boundaries){
			
			//store nodes of a part way
			Vector<Long> partWayNodes = new Vector<Long>();
			Vector<Vector<Long>> partWays = new Vector<Vector<Long>>(MAX_PARTWAYS);
			
			//check if boundary contains every node, otherwise split up ways
			for (Long nodeID : wayNodes){
				if (boundary.getNodeList().containsKey(nodeID))	//start a part way if boundary contains node
					partWayNodes.add(nodeID);
				else if (partWayNodes.size() >= MIN_WAYSIZE){	//is our part way long enough?
					partWays.add(partWayNodes);
					partWayNodes = new Vector<Long>(MAX_WAYSIZE);	//new vector for next part way
				}
				else								
					partWayNodes.clear();	//otherwise delete/empty part way for next loop
			}
		
			//if last nodes builds an part way, store it
			if (partWayNodes.size() >= MIN_WAYSIZE)
				partWays.add(partWayNodes);
		
			//for all ways belongs to the way id
			for (Vector<Long> nodes : partWays){
				//increase outerNodeCount for outer ways
				boundary.getNodeList().get((Long) nodes.firstElement()).increaseOuterNodeCount();
				boundary.getNodeList().get((Long) nodes.lastElement()).increaseOuterNodeCount();
				//increase innerNodeCount for inner way nodes
				for (int i=1; i<nodes.size()-2;i++)
					((OSMNode)boundary.getNodeList().get((Long) nodes.elementAt(i))).increaseInnerNodeCount();
			}

			//insert way into data structure
			if (!partWays.isEmpty())
				boundary.getOSMWays().put(id, new OSMWay(id.longValue(), partWays, maxSpeed, oneWay,
							lanes, motorcar, highway, name, meansOfTransport));
		}
		//if (partWays.size()>1)
		//	System.out.println("Way (id="+id+") split in "+partWays.size()+" part Ways");
	}
	
	/**
	 * handles boundary tag
	 */
	public static void boundsHandler(){
  	  //now we check all Attributes of the bounds
  	  for ( int i = 0; i < parser.getAttributeCount(); i++ ) {
	    	  if (parser.getAttributeLocalName(i).equals("minlat"))
	    		  osmMinLat = Double.valueOf(parser.getAttributeValue(i));
	    	  else if (parser.getAttributeLocalName(i).equals("maxlat"))
	    		  osmMaxLat = Double.valueOf(parser.getAttributeValue(i));
	    	  else if (parser.getAttributeLocalName(i).equals("minlon"))
	    		  osmMinLon = Double.valueOf(parser.getAttributeValue(i));
	    	  else if (parser.getAttributeLocalName(i).equals("maxlon"))
	    		  osmMaxLon = Double.valueOf(parser.getAttributeValue(i));
	    	  else
	    		  //should never be called
	    		  printUnknownAttribute("bounds", i);
  	  }
  	  
  	  //print min,max lat/lon of OSM-file
  	  System.out.println("OSM-file boundary min(Lat/Lon),max(Lat/Lon) : ("+
  			  			  osmMinLat+", "+osmMinLon+"),("+osmMaxLat+", "+osmMaxLon+")");
	}
	
	
	/**
	 * Handles OSM tag 
	 */
	public static void osmHandler(){
		//read OSM general info
		for ( int i=0; i < parser.getAttributeCount(); i++){
			if (parser.getAttributeLocalName(i).equals("version"))
				osmVersion = parser.getAttributeValue(i);
			else if (parser.getAttributeLocalName(i).equals("generator"))
				osmGenerator = parser.getAttributeValue(i);
		}
		//print these info
		System.out.println("Parsing "+xmlFile.getName()+"...\nOSM-Version: "+osmVersion+"\nGenerator: "+osmGenerator);
	}
	
	/**
	 * Handler for XML tags
	 */
	public static void tagHandler(){
		for ( int i = 0; i < parser.getAttributeCount(); i++ ) {
			//as all tags are (k)ey / (v)alue pairs (in this order) we remember the last key and halde the
  		  	//assignment when we find a value.
  		  	if (parser.getAttributeLocalName(i).equals("k"))
  		  		lastkey=parser.getAttributeValue(i);
  		  	//check value
  		  	else if (parser.getAttributeLocalName(i).equals("v")){
  		  		if (lastkey.equals("created_by") || lastkey.equals("visible")){
  		  			//ignore
  		  			lastkey="";
  		  		}	
  		  		else if (lastkey.equals("highway")){
  		  			highway=parser.getAttributeValue(i);
  		  			if (highway.equals("service"))
  		  				//System.out.println("Service route, way id="+id);
  		  				wayNotNeeded=true;	//we don't need this way
  		  		}
  		  		else if (lastkey.equals("motorcar"))
  		  			motorcar=parser.getAttributeValue(i);
  		  		else if (lastkey.equals("oneway"))
  		  			oneWay=Boolean.valueOf(parser.getAttributeValue(i)).booleanValue();
  		  		else if (lastkey.equals("lanes"))
  		  			lanes=parser.getAttributeValue(i);
  		  		else if (lastkey.equals("name"))
  		  			name=parser.getAttributeValue(i);
  		  		else if (lastkey.equals("railway") && parser.getAttributeValue(i).equals("tram")){
  		  			meansOfTransport|=TransformOSMData.TRAM; 	// mark as tram railway
  		  			//System.out.println("tram-trailway Nr."+(++tramTags)+" way id="+id);
  		  		}
  		  		/*
  		  		else if (lastkey.equals("tracks")){
  		  			System.out.println("tracks="+parser.getAttributeValue(i)+" Nr."+(++trackTags)+" id="+id);
  		  		}
  		  		*/
  		  		else if (lastkey.equals("area") && parser.getAttributeValue(i).equals("yes"))
  		  			wayNotNeeded=true;
  		  		/*
  		  		if(primType=="node")
  		  			System.out.print("nodeID: "+id+" "+lastkey+"="+parser.getAttributeValue(i)+"\n");
  		  		*/
  		  	}
	    	else 
	    		//we should never end here
	    		printUnknownAttribute("tag", i);
  	  	}
	}
	
	
	/** 
	 * Check the command line arguments
	 * 
	 * @param args
	 * @return
	 */
	private static boolean checkArgs(String[] args){
		
		//application must be started with argument(s)!
		if (args.length == 0)
			return false;
		
		//current argument position
		int argPos=0;
		
		//first argument must be the XML file, try to open it
		try {
			xmlFile = new MyFile( args[argPos]);
			parser = factory.createXMLStreamReader( new FileInputStream( xmlFile));
		} catch (FileNotFoundException e) {
			System.err.println("Error, xml input file "+args[argPos]+" could not be found.");
			return false;
		} catch (XMLStreamException e) {
			System.err.println("Error, the input File "+args[argPos]+" is no valid XML file.");
			return false;
		}
		
		//next argument
		argPos++;
		
		//check if user wants to extract the whole map
		if (args.length == 1) {
			boundaries.add(new Boundary(TransformOSMData.CAR, xmlFile.getNameWithoutExt()));
		}
		//or tram routes of the whole map
		else if (args[argPos].equals("-tram")){
			boundaries.add(new Boundary(TransformOSMData.TRAM, xmlFile.getNameWithoutExt() + ".tram"));
			argPos++;
		}
		
		//save values during going through all arguments
		String boundaryName="";
		int meansOfTransport=0;
		double minLat=0;
		double minLon=0;
		double maxLat=0;
		double maxLon=0;
		int motOffSet=0;
		int argOffSet=0;
		
		//try to read rest of arguments
		try {
			while (argPos < args.length){
				//set car as default means of transport, clean offset 
				meansOfTransport=TransformOSMData.CAR;
				motOffSet=0;
				
				//is argument a double value? if it is so, parse value
				if (Tools.isDouble(args[argPos])){
					//try to read values
					minLat=Double.parseDouble(args[argPos]);
					minLon=Double.parseDouble(args[argPos+1]);
					maxLat=Double.parseDouble(args[argPos+2]);
					maxLon=Double.parseDouble(args[argPos+3]);
					//set offset
					argOffSet=BOUNDARY_OFFSET;
					//set boundary name
					boundaryName="boundary" + (++boundaryCounter);
				}
				//otherwise it must an gps trace
				else {
					//try to read GPS trace file
					GPSTrace gpsTrace = new GPSTrace(args[argPos]);
					//read gps values
					minLat=gpsTrace.getMinGPSLat();
					minLon=gpsTrace.getMinGPSLon();
					maxLat=gpsTrace.getMaxGPSLat();
					maxLon=gpsTrace.getMaxGPSLon();
					//set offset
					argOffSet=GPS_TRACE_OFFSET;
					//set boundary name
					boundaryName=gpsTrace.getFileNameWithoutEx();
				}
				
				// check if tram routes need to be extracted
				if((argPos+argOffSet < args.length) &&
				   (args[argPos+argOffSet].equals("-tram"))){
						//notice, change boundary name and set offset
						meansOfTransport=TransformOSMData.TRAM;
						boundaryName=boundaryName + ".tram";
						motOffSet=1;
				}
				
				//create new boundary for user inputs
				boundaries.add(new Boundary(minLat, minLon, maxLat, maxLon, MAP_BUFFER, meansOfTransport,boundaryName));
				
				//change current argument position to new unread position
				argPos += (argOffSet+motOffSet);
			}
		} catch(Exception e) {
			e.printStackTrace();
			return false;
		}
		
		//everything is fine
		return true;
	}
	
	/**
	 * Print a small usageinfo
	 */
	private static void printUsage(String[] args){
		//print instruction how to use OSMStAXParser
		System.out.println("OSMStaxParser:\n" +
				"This Parser converts an OpenStreetMap XML File\n"+
				"into a Routing Graph for use with GeoAN JavaME and JavaSE\n"+
				"Peers.\n"+
				"Allowed arguments: OsmXMLfile [GPSTraceFile | minLat minLon maxLat maxLon] [-tram]\n"+
				"You entered following " + args.length + " arguments:" );
		
		//print arguments
		for (String arg : args) System.out.println("\"" + arg + "\"");
	}
	
	/**
	 * prints unknown attributes and their values
	 * 
	 * @param tagName	tag name to which unknown attribute belongs
	 * @param attrIndex	index position of attribute inside tag
	 */
	private static void printUnknownAttribute(String tagName, int attrIndex)
	{
		System.out.println("unknown " + tagName + " attribute: " +
	  						parser.getAttributeLocalName(attrIndex) + 
	  						"=" + parser.getAttributeValue(attrIndex));
	}
	
	/**
	 * Parses the XML file to a dynamic osmData Datastructure
	 * @return
	 */
	private static boolean parseXML(){		
		//start reading xml data via "stream"
		try {
		  parser_loop:
			  
			while ( parser.hasNext() ) 
			{ 
				boolean Systemoutprint = false;		
				  				
				if (Systemoutprint) System.out.println( "Event: " + parser.getEventType() );
				switch (parser.getEventType()) 
				{ 
				case XMLStreamConstants.START_DOCUMENT: 
					if (Systemoutprint) System.out.println( "START_DOCUMENT: " + parser.getVersion() ); 
					break; 

				case XMLStreamConstants.END_DOCUMENT: 
					if (Systemoutprint) System.out.println( "END_DOCUMENT: " ); 
					parser.close(); 
					break; 

				case XMLStreamConstants.NAMESPACE: 
					if (Systemoutprint) System.out.println( "NAMESPACE: " + parser.getNamespaceURI() ); 
					break; 

				case XMLStreamConstants.START_ELEMENT: 
					spacer.append( "  " ); 
					if (Systemoutprint) System.out.println( /*spacer + */ "START_ELEMENT: " + parser.getLocalName() + "\n" ); 

					if (parser.getLocalName()=="node")
					{
						//handle nodes
						nodeHandler();
					}
					else if (parser.getLocalName()=="way"){	 
						//handle ways
						wayHandler();
						if (Systemoutprint) System.out.println("Way!\n");
					}
					else if (parser.getLocalName()=="nd"){
						//handle node references in ways
						referenceHandler();
					}
					else if (parser.getLocalName()=="tag"){
						//handle tags in ways
						tagHandler();
					}
					else if (parser.getLocalName()=="bounds"){
						//handle boundary of the XLM file
						boundsHandler();
					}
					else if (parser.getLocalName()=="osm"){
						//handle general OSM info
						osmHandler();
					}
					else if (parser.getLocalName()=="relation"){
						// stop parsing file, leave while loop, actually we don't this block at the moment
						parser.close();
						break parser_loop;
					}
					break; 

				case XMLStreamConstants.CHARACTERS: 
					if ( ! parser.isWhiteSpace() ){ 
						//System.out.println( spacer + "  CHARACTERS: " + parser.getText() );
						;
					}
					break; 

				case XMLStreamConstants.END_ELEMENT: 
					// Save way
					if (parser.getLocalName()=="way" && !wayNotNeeded)
						addWay(); 
					//System.out.println( spacer + "END_ELEMENT: " + parser.getLocalName() ); 
					spacer.delete(spacer.length()-2, spacer.length()); 
					break; 

				default: 
					break; 
				} 
				parser.next(); 
			}
		} catch (XMLStreamException e) {
			System.err.println("Error parsing XML File!");
			return false;
		} 
		return true;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		Long workingNodeID = new Long(0);
		
		//measure time when program starts
		progStartTime = System.currentTimeMillis();
		
		
		//check arguments
		if (!checkArgs(args)){
			printUsage(args);
			return;
		}
		
		/*
		for(Boundary bound : boundaries){
			System.out.println(bound.getBoundaryName());
			System.out.println(bound.getMinLat());
			System.out.println(bound.getMinLon());
			System.out.println(bound.getMaxLat());
			System.out.println(bound.getMaxLon());
			System.out.println(bound.getMeansOfTransport() + "\n");
		}
		System.exit(0);
		*/
		
		
		//parse OSM data in given file
		if(parseXML())
		{
			//measure interim time
			calculationTime=System.currentTimeMillis() - progStartTime;
			
			for (Boundary boundary : boundaries)
			{
				//notice start time for each loop
				sectionStartTime=System.currentTimeMillis();
				
				//get node list of boundary
				Hashtable<Long, OSMNode> nodeList = boundary.getNodeList();
		
				//create new routing way
				routingWays = new Vector<OSMWay>();
				
				//generate a routing graph out of the OSM Data
				//STEP 1: split up all ways that have inner nodes that are outer nodes for other ways.
				for (Map.Entry<Long, OSMWay> mapEntry : boundary.getOSMWays().entrySet())
				{
					//take an OSM way out of hash table
					OSMWay workingWay = mapEntry.getValue();
				
					//for every part way in this OSM way...
					for(Vector<Long> partWay : workingWay.getOSMPartWays())
					{
						//take node list of part way
						Vector<Long> wayNodes = new Vector<Long>();
					
						//add first node to way
						wayNodes.add(partWay.firstElement());
					
						for (int j=1;j < partWay.size();j++)
						{
							workingNodeID = partWay.elementAt(j);
							//ad the split node to the new wayNodes List
							wayNodes.add(workingNodeID);

							//do we need to split this way at this node?
							//we need to if
							//a) the workingNode (which is an inner Node for this way!) is an outer node for another way
							//b) the workingNode is an inner Node for more than one way.
							if ( ((OSMNode)nodeList.get(workingNodeID)).getOuterNodeCount() > 0 || 
									((OSMNode)nodeList.get(workingNodeID)).getInnerNodeCount() > 1)
							{

								//add the new routingWay to the routingWays Vector
								routingWays.add(new OSMWay(workingWay,wayNodes));
								//create a new wayNodes Vector and add the splitnode to it as next "start" node
								wayNodes = new Vector<Long>();
								wayNodes.add(workingNodeID);
							}
						}
					}
				}

				//STEP 2: count how many nodes and edges the graph will get
				//so we can initialize the graph structure as needed
				Hashtable<Long, Integer> nodeIDtoIndex = new Hashtable<Long, Integer>();

				
				for(OSMWay workingWay : routingWays){
					//if desired mean of transport can use this way, add to graph
					if (workingWay.getMeansOfTransportPermission(boundary.getMeansOfTransport())){
			
						//get start node of way
						Long start = workingWay.getStart();

						//add the start node to the Graph
						if (!nodeIDtoIndex.containsKey(start)){
							nodeIDtoIndex.put(start, new Integer(nodes));
							nodes++;
						}
						
						//get target node of way
						Long target = workingWay.getTarget();
						
						//add the target node to the Graph
						if (!nodeIDtoIndex.containsKey(target)){
							nodeIDtoIndex.put(target, new Integer(nodes));
							nodes++;
						}
						
						//add the Edge to the graph
						if (workingWay.isOneWay())
							edges++;
						else
							edges+=2;		
					}
				}

				membefore=memUsage(); //remember the memory usage before we start building the graph

				//STEP 3: As we now know how many nodes and edges the graph will have we can create the graph structure
				//generate a new routingGraph, clear variables
				Graph routingGraph = new Graph(nodes,edges);
				totalStreetLength=0;
				edges=0;
				nodes=0;

				//add all ways to the graph
				int edgeNumber = 0;
				
				for(OSMWay workingWay : routingWays){
					
					if (workingWay.getMeansOfTransportPermission(boundary.getMeansOfTransport())){

						Vector<Long> workingOSMNodes = workingWay.getOSMPartWay();
						int numOSMNodes = workingOSMNodes.size();
						
						double[] betweenLat = new double[numOSMNodes -2];
						double[] betweenLon = new double[numOSMNodes -2];

						long[] betweenId = new long[numOSMNodes -2];
						
						int[] betweenDistFromEdgeStart = new int[numOSMNodes -2]; 
						
						int distance = 0;
						int nodedist = 0;
							
						double targetlon;
						double targetlat;
						long targatid;
						Long start = workingWay.getStart();
						double startlon = ((OSMNode)nodeList.get(start)).getLongitute();
						double startlat = ((OSMNode)nodeList.get(start)).getLatitude();
						long startid = ((OSMNode)nodeList.get(start)).getId();

						//add the start node to the Graph
						routingGraph.addNode( ((Integer)nodeIDtoIndex.get(start)).intValue() , startid, startlon, startlat);

						for (int j=1; j<numOSMNodes;j++)
						{
							Long target = (Long) workingOSMNodes.elementAt(j);
							targetlon = ((OSMNode)nodeList.get(target)).getLongitute();
							targetlat = ((OSMNode)nodeList.get(target)).getLatitude();
							targatid = ((OSMNode)nodeList.get(target)).getId();
							nodedist = Tools.distance(startlat,startlon,targetlat,targetlon);
							distance += nodedist;
							
							if (j<numOSMNodes -1)
							{
								//add between nodes lat, lon and dist
								betweenLat[j-1] = targetlat;
								betweenLon[j-1] = targetlon;
								
								betweenId[j-1] = targatid;
								
								betweenDistFromEdgeStart[j-1] = distance;
							}

							start = target;
							startlon=targetlon;
							startlat=targetlat;
							startid=targatid;
						}
						
						//add the totalStreetLength
						totalStreetLength+=distance;

						//add the target node to the Graph
						routingGraph.addNode( ((Integer)nodeIDtoIndex.get(start)).intValue() , startid, startlon, startlat);

						//create the new Edge
						Edge newEdge = new Edge(workingWay.getId() ,workingWay.getMaxSpeed(),workingWay.getCarPermission(),workingWay.getLanes(),
								workingWay.getHighwayType(),workingWay.getName(),
								((Integer)nodeIDtoIndex.get(workingWay.getTarget())).intValue(),distance,
								betweenDistFromEdgeStart, betweenLon, betweenLat, betweenId);

						//add the Edge to the graph
						routingGraph.addEdge(((Integer)nodeIDtoIndex.get(workingWay.getStart())).intValue(), 
								edgeNumber++, newEdge);

						//add a second edge in the opposite direction if the way is not oneway
						if (!workingWay.isOneWay()){
							//first we need to reverse the betweenlists and recalculate the distances!
							double[] betweenLatRev = new double[numOSMNodes -2];
							double[] betweenLonRev = new double[numOSMNodes -2];
							
							long[] betweenIdRev = new long[numOSMNodes -2];
							
							int[] betweenDistFromEdgeStartRev = new int[numOSMNodes -2]; 
							
							for (int k=0; k<numOSMNodes -2; k++)
							{
								betweenLatRev[k] = betweenLat[numOSMNodes - 3 - k];
								betweenLonRev[k] = betweenLon[numOSMNodes - 3 - k];
								
								betweenIdRev[k] = betweenId[numOSMNodes - 3 - k];
								
								betweenDistFromEdgeStartRev[k] = distance - betweenDistFromEdgeStart[numOSMNodes - 3 - k];
							}
							
							newEdge = new Edge(workingWay.getId(), workingWay.getMaxSpeed(),workingWay.getCarPermission(),workingWay.getLanes(),
									workingWay.getHighwayType(),workingWay.getName(),
									((Integer)nodeIDtoIndex.get(workingWay.getStart())).intValue(),distance,
									betweenDistFromEdgeStartRev, betweenLonRev, betweenLatRev, betweenIdRev);
							//add the Edge to the graph
							routingGraph.addEdge(((Integer)nodeIDtoIndex.get(workingWay.getTarget())).intValue(), 
									edgeNumber++, newEdge);
						}
					}
				}
				
				//how long took calculation time? add to overall calculation time
				calculationTime += System.currentTimeMillis() - sectionStartTime;

				//normalize the coordinates of all data in the routinggraph to the range of 0.0 - 100.0
				
				//FIXME: why normalization?
				//routingGraph.normalize(0.0, 100.0);

				//FIXME: instead of normalization we now add the bounds the osm file tells us
				routingGraph.setDimensions(osmMinLon, osmMaxLon, osmMinLat, osmMaxLat);

				System.out.println("\nOverall only "+routingGraph.nodeCount()+" of "+nodeList.size()+" nodes are "+
				"used for the routinggraph.");
				System.out.println("But we started with "+boundary.getOSMWays().size()+" undirected edges and ended with "
						+routingGraph.edgeCount()+" directed edges.");
				System.out.println("This Graph covers a streetlength (undirected edges) of "+totalStreetLength+ " meters.");

				//just for me ;)
				memafter = memUsage(); //check the memusage after building the graph
				System.out.println("For this graph the runtime uses: "+(memafter-membefore)+" MB");

				String s = "";
				
				System.out.println("Writing the " + boundary.getBoundaryName() + ".routing.graph.small to disk!");
				//and now we try to write the graph to a file
				File f= new File(boundary.getBoundaryName() + ".routing.graph.small");
				
				s = boundary.getBoundaryName() + ".routing.graph.small";
				
				s = s + "";
				
				//measure writing time
				sectionStartTime = System.currentTimeMillis();
				
				try {
					//delete the file if it already exists
					if (f.exists()){
						f.delete();
					}
					//create a new file
					f.createNewFile();
					//open a new fileoutputstream and a corresponding dataoutputstream on the fileoutputstream
					FileOutputStream fos = new FileOutputStream(f);
					DataOutputStream dos = new DataOutputStream(fos);

					//serialize the routingGraph
					
					int ss = dos.size();
					
					routingGraph.toDataStream(dos,false);
					dos.flush();
					dos.close();
					fos.flush();
					fos.close();
				} catch (IOException e) {
					System.out.println("Error writing routing.graph.small");
				}

				System.out.println("Writing the " + boundary.getBoundaryName() + ".routing.graph.large to disk!");
				//and now we try to write the graph to a file
				f= new File(boundary.getBoundaryName() + ".routing.graph.large");

				try {
					//delete the file if it already exists
					if (f.exists()){
						f.delete();
					}
					//create a new file
					f.createNewFile();
					//open a new file output stream and a corresponding data output stream on the fileoutputstream
					FileOutputStream fos = new FileOutputStream(f);
					DataOutputStream dos = new DataOutputStream(fos);

					//serialize the routingGraph
					routingGraph.toDataStream(dos,true);
					dos.flush();
					dos.close();
					fos.flush();
					fos.close();
				} catch (IOException e1) {
					System.out.println("Error writing routing.graph.large");
				}
				
				//measure time took to write
				writingTime += System.currentTimeMillis() - sectionStartTime; 
				
				System.out.println("Writing done!");
			}	
			
			//measure time when finished
			runtimeLength = System.currentTimeMillis() - progStartTime;
			
			// print all measured times
			System.out.println("\nCalculation runtime: "+ ((float) calculationTime / 1000) +"sec");
			System.out.println("Writing time: " + ((float) writingTime / 1000) +" sec");
			System.out.println("Overall runtime: "+ ((float) runtimeLength / 1000) +" sec");	
		}
	}
}
