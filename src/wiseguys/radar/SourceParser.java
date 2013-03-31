package wiseguys.radar;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;

public class SourceParser {

	private String code;	
	
	public SourceParser(String sourceCode) {
		this.code = sourceCode;
	}
	
	/**
	 * Breaks apart the page source to retrieve gif images
	 * @return a collated collection of gif image names from the URL source
	 */
	public String parseRadarImages() {
		String temp = code.substring(code.indexOf("<div class=\"image list\">"));
		temp = temp.substring(0,temp.indexOf("</div>"));
		
		//At times, the Env. Canada page does not have available data
		if (!temp.contains("li")) {
			return null;
		}	
		
		temp = temp.substring(temp.indexOf("<li><a href"),temp.indexOf("</ol>"));
		String images = "";			
		
		Pattern p = Pattern.compile("display=(.*)\\\"");
		Matcher m = p.matcher(temp);
		
		while (m.find()) {
			String imgName = m.group(1);
			images += imgName + "|";
			Log.d("ImageFetcher",imgName);
		}
		
		return images;
	}
}
