package wiseguys.radar.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import wiseguys.radar.ImageFetcher;
import wiseguys.radar.R;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.ImageView;
import android.widget.TextView;

public class RadarActivity extends Activity {
	
	private Map<String,String> codeMap;
	private ImageFetcher imgFetch;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.radar);
        
        /**
         * Shared Preferences -- Needed to pass data between activities.
         */
        SharedPreferences settings = getSharedPreferences("APP_PREFS", 0);
        SharedPreferences.Editor spEditor = settings.edit();
        spEditor.putString("APP_NAME", "WiseRadar v0.1");
        spEditor.commit();
        imgFetch = ImageFetcher.getImageFetcher();
        parseCodeArray();
        
        
        //TODO:: TItle is the code and not the radar site name
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
    	List<Bitmap> images = new ArrayList<Bitmap>();
	    Bundle bundle = getIntent().getExtras();
	    TextView radarTextName = (TextView)findViewById(R.id.radarName);
	    String radarName = bundle.getString("radarCode");
	    radarTextName.setText(radarName);   
	    String code = codeMap.get(radarName);
	    
	    //TODO: Temp fix
	    if (code == null) {
	    	code = "xbe";
	    }
	    
	    //Check if we need GPS or not
	    if (!code.equals("gps")) { //No GPS
	    	images = imgFetch.getRadarImages(code);
	    } else { //GPS
	    	matchGPS();
	    }
	    	    
	    ImageView sImage = (ImageView)findViewById(R.id.radarImage);
	    
	    AnimationDrawable anim = new AnimationDrawable();
	    
	    for (int i = images.size()-1; i >= 0; i--)
	    	anim.addFrame(new BitmapDrawable(images.get(i)), 750);
	    
	    sImage.setBackgroundDrawable(anim);
	    sImage.setImageBitmap(null);
	    anim.setOneShot(false);
	    anim.start();
	       
	    //SharedPreferences settings = getSharedPreferences("APP_PREFS",0);
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
    
    
    private void parseCodeArray() {
    	codeMap = new HashMap<String, String>();
    	String[] codeMaps = getResources().getStringArray(R.array.NameToCode);
    	
    	for (String s : codeMaps) {
    		String key = s.substring(0, s.indexOf('|'));
    		String val = s.substring(s.indexOf('|')+1);
    	
    		codeMap.put(key, val);
    	}
    }
    
    private String matchGPS() {
    	return "xbe";	//TODO: Update to real GPS use
    }
}
