package heuristiek_impl;

import Evaluation.Evaluation;
import objects.*;

import java.util.*;

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
        //niet gebruiken anders miserie!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        //initTruckToClosestDepots();

        //voeg requests toe aan dichtstbijzijnde depot
        Map<Depot, Set<Request>> clustering = new HashMap<>();//clusterRequestsToClosestDepotsWithTrucks();

        //wijs requests per depot toe aan trucks in dat depot
        assignRequestsToTrucks(clustering);

    }

    public static void perturbatieveHeuristiek() {
        //paar iteraties
        for(int i = 0; i < 800; i++) {
            System.out.println("itr " + i);
            localSearch();
        }
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
    private static void moveRequestsBetweenTrucks() {
        //drop en collect request met hun index in truck1 requestlijst zodat als
        //move niet lukt deze kan terug plaasten op de originele plek
        Request collect;
        Request drop;
        int indexDrop;
        int indexCollect;
        Evaluation evaluation;
        Truck truck1;
        Truck truck2;

        int NUMBER_OF_TRUCKS_2 = 20;
        int NUMBER_OF_REQUESTS_TRY_FROM_TRUCK_1 = 30;
        int NUMBER_OF_BEST_BESTE_PLAATSEN_TRY = 10;

        //shuffle zodat bij elke local search andere volgorde van truck picking is
        Collections.shuffle(trucksList);
        boolean placed = false; //is het koppeltje geplaatst
        int i = 0;

        //gaat collect&drop koppel verplaatsen van truck1 naar truck2
        truck1 = trucksList.get(i);
        //System.out.println("\ttruck1: " + i);
        i++;

        int it = 0;
        //probeer 30 requests uit truck1 te verplaatsen
        while(it < NUMBER_OF_REQUESTS_TRY_FROM_TRUCK_1 && it < truck1.getRoute().size()) {
            placed = false;
            //System.out.println("\t\t" + it);
            //get random drop/collect en de bijhorende collect/drop
            // + verwijder uit truck1 lijst!
            //-1 want laatste mag niet pakken!!
            int q = (int) (Math.random() * truck1.getRoute().size()-1);
            Request tmp = truck1.removeRequest(q, false);
            it++;
            //if speciale request om leeg naar depot te rijden => zet terug in list
            // en pak een andere random
            if(tmp.getMachine() == null && tmp.getMachineType() == null){
                truck1.addRequestToRoute(tmp, q, false);
                continue;
            }

            if(tmp.isDrop()) {
                drop = tmp;
                indexDrop = q;
                collect = tmp.getPair();
                if(collect == truck1.getRoute().getLast()){
                    truck1.addRequestToRoute(drop, indexDrop, false);
                    continue;
                }
                indexCollect = truck1.removeRequest(collect, false);
            }
            else {
                collect = tmp;
                indexCollect = q;
                drop = tmp.getPair();
                if(drop == truck1.getRoute().getLast()){
                    truck1.addRequestToRoute(collect, indexCollect, false);
                    continue;
                }
                indexDrop = truck1.removeRequest(drop, false);
            }

            int j = 0;
            while (!placed && j < trucksList.size() && j < NUMBER_OF_TRUCKS_2) {
                if(j == i-1) {
                    j++;
                    continue;
                }
                truck2 = trucksList.get(j);
                //System.out.println("\t\t\ttruck2: " + j);
                j++;

                //get request dat dichtst in de buurt ligt van drop die moet tussen plaasten
                // en zet de drop voor de gevonden plaats
                List<Request> requestsListTruck2 = truck2.getRoute();
                Request finalDrop = drop;
                Queue<Request> bestePlaatsenVoorDrop = new PriorityQueue<>((request, t1) -> getDistance(request.getLocation(), finalDrop.getLocation()) - getDistance(t1.getLocation(), finalDrop.getLocation()));

                //ipv addAll slechts een paar toevoegen, als voor een paar geen betere move kan uitvoeren => truck is wss ver weg aan het rijden
                // set zal wss geen 20 groot zijn (duplicaten)
                bestePlaatsenVoorDrop.addAll(requestsListTruck2);
                /*Set<Integer> numbers = new HashSet<>();
                for(int ii = 0; ii<1000; ii++){
                    int r = (int)(Math. random() * requestsListTruck2.size());
                    numbers.add(r);
                    if(!numbers.contains(r))
                        bestePlaatsenVoorDrop.add(requestsListTruck2.get(r));
                }*/
                int v = 0;
                while (!bestePlaatsenVoorDrop.isEmpty() && v++ < NUMBER_OF_BEST_BESTE_PLAATSEN_TRY) {
                    //System.out.println("\t\t\t\tnieuwe beste plaats genomen");
                    Request besteInBuurtVoorDrop = bestePlaatsenVoorDrop.poll();
                    int index = requestsListTruck2.indexOf(besteInBuurtVoorDrop);
                    //check of index 0 is => start locatie is beste => moet drop NA start zetten!!
                    if(index == 0)
                        index = 1;

                    truck2.addRequestToRoute(drop, index, false);

                    //get beste plaats voor collect te zetten
                    //(request dat dichtst in de buurt van collect locatie ligt)
                    //get sublist van alles voor het ingevoegde drop request
                    List<Request> sublist = requestsListTruck2.subList(0, index);
                    Request besteInBuurtVoorCollect = getDichtsteRequest(sublist, collect);

                    index = sublist.indexOf(besteInBuurtVoorCollect);
                    //collect na de beste locatie zetten, zodat niet voor startlocatie zou komen
                    index++;

                    truck2.addRequestToRoute(collect, index, false);

                    evaluation = solution.evaluate(truck1, truck2);

                    if (evaluation != null) {
                        //if oplossing is beter => deze localsearch iteratie is klaar (hill climbing)
                        if(evaluation.isReallyFeasable())
                            System.out.println(evaluation.getTotalDistance());
                        //if not feasable maar wel minder overload => goeie stap => verder doen in volgende itr
                        else
                            System.out.println("infeasable overload " + evaluation.getInfeasableOverload());

                        placed = true;
                        break;
                    }
                    else{
                        truck2.removeRequest(drop, false);
                        truck2.removeRequest(collect, false);
                        placed = false;
                    }
                }
            }

            //enkel request terug plaatsen in truck1 als nergens anders kon zetten
            if(!placed) {
                //voeg laagste uitgehaald eerst toe (ander kan IndexOutOfBoundsDingenException krijgen)
                //tmp was eerst uitgehaalde, als drop eerst is uitgehaald => eerst collect terugzetten
                if (drop == tmp) {
                    truck1.addRequestToRoute(collect, indexCollect, false);
                    truck1.addRequestToRoute(drop, indexDrop, false);
                } else {
                    truck1.addRequestToRoute(drop, indexDrop, false);
                    truck1.addRequestToRoute(collect, indexCollect, false);
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
                    if (depot.getLocation() == machine.getLocation()) {
                        depot.addMachine(machine);
                    }
                }
            }
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

        if (totaleTijd > truck.getTruckWorkingTime()) {
            return false;
        }
        if (request.isDrop()) {
            if (!truck.isMachineTypeAvailable(request.getMachineType())) {
                if (totaleCapacity > truck.getTruckCapacity()) {
                    return false;
                }
            }
        } else {
            if (totaleCapacity > truck.getTruckCapacity()) {
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

        if (totaleTijd > truck.getTruckWorkingTime()) {
            return false;
        }
        if (!truck.isMachineTypeAvailable(drop.getMachineType())) {
            if (totaleCapacity > truck.getTruckCapacity()) {
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
            Request collect = truck.getRequestForMachine(machine);
            Request drop = new Request(depot.getLocation(), machine, true, true);
            drop.setPair(collect);
            collect.setPair(drop);
            truck.addRequestToRoute(drop, true);
            /*
            truck.addTotaleTijdGereden(getTime(depot.getLocation(), truck.getCurrentLocation()));
            truck.addTotaleAfstand(getDistance(depot.getLocation(), truck.getCurrentLocation()));*/
            //truck.setCurrentLocation(depot.getLocation());
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
            Request collect = truck.getRequestForMachine(drop.getMachine());
            drop.setPair(collect);
            collect.setPair(drop);
            truck.removeMachine(truck.getMachineOfType(drop.getMachineType()));

        } else {
            if (depot == null) {
                depot = getNearestDepot(truck.getCurrentLocation());
            }
            machine = depot.getMachine(drop.getMachineType());
            depot.removeMachine(machine);
            drop.setMachine(machine);
            Request collect = new Request(truck.getCurrentLocation(), drop.getMachine(), false, true);
            truck.addTotaleTijdGereden(2 * drop.getMachineType().getServiceTime());
            truck.addRequestToRoute(collect, true);
            drop.setPair(collect);
            collect.setPair(drop);
            emptyTruck(truck); //legen want truck is in depot
        }

/*
        truck.addTotaleTijdGereden(getTime(truck.getCurrentLocation(), drop.getLocation()));
        truck.addTotaleAfstand(getDistance(truck.getCurrentLocation(), drop.getLocation()));*/
        //truck.setCurrentLocation(depot.getLocation());
        truck.addRequestToRoute(drop, true);
    }

    //toekennen drop aan truck indien deze eerst moet paseren langs een ander depot
    private static void assignDropToTruckFromDifferentDepot(Truck truck, Request drop, Depot depot) {
        depot.removeMachine(drop.getMachine());

        Request collect = new Request(depot.getLocation(), drop.getMachine(), false, true);
        truck.addRequestToRoute(collect, true);
        collect.setPair(drop);
        drop.setPair(collect);
        /*
        truck.addTotaleTijdGereden(getTime(truck.getCurrentLocation(), depot.getLocation()));
        truck.addTotaleAfstand(getDistance(truck.getCurrentLocation(), depot.getLocation()));
        */
        truck.addTotaleTijdGereden(2 * drop.getMachineType().getServiceTime());
        //truck.setCurrentLocation(depot.getLocation());
        emptyTruck(truck); //truck legen want truck is in depot

        /*
        truck.addTotaleTijdGereden(getTime(truck.getCurrentLocation(), drop.getLocation()));
        truck.addTotaleAfstand(getDistance(truck.getCurrentLocation(), drop.getLocation()));
        */
        //truck.setCurrentLocation(drop.getLocation());
        truck.addRequestToRoute(drop, true);
    }


    //toekennen collect aan truck
    private static void assignCollectToTruck(Truck truck, Request collect) {

        truck.getMachineList().add(collect.getMachine());
        truck.addTotaleTijdGereden(2 * collect.getMachine().getMachineType().getServiceTime());
        /*
        truck.addTotaleTijdGereden(getTime(truck.getCurrentLocation(), collect.getLocation()));
        truck.addTotaleAfstand(getDistance(truck.getCurrentLocation(), collect.getLocation()));*/
        //truck.setCurrentLocation(collect.getLocation());
        truck.addRequestToRoute(collect, true);
    }

    private static void assignRequestsToTrucks(Map<Depot, Set<Request>> clusters) {

        Depot depot;
        Request request;

        surplusRequests = new ArrayList<>();

        Machine machine;

        //requests toekennen aan trucks
        ArrayList<Request> addList = new ArrayList<>();
        boolean requestsLeft = true;
        Iterator requestListIterator = requestList.listIterator();
        Truck truck;
        int addlistSize = -1;
        while (requestsLeft) {

            if (!addList.isEmpty()) {
                addlistSize = addList.size();
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
            if (addlistSize == addList.size()) {
                for (Truck trucker : trucksList) {
                    trucker.addToTruckWorkingTime(50);
                }
            }
        }



        Machine truckMachine;
        Iterator<Machine> machineListIterator;
        for (Truck finishedtruck : trucksList) {
            machineListIterator = finishedtruck.getMachineList().listIterator();
            while (machineListIterator.hasNext()) {
                truckMachine = machineListIterator.next();
                if (doDrop(finishedtruck, truckMachine)) {
                    machineListIterator.remove();
                }
            }
            if (finishedtruck.getCurrentLocation() != finishedtruck.getEindlocatie()) {
                finishedtruck.addRequestToRoute(new Request(finishedtruck.getEindlocatie(), false, true), true);


            }
        }

    }
}