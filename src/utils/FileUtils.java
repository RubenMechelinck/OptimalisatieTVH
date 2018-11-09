package utils;

import objects.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import static main.Main.*;

/**
 * Created by ruben on 9/11/18.
 */
public class FileUtils {

    public static void readFromFile(String filename) {

        String line;
        String[] split;
        int start;
        int truckCap;
        int truckTime;
        boolean location = false;
        int size = 0;
        int dimension = 0;
        boolean depot = false;
        boolean truck = false;
        boolean machine_type = false;
        boolean machine = false;
        boolean drop = false;
        boolean collect = false;
        boolean timeMatrixCheck = false;
        boolean distanceMatrixCheck = false;

        try {

            BufferedReader bufferedReader =
                    new BufferedReader(new FileReader(filename));
            while ((line = bufferedReader.readLine()) != null) {
                //System.out.println(line);
                split = line.split("\\s+");
                if (split[0].equals("") && (split.length > 1)) {
                    start = 1;
                } else {
                    start = 0;
                }
                if (split[start].equals("INFO:")) {
                    System.out.println("discard");
                } else if (split[start].equals("TRUCK_CAPACITY:")) {
                    truckCap = Integer.parseInt(split[1]);
                    System.out.println("truck capacity = " + truckCap);
                } else if (split[start].equals("TRUCK_WORKING_TIME:")) {
                    truckTime = Integer.parseInt(split[1]);

                    System.out.println("truck time = " + truckTime);
                } else if (split[start].equals("LOCATIONS:")) {
                    location = true;
                    size = Integer.parseInt(split[1]);
                    dimension = size;
                    System.out.println("starting location creation for " + size + " locations");

                } else if (split[start].equals("DEPOTS:")) {
                    depot = true;
                    size = Integer.parseInt(split[1]);
                    System.out.println("starting Depot creation for " + size + " Depots");
                } else if (split[start].equals("TRUCKS:")) {
                    truck = true;
                    size = Integer.parseInt(split[1]);
                    System.out.println("starting objects.Truck creation for " + size + " Trucks");
                } else if (split[start].equals("MACHINE_TYPES:")) {
                    machine_type = true;
                    size = Integer.parseInt(split[1]);
                    System.out.println("starting machine type creation for " + size + " machine types");
                } else if (split[start].equals("MACHINES:")) {
                    machine = true;
                    size = Integer.parseInt(split[1]);
                    System.out.println("starting machine creation for " + size + " machines");
                } else if (split[start].equals("DROPS:")) {
                    drop = true;
                    size = Integer.parseInt(split[1]);
                    System.out.println("starting drop creation for " + size + " Drops");
                } else if (split[start].equals("COLLECTS:")) {
                    collect = true;
                    size = Integer.parseInt(split[1]);
                    System.out.println("starting collect creation for " + size + " collects");
                } else if (split[start].equals("TIME_MATRIX:")) {
                    timeMatrixCheck = true;
                    size = Integer.parseInt(split[1]);
                    timeMatrix = new int[size][size];
                    System.out.println("starting time matrix creation for " + size + " locations");
                } else if (split[start].equals("DISTANCE_MATRIX:")) {
                    distanceMatrixCheck = true;
                    size = Integer.parseInt(split[1]);
                    distanceMatrix = new int[size][size];
                    System.out.println("starting distance matrix creation for " + size + " locations");
                } else if (location) {
                    //System.out.println("entering location");
                    size -= 1;
                    if (size < 1) {
                        location = false;
                    }
                    //System.out.println(location);
                    locationList.add(new Location(Double.parseDouble(split[start + 1]), Double.parseDouble(split[start + 2]), split[start + 3], Integer.parseInt(split[start])));
                } else if (depot) {
                    Location dep = locationList.get(Integer.parseInt(split[start + 1]));
                    dep.setDepot(true);
                    depots.add(dep);
                    size -= 1;
                    if (size < 1) {
                        depot = false;
                    }
                } else if (truck) {
                    trucksList.add(new Truck(locationList.get(Integer.parseInt(split[start + 1])), locationList.get(Integer.parseInt(split[start + 2]))));
                    size -= 1;
                    if (size < 1) {
                        truck = false;
                    }
                } else if (machine_type) {
                    machineTypeList.add(new MachineType(Integer.parseInt(split[start + 1]), Integer.parseInt(split[start + 2]), split[start + 3]));
                    size -= 1;
                    if (size < 1) {
                        machine_type = false;
                    }
                } else if (machine) {
                    machineList.add(new Machine(machineTypeList.get(Integer.parseInt(split[start + 1])), locationList.get(Integer.parseInt(split[start + 2]))));
                    size -= 1;
                    if (size < 1) {
                        machine = false;
                    }
                } else if (drop) {
                    requestList.add(new Request(locationList.get(Integer.parseInt(split[start + 1])), machineList.get(Integer.parseInt(split[start + 2])), true, false));
                    size -= 1;
                    if (size < 1) {
                        drop = false;
                    }
                } else if (collect) {
                    requestList.add(new Request(machineList.get(Integer.parseInt(split[start + 1])).getLocation(), machineList.get(Integer.parseInt(split[start + 1])), false, false));
                    size -= 1;
                    if (size < 1) {
                        collect = false;
                    }
                } else if (timeMatrixCheck) {
                    int j = 0;
                    for (int i = start; i < split.length; i++) {
                        timeMatrix[dimension - size][j] = Integer.parseInt(split[i]);
                        j += 1;
                    }
                    size -= 1;
                    if (size < 1) {
                        timeMatrixCheck = false;
                    }
                } else if (distanceMatrixCheck) {
                    int j = 0;
                    for (int i = start; i < split.length; i++) {
                        distanceMatrix[dimension - size][j] = Integer.parseInt(split[i]);
                        j += 1;
                    }
                    size -= 1;
                    if (size < 1) {
                        distanceMatrixCheck = false;
                    }
                }
            }
            bufferedReader.close();
        } catch (
                IOException e) {
            e.printStackTrace();
        }

        /*for (Location l : locationList) {
            l.print();
        }
        for (Truck t : trucksList) {
            t.print();
        }
        for (MachineType m_type : machineTypeList) {
            m_type.print();
        }
        for (Machine m : machineList) {
            m.print();
        }
        for (Request r : requestList) {
            r.print();
        }
        System.out.println("time");
        for (int i = 0; i < timeMatrix.length; i++) {
            for (int j = 0; j < timeMatrix[i].length; j++) {
                System.out.print(timeMatrix[i][j]);

                if (timeMatrix[i][j] < 10) {
                    System.out.print("   ");
                } else if (timeMatrix[i][j] < 100) {
                    System.out.print("  ");
                } else if (timeMatrix[i][j] < 1000) {
                    System.out.print(" ");
                }
            }
            System.out.println("");
        }
        System.out.println("distance");
        for (int i = 0; i < distanceMatrix.length; i++) {
            for (int j = 0; j < distanceMatrix[i].length; j++) {
                System.out.print(distanceMatrix[i][j]);
                if (distanceMatrix[i][j] < 10) {
                    System.out.print("   ");
                } else if (distanceMatrix[i][j] < 100) {
                    System.out.print("  ");
                } else if (distanceMatrix[i][j] < 1000) {
                    System.out.print(" ");
                }
            }
            System.out.println("");
        }*/
    }

}
