package fr.wildcodeschool.metro;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class ShareFragment extends Fragment {
    private View shareView;
    private FloatingActionButton returnFloat;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        shareView = inflater.inflate(R.layout.fragment_share, container, false);
        shareView = inflater.inflate(R.layout.fragment_send, container, false);
        returnFloat = shareView.findViewById(R.id.fbReturn);
        returnFloat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goListStationActivity = new Intent(getActivity(), ListStation.class);
                startActivity(goListStationActivity);
            }
        });

        return  shareView;
    }

}
