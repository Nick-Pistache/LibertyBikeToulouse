package fr.wildcodeschool.metro;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class StationsRecyclerAdapter extends RecyclerView.Adapter<StationsRecyclerAdapter.ViewHolder> {

    private ArrayList<Station> mStations;
    private String userID;
    private FirebaseDatabase database;
    private FirebaseAuth userAuth;
    private FirebaseUser currentUser;
    private Settings mSettings;
    private Singleton settings;
    private ArrayList<Integer> favoriteStations;
    private DatabaseReference favoriteStationBase;

    public StationsRecyclerAdapter(ArrayList<Station> stations) {
        mStations = stations;
    }

    @Override
    public StationsRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_display_recycle_station, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final StationsRecyclerAdapter.ViewHolder holder, final int position) {
        settings = Singleton.getInstance();
        mSettings = settings.getSettings();
        Station station = mStations.get(position);
        holder.stationNameView.setText(station.getName());
        holder.stationAddressView.setText(station.getAddress());
        holder.distanceView.setText(String.format("%s  meters",(int) station.getDistance()));
        holder.bikesView.setText((Integer.toString(station.getAvailableBikes())));
        holder.standsView.setText((Integer.toString(station.getAvailableStands())));
        initiateDatabase();
        if (mSettings.isFragmentActivity()) {
            stationsOnFavoriteFragment(holder, position);
            clickOnBikeWay(holder, station);

        } else {
            holder.favoriteView.setImageResource(R.drawable.ic_favorite_unchecked);
            holder.favoriteView.setTag(R.drawable.ic_favorite_unchecked);
            setFavoriteStation(station, holder);
            stationsOnListStation(holder, position, station);
            clickOnBikeWay(holder, station);
        }
    }

    @Override
    public int getItemCount() {
        return mStations.size();

    }

    public void clickOnBikeWay(final StationsRecyclerAdapter.ViewHolder holder, final Station station) {
        final LatLng stationLatLng = new LatLng(station.getLatitude(), station.getLongitude());
        final LatLng userLatLng = new LatLng(mSettings.getLocation().getLatitude(), mSettings.getLocation().getLongitude());

        holder.makeWayView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), v.getContext().getString(R.string.wayComputing), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                        Uri.parse("http://maps.google.com/maps?saddr=" + stationLatLng.latitude + "," + stationLatLng.longitude + "&daddr=" + userLatLng.latitude + "," + userLatLng.longitude));
                v.getContext().startActivity(intent);
            }
        });
    }

    private void stationsOnFavoriteFragment(final StationsRecyclerAdapter.ViewHolder holder, final int position) {
        holder.favoriteView.setImageResource(R.drawable.ic_clear);
        holder.favoriteView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                favoriteStationBase.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        eraseFromDataBase(dataSnapshot, position);
                        deleteFromAdapter(holder);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {

                        Toast.makeText(v.getContext(), v.getContext().getString(R.string.readValueFailed), Toast.LENGTH_LONG).show();
                    }
                });

            }
        });
    }

    private void setFavoriteStation(Station station, final StationsRecyclerAdapter.ViewHolder holder) {
        favoriteStationBase.child(Integer.toString(station.getNumber())).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    holder.favoriteView.setImageResource(R.drawable.ic_favorite_checked);
                    holder.favoriteView.setTag(R.drawable.ic_favorite_checked);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
    }

    private void stationsOnListStation(final StationsRecyclerAdapter.ViewHolder holder, final int position, final Station station) {
        holder.favoriteView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Toast.makeText(v.getContext(), v.getContext().getString(R.string.addedToFavorites), Toast.LENGTH_SHORT).show();
                if (holder.favoriteView.getTag().equals(R.drawable.ic_favorite_checked)) {
                    holder.favoriteView.setImageResource(R.drawable.ic_favorite_unchecked);
                    holder.favoriteView.setTag(R.drawable.ic_favorite_unchecked);
                    favoriteStationBase.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            eraseFromDataBase(dataSnapshot, position);
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {

                            Toast.makeText(v.getContext(), v.getContext().getString(R.string.failedToReadValue), Toast.LENGTH_LONG).show();
                        }
                    });

                } else {
                    saveDatabase(position);
                    holder.favoriteView.setImageResource(R.drawable.ic_favorite_checked);
                    holder.favoriteView.setTag(R.drawable.ic_favorite_checked);
                }
            }
        });

    }

    private void initiateDatabase() {
        database = FirebaseDatabase.getInstance();
        userAuth = FirebaseAuth.getInstance();
        currentUser = userAuth.getCurrentUser();
        if (currentUser != null) {
            userID = currentUser.getUid();
        }
        favoriteStationBase = database.getReference(userID);
    }

    private void eraseFromDataBase(DataSnapshot dataSnapshot, int position) {
        for (DataSnapshot favoriteStationNumberData : dataSnapshot.getChildren()) {
            if (Integer.parseInt(favoriteStationNumberData.getKey()) == mStations.get(position).getNumber()) {
                favoriteStationNumberData.getRef().removeValue();
            }
        }
    }

    private void saveDatabase(int position) {
        String key = Integer.toString(mStations.get(position).getNumber());
        favoriteStationBase.child(key).setValue(mStations.get(position).getNumber());
    }

    public void deleteFromAdapter(StationsRecyclerAdapter.ViewHolder holder) {
        mStations.remove(holder.getAdapterPosition());
        notifyItemRemoved(holder.getAdapterPosition());
        notifyItemRangeChanged(holder.getAdapterPosition(), mStations.size());
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView stationNameView, stationAddressView, distanceView, bikesView, standsView;
        ImageView favoriteView, makeWayView;

        public ViewHolder(View v) {
            super(v);
            this.stationNameView = v.findViewById(R.id.tvStationName);
            this.stationAddressView = v.findViewById(R.id.tvStationAddress);
            this.distanceView = v.findViewById(R.id.tvDistance);
            this.bikesView = v.findViewById(R.id.tvBikes);
            this.standsView = v.findViewById(R.id.tvStands);
            this.favoriteView = v.findViewById(R.id.ibFavorite);
            this.makeWayView = v.findViewById(R.id.ibWay);
        }
    }

}



