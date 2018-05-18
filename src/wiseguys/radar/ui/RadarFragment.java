package wiseguys.radar.ui;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import wiseguys.radar.R;
import wiseguys.radar.helpers.RadarHelper;
import wiseguys.radar.RadarLoader;
import wiseguys.radar.helpers.GPSHelper;
import wiseguys.radar.ui.adapter.PhotoViewAttacher;

public class RadarFragment extends Fragment {

    private boolean useGPS;
    private GPSHelper gps;

    private MainActivity activity;
    private Context context;

    private SharedPreferences sharedPrefs;

    private RadarLoader loader;

    private PhotoViewAttacher adapter;

    public static int screenWidth;
    public static int screenHeight;
    public static float density;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = (MainActivity)getActivity();
        context = activity.getBaseContext();
        useGPS = false;

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.radar, container, false);
    }

    /**
     * Loads when returning to this screen
     */
    @Override
    public void onResume() {
        super.onResume();
        getScreenMetrics();
        refresh();
    }

    @Override
    public void onPause() {
        super.onPause();

        if (gps != null && gps.ready()) {
            gps.disable();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //cleanupAdapter();
    }

    /**
     * Re-receive the images on command
     */
    public void refresh() {
        TextView radarName = (TextView) activity.findViewById(R.id.radarName);
        ImageView sImage = (ImageView) activity.findViewById(R.id.radarImage);
        sImage.setScaleType(ImageView.ScaleType.FIT_XY);
        String selectedRadarCode = sharedPrefs.getString("pref_radar_code", "NAT");
        String codeToUse;

        //Pre-checks
        if (!validConnection()) {
            //Verify we have a network
            radarName.setText(getString(R.string.noNetwork));
            sImage.setImageResource(R.drawable.radar);

            return;
        }

        cleanupAdapter();
        checkAndCancelUpdate();
        GPSCheck();
        //--End of pre-checks

        //Setup Radar Image Download
        codeToUse = selectedRadarCode;

        if (useGPS) {
            if (gps.ready()) {
                //Assume we have a valid GPS setup now.
                codeToUse = gps.findClosestCity(gps.getLastLocation());
                if (codeToUse == null) codeToUse = selectedRadarCode;
            }
        } else {
            if (selectedRadarCode.equals("NAT")) radarName.setText(getString(R.string.setPreferences));
        }

        String selectedDuration = sharedPrefs.getString("pref_radar_dur", "short");
        String selectedRadarName = codeToUse.equals("NAT") ? getString(R.string.setPreferences) : RadarHelper.codeToName(codeToUse, context);
        radarName.setText(selectedRadarName);

        //Put it all together and run the query
        loader = new RadarLoader(context, this.getResources(), sImage, radarName, adapter);
        loader.execute(codeToUse, selectedDuration);
    }

    /**
     * Set public variables for screen dimensions
     */
    private void getScreenMetrics() {
        WindowManager winManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = winManager.getDefaultDisplay();
        DisplayMetrics point = new DisplayMetrics();
        display.getMetrics(point);

        screenWidth = point.widthPixels;
        screenHeight = point.heightPixels;
        density = point.density;
    }

    /**
     * Check for any active networks such as wifi or cellular
     * @return true when a valid connection is available
     */
    private boolean validConnection() {
        boolean status = false;
        try {
            ConnectivityManager connManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo networkInfo0 = connManager.getNetworkInfo(0);
            NetworkInfo networkInfo1 = connManager.getNetworkInfo(1);

            status = ((networkInfo0 != null && networkInfo0.getState() == NetworkInfo.State.CONNECTED) ||
                      (networkInfo1 != null && networkInfo1.getState() == NetworkInfo.State.CONNECTED));
        } catch (Exception e) {
            Log.e("WiseRadar","Exception while checking for valid networks: " + e.getMessage());
        } finally {
            return status;
        }
    }

    /**
     * Dispose of the PhotoViewAdapter (Used for Zoom controls)
     */
    public void cleanupAdapter() {
        if (adapter != null) {
            adapter.cleanup();
            adapter = null;
        }
    }

    /**
     * Setup the GPS variables and initialize adapter if required
     */
    private void GPSCheck() {
        useGPS = sharedPrefs.getBoolean("gps", false);
        if (useGPS) {
            if (gps == null) { gps = new GPSHelper(activity); }
            if (!gps.ready()) { gps.setup(); }
        }
    }

    /**
     * Checks the status of our loader and cancels it in the situation it is still running
     */
    public void checkAndCancelUpdate() {
        if (loader == null) return;
        if (loader.getStatus() == AsyncTask.Status.RUNNING) loader.cancel(true);
        if (useGPS && gps != null) gps.disable();
    }
}