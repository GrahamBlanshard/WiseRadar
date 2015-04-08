package wiseguys.radar.helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import wiseguys.radar.R;
import wiseguys.radar.conn.ImageDownloaderThread;
import wiseguys.radar.conn.SourceFetcherThread;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.util.Log;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class RadarHelper {
	
	public static final String baseURL = "http://weather.gc.ca"; 	//http://weather.gc.ca/radar/index_e.html?id=<CODE>
    public static final String c8ImageURL = baseURL + "/data/radar/detailed/temp_image/";
    public static final String c14ImageURL = baseURL + "/data/radar/temp_image/";
    public static Location latestLocation;

    public static final int TEN_MINUTES = 60000;

	public static String codeToName(String code, Context systemContext) {
		//Test to make sure we're ready to accept it
		if (systemContext == null) {
			return null;
		}
		
		if (code == null) {
			return null;
		}
		
		String[] radarCodes = systemContext.getResources().getStringArray(R.array.radar_codes);
		String[] radarNames = systemContext.getResources().getStringArray(R.array.radar_cities);

		int index;
		for (index = 0; index < radarCodes.length; index++) {
			if (radarCodes[index].equals(code)) {
				break;
			}
		}
		
		//Couldn't find it. Invalid entry!
		if (index == radarCodes.length) {
			return null;
		}
		
		return radarNames[index];
	}
	
	/**
	 * Breaks apart the page source to retrieve gif images
	 * @param code HTML code of page we're looking to parse
	 * @return a collated collection of gif image names from the URL source
	 */
	public static List<String> parseRadarImages(Document code, String duration, int depth) {
		List<String> imageURLs = new ArrayList<String>();

        //Get Image List DIV
        Elements imageList = null;
        try {
            imageList = code.select("a[href*=display]");
            if (imageList.isEmpty()) {
                return null;
            }
        } catch ( Exception e ) {
            Log.e("WiseRadar",e.getMessage());
        }

        Pattern p = Pattern.compile("display='(([A-Z]{3})_[A-Z_]*_[0-9]{4}(_[0-9]{2})+)'");
        //Future: Can use this regex to extract image datetime

        //Should always be 15 long. First 6 are "Short" last 9 are "Long"
        int count = (duration.equals("long") ? 15 : 6);

        for (int i = 0; i < count; i++) {
            Element e = imageList.get(i);
            String contents = e.attributes().get("href");
            Matcher m = p.matcher(contents);

            while (m.find()) {
                String image = m.group(1);
                String prefix = m.group(2);
                String imageURL = (depth == 14 ? RadarHelper.c14ImageURL : RadarHelper.c8ImageURL) + prefix + "/" + image + ".GIF";

                if (!imageURLs.contains(imageURL)) {
                    imageURLs.add(imageURL);
                }
            }
        }
		return imageURLs;
	}


    public static Bitmap GetCanadaWideImage(Resources r) {
        ImageDownloaderThread imgDown;
        SourceFetcherThread fetcher = new SourceFetcherThread();
        fetcher.setBaseFetch();

        try {
            //Download the requested page
            fetcher.start();
            fetcher.join();

            List<String> allImages = parseRadarImages(fetcher.getSource(),"short",14);

            imgDown = new ImageDownloaderThread(baseURL + allImages.get(0));
            imgDown.start();
            imgDown.join();
        } catch (Exception ie) {
            return BitmapFactory.decodeResource(r, R.drawable.radar);
        }
        return imgDown.getImage();
    }
}
