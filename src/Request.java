public class Request {

    private int location;
    private int machine;
    private boolean drop;
    private boolean depot;

    public Request(int location, int machine, boolean drop, boolean depot) {
        this.location = location;
        this.machine = machine;
        this.drop = drop;
        this.depot = depot;
    }

    public int getLocation() {
        return location;
    }

    public void setLocation(int location) {
        this.location = location;
    }

    public int getMachine() {
        return machine;
    }

    public void setMachine(int machine) {
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
