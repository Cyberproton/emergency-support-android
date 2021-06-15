package hcmut.team15.emergencysupport.model;

public class Location {
    private double longitude;
    private double latitude;
    private double altitude;

    public Location(double longitude, double latitude, double altitude) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.altitude = altitude;
    }

    public Location(android.location.Location location) {
        this(location.getLongitude(), location.getLatitude(), location.getAltitude());
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public static float distanceBetween(Location l1, Location l2) {
        float[] results = new float[2];
        android.location.Location.distanceBetween(l1.latitude, l1.longitude, l2.latitude, l2.longitude, results);
        return results[0];
    }
}
