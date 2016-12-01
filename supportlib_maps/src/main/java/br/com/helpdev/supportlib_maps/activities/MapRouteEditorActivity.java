package br.com.helpdev.supportlib_maps.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Guilherme Biff Zarelli on 01/12/16.
 */

public class MapRouteEditorActivity extends SimpleMapActivity implements GoogleMap.OnMapLongClickListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnMarkerDragListener {

    private List<Marker> markersRoute;
    private Polyline polylines;
    private List<LatLng> route;

    private int resIdColorRoute;
    private boolean enableRouteCreate;
    private boolean enableRouteEdit;
    private int indexMarkerRoute;

    public MapRouteEditorActivity(int idLayoutMap, int idMap,int resIdColorRoute) {
        super(idLayoutMap, idMap);
        this.resIdColorRoute = resIdColorRoute;
        enableRouteCreate = true;
        enableRouteEdit = true;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        markersRoute = new ArrayList<>();
        route = new ArrayList<>();
    }

    @Override
    protected void onMapReady() {
        getMap().setOnMapLongClickListener(this);
        getMap().setOnMarkerDragListener(this);
        getMap().setOnMarkerClickListener(this);
    }

    public void loadMap(List<LatLng> locations) {
        route = locations;
        clearMap();
        for (LatLng location : locations) {
            locations.add(location);
            markersRoute.add(addMarkerRoute(location));
        }
        traceRoute();
    }

    private Marker addMarkerRoute(LatLng loc) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(loc);
        BitmapDescriptor iconMarker = getIconMarker();
        markerOptions.icon(iconMarker);
        markerOptions.anchor(0.5f, 0.5f);
        markerOptions.draggable(true);
        return addMarker(markerOptions);
    }

    public void addRouteLine(LatLng loc) {
        markersRoute.add(addMarkerRoute(loc));
        route.add(loc);
        traceRoute();
    }

    private void traceRoute() {
        if (polylines != null) {
            polylines.remove();
        }
        polylines = traceRoute(route, resIdColorRoute);
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        if (!enableRouteCreate) return;
        addRouteLine(latLng);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (!enableRouteEdit) return false;
        indexMarkerRoute = markersRoute.indexOf(marker);
        if (indexMarkerRoute > -1) {
            markersRoute.get(indexMarkerRoute).remove();
            markersRoute.remove(indexMarkerRoute);
            route.remove(indexMarkerRoute);
            traceRoute();
        }
        return true;
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        if (!enableRouteEdit) return;
        indexMarkerRoute = markersRoute.indexOf(marker);
    }

    @Override
    public void onMarkerDrag(Marker marker) {
        if (!enableRouteEdit) return;
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        if (!enableRouteEdit) return;
        markersRoute.set(indexMarkerRoute, marker);
        route.set(indexMarkerRoute, marker.getPosition());
        traceRoute();
    }

    public boolean isEnableRouteCreate() {
        return enableRouteCreate;
    }

    public void setEnableRouteCreate(boolean enableRouteCreate) {
        this.enableRouteCreate = enableRouteCreate;
    }

    public boolean isEnableRouteEdit() {
        return enableRouteEdit;
    }

    public void setEnableRouteEdit(boolean enableRouteEdit) {
        this.enableRouteEdit = enableRouteEdit;
    }

    public List<Marker> getMarkersRoute() {
        return markersRoute;
    }

    public Polyline getPolylines() {
        return polylines;
    }

    public List<LatLng> getRoute() {
        return route;
    }
}
