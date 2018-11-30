package objects;

import java.util.*;

public class Depot {

    private boolean truckBeschikbaar;
    private Location location;
    private List<Truck> trucksList;
    private List<Machine> machineList;
    private HashMap<MachineType, Integer> truckToekeningMachineTypeMap;
    private HashMap<MachineType, Integer> requestToekeningMachineTypeMap;

    public Depot(Location location) {
        truckBeschikbaar = true;
        this.location = location;
        this.trucksList = new ArrayList<>();
        this.machineList = new ArrayList<>();
        this.truckToekeningMachineTypeMap = new HashMap<>();
        this.requestToekeningMachineTypeMap = new HashMap<>();
    }

    public void addMachine(Machine machine) {
        //System.out.println("depot has machine: "+machineList.contains(machine));
        machineList.add(machine);
        if (truckToekeningMachineTypeMap.containsKey(machine.getMachineType())) {
            int aantal = truckToekeningMachineTypeMap.get(machine.getMachineType());
            aantal++;
            truckToekeningMachineTypeMap.replace(machine.getMachineType(), aantal);
            requestToekeningMachineTypeMap.replace(machine.getMachineType(), aantal);
        } else {
            truckToekeningMachineTypeMap.put(machine.getMachineType(), 1);
            requestToekeningMachineTypeMap.put(machine.getMachineType(), 1);
        }

    }

    public void removeMachine(Machine machine) {
        if (machine != null) {
            if (truckToekeningMachineTypeMap.get(machine.getMachineType()) != null) {
                machineList.remove(machine);


                int aantal = truckToekeningMachineTypeMap.get(machine.getMachineType());

                aantal--;
                truckToekeningMachineTypeMap.replace(machine.getMachineType(), aantal);
            }
        }
    }


    public boolean isTruckBeschikbaar() {
        return truckBeschikbaar;
    }

    public void setTruckBeschikbaar(boolean truckBeschikbaar) {
        this.truckBeschikbaar = truckBeschikbaar;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public List<Truck> getTrucksList() {
        return trucksList;
    }

    public void setTrucksList(List<Truck> trucksList) {
        this.trucksList = trucksList;
    }

    public List<Machine> getMachineList() {
        return machineList;
    }

    public void setMachineList(List<Machine> machineList) {
        this.machineList = machineList;
    }

    public HashMap<MachineType, Integer> getTruckToekeningsMachineTypeMap() {
        return truckToekeningMachineTypeMap;
    }

    public void setTruckToekeningsMachineTypeSet(HashMap<MachineType, Integer> machineTypeMap) {
        this.truckToekeningMachineTypeMap = machineTypeMap;
    }

    public HashMap<MachineType, Integer> getRequestToekeningMachineTypeMap() {
        return requestToekeningMachineTypeMap;
    }

    public void setRequestToekeningMachineTypeMap(HashMap<MachineType, Integer> requestToekeningMachineTypeMap) {
        this.requestToekeningMachineTypeMap = requestToekeningMachineTypeMap;
    }

    public Machine getMachine(MachineType machineType) {
        if (truckToekeningMachineTypeMap.get(machineType) != null) {
            if (truckToekeningMachineTypeMap.get(machineType) > 0) {
                for (Machine machine : machineList) {
                    if (machine.getMachineType().getName().equals(machineType.getName())) {
                        return machine;
                    }
                }
            }
        }
        return null;
    }
}
