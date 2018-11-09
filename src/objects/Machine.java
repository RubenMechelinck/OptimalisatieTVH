package objects;

public class Machine {

    private MachineType machine_type;
    private Location location;

    public Machine(MachineType machine_type, Location location) {
        this.machine_type = machine_type;
        this.location = location;
    }

    public MachineType getMachine_type() {
        return machine_type;
    }

    public void setMachine_type(MachineType machine_type) {
        this.machine_type = machine_type;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public void print(){
        System.out.println("machine type: " + machine_type + " location " + location);
    }
}
