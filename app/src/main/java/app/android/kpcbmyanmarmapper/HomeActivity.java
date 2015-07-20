package app.android.kpcbmyanmarmapper;

import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;


public class HomeActivity extends FragmentActivity implements OnMapReadyCallback {

    private Button mCalculateButton;
    private TextView mAreaCalculateTextView;
    private LatLng mPrevLatLng;
    final static LatLng sMyanmarLatLng = new LatLng(22.0,96.0);
    static final double EARTH_RADIUS = 6371009;

    private ArrayList<LatLng> mLatLngPolylineList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mCalculateButton = (Button) findViewById(R.id.calculate_polyline_area);
        mAreaCalculateTextView = (TextView) findViewById(R.id.calculation_area);

        mPrevLatLng = new LatLng(22.0, 96.0);
        mLatLngPolylineList = new ArrayList<LatLng>();
        mLatLngPolylineList.add(mPrevLatLng);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        googleMap.addMarker(new MarkerOptions()
                .position(sMyanmarLatLng)
                .title("Myanmar"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sMyanmarLatLng));

        googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                googleMap.addPolyline(new PolylineOptions()
                .add(mPrevLatLng, latLng)
                .width(3)
                .color(Color.RED));

                mLatLngPolylineList.add(latLng);
                mPrevLatLng = latLng;
            }
        });

        mCalculateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Double areaInMeters = computeArea(mLatLngPolylineList);
                mAreaCalculateTextView.setText(String.valueOf(areaInMeters));
            }
        });


    }

    private static double polarTriangleArea(double tan1, double lng1, double tan2, double lng2) {
        double deltaLng = lng1 - lng2;
        double t = tan1 * tan2;
        return 2 * Math.atan2(t * Math.sin(deltaLng), 1 + t * Math.cos(deltaLng));
    }

    static double computeSignedArea(List<LatLng> path, double radius) {
        int size = path.size();
        if (size < 3) { return 0; }
        double total = 0;
        LatLng prev = path.get(size - 1);
        double prevTanLat = Math.tan((Math.PI / 2 - Math.toRadians(prev.latitude)) / 2);
        double prevLng = Math.toRadians(prev.longitude);
        // For each edge, accumulate the signed area of the triangle formed by the North Pole
        // and that edge ("polar triangle").
        for (LatLng point : path) {
            double tanLat = Math.tan((Math.PI / 2 - Math.toRadians(point.latitude)) / 2);
            double lng = Math.toRadians(point.longitude);
            total += polarTriangleArea(tanLat, lng, prevTanLat, prevLng);
            prevTanLat = tanLat;
            prevLng = lng;
        }
        return total * (radius * radius);
    }

    public static double computeSignedArea(List<LatLng> path) {
        return computeSignedArea(path, EARTH_RADIUS);
    }

    public static double computeArea(List<LatLng> path) {
        return Math.abs(computeSignedArea(path));
    }

}
