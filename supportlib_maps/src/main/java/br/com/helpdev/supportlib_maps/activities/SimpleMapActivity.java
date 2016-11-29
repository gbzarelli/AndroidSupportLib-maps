package br.com.helpdev.supportlib_maps.activities;

import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import br.com.helpdev.supportlib_maps.R;

/**
 * <pre>
 *
 * Declare in your Manifest.xml
 * <service android:name="br.com.helpdev.supportlib_maps.locations.LocationUtils" />
 *
 * </pre>
 * Created by Guilherme Biff Zarelli on 16/11/16.
 */
public abstract class SimpleMapActivity extends AppCompatLocation implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener {

    private int mIdLayoutMap;
    private int mIdMap;

    protected GoogleMap map;
    private Button btTerrain;

    public SimpleMapActivity(int idLayoutMap, int idMap) {
        this.mIdLayoutMap = idLayoutMap;
        this.mIdMap = idMap;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(mIdLayoutMap);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(mIdMap);
        mapFragment.getMapAsync(this);

    }

    protected void setEnableBtTerrain(int idBtTerrain, boolean visible) {
        btTerrain = (Button) findViewById(idBtTerrain);
        if (btTerrain == null) {
            throw new IllegalArgumentException("BtTerrain not found");
        }
        btTerrain.setVisibility(visible ? View.VISIBLE : View.GONE);
        btTerrain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMapTypeSelectorDialog();
            }
        });
    }

    private void showMapTypeSelectorDialog() {
        final String fDialogTitle = getString(R.string.tipo_mapa);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(fDialogTitle);

        int checkItem = map.getMapType() - 1;

        final CharSequence[] MAP_TYPE_ITEMS = {
                getString(R.string.map_type_road),
                getString(R.string.map_type_hybrid),
                getString(R.string.map_type_satellite),
                getString(R.string.map_type_terrain)
        };
        builder.setSingleChoiceItems(
                MAP_TYPE_ITEMS,
                checkItem,
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int item) {
                        switch (item) {
                            case 1:
                                map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                                break;
                            case 2:
                                map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                                break;
                            case 3:
                                map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                                break;
                            default:
                                map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        }
                        btTerrain.setText(MAP_TYPE_ITEMS[item]);
                        dialog.dismiss();
                    }
                }
        );

        AlertDialog fMapTypeDialog = builder.create();
        fMapTypeDialog.setCanceledOnTouchOutside(true);
        fMapTypeDialog.show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        setUpMap();
        if (getGoogleApiClient().isConnected()) {
            onMyLocationButtonClick();
        }
    }

    protected void setUpMap() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        map.setMyLocationEnabled(true);
        map.setOnMyLocationButtonClickListener(this);
        map.setBuildingsEnabled(true);
    }

    @Override
    public void onConnectedLocation() {
        onMyLocationButtonClick();
    }

    @Override
    public boolean onMyLocationButtonClick() throws SecurityException {
        Location location = getLastLocation();
        if (location == null)
            return false;
        LatLng ponto = new LatLng(location.getLatitude(), location.getLongitude());
        goToPoint(ponto);
        return true;
    }

    protected void goToPoint(LatLng latLng, float zoom) {
        if (map != null) map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    protected void goToBound(LatLng... latLngs) {
        LatLngBounds.Builder lngBounds = new LatLngBounds.Builder();
        for (LatLng latLng : latLngs) {
            lngBounds.include(latLng);
        }
        if (map != null)
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(lngBounds.build(), 10));
    }

    protected void goToPoint(LatLng latLng) {
        goToPoint(latLng, 17.0f);
    }


    protected void addSimplePoint(String title, int resIdIcon, LatLng position) {
        MarkerOptions point = new MarkerOptions();
        point.position(position);
        point.title(title);
        point.icon(BitmapDescriptorFactory.fromResource(resIdIcon));
        getMap().addMarker(point);
    }

    public GoogleMap getMap() {
        return map;
    }

}
