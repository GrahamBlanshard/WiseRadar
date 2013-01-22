package wiseguys.radar;

import android.util.Log;

public class SourceParser {

	private String code;	
	
	public SourceParser(String sourceCode) {
		this.code = sourceCode;
	}
	
	public String parseRadarImages() {
		String temp = code.substring(code.indexOf("<div class=\"image list\">"));
		temp = temp.substring(temp.indexOf("<li><a href"),temp.indexOf("</ol>"));
		String images = "";
		int NUM_PICS = 6;//(R.integer.image_count);TODO: FIX
		
		//Find the bitmap strings
		for (int i = 0; i < NUM_PICS; i++) {
			temp = temp.substring(temp.indexOf("display="));
			Log.d("ImageFetcher",temp.substring(temp.indexOf('/'),temp.indexOf("\"")) + "|");
			images += temp.substring(temp.indexOf('/'),temp.indexOf("\"")) + "|";
			temp = temp.substring(temp.indexOf("\""));			
		}
		
		return images;
	}
	
	
}
