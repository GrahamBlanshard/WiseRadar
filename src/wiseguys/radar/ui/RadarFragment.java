package wiseguys.radar.ui;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
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

        useGPS = sharedPrefs.getBoolean("gps", false);

        if (useGPS) {
            if (gps == null) {
                gps = new GPSHelper(activity);
            }

            if (!gps.ready()) {
                gps.setup();
            }
        }

        getScreenMetrics();
        refresh();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (gps != null && gps.ready())
            gps.disable();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        cleanupAdapter();
    }

    /**
     * Re-receive the images on command
     */
    public void refresh() {
        TextView warn = (TextView) activity.findViewById(R.id.locationWarning);
        TextView radarName = (TextView) activity.findViewById(R.id.radarName);
        ImageView sImage = (ImageView) activity.findViewById(R.id.radarImage);
        String selectedRadarCode = sharedPrefs.getString("pref_radar_code", "NAT");
        String codeToUse;

        //Verify we have a network
        if (!validConnection()) {
            radarName.setText(getString(R.string.noNetwork));
            sImage.setImageResource(R.drawable.radar);
            updateAdapter(sImage);
            return;
        }

        checkAndCancelUpdate();
        codeToUse = selectedRadarCode;

        if (useGPS) {
            if (gps.ready()) {
                //Assume we have a valid GPS setup now.
                codeToUse = gps.findClosestCity(gps.getLastLocation());

                if (codeToUse == null) {
                    codeToUse = selectedRadarCode;
                }
            }
        } else {
            if (selectedRadarCode.equals("NAT")) {
                radarName.setText(getString(R.string.setPreferences));
            }
        }

        String selectedDuration = sharedPrefs.getString("pref_radar_dur", "short");
        String selectedRadarName = codeToUse.equals("NAT") ? getString(R.string.setPreferences) : RadarHelper.codeToName(codeToUse, context);

        //Error: Could not find radar code
        if (selectedRadarName == null) {
            radarName.setText(getString(R.string.noLocation));
            sImage.setImageResource(R.drawable.radar);

            return;
        }

        radarName.setText(selectedRadarName);

        //Put it all together
        loader = new RadarLoader(context, this.getResources(), sImage, radarName);
        loader.execute(codeToUse, selectedDuration);

        ViewGroup.LayoutParams layoutParams = sImage.getLayoutParams();

        if (layoutParams != null) {
            //noinspection SuspiciousNameCombination
            layoutParams.height = screenWidth;
            layoutParams.width = screenWidth;
            sImage.setLayoutParams(layoutParams);
        }

        warn.setVisibility(sharedPrefs.getBoolean("gps",false) ? View.VISIBLE : View.INVISIBLE);

        updateAdapter(sImage);
    }

    private void getScreenMetrics() {
        WindowManager winManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = winManager.getDefaultDisplay();
        DisplayMetrics point = new DisplayMetrics();
        display.getMetrics(point);

        screenWidth = point.widthPixels;
        screenHeight = point.heightPixels;
        density = point.density;
    }

    private boolean validConnection() {
        boolean status;
        try {
            ConnectivityManager connManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo0 = connManager.getNetworkInfo(0);
            NetworkInfo networkInfo1 = connManager.getNetworkInfo(1);

            status = ((networkInfo0 != null && networkInfo0.getState() == NetworkInfo.State.CONNECTED) ||
                    (networkInfo1 != null && networkInfo1.getState() == NetworkInfo.State.CONNECTED));
        } catch (Exception e) {
            return false;
        }

        return status;
    }

    /**
     * Do an update to the PhotoViewAdapter (or create if necessary)
     *
     * @param img ImageView we are attaching to
     */
    private void updateAdapter(ImageView img) {
        if (adapter == null) {
            adapter = new PhotoViewAttacher(img);
            adapter.setScaleType(ImageView.ScaleType.CENTER);
        }

        float zoomWidth = (screenHeight > screenWidth) ? screenWidth * 0.95f : screenHeight * 0.80f;
        float scale = zoomWidth / (float) img.getDrawable().getIntrinsicWidth();
        adapter.zoomTo(scale, (float) img.getDrawable().getIntrinsicWidth() / 2.0f, (float) img.getDrawable().getIntrinsicWidth() / 2.0f);
        adapter.update();

    }

    public void cleanupAdapter() {
        if (adapter != null) {
            adapter.cleanup();
            adapter = null;
        }
    }

    /**
     * Checks the status of our loader and cancels it in the situation it is still running
     */
    public void checkAndCancelUpdate() {
        if (loader == null) {
            return; //Nothing to cancel
        }

        if (loader.getStatus() == AsyncTask.Status.RUNNING) {
            loader.cancel(true);
        }

        //Remove GPS if used
        if (useGPS && gps != null) {
            gps.disable();
        }
    }
}