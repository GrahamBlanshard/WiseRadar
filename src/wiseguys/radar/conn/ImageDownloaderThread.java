package wiseguys.radar.conn;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ImageDownloaderThread extends Thread {

	private String Url;
	private Bitmap image;
	
	public ImageDownloaderThread(String URL) {
		this.Url = URL;
	}
	
	public Bitmap getImage() {
		if (this.isAlive()) {
			return null;
		} else {
			return image;
		}
	}
	
	/**
	 * Downloads an image passed into the constructor via URL
	 */
	public void run() {
		try {
	        URL url = new URL(Url); //you can write here any link
	        HttpURLConnection ucon = (HttpURLConnection)url.openConnection();
	        InputStream is = ucon.getInputStream();
	        
	        image = BitmapFactory.decodeStream(is);
	        
	        is.close();	                
		} catch (IOException e) { }

	}

}
