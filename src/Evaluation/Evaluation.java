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
    private boolean isFeasable = true;
    private boolean isBetterSolution = false;
    private Map<Truck, Integer> distanceMapping = new HashMap<>();

    private Map<Truck, Integer> revertMap;
    private int revertTotalDistance;
    private boolean revertFeasable;
    private boolean revertBetter;


    private int COST_DRIVE_LIMIT = 10000;
    private int COST_CAPACITY = 10000;
    private int COST_NO_DROP_COLLECT_COUPLE = 10000;
    private int COST_MULTIPLE_TRUCKS_PER_REQUEST = 10000;
    private int COST_NO_DEPOT_ENDPOINT = 10000;
    private int COST_ENDCAP_NOT_ZERO = 10000;

    public Evaluation() {
        revertMap = new HashMap<>();
        revertTotalDistance = totalDistance;
        revertBetter = isBetterSolution;
        revertFeasable = isFeasable;

        for (Truck t : trucksList)
            evaluate(t);
    }

    public void deltaEvaluate(Truck... trucks) {
        isFeasable = true;
        isBetterSolution = false;
        revertTotalDistance = totalDistance;

        for (Truck truck : trucks) {
            revertMap.put(truck, distanceMapping.get(truck));
            totalDistance -= distanceMapping.get(truck);
            evaluate(truck);
        }
    }

    public void revert(){
        totalDistance = revertTotalDistance;
        isFeasable = revertFeasable;
        isBetterSolution = revertBetter;

        for(Truck t: revertMap.keySet()){
            distanceMapping.put(t, revertMap.get(t));
        }
        revertMap = new HashMap<>();

    }

    private void evaluate(Truck t){
        //Calculate the total distance
        int afstand = t.getTotaleAfstandTruck();
        totalDistance += afstand;
        distanceMapping.put(t, afstand);

        //Een boete van 10000 indien een truck over zijn rijlimiet gaat
        if (t.getTotaleTijdGereden() > t.getTRUCK_WORKING_TIME()) {
            //System.out.println("over tijdslimiet infeasible");
            weight += COST_DRIVE_LIMIT;
            isFeasable = false;
        }

        //Kijken of truck over zijn capaciteit gaat, zoja -> Straf van 10.000 per keer
        int tempCapacity = 0;
        //  System.out.println(t.getRoute().size());
        for (Request r : t.getRoute()) {
            //  r.print();
            if (r.isDrop()) {
                //System.out.println("r is drop");
                //Voor capaciteit te checken
                if (r.getMachine() == null) {
                    //System.out.println("huhn");
                }
                else if (r.getMachine().getMachineType() == null) {
                    //System.out.println("uhuun");

                }
                tempCapacity -= r.getMachine().getMachineType().getVolume();


                //Altijd kijken of de pickup hiervan ook aanwezig is!
                if (noPickUp(t, r)) {
                    weight += COST_NO_DROP_COLLECT_COUPLE;
                    //System.out.println("pickup niet aanwezig infeasible");
                    isFeasable = false;
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
                    isFeasable = false;
                }
            }


            if (tempCapacity > t.getTRUCK_CAPACITY()) {
                weight += COST_CAPACITY;
                //System.out.println("capacity infeasible");
                isFeasable = false;
            }

            //Elke request wordt maar gedaan door 1 enkele truck, anders +10000
            if (doubleRequests(r, trucksList)) {
                weight += COST_MULTIPLE_TRUCKS_PER_REQUEST;
                //System.out.println("meerdere trucks aan zelfde request infeasible");
                isFeasable = false;
            }
        }

        if (t.getRoute().size() > 0) {
            if (!t.getRoute().get(t.getRoute().size() - 1).getLocation().equals(t.getEindlocatie())) {
                weight += COST_NO_DEPOT_ENDPOINT;
                //System.out.println("geen depot op einde infeasible");
                isFeasable = false;
            }
        }

        //op einde is capaciteit ook weer 0
        if (tempCapacity != 0) {
            weight += COST_ENDCAP_NOT_ZERO;
            //System.out.println("eindcapaciteit niet nul infeasible");
            isFeasable = false;
        }
    }


    public boolean doubleRequests(Request r, List<Truck> trucks) {
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

    public boolean noPickUp(Truck t, Request r) {
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

    public boolean noDrop(Truck t, Request r) {
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

    public boolean isBetterSolution() {
        return isBetterSolution;
    }

    public void setBetterSolution(boolean betterSolution) {
        isBetterSolution = betterSolution;
    }
}
