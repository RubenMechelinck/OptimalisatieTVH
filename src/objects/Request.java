package objects;

public class Request {

    private Location location;
    private MachineType machineType;
    private Machine machine;


    private boolean drop; //true = drop, false = collect
    private boolean depot;

    public Request(Location location, Machine machine, boolean drop, boolean depot) {
        this.location = location;
        this.machineType = null;
        this.machine = machine;
        this.machineType = null;
        this.drop = drop;
        this.depot = depot;
    }

    public Request(Location location, MachineType machineType, boolean drop, boolean depot) {
        this.location = location;
        this.machineType = machineType;
        this.machine = null;
        this.drop = drop;
        this.depot = depot;
    }

    public Request(Location location, boolean drop, boolean depot) {
        this.location = location;
        this.machineType = null;
        this.machine = null;
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

    public MachineType getMachineType() {
        return machineType;
    }

    public void setMachineType(MachineType machineType) {
        this.machineType = machineType;
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

    public void print() {
        if (machine != null) {
            System.out.println("location: " + location.getLocatieId() + " machine: " + machine.getMachineId() + " drop: " + drop + " depot: " + depot);
        } else {
            System.out.println("location: "+location.getLocatieId() +" geen machine drop "+ drop+ " depot "+depot);
        }
    }

}
