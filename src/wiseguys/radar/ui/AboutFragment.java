package wiseguys.radar.ui;

import wiseguys.radar.R;
import wiseguys.radar.RadarHelper;
import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class AboutFragment extends Fragment {
    private static final int ABOUT_AUTHOR_ID = 0;
    private static final int ABOUT_APP_ID = 1;
    private static final int ABOUT_LATEST = 2;
    private static final int ABOUT_LICENSE_ID = 3;

    private MainActivity activity;
    private Button authorButton;
    private Button appButton;
	private Button licenceButton;
    private Button latestButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);

        activity = (MainActivity)getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.about, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();

        if ( authorButton == null ) {
            authorButton = (Button) activity.findViewById(R.id.author);
            authorButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showAboutText(ABOUT_AUTHOR_ID);
                }
            });
        }

        if (appButton == null) {
            appButton = (Button) activity.findViewById(R.id.app);
            appButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showAboutText(ABOUT_APP_ID);
                }
            });
        }

        if (latestButton == null) {
            latestButton = (Button) activity.findViewById(R.id.latest);
            latestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showAboutText(ABOUT_LATEST);
                }
            });
        }

        if (licenceButton == null) {
            licenceButton = (Button) activity.findViewById(R.id.license);
            licenceButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showAboutText(ABOUT_LICENSE_ID);
                }
            });
        }
    }


    public void showAboutText (int id) {
        String about;
        TextView authorButton = (TextView) activity.findViewById(R.id.about_text);
        switch(id) {
        case ABOUT_AUTHOR_ID:
        	about = "All app programming by Graham Blanshard\n\n   http://www.pro-graham.com/\n   graham.blanshard@gmail.com\n\nIf you have any questions or would like to see any features " +
        	"please contact me at one of these locations.\n\nPinchZoom and Scroll functions (added v1.2) are provided by the PhotoView project by Chris Banes\n\n";
            break;
        case ABOUT_LICENSE_ID:
        	//http://opensource.org/licenses/GPL-3.0
        	about = "Copyright (C) 2014 Graham Blanshard\n\n" +
        						"This program is free software: you can redistribute it and/or modify\n"+
								    "it under the terms of the GNU General Public License as published by\n"+
								    "the Free Software Foundation, either version 3 of the License, or\n"+
								    "(at your option) any later version.\n\n" + 
        						"This program is distributed in the hope that it will be useful,\n"+
								   "but WITHOUT ANY WARRANTY; without even the implied warranty of\n"+
								    "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\n"+
								    "GNU General Public License for more details.\n\n"+
								"You should have received a copy of the GNU General Public License\n"+    
								    "along with this program.  If not, see http://www.gnu.org/licenses/.\n\n"+
                                "PinchZoom and Scroll functionality has been provided as part of the PhotoView\n"+
                                    "project available on GitHub (https://github.com/chrisbanes/PhotoView)\n"+
                                    "licensed under the Apache License v2.0. See documentation for details\n\n";
            break;
        case ABOUT_APP_ID:
        	about = "WiseRadar is an open source mobile portal to view Environment Canada weather radar images. " +
        					   "\nAll radar imagry is provided free on behalf of Environment Canada for non-commercial uses. If you wish to know more about Environment" +
        					   " Canada, the radar images, or the use of this data please visit their website:\n\n"+RadarHelper.baseURL+"\n\nThis app's source code can "+
        					   " be located online at https://github.com/GrahamBlanshard/WiseRadar\n\n";
            break;
        case ABOUT_LATEST:
            about = "v2.0 (July 2014)\n" +
                    "- Option to show current location on radar\n" +
                    "- Numerous bug fixes\n" +
                    "- More modern UI handling\n\n";
            break;
        default:
            about = "";
        }

        authorButton.setText(about);
    }
}
