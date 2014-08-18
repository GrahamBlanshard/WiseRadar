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

import wiseguys.radar.helpers.RadarHelper;

public class SourceFetcherThread extends Thread {

	private String code;
	private String duration;
	private String source;
	private boolean getBaseImage;
	
	
	public SourceFetcherThread() {
		code = null;
		source = null;
		getBaseImage = false;
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
		if (code != null || getBaseImage) {
			try {
				if (!getBaseImage) {
					source = getHtml();
				} else {
					//We do this as a one time fetch for when we need to get the Canada Wide image
					source = getCanadaImage();
					getBaseImage = false;
				}
			} catch (IOException ioe) {
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
	
	private String getCanadaImage() throws ClientProtocolException, IOException {

		String envURL = RadarHelper.baseURL + "/radar/";
		
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
	
	public void setBaseFetch() {
		getBaseImage = true;
	}
}
