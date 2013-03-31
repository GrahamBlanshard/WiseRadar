package wiseguys.radar.ui;

import wiseguys.radar.R;
import wiseguys.radar.RadarHelper;
import wiseguys.radar.RadarLoader;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

        refresh();
    }
    
    /**
     * Re-receive the images on command
     */
    private void refresh() {
    	checkAndCancelUpdate();
    	
        selectedRadarCode = sharedPrefs.getString("pref_radar_code", "Pick a Radar");
        selectedDuration = sharedPrefs.getString("pref_radar_dur", "short");
        TextView radarName = (TextView)findViewById(R.id.radarName);
              
        String selectedRadarName = RadarHelper.codeToName(selectedRadarCode,this.getBaseContext());
        radarName.setText(selectedRadarName);
        
	    //Put it all together
	    ImageView sImage = (ImageView)findViewById(R.id.radarImage);
        
        loader = new RadarLoader(this.getBaseContext(),this.getResources(),sImage,radarName);
        loader.execute(selectedRadarCode,selectedDuration);
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
}