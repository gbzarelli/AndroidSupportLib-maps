package br.com.helpdev.supportlib_maps.gpx;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;

import org.simpleframework.xml.core.Persister;

import java.io.File;
import java.io.FileInputStream;

import br.com.helpdev.supportlib.utils.ThisObjects;
import br.com.helpdev.supportlib_maps.R;
import br.com.helpdev.supportlib_maps.gpx.objetos.Gpx;
import br.com.helpdev.supportlib_maps.gpx.objetos.TrkPt;

/**
 * Created by Guilherme Biff Zarelli on 17/11/16.
 */

public class GpxMapUtils {

    public static void loadGpxOnMap(Context context, GoogleMap googleMap, Gpx gpx) {
        PolygonOptions polygonOptions = new PolygonOptions();
        polygonOptions.strokeColor(ContextCompat.getColor(context, R.color.colorPrimary));
        for (TrkPt tr : gpx.getTrk().getTrkseg().getTrkPts()) {
            try {
                LatLng ll = new LatLng(Double.valueOf(tr.getLat()), Double.valueOf(tr.getLon()));
                polygonOptions.add(ll);
            } catch (Throwable t) {
            }
        }
        googleMap.clear();

        addSimplePoint(googleMap, context.getString(R.string.start_point), R.drawable.home, polygonOptions.getPoints().get(0));
        addSimplePoint(googleMap, context.getString(R.string.end_point), R.drawable.flag_checkered, polygonOptions.getPoints().get(polygonOptions.getPoints().size() - 1));

        googleMap.addPolygon(polygonOptions);
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(polygonOptions.getPoints().get(0), 15f));
    }

    public static Gpx loadGpxFile(File file) throws Exception {
        Persister persister = new Persister();
        Gpx gpxFile = persister.read(Gpx.class, new FileInputStream(file));
        if (null == gpxFile) {
            throw new Exception("IlegalFile");
        }
        return gpxFile;
    }

    protected static void addSimplePoint(GoogleMap googleMap, String title, int resIdIcon, LatLng position) {
        MarkerOptions point = new MarkerOptions();
        point.position(position);
        point.title(title);
        point.icon(BitmapDescriptorFactory.fromResource(resIdIcon));
        googleMap.addMarker(point);
    }

    public interface LoadGpxAsyncCallback {
        void gpxLoadStart();

        void gpxLoadError(Throwable t);

        void gpxLoadSucess();
    }

    public static void loadGpxAsync(final AppCompatActivity activity, final GoogleMap googleMap, final File file, final LoadGpxAsyncCallback loadGpxAsyncCallback) {
        ThisObjects.requireNonNull(loadGpxAsyncCallback);
        loadGpxAsyncCallback.gpxLoadStart();
        new Thread() {
            @Override
            public void run() {
                try {
                    final Gpx gpx = GpxMapUtils.loadGpxFile(file);
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            GpxMapUtils.loadGpxOnMap(activity, googleMap, gpx);
                            loadGpxAsyncCallback.gpxLoadSucess();
                        }
                    });
                } catch (final Throwable t) {
                    t.printStackTrace();
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loadGpxAsyncCallback.gpxLoadError(t);
                        }
                    });
                }
            }
        }.start();
    }
}
