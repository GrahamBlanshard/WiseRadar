package wiseguys.radar;

import android.content.Context;
import android.util.Log;

public class RadarHelper {
	
	public static final String baseURL = "http://www.weatheroffice.gc.ca";
	
	public static String codeToName(String code, Context systemContext) {
		//Test to make sure we're ready to accept it
		if (systemContext == null) {
			Log.e("RadarHelper","You have not yet set the system context");
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

}
