package utils;

import objects.*;

import java.io.*;
import java.util.List;

import static main.Main.*;

/**
 * Created by ruben on 9/11/18.
 */
public class FileUtils {

    public static void readFromFile(String filename) {

        String line;
        String[] split;
        int start;
        int truckCapacity = 0;
        int truckWorkingTime = 0;
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
        Truck trucker;

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
                    truckCapacity = Integer.parseInt(split[1]);
                    System.out.println("truck capacity = " + truckCapacity);
                } else if (split[start].equals("TRUCK_WORKING_TIME:")) {
                    truckWorkingTime = Integer.parseInt(split[1]);

                    System.out.println("truck time = " + truckWorkingTime);
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
                    locationList.add(new Location(Double.parseDouble(split[start + 1]), Double.parseDouble(split[start + 2]), split[start + 3], Integer.parseInt(split[start]), Integer.parseInt(split[start])));
                } else if (depot) {
                    Location loc = locationList.get(Integer.parseInt(split[start + 1]));
                    loc.setDepot(true);
                    Depot dep = new Depot(loc);
                    depots.add(dep);
                    size -= 1;
                    if (size < 1) {
                        depot = false;
                    }
                } else if (truck) {
                    trucker = new Truck(locationList.get(Integer.parseInt(split[start + 1])), locationList.get(Integer.parseInt(split[start + 2])), truckCapacity, truckWorkingTime, truckWorkingTime, Integer.parseInt(split[start]));
                    trucker.getRoute().add(new Request(trucker.getStartlocatie(), false, false));
                    trucksList.add(trucker);
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
                    machineList.add(new Machine(machineTypeList.get(Integer.parseInt(split[start + 1])), locationList.get(Integer.parseInt(split[start + 2])), Integer.parseInt(split[start])));
                    size -= 1;
                    if (size < 1) {
                        machine = false;
                    }
                } else if (drop) {

                    requestList.add(new Request(locationList.get(Integer.parseInt(split[start + 2])), machineTypeList.get(Integer.parseInt(split[start + 1])), true, false));

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

    public static void writeOutputFile(String outputFilename, String inputFilename, Solution solution) {
        File file = new File(outputFilename);
        int totalDistance = solution.getBestCost();
        List<Truck> trucksList = solution.getBestTrucksList();

        int workingTrucks = 0;
        int j;
        if (trucksList != null) {
            workingTrucks = getWorkingTrucks(trucksList);
        }


        boolean contin = true;
        try {
            PrintWriter printWriter = new PrintWriter(file);
            printWriter.println("PROBLEM: " + inputFilename);
            printWriter.println("DISTANCE: " + totalDistance);
            printWriter.println("TRUCKS: " + workingTrucks);
            if(trucksList!=null) {
                for (Truck truck : trucksList) {
                    //System.out.println(truck.getTruckId());
                    if (truck.worked()) {
                        printWriter.print(truck.getTruckId() + " ");
                        printWriter.print(truck.getTotaleAfstandTruck() + " ");
                        printWriter.print(truck.getTotaleTijdGereden() + " ");

                        //if geen tussenlocaties => print eindlocatie
                        if (truck.getRoute().size() == 0) {
                            //printWriter.print(truck.getEindlocatie().getLocatieId());

                            //if wel tussenstops print stops met machines
                        } else {
                            for (int i = 0; i < truck.getRoute().size(); i++) {
                                j = 1;
                                //  System.out.println(truck.getRoute().size());
                                Request request = truck.getRoute().get(i);

                                if (i != 0 && truck.getRoute().get(i - 1).getLocation() != request.getLocation()) {
                                    if (request.getMachine() != null) {
                                        printWriter.print(request.getLocation().getLocatieId() + ":" + request.getMachine().getMachineId());
                                    } else {
                                        printWriter.print(request.getLocation().getLocatieId());
                                    }


                                    //check of volgende request over zelfe locatie gaat, if so groepeer in output
                                    contin = true;
                                    while (i + j < truck.getRoute().size() && contin) {
                                        if (truck.getRoute().get(i + j).getLocation() == request.getLocation()) {
                                            if (truck.getRoute().get(i + j).getMachine() != null) {
                                                printWriter.print(":" + truck.getRoute().get(i + j).getMachine().getMachineId());

                                            } else {
                                                contin = false;
                                            }
                                        } else {
                                            contin = false;
                                        }
                                        j++;

                                    }
                                    printWriter.print(" ");
                                } else if (i == 0) {
                                    if (request.getMachine() != null) {
                                        printWriter.print(request.getLocation().getLocatieId() + ":" + request.getMachine().getMachineId());
                                    } else {
                                        printWriter.print(request.getLocation().getLocatieId());
                                    }


                                    //check of volgende request over zelfe locatie gaat, if so groepeer in output
                                    contin = true;
                                    while (i + j < truck.getRoute().size() && contin) {
                                        if (truck.getRoute().get(i + j).getLocation() == request.getLocation()) {
                                            if (truck.getRoute().get(i + j).getMachine() != null) {
                                                printWriter.print(":" + truck.getRoute().get(i + j).getMachine().getMachineId());

                                            } else {
                                                contin = false;
                                            }
                                        } else {
                                            contin = false;
                                        }
                                        j++;

                                    }
                                    printWriter.print(" ");
                                }

                            }
                        }
                        printWriter.println();
                    }
                }
            }

            printWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IndexOutOfBoundsException e2) {
            e2.printStackTrace();
        }


    }

    private static int getWorkingTrucks(List<Truck> trucksList) {
        int counter = 0;
        //if dit is null oplossing was niet feasable!!
        for (Truck truck : trucksList) {
            if (truckWorks(truck)) {
                counter++;
            }
        }
        return counter;
    }

    private static boolean truckWorks(Truck truck) {
        for (Request req : truck.getRoute()) {
            if (req.getMachine() != null) {
                return true;
            }
        }
        return false;
    }

}
