package wiseguys.radar.ui;

import wiseguys.radar.R;
import wiseguys.radar.RadarHelper;
import wiseguys.radar.RadarLoader;
import wiseguys.radar.conn.GPSHelper;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class MenuActivity extends Activity {
	
	private SharedPreferences sharedPrefs;
	private String selectedRadarCode;
	private String selectedDuration;
	private RadarLoader loader;
	private GPSHelper gps;
	private boolean useGPS;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu);
        
        //Shared Preferences -- Needed to pass data between activities.
        SharedPreferences settings = getSharedPreferences("APP_PREFS", 0);
        SharedPreferences.Editor spEditor = settings.edit();
        String versionID = this.getBaseContext().getString(R.string.version);
        spEditor.putString("APP_NAME", "WiseRadar" + versionID);
        spEditor.commit();
        
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        
        Button pickButton = (Button) findViewById(R.id.prefButton);        
        pickButton.setOnClickListener(new View.OnClickListener() {        				
			@Override
			public void onClick(View v) {
				Intent myIntent = new Intent(v.getContext(), PrefActivity.class);
				startActivity(myIntent);
			}
		});
        
        Button refreshButton = (Button) findViewById(R.id.refreshButton);        
        refreshButton.setOnClickListener(new View.OnClickListener() {        				
			@Override
			public void onClick(View v) {
				refresh();
			}
		});
        
        useGPS = false;
    }
    
    /**
     * On quit print log message
     */
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    }
    
    /**
     * On halt, print log message
     */
    @Override
    protected void onStop() {
	     super.onStop();
    }
    
    /**
     * Loads when returning to this screen
     */
    @Override
    protected void onResume() {
	    super.onResume();
	    
	    useGPS = sharedPrefs.getBoolean("gps",false);
	    
	    if (useGPS) {
	    	if (gps == null) {
	    		gps = new GPSHelper(this);
	    	}
	    	
	    	if (!gps.ready()) {
	    		gps.setup();
	    	}
	    }
	    
        refresh();
    }
    
    /**
     * Re-receive the images on command
     */
    private void refresh() {
    	
    	TextView radarName = (TextView)findViewById(R.id.radarName);
	    ImageView sImage = (ImageView)findViewById(R.id.radarImage);
    	
	    //Verify we have a network
    	if (!validConnection()) {    		
    		radarName.setText("No valid Networks");
    		sImage.setImageDrawable(null);
    		return;
    	}
    	
    	String codeToUse = null;
    			
    	checkAndCancelUpdate();
    	selectedRadarCode = sharedPrefs.getString("pref_radar_code", "Pick a Radar");
    	codeToUse = selectedRadarCode;
    	
    	if (useGPS) {
    		if (!gps.ready()) {    			
				Log.w("WiseRadar","System Error setting up GPS, using pre-selected value");
			} else {    		
	    		//Assume we have a valid GPS setup now.
	    		codeToUse = gps.findClosestCity(gps.getLastLocation());
	    		
	    		if (codeToUse == null) {
	    			Log.w("WiseRadar","Unable to retrieve last known good location");
	    			codeToUse = selectedRadarCode;
	        	}
			}
    	}
    	
        selectedDuration = sharedPrefs.getString("pref_radar_dur", "short");        
        String selectedRadarName = RadarHelper.codeToName(codeToUse,this.getBaseContext());
        
        if (selectedRadarName == null) {
        	radarName.setText("No Location Selected");
        	sImage.setImageDrawable(null);
        	return;
        }
        
        radarName.setText(selectedRadarName);
        
	    //Put it all together      
        loader = new RadarLoader(this.getBaseContext(),this.getResources(),sImage,radarName);
        loader.execute(codeToUse,selectedDuration);
    }

	/**
     * Checks the status of our loader and cancels it in the situation it is still running
     */
    private void checkAndCancelUpdate() {
    	if (loader == null) {
    		return; //Nothing to cancel
    	}
    	
    	if (loader.getStatus() == AsyncTask.Status.RUNNING) {
	    	 loader.cancel(true);
	    }
    }
    
    @Override
    protected void onPause() {
    	super.onPause();	     
	    checkAndCancelUpdate();
	    
	    //Remove GPS if used
	    if (useGPS && gps != null) {
	    	gps.disable();
	    }
    }
    
    @Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
    		Intent intent = new Intent();
            setResult(RESULT_OK, intent);
            
            finish();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MENU) {
        	return true;
        }
        return false;
    }
    
    private boolean validConnection() {
	    boolean status=false;
	    try{
	        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	        NetworkInfo networkInfo0 = connManager.getNetworkInfo(0);
	        NetworkInfo networkInfo1 = connManager.getNetworkInfo(1);
	        
	        status = ((networkInfo0 != null && networkInfo0.getState()==NetworkInfo.State.CONNECTED) ||
	        		 (networkInfo1 != null && networkInfo1.getState()==NetworkInfo.State.CONNECTED));	       
	    }catch(Exception e){
	        Log.e("WiseRadar", "Exception while validating network: " + e.getLocalizedMessage());  
	        return false;
	    }
	    
	    return status;
    }
}