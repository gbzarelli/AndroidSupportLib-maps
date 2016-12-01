package br.com.helpdev.supportlib_maps.gpx;

import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;

/**
 * Created by Guilherme Biff Zarelli on 01/12/16.
 */

public class ObGpxMap {
    private Polyline polyline;
    private Marker markerStart;
    private Marker markerEnd;

    public ObGpxMap(Polyline polyline, Marker markerStart, Marker markerEnd) {
        this.polyline = polyline;
        this.markerStart = markerStart;
        this.markerEnd = markerEnd;
    }


    public void remove() {
        polyline.remove();
        markerStart.remove();
        markerEnd.remove();
    }

    @Override
    public String toString() {
        return "ObGpxMap{" +
                "polyline=" + polyline +
                ", markerStart=" + markerStart +
                ", markerEnd=" + markerEnd +
                '}';
    }
}
