package wiseguys.radar;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import wiseguys.radar.conn.ImageDownloaderThread;
import wiseguys.radar.conn.SourceFetcherThread;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;

public class ImageFetcher {
	
	private static ImageFetcher imgFetch;
	private SourceFetcherThread htmlFetch;
	private List<Bitmap> latestImages = null;
	private boolean finished;
	private boolean failedPreviously;
	private Date lastUpdate;
	/**
	 * Singleton constructor
	 */
	private ImageFetcher() {	
		failedPreviously = false;		
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
	private boolean setupFetch(String code, String duration) {
		if (htmlFetch != null) {
			//Skip update when we are looking at the same radar which hasn't seen an update in 10 minutes, or last attempt had failed
			if (htmlFetch.getCode().equals(code) && !timeToUpdate() && !failedPreviously) {
				return false;
			} else {
				return getRadarFromConnection(code,duration);
			}
		}  else {			
			return getRadarFromConnection(code,duration);
		}
	}
	
	/**
	 * Downloads the Radar data using a thread.
	 * @param code
	 * @return
	 */
	private boolean getRadarFromConnection(String code,String duration) {
		htmlFetch = new SourceFetcherThread();
		htmlFetch.setCode(code);
		htmlFetch.setDuration(duration);
		htmlFetch.start();
		
		try {
			htmlFetch.join();			
		} catch (InterruptedException e) {
			failedPreviously = true;
			return false;
		}
		
		return true;
	}

	public List<Bitmap> getRadarImages(String code, String duration) {
		
		finished = false;
		List<Bitmap> images = new ArrayList<Bitmap>();
		List<String> radarImgUrls = null;
		
		if (!setupFetch(code,duration)) {
			finished = true;
			return latestImages;
		}
				
		radarImgUrls = RadarHelper.parseRadarImages(htmlFetch.getSource());
		
		//Source parsing failed; likely due to a lack of images provided from the host
		if (radarImgUrls == null) {
			return latestImages;
		}
		
		for (String imageURL : radarImgUrls) {
			Bitmap newImage = getImage(imageURL);
			
			if (newImage == null) {
				return latestImages; //We failed. Return last set
			} else {
				images.add(newImage);
			}
		}		
		
		failedPreviously = false;
		finished = true;
		latestImages = images;
		lastUpdate = new Date();
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
			failedPreviously = true;
			return null;
		}
		return (Bitmap)imgDown.getImage();
	}
	
	public Bitmap getOverlays(String selectedRadarCode, SharedPreferences sharedPrefs, Context context) {
		List<Bitmap> overlays = new ArrayList<Bitmap>();
		
		//Fetch our overlay preferences
	    Boolean showRoads = sharedPrefs.getBoolean("roads",false);
	    Boolean showTowns = sharedPrefs.getBoolean("towns",false);
	    Boolean showRadarCircles = sharedPrefs.getBoolean("circles",false);
	    Boolean showRoadNumbers = sharedPrefs.getBoolean("roadNums",false);
	    Boolean showTownsMore = sharedPrefs.getBoolean("addTowns",false);
	    Boolean showRivers = sharedPrefs.getBoolean("rivers",false);    
	    
	    Bitmap roadImage = null;
	    Bitmap townImage = null;
	    Bitmap townsMoreImage = null;
	    Bitmap riverImage = null;
	    Bitmap roadNumbersImage = null;
	    Bitmap radarCircles = BitmapFactory.decodeResource(context.getResources(),R.drawable.radar_circle);
	    
	    if (showRoads) {
	    	roadImage = getRoads(selectedRadarCode);
	    	overlays.add(roadImage);
	    } 
	    if (showTowns) {
	    	townImage = getTowns(selectedRadarCode);
	    	overlays.add(townImage);
	    } 
	    if (showRadarCircles) {
	    	overlays.add(radarCircles);
	    }
	    if (showTownsMore) {
	    	townsMoreImage = getTownsMore(selectedRadarCode);
	    	overlays.add(townsMoreImage);
	    }
	    if (showRivers) {
	    	riverImage = getRivers(selectedRadarCode);
	    	overlays.add(riverImage);
	    }
	    if (showRoadNumbers) {
	    	roadNumbersImage = getRoadNumbers(selectedRadarCode);
	    	overlays.add(roadNumbersImage);
	    }
	    
	    return combine(overlays);
	}
	
	/**
	 * Combines the given list of Bitmaps to a single image
	 * @param overlays Bitmaps to combine in order
	 * @return a single Bitmap image
	 */
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

    		image1 = Bitmap.createBitmap(newOverlay, 0, 0, newOverlay.getWidth(), newOverlay.getHeight());
    	}
    	
    	return image1;
    }
	
	private Bitmap getRoads(String code) {
    	//String roadImageURL = "/radar/images/layers/roads/" + code.toUpperCase() + "_roads.gif";
		String roadImageURL = "/cacheable/images/radar/layers/roads/" + code.toUpperCase() + "_roads.gif";
    	Bitmap roadMap = imgFetch.getImage(roadImageURL);
    	
    	return roadMap;
    }
    
    private Bitmap getTowns(String code) {
    	String townImageURL = "/cacheable/images/radar/layers/default_cities/" + code + "_towns.gif";
    	Bitmap towns = imgFetch.getImage(townImageURL);
    	
    	return towns;
    }
    
    private Bitmap getTownsMore(String code) {
    	String townImageURL = "/cacheable/images/radar/layers/additional_cities/" + code + "_towns.gif";
    	Bitmap towns = imgFetch.getImage(townImageURL);
    	return towns;
    }
    
    private Bitmap getRoadNumbers(String code) {
    	String roadNumURL = "/cacheable/images/radar/layers/road_labels/" + code + "_labs.gif";
    	Bitmap roadNums = imgFetch.getImage(roadNumURL);
    	return roadNums;
    }
    
    private Bitmap getRivers(String code) {
    	String riverURL = "/cacheable/images/radar/layers/rivers/" + code + "_rivers.gif";
    	Bitmap rivers = imgFetch.getImage(riverURL);
    	return rivers;
    }
    
    /**
     * Call to ensure this thread finished in full
     * @return true if we completed all operations
     */
    public boolean finished() {
    	return finished;
    }
    
    /**
     * Check the time, skip update if we have not surpassed 10 minutes (interval of which Env. Canada updates)
     * @return True if its time to update the radar
     */
    private boolean timeToUpdate() {
    	if (lastUpdate != null) {
    		Date now = new Date();
        	return (now.getTime() - lastUpdate.getTime()) >= RadarHelper.TEN_MINUTES;
    	}
    	
    	return true;    		
    }
}
