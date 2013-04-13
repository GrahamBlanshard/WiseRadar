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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
        
        useGPS = false;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());   
    }

    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_quit:
            	quit();
                return true;
            case R.id.menu_about:
                showAbout();
                return true;
            case R.id.menu_refresh:
            	refresh();
                return true;
            case R.id.menu_preferences:
            	showPreferences();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
     * Creates our menu
     */
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.radar_menu, menu);
        return true;
    }
    
    /**
     * Activity Pause -- Cancel any updates and disable GPS
     */
    @Override
    protected void onPause() {
    	super.onPause();	     
	    checkAndCancelUpdate();
	    
	    //Remove GPS if used
	    if (useGPS && gps != null) {
	    	gps.disable();
	    }
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
    	selectedRadarCode = sharedPrefs.getString("pref_radar_code", "new");
    	codeToUse = selectedRadarCode;
    	
    	if (selectedRadarCode.equals("new")) {
    		radarName.setText("Please set your preferences!");
        	sImage.setImageDrawable(null);
        	return;
    	}
    	
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
	public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
    		quit();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MENU) {
        	openOptionsMenu();
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
    
    private void showAbout() {
    	Intent myIntent = new Intent(this.getBaseContext(), AboutActivity.class);
		startActivity(myIntent);  	 
    }
    
    private void showPreferences() {
    	Intent myIntent = new Intent(this.getBaseContext(), PrefActivity.class);
		startActivity(myIntent);
    }
    
    /**
     * Closes our application
     */
    private void quit() {
    	Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        
        finish();
    }
}