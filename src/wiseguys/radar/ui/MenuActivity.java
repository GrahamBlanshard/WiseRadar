package wiseguys.radar.ui;

import wiseguys.radar.R;
import wiseguys.radar.RadarHelper;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MenuActivity extends Activity {
	
	private SharedPreferences sharedPrefs;
	
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
        
        /* Loads the radar data  */
        Button goButton = (Button) findViewById(R.id.launchButton);
        goButton.setOnClickListener(new View.OnClickListener() {
        	@Override
        	public void onClick (View v) {
        		loadRadar(v);
        	}

        }); 
        
        Button pickButton = (Button) findViewById(R.id.prefButton);        
        pickButton.setOnClickListener(new View.OnClickListener() {        				
			@Override
			public void onClick(View v) {
				Intent myIntent = new Intent(v.getContext(), PrefActivity.class);
				startActivity(myIntent);
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
        String selectedRadarCode = sharedPrefs.getString("pref_radar_code", "Pick a Radar");
        TextView radarName = (TextView)findViewById(R.id.radarCodeText);
        String selectedRadarName = RadarHelper.codeToName(selectedRadarCode,this.getBaseContext());
        radarName.setText(selectedRadarName);
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
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
	
    /**
     * Loads the radar screen
     * @param 
     */
	private void loadRadar(View v) {
		Intent myIntent = new Intent(v.getContext(), RadarActivity.class);
		String radarCode = sharedPrefs.getString("pref_radar_code", "gps");
		myIntent.putExtra("radarCode", radarCode);
        startActivityForResult(myIntent, 0);
	}
}