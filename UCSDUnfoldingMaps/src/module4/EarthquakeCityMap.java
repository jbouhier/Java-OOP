package module4;

import java.util.*;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.GeoJSONReader;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.AbstractShapeMarker;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.MultiMarker;
import de.fhpotsdam.unfolding.providers.GeoMapApp;
import de.fhpotsdam.unfolding.providers.Google;
import de.fhpotsdam.unfolding.providers.MBTilesMapProvider;
import de.fhpotsdam.unfolding.utils.MapUtils;
import parsing.ParseFeed;
import processing.core.PApplet;

/** EarthquakeCityMap
 * An application with an interactive map displaying earthquake data.
 * Author: UC San Diego Intermediate Software Development MOOC team
 * @author Your name here
 * Date: July 17, 2015
 * */
public class EarthquakeCityMap extends PApplet {

	// We will use member variables, instead of local variables, to store the data
	// that the setUp and draw methods will need to access (as well as other methods)
	// You will use many of these variables, but the only one you should need to add
	// code to modify is countryQuakes, where you will store the number of earthquakes
	// per country.

	// You can ignore this.  It's to get rid of eclipse warnings
	private static final long serialVersionUID = 1L;

	// IF YOU ARE WORKING OFFILINE, change the value of this variable to true
	private static final boolean offline = false;

	/** This is where to find the local tiles, for working without an Internet connection */
	public static String mbTilesString = "blankLight-1-3.mbtiles";



	//feed with magnitude 2.5+ Earthquakes
	private String earthquakesURL = "https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/2.5_week.atom";

	// The files containing city names and info and country names and info
	private String cityFile = "city-data.json";
	private String countryFile = "countries.geo.json";

	// The map
	private UnfoldingMap map;

	// Markers for each city
	private List<Marker> cityMarkers;
	// Markers for each earthquake
	private List<Marker> quakeMarkers;

	// A List of country markers
	private List<Marker> countryMarkers;

	public void setup() {
		size(900, 700, OPENGL);

		if (offline) {
		    map = new UnfoldingMap(this, 200, 50, 650, 600, new MBTilesMapProvider(mbTilesString));
			earthquakesURL = "2.5_week.atom";  // The same feed, but saved August 7, 2015
		}
		else
			map = new UnfoldingMap(this, 200, 50, 650, 600, new GeoMapApp.TopologicalGeoMapProvider());
//			map = new UnfoldingMap(this, 200, 50, 650, 600, new Google.GoogleMapProvider());


		MapUtils.createDefaultEventDispatcher(this, map);

		// FOR TESTING: Set earthquakesURL to be one of the testing files by uncommenting
		// one of the lines below.  This will work whether you are online or offline
		// earthquakesURL = "test1.atom";
		// earthquakesURL = "test2.atom";

		// WHEN TAKING THIS QUIZ: Uncomment the next line
		//earthquakesURL = "quiz1.atom";


		// (2) Reading in earthquake data and geometric properties
	    //     STEP 1: load country features and markers
		List<Feature> countries = GeoJSONReader.loadData(this, countryFile);
		countryMarkers = MapUtils.createSimpleMarkers(countries);

		//     STEP 2: read in city data
		List<Feature> cities = GeoJSONReader.loadData(this, cityFile);
		cityMarkers = new ArrayList<Marker>();
		for (Feature city : cities) {
		  cityMarkers.add(new CityMarker(city));
		}

		//     STEP 3: read in earthquake RSS feed
	    List<PointFeature> earthquakes = ParseFeed.parseEarthquake(this, earthquakesURL);
	    quakeMarkers = new ArrayList<Marker>();

	    for (PointFeature feature : earthquakes) {
		  if (isLand(feature)) quakeMarkers.add(new LandQuakeMarker(feature)); // LandQuakes
		  else quakeMarkers.add(new OceanQuakeMarker(feature)); // OceanQuakes
	    }

	    // printQuakes();

	    map.addMarkers(quakeMarkers);
	    map.addMarkers(cityMarkers);

	}  // End setup


	public void draw() {
		background(216, 230, 255); // light purple
		map.draw();
		addKey();
	}


	// Draw key in GUI
	private void addKey() {
		fill(255, 250, 240);
		rect(25, 50, 150, 350);

		fill(0); // black
		textAlign(LEFT, CENTER);
		textSize(12);
		text("Earthquake Key", 50, 75);

		fill(color(255, 0, 0)); // red
		triangle(50, 120, 45, 130, 55, 130);
		fill(color(255, 255, 255)); // white
		ellipse(50, 154, 15, 15);
		rect(43, 185, 15, 15);
		ellipse(50, 355, 15, 15);
		stroke(0);
		strokeWeight(2);
		line(40, 345, 60, 365);
		line(60, 345, 40, 365);
		fill(color(255, 255, 0)); // yellow
		ellipse(50, 265, 15, 15);
		fill(color(0, 0, 255)); // blue
		ellipse(50, 295, 15, 15);
		fill(color(255, 0, 0)); // red
		ellipse(50, 325, 15, 15);

		fill(color(0, 0, 0));
		text("City Marker", 75, 124);
		text("Land Quake", 75, 154);
		text("Ocean Quake", 75, 192);
		text("Size ~ Magnitude", 45, 225);
		text("Shallow", 75, 265);
		text("Intermediate", 75, 295);
		text("Deep", 75, 325);
		text("Past day", 75, 354);
	}


	// Checks whether this quake occurred on land.  If it did, it sets the
	// "country" property of its PointFeature to the country where it occurred
	// and returns true.  Notice that the helper method isInCountry will
	// set this "country" property already.  Otherwise it returns false.
	private boolean isLand(PointFeature earthquake) {

		// Loop over all the country markers.
		// For each, check if the earthquake PointFeature is in the
		// country in m.  Notice that isInCountry takes a PointFeature
		// and a Marker as input.
		// If isInCountry ever returns true, isLand should return true.
		for (Marker m : countryMarkers) {
			if (isInCountry(earthquake, m)) return true;
		}

		// not inside any country
		return false;
	}


	private void printQuakes() {
		LinkedHashMap<String, Integer> quakesByCountry = new LinkedHashMap<String, Integer>();
		int quakesCount;
		String countryName;
		String quakeCountry;
		boolean isOnLand;

		for (Marker cm : countryMarkers) {
			quakesCount = 0;
			countryName = (String) cm.getProperty("name");

			for (Marker qm : quakeMarkers) {
				EarthquakeMarker m = (EarthquakeMarker) qm;
				isOnLand = m.isOnLand();
				quakeCountry = (String) qm.getProperty("country");

				if (isOnLand && countryName.equals(quakeCountry)) quakesCount++;
			}
			quakesByCountry.put(countryName, quakesCount);
		}

		quakesCount = 0;

		for (Marker cm : quakeMarkers) {
			EarthquakeMarker m = (EarthquakeMarker) cm;
			if (!m.isOnLand()) quakesCount++;
			quakesByCountry.put("OCEAN QUAKES", quakesCount);
		}

		for (Map.Entry<String, Integer> entry : quakesByCountry.entrySet()) {
			String key = entry.getKey();
			Integer value = entry.getValue();
			if (value >= 1) System.out.println(key + ": " + value);
		}
	}


	// Test whether a given earthquake is in a given country
	// This will also add the country property to the properties of the earthquake
	// feature if it's in one of the countries.
	// You should not have to modify this code
	private boolean isInCountry(PointFeature earthquake, Marker country) {
		// getting location of feature
		Location checkLoc = earthquake.getLocation();

		// some countries represented it as MultiMarker
		// looping over SimplePolygonMarkers which make them up to use isInsideByLoc
		if ((country.getClass()) == MultiMarker.class) {

			// looping over markers making up MultiMarker
			for (Marker marker : ((MultiMarker)country).getMarkers()) {
				// checking if inside
				if (((AbstractShapeMarker)marker).isInsideByLocation(checkLoc)) {
					earthquake.addProperty("country", country.getProperty("name"));
					// return if is inside one
					return true;
				}
			}
		}
		// check if inside country represented by SimplePolygonMarker
		else if (((AbstractShapeMarker)country).isInsideByLocation(checkLoc)) {
			earthquake.addProperty("country", country.getProperty("name"));
			return true;
		}
		return false;
	}

}
