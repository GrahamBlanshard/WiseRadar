package wiseguys.radar.ui;

import wiseguys.radar.R;
import wiseguys.radar.RadarHelper;
import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class AboutActivity extends Activity {
    private static final int DIALOG_AUTHOR_ID = 0;
    private static final int DIALOG_APP_ID = 1;
    private static final int DIALOG_LICENSE_ID = 2;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.about);   
        
        Button authorButton = (Button) findViewById(R.id.author);        
        authorButton.setOnClickListener(new View.OnClickListener() {        				
			@Override
			public void onClick(View v) {
				showDialog(DIALOG_AUTHOR_ID);
			}
		});
        
        Button appButton = (Button) findViewById(R.id.app);        
        appButton.setOnClickListener(new View.OnClickListener() {        				
			@Override
			public void onClick(View v) {
				showDialog(DIALOG_APP_ID);
			}
		});
        
        Button licenceBtn = (Button) findViewById(R.id.license);        
        licenceBtn.setOnClickListener(new View.OnClickListener() {        				
			@Override
			public void onClick(View v) {
				showDialog(DIALOG_LICENSE_ID);
			}
		});
    }
    
    @Override
    public Dialog onCreateDialog (int id) {
        Dialog dialog = new Dialog(this);;
        TextView text = null;
        switch(id) {
        case DIALOG_AUTHOR_ID:
        	dialog.setTitle("About the Author");
        	dialog.setContentView(R.layout.dialog_layout);
        	dialog.setCanceledOnTouchOutside(true);
        	text = (TextView) dialog.findViewById(R.id.text);
        	text.setText("All radar by Graham Blanshard\n\n   http://www.pro-graham.com/\n   graham.blanshard@gmail.com\n\nIf you have any questions or would like to see any features " +
        	"please contact me at one of these locations.\n\nPinchZoom and Scroll functions (added v1.2) are provided by the PhotoView project by Chris Banes");
            break;
        case DIALOG_LICENSE_ID:
        	dialog.setTitle("GPL v3.0"); //http://opensource.org/licenses/GPL-3.0
        	dialog.setContentView(R.layout.dialog_layout);
        	dialog.setCanceledOnTouchOutside(true);
        	text = (TextView) dialog.findViewById(R.id.text);
        	String licenceText = "Copyright (C) 2013 Graham Blanshard\n\n" + 
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
                                    "licensed under the Apache License v2.0. See documentation for details";
        	text.setText(licenceText);
        	break;        	
        case DIALOG_APP_ID:
        	dialog.setTitle("WiseRadar");
        	dialog.setContentView(R.layout.dialog_layout);
        	dialog.setCanceledOnTouchOutside(true);
        	text = (TextView) dialog.findViewById(R.id.text);
        	String textValue = "WiseRadar " + this.getResources().getString(R.string.version) + " is an open source mobile portal to view Environment Canada weather radar images. " +
        					   "\nAll radar imagry is provided free on behalf of Environment Canada for non-commercial uses. If you wish to know more about Environment" +
        					   " Canada, the radar images, or the use of this data please visit their website:\n\n"+RadarHelper.baseURL+"\n\nThis app's source code can "+
        					   " be located online at https://github.com/GrahamBlanshard/WiseRadar";
        	text.setText(textValue);        	
            break;
        default:
            dialog = null;
        }
        return dialog;
    }
}
