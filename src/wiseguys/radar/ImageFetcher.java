package wiseguys.radar;

import java.util.ArrayList;
import java.util.List;

import wiseguys.radar.helpers.GPSHelper;
import wiseguys.radar.conn.ImageDownloaderThread;
import wiseguys.radar.conn.SourceFetcherThread;
import wiseguys.radar.helpers.RadarHelper;
import wiseguys.radar.ui.RadarFragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.location.Location;

public class ImageFetcher {

    private static ImageFetcher imgFetch;
    private SourceFetcherThread htmlFetch;
    private List<Bitmap> latestImages = null;
    private boolean finished;
    private boolean failedPreviously;
    private String lastSuccessfulCode;

    /*
      Transparent Colour Values
      Unused in code, but handy to have
        private final int TRANSPARENT_BLACK = -16777216;
        private final int TRANSPARENT_WHITE = -1;
        private final int TRANSPARENT_GREEN = -10319613;
        private final int TRANSPARENT_GRAY = -11570469;
                                             -11579569;
        private final int TRANSPARENT_RED = -15724432;
     */

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
    private boolean setupFetch(String code) {
        if (htmlFetch != null) {
            //Skip update when we are looking at the same radar, or last attempt had failed
            return !(htmlFetch.getCode().equals(code) && !failedPreviously) && getRadarFromConnection(code);
        }  else {
            return getRadarFromConnection(code);
        }
    }

    /**
     * Downloads the Radar data using a thread.
     * @param code radar code
     * @return true when we get a successful html download
     */
    private boolean getRadarFromConnection(String code) {
        htmlFetch = new SourceFetcherThread();
        htmlFetch.setCode(code);
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

        if (!setupFetch(code)) {
            finished = true;

            if (code.equals(lastSuccessfulCode)) {
                return latestImages;
            } else {
                return null;
            }
        }

        radarImgUrls = RadarHelper.parseRadarImages(htmlFetch.getSource(), duration, colours);

        //Source parsing failed; likely due to a lack of images provided from the host
        if (radarImgUrls == null) {
            if (code.equals(lastSuccessfulCode)) {
                return latestImages;
            } else {
                return null;
            }
        }

        for (String imageURL : radarImgUrls) {
            Bitmap newImage = getImage(imageURL);

            if (newImage == null) {
                if (code.equals(lastSuccessfulCode)) {
                    return latestImages;
                } else {
                    return null;
                }
            } else {
                images.add(newImage);
            }
        }

        failedPreviously = false;
        finished = true;
        latestImages = images;
        lastSuccessfulCode = code;
        return images;
    }

    /**
     * Fetches image at the given URL
     * @param URL URL of image
     * @return Bitmap of image from URL
     */
    public Bitmap getImage(String URL) {
        ImageDownloaderThread imgDown = new ImageDownloaderThread(URL);
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
        Boolean showRoads = sharedPrefs.getBoolean("roads",true);
        Boolean showTowns = sharedPrefs.getBoolean("towns",true);
        Boolean showRadarCircles = sharedPrefs.getBoolean("circles",true);
        Boolean showRoadNumbers = sharedPrefs.getBoolean("roadNums",false);
        Boolean showTownsMore = sharedPrefs.getBoolean("addTowns",false);
        Boolean showRivers = sharedPrefs.getBoolean("rivers",false);
        Boolean showLocation = sharedPrefs.getBoolean("show_location", false) && sharedPrefs.getBoolean("gps",false);

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

        //Note Circles always added last
        if (showRadarCircles) {
            overlays.add(radarCircles);
        }

        if (overlays.size() > 0)
            return combine(overlays,showLocation,latestImages.get(0).getHeight(),latestImages.get(0).getWidth(),showRadarCircles);
        else
            return null;
    }

    /**
     * Combines the given list of Bitmaps to a single image
     * @param overlays Bitmaps to combine in order
     * @return a single Bitmap image
     */
    private Bitmap combine(List<Bitmap> overlays, boolean showLocation, int vSize, int hSize, boolean showCircles) {
        Bitmap image1;
        Canvas tempCanvas;

        Bitmap newOverlay = Bitmap.createBitmap(hSize, vSize, Bitmap.Config.ARGB_8888);
        tempCanvas = new Canvas(newOverlay);

        for (int i = 0; i < overlays.size(); i++) {
            Bitmap overlay = overlays.get(i);

            int w = overlay.getWidth();
            int h = overlay.getHeight();

            if ( overlay.getWidth() > hSize || overlay.getHeight() > vSize ) {
                Matrix m = new Matrix();
                if ( showCircles && i == overlays.size()-1 ) {
                    //Special treatment for radar circles.
                    m.setScale(((float) vSize / (float) overlay.getWidth()), ((float) vSize / (float) overlay.getHeight()));
                } else {
                    m.setScale(((float) hSize / (float) overlay.getWidth()), ((float) vSize / (float) overlay.getHeight()));
                }

                overlay = Bitmap.createBitmap(overlay, 0, 0, overlay.getWidth(), overlay.getHeight(), m, false);
            }

            tempCanvas.drawBitmap( fixBackground(overlay), 0, 0, null);
        }

        if (showLocation) {
            drawGPS(tempCanvas);
        }

        image1 = Bitmap.createBitmap(newOverlay, 0, 0, newOverlay.getWidth(), newOverlay.getHeight());

        return image1;
    }

    /***
     * Good resources:
     *  - http://williams.best.vwh.net/avform.htm#LL
     *  - http://www.freemaptools.com/radius-around-point.htm
     */
    private void drawGPS(Canvas canvas) {
        Paint p = new Paint();
        float size = 4.0f * RadarFragment.density * 0.5f;

        double lat = GPSHelper.lastGoodLat;
        double lng = GPSHelper.lastGoodLong;

        /**
         * Radar is located center of image && 240km from edges
         */
        final double latOf240km = 2.155; //Estimates, should be accurate for our small section of the earth
        final double longOf240km = 3.406; //Anything exact requires additional API calls for eliptical calcs
        double midLat = GPSHelper.radarLat;
        double midLong = GPSHelper.radarLong;

        double sqPixels = canvas.getHeight(); //Because we want to work off the square radar image, not the right side legend
        double midSqPixel = sqPixels / 2;

        double TLLat = midLat + latOf240km;     //northern-most point on map
        double TLLong = midLong - longOf240km;  //western-most point on map

        //We now have our two coordinate systems with points in the top-left and center
        float circleY = normalize((float)lat,(float)TLLat,(float)midLat) * (float)midSqPixel;
        float circleX = normalize((float)lng,(float)TLLong,(float)midLong) * (float)midSqPixel;

        p.setColor(Color.BLACK);
        canvas.drawCircle(circleX,circleY,size,p);
        p.setColor(Color.WHITE);
        canvas.drawCircle(circleX,circleY,size*0.75f,p);
    }

    /**
     * Used for converting our coordinate systems
     * @param value - Point we're normalizing
     * @param min - Top || Left point of view
     * @param max - Radar point
     * @return - A normalized point from 0-2. 1 being center of screen
     */
    private float normalize(float value, float min, float max) {
        return Math.abs((value - min) / (max - min));
    }

    private Bitmap fixBackground(Bitmap img) {
        int width = img.getWidth();
        int height = img.getHeight();

        Bitmap copy = img.copy(Bitmap.Config.ARGB_8888,true);
        copy.setHasAlpha(true);

        int[] pixels = new int[width * height];
        img.getPixels(pixels,0,width,0,0,width,height);

        int colour = pixels[0];

        if (htmlFetch.getCode().equals("xbu") ||
                htmlFetch.getCode().equals("wkr") ||
                htmlFetch.getCode().equals("wmn") ||
                htmlFetch.getCode().equals("wtp")) {
            /*
            Special case scenarios --
                Radar Overlays use different transparent values in top corner
                    compared to the rest of overlay
                Schuler AB      (xbu)   Uses -11570469
                King City ON    (wkr)   Uses -15724432
                McGill QB       (wmn)   Uses -11579569
                Holyrood NL     (wtp)   Uses -11570569
            */
            colour = pixels[5];
        }

        for (int i = 0; i < pixels.length; i++) {
            if (pixels[i] == colour) {
                pixels[i] = 0;
            }
        }

        copy.setPixels(pixels, 0, width, 0, 0, width, height);

        return copy;
    }

    private Bitmap getRoads(String code) {
        //String roadImageURL = "/radar/images/layers/roads/" + code.toUpperCase() + "_roads.gif";
        String roadImageURL = RadarHelper.baseURL + "/cacheable/images/radar/layers/roads/" + code.toUpperCase() + "_roads.gif";
        return imgFetch.getImage(roadImageURL);
    }

    private Bitmap getTowns(String code) {
        String townImageURL = RadarHelper.baseURL + "/cacheable/images/radar/layers/default_cities/" + code + "_towns.gif";
        return imgFetch.getImage(townImageURL);
    }

    private Bitmap getTownsMore(String code) {
        String townImageURL = RadarHelper.baseURL + "/cacheable/images/radar/layers/additional_cities/" + code + "_towns.gif";
        return imgFetch.getImage(townImageURL);
    }

    private Bitmap getRoadNumbers(String code) {
        String roadNumURL = RadarHelper.baseURL + "/cacheable/images/radar/layers/road_labels/" + code + "_labs.gif";
        return imgFetch.getImage(roadNumURL);
    }

    private Bitmap getRivers(String code) {
        String riverURL = RadarHelper.baseURL + "/cacheable/images/radar/layers/rivers/" + code + "_rivers.gif";
        return imgFetch.getImage(riverURL);
    }

    /**
     * Call to ensure this thread finished in full
     * @return true if we completed all operations
     */
    public boolean finished() {
        return finished;
    }
}
