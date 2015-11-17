package wiseguys.radar.conn;
import android.util.Log;

import java.io.IOException;

import org.jsoup.*;
import org.jsoup.nodes.Document;

import wiseguys.radar.helpers.RadarHelper;

public class SourceFetcherThread extends Thread {

	private String code;
	private Document htmlSource;
	private boolean getBaseImage;
	
	
	public SourceFetcherThread() {
		code = null;
        htmlSource = null;
		getBaseImage = false;
	}
	
	public void setCode(String code) {
		this.code = code;
	}
	
	public String getCode() {
		return code;
	}
	
	public Document getSource() {
		return htmlSource;
	}
	
	public void run() {
		if (code != null || getBaseImage) {

			String envURL = RadarHelper.baseURL;

            //Get a specific radar code
            envURL += "/radar/index_e.html?id=";
            envURL += (!getBaseImage && code != null) ? code : "NAT";
            getBaseImage = false;

            try {
                Connection c = Jsoup.connect(envURL);
                htmlSource = c.get();
			} catch (IOException ioe) {
                Log.e("WiseRadar","IOException Catching HTML Source");
                htmlSource = null;
			}
		}
	}
	
	public void setBaseFetch() {
		getBaseImage = true;
	}
}
