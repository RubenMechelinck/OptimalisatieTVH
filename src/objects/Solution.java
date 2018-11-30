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

    public Evaluation evaluate() {

        //Zo kan je dan aan de weight en de afstand
        Evaluation result = new Evaluation(trucksList);
        //System.out.println("total distance: " + result.getTotalDistance());
        //System.out.println("total weight: " + result.getWeight());
        //System.out.println("feasable: " + result.isFeasable());

        if(result.getTotalDistance() < bestCost){
            result.setBetterSolution(true);
            bestCost = result.getTotalDistance();
            bestTrucksList = new ArrayList<>();
            for(Truck truck: trucksList)
                bestTrucksList.add(new Truck(truck));
        }

        return result;
    }

    public List<Truck> getBestTrucksList() {
        return bestTrucksList;
    }

    public int getBestCost() {
        return bestCost;
    }
}
