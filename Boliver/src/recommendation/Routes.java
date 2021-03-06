package recommendation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import db.DBConnection;
import db.DBConnectionFactory;
import entity.BaseStatus;
import entity.DistanceMatrix;
import entity.GeoLocation;
import entity.robotInfo;
import external.CalDrone;
import external.GoogleAPI;
import external.Haversince;

public class Routes {
	public static JSONObject calculateRoutes(String origin, String destination) {

		// Pre-process address before passing them to GoogleAPI
		String originPlus = AddrAddPlus.convert(origin);
		String destinationPlus = AddrAddPlus.convert(destination);

		// Convert address into lat,lon
		GeoLocation encoding_origin = GoogleAPI.getGeoEncoding(originPlus);
		GeoLocation encoding_dest = GoogleAPI.getGeoEncoding(destinationPlus);

		// Get status on available robots in each base
		DBConnection conn = DBConnectionFactory.getConnection(); // Connect to database
		List<BaseStatus> baseStatus = conn.getBaseStatus(); // get status for each base on its drone and ground robot
															// availability, and each of their addresses

		List<GeoLocation> robotBasesAvail = new ArrayList<>();
		List<GeoLocation> droneBasesAvail = new ArrayList<>();

		for (BaseStatus base : baseStatus) { // iterate over all bases and add them to the two lists declared above
												// given their status on bot availability
			if (base.getGroundstatus()) {
				robotBasesAvail.add(new GeoLocation(Double.parseDouble(base.getLat()),
						Double.parseDouble(base.getLon()), base.getAddress()));
			}
			if (base.getDroneStatus()) {
				droneBasesAvail.add(new GeoLocation(Double.parseDouble(base.getLat()),
						Double.parseDouble(base.getLon()), base.getAddress()));
			}
		}

		// Get the closest ground robot base and Distance Matrix based on travel time.
		GeoLocation ClosestGrobotBase = null; // Select base based on the distance and availability.
		DistanceMatrix dMatrixOfCGB = null;
		for (GeoLocation base : robotBasesAvail) {
			DistanceMatrix dm = GoogleAPI.getDistanceMatrix(base, encoding_origin).get(0);

			if (ClosestGrobotBase == null) {
				ClosestGrobotBase = base;
				dMatrixOfCGB = dm;
			} else {
				if (dm.getDuration_seconds() < dMatrixOfCGB.getDuration_seconds()) {
					dMatrixOfCGB = dm;
					ClosestGrobotBase = base;
				}
			}
		}

		// Get the closest drone base and Distance Matrix based on travel time.
		GeoLocation ClosestDroneBase = null; // Select base based on the distance and availability.
		double distanceOfCDB = 0.0;
		for (GeoLocation base : droneBasesAvail) {
			double distance = Haversince.calculateDistance(base, encoding_origin);

			if (ClosestDroneBase == null) {
				ClosestDroneBase = base;
				distanceOfCDB = distance;
			} else {
				if (distance < distanceOfCDB) {
					distanceOfCDB = distance;
					ClosestDroneBase = base;
				}
			}
		}

		// Get travel distance and time for groundRobots from pickup location to
		// destination
		DistanceMatrix groundRobot = GoogleAPI.getDistanceMatrix(encoding_origin, encoding_dest).get(0);
		// Get travel distance and time for drones pickup location to destination
		Map<String, Double> drone = CalDrone.calculateDrone(encoding_origin, encoding_dest);
		//
		double dronePrice = -0.1;
		if (distanceOfCDB != 0.0) {
			dronePrice = robotInfo.getDroneRate() * (drone.get("distance") + distanceOfCDB);
		}

		//
		double groundPrice = -0.1;
		if (dMatrixOfCGB != null) {
			groundPrice = robotInfo.getGroundRobotRate()
					* ((groundRobot.getDistance_meters() + dMatrixOfCGB.getDistance_meters()) / 1609.344);
		}

		System.out.println("returning object"); // <--- to show in console that I have successfully reached this point

		JSONObject mainObj = new JSONObject();
		JSONObject addr = new JSONObject();
		JSONObject droneObj = new JSONObject();
		JSONObject groundBotObj = new JSONObject();

		try {
			addr.put("origin", origin).put("destination", destination);
			droneObj.put("travel_time", drone.get("time")) // time unit = minutes
					.put("cost", dronePrice == -0.1 ? "nope" : dronePrice)
					.put("pickup_time", ClosestDroneBase == null ? "nope" : distanceOfCDB / drone.get("speed"))
					.put("travel_distance", distanceOfCDB == 0.0 ? "nope" : drone.get("distance") + distanceOfCDB) // distance
																													// unit
																													// =
																													// miles
					.put("avail_status", ClosestDroneBase == null ? "no" : "yes")
					.put("base", ClosestDroneBase == null ? "no available drone, try again later"
							: ClosestDroneBase.getAddress());

			groundBotObj.put("travel_time", groundRobot.getDuration_seconds() / 60) // time unit = minutes
					.put("cost", groundPrice == -0.1 ? "nope" : groundPrice)
					.put("pickup_time",
							ClosestGrobotBase == null ? "no available groundBot, try again later"
									: dMatrixOfCGB.getDuration_seconds() / 60)
					.put("travel_distance",
							dMatrixOfCGB == null ? "nope"
									: (groundRobot.getDistance_meters() + dMatrixOfCGB.getDistance_meters()) / 1609.344) // distance
																															// unit
																															// =
																															// miles
					.put("avail_status", ClosestGrobotBase == null ? "no" : "yes")
					.put("base", ClosestGrobotBase == null ? "no available groundBot, try again later"
							: ClosestGrobotBase.getAddress());

			mainObj.put("DeliveryAddress", addr).put("Drone", droneObj).put("GroundBot", groundBotObj);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return mainObj;
	}

}
