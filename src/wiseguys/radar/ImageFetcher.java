package wiseguys.radar;

import java.util.ArrayList;
import java.util.List;

import wiseguys.radar.conn.ImageDownloaderThread;
import wiseguys.radar.conn.SourceFetcherThread;
import android.graphics.Bitmap;
import android.util.Log;

public class ImageFetcher {
	
	private static ImageFetcher imgFetch;
	private final String baseAddress = "http://www.weatheroffice.gc.ca";	
	private SourceParser parser;
	private SourceFetcherThread htmlFetch;
	private List<Bitmap> latestImages = null;
	
	/**
	 * Singleton constructor
	 */
	private ImageFetcher() {	
	}
	
	/**
	 * Static class for singleton
	 * @return instance of this class
	 */
	public static ImageFetcher getImageFetcher() {
		if (imgFetch == null) {
			imgFetch = new ImageFetcher();
		} 
		return imgFetch;
	}
	
	/**
	 * Checks and executes the code download if necessary
	 * @param code
	 * @return
	 */
	private boolean setupFetch(String code) {
		if (htmlFetch != null) {
			if (htmlFetch.getCode().equals(code)) {
				return false;
			} else {
				return getRadarFromConnection(code);
			}
		}  else {			
			return getRadarFromConnection(code);
		}
	}
	
	/**
	 * Downloads the Radar data using a thread.
	 * @param code
	 * @return
	 */
	private boolean getRadarFromConnection(String code) {
		htmlFetch = new SourceFetcherThread();
		htmlFetch.setCode(code);
		htmlFetch.start();
		
		try {
			htmlFetch.join(); //TODO: Thread this better down the line				
		} catch (InterruptedException e) {
			e.printStackTrace();
			return false;
		}
		
		parser = new SourceParser(htmlFetch.getSource());			
		return true;
	}

	public List<Bitmap> getRadarImages(String code) {
		List<Bitmap> images = new ArrayList<Bitmap>();
		String radarImgUrls = null;
		
		if (!setupFetch(code)) {
			return latestImages;
		}
				
		radarImgUrls = parser.parseRadarImages();
		
		while (radarImgUrls.contains("|")) {
			String imageURL = baseAddress + radarImgUrls.substring(0, radarImgUrls.indexOf('|'));
			
			ImageDownloaderThread imgDown = new ImageDownloaderThread(imageURL);
			imgDown.start();
			try {
				imgDown.join();
			} catch (InterruptedException ie) {
				Log.e("ImageFetcher", "Error: " + ie);
				return latestImages;
			}
			images.add((Bitmap)imgDown.getImage());
			
			if (radarImgUrls.length() > 1) {
				radarImgUrls = radarImgUrls.substring(radarImgUrls.indexOf('|')+1);
			} else {
				break;
			}
		}		
		latestImages = images;
		return images;
	}
}
