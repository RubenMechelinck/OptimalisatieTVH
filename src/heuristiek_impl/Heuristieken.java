package heuristiek_impl;

import objects.Location;
import objects.Request;
import objects.Truck;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static main.Main.depots;
import static main.Main.requestList;
import static main.Main.trucksList;
import static utils.Utils.getDistance;

/**
 * Created by ruben on 9/11/18.
 */
public class Heuristieken {
    // OPMERKINGEN
        //telkens als truck van locatie veranderd => currentLocation aanpassen + request aan route toevoegen!!!!


    /////////////////////////////// Heuristieken ///////////////////////////////

    public static void contructieveHeuristiek(){

        //voor start: trucks naar dichtstbijzijnde depot laten rijden
        initTruckToClosestDepots();

        //voeg requests toe aan dichtstbijzijnde depot
        Map<Location, Set<Request>> clustering = clusterRequestsToClosestDepots();


    }

    public static void perturbatieveHeuristiek(){

    }


    //////////////////////////////// Heuristiek onderdelen ///////////////////////

    private static void initTruckToClosestDepots(){
        for(Truck truck: trucksList){
            //check if truck al niet op depot staat
            if(depots.contains(truck.getStartlocatie())) {
                //geen extra afstand gereden + current is al start
            }
            //if not zoek dichtsbijzijnde depot
            else{
                int distance = Integer.MAX_VALUE;
                Location dep = null;
                for (Location depot : depots) {
                    int tmp = getDistance(depot, truck.getStartlocatie());
                    if (tmp < distance){
                        distance = tmp;
                        dep = depot;
                    }
                }
                //wel extra afstand gereden van start naar depot => toevoegen aan route
                //drop staat op false! (zowel true en false hebben hier allebei geen betekenis)
                truck.addRequestToRoute(new Request(dep, null, false, true));
                truck.setCurrentLocation(dep);
            }
        }
    }

    //return map met key = elke depot, value is set van alle requests die aan die depot zijn toegekent
    private static Map<Location, Set<Request>> clusterRequestsToClosestDepots(){
        HashMap<Location, Set<Request>> cluster = new HashMap<>();

        //overloop requests en zoek dichtstbijzijnde depot
        for(Request request: requestList){
            int distance = Integer.MAX_VALUE;
            Location dep = null;
            for (Location depot : depots) {
                int tmp = getDistance(depot, request.getLocation());
                if (tmp < distance){
                    distance = tmp;
                    dep = depot;
                }
            }

            //voeg request toe aan gevonden dichtste depot in map
            Set<Request> requests = cluster.get(dep);
            if(requests == null)
                requests = new HashSet<>();
            requests.add(request);
        }

        return cluster;
    }




}
