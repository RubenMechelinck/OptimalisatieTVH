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
    //overload van alle trucks (som van tijd dat over toegelaten is) als infeasable is
    private int bestInfeasable = Integer.MAX_VALUE;
    private List<Truck> bestTrucksList;
    //if false => unfeasable word aanvaard als beter is, if true => enkel feasable oplossingen
    private boolean switchToFeasable = false;

    private Evaluation lastEvaluation;

    //altijd eerst een volledige evaluatie doen voordat delta doet!!!
    public Evaluation evaluate(Truck... trucks) {

        if(trucks.length != 0){
            //lastEvaluation.deltaEvaluate(trucks);
            lastEvaluation= new Evaluation();
        }
        else {
            //Zo kan je dan aan de weight en de afstand
            lastEvaluation= new Evaluation();
            //System.out.println("total distance: " + result.getTotalDistance());
            //System.out.println("total weight: " + result.getWeight());
            //System.out.println("feasable: " + result.isFeasable());
        }

        //if feasable en betere afstand => in best steken
        if (lastEvaluation.isReallyFeasable() && lastEvaluation.getTotalDistance() < bestCost) {
            //eenmaal een feasable oplossing gevonden => altijd verder zoeken op feasable pad
            switchToFeasable = true;
            bestCost = lastEvaluation.getTotalDistance();
            bestTrucksList = new ArrayList<>();
            for (Truck truck : trucksList)
                bestTrucksList.add(new Truck(truck));
            return lastEvaluation;
        }
        //if niet feasable maar wel minder infeasable afstand => ook aanvaarden maar niet als best
        else if(!switchToFeasable && lastEvaluation.isFeasable() && lastEvaluation.getInfeasableOverload() < bestInfeasable){
            bestInfeasable = lastEvaluation.getInfeasableOverload();
            return lastEvaluation;
        }

        //if niet feasable en niet minder unfeasable afstand => reject
        else{
            //System.out.println(lastEvaluation.getTotalDistance());
            //lastEvaluation.revert();
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
