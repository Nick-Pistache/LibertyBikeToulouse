package fr.wildcodeschool.metro;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;


public class SettingsFragment extends Fragment {
    private Singleton settings;
    public static Settings mSettings;
    private FloatingActionButton returnFloat;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        settings = Singleton.getInstance();
        mSettings = settings.getSettings();
        View settingsView = inflater.inflate(R.layout.fragment_settings, container, false);

        Switch switchTheme = settingsView.findViewById(R.id.switchTheme);
        switchTheme.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settings.setTheme(isChecked);
                if (mSettings.isTheme()) {
                    Toast.makeText(getContext(), "Dark theme activated", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(getContext(), "Default theme activated", Toast.LENGTH_SHORT).show();
                }
            }
        });

        returnFloat = settingsView.findViewById(R.id.fbReturn);
        returnFloat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goListStationActivity = new Intent(getActivity(), ListStation.class);
                startActivity(goListStationActivity);
            }
        });
        return settingsView;

    }
}
