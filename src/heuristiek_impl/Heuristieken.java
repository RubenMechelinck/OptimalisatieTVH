package heuristiek_impl;

import objects.*;

import java.util.*;

import static java.lang.Thread.sleep;
import static main.Main.*;
import static utils.Utils.getDistance;
import static utils.Utils.getTime;

/**
 * Created by ruben on 9/11/18.
 */
public class Heuristieken {
    // OPMERKINGEN
    //telkens als truck van locatie veranderd => currentLocation aanpassen + request aan route toevoegen!!!!
    static List<Request> surplusRequests;

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

    }

    public static void perturbatieveHeuristiek() {

    }


    //////////////////////////////// Heuristiek onderdelen ///////////////////////

    //dit kan bij inlezen ook gebeuren
    private static void initPlaceMachineInMachineListDepot() {
        for (Machine machine : machineList) {
            if (machine.getLocation().getDepot()) {
                for (Depot depot : depots) {
                    if (depot.getLocation().equals(machine.getLocation())) {
                        depot.addMachine(machine);
                    }
                }
            }
        }
    }

    //trucks in hun dichtsbijzijnde depot plaatsen
    private static void initTruckToClosestDepots() {
        int distance;
        int tmp;
        Depot dep;
        for (Truck truck : trucksList) {
            dep = null;
            //check if truck al op depot staat

            //if not zoek dichtsbijzijnde depot
            if (!truck.getStartlocatie().getDepot()) {
                distance = Integer.MAX_VALUE;
                for (Depot depot : depots) {
                    tmp = getDistance(depot.getLocation(), truck.getStartlocatie());
                    if (tmp < distance) {
                        distance = tmp;
                        dep = depot;
                    }
                }
                //wel extra afstand gereden van start naar depot => toevoegen aan route
                //drop staat op false! (zowel true en false hebben hier allebei geen betekenis)
                truck.addRequestToRoute(new Request(dep.getLocation(), false, true));
                truck.setCurrentLocation(dep.getLocation());
                truck.setTotaleAfstandTruck(getDistance(truck.getStartlocatie(), dep.getLocation()));
                tmp = getTime(truck.getStartlocatie(), dep.getLocation()); //tijd voor truck om naar depot te gaan+// ;
                truck.addTotaleTijdGereden(tmp);
            } else {
                truck.addRequestToRoute(new Request(getNearestDepot(truck.getCurrentLocation()).getLocation(), false, true));
                dep = depots.get(truck.getStartlocatie().getLocatieId());

            }
            dep.getTrucksList().add(truck);
        }
    }

    //return map met key = elke depot, value is set van alle requests die aan die depot zijn toegekent
    private static Map<Depot, Set<Request>> clusterRequestsToClosestDepotsWithTrucks() {
        HashMap<Depot, Set<Request>> clusters = new HashMap<>();
        Set<Request> requestSet;
        int distance;
        int tmp;
        Depot dep;
        Request request;

        ListIterator<Request> requestListIterator = requestList.listIterator();
        //overloop requests en zoek dichtstbijzijnde depot
        while (requestListIterator.hasNext()) {
            distance = Integer.MAX_VALUE;
            dep = null;
            request = requestListIterator.next();

            for (Depot depot : depots) {
                if (possibleAddition(request, depot)) {
                    tmp = getDistance(depot.getLocation(), request.getLocation());
                    if (tmp < distance) {
                        distance = tmp;
                        dep = depot;
                    }
                }
            }
            if (dep != null) {
                if (clusters.containsKey(dep)) {
                    clusters.get(dep).add(request);
                } else {
                    requestSet = new HashSet<>();
                    requestSet.add(request);
                    clusters.put(dep, requestSet);
                }
                if (request.isDrop()) {
                    tmp = dep.getRequestToekeningMachineTypeMap().get(request.getMachineType());
                    tmp--;
                    dep.getRequestToekeningMachineTypeMap().replace(request.getMachineType(), tmp);
                }
                requestListIterator.remove();
            }
        }
        for (Depot depot : depots) {
            if (!clusters.containsKey(depot)) {
                requestSet = new HashSet<>();
                clusters.put(depot, requestSet);
            }
        }

        return clusters;
    }

    //kijk of een request aan een depot toegekend kan worden
    private static boolean possibleAddition(Request req, Depot depot) {
        if (req.isDrop()) {
            if (depot.getRequestToekeningMachineTypeMap().get(req.getMachineType()) != null && depot.getRequestToekeningMachineTypeMap().get(req.getMachineType()) > 0) {
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }

    }

    //controleer of constraints niet verbroken worden indien truck rechtstreeks naar request rijd.
    public static boolean possibleAssignment(Truck truck, Request request) {
        int serviceTijd;
        int extraVolume;
        int totaleTijd;
        int totaleCapacity;
        int truckUsedCapacity = truck.getCurrentUsedCapacity();
        int truckTijd = truck.getTotaleTijdGereden();
        int tijdNaarReq = getTime(truck.getCurrentLocation(), request.getLocation());
        int tijdNaarDepot = getNearestDepotTime(request.getLocation());
        Depot dep = getNearestDepot(request.getLocation());
        int tijdNaarHuis = getTime(dep.getLocation(), truck.getEindlocatie());


        if (request.isDrop()) {
            if (request.getMachineType() != null) {
                serviceTijd = 2 * request.getMachineType().getServiceTime();
                extraVolume = request.getMachineType().getVolume();
            } else {
                serviceTijd = 0;
                extraVolume = 0;
            }

        } else {
            if (request.getMachine() != null) {
                serviceTijd = 2 * request.getMachine().getMachineType().getServiceTime();
                extraVolume = request.getMachine().getMachineType().getVolume();
            } else {
                serviceTijd = 0;
                extraVolume = 0;
            }

        }

        totaleTijd = truckTijd + serviceTijd + tijdNaarReq + tijdNaarDepot + tijdNaarHuis;
        totaleCapacity = truckUsedCapacity + extraVolume;

        if (totaleTijd > truck.getTRUCK_WORKING_TIME()) {
            return false;
        }
        if (request.isDrop()) {
            if (!truck.isMachineTypeAvailable(request.getMachineType())) {
                if (totaleCapacity > truck.getTRUCK_CAPACITY()) {
                    return false;
                }
            }
        } else {
            if (totaleCapacity > truck.getTRUCK_CAPACITY()) {
                return false;
            }
        }
        return true;
    }

    //kijken of de truck aan de constraints voldoet indien deze eerst naar een depot rijd alvorens hij naar de drop moet rijden
    private static boolean possibleAssignmentFromDifferentDepot(Truck truck, Request drop, Depot depot) {

        int totaleTijd;
        int totaleCapacity;

        int serviceTijd = 2 * drop.getMachineType().getServiceTime();
        int extraVolume = drop.getMachineType().getVolume();

        int truckUsedCapacity = truck.getCurrentUsedCapacity();
        int truckTijd = truck.getTotaleTijdGereden();
        int tijdNaarReq = getTime(depot.getLocation(), drop.getLocation()) + getTime(truck.getCurrentLocation(), depot.getLocation());
        int tijdNaarDepot = getNearestDepotTime(drop.getLocation());
        Depot dep = getNearestDepot(drop.getLocation());
        int tijdNaarHuis = getTime(dep.getLocation(), truck.getEindlocatie());

        totaleTijd = truckTijd + serviceTijd + tijdNaarReq + tijdNaarDepot + tijdNaarHuis;
        totaleCapacity = truckUsedCapacity + extraVolume;

        if (totaleTijd > truck.getTRUCK_WORKING_TIME()) {
            return false;
        }
        if (!truck.isMachineTypeAvailable(drop.getMachineType())) {
            if (totaleCapacity > truck.getTRUCK_CAPACITY()) {
                return false;
            }
        }
        return true;

    }

    //kijken of de truck machine heeft of machine in depot van truck staat
    private static boolean possibleAssignDropToTruck(Truck truck, Request request) {
        Depot depot;
        if (request != null) {
            if (!truck.isMachineTypeAvailable(request.getMachineType())) {
                if (truck.getCurrentLocation().getDepot()) {
                    depot = getNearestDepot(truck.getCurrentLocation());
                    if (depot.getTruckToekeningsMachineTypeMap().get(request.getMachineType()) != null) {
                        if ((depot.getTruckToekeningsMachineTypeMap().get(request.getMachineType()) > 0)) {
                            return true;
                        } else {
                            return false;
                        }

                    }
                    return false;
                }
                return false;
            }
            return true;
        }
        return false;
    }

    //ophalen dichtste depot
    public static Depot getNearestDepot(Location location) {
        int time = Integer.MAX_VALUE;
        int tmp;
        Depot dep = null;
        for (Depot depot : depots) {
            tmp = getTime(depot.getLocation(), location);
            if (tmp < time) {
                time = tmp;
                dep = depot;
            }
        }
        return dep;
    }

    //ophalen tijd naar het dichste depot
    public static int getNearestDepotTime(Location location) {
        int time = Integer.MAX_VALUE;
        int tmp;
        for (Depot depot : depots) {
            tmp = getTime(depot.getLocation(), location);
            if (tmp < time) {
                time = tmp;
            }
        }
        return time;
    }

    //ophalen dichtste depot met machine geschikt voor dit request
    public static Depot getNearestDepotWithMachine(Request request) {
        int time = Integer.MAX_VALUE;
        int tmp;
        Depot dep = null;
        for (Depot depot : depots) {
            tmp = getTime(depot.getLocation(), request.getLocation());
            if (depot.getTruckToekeningsMachineTypeMap().get(request.getMachineType()) != null) {
                if (tmp < time && depot.getTruckToekeningsMachineTypeMap().get(request.getMachineType()) > 0) {
                    time = tmp;
                    dep = depot;
                }
            }
        }
        return dep;
    }

    //ophalen best gepaste truck voor de situatie (wordt gebruikt nadat iedere truck al een request gekregen heeft)
    public static Truck bestFittingTruck(Request request) {
        Truck bestTruck = null;
        int time = Integer.MAX_VALUE;
        int tmp;
        Depot depot;
        for (Truck truck : trucksList) {
            if (possibleAssignment(truck, request)) {
                if (request.isDrop()) {
                    if (possibleAssignDropToTruck(truck, request)) {
                        tmp = getTime(truck.getCurrentLocation(), request.getLocation());
                        if (tmp < time) {
                            time = tmp;
                            bestTruck = truck;
                        }
                    } else {
                        depot = getNearestDepotWithMachine(request);
                        if (depot != null) {
                            if (possibleAssignmentFromDifferentDepot(truck, request, depot)) {
                                tmp = getTime(truck.getCurrentLocation(), depot.getLocation());
                                tmp += getTime(depot.getLocation(), request.getLocation());
                                if (tmp < time) {
                                    time = tmp;
                                    bestTruck = truck;
                                }
                            }
                        }
                    }
                } else {
                    tmp = getTime(truck.getCurrentLocation(), request.getLocation());
                    if (tmp < time) {
                        time = tmp;
                        bestTruck = truck;
                    }
                }
            }
        }
        return bestTruck;
    }

    //alle trucks naar depot laten gaan om hun machines te legen
    private static void emptyTrucks() {
        for (Truck truck : trucksList) {
            emptyTruck(truck);
        }
    }

    //alle machines die niet nodig zijn voor een drop legen in het dichtsbijzijnde depot
    private static void emptyTruck(Truck truck) {
        Set<MachineType> stillNeededMachineTypes = new HashSet<>();

        for (Request req : requestList) {
            if (req.isDrop()) {
                stillNeededMachineTypes.add(req.getMachineType());
            }
        }
        if (!surplusRequests.isEmpty()) {
            for (Request req : surplusRequests) {
                if (req.isDrop()) {
                    stillNeededMachineTypes.add(req.getMachineType());
                }
            }
        }


        Iterator machineList = truck.getMachineList().listIterator();
        while (machineList.hasNext()) {
            Machine machine = (Machine) machineList.next();
            if (!stillNeededMachineTypes.contains(machine.getMachineType())) {
                if (doDrop(truck, machine)) {
                    machineList.remove();
                }
            }

        }
    }

    //een opgepakte machine in een depot plaatsen return true indien uitgevoerd return false indien niet uitgevoerd
    private static boolean doDrop(Truck truck, Machine machine) {
        Depot depot = getNearestDepot(truck.getCurrentLocation());
        if (possibleAssignment(truck, new Request(depot.getLocation(), machine, true, true))) {
            truck.addRequestToRoute(new Request(depot.getLocation(), machine, true, true));
            truck.addTotaleTijdGereden(getTime(depot.getLocation(), truck.getCurrentLocation()));
            truck.addTotaleAfstand(getDistance(depot.getLocation(), truck.getCurrentLocation()));
            truck.setCurrentLocation(depot.getLocation());
            return true;
        } else {
            return false;
        }
    }

    /*toekennen drop aan een truck die de machine bezit of op een depot staat waar machine zich bevind
      in de eerste toekenning mogen de camions enkel vanuit hun toegekende depot machines meenemen daarom geven we in de methode dit depot mee
      anders geven we null mee (zie bij vrije trucks en losse eindjes
    */
    private static void assignDropToTruck(Truck truck, Request drop, Depot depot) {
        Machine machine;
        if (truck.isMachineTypeAvailable(drop.getMachineType())) {
            drop.setMachine(truck.getMachineOfType(drop.getMachineType()));
            truck.removeMachine(truck.getMachineOfType(drop.getMachineType()));

        } else {
            if (depot == null) {
                depot = getNearestDepotWithMachine(drop);
            }
            machine = depot.getMachine(drop.getMachineType());
            depot.removeMachine(machine);
            drop.setMachine(machine);
            truck.addRequestToRoute(new Request(truck.getCurrentLocation(), drop.getMachine(), false, true));
            emptyTruck(truck);
        }
        truck.addTotaleTijdGereden(2 * drop.getMachineType().getServiceTime());

        truck.addTotaleTijdGereden(getTime(truck.getCurrentLocation(), drop.getLocation()));
        truck.addTotaleAfstand(getDistance(truck.getCurrentLocation(), drop.getLocation()));
        truck.setCurrentLocation(drop.getLocation());
        truck.addRequestToRoute(drop);
    }

    //toekennen drop aan truck indien deze eerst moet paseren langs een ander depot
    private static void assignDropToTruckFromDifferentDepot(Truck truck, Request drop, Depot depot) {
        depot.removeMachine(drop.getMachine());

        truck.addRequestToRoute(new Request(depot.getLocation(), drop.getMachine(), false, true));

        truck.addTotaleTijdGereden(getTime(truck.getCurrentLocation(), depot.getLocation()));
        truck.addTotaleAfstand(getDistance(truck.getCurrentLocation(), depot.getLocation()));
        truck.addTotaleTijdGereden(2 * drop.getMachineType().getServiceTime());
        truck.setCurrentLocation(depot.getLocation());
        emptyTruck(truck);

        truck.addTotaleTijdGereden(getTime(truck.getCurrentLocation(), drop.getLocation()));
        truck.addTotaleAfstand(getDistance(truck.getCurrentLocation(), drop.getLocation()));
        truck.setCurrentLocation(drop.getLocation());
        truck.addRequestToRoute(drop);
    }


    //toekennen collect aan truck
    private static void assignCollectToTruck(Truck truck, Request collect) {

        truck.getMachineList().add(collect.getMachine());
        truck.addTotaleTijdGereden(2 * collect.getMachine().getMachineType().getServiceTime());
        truck.addTotaleTijdGereden(getTime(truck.getCurrentLocation(), collect.getLocation()));
        truck.addTotaleAfstand(getDistance(truck.getCurrentLocation(), collect.getLocation()));
        truck.setCurrentLocation(collect.getLocation());
        truck.addRequestToRoute(collect);
    }



    private static void assignRequestsToTrucks(Map<Depot, Set<Request>> clusters) {

        Depot depot;
        Request request;
        List<Truck> trucksAtDepotList;
        List<Truck> vrijeTrucks = new ArrayList<>();
        Set<Request> requestSet;
        surplusRequests = new ArrayList<>();

        Iterator it = clusters.entrySet().iterator();
        Machine machine;

        //alle request van depots zoveel mogelijk proberen te verdelen over aanwezige trucks
        while (it.hasNext()) {

            Map.Entry pair = (Map.Entry) it.next();
            depot = (Depot) pair.getKey();
            requestSet = (Set<Request>) pair.getValue();
            trucksAtDepotList = depot.getTrucksList();

            //iterator over de Set met requests
            Iterator setIterator = requestSet.iterator();

            //verdelen over trucks
            for (Truck truck : trucksAtDepotList) {
                //indien een request aanwezig is -> geven aan truck.
                if (setIterator.hasNext()) {

                    request = (Request) setIterator.next();
                    //toekennen request aan truck + bewegen
                    if (possibleAssignment(truck, request)) {
                        if (request.isDrop()) {
                            if (possibleAssignDropToTruck(truck, request)) {
                                assignDropToTruck(truck, request, depot);
                            } else {
                                surplusRequests.add(request);
                            }
                        } else {
                            assignCollectToTruck(truck, request);
                        }
                    } else {
                        surplusRequests.add(request);
                    }
                    setIterator.remove();
                } else {
                    //indien er meer trucks dan requests zijn -> in vrije map steken
                    vrijeTrucks.add(truck);
                }
            }

            //indien er meer requests dan trucks zijn -> terug in requestLijst
            while (setIterator.hasNext()) {
                request = (Request) setIterator.next();
                surplusRequests.add(request);
                setIterator.remove();
            }
        }

        //overlopen requestlijst en toevoegen aan eerste truck die voldoet
        ListIterator requestListIterator = surplusRequests.listIterator();
        ListIterator machineListIterator;
        Truck truck;
        boolean requestNotAssigned;
        int counter;

        while (requestListIterator.hasNext()) {
            request = (Request) requestListIterator.next();
            machineListIterator = vrijeTrucks.listIterator();
            requestNotAssigned = true;
            counter = 0;
            while (machineListIterator.hasNext() && requestNotAssigned) {

                truck = (Truck) machineListIterator.next();
                if (possibleAssignment(truck, request)) {
                    //vrije trucks staan gegarandeerd in depot
                    if (request.isDrop()) {
                        //controleer of deze in depot beschikbaar is zoniet kijk in naburige depots indien nog niet sla over
                        if (possibleAssignDropToTruck(truck, request)) {
                            assignDropToTruck(truck, request, null);
                            requestNotAssigned = false;
                        } else {
                            depot = getNearestDepotWithMachine(request);
                            if (depot != null) {
                                machine = depot.getMachine(request.getMachineType());
                                if (possibleAssignmentFromDifferentDepot(truck, request, depot)) {
                                    request.setMachine(machine);
                                    assignDropToTruckFromDifferentDepot(truck, request, depot);
                                    requestNotAssigned = false;
                                }
                            } else {
                                if (!requestList.contains(request)) {
                                    requestList.add(request);
                                }
                            }
                        }
                    } else {
                        assignCollectToTruck(truck, request);
                        requestNotAssigned = false;
                    }
                    machineListIterator.remove();
                }
                counter++;
                if (counter == vrijeTrucks.size()) {
                    requestList.add(request);
                }
            }
            if (vrijeTrucks.isEmpty()) {
                requestList.add(request);
            }
            requestListIterator.remove();
        }

        //de rest van de requests toekennen aan de best passende truck
        ArrayList<Request> addList = new ArrayList<>();
        boolean requestsLeft = true;
        requestListIterator = requestList.listIterator();

        while (requestsLeft) {
            if (!addList.isEmpty()) {
                requestList.addAll(addList);
                emptyTrucks();
                addList = new ArrayList<>();
                requestListIterator = requestList.listIterator();
            }
            while (requestListIterator.hasNext()) {
                request = (Request) requestListIterator.next();
                truck = bestFittingTruck(request);

                if (truck != null) {
                    if (request.isDrop()) {
                        if (possibleAssignDropToTruck(truck, request)) {
                            assignDropToTruck(truck, request, null);

                        } else {
                            depot = getNearestDepotWithMachine(request);
                            if (depot != null) {
                                machine = depot.getMachine(request.getMachineType());
                                if ((possibleAssignmentFromDifferentDepot(truck, request, depot))) {
                                    request.setMachine(machine);
                                    assignDropToTruckFromDifferentDepot(truck, request, depot);
                                } else {
                                    addList.add(request);
                                }
                            } else {
                                addList.add(request);
                            }
                        }
                    } else {
                        assignCollectToTruck(truck, request);
                    }
                } else {
                    addList.add(request);
                }
                requestListIterator.remove();
            }

            if (addList.isEmpty() && requestList.isEmpty()) {
                requestsLeft = false;
            }
        }


        Machine truckMachine;

        for (Truck finishedtruck : trucksList) {
            machineListIterator = finishedtruck.getMachineList().listIterator();
            while (machineListIterator.hasNext()) {
                truckMachine = (Machine) machineListIterator.next();
                if (doDrop(finishedtruck, truckMachine)) {
                    machineListIterator.remove();
                }


            }
            if (finishedtruck.getCurrentLocation() != finishedtruck.getEindlocatie()) {
                if (possibleAssignment(finishedtruck, new Request(finishedtruck.getEindlocatie(), false, true))) {
                    finishedtruck.addTotaleTijdGereden(getTime(finishedtruck.getCurrentLocation(), finishedtruck.getEindlocatie()));
                    finishedtruck.addRequestToRoute(new Request(finishedtruck.getEindlocatie(), false, true));
                    finishedtruck.addTotaleAfstand(getDistance(finishedtruck.getEindlocatie(), finishedtruck.getCurrentLocation()));

                    finishedtruck.setCurrentLocation(finishedtruck.getEindlocatie());
                }
            }
        }
    }
}