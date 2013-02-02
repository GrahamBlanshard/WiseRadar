package wiseguys.radar;

import java.util.ArrayList;
import java.util.List;

import wiseguys.radar.conn.ImageDownloaderThread;
import wiseguys.radar.conn.SourceFetcherThread;
import android.graphics.Bitmap;
import android.util.Log;

public class ImageFetcher {
	
	private static ImageFetcher imgFetch;
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
			String imageURL = radarImgUrls.substring(0, radarImgUrls.indexOf('|'));
			Bitmap newImage = getImage(imageURL);
			
			if (newImage == null) {
				return latestImages; //We failed. Return last set
			} else {
				images.add(newImage);
			}
			
			if (radarImgUrls.length() > 1) {
				radarImgUrls = radarImgUrls.substring(radarImgUrls.indexOf('|')+1);
			} else {
				break;
			}
		}		
		latestImages = images;
		return images;
	}
	
	/**
	 * Fetches image at the given URL
	 * @param URL URL of image
	 * @return Bitmap of image from URL
	 */
	public Bitmap getImage(String URL) {
		ImageDownloaderThread imgDown = new ImageDownloaderThread(RadarHelper.baseURL + URL);
		imgDown.start();
		try {
			imgDown.join();
		} catch (InterruptedException ie) {
			Log.e("ImageFetcher", "Error: " + ie);
			return null;
		}
		return (Bitmap)imgDown.getImage();
	}
}
