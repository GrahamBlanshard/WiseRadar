package wiseguys.radar.conn;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;

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
			HttpGet hgr = new HttpGet(URI.create(Url));
			HttpClient hc = new DefaultHttpClient();
			HttpResponse r = (HttpResponse)hc.execute(hgr);
			HttpEntity e = r.getEntity();
			BufferedHttpEntity nhe = new BufferedHttpEntity(e);

			image = BitmapFactory.decodeStream(nhe.getContent());
			hgr.abort();
		} catch (IOException e) {
			e.printStackTrace();
			image = null;
		}

	}

}
