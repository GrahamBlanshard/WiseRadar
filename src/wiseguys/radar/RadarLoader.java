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
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import wiseguys.radar.ui.RadarFragment;
import wiseguys.radar.ui.adapter.PhotoViewAttacher;

import static wiseguys.radar.ui.RadarFragment.screenWidth;

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
    private PhotoViewAttacher adapter;

	public RadarLoader(Context c, Resources r, ImageView sImage, TextView name, PhotoViewAttacher mAdapter) {
		context = c;
		resources = r;
		anim = null;
		this.sImage = sImage;
		this.name = name;
		this.originalName = name.getText().toString();
        adapter = mAdapter;
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
     */
	private LayerDrawable loadRadar() {
		//Generate list of radar base images
		imgFetch = ImageFetcher.getImageFetcher();
		List<Bitmap> images;
	    
		if (selectedRadarCode == null) { return null; }
	    
	    publishProgress(resources.getString(R.string.dlFetch));

	    boolean detailed_colours = Integer.valueOf(sharedPrefs.getString("pref_radar_colour","14")) == 14;
	    images = imgFetch.getRadarImages(selectedRadarCode, detailed_colours);

	    if (images == null) {
	    	publishProgress(resources.getString(R.string.dlFailure));
	    	return null;
	    }
	    
	    publishProgress(resources.getString(R.string.dlReceived));
	    
	    //Drop Images into an animation
	    anim = new AnimationDrawable();
	    
	    if (!imgFetch.finished()) { return null; }
	    
	    for (int i = 0; i < images.size(); ++i) {
	    	anim.addFrame(new BitmapDrawable(context.getResources(),images.get(i)), 750);
	    }

	    publishProgress(resources.getString(R.string.dlOverlays));
	    
	    //Generate Overlays
	    Bitmap overlay = imgFetch.getOverlays(selectedRadarCode,sharedPrefs,context);
	    
	    publishProgress(resources.getString(R.string.dlSorting));
	    
	    LayerDrawable layers;
	    
	    if (overlay != null) {
	    	//Layer the overlay ontop of the animated radar images
		    Drawable[] layering = new Drawable[2];
		    BitmapDrawable overlayBitmap = new BitmapDrawable(resources,overlay); //Convert Bitmap to drawable
		    int calculatedOffset = images.get(0).getWidth() - overlay.getWidth(); //our overlay doesn't sit over the radar properly. Offset to the left to uncover legend
		    layering[0] = anim;
		    layering[1] = overlayBitmap;
		    layers = new LayerDrawable(layering);

            layers.setLayerInset(1, 0, 0, calculatedOffset, 0);
	    } else {
	    	//No layers selected!
	    	Drawable[] layering = new Drawable[1];
	    	layering[0] = anim;
	    	layers = new LayerDrawable(layering);
	    }
	    
	    publishProgress(resources.getString(R.string.dlFinished));
	    
	    return layers;
	}
	
	@Override
	protected void onPostExecute (LayerDrawable result) {
		if (result == null) {
			name.setText(resources.getString(R.string.noRadar));
			if (anim == null) { return;	}
		}

		sImage.setImageDrawable(result);

        ViewGroup.LayoutParams lp = sImage.getLayoutParams();
        lp.height = screenWidth;
        lp.width = screenWidth;
        sImage.setLayoutParams(lp);

		name.setText(originalName);
		
	    anim.setOneShot(false);
	    anim.start();

        updateAdapter(sImage);
	}

    /**
     * Do an update to the PhotoViewAdapter (or create if necessary)
     * @param img ImageView we are attaching to
     */
    public void updateAdapter(ImageView img) {
        if (adapter != null) {
            adapter.cleanup();
            adapter = null;
        }

        adapter = new PhotoViewAttacher(img);

        float zoomWidth = (RadarFragment.screenHeight > RadarFragment.screenWidth) ? RadarFragment.screenWidth * 0.95f : RadarFragment.screenHeight * 0.80f;
        float scale = zoomWidth / (float) img.getDrawable().getIntrinsicWidth();

        adapter.setScaleType(ImageView.ScaleType.CENTER);
        adapter.zoomTo(scale, (float) img.getDrawable().getIntrinsicWidth() / 2.0f, (float) img.getDrawable().getIntrinsicWidth() / 2.0f);
        adapter.setMaxScale( scale * 5.0f );
        adapter.setMidScale( scale * 2.5f );
        adapter.setMinScale(scale);
        adapter.update();
    }
	
	@Override
	protected void onPreExecute () {
		sImage.setImageResource(R.drawable.radar);
		name.setText(originalName);
	}
	
	@Override
	protected void onProgressUpdate (String... values) {
		name.setText(values[0]);		
	}

}
