package fr.wildcodeschool.metro;

import android.location.Location;

public class Singleton {

    private static Singleton instance;
    private Settings settings;

    public static Singleton getInstance() {
        if (instance == null) {
            instance = new Singleton();
        }
        return instance;
    }
    private Singleton(){
    }

    public void initiateSettings(int zoom, boolean dropOff, Location lastKnownLocation, boolean fragmentActivity, boolean theme, int zoomProgress) {
        settings = new Settings(zoom, dropOff, lastKnownLocation, fragmentActivity, theme, zoomProgress);
    }

    public Settings getSettings() {
        return settings;
    }

    public void setZoom(int zoom) {
        settings.setZoom(zoom);
    }


    public void setTheme(boolean theme) {
        settings.setTheme(theme);
    }

    public void setDropOff(boolean dropOff){
        settings.setDropOff(dropOff);
    }

    public void setLocation(Location location) {
        settings.setLocation(location);
    }

    public void  setFragmentActivity(boolean fragmentActivity){
        settings.setFragmentActivity(fragmentActivity);
    }

    public void setZoomProgress(int zoomProgress) {
        settings.setZoomProgress(zoomProgress);
    }
}
