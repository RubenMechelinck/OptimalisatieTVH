package Evaluation;

import objects.Request;
import objects.Truck;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static main.Main.trucksList;

/**
 * Created by Joran on 13/11/18.
 */
public class Evaluation {
    private int totalDistance = 0;
    private int weight = 0;
    private int infeasableOverload = 0;
    private boolean isFeasable = true;
    private boolean reallyFeasable = true;
    private Map<Truck, Integer> distanceMapping = new HashMap<>();
    private Map<Truck, Integer> overloadTimeMapping = new HashMap<>();

    private Map<Truck, Integer> revertDistanceMap;
    private Map<Truck, Integer> revertOverloadTimeMap;
    private boolean revertFeasable = true;
    private boolean revertReallyFeasable = true;


    private int COST_DRIVE_LIMIT = 10000;
    private int COST_CAPACITY = 10000;
    private int COST_NO_DROP_COLLECT_COUPLE = 10000;
    private int COST_MULTIPLE_TRUCKS_PER_REQUEST = 10000;
    private int COST_NO_DEPOT_ENDPOINT = 10000;
    private int COST_ENDCAP_NOT_ZERO = 10000;

    private static int counter = 0;


    public Evaluation() {
        completeEvaluation();
    }

    private void completeEvaluation(){
        overloadTimeMapping = new HashMap<>();
        distanceMapping = new HashMap<>();

        for (Truck t : trucksList) {
            boolean[] result = evaluate(t);

            isFeasable = result[0];

            //als isFeasable false is => reallyFeasable is sws ook false => break
            if(!isFeasable) {
                reallyFeasable = false;
                break;
            }

            //vanaf dat reallyFeasable false is => blijft false
            if(reallyFeasable)
                reallyFeasable = result[1];
        }
    }

    //if een nieuwe trucks zijn isFeasable false => sws revert
    //else if alle isFeasable true en vorige isfeasable is ook true => nieuwe isfeasable is true
    //if een reallyfeasable is false => nieuwe reallyFeasable is ook false
    //if alle reallyfeasable is true en vorige reallyfeasable is ook true => nieuwe reallyfeasable is ook true
    //if alle reallyFeasable is true en vorige reallyfeasable is false => check alles voor juiste nieuwe reallyfeasable
    public void deltaEvaluate(Truck... trucks) {

        revertOverloadTimeMap = new HashMap<>();
        revertDistanceMap = new HashMap<>();
        revertReallyFeasable = reallyFeasable;
        revertFeasable = isFeasable;

        boolean oudeReallyFeasable = reallyFeasable;

        for (Truck truck: trucks) {
            revertDistanceMap.put(truck, distanceMapping.get(truck));
            totalDistance -= distanceMapping.get(truck);

            Integer t = overloadTimeMapping.get(truck);
            if(t != null) {
                revertOverloadTimeMap.put(truck, t);
                infeasableOverload -= t;
            }

            boolean[] result = evaluate(truck);

            //if een truck isFeasable is false => sws niet meer te rijden => break
            if(!result[0]){
                isFeasable = false;
                reallyFeasable = false;
                break;
            }

            //enkel reallyFeasable veranderen als true is
            if(reallyFeasable){
                reallyFeasable = result[1];
            }
        }

        if(!oudeReallyFeasable && reallyFeasable){
            completeEvaluation();
        }

        Evaluation evaluation = new Evaluation();
        if(evaluation.isFeasable() && evaluation.getInfeasableOverload() != infeasableOverload)
            System.out.println();

    }

    public void revert(){
        isFeasable = revertFeasable;
        reallyFeasable = revertReallyFeasable;

        for(Truck t: revertDistanceMap.keySet()){
            totalDistance -= distanceMapping.get(t);
            totalDistance += revertDistanceMap.get(t);
            distanceMapping.put(t, revertDistanceMap.get(t));
        }

        for(Truck t: revertOverloadTimeMap.keySet()){
            if(overloadTimeMapping.get(t) != null) {
                infeasableOverload -= overloadTimeMapping.get(t);
                infeasableOverload += revertOverloadTimeMap.get(t);
            }
            overloadTimeMapping.put(t, revertOverloadTimeMap.get(t));

        }
    }

    private boolean[] evaluate(Truck t){
        boolean isFeasable = true;
        boolean reallyFeasable = true;

        //Calculate the total distance
        int afstand = t.getTotaleAfstandTruck();
        totalDistance += afstand;
        distanceMapping.put(t, afstand);

        if (t.getTotaleTijdGereden() > t.getTruckWorkingTime()) {
            isFeasable = false;
        }

        //Een boete van 10000 indien een truck over zijn rijlimiet gaat
        if (t.getTotaleTijdGereden() > t.getREAL_TRUCK_WORKING_TIME()) {
            int overload = t.getTotaleTijdGereden() - t.getREAL_TRUCK_WORKING_TIME();
            infeasableOverload += overload;
            overloadTimeMapping.put(t, overload);
            weight += COST_DRIVE_LIMIT;
            reallyFeasable = false;
        }
        else{
            overloadTimeMapping.remove(t);
        }

        //Kijken of truck over zijn capaciteit gaat, zoja -> Straf van 10.000 per keer
        int tempCapacity = 0;
        //  System.out.println(t.getRoute().size());
        for (Request r : t.getRoute()) {
            //  r.print();
            if (r.isDrop()) {
                tempCapacity -= r.getMachine().getMachineType().getVolume();

                //Altijd kijken of de pickup hiervan ook aanwezig is!
                if (noPickUp(t, r)) {
                    weight += COST_NO_DROP_COLLECT_COUPLE;
                    //System.out.println("pickup niet aanwezig infeasible");
                    reallyFeasable = false;
                    isFeasable = false;
                    break;
                }
            } else {
                //System.out.println("r is geen drop");
                if (r.getMachine() != null) {
                    tempCapacity += r.getMachine().getMachineType().getVolume();
                }


                //Altijd kijken of de drop van deze pickup ook aanwezig is!
                if (noDrop(t, r)) {
                    weight += COST_NO_DROP_COLLECT_COUPLE;
                    //System.out.println("drop niet aanwezig infeasible");
                    reallyFeasable = false;
                    isFeasable = false;
                    break;
                }
            }

            if (tempCapacity > t.getTruckCapacity()) {
                weight += COST_CAPACITY;
                //System.out.println("capacity infeasible");
                reallyFeasable = false;
                isFeasable = false;
                break;
            }

            //Elke request wordt maar gedaan door 1 enkele truck, anders +10000
            if (doubleRequests(r, trucksList)) {
                weight += COST_MULTIPLE_TRUCKS_PER_REQUEST;
                //System.out.println("meerdere trucks aan zelfde request infeasible");
                reallyFeasable = false;
                isFeasable = false;
                break;
            }
        }

        if (t.getRoute().size() > 0) {
            if (!t.getRoute().get(t.getRoute().size() - 1).getLocation().equals(t.getEindlocatie())) {
                weight += COST_NO_DEPOT_ENDPOINT;
                //System.out.println("geen depot op einde infeasible");
                reallyFeasable = false;
                isFeasable = false;
            }
        }

        //op einde is capaciteit ook weer 0
        if (tempCapacity != 0) {
            weight += COST_ENDCAP_NOT_ZERO;
            //System.out.println("eindcapaciteit niet nul infeasible");
            reallyFeasable = false;
            isFeasable = false;
        }

        return new boolean[]{isFeasable, reallyFeasable};
    }


    private boolean doubleRequests(Request r, List<Truck> trucks) {
        int requests = 0;
        for (Truck t : trucks) {
            if (t.getRoute().contains(r)) {
                requests++;
            }
        }
        if (requests > 1) {
            return true;
        }
        return false;
    }

    private boolean noPickUp(Truck t, Request r) {
        boolean noPickUp = true;
        for (Request req : t.getRoute()) {
            if (r != req) {
                if (req.getMachine() != null && r.getMachine() != null) {
                    if (req.getMachine().getMachineId() == r.getMachine().getMachineId()) {
                        if (!req.isDrop()) {
                            noPickUp = false;
                        }
                    }
                } else if (r.getMachine() == null) {
                    noPickUp = false;
                }
            }
        }
        return noPickUp;
    }

    private boolean noDrop(Truck t, Request r) {
        boolean noDrop = true;
        for (Request req : t.getRoute()) {
            if (r != req) {
                if (req.getMachine() != null && r.getMachine() != null) {
                   // req.print();
                   // r.print();
                    if (req.getMachine().getMachineId() == r.getMachine().getMachineId()) {
                        if (req.isDrop()) {
                            noDrop = false;
                        }
                    }
                } else if (r.getMachine() == null) {
                    noDrop = false;
                } else if (req.getMachine() == null) {

                }
            } else{
                noDrop = false;
            }
        }
        return noDrop;
    }

    public int getTotalDistance() {
        return totalDistance;
    }

    public int getWeight() {
        return weight;
    }

    public boolean isFeasable() {
        return isFeasable;
    }

    public boolean isReallyFeasable() {
        return reallyFeasable;
    }

    public void setReallyFeasable(boolean reallyFeasable) {
        this.reallyFeasable = reallyFeasable;
    }

    public int getInfeasableOverload() {
        return infeasableOverload;
    }

    public void setInfeasableOverload(int infeasableOverload) {
        this.infeasableOverload = infeasableOverload;
    }


}
