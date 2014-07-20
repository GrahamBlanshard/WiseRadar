package wiseguys.radar;

import java.util.ArrayList;
import java.util.List;

import wiseguys.radar.conn.GPSHelper;
import wiseguys.radar.conn.ImageDownloaderThread;
import wiseguys.radar.conn.SourceFetcherThread;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;

public class ImageFetcher {
	
	private static ImageFetcher imgFetch;
	private SourceFetcherThread htmlFetch;
	private List<Bitmap> latestImages = null;
	private boolean finished;
	private boolean failedPreviously;

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
	 * @param code radar site code
	 * @return true when an updated is required
	 */
	private boolean setupFetch(String code, String duration) {
		if (htmlFetch != null) {
			//Skip update when we are looking at the same radar, or last attempt had failed
            return !(htmlFetch.getCode().equals(code) && !failedPreviously) && getRadarFromConnection(code, duration);
		}  else {			
			return getRadarFromConnection(code,duration);
		}
	}
	
	/**
	 * Downloads the Radar data using a thread.
	 * @param code radar code
	 * @return true when we get a successful html download
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

	public List<Bitmap> getRadarImages(String code, String duration, int colours) {
		
		finished = false;
		List<Bitmap> images = new ArrayList<Bitmap>();
		List<String> radarImgUrls;
		
		if (!setupFetch(code,duration)) {
			finished = true;
			return latestImages;
		}
				
		radarImgUrls = RadarHelper.parseRadarImages(htmlFetch.getSource());
		
		//Source parsing failed; likely due to a lack of images provided from the host
		if (radarImgUrls == null) {
			return latestImages;
		}

        radarImgUrls = changeDetail(radarImgUrls,colours);
		
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
		return imgDown.getImage();
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
        Boolean showLocation = sharedPrefs.getBoolean("show_location", false);
	    
	    Bitmap roadImage;
	    Bitmap townImage;
	    Bitmap townsMoreImage;
	    Bitmap riverImage;
	    Bitmap roadNumbersImage;
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
	    
	    return combine(overlays,showLocation);
	}
	
	/**
	 * Combines the given list of Bitmaps to a single image
	 * @param overlays Bitmaps to combine in order
	 * @return a single Bitmap image
	 */
	private Bitmap combine(List<Bitmap> overlays, boolean showLocation) {
    	Bitmap image1 = null;
    	
    	if (overlays.size() != 0) {
    		image1 = overlays.get(0);
    		Bitmap newOverlay = Bitmap.createBitmap(image1.getWidth(), image1.getHeight(), Bitmap.Config.ARGB_8888);
    		Canvas tempCanvas = new Canvas(newOverlay);

            if (showLocation) {
                tempCanvas = drawGPS(tempCanvas);
            }

            Bitmap base = fixBackground(image1);
    		tempCanvas.drawBitmap(base, new Matrix(), null);

            int w = newOverlay.getWidth();
            int h = newOverlay.getHeight();
    		
    		for (int i = 1; i < overlays.size(); i++) {
                Bitmap overlay = overlays.get(i);

                if ( overlay.getWidth() > w || overlay.getHeight() > h ) {
                    Matrix m = new Matrix();
                    m.setScale( ((float)w / (float)overlay.getWidth()),( (float)h / (float)overlay.getHeight() ) );
                    overlay = Bitmap.createBitmap(overlay, 0, 0, overlay.getWidth(), overlay.getHeight(), m, false);
                }

    			tempCanvas.drawBitmap( fixBackground(overlay), 0, 0, null);
    		}

    		image1 = Bitmap.createBitmap(newOverlay, 0, 0, newOverlay.getWidth(), newOverlay.getHeight());
    	}
    	
    	return image1;
    }

    /***
     * TODO:
     * - GPSHelper, set a static value for lat/long when the closest city is first determined!
     * - sharedPrefs, give user an option to show their location or not
     */
    private Canvas drawGPS(Canvas canvas) {

        double lat = GPSHelper.lastGoodLat;
        double lng = GPSHelper.lastGoodLong;
        double cLng = GPSHelper.cityLat;
        double cLat = GPSHelper.cityLong;

        Paint p = new Paint();
        float X = 20.0f;
        float Y = 20.0f;
        float size = 8.0f;


        p.setColor(Color.BLACK);
        canvas.drawCircle(X,Y,size,p);
        p.setColor(Color.WHITE);
        canvas.drawCircle(X,Y,size*0.75f,p);

        /**
         * Determine our location based on knowledge that mid map is our town lat/long
         */


        return canvas;
    }

    private Bitmap fixBackground(Bitmap img) {
        int width = img.getWidth();
        int height = img.getHeight();

        Bitmap copy = img.copy(Bitmap.Config.ARGB_8888,true);
        copy.setHasAlpha(true);

        int[] pixels = new int[width * height];
        img.getPixels(pixels,0,width,0,0,width,height);

        int color = (pixels[0] == -1) ? -1 : -16777216; //-1 for white background | -16777216 for black

        for (int i = 0; i < pixels.length; i++) {
            if (pixels[i] == color) {
                pixels[i] = 0;
            }
        }

        copy.setPixels(pixels, 0, width, 0, 0, width, height);

        return copy;
    }
	
	private Bitmap getRoads(String code) {
    	//String roadImageURL = "/radar/images/layers/roads/" + code.toUpperCase() + "_roads.gif";
		String roadImageURL = "/cacheable/images/radar/layers/roads/" + code.toUpperCase() + "_roads.gif";
    	return imgFetch.getImage(roadImageURL);
    }
    
    private Bitmap getTowns(String code) {
    	String townImageURL = "/cacheable/images/radar/layers/default_cities/" + code + "_towns.gif";
    	return imgFetch.getImage(townImageURL);
    }
    
    private Bitmap getTownsMore(String code) {
    	String townImageURL = "/cacheable/images/radar/layers/additional_cities/" + code + "_towns.gif";
    	return imgFetch.getImage(townImageURL);
    }
    
    private Bitmap getRoadNumbers(String code) {
    	String roadNumURL = "/cacheable/images/radar/layers/road_labels/" + code + "_labs.gif";
    	return imgFetch.getImage(roadNumURL);
    }
    
    private Bitmap getRivers(String code) {
    	String riverURL = "/cacheable/images/radar/layers/rivers/" + code + "_rivers.gif";
    	return imgFetch.getImage(riverURL);
    }

    private List<String> changeDetail(List<String> images, int colours) {
        List<String> newList = new ArrayList<String>();
        if (colours == 8) {
            //"detailed"
            for (String img : images) {
                if (!img.contains("/detailed/")) {
                    //Add detailed to URL
                    img = img.replace("/radar/","/radar/detailed/");
                }
                newList.add(img);
            }

        } else {
            for (String img : images) {
                if (img.contains("/detailed/")) {
                    //Remove detailed to URL
                    img = img.replace("/detailed/","/");
                }
                newList.add(img);
            }
        }
        return newList;
    }
    
    /**
     * Call to ensure this thread finished in full
     * @return true if we completed all operations
     */
    public boolean finished() {
    	return finished;
    }
}
