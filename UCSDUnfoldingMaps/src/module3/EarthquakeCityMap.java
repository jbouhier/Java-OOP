package module3;

//Java utilities libraries
import java.util.ArrayList;
//import java.util.Collections;
//import java.util.Comparator;
import java.util.List;

//Processing library
import de.fhpotsdam.unfolding.providers.GeoMapApp;
import processing.core.PApplet;

//Unfolding libraries
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.marker.SimplePointMarker;
import de.fhpotsdam.unfolding.providers.MBTilesMapProvider;
import de.fhpotsdam.unfolding.utils.MapUtils;

//Parsing library
import parsing.ParseFeed;

/** EarthquakeCityMap
 * An application with an interactive map displaying earthquake data.
 * Author: UC San Diego Intermediate Software Development MOOC team
 * @author Your name here
 * Date: July 17, 2015
 * */
public class EarthquakeCityMap extends PApplet {

	// You can ignore this.  It's to keep eclipse from generating a warning.
	private static final long serialVersionUID = 1L;

	// IF YOU ARE WORKING OFFLINE, change the value of this variable to true
	private static final boolean offline = false;
	
	// Less than this threshold is a light earthquake
	public static final float THRESHOLD_MODERATE = 5;
	// Less than this threshold is a minor earthquake
	public static final float THRESHOLD_LIGHT = 4;

	/** This is where to find the local tiles, for working without an Internet connection */
	public static String mbTilesString = "blankLight-1-3.mbtiles";
	
	// The map
	private UnfoldingMap map;
	
	//feed with magnitude 2.5+ Earthquakes
	private String earthquakesURL = "https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/2.5_week.atom";

	
	public void setup() {
		size(950, 600, OPENGL);

		if (offline) {
		    map = new UnfoldingMap(this, 200, 50, 700, 500, new MBTilesMapProvider(mbTilesString));
		    earthquakesURL = "2.5_week.atom"; 	// Same feed, saved Aug 7, 2015, for working offline
		}
		else {
			map = new UnfoldingMap(this, 200, 50, 700, 500, new GeoMapApp.TopologicalGeoMapProvider());
			// IF YOU WANT TO TEST WITH A LOCAL FILE, uncomment the next line
			//earthquakesURL = "2.5_week.atom";
		}

	    map.zoomToLevel(2);
	    MapUtils.createDefaultEventDispatcher(this, map);	
			
	    // The List you will populate with new SimplePointMarkers
	    List<Marker> markers = new ArrayList<Marker>();

	    //Use provided parser to collect properties for each earthquake
	    //PointFeatures have a getLocation method
	    List<PointFeature> earthquakes = ParseFeed.parseEarthquake(this, earthquakesURL);

	    // Creates a new SimplePointMarker for each PointFeature
		// in earthquakes, then add them to the List markers.
		for (PointFeature pf : earthquakes) {
			markers.add(createMarker(pf));
		}
	    
	    // Add the markers to the map so that they are displayed
	    map.addMarkers(markers);
	}
		
	/**
	 * Takes in an earthquake feature
	 * returns a SimplePointMarker for that earthquake.
	 *
	 * @param feature Earthquake data
	 * @return SimplePointMarker to display earthquake on the map with map.addMarkers(marker);
	*/
	private SimplePointMarker createMarker(PointFeature feature)
	{
		int color;
		float radius;

		// To print all of the features in a PointFeature (so you can see what they are)
		// uncomment the line below.  Note this will only print if you call createMarker 
		// from setup
		//System.out.println(feature.getProperties());

		// Create a new SimplePointMarker at the location given by the PointFeature
		SimplePointMarker marker = new SimplePointMarker(feature.getLocation());
		
		Object magObj = feature.getProperty("magnitude");
		float mag = Float.parseFloat(magObj.toString());

		// Set marker's color and size according to magnitude
		if (mag >= THRESHOLD_MODERATE) {
			color = color(255, 0, 0); // Red
			radius = 20f;
		} else if (mag >= THRESHOLD_LIGHT) {
			color = color(255, 210, 0); // Yellow
			radius = 12f;
		} else {
			color = color(0, 35, 255); // Blue
			radius = 9f;
		}

		marker.setColor(color);
		marker.setRadius(radius);

	    return marker;
	}
	
	public void draw() {
	    background(216, 230, 255); // light purple
	    map.draw();
	    addKey();
	}

	/**
	 * Display map key
	 */
	private void addKey() 
	{
		fill(255, 255, 255); // White
		noStroke();
		rect(25, 50, 150, 300);

		// Title
		fill(0); // Black
		text("Earthquake Key", 50, 80);

		// Medium magnitude
		fill(255, 0, 0); // Red
		ellipse(50, 120, 20, 20);
		fill(0); // Black
		text("5.0+ Magnitude", 70, 125);

		// Light magnitude
		fill(255, 210, 0); // Yellow
		ellipse(50, 170, 12, 12);
		fill(0); // Black
		text("5.0+ Magnitude", 70, 175);

		// Tiny magnitude
		fill(0, 35, 255); // Blue
		ellipse(50, 220, 9, 9);
		fill(0); // Black
		text("Below 4.0", 70, 225);
	}
}
