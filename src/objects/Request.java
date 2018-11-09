package objects;

public class Request {

    private Location location;
    private Machine machine;
    private boolean drop; //true = drop, false = collect
    private boolean depot;

    public Request(Location location, Machine machine, boolean drop, boolean depot) {
        this.location = location;
        this.machine = machine;
        this.drop = drop;
        this.depot = depot;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Machine getMachine() {
        return machine;
    }

    public void setMachine(Machine machine) {
        this.machine = machine;
    }

    public boolean isDrop() {
        return drop;
    }

    public void setDrop(boolean drop) {
        this.drop = drop;
    }

    public boolean isDepot() {
        return depot;
    }

    public void setDepot(boolean depot) {
        this.depot = depot;
    }

    public void print(){
        System.out.println("location: " + location + " machine: " + machine + " drop: " + drop + " depot: " + depot);
    }
}
