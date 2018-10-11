package br.com.helpdev.supportlib_maps.gpx;

import android.content.Context;
import android.location.Location;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.simpleframework.xml.core.Persister;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import br.com.helpdev.supportlib.utils.ThisObjects;
import br.com.helpdev.supportlib_maps.R;
import br.com.helpdev.supportlib_maps.gpx.objetos.Gpx;
import br.com.helpdev.supportlib_maps.gpx.objetos.MetaData;
import br.com.helpdev.supportlib_maps.gpx.objetos.Trk;
import br.com.helpdev.supportlib_maps.gpx.objetos.TrkPt;
import br.com.helpdev.supportlib_maps.gpx.objetos.TrkSeg;

/**
 * Created by Guilherme Biff Zarelli on 17/11/16.
 */

public class GpxMapUtils {

    public static ObGpxMap loadGpxOnMap(Context context, GoogleMap googleMap, Gpx gpx) {
        PolylineOptions polygonOptions = new PolylineOptions();
        polygonOptions.color(ContextCompat.getColor(context, R.color.colorPrimary));
        for (TrkPt tr : gpx.getTrk().getTrkseg().getTrkPts()) {
            try {
                LatLng ll = new LatLng(Double.valueOf(tr.getLat()), Double.valueOf(tr.getLon()));
                polygonOptions.add(ll);
            } catch (Throwable t) {
            }
        }

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng latLng : polygonOptions.getPoints()) {
            builder.include(latLng);
        }
        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 20));

        return new ObGpxMap(googleMap.addPolyline(polygonOptions),
                addSimplePoint(googleMap, context.getString(R.string.gpx_start_point), R.drawable.home, polygonOptions.getPoints().get(0)),
                addSimplePoint(googleMap, context.getString(R.string.gpx_end_point), R.drawable.flag_checkered, polygonOptions.getPoints().get(polygonOptions.getPoints().size() - 1))
        );
    }

    public static Gpx createGpx(String nameCreator, String nameFile, List<Location> locationList) {
        Gpx gpx = new Gpx(nameCreator);
        MetaData metaData = new MetaData();
        metaData.setTime(getUtcGpxTime(locationList.get(0).getTime()));
        Trk trk = new Trk();
        trk.setName(nameFile);
        TrkSeg trkSeg = new TrkSeg();
        ArrayList<TrkPt> trkPtArrayList = new ArrayList<>();
        for (Location loc : locationList) {
            TrkPt trkPt = new TrkPt();
            trkPt.setLat(loc.getLatitude());
            trkPt.setLon(loc.getLongitude());
            trkPt.setEle(loc.getAltitude());
            trkPt.setTime(getUtcGpxTime(loc.getTime()));
            trkPtArrayList.add(trkPt);
        }

        trkSeg.setTrkPts(trkPtArrayList);
        trk.setTrkseg(trkSeg);
        gpx.setTrk(trk);
        gpx.setMetaData(metaData);
        return gpx;
    }

    private static String getUtcGpxTime(long data) {//2017-04-11T09:02:40Z
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(new Date(data));
    }

    public static Gpx loadGpxFile(File file) throws Exception {
        Persister persister = new Persister();
        Gpx gpxFile = persister.read(Gpx.class, new FileInputStream(file));
        if (null == gpxFile) {
            throw new Exception("IlegalFile");
        }
        return gpxFile;
    }

    protected static Marker addSimplePoint(GoogleMap googleMap, String title, int resIdIcon, LatLng position) {
        MarkerOptions point = new MarkerOptions();
        point.position(position);
        point.title(title);
        point.icon(BitmapDescriptorFactory.fromResource(resIdIcon));
        return googleMap.addMarker(point);
    }

    public interface LoadGpxAsyncCallback {
        void gpxLoadStart();

        void gpxLoadError(Throwable t);

        void gpxLoadSucess(Gpx gpx, ObGpxMap polyline);
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
                            ObGpxMap polyline = GpxMapUtils.loadGpxOnMap(activity, googleMap, gpx);
                            loadGpxAsyncCallback.gpxLoadSucess(gpx, polyline);
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
