package heuristiek_impl;

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

/**
 * Created by ruben on 9/11/18.
 */
public class Heuristieken {
    // OPMERKINGEN
        //telkens als truck van locatie veranderd => currentLocation aanpassen + request aan route toevoegen!!!!


    /////////////////////////////// Heuristieken ///////////////////////////////

    public static void constructieveHeuristiek(){

        //voor start: machines toevoegen aan bijhorende depots
        initPlaceMachineInMachineListDepot();

        //voor start: trucks naar dichtstbijzijnde depot laten rijden
        initTruckToClosestDepots();

        //voeg requests toe aan dichtstbijzijnde depot
        Map<Location, Set<Request>> clustering = clusterRequestsToClosestDepotsWithTrucks();

        //wijs requests per depot toe aan trucks in dat depot
        assignRequestsToTrucks(clustering);
    }

    public static void perturbatieveHeuristiek(){

    }


    //////////////////////////////// Heuristiek onderdelen ///////////////////////

    private static void initPlaceMachineInMachineListDepot(){
        for(Machine machine: machineList)
            if(machine.getLocation().getDepot())
                for(Depot depot: depots)
                    if(depot.getLocation().equals(machine.getLocation()))                                               // Zal dit het juiste resultaat geven of moet de functie zelf geïmplementeerd worden?
                        depot.getMachineList().add(machine);
    }

    private static void initTruckToClosestDepots(){                                                                     // Kan waarschijnlijk efficiënter geprogrammeerd worden
        for(Truck truck: trucksList){
            //check if truck al niet op depot staat
            boolean inDepot = false;
            for(Depot d: depots) {
                if(d.getLocation().equals(truck.getStartlocatie())) {                                                   // Zal dit het juiste resultaat geven of moet de functie zelf geïmplementeerd worden?
                    inDepot = true;
                    break;
                    //geen extra afstand gereden + current is al start
                }
            }
            //if not zoek dichtsbijzijnde depot
            if(!inDepot){
                int distance = Integer.MAX_VALUE;
                Depot dep = null;
                for (Depot depot : depots) {
                    int tmp = getDistance(depot.getLocation(), truck.getStartlocatie());
                    if (tmp < distance){
                        distance = tmp;
                        dep = depot;
                    }
                }
                //wel extra afstand gereden van start naar depot => toevoegen aan route
                //drop staat op false! (zowel true en false hebben hier allebei geen betekenis)
                truck.addRequestToRoute(new Request(dep.getLocation(), null, false, true));
                truck.setCurrentLocation(dep.getLocation());
            }
        }
    }

    //return map met key = elke depot, value is set van alle requests die aan die depot zijn toegekent
    private static Map<Location, Set<Request>> clusterRequestsToClosestDepotsWithTrucks(){
        HashMap<Location, Set<Request>> cluster = new HashMap<>();

        //overloop requests en zoek dichtstbijzijnde depot
        /*for(Request request: requestList){
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

            for(Request request: requestList){
                boolean machineIsInDepot = true;
                int distance = Integer.MAX_VALUE;
                Depot dep = null;
                Machine machine = null;
                if(request.isDrop())
                    machine = request.getMachine();
                for (Depot depot : depots) {
                    if(machine != null) {
                        machineIsInDepot = false;
                        if(!depot.getMachineList().isEmpty())
                            for(Machine machineInDepot: depot.getMachineList())
                                if(machine.equals(machineInDepot))                                                      // Zal dit het juiste resultaat geven of moet de functie zelf geïmplementeerd worden?
                                    machineIsInDepot = true;
                    }
                    if (!depot.getTrucksList().isEmpty() && machineIsInDepot) {
                        int tmp = getDistance(depot.getLocation(), request.getLocation());
                        if (tmp < distance) {
                            distance = tmp;
                            dep = depot;
                        }
                    }
                }

            //voeg request toe aan gevonden dichtste depot in map
            Set<Request> requests = cluster.get(dep.getLocation());
            if(requests == null) {
                requests = new HashSet<>();
                cluster.put(dep.getLocation(), requests);
            }
            requests.add(request);
        }

        return cluster;
    }

    private static void assignRequestsToTrucks(Map<Location, Set<Request>> cluster){
        for(Depot depot: depots){
            Set<Request> requestsDepot = cluster.get(depot.getLocation());
        }
    }
}