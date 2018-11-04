public class Machine {

    private int machine_type;
    private int location;

    public Machine(int machine_type, int location) {
        this.machine_type = machine_type;
        this.location = location;
    }

    public int getMachine_type() {
        return machine_type;
    }

    public void setMachine_type(int machine_type) {
        this.machine_type = machine_type;
    }

    public int getLocation() {
        return location;
    }

    public void setLocation(int location) {
        this.location = location;
    }

    public void print(){
        System.out.println("machine type: " + machine_type + " location " + location);
    }
}
