package fr.wildcodeschool.metro;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
    private final View mWindow;
    private Context mContext;
    private int bikeAvailable;
    private Station selectedMarker;
    private TextView stationTitle;

    public CustomInfoWindowAdapter(Context context) {
        mContext = context;
        mWindow = LayoutInflater.from(context).inflate(R.layout.info_windows_map, null);
    }

    private void rendowWindowText(Marker marker, final View view) {
        selectedMarker = (Station) marker.getTag();
        String title = marker.getTitle();
        stationTitle = view.findViewById(R.id.tvTitle);
        if (!title.equals("")) {
            stationTitle.setText(title);
        }
    }

    @Override
    public View getInfoWindow(Marker marker) {
        rendowWindowText(marker, mWindow);
        return mWindow;
    }

    @Override
    public View getInfoContents(Marker marker) {
        rendowWindowText(marker, mWindow);
        return mWindow;
    }


}
