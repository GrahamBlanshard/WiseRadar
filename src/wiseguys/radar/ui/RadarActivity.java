package wiseguys.radar.ui;

import java.util.ArrayList;
import java.util.List;

import wiseguys.radar.ImageFetcher;
import wiseguys.radar.R;
import wiseguys.radar.RadarHelper;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.ImageView;
import android.widget.TextView;

public class RadarActivity extends Activity {
	
	private ImageFetcher imgFetch;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.radar);
        imgFetch = ImageFetcher.getImageFetcher();
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
    	
    	//Set the radar's title
	    Bundle bundle = getIntent().getExtras();
	    String code = bundle.getString("radarCode");

	    if (code == null) {
	    	Log.e("RadarActivity","System error -- No code passed to activity");
	    	code = "xbe"; //TODO: Temp fix
	    }
	    String radarName = RadarHelper.codeToName(code,this.getBaseContext());
	    TextView radarTextName = (TextView)findViewById(R.id.radarName);
	    radarTextName.setText(radarName);   

	    
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
    
    private String matchGPS() {
    	return "xbe";	//TODO: Update to real GPS use eventually
    }
}
