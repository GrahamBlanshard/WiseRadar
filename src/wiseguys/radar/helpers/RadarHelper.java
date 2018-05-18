package wiseguys.radar.helpers;

import wiseguys.radar.R;
import android.content.Context;
import android.location.Location;

public class RadarHelper {
	
	public static final String baseURL = "http://weather.gc.ca"; 	//http://weather.gc.ca/radar/index_e.html?id=<CODE>
    public static Location latestLocation;

    public static final String jsonURLRaw = "http://weather.gc.ca/radar/xhr.php?action=retrieve&target=images&region=%s&format=json";

    public static final int TEN_MINUTES = 60000;

	public static String codeToName(String code, Context systemContext) {
		String defaultReturn = "NAT";
		if (systemContext == null) {
			//Test to make sure we're ready to accept it
			return defaultReturn;
		}

		if (code == null) { return defaultReturn; }

		String[] radarCodes = systemContext.getResources().getStringArray(R.array.radar_codes);
		String[] radarNames = systemContext.getResources().getStringArray(R.array.radar_cities);

		int index;
		for (index = 0; index < radarCodes.length; index++) {
			if (radarCodes[index].equals(code)) { break; }
		}

		if (index != radarCodes.length) {
			defaultReturn = radarNames[index];
		}
		
		return defaultReturn;
	}
}
