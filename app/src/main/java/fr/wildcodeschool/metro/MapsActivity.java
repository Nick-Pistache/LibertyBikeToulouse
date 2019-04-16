package fr.wildcodeschool.metro;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static fr.wildcodeschool.metro.Helper.extractStation;
import static fr.wildcodeschool.metro.ListStations.SETTINGS_RETURN;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    public static final String SETTINGS = "Settings";
    private static final int REQUEST_LOCATION = 2000;
    public static final int REQUEST_IMAGE_CAPTURE = 1234;
    public static ArrayList<Marker> stationMarkers = new ArrayList<Marker>();
    public static boolean init = false;
    public static boolean changeActivity = false;
    public static boolean theme = false;
    private static GoogleMap mMap;
    private static boolean dropOff = true;
    private static int zoom = 14;
    private static Settings settings;
    private static Location lastKnownlocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        if (!init) {
            checkPermission();
        } else {
            lastKnownLocation();
        }
        Intent receiveListActivity = getIntent();
        settings = receiveListActivity.getParcelableExtra(SETTINGS_RETURN);
        switchButton();
        floatingButton();
        takePicIssues();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void takePicIssues() {
        ImageButton takePic = findViewById(R.id.ibTakePicOfIssue);
        takePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();


            }
        });

    }

    private Uri mFileUri = null;
    private void dispatchTakePictureIntent() {
        // ouvrir l'application de prise de photo
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // lors de la validation de la photo
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // créer le fichier contenant la photo
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException e) {
                // TODO : gérer l'erreur
            }

            if (photoFile != null) {
                // récupèrer le chemin de la photo
                 mFileUri = FileProvider.getUriForFile(this,
                        "fr.wildcodeschool.metro.fileprovider",
                        photoFile);
                // déclenche l'appel de onActivityResult
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mFileUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        ImageView ivRecupPic = findViewById(R.id.ivRecupPic);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            ivRecupPic.setImageURI(mFileUri);

        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imgFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imgFileName, ".jpg", storageDir);
        return image;
    }

    private void floatingButton() {
        FloatingActionButton button = findViewById(R.id.buttonSettings);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displaySettings();
            }
        });
    }

    private void switchButton() {
        Switch switchButton = findViewById(R.id.switch1);
        switchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                changeActivity = true;
                Intent goListStationAcitvity = new Intent(MapsActivity.this, ListStations.class);
                goListStationAcitvity.putExtra(SETTINGS, (Parcelable) settings);
                startActivity(goListStationAcitvity);
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void lastKnownLocation() {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    lastKnownlocation = location;
                    if (!changeActivity) {
                        settings = new Settings(zoom, dropOff, lastKnownlocation, init, changeActivity, theme);
                    }
                    removeMarkers();
                    mMap.setMyLocationEnabled(true);
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), zoom));
                    mMap.getUiSettings().setZoomControlsEnabled(true);
                    createStationMarker(settings);
                }
            }
        });
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        checkPermission();
        mMap = googleMap;
        if (!changeActivity) {
            settings = new Settings(zoom, dropOff, lastKnownlocation, init, changeActivity, theme);
        }
        switchTheme(googleMap);
        if (settings.isTheme()) {
            displayDarkTheme(googleMap);
        } else {
            displayDefaultTheme(googleMap);
        }
    }

    private void switchTheme(final GoogleMap googleMap) {
        Switch switchDarkMap = findViewById(R.id.switchMap);
        switchDarkMap.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settings.setTheme(!settings.isTheme());
                if (settings.isTheme()) {
                    displayDarkTheme(googleMap);
                } else {
                    displayDefaultTheme(googleMap);
                }
            }
        });
    }

    private void displayDefaultTheme(final GoogleMap googleMap) {
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            MapsActivity.this, R.raw.mapstyle));

            if (!success) {
                Log.e("MapsActivity", "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("MapsActivity", "Can't find style. Error: ", e);
        }
    }

    private void displayDarkTheme(final GoogleMap googleMap) {
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            MapsActivity.this, R.raw.mapstyledark));
            if (!success) {
                Log.e("MapsActivity", "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("MapsActivity", "Can't find style. Error: ", e);
        }
    }

    private void displaySettings() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String[] perimeter = {getString(R.string.perimeter1), getString(R.string.perimeter2), getString(R.string.perimeter3), getString(R.string.perimeter4), getString(R.string.perimeter5)};
        final boolean[] checkedItems = {false, false, false, false, false};
        View switchButtonView = LayoutInflater.from(this).inflate(R.layout.activity_toggle, null);
        Switch switchButton = switchButtonView.findViewById(R.id.switch2);
        builder.setTitle(R.string.settings);
        builder.setMultiChoiceItems(perimeter, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                zoom = checkedItems[0] ? 14 : checkedItems[1] ? 15 : checkedItems[2] ? 16 : checkedItems[3] ? 17 : 18;
            }
        });
        builder.setView(switchButtonView);
        switchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                dropOff = isChecked ? true : false;
                if (dropOff) {
                    Toast.makeText(MapsActivity.this, getString(R.string.takeBike), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MapsActivity.this, getString(R.string.dropBike), Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                settings.setDropOff(dropOff);
                settings.setZoom(zoom);
                lastKnownLocation();
                Toast.makeText(MapsActivity.this, getString(R.string.appliedSettings), Toast.LENGTH_LONG).show();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void createStationMarker(Settings settings) {
        extractStation(MapsActivity.this, settings, new Helper.BikeStationListener() {
            @Override
            public void onResult(ArrayList<Station> stations) {
                for (int i = 0; i < stations.size(); i++) {
                    LatLng newStation = new LatLng(stations.get(i).getLatitude(), stations.get(i).getLongitude());
                    Marker marker = mMap.addMarker((new MarkerOptions().position(newStation).icon(BitmapDescriptorFactory.fromResource(R.drawable.icon)).title(stations.get(i).getAddress()).snippet(stations.get(i).getName())));
                    stationMarkers.add(marker);
                    marker.showInfoWindow();
                    mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(MapsActivity.this));
                }
            }
        });
    }

    private void removeMarkers() {
        for (Marker marker : stationMarkers) {
            marker.remove();
        }
        stationMarkers.clear();
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(MapsActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MapsActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(MapsActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_LOCATION);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {

            lastKnownLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                    lastKnownLocation();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }
}

