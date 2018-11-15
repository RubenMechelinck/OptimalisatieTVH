package heuristiek_impl;

import Evaluatie.Evaluatie;
import objects.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static main.Main.depots;
import static main.Main.requestList;
import static main.Main.trucksList;
import static main.Main.machineList;
import static utils.Utils.getDistance;
import static utils.Utils.getTime;

/**
 * Created by ruben on 9/11/18.
 */
public class Heuristieken {
    // OPMERKINGEN
        //telkens als truck van locatie veranderd => currentLocation aanpassen + request aan route toevoegen!!!!


    /////////////////////////////// Heuristieken ///////////////////////////////

    public static void constructieveHeuristiek() {

        //voor start: machines toevoegen aan bijhorende depots
        initPlaceMachineInMachineListDepot();
        requestList.size();

        //voor start: trucks naar dichtstbijzijnde depot laten rijden
        initTruckToClosestDepots();
        depots.size();

        //voeg requests toe aan dichtstbijzijnde depot
        Map<Depot, Set<Request>> clustering = clusterRequestsToClosestDepotsWithTrucks();
        requestList.size();
        depots.size();

        //wijs requests per depot toe aan trucks in dat depot
        assignRequestsToTrucks(clustering);

        //Zo kan je dan aan de weight en de afstand
        Evaluatie result = new Evaluatie(trucksList);
        result.getTotalDistance();
        result.getWeight();

    }

    public static void perturbatieveHeuristiek() {

    }


    //////////////////////////////// Heuristiek onderdelen ///////////////////////

    private static void initPlaceMachineInMachineListDepot() {
        for(Machine machine: machineList)
            if(machine.getLocation().getDepot())
                for(Depot depot: depots)
                    if(depot.getLocation().equals(machine.getLocation()))                                               // Zal dit het juiste resultaat geven of moet de functie zelf geïmplementeerd worden?
                        depot.getMachineList().add(machine);
    }

    private static void initTruckToClosestDepots() {                                                                     // Kan waarschijnlijk efficiënter geprogrammeerd worden
        for(Truck truck: trucksList) {
            //check if truck al niet op depot staat
            boolean inDepot = false;
            for(Depot d: depots) {
                if(d.getLocation().equals(truck.getStartlocatie())) {                                                   // Zal dit het juiste resultaat geven of moet de functie zelf geïmplementeerd worden?
                    d.getTrucksList().add(truck);
                    inDepot = true;
                    break;
                    //geen extra afstand gereden + current is al start
                }
            }
            //if not zoek dichtsbijzijnde depot
            if(!inDepot) {
                int distance = Integer.MAX_VALUE;
                Depot dep = null;
                for (Depot depot : depots) {
                    int tmp = getDistance(depot.getLocation(), truck.getStartlocatie());
                    if (tmp < distance) {
                        distance = tmp;
                        dep = depot;
                    }
                }
                //wel extra afstand gereden van start naar depot => toevoegen aan route
                //drop staat op false! (zowel true en false hebben hier allebei geen betekenis)
                truck.addRequestToRoute(new Request(dep.getLocation(), null, false, true));
                truck.setCurrentLocation(dep.getLocation());
                truck.setTotaleAfstandTruck(getDistance(truck.getStartlocatie(), dep.getLocation()));
                int tmp = truck.getTotaleTijdGereden()
                        + getTime(truck.getStartlocatie(), dep.getLocation())
                        + getTime(dep.getLocation(), truck.getEindlocatie());
                truck.setTotaleTijdGereden(tmp);
                dep.getTrucksList().add(truck);
            }
        }
    }

    //return map met key = elke depot, value is set van alle requests die aan die depot zijn toegekent
    private static Map<Depot, Set<Request>> clusterRequestsToClosestDepotsWithTrucks() {
        HashMap<Depot, Set<Request>> cluster = new HashMap<>();

        //overloop requests en zoek dichtstbijzijnde depot
        /*for(Request request: requestList) {
            int distance = Integer.MAX_VALUE;
            Depot dep = null;
            for (Depot depot : depots) {
                if (!depot.getTrucksList().isEmpty()) {
                    int tmp = getDistance(depot.getLocation(), request.getLocation());
                    if (tmp < distance) {
                        distance = tmp;
                        dep = depot;
                    }
                }
            }*/

        for(Request request: requestList) {
            boolean machineTypeIsInDepot = false;
            boolean inDepot = true;
            int distance = Integer.MAX_VALUE;
            Depot dep = null;
            MachineType machineType = null;
            if(request.isDrop())
                machineType = request.getMachine().getMachine_type();
            for (Depot depot : depots) {
                if (!cluster.containsKey(depot))
                    cluster.put(depot, new HashSet<>());
                if (machineType != null) {
                    inDepot = false;
                    if (!depot.getMachineList().isEmpty()) {
                        for (Machine machineInDepot : depot.getMachineList()) {
                            if (machineType.equals(machineInDepot.getMachine_type())) {                                  // Zal dit het juiste resultaat geven of moet de functie zelf geïmplementeerd worden?
                                inDepot = true;
                                machineTypeIsInDepot = true;
                            }
                        }
                    }
                }
                if (!depot.getTrucksList().isEmpty() && inDepot) {
                    int tmp = getDistance(depot.getLocation(), request.getLocation());
                    if (tmp < distance) {
                        distance = tmp;
                        dep = depot;
                    }
                }
            }
            if(!machineTypeIsInDepot) {
                dep = depots.get(0);
                for (Depot depot : depots)
                    if(depot.getMachineList().size() < dep.getMachineList().size())
                        dep = depot;
                //dep.getMachineList().add(request.getMachine());
            }
            //voeg request toe aan gevonden dichtste depot in map
            if(cluster.get(dep) != null) {
                Set<Request> requests = cluster.get(dep);
                if (requests == null) {
                    requests = new HashSet<>();
                    cluster.put(dep, requests);
                }
                requests.add(request);
            }
        }

        return cluster;
    }

    private static void assignRequestsToTrucks(Map<Depot, Set<Request>> cluster){
        while(!cluster.isEmpty()) {
            boolean isMovement = false;
            boolean truckBeschikbaar = false;
            do{
                for(Depot depot: depots) {
                    cluster.get(depot).size();
                    for(Request request: cluster.get(depot)) {
                        if(request.isDrop() && !depot.getMachineList().contains(request.getMachine()))
                            break;
                        else {
                            for(Truck truck: depot.getTrucksList()) {
                                int tijdRequest = truck.getTotaleTijdGereden();
                                tijdRequest += 2 * getTime(depot.getLocation(), request.getLocation())
                                        + request.getMachine().getMachine_type().getServiceTime();
                                if(tijdRequest <= truck.getTruckWorkingTime()) {
                                    truck.setTotaleTijdGereden(tijdRequest);
                                    truck.setTotaleAfstandTruck(2 * getDistance(depot.getLocation(), request.getLocation()));
                                    truck.addRequestToRoute(request);
                                    truck.addRequestToRoute(new Request(depot.getLocation(), null, true, true));
                                    cluster.get(depot).remove(request);
                                    if(request.isDrop()) {
                                        depot.getMachineList().remove(request.getMachine());
                                        // Ook een lijst van Machines per locatie?
                                    } else {
                                        depot.getMachineList().add(request.getMachine());
                                        //request.getMachine().setLocation(depot.getLocation());
                                    }
                                    isMovement = true;
                                    truck.setTijdVoorRequest(true);
                                    System.out.println("Request behandeld");
                                    break;
                                } else
                                    truck.setTijdVoorRequest(false);
                            }
                        }
                        // <Eerste comment>
                    }
                }
            } while(isMovement);
            for(Depot depot: depots) {
                for(Request request: cluster.get(depot)) {
                    if(request.isDrop() && !depot.getMachineList().contains(request.getMachine()))
                        request.setLocation(depots.get(depots.indexOf(depot) + 1).getLocation());
                }
            }
            for(Depot depot: depots) {
                boolean truckDepotBeschikbaar = false;
                for(Truck truck: depot.getTrucksList()) {
                    if(truck.isTijdVoorRequest())
                        truckDepotBeschikbaar = true;
                }
                if(!truckDepotBeschikbaar)
                    depot.setTruckBeschikbaar(false);
                else
                    truckBeschikbaar = true;
            }
            if(!truckBeschikbaar) {
                for(Truck truck: trucksList)
                    truck.setTruckWorkingTime(truck.getTruckWorkingTime() + 50);
            }
            // <Tweede comment>
        }
    }
}

                    // <Eerste comment>
                    /*if(!request.isDrop()) {
                        for(Truck truck: depot.getTrucksList()) {
                            if(truck.getTotaleTijdGereden() == getTime(truck.getStartlocatie(), depot.getLocation())){
                                int tmp = truck.getTotaleTijdGereden() + getTime(depot.getLocation(), truck.getEindlocatie());
                                truck.setTotaleTijdGereden(tmp);
                            }
                            int tijdRequest = truck.getTotaleTijdGereden();
                            tijdRequest += 2 * getTime(depot.getLocation(), request.getLocation())
                                           + request.getMachine().getMachine_type().getServiceTime();
                            if(tijdRequest <= truck.getTruckWorkingTime()) {
                                truck.setTotaleTijdGereden(tijdRequest);
                                truck.setTotaleAfstandTruck(2 * getDistance(depot.getLocation(), request.getLocation()));
                                truck.addRequestToRoute(request);
                                truck.addRequestToRoute(new Request(depot.getLocation(), null, true, true));
                                cluster.get(depot).remove(request);
                                depot.getMachineList().add(request.getMachine());
                                //request.getMachine().setLocation(depot.getLocation());
                                break;
                            }
                        }
                    }
                    else {
                        for(Truck truck: depot.getTrucksList()) {
                            if(depot.getMachineList().contains(request.getMachine())) {
                                if(truck.getTotaleTijdGereden() == getTime(truck.getStartlocatie(), depot.getLocation())) {
                                    int tmp = truck.getTotaleTijdGereden() + getTime(depot.getLocation(), truck.getEindlocatie());
                                    truck.setTotaleTijdGereden(tmp);
                                }
                                int tijdRequest = truck.getTotaleTijdGereden();
                                tijdRequest += 2 * getTime(depot.getLocation(), request.getLocation())
                                        + request.getMachine().getMachine_type().getServiceTime();
                                if(tijdRequest <= truck.getTruckWorkingTime()) {
                                    truck.setTotaleTijdGereden(tijdRequest);
                                    truck.setTotaleAfstandTruck(2 * getDistance(depot.getLocation(), request.getLocation()));
                                    truck.addRequestToRoute(request);
                                    truck.addRequestToRoute(new Request(depot.getLocation(), null, true, true));
                                    cluster.get(depot).remove(request);
                                    depot.getMachineList().remove(request.getMachine());
                                    break;
                                }
                            }
                        }
                    }*/


            // <Tweede comment>
            /*for(Location location: locationList) {
                if(!location.getDepot()) {
                    //Set<Request> requests = cluster.get(location);
                    for(Request request: cluster.get(location)) {

                    }
                }
            }
            for(Depot depot: depots) {
                Set<Request> requestsDepot = cluster.get(depot.getLocation());
                for(Request request: requestsDepot) {
                    if(depot.getMachineList().contains(request.getMachine())) {

                    }
                    else {

                    }
                }
            }*/