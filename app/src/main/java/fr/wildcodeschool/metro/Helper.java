package fr.wildcodeschool.metro;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;


public class Helper {
    private final static String API_KEY = "6dcf293ec6f59ca711dd9d89646478ef2acb872d";
    private static ArrayList<Station> stations = new ArrayList<>();


    public static void extractStation(Context context, final Settings settings, final BikeStationListener listener) {
        String url = "https://api.jcdecaux.com/vls/v1/stations?contract=Toulouse&apiKey=" + API_KEY;
        final RequestQueue requestQueue = Volley.newRequestQueue(context);
        Singleton settingsSingleton = Singleton.getInstance();
        final Settings mSettings = settingsSingleton.getSettings();

        final JsonArrayRequest jsonArrayRequestRequest = new JsonArrayRequest(
                Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {
                        Location stationLocation = new Location(LocationManager.GPS_PROVIDER);

                        try {
                            stations.clear();
                            JSONArray listStation = response;
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject bikeStation = (JSONObject) listStation.get(i);
                                int availableBike = (int) bikeStation.get("available_bikes");
                                int availabeStands = (int) bikeStation.get("available_bike_stands");
                                JSONObject position = (JSONObject) bikeStation.get("position");
                                double latitude = (double) position.get("lat");
                                double longitude = (double) position.get("lng");
                                int number = (int) bikeStation.get("number");
                                String name = (String) bikeStation.get("name");
                                String address = (String) bikeStation.get("address");
                                int stands = (int) bikeStation.get("bike_stands");
                                String status = (String) bikeStation.get("status");
                                stationLocation.setLatitude(latitude);
                                stationLocation.setLongitude(longitude);
                                float stationDistance = stationLocation.distanceTo(settings.getLocation());
                                float askPerimeter = settings.getZoom() == 18 ? 100f : settings.getZoom() == 17 ? 200f : settings.getZoom() == 16 ? 500f : settings.getZoom() == 15 ? 700f : 1000f;
                                if (stationDistance <= askPerimeter && !mSettings.isFragmentActivity()) {
                                    if ((bikeStation.get("status").equals("OPEN") && availableBike != 0 && settings.isDropOff()) || (bikeStation.get("status").equals("OPEN") && availabeStands != 0 && !settings.isDropOff())) {
                                        Station station = new Station(number, name, address, latitude, longitude, stands, availableBike, availabeStands, status, stationDistance);
                                        stations.add(station);
                                    }
                                } else if (mSettings.isFragmentActivity()) {
                                    Station station = new Station(number, name, address, latitude, longitude, stands, availableBike, availabeStands, status, stationDistance);
                                    stations.add(station);
                                }
                            }
                            Collections.sort(stations);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        listener.onResult(stations);
                    }
                },
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("VOLLEY_ERROR", "onErrorResponse: " + error.getMessage());
                    }
                }
        );
        requestQueue.add(jsonArrayRequestRequest);
    }


    public interface BikeStationListener {
        void onResult(ArrayList<Station> stations);
    }
}
