package fr.wildcodeschool.metro;

import android.location.Location;

public class Settings {
    private int zoom;
    private boolean dropOff;
    private Location location;
    private boolean fragmentActivity;
    private boolean theme;

    public Settings(int zoom, boolean dropOff, Location location, boolean fragmentActivity, boolean theme) {
        this.zoom = zoom;
        this.dropOff = dropOff;
        this.location = location;
        this.fragmentActivity = fragmentActivity;
        this.theme = theme;
    }

    public int getZoom() {
        return zoom;
    }

    public void setZoom(int zoom) {
        this.zoom = zoom;
    }

    public boolean isDropOff() {
        return dropOff;
    }

    public void setDropOff(boolean dropOff) {
        this.dropOff = dropOff;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public boolean isTheme() {
        return theme;
    }

    public void setTheme(boolean theme) {
        this.theme = theme;
    }

    public boolean isFragmentActivity() {
        return fragmentActivity;
    }
    public void setFragmentActivity(boolean fragmentActivity) {
        this.fragmentActivity = fragmentActivity;
    }
}


