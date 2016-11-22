package br.com.helpdev.supportlib_maps.activities;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import br.com.helpdev.supportlib_maps.R;

/**
 * <pre>
 *
 * Declare in your Manifest.xml
 * <service android:name="br.com.helpdev.supportlib_maps.services.ServiceLocation" />
 *
 * </pre>
 * Created by Guilherme Biff Zarelli on 16/11/16.
 */
public abstract class SimpleMapAbs extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final int REQUEST_ERRO_PLAY_SERVICES = 1;
    private int mIdLayoutMap;
    private int mIdMap;

    protected GoogleMap map;
    protected GoogleApiClient googleApiClient;

    public SimpleMapAbs(int idLayoutMap, int idMap) {
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

        if (googleApiClient == null) {
            setUpGoogleApiClient();
        }
    }

    private Button btTerrain;

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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (googleApiClient != null && requestCode == REQUEST_ERRO_PLAY_SERVICES && resultCode == RESULT_OK) {
            googleApiClient.connect();
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        setUpMap();
    }

    protected void setUpGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
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
    public boolean onMyLocationButtonClick() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        if (googleApiClient == null)
            return false;
        Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (location == null)
            return false;
        LatLng ponto = new LatLng(location.getLatitude(), location.getLongitude());
        goToPoint(ponto);
        return true;
    }

    protected void goToPoint(LatLng latLng, float zoom) {
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    protected void goToPoint(LatLng latLng) {
        goToPoint(latLng, 17.0f);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        onMyLocationButtonClick();
    }

    @Override
    public void onConnectionSuspended(int i) {
        googleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        try {
            if (connectionResult.hasResolution()) {
                connectionResult.startResolutionForResult(this, REQUEST_ERRO_PLAY_SERVICES);
            } else {
                showMensageError(this, connectionResult.getErrorCode());
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    protected void addSimplePoint(String title, int resIdIcon, LatLng position) {
        MarkerOptions point = new MarkerOptions();
        point.position(position);
        point.title(title);
        point.icon(BitmapDescriptorFactory.fromResource(resIdIcon));
        getMap().addMarker(point);
    }

    private void showMensageError(AppCompatActivity activity, final int codError) {
        final String TAG = "DIALOG_ERRO_PLAY_SERVICES";
        if (getSupportFragmentManager().findFragmentByTag(TAG) == null) {
            DialogFragment errorFragment = new DialogFragment() {
                @NonNull
                @Override
                public Dialog onCreateDialog(Bundle savedInstanceState) {
                    return super.onCreateDialog(savedInstanceState);
                }
            };
            errorFragment.show(activity.getSupportFragmentManager(), TAG);
        }
    }

    public GoogleMap getMap() {
        return map;
    }


    public GoogleApiClient getGoogleApiClient() {
        return googleApiClient;
    }

}
