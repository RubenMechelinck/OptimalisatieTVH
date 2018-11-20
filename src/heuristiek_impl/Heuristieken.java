package heuristiek_impl;

import objects.*;

import java.util.*;

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

        //voor start: trucks naar dichtstbijzijnde depot laten rijden
        initTruckToClosestDepots();

        //voeg requests toe aan dichtstbijzijnde depot
        Map<Depot, Set<Request>> clustering = clusterRequestsToClosestDepotsWithTrucks();

        //wijs requests per depot toe aan trucks in dat depot
        assignRequestsToTrucks(clustering);

        //trucks naar hun eindlocatie laten rijden
        driveTrucksToEndLocation();

    }

    public static void perturbatieveHeuristiek() {

    }


    //////////////////////////////// Heuristiek onderdelen ///////////////////////

    // Dit kan bij inlezen ook gebeuren
    // Plaatsen van alle machines die in een depot staan in de respectievelijke depotList
    private static void initPlaceMachineInMachineListDepot() {
        for (Machine machine: machineList)
            if (machine.getLocation().getDepot())
                for (Depot depot: depots)
                    if (depot.getLocation().equals(machine.getLocation()))
                        depot.getMachineList().add(machine);
    }

    // Rijden van trucks naar dichtstbijzijnde depot
    private static void initTruckToClosestDepots() {
        for (Truck truck: trucksList) {

            // Check if truck al op depot staat
            boolean inDepot = false;
            for (Depot d: depots) {
                if (d.getLocation().equals(truck.getStartlocatie())) {
                    d.getTrucksList().add(truck);
                    inDepot = true;
                    break;
                    //geen extra afstand gereden + current is al start
                }
            }

            // If not zoek dichtsbijzijnde depot
            if (!inDepot) {
                int distance = Integer.MAX_VALUE;
                Depot dep = null;
                for (Depot depot : depots) {
                    int tmp = getDistance(depot.getLocation(), truck.getStartlocatie());
                    if (tmp < distance) {
                        distance = tmp;
                        dep = depot;
                    }
                }
                // Wel extra afstand gereden van start naar depot => toevoegen aan route
                // Drop staat op false! (zowel true en false hebben hier allebei geen betekenis)
                truck.addRequestToRoute(new Request(dep.getLocation(), null, null, false, true));
                truck.setCurrentLocation(dep.getLocation());
                truck.setTotaleAfstandTruck(getDistance(truck.getStartlocatie(), dep.getLocation()));
                int tmp = getTime(truck.getStartlocatie(), dep.getLocation())
                        + getTime(dep.getLocation(), truck.getEindlocatie()); //wat is dit? => ik voeg al de tijd toe dat de truck erover zal doen om van de depot tot aan z'n eindlocatie te geraken.
                truck.addTotaleTijdGereden(tmp);
                dep.getTrucksList().add(truck);
            }
        }
    }

    // Return map met key = elke depot, value is set van alle requests die aan dat depot zijn toegekend
    private static Map<Depot, Set<Request>> clusterRequestsToClosestDepotsWithTrucks() {
        HashMap<Depot, Set<Request>> cluster = new HashMap<>();

        // Overloop requests en zoek dichtstbijzijnde depot
        for (Request request: requestList) {
            boolean machineTypeIsInDepot = true;
            boolean inDepot = true;
            int distance = Integer.MAX_VALUE;
            Depot dep = null;
            MachineType machineType = null;
            // Als de request een drop is, initialiseer extra variabelen
            if (request.isDrop()) {
                machineType = request.getMachineType();
                machineTypeIsInDepot = false;
            }
            for (Depot depot : depots) {
                if (!cluster.containsKey(depot))
                    cluster.put(depot, new HashSet<>());
                if (machineType != null) {
                    inDepot = false;
                    if (!depot.getMachineList().isEmpty()) {
                        for (Machine machineInDepot : depot.getMachineList()) {
                            if (machineType.equals(machineInDepot.getMachineType())) {
                                inDepot = true;
                                machineTypeIsInDepot = true;
                                break;
                            }
                        }
                    }
                }
                // Als er een truck in depot is en de machine is aanwezig in geval van een drop,
                // vergelijk de afstand van de request tot dit depot met andere geschikte depots
                if (!depot.getTrucksList().isEmpty() && inDepot) {
                    int tmp = getDistance(depot.getLocation(), request.getLocation());
                    if (tmp < distance) {
                        distance = tmp;
                        dep = depot;
                    }
                }
            }
            // Als een machine niet aanwezig is in één van de depots,
            // wijs de request toe aan het depot met de minste requests
            if (!machineTypeIsInDepot) {
                dep = depots.get(0);
                for (Depot depot : depots)
                    if (depot.getMachineList().size() < dep.getMachineList().size())
                        dep = depot;
            }
            // Voeg de request toe aan het dichtstbijzijnde depot in map
            if (cluster.get(dep) != null) {
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

    // Input: Map gecreëerd in de methode 'clusterRequestsToClosestDepotsWithTrucks()'
    private static void assignRequestsToTrucks(Map<Depot, Set<Request>> cluster){
        int requestCount = 0;
        boolean requestsAanwezig = true;
        // Blijf doorlopen zolang er nog requests onbehandeld zijn
        while (requestsAanwezig) {
            boolean isMovement;
            boolean truckBeschikbaar = false;
            // Blijf doorlopen zolang er nog requests aan trucks toegevoegd worden
            do {
                isMovement = false;
                // Doorlopen van elk depot
                for (Depot depot: depots) {
                    List<Request> tempRequestList = new LinkedList<>();
                    // Doorlopen van elke request aanwezig in bovenliggend depot
                    for (Request request: cluster.get(depot)) {
                        // Als de request een 'drop' is, controleer of machineType aanwezig is in depot
                        if (request.isDrop()) {
                            boolean machineInDepot = false;
                            for (Machine machine: depot.getMachineList()) {
                                if (machine.getMachineType().equals(request.getMachineType())) {
                                    machineInDepot = true;
                                    break;
                                }
                            }
                            // Indien machine niet aanwezig in depot, ga naar de volgende request
                            if (!machineInDepot) break;
                        }
                        // Doorlopen van de truckList in bovenliggend depot
                        // Controleren of er nog plaats is voor een request.
                        // Indien plaats --> request toewijzen en alle nodige lijsten en variabelen updaten
                        for (Truck truck: depot.getTrucksList()) {
                            int tijdRequest = truck.getTotaleTijdGereden();
                            if (request.isDrop()) {
                                tijdRequest += 2 * getTime(depot.getLocation(), request.getLocation())
                                               + request.getMachineType().getServiceTime();
                            } else {
                                tijdRequest += 2 * getTime(depot.getLocation(), request.getLocation())
                                               + request.getMachine().getMachineType().getServiceTime();
                            }
                            if (tijdRequest <= truck.getTruckWorkingTime()) {
                                truck.setTotaleTijdGereden(tijdRequest);
                                truck.setTotaleAfstandTruck(2 * getDistance(depot.getLocation(), request.getLocation()));
                                truck.addRequestToRoute(request);
                                truck.addRequestToRoute(new Request(depot.getLocation(), null, null, true, true));
                                tempRequestList.add(request);
                                if (request.isDrop()) {
                                    for (Machine machine: depot.getMachineList()) {
                                        if (machine.getMachineType().equals(request.getMachineType())) {
                                            depot.getMachineList().remove(machine);
                                            request.setMachine(machine);
                                            break;
                                        }
                                    }
                                } else {
                                    depot.getMachineList().add(request.getMachine());
                                    request.setMachine(null);
                                }
                                isMovement = true;
                                truck.setTijdVoorRequest(true);
                                requestCount++;
                                System.out.println("Request " + requestCount + " behandeld");
                                break;
                            } else
                                truck.setTijdVoorRequest(false);
                        }
                    }
                    // Verwijderen van alle behandelde requests uit de cluster
                    for (Request request: tempRequestList) {
                        Set<Request> requestsDepot = cluster.get(depot);
                        requestsDepot.remove(request);
                    }
                }
            } while (isMovement);

            // Controleren of er nog onbehandelde requests zijn
            for (Depot depot: depots) {
                if (!cluster.get(depot).isEmpty()) {
                    requestsAanwezig = true;
                    break;
                } else
                    requestsAanwezig = false;
            }

            // Controleren of er nog trucks beschikbaar zijn
            for (Depot depot: depots) {
                boolean truckDepotBeschikbaar = false;
                for (Truck truck: depot.getTrucksList()) {
                    if (truck.isTijdVoorRequest()) {
                        truckDepotBeschikbaar = true;
                        break;
                    }
                }
                if (!truckDepotBeschikbaar)
                    depot.setTruckBeschikbaar(false);
                else
                    truckBeschikbaar = true;
            }
            if (!truckBeschikbaar) {
                for (Truck truck: trucksList)
                    truck.setTruckWorkingTime(truck.getTruckWorkingTime() + 50);
            }

            // Shuffelen van onbehandelde requests tussen depots
            for (Depot depot: depots) {
                List<Request> tempRequestList = new LinkedList<>();
                for (Request request: cluster.get(depot)) {
                    boolean machineTypeAanwezig = false;
                    // Controleren bij een drop of de machine aanwezig is in het depot
                    if (request.isDrop()) {
                        for (Machine machine : depot.getMachineList()) {
                            if (machine.getMachineType().equals(request.getMachineType())) {
                                if (depot.isTruckBeschikbaar()) {
                                    machineTypeAanwezig = true;
                                    break;
                                } else {
                                    // TODO: assign truck from other depot
                                }
                            }
                        }
                    }
                    if (!request.isDrop() || !machineTypeAanwezig)
                        tempRequestList.add(request);
                }
                // Random toewijzen van onbehandelde requests aan depots
                if (!tempRequestList.isEmpty()) {
                    for (Request request : tempRequestList) {
                        Random random = new Random();
                        int randomInt = random.nextInt(depots.size());
                        Set<Request> requestsFrom = cluster.get(depot);
                        Set<Request> requestsTo = cluster.get(depots.get(randomInt));
                        requestsFrom.remove(request);
                        requestsTo.add(request);
                    }
                }
            }
        }
        System.out.println("Truck Working Time: " + trucksList.get(0).getTRUCK_WORKING_TIME());
    }

    private static void driveTrucksToEndLocation() {
        for (Truck truck: trucksList) {
            double geredenAfstand = truck.getTotaleAfstandTruck() +
                                    getDistance(truck.getCurrentLocation(), truck.getEindlocatie());
            truck.setTotaleAfstandTruck(geredenAfstand);
        }
    }
}