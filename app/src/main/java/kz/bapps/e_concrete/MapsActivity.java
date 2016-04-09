package kz.bapps.e_concrete;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    final public static String EXTRA_JSON = "json_data";

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        Bundle extras = getIntent().getExtras();

        try {
            JSONArray jsonArray = new JSONArray(extras.getString(EXTRA_JSON));
            PolylineOptions options = new PolylineOptions();

            for(int i = 0; i < jsonArray.length(); i++ ) {
                JSONObject driverLoc = jsonArray.getJSONObject(i);
                LatLng latLng = new LatLng(driverLoc.getDouble("lat"),driverLoc.getDouble("lon"));
                options.add(latLng);
            }

            LatLng startLoc = new LatLng(jsonArray.getJSONObject(0).getDouble("lat"),
                    jsonArray.getJSONObject(0).getDouble("lon"));

            LatLng endLoc = new LatLng(jsonArray.getJSONObject(jsonArray.length() - 1).getDouble("lat"),
                    jsonArray.getJSONObject(jsonArray.length() - 1).getDouble("lon"));

            mMap.addPolyline(options);
            mMap.addMarker(new MarkerOptions().position(startLoc).title("Начало движения"));
            mMap.addMarker(new MarkerOptions().position(endLoc).title("Текущее местоположение"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(endLoc,12));
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

}
