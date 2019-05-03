package fr.wildcodeschool.metro;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static fr.wildcodeschool.metro.Helper.extractStation;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    public final int REQUEST_IMAGE_CAPTURE = 1234;
    public final int REQUEST_LOCATION = 2000;
    public ArrayList<Marker> mStationMarkers = new ArrayList<>();
    public ArrayList<Station> mCurrentStation = new ArrayList<>();
    public boolean mTheme = false;
    public GoogleMap mMap;
    public boolean mDropOff = false;
    public int mZoom = 14;
    public Settings mSettings;
    public Location mLastKnownLocation;
    public Uri mFileUri = null;
    private SeekBar mSeekbar;
    private int mProgress = 0;
    private Singleton settings;
    private boolean fragmentActivity = false;
    private int mFavoriteStationNumber;
    private Switch btChooseYourCase;
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    return true;
                case R.id.navigation_list:
                    Intent goListStationAcitvity = new Intent(MapsActivity.this, ListStation.class);
                    startActivity(goListStationAcitvity);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        switchActivity();
        checkPermission();
        getSettings();
        takePicIssues();
        seekBar();
        toggleButton();
        setButtons();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void saveToFireBase() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference favoriteStationBase = database.getReference("favoriteStationBase");
        String key = Integer.toString(mFavoriteStationNumber);
        favoriteStationBase.child(key).setValue(mFavoriteStationNumber);
    }

    private void selectMarker(GoogleMap googleMap) {
        googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Station selectedStation = (Station) marker.getTag();
                mFavoriteStationNumber = selectedStation.getNumber();
                Toast.makeText(MapsActivity.this, String.format(getString(R.string.stringFormat), getString(R.string.station), selectedStation.getName(), getString(R.string.addFavorites)), Toast.LENGTH_SHORT).show();
                saveToFireBase();
            }
        });
    }

    private void getSettings() {
        settings = Singleton.getInstance();
        mSettings = settings.getSettings();
        if (mSettings == null) {
            settings.initiateSettings(mZoom, mDropOff, mLastKnownLocation, fragmentActivity, mTheme, mProgress);
            mSettings = settings.getSettings();
        }
        currentLocation();
    }

    private void switchActivity() {
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.getMenu().setGroupCheckable(0, true, true);
        navigation.setSelectedItemId(R.id.navigation_home);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    private void setButtons() {        ;
        btChooseYourCase.setChecked(mSettings.isDropOff());
        mSeekbar.setProgress(mSettings.getZoomProgress());
    }

    private void seekBar() {
        mSeekbar = findViewById(R.id.seekBar);
        mSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mProgress = seekBar.getProgress();
                if (mProgress > 0 && mProgress < 20) {
                    seekBar.setProgress(0);
                    mZoom = 14;
                    Toast.makeText(MapsActivity.this, getString(R.string.zone1), Toast.LENGTH_SHORT).show();
                } else if (mProgress > 20 && mProgress < 40) {
                    seekBar.setProgress(25);
                    mProgress = 25;
                    mZoom = 15;
                    Toast.makeText(MapsActivity.this, getString(R.string.zone2), Toast.LENGTH_SHORT).show();
                } else if (mProgress > 40 && mProgress < 60) {
                    seekBar.setProgress(50);
                    mProgress = 50;
                    mZoom = 16;
                    Toast.makeText(MapsActivity.this, getString(R.string.zone3), Toast.LENGTH_SHORT).show();
                } else if (mProgress > 60 && mProgress < 80) {
                    seekBar.setProgress(75);
                    mProgress = 75;
                    mZoom = 17;
                    Toast.makeText(MapsActivity.this, getString(R.string.zone4), Toast.LENGTH_SHORT).show();
                } else if (mProgress > 80 && mProgress < 100) {
                    seekBar.setProgress(100);
                    mProgress = 100;
                    mZoom = 18;
                    Toast.makeText(MapsActivity.this, getString(R.string.zone5), Toast.LENGTH_SHORT).show();
                }
                settings.setZoomProgress(mProgress);
                settings.setZoom(mZoom);
                currentLocation();
            }
        });
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

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException e) {
            }
            if (photoFile != null) {
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

    private void toggleButton() {
        btChooseYourCase = findViewById(R.id.userMode);
        btChooseYourCase.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mDropOff = isChecked ? true : false;
                settings.setDropOff(mDropOff);
                if (mDropOff) {
                    Toast.makeText(MapsActivity.this, getString(R.string.takeBike), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MapsActivity.this, getString(R.string.dropBike), Toast.LENGTH_SHORT).show();
                }
                removeMarkers();
                createStationMarker(mSettings);
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void currentLocation() {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        LocationManager locationManager = (LocationManager) this.getSystemService(MapsActivity.LOCATION_SERVICE);
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null && mMap != null) {
                    mLastKnownLocation = location;
                    removeMarkers();
                    //designMarkers();
                    mMap.setMyLocationEnabled(true);
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), mSettings.getZoom()));
                    mMap.getUiSettings().setZoomControlsEnabled(true);
                    settings.setLocation(location);
                    createStationMarker(mSettings);
                }
            }
        });

        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                mLastKnownLocation = location;
                mMap.setMyLocationEnabled(true);
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), mSettings.getZoom()));
                mMap.getUiSettings().setZoomControlsEnabled(true);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };

        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mMap = googleMap;
        checkPermission();
        setTheme(googleMap);
        selectMarker(googleMap);
        displayRoute();
    }

    private void displayRoute() {

    }

    private void setTheme(GoogleMap googleMap) {
        if (mSettings.isTheme()) {
            displayDarkTheme(googleMap);
        } else {
            displayDefaultTheme(googleMap);
        }
    }

    private void displayDefaultTheme(final GoogleMap googleMap) {
        try {
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            MapsActivity.this, R.raw.mapstyle));

            if (!success) {
                Log.e("MapsActivity", getString(R.string.StyleFailed));
            }
        } catch (Resources.NotFoundException e) {
            Log.e("MapsActivity", getString(R.string.StyleError), e);
        }
    }

    private void displayDarkTheme(final GoogleMap googleMap) {
        try {
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

    private void createStationMarker(Settings settings) {
        extractStation(MapsActivity.this, settings, new Helper.BikeStationListener() {
            @Override
            public void onResult(ArrayList<Station> stations) {
                mCurrentStation = stations;

                for (int i = 0; i < stations.size(); i++) {
                    Station station = stations.get(i);
                    LatLng newStation = new LatLng(station.getLatitude(), station.getLongitude());

                    View inflatedFrame = getLayoutInflater().inflate(R.layout.map_layout, null);
                    TextView amountBikeOrStand = inflatedFrame.findViewById(R.id.tvBikeOrStand);
                    if (mSettings.isDropOff()) {
                        amountBikeOrStand.setText(Integer.toString(station.getAvailableBikes()));
                        amountBikeOrStand.setTextColor(Color.parseColor("#1ec62a"));
                    } else {
                        amountBikeOrStand.setText(Integer.toString(station.getAvailableStands()));
                        amountBikeOrStand.setTextColor(Color.parseColor("#1ea2c6"));

                    }
                    Bitmap bitmap = createBitmapFromView(inflatedFrame.findViewById(R.id.screen));

                    Marker marker = mMap.addMarker((new MarkerOptions().position(newStation)
                            .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                            .title(station.getAddress()).snippet(station.getName())));
                    marker.setTag(station);
                    mStationMarkers.add(marker);


                }
                mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(MapsActivity.this));
            }
        });
    }

    private void removeMarkers() {
        for (Marker marker : mStationMarkers) {
            marker.remove();
        }
        mStationMarkers.clear();
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(MapsActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MapsActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
            } else {
                ActivityCompat.requestPermissions(MapsActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_LOCATION);
            }
        } else {
            currentLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    currentLocation();
                } else {
                }
                return;
            }
        }
    }

    private Bitmap createBitmapFromView(View v) {
        v.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT));
        v.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        v.layout(0, 0, v.getMeasuredWidth(), v.getMeasuredHeight());
        Bitmap bitmap = Bitmap.createBitmap(v.getMeasuredWidth(),
                v.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);

        Canvas c = new Canvas(bitmap);
        v.layout(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
        v.draw(c);
        return bitmap;
    }

    private void setInfoMarker() {

    }
}

