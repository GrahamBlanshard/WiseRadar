package wiseguys.radar;

import java.util.List;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

public class ImageCycler implements Runnable {

	List<Bitmap> images;
	Activity parent;
	
	public ImageCycler(List<Bitmap> images, Activity parent) {
		this.images = images;//addAll(images);
		this.parent = parent;
	}
	/****
	 * Chagen to a handler? http://stackoverflow.com/questions/10209819/android-change-view-from-other-thread
	 */
	@Override
	public void run() {
		int numImages = 6;//R.integer.image_count; //TODO: Fix?
		
		//while (true) {
			for (int i = 0; i < numImages; i++) {
				
		    	ImageView sImage = (ImageView)parent.findViewById(R.id.radarImage);
		    	Bitmap tmpImage = (Bitmap)images.get(i);
		    	sImage.setImageBitmap(tmpImage);
		    	
		    	//Drawable image = new BitmapDrawable(tmpImage);
		    	
		    	//sImage.setImageDrawable(image);	
		    	
				//try {
					//this.wait(10000);
					//Thread.sleep(10000);
				// catch (InterruptedException ex) {
					//Do nothing!
				//}
			}	  
		//}
	}
}
