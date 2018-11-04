public class Location {
    private double lat;
    private double lon;
    private String name;
    private boolean depot;

    public Location(double lat, double lon, String name) {
        this.lat = lat;
        this.lon = lon;
        this.name = name;
        this.depot = false;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDepot(boolean depot) {
        this.depot = depot;
    }

    public boolean getDepot(){
        return depot;
    }

    public void print(){
        System.out.println("lat: " + lat + " lon: " + lon + " name: " + name + " depot: " + depot);
    }
}
