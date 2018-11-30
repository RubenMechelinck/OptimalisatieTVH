package heuristiek_impl;

import Evaluation.Evaluation;
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
    private static List<Request> surplusRequests;

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
        //paar iteraties
        for(int i = 0; i < 1000; i++)
            localSearch();
        //meta toepassen
    }

    //////////////////////////////// Perturbative Heuristiek onderdelen ///////////////////////

    //local search in huidige ruimte
    //kan request wisselen:
    //  binnen truck
    //  tussen trucks onderling
    //  locatie van depot request wisselen
    //  depot request zo dicht mogelijk bij elkaar zetten
    private static void localSearch(){
        moveRequestsBetweenTrucks();
        //move van Joran
        //nog andere moves
    }


    // hill climbing (eerste verbetering)
    //pak 2 random trucks
    //pak uit truck1 een requestkoppel (drop en collect)
    //zet drop op plaats waar truck2 het dichtst in buurt van droplocatie komt
    //zoek verder naar voor waar truck2 in buurt van collectlocatie komt
    //if collect past tussen => check if opl feasable + beter is
    // if not => zoek verder naar voor in requestlist van truck2 voor goeie DROP plaats
    //(de collect verder naar voor schuiven brengt wss niet veel op => drop naar voor => collect schuift ook naar voor)
    //kan koppeltje niet in truck2 zetten => neem volgende random truck totdat
    // koppeltje kan plaatsen
    // kan koppel in geen enkel truck plaatsen => pak volgend koppeltje uit truck1 en doe zelfde
    //vind nog altijd niets => pak andere truck1
    //als dan ook nog niet lukt => niets wordt gedaan
    private static void moveRequestsBetweenTrucks(){
        //drop en collect request met hun index in truck1 requestlijst zodat als
        //move niet lukt deze kan terug plaasten op de originele plek
        Request collect;
        Request drop;
        int indexDrop;
        int indexCollect;
        Evaluation evaluation;

        //shuffle zodat bij elke local search andere volgorde van truck picking is
        Collections.shuffle(trucksList);
        boolean placed = false; //is het koppeltje geplaatst
        int i = 0;
        while (i<trucksList.size()-1 && !placed){
            //gaat collect&drop koppel verplaatsen van truck1 naar truck2
            //TODO mogelijk om ook een request terug te plaatsen vanuit truck2 naar truck1
            Truck truck1 = trucksList.get(i++);

            while(!placed) {
                //get random drop/collect en de bijhorende collect/drop
                // + verwijder uit truck1 lijst!
                int q = (int) (Math.random() * truck1.getRoute().size());
                Request tmp = truck1.getRoute().remove(q);
                //if speciale request om leeg naar depot te rijden => pak een andere random
                if(tmp.getMachine() == null && tmp.getMachineType() == null)
                    continue;

                if(tmp.isDrop()) {
                    drop = tmp;
                    indexDrop = q;
                    indexCollect = vindKoppeltje(tmp, truck1.getRoute(), true, q);
                    collect = truck1.getRoute().remove(indexCollect);
                }
                else {
                    collect = tmp;
                    indexCollect = q;
                    indexDrop = vindKoppeltje(tmp, truck1.getRoute(), false, q);
                    drop = truck1.getRoute().remove(indexDrop);
                }

                while (!placed) {
                    if(i >= trucksList.size())
                        break;
                    Truck truck2 = trucksList.get(i++);
                    //get request dat dichtst in de buurt ligt van drop die moet tussen plaasten
                    // en zet de drop voor de gevonden plaats
                    List<Request> requestsListTruck2 = truck2.getRoute();
                    Request finalDrop = drop;
                    Queue<Request> bestePlaatsenVoorDrop = new PriorityQueue<>((request, t1) -> getDistance(request.getLocation(), finalDrop.getLocation()) - getDistance(t1.getLocation(), finalDrop.getLocation()));
                    bestePlaatsenVoorDrop.addAll(requestsListTruck2);

                    while (!bestePlaatsenVoorDrop.isEmpty()) {
                        Request besteInBuurtVoorDrop = bestePlaatsenVoorDrop.poll();
                        int index = requestsListTruck2.indexOf(besteInBuurtVoorDrop);
                        requestsListTruck2.add(index, drop);

                        //get beste plaats voor collect te zetten
                        //(request dat dichtst in de buurt van collect locatie ligt)
                        //get sublist van alles voor het ingevoegde drop request
                        List<Request> sublist = requestsListTruck2.subList(0, index);
                        Request besteInBuurtVoorCollect = getDichtsteRequest(sublist, collect);
                        //als geen beste gevonden is (bv index = 0 => sublist is null) => beste locatie is vooraan
                        if(besteInBuurtVoorCollect != null)
                            index = sublist.indexOf(besteInBuurtVoorCollect);
                        else
                            index = 0;
                        requestsListTruck2.add(index, collect);

                        evaluation = solution.evaluate();

                        //if oplossing is beter => deze localsearch itteratie is klaar (hill climbing)
                        if (evaluation.isFeasable() && evaluation.isBetterSolution()) {
                            placed = true;
                            break;
                        }
                        else{
                            truck2.getRoute().remove(drop);
                            truck2.getRoute().remove(collect);
                        }
                    }
                }

                //voeg laagste uitgehaald eerst toe (ander kan IndexOutOfBoundsDingenException krijgen)
                //tmp was eerst uitgehaalde, als drop eerst is uitgehaald => eerst collect terugzetten
                if(drop == tmp) {
                    truck1.getRoute().add(indexCollect, collect);
                    truck1.getRoute().add(indexDrop, drop);
                }
                else{
                    truck1.getRoute().add(indexDrop, drop);
                    truck1.getRoute().add(indexCollect, collect);
                }
            }
        }
    }

    //zoek de request die bij par:bron past, return index in par:list
    //par:isDrop zegt of bron een drop is
    //if bron is drop => return collect ervoor van dat machine type
    //if bron is collect => return drop erna van dat machine type
    private static int vindKoppeltje(Request bron, List<Request> list, boolean isDrop, int index){
        MachineType machineType = bron.getMachineType();
        if(machineType == null)
            machineType = bron.getMachine().getMachineType();

        //if bron is drop => zoek collect voor de drop
        //if bron is collect => zoek drop erna
        if(isDrop){
            for(int i = 0; i < index; i++){
                Request t = list.get(i);
                if(t.getMachine() == null && t.getMachineType() == null)
                    continue;

                if(!t.isDrop()  && (t.getMachineType() != null || t.getMachine().getMachineType() != null)){
                    MachineType type = t.getMachineType();
                    if(type == null)
                        type = t.getMachine().getMachineType();
                    if(machineType == type){
                        return i;
                    }
                }
            }
        }
        else{
            for(int i = index+1; i < list.size(); i++){
                Request t = list.get(i);
                if(t.getMachine() == null && t.getMachineType() == null)
                    continue;

                if(t.isDrop() && (t.getMachineType() != null || t.getMachine().getMachineType() != null)){
                    MachineType type = t.getMachineType();
                    if(type == null)
                        type = t.getMachine().getMachineType();
                    if(machineType == type){
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    //return de request uit par:requests die het dichtst licht bij de par:request
    private static Request getDichtsteRequest(List<Request> requests, Request request){
        int bestDistance = Integer.MAX_VALUE;
        Request dichtst = null;

        for(Request r: requests){
            int t = getDistance(r.getLocation(), request.getLocation());
            if(t < bestDistance){
                dichtst = r;
                bestDistance = t;
            }
        }

        return dichtst;
    }


    //////////////////////////////// Constructieve Heuristiek onderdelen ///////////////////////

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
                truck.addTotaleAfstandTruck(getDistance(truck.getStartlocatie(), dep.getLocation()));
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
            //TODO: wat als geen depots meer vind om in te dumpen? (staat in TODO om op te vallen ;) )
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
    private static boolean possibleAssignment(Truck truck, Request request) {
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
    private static Depot getNearestDepot(Location location) {
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
    private static int getNearestDepotTime(Location location) {
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
    private static Depot getNearestDepotWithMachine(Request request) {
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
    private static Truck bestFittingTruck(Request request) {
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
            emptyTruck(truck); //legen want truck is in depot
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
        emptyTruck(truck); //truck legen want truck is in depot

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