package wiseguys.radar;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

public class RadarLoader extends AsyncTask<String, String, LayerDrawable> {

	private String selectedRadarCode;
	private String selectedDuration;
	private ImageFetcher imgFetch;
	private SharedPreferences sharedPrefs;
	private Context context;
	private Resources resources;
	private ImageView sImage;
	private TextView name;
	private String originalName;
	private AnimationDrawable anim;
	
	public RadarLoader(Context c, Resources r, ImageView sImage, TextView name) {
		context = c;
		resources = r;
		anim = null;
		this.sImage = sImage;
		this.name = name;
		this.originalName = name.getText().toString();
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
	}
	
	@Override
	protected LayerDrawable doInBackground(String... radarCode) {
		selectedRadarCode = radarCode[0];
		selectedDuration = radarCode[1];
		
		return loadRadar();
	}
	
	/**
     * Loads the radar screen
     * @param 
     */
	private LayerDrawable loadRadar() {
		//Generate list of radar base images
		imgFetch = ImageFetcher.getImageFetcher();
		List<Bitmap> images = new ArrayList<Bitmap>();
	    
		if (selectedRadarCode == null) {
	    	Log.e("WiseRadar","System error -- No code radar code selected from preferences, default failed.");	    	
	    	return null;
	    }
	    
	    publishProgress("Fetching images");
	    
	    images = imgFetch.getRadarImages(selectedRadarCode,selectedDuration);
	    
	    if (images == null) {
	    	Log.e("WiseRadar","System error -- No images were received. Likely due to Environment Canada not having any available data.");
	    	publishProgress("Image update failed");
	    	return null;
	    }
	    
	    publishProgress("Images received");
	    
	    //Drop Images into an animation
	    anim = new AnimationDrawable();
	    
	    if (!imgFetch.finished()) {
	    	Log.w("WiseRadar","Fetching images was interrupted early");
	    	return null;
	    }
	    
	    for (int i = images.size()-1; i >= 0; i--) {
	    	anim.addFrame(new BitmapDrawable(images.get(i)), 750); //TODO: Magic number, should we allow customization via preferences?	    
	    }

	    publishProgress("Fetching overlays");
	    
	    //Generate Overlays
	    Bitmap overlay = imgFetch.getOverlays(selectedRadarCode,sharedPrefs,context);
	    
	    publishProgress("Overlays received");
	    
	    //Layer the overlay ontop of the animated radar images
	    Drawable[] layering = new Drawable[2];
	    BitmapDrawable overlayBitmap = new BitmapDrawable(resources,overlay); //Convert Bitmap to drawable
	    int calculatedOffset = images.get(0).getWidth() - overlay.getWidth(); //our overlay doesn't sit over the radar properly. Offset to the left to uncover legend
	    layering[0] = anim;
	    layering[1] = overlayBitmap;
	    LayerDrawable layers = new LayerDrawable(layering);
	    
	    layers.setLayerInset(1, 0, 0, calculatedOffset, 0);
	    
	    publishProgress("Finishing");
	    
	    return layers;
	}
	
	@Override
	protected void onPostExecute (LayerDrawable result) {
		
		if (result == null) {
			name.setText("Update Failed.");
			
			//Back out of updates if we don't have a running animated object
			if (anim == null) {
				return;
			}
		}
		
		sImage.setImageDrawable(result);
		name.setText(originalName);
		
	    anim.setOneShot(false);
	    anim.start();
	}
	
	@Override
	protected void onPreExecute () {
		sImage.setImageDrawable(null); //TODO: Let's draw an image for this someday
		name.setText(originalName);
	}
	
	@Override
	protected void onProgressUpdate (String... values) {
		name.setText(values[0]);		
	}

}
