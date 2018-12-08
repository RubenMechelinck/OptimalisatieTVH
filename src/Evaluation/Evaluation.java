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
    private int infeasableOverload = 0;
    private boolean isFeasable = true;
    private Map<Truck, Integer> distanceMapping = new HashMap<>();
    private Map<Truck, Integer> overloadTimeMapping = new HashMap<>();

    private Map<Truck, Integer> revertDistanceMap;
    private Map<Truck, Integer> revertOverloadTimeMap;
    private boolean revertFeasable = true;


    public Evaluation() {
        completeEvaluation();
    }

    public Evaluation(Evaluation evaluation){
        totalDistance = evaluation.getTotalDistance();
        infeasableOverload = evaluation.getInfeasableOverload();
        isFeasable = evaluation.isFeasable();

        distanceMapping = new HashMap<>();
        for(Truck truck: evaluation.getDistanceMapping().keySet())
            distanceMapping.put(truck, evaluation.getDistanceMapping().get(truck));

        overloadTimeMapping = new HashMap<>();
        for(Truck truck: evaluation.getOverloadTimeMapping().keySet())
            overloadTimeMapping.put(truck, evaluation.getOverloadTimeMapping().get(truck));
    }


    private void completeEvaluation(){
        overloadTimeMapping = new HashMap<>();
        distanceMapping = new HashMap<>();

        for (Truck t : trucksList) {
            isFeasable = evaluate(t);

            if(!isFeasable) {
                break;
            }
        }

    }

    public void deltaEvaluate(Truck... trucks) {

        revertOverloadTimeMap = new HashMap<>();
        revertDistanceMap = new HashMap<>();
        revertFeasable = isFeasable;

        for (Truck truck: trucks) {
            revertDistanceMap.put(truck, distanceMapping.get(truck));
            totalDistance -= distanceMapping.get(truck);

            Integer t = overloadTimeMapping.get(truck);
            if(t != null) {
                revertOverloadTimeMap.put(truck, t);
                infeasableOverload -= t;
            }

            boolean result = evaluate(truck);

            //if een truck isFeasable is false => sws niet meer te rijden => break
            if(!result){
                isFeasable = false;
                break;
            }
        }
    }

    public void revert(){
        isFeasable = revertFeasable;

        for(Truck t: revertDistanceMap.keySet()){
            totalDistance -= distanceMapping.get(t);
            totalDistance += revertDistanceMap.get(t);
            distanceMapping.put(t, revertDistanceMap.get(t));
        }

        for(Truck t: revertOverloadTimeMap.keySet()){
            if(overloadTimeMapping.get(t) != null) {
                infeasableOverload -= overloadTimeMapping.get(t);
            }
            infeasableOverload += revertOverloadTimeMap.get(t);
            overloadTimeMapping.put(t, revertOverloadTimeMap.get(t));
        }
    }

    private boolean evaluate(Truck t){
        boolean isFeasable = true;

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
        }
        else{
            overloadTimeMapping.put(t, 0);
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
                    //System.out.println("pickup niet aanwezig infeasible");
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
                    //System.out.println("drop niet aanwezig infeasible");
                    isFeasable = false;
                    break;
                }
            }

            if (tempCapacity > t.getTruckCapacity()) {
                //System.out.println("capacity infeasible");
                isFeasable = false;
                break;
            }

            //Elke request wordt maar gedaan door 1 enkele truck, anders +10000
            if (doubleRequests(r, trucksList)) {
                //System.out.println("meerdere trucks aan zelfde request infeasible");
                isFeasable = false;
                break;
            }
        }

        if (t.getRoute().size() > 0) {
            if (t.getRoute().get(t.getRoute().size() - 1).getLocation() != t.getEindlocatie()) {
                //System.out.println("geen depot op einde infeasible");
                isFeasable = false;
            }
        }

        //op einde is capaciteit ook weer 0
        if (tempCapacity != 0) {
            //System.out.println("eindcapaciteit niet nul infeasible");
            isFeasable = false;
        }

        return isFeasable;
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

    public boolean isFeasable() {
        return isFeasable;
    }

    public int getInfeasableOverload() {
        return infeasableOverload;
    }

    public void setInfeasableOverload(int infeasableOverload) {
        this.infeasableOverload = infeasableOverload;
    }

    public boolean isReallyFeasable(){
        return infeasableOverload <= 0;
    }

    public Map<Truck, Integer> getDistanceMapping() {
        return distanceMapping;
    }

    public Map<Truck, Integer> getOverloadTimeMapping() {
        return overloadTimeMapping;
    }
}
