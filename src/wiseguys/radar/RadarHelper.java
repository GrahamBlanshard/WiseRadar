package wiseguys.radar;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import wiseguys.radar.conn.ImageDownloaderThread;
import wiseguys.radar.conn.SourceFetcherThread;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class RadarHelper {
	
	public static final String baseURL = "http://weather.gc.ca";//"http://www.weatheroffice.gc.ca";
	//http://weather.gc.ca/radar/index_e.html?id=<CODE>
	public static final int TEN_MINUTES = 600000;
	
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
			throw new IllegalArgumentException();
		}
		
		
		return radarNames[index];
	}
	
	/**
	 * Breaks apart the page source to retrieve gif images
	 * @param code HTML code of page we're looking to parse
	 * @return a collated collection of gif image names from the URL source
	 */
	public static List<String> parseRadarImages(String code) {
		List<String> imageURLs = new ArrayList<String>();
		
		if (!code.contains("image-list-ol")) {
			return null;
		}
		
		String temp = code.substring(code.indexOf("<ol class=\"image-list-ol\">"));
		temp = temp.substring(0,temp.indexOf("</ol>"));
		
		//At times, the Env. Canada page does not have available data
		if (!temp.contains("<li>")) {			
			return null;
		}	
		
		temp = temp.substring(temp.indexOf("<li><a href"),temp.lastIndexOf("</li>"));	
		
		Pattern p = Pattern.compile("display=(.*)&amp");
		Matcher m = p.matcher(temp);
		
		while (m.find()) {
			String imgName = m.group(1);
			imageURLs.add(imgName);
		}
		
		return imageURLs;
	}
	
	public static Bitmap GetCanadaWideImage(Resources r) {
		ImageDownloaderThread imgDown;
		SourceFetcherThread fetcher = new SourceFetcherThread();
		fetcher.setBaseFetch();		
		String basicSource = null;

		try {
			fetcher.start();
			fetcher.join();
			basicSource = fetcher.getSource();
			List<String> allImages = parseRadarImages(basicSource);
			
			imgDown = new ImageDownloaderThread(baseURL + allImages.get(0));
			imgDown.start();
			imgDown.join();
		} catch (Exception ie) {		
			return BitmapFactory.decodeResource(r, R.drawable.radar);
		}
		return (Bitmap)imgDown.getImage();
	}
}
