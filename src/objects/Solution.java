package objects;

import Evaluation.Evaluation;

import java.util.ArrayList;
import java.util.List;

import static main.Main.trucksList;

/**
 * Created by ruben on 27/11/18.
 */
public class Solution {
    private int bestCost = Integer.MAX_VALUE;
    private List<Truck> bestTrucksList;

    private Evaluation lastEvaluation;

    //altijd eerst een volledige evaluatie doen voordat delta doet!!!
    public Evaluation evaluate(Truck... trucks) {

        if(trucks.length != 0){
            lastEvaluation.deltaEvaluate(trucks);
        }
        else {
            //Zo kan je dan aan de weight en de afstand
            lastEvaluation= new Evaluation();
            //System.out.println("total distance: " + result.getTotalDistance());
            //System.out.println("total weight: " + result.getWeight());
            //System.out.println("feasable: " + result.isFeasable());
        }

        if (lastEvaluation.isFeasable() && lastEvaluation.getTotalDistance() < bestCost) {
            lastEvaluation.setBetterSolution(true);
            bestCost = lastEvaluation.getTotalDistance();
            bestTrucksList = new ArrayList<>();
            for (Truck truck : trucksList)
                bestTrucksList.add(new Truck(truck));
            return lastEvaluation;
        }
        else{
            lastEvaluation.revert();
            return null;
        }
    }

    public List<Truck> getBestTrucksList() {
        return bestTrucksList;
    }

    public int getBestCost() {
        return bestCost;
    }
}
