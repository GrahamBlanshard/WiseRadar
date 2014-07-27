package wiseguys.radar.ui;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import wiseguys.radar.R;

public class MainActivity extends Activity {

    private RadarFragment radarFragment;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        if (findViewById(R.id.main_container) != null) {
            if (savedInstanceState != null) {
                return;
            }

            radarFragment = new RadarFragment();
            radarFragment.setArguments(getIntent().getExtras());

            getFragmentManager().beginTransaction()
                    .add(R.id.main_container, radarFragment, "radar_fragment")
                    .commit();
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_quit:
                quit();
                return true;
            case R.id.menu_about:
                //Show about page
                AboutFragment aboutFragment = new AboutFragment();

                getFragmentManager().beginTransaction()
                        .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                        .replace(R.id.main_container, aboutFragment, "about_fragment")
                        .addToBackStack(null)
                        .commit();
                return true;
            case R.id.menu_refresh:
                radarFragment.refresh();
                return true;
            case R.id.menu_preferences:
                //Show preferences
                PrefFragment prefFragment = new PrefFragment();

                getFragmentManager().beginTransaction()
                                    .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                                    .replace(R.id.main_container, prefFragment, "pref_fragment")
                                    .addToBackStack(null)
                                    .commit();
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
     * Creates our main
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
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ( getFragmentManager().getBackStackEntryCount() == 0 ) {
                quit();
            } else {
                getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MENU) {
            openOptionsMenu();
            return true;
        }
        return false;
    }

    /**
     * Closes our application
     */
    private void quit() {
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);

        finish();
    }
}
