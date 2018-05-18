package wiseguys.radar.conn;
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import wiseguys.radar.R;
import wiseguys.radar.RadarImage;
import wiseguys.radar.helpers.RadarHelper;

public class SourceFetcherThread extends Thread {

	private String code;
	private List<RadarImage> newImages;
	private boolean getBaseImage;
	
	public SourceFetcherThread() {
		code = null;
        newImages = new ArrayList<RadarImage>();
		getBaseImage = false;
	}

    public String getCode() {
        return code;
    }
	public void setCode(String code) {
		this.code = code;
	}
	public List<RadarImage> getNewImages() {
		return newImages;
	}
	
	public void run() {
		if (code != null || getBaseImage) {
			//Resources res = context.getResources();
            String jsonURL = String.format(RadarHelper.jsonURLRaw, (!getBaseImage && code != null) ? code : "NAT");
            getBaseImage = false;

            try {
                //Using HTTP client to fetch Weather Canada content
                HttpClient client = new DefaultHttpClient();
                HttpGet get = new HttpGet(jsonURL);

                //Hacky header addition to get proper response from webpage
                get.addHeader("X-Requested-With","XMLHttpRequest");
                HttpResponse response = client.execute(get);
                StatusLine retStatus = response.getStatusLine();

                if (retStatus.getStatusCode() == 200) {
                    HttpEntity pageContent = response.getEntity();
                    InputStream is = pageContent.getContent();

                    try {
                        Reader responseReader = new InputStreamReader(is, "UTF-8");
                        Gson json = new GsonBuilder().create();
                        JsonReader rawSrc = json.newJsonReader(responseReader);

                        if (rawSrc.hasNext()) {
                            rawSrc.beginObject();
                            rawSrc.skipValue();
                            rawSrc.beginArray();

                            while (rawSrc.hasNext()) {
                                RadarImage newImage = json.fromJson(rawSrc, RadarImage.class);
                                newImages.add(newImage);
                            }
                        }

                        rawSrc.close();
                        /*
                           http://weather.gc.ca/radar/xhr.php?
                            action=retrieve&
                            target=images&
                            region=__CODE___&
                            product=PRECIP_RAIN&
                            lang=en-CA&
                            format=json&
                            rand=0.4816295127364810
                         */
                    } catch (Exception ex) {
                        Log.e("WiseRadar", "Parsing HTML Source: " + ex);
                        newImages = null;
                    }
                }
		    } catch (Exception ee) {
                Log.e("WiseRadar","Exception while downloading HTML content: " + ee);
                newImages = null;
            }
        }
    }
	
	public void setBaseFetch() {
		getBaseImage = true;
	}
}
