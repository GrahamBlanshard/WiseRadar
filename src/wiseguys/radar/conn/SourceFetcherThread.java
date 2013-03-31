package wiseguys.radar.conn;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import wiseguys.radar.RadarHelper;

import android.util.Log;

public class SourceFetcherThread extends Thread {

	private String code;
	private String duration;
	private String source;
	
	
	public SourceFetcherThread() {
		code = null;
		source = null;
	}
	
	public void setCode(String code) {
		this.code = code;
	}
	
	public void setDuration(String duration) {
		this.duration = duration;
	}
	
	public String getCode() {
		return code;
	}
	
	public String getSource() {
		return source;
	}
	
	public void run() {
		if (code != null) {
			try {
				source = getHtml();
			} catch (IOException ioe) {
				Log.e("HTML_ERR","IOException reported on getHtml. " + ioe.getLocalizedMessage());
			} finally {
				if (source == null)
					source = "Error!";
			}
		}
	}
	
	private String getHtml() throws ClientProtocolException, IOException
	{
		String durationAddition = "";
		if (duration.equals("long")) {
			durationAddition = "&duration=long";
		}
		String envURL = RadarHelper.baseURL + "/radar/index_e.html?id=" + code + durationAddition;
		Log.i("WiseRadar","Loading page for parsing: " + envURL);
		
	    HttpClient httpClient = new DefaultHttpClient();
	    HttpContext localContext = new BasicHttpContext();
	    
	    HttpGet httpGet = new HttpGet(envURL);
	    HttpResponse response = httpClient.execute(httpGet, localContext);
	    StringBuilder result = new StringBuilder();

	    BufferedReader reader = new BufferedReader(
	        new InputStreamReader(
	          response.getEntity().getContent()
	        )
	      );

	    String line = null;
	    while ((line = reader.readLine()) != null){
	      result.append(line + "\n");
	    }

	    return result.toString();
	}
}
