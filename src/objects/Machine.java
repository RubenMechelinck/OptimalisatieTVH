package objects;

public class Machine {

    private MachineType machineType;
    private Location location;
    private int machineId;

    public Machine(MachineType machine_type, Location location, int machineId) {
        this.machineType = machine_type;
        this.location = location;
        this.machineId = machineId;
    }

    public MachineType getMachineType() {
        return machineType;
    }

    public void setMachineType(MachineType machineType) {
        this.machineType = machineType;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public void print(){
        System.out.println("machine type: " + machineType + " location " + location);
    }

    public int getMachineId() {
        return machineId;
    }

    public void setMachineId(int machineId) {
        this.machineId = machineId;
    }
}
