package kz.bapps.e_concrete;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xwalk.core.JavascriptInterface;
import org.xwalk.core.XWalkPreferences;
import org.xwalk.core.XWalkView;
import org.xwalk.core.internal.XWalkCookieManager;

import java.util.Date;
import java.util.List;

import kz.bapps.e_concrete.auth.AuthActivity;
import kz.bapps.e_concrete.chat.ChatActivity;
import kz.bapps.e_concrete.model.LocationModel;
import kz.bapps.e_concrete.model.MenuItemModel;
import kz.bapps.e_concrete.model.UserModel;
import kz.bapps.e_concrete.signature.KalkanHelper;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        KalkanHelper.OnKalkanHelperListener,
        FileChooserFragment.OnFileSelectedListener{

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    // Keys for storing activity state in the Bundle.
    protected final static String LOCATION_KEY = "location-key";

    /**
     * ГЛАВНЫЙ ФАЙЛ ЗАГРУЗЧИК
     */
    protected final static String WEB_INDEX = "static/mobile.html"; //

    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;

    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    protected LocationRequest mLocationRequest;

    /**
     * Represents a geographical location.
     */
    protected Location mCurrentLocation;

    /**
     * ОТВЕТ ДЛЯ ВЫБОРКИ ФАЙЛА
     */
    private static final int REQUEST_PICK_FILE = 1;

    private XWalkView xWalkWebView;
    private XWalkCookieManager mCookieManager;
    private RelativeLayout webContainer;
    protected static String LOG_TAG = "MainActivity";
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;


    /**
     * Builds a GoogleApiClient. Uses the {@code #addApi} method to request the
     * LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        Log.i(LOG_TAG, "Building GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        createLocationRequest();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        mGoogleApiClient.connect();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://kz.bapps.e_concrete/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Within {@code onPause()}, we pause location updates, but leave the
        // connection to GoogleApiClient intact.  Here, we resume receiving
        // location updates if the user has requested them.

        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
        }
        if (xWalkWebView != null) {
            xWalkWebView.resumeTimers();
            xWalkWebView.onShow();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop location updates to save battery, but don't disconnect the GoogleApiClient object.
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }
        if (xWalkWebView != null) {
            xWalkWebView.pauseTimers();
            xWalkWebView.onHide();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://kz.bapps.e_concrete/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.disconnect();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /** Проверка на включение GPS
         ------------------------------*/
        if (!EConcrete.isGpsEnabled(this)) {
            displayPromptForEnablingGPS(this);
        }

        /** Бинд элемента XWalkWebView
         ------------------------------*/
        webContainer = (RelativeLayout) findViewById(R.id.webView);
        xWalkWebView = (XWalkView)findViewById(R.id.xwalkWebView);
        xWalkWebView.clearCache(false);

        // Your can use this inside the onCreate() method
        mCookieManager = new XWalkCookieManager();
        mCookieManager.setAcceptCookie(true);
        mCookieManager.setAcceptFileSchemeCookies(true);

        CookieManager cookieManager = CookieManager.getInstance();
        mCookieManager.setCookie(JSONParser.URL_ROOT, cookieManager.getCookie(JSONParser.URL_ROOT));

        xWalkWebView.load(JSONParser.URL_ROOT + WEB_INDEX, null);

        xWalkWebView.addJavascriptInterface(new KalkanHelper(this), "Mobile");
        xWalkWebView.addJavascriptInterface(this, "MobileMap");

        // turn on debugging
        XWalkPreferences.setValue(XWalkPreferences.REMOTE_DEBUGGING, true);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        /** Инициализация меню
         -----------------------------*/
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        /** Список меню из БД
         -----------------------------*/
        List<MenuItemModel> menuItems = new Select().from(MenuItemModel.class).execute();

        /** Добавляем динамическое меню
         -----------------------------*/
        int order = 0; // счетчик для порядка
        for (final MenuItemModel menuItem : menuItems) {
            navigationView.getMenu().add(Menu.NONE, menuItem.id, order++,
                    menuItem.titleRu.isEmpty() ? menuItem.title : menuItem.titleRu);
        }


        /** Set Image
         ------------------------------*/

        UserModel user = EConcrete.getInstance(this).getCurrentUser();

        if (user != null) {

            Log.d(LOG_TAG, "EMAIL = " + user.email);
            /**
             *      ШАПКА МЕНЮ
             */
            View headerLayout = navigationView.getHeaderView(0);
            ImageView imageView = (ImageView) headerLayout.findViewById(R.id.imageView);
            imageView.setAdjustViewBounds(true);
            TextView textViewName = (TextView) headerLayout.findViewById(R.id.textViewName);
            TextView textViewEmail = (TextView) headerLayout.findViewById(R.id.textViewEmail);

            Picasso.with(this)
                    .load(JSONParser.URL_ROOT + "/getfile?code=" + user.userPicFile)
                    .placeholder(R.drawable.ic_default_logo) // optional
                    .error(R.drawable.ic_default_logo)
                    .into(imageView);

            textViewName.setText(user.name);
            textViewEmail.setText(user.email);
        }
        /** Построение ГуглАпи клиента
         ------------------------------*/
        buildGoogleApiClient();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        webContainer.removeAllViews();
        if (xWalkWebView != null) {
            xWalkWebView.onDestroy();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (xWalkWebView != null) {
            xWalkWebView.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (xWalkWebView != null) {
            xWalkWebView.onNewIntent(intent);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if(!xWalkWebView.isActivated()) {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_profile) {
            UserModel user = EConcrete.getInstance(this).getCurrentUser();
            xWalkWebView.load(JSONParser.URL_ROOT + WEB_INDEX + "#/settings/userdetails/" + user.id, null);
        } else if(id == R.id.action_eds) {

            // And whereever you want to start the Fragment:
            FileChooserFragment fileFragment = new FileChooserFragment();
            fileFragment.show(getFragmentManager(), "fileChooser");
        } else if (id == R.id.action_chat) {
            Intent intent = new Intent(this, ChatActivity.class);
            startActivity(intent);
        } else if (id == R.id.action_exit) {
            xWalkWebView.load(JSONParser.URL_ROOT + "auth/logout", null);
            Intent intent = new Intent(this, AuthActivity.class);
            startActivity(intent);
            finish();
        } else {
            return super.onOptionsItemSelected(item);
        }

        return true;

    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        MenuItemModel menuItem = new Select()
                .from(MenuItemModel.class)
                .where("item_id = ?", id)
                .executeSingle();

        if (menuItem != null) {
            xWalkWebView.clearCache(true);
//            String summary = "<html><body>You scored <b>192</b> points.</body></html>";
//            webView.loadData(summary, "text/html", null);
            xWalkWebView.load(JSONParser.URL_ROOT + WEB_INDEX + menuItem.url,null);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        // Provides a simple way of getting a device's location and is well suited for
        // applications that do not require a fine-grained location and that do not need location
        // updates. Gets the best and most recent location currently available, which may be null
        // in rare cases when a location is not available.

        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mCurrentLocation != null) {

            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            updateUI();
        } else {
            Log.d(LOG_TAG, "не найдено место дислокации");
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(LOG_TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }


    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(LOG_TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }


    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Requests location updates from the FusedLocationApi.
     */
    @SuppressWarnings("ALL")
    protected void startLocationUpdates() {
        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    /**
     * Updates the latitude, the longitude, and the last location time in the UI.
     */
    private void updateUI() {

        LocationModel locationModel = new LocationModel();
        locationModel.lat = mCurrentLocation.getLatitude();
        locationModel.lng = mCurrentLocation.getLongitude();
        locationModel.createdAt = new Date();
        locationModel.save();
        Log.d(LOG_TAG, locationModel.lat + " " + locationModel.lng);

        if(EConcrete.isNetworkAvailable(this)) {
            SendLatLngTask task = new SendLatLngTask();
            task.execute();
        }
    }

    /**
     *       ПОЛУЧЕНИЕ ДАННЫХ ПОЛЬЗОВАТЕЛЯ С СЕРВЕРА
     */
    public class SendLatLngTask extends AsyncTask<String, Void, Boolean> {

        private String result = "";

        @Override
        protected Boolean doInBackground(String... params) {

            JSONParser jpar = new JSONParser(MainActivity.this);
            jpar.setResource("restapi/update_v_1_1");
            jpar.setMethod(JSONParser.METHOD_POST);

            JSONObject jsonObject = new JSONObject();
            JSONArray items = new JSONArray();
            JSONObject item = new JSONObject();
            JSONArray values = new JSONArray();
            JSONObject value = new JSONObject();

            List<LocationModel> locations = new Select()
                    .from(LocationModel.class)
                    .execute();
            try {

                for(LocationModel location : locations) {
                    value.put("lat", location.lat);
                    value.put("lon", location.lng);
                    value.put("created_at", location.createdAt);
                }

                item.put("table_name", "bi_driver_locs");
                item.put("action", "insert");
                item.put("values", values);

                items.put(item);

                jsonObject.put("items", items);

                jpar.setJsonDataObject(jsonObject);

            } catch (JSONException e) {
                e.printStackTrace();
                result = "Ошибка сбора данных в JSON";
                return false;
            }

            if(!jpar.execute()) {
                result = getString(R.string.connection_error);
                return false;
            }

            Log.d(LOG_TAG, jpar.getJson());

            JSONObject respJson = jpar.toJsonObject();
            try {
                return respJson.getString("error_text").toLowerCase().equals("ok");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                new Delete()
                        .from(LocationModel.class)
                        .execute();
            } else {
                Snackbar
                        .make(MainActivity.this.xWalkWebView,result,Snackbar.LENGTH_LONG)
                        .setAction("Сообщение",null)
                        .show();
            }
        }
    }

    /**
     * Removes location updates from the FusedLocationApi.
     */
    protected void stopLocationUpdates() {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.

        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        updateUI();
    }

    /**
     * Stores activity data in the Bundle.
     */
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelable(LOCATION_KEY, mCurrentLocation);
        super.onSaveInstanceState(savedInstanceState);
    }

    public static void displayPromptForEnablingGPS(final Context context) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final String action = Settings.ACTION_LOCATION_SOURCE_SETTINGS;

        builder.setMessage(R.string.open_GPS)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int id) {
                                context.startActivity(new Intent(action));
                                d.dismiss();
                            }
                        })
                .setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int id) {
                                d.cancel();
                            }
                        });
        builder.create().show();
    }

    @Override
    public void onFileSelected(String file) {
        SharedPreferences prefs = getSharedPreferences(EConcrete.appName, Context.MODE_PRIVATE);
        prefs
                .edit()
                .putString("edspath",file)
                .apply();
    }


    @Override
    public void onShowMessage(String message) {

        Snackbar.make(xWalkWebView, message, Snackbar.LENGTH_LONG)
                .setAction("Сообщение", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FileChooserFragment fileFragment = new FileChooserFragment();
                        fileFragment.show(getFragmentManager(), "fileChooser");
                    }
                })
                .show();
    }

    @JavascriptInterface
    public void showDriverOnMap(String json) {
        Intent intent = new Intent(this,MapsActivity.class);
        intent.putExtra(MapsActivity.EXTRA_JSON, json);
        startActivity(intent);
    }
}
