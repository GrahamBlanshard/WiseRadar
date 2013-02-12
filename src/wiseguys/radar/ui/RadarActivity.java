package wiseguys.radar.ui;

import java.util.ArrayList;
import java.util.List;

import wiseguys.radar.ImageFetcher;
import wiseguys.radar.R;
import wiseguys.radar.RadarHelper;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
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
	    	anim.addFrame(new BitmapDrawable(images.get(i)), 750); //TODO: Magic number, should we allow customization?	    
	    
	    //Generate Overlays
	    Boolean showRoads = bundle.getBoolean("showRoads");
	    Boolean showTowns = bundle.getBoolean("showTowns");
	    Boolean showRadarCircles = bundle.getBoolean("showRadarCircles");
	    Boolean showTownsMore = bundle.getBoolean("showAdditionalTowns");;
	    Boolean showRivers = bundle.getBoolean("showRivers");;
	    Boolean showRoadNumbers = bundle.getBoolean("showRoadNumbers");;
	       
	    List<Bitmap> overlays = new ArrayList<Bitmap>();
	    
	    Bitmap roadImage = null;
	    Bitmap townImage = null;
	    Bitmap townsMoreImage = null;
	    Bitmap riverImage = null;
	    Bitmap roadNumbersImage = null;
	    Bitmap radarCircles = BitmapFactory.decodeResource(getResources(),R.drawable.radar_circle);
	    Bitmap overlay = null;
	    
	    if (showRoads) {
	    	roadImage = getRoads(code);
	    	overlays.add(roadImage);
	    } 
	    if (showTowns) {
	    	townImage = getTowns(code);
	    	overlays.add(townImage);
	    } 
	    if (showRadarCircles) {
	    	overlays.add(radarCircles);
	    }
	    if (showTownsMore) {
	    	townsMoreImage = getTownsMore(code);
	    	overlays.add(townsMoreImage);
	    }
	    if (showRivers) {
	    	riverImage = getRivers(code);
	    	overlays.add(riverImage);
	    }
	    if (showRoadNumbers) {
	    	roadNumbersImage = getRoadNumbers(code);
	    	overlays.add(roadNumbersImage);
	    }
	    
	    overlay = combine(overlays);
	    
	    //sImage.setImageBitmap(overlay);
	    //sImage.setBackgroundDrawable(anim);
	    
	    Drawable[] layering = new Drawable[2];
	    BitmapDrawable overlayBitmap = new BitmapDrawable(getResources(),overlay);
	    layering[0] = anim;
	    layering[1] = overlayBitmap;
	    LayerDrawable layers = new LayerDrawable(layering);
	    int calculatedOffset = images.get(0).getWidth() - overlay.getWidth();
	    layers.setLayerInset(1, 0, 0, calculatedOffset, 0);
	    sImage.setImageDrawable(layers);
	    
	    anim.setOneShot(false);
	    anim.start();
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
    
    private Bitmap getRoads(String code) {
    	String roadImageURL = "/radar/images/layers/roads/" + code.toUpperCase() + "_roads.gif";
    	Bitmap roadMap = imgFetch.getImage(roadImageURL);
    	
    	return roadMap;
    }
    
    private Bitmap getTowns(String code) {
    	String townImageURL = "/radar/images/layers/default_cities/" + code + "_towns.gif";
    	Bitmap towns = imgFetch.getImage(townImageURL);
    	
    	return towns;
    }
    
    private Bitmap getTownsMore(String code) {
    	String townImageURL = "/radar/images/layers/additional_cities/" + code + "_towns.gif";
    	Bitmap towns = imgFetch.getImage(townImageURL);
    	return towns;
    }
    
    private Bitmap getRoadNumbers(String code) {
    	String roadNumURL = "/radar/images/layers/road_labels/" + code + "_labs.gif";
    	Bitmap roadNums = imgFetch.getImage(roadNumURL);
    	return roadNums;
    }
    
    private Bitmap getRivers(String code) {
    	String riverURL = "/radar/images/layers/rivers/" + code + "_rivers.gif";
    	Bitmap rivers = imgFetch.getImage(riverURL);
    	return rivers;
    }
    
    private Bitmap combine(List<Bitmap> overlays) {
    	Bitmap image1 = null;
    	
    	if (overlays.size() != 0) {
    		image1 = overlays.get(0);
    		Bitmap newOverlay = Bitmap.createBitmap(image1.getWidth(), image1.getHeight(), Bitmap.Config.ARGB_8888);
    		Canvas tempCanvas = new Canvas(newOverlay);
    		tempCanvas.drawBitmap(overlays.get(0), new Matrix(), null);
    		
    		for (int i = 1; i < overlays.size(); i++) {
    			tempCanvas.drawBitmap(overlays.get(i), 0, 0, null);
    		}
    		//TODO: Fix this so we don't overlay the legend, that is wrong!
    		//Current dimensions: 480x480
    		//Radars = 480x480 + 100px sidebar legend
    		image1 = Bitmap.createBitmap(newOverlay, 0, 0, newOverlay.getWidth(), newOverlay.getHeight());
    	}
    	
    	return image1;
    }
}
