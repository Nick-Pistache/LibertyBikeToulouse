package fr.wildcodeschool.metro;

public class Station implements Comparable<Station> {

    private int number;
    private String name;
    private String address;
    private double latitude;
    private double longitude;
    private int stands;
    private int availableBikes;
    private int availableStands;
    private float distance;

    private String status;

    public Station(int number, String name, String address, double latitude, double longitude, int stands, int availableBike, int availableStands, String status, float distance) {
        this.number = number;
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.stands = stands;
        this.availableBikes = availableBike;
        this.availableStands = availableStands;
        this.status = status;
        this.distance = distance;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        name = name;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        longitude = longitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        address = address;
    }

    public int getAvailableStands() {
        return stands;
    }

    public void setAvailableStands(int availableStands) {
        this.stands = availableStands;
    }

    public int getAvailableBikes() {
        return availableBikes;
    }

    public void setAvailableBikes(int availableBike) {
        this.availableBikes = availableBike;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getStands() {
        return stands;
    }

    public void setStands(int stands) {
        this.stands = stands;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    @Override
    public int compareTo(Station station) {
        return station.getDistance() < this.getDistance() ? 1 : -1;
    }
}
