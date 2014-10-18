package wiseguys.radar.conn;

import wiseguys.radar.R;
import wiseguys.radar.helpers.RadarHelper;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

public class GPSHelper {
	
	private Context context;
	
	private Location lastKnownLocation;
	private boolean GPSSetup;
	private LocationManager locationManager;
	private LocationListener locationListener;

    public static double lastGoodLat;
    public static double lastGoodLong;
    public static double cityLat;
    public static double cityLong;

	
	public GPSHelper(Context c) {
		context = c;
		lastKnownLocation = null;
        GPSSetup = false;
	}
	
	public Location getLastLocation() {
		return lastKnownLocation;
	}
	
	/**
     * Warn if they have a disabled GPS
     */
    private void alert() {
    	    final AlertDialog.Builder builder = new AlertDialog.Builder(context);
    	    builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
    	           .setCancelable(false)
    	           .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
    	               public void onClick(final DialogInterface dialog, final int id) {
    	                   context.startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
    	                   setup();
    	               }
    	           })
    	           .setNegativeButton("No", new DialogInterface.OnClickListener() {
    	               public void onClick(final DialogInterface dialog, final int id) {
    	                    dialog.cancel();
    	               }
    	           });
    	    final AlertDialog alert = builder.create();
    	    alert.show();
	}
    
    public void setup() {
		// Acquire a reference to the system Location Manager
		locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

		if (!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) &&
		    !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			//No providers enabled
			alert();
		}

		// Define a listener that responds to location updates
		locationListener = new LocationListener() {

		    public void onLocationChanged(Location location) {
		    	if (newLocationIsBetter(location)) {
		    		lastKnownLocation = location;
                    RadarHelper.latestLocation = location;
		    	}
		    }

			public void onStatusChanged(String provider, int status, Bundle extras) {

			}

		    public void onProviderEnabled(String provider) {
	    		if (!locationManager.getProviders(true).contains(provider)) {
	    			locationManager.requestLocationUpdates(provider, 0, 0, this);
	    		}
	    		GPSSetup = true;
		    }

		    public void onProviderDisabled(String provider) {
		    	if (provider.equals(LocationManager.NETWORK_PROVIDER)) {
		    		GPSSetup = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		    	} else if (provider.equals(LocationManager.GPS_PROVIDER)) {
		    		GPSSetup = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		    	}
		    }
		};

		// Register the listeners with the Location Manager to receive location updates
		if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, RadarHelper.TEN_MINUTES, 2000, locationListener);
		}

		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, RadarHelper.TEN_MINUTES, 2000, locationListener);
		}

		//Setup our last known good location
		lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        lastGoodLat = lastKnownLocation.getLatitude();
        lastGoodLong = lastKnownLocation.getLongitude();
		
		GPSSetup = !locationManager.getProviders(true).isEmpty();
    }

    /**
     * Compare new GPS location to old and update if necessary
     * @param location new location we compare our old to
     * @return true if we need an update
     */
	public boolean newLocationIsBetter(Location location) {
		//Always update a null
		if (lastKnownLocation == null) {
			return true;
		}

	    //Check times
	    long timeDelta = location.getTime() - lastKnownLocation.getTime();
	    boolean isSignificantlyNewer = timeDelta > RadarHelper.TEN_MINUTES;
	    boolean isSignificantlyOlder = timeDelta < -RadarHelper.TEN_MINUTES;
	    boolean isNewer = timeDelta > 0;

	    // If it's been more than two minutes since the current location, use the new location
	    // because the user has likely moved
	    if (isSignificantlyNewer) {
	        return true;
	    // If the new location is more than two minutes older, it must be worse
	    } else if (isSignificantlyOlder) {
	        return false;
	    }

	    // Check whether the new location fix is more or less accurate
	    int accuracyDelta = (int) (location.getAccuracy() - lastKnownLocation.getAccuracy());
	    boolean isLessAccurate = accuracyDelta > 0;
	    boolean isMoreAccurate = accuracyDelta < 0;
	    boolean isSignificantlyLessAccurate = accuracyDelta > 2000; //2KM Difference

	    // Check if the old and new location are from the same provider
	    boolean isFromSameProvider = location.getProvider().equals( lastKnownLocation.getProvider() );

	    // Determine location quality using a combination of timeliness and accuracy
	    if (isMoreAccurate) {
	        return true;
	    } else if (isNewer && !isLessAccurate) {
	        return true;
	    } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
	        return true;
	    }
	    return false;
	}

	public void disable() {
 		locationManager.removeUpdates(locationListener);
 		GPSSetup = false;
	}
	
	public boolean ready() {
		if (!GPSSetup) {
			setup();
		}
		return GPSSetup;
	}

	public String findClosestCity(Location lastKnownLocation) {	
		if (lastKnownLocation == null) {
			return null;
		}
		
		String[] radarLats = context.getResources().getStringArray(R.array.city_lat_vals);
		String[] radarLongs = context.getResources().getStringArray(R.array.city_long_vals);
		String[] radarCodes = context.getResources().getStringArray(R.array.radar_codes);
		String[] radarNames = context.getResources().getStringArray(R.array.radar_cities);
		
		String closest = null;
		String closestName = null;
		float closestDist = Float.MAX_VALUE;
		
		for (int i = 0; i < radarLats.length; i++) {
			try {
				double lat = Double.parseDouble(radarLats[i]);
				double lon = Double.parseDouble(radarLongs[i]);
				
				Location currCityLoc = new Location("WiseRadar");
				currCityLoc.setLatitude(lat);
				currCityLoc.setLongitude(lon);
				
				float currDist = lastKnownLocation.distanceTo(currCityLoc);
				if (currDist < closestDist) {
					closestDist = currDist;
					closestName = radarNames[i];
					closest = radarCodes[i];
                    cityLat = lat;
                    cityLong = lon;
				}				
			} catch (NumberFormatException e) {
				return null;
			}
		}
		
		Toast.makeText(context, "GPS has selected '" + closestName + "'", Toast.LENGTH_LONG).show();
		
		return closest;
	}
}
