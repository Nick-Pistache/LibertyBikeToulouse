package fr.wildcodeschool.metro;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static fr.wildcodeschool.metro.Helper.extractStation;


public class FavoriteFragment extends Fragment {
    private ArrayList<Integer> mFavoriteStationNumbers = new ArrayList<>();
    private int mFavoriteStationNumber;
    private Settings mSettings;
    private ArrayList<Station> mFavoriteStation = new ArrayList<>();
    private Singleton settings;
    private FloatingActionButton returnFloat;
    private String userID;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View favoriteView = inflater.inflate(R.layout.fragment_favorite, container, false);
        settings = Singleton.getInstance();
        mSettings = settings.getSettings();
        settings.setFragmentActivity(true);
        FirebaseDatabase favoriteStationBase = FirebaseDatabase.getInstance();
        FirebaseAuth userAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = userAuth.getCurrentUser();
        if (currentUser != null) {
            userID = currentUser.getUid();
        }
        DatabaseReference favoriteStationReference = favoriteStationBase.getReference(userID);
        favoriteStationReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot favoriteStationNumberData : dataSnapshot.getChildren()) {
                    mFavoriteStationNumber = Integer.parseInt(favoriteStationNumberData.getKey());
                    mFavoriteStationNumbers.add(mFavoriteStationNumber);
                }
                extractStation(getContext(), mSettings, new Helper.BikeStationListener() {
                    @Override
                    public void onResult(ArrayList<Station> stations) {
                        for (int index = 0; index < mFavoriteStationNumbers.size(); index++) {
                            for (Station searchFavoriteStation : stations) {
                                if (searchFavoriteStation.getNumber() == mFavoriteStationNumbers.get(index)) {
                                    mFavoriteStation.add(searchFavoriteStation);
                                }
                            }
                        }
                        RecyclerView recycleListStations = favoriteView.findViewById(R.id.fragment_recycle_station);
                        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
                        recycleListStations.setLayoutManager(layoutManager);
                        StationsRecyclerAdapter adapter = new StationsRecyclerAdapter(mFavoriteStation);
                        recycleListStations.setAdapter(adapter);
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError error) {

                Toast.makeText(getContext(), getString(R.string.fail), Toast.LENGTH_LONG).show();
            }
        });
        returnFloat = favoriteView.findViewById(R.id.fbReturn);
        returnFloat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settings.setFragmentActivity(false);
                Intent goListStationActivity = new Intent(getActivity(), ListStation.class);
                startActivity(goListStationActivity);
            }
        });
        return favoriteView;
    }
}
