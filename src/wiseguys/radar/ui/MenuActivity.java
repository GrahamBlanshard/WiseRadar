package wiseguys.radar.ui;

import wiseguys.radar.ui.adapter.PhotoViewAttacher;
import wiseguys.radar.R;
import wiseguys.radar.RadarHelper;
import wiseguys.radar.RadarLoader;
import wiseguys.radar.conn.GPSHelper;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

public class MenuActivity extends Activity {

    private SharedPreferences sharedPrefs;
    private RadarLoader loader;
    private GPSHelper gps;
    private boolean useGPS;
    private PhotoViewAttacher adapter;

    private int screenWidth;
    private int screenHeight;
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu);

        getScreenSize();

        useGPS = false;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_quit:
                quit();
                return true;
            case R.id.menu_about:
                showAbout();
                return true;
            case R.id.menu_refresh:
                refresh();
                return true;
            case R.id.menu_preferences:
                showPreferences();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * On quit print log message
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * On halt, print log message
     */
    @Override
    protected void onStop() {
        super.onStop();
    }

    /**
     * Loads when returning to this screen
     */
    @Override
    protected void onResume() {
        super.onResume();

        useGPS = sharedPrefs.getBoolean("gps", false);

        if (useGPS) {
            if (gps == null) {
                gps = new GPSHelper(this);
            }

            if (!gps.ready()) {
                gps.setup();
            }
        }

        refresh();
    }

    /**
     * Creates our menu
     */
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.radar_menu, menu);
        return true;
    }

    /**
     * Activity Pause -- Cancel any updates and disable GPS
     */
    @Override
    protected void onPause() {
        super.onPause();
        checkAndCancelUpdate();

        //Remove GPS if used
        if (useGPS && gps != null) {
            gps.disable();
        }
    }

    /**
     * Re-receive the images on command
     */
    private void refresh() {

        TextView radarName = (TextView) findViewById(R.id.radarName);
        ImageView sImage = (ImageView) findViewById(R.id.radarImage);
        String selectedRadarCode = sharedPrefs.getString("pref_radar_code", "new");
        String codeToUse;

        //Verify we have a network
        if (!validConnection()) {
            radarName.setText("No valid Networks");
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
            if (selectedRadarCode.equals("new")) {
                radarName.setText("Set preferences using the Menu!");
                Bitmap canadaWide = RadarHelper.GetCanadaWideImage(getResources());
                sImage.setImageBitmap(canadaWide);
                updateAdapter(sImage);
                return;
            }
        }


        String selectedDuration = sharedPrefs.getString("pref_radar_dur", "short");
        String selectedRadarName = RadarHelper.codeToName(codeToUse, this.getBaseContext());

        if (selectedRadarName == null) {
            radarName.setText("No Location Selected");
            sImage.setImageResource(R.drawable.radar);

            return;
        }

        radarName.setText(selectedRadarName);

        //Put it all together
        loader = new RadarLoader(this.getBaseContext(), this.getResources(), sImage, radarName);
        loader.execute(codeToUse, selectedDuration);


        ViewGroup.LayoutParams layoutParams = sImage.getLayoutParams();

        if (layoutParams != null) {
            //noinspection SuspiciousNameCombination
            layoutParams.height = screenWidth;
            layoutParams.width = screenWidth;
            sImage.setLayoutParams(layoutParams);
        }

        updateAdapter(sImage);
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

    /**
     * Checks the status of our loader and cancels it in the situation it is still running
     */
    private void checkAndCancelUpdate() {
        if (loader == null) {
            return; //Nothing to cancel
        }

        if (loader.getStatus() == AsyncTask.Status.RUNNING) {
            loader.cancel(true);
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            quit();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MENU) {
            openOptionsMenu();
            return true;
        }
        return false;
    }

    private boolean validConnection() {
        boolean status;
        try {
            ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo0 = connManager.getNetworkInfo(0);
            NetworkInfo networkInfo1 = connManager.getNetworkInfo(1);

            status = ((networkInfo0 != null && networkInfo0.getState() == NetworkInfo.State.CONNECTED) ||
                    (networkInfo1 != null && networkInfo1.getState() == NetworkInfo.State.CONNECTED));
        } catch (Exception e) {
            return false;
        }

        return status;
    }

    private void showAbout() {
        Intent myIntent = new Intent(this.getBaseContext(), AboutActivity.class);
        startActivity(myIntent);
    }

    private void showPreferences() {
        Intent myIntent = new Intent(this.getBaseContext(), PrefActivity.class);
        startActivity(myIntent);
    }

    /**
     * Closes our application
     */
    private void quit() {
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);

        finish();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        adapter.cleanup();
        adapter = null;

        setContentView(R.layout.menu);
        getScreenSize();
        refresh();
    }

    private void getScreenSize() {
        WindowManager winManager = (WindowManager) getBaseContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = winManager.getDefaultDisplay();
        DisplayMetrics point = new DisplayMetrics();
        display.getMetrics(point);

        screenWidth = point.widthPixels;
        screenHeight = point.heightPixels;
    }
}
