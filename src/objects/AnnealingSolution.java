package objects;

import Evaluation.Evaluation;

import java.util.ArrayList;
import java.util.List;

import static main.Main.solution;
import static main.Main.trucksList;

public class AnnealingSolution {
    private int bestEnergy = Integer.MAX_VALUE;
    private List<Truck> bestTrucksList;
    private Evaluation bestEvaluation;

    private int lastEnergy = Integer.MAX_VALUE;
    private Evaluation lastEvaluation = new Evaluation(solution.getLastEvaluation());

    public Evaluation evaluate(double T, Truck... trucks) {

        lastEvaluation.deltaEvaluate(trucks);

        int deltaE = lastEvaluation.getTotalDistance() - lastEnergy;
        if (lastEvaluation.isFeasable() && (deltaE < 0 || Math.exp(-deltaE/T) > Math.random())) {
            lastEnergy = lastEvaluation.getTotalDistance();

            if(lastEnergy < bestEnergy) {
                bestEnergy = lastEnergy;
                bestEvaluation = new Evaluation(lastEvaluation);
                bestTrucksList = new ArrayList<>();
                for (Truck truck : trucksList) {
                    bestTrucksList.add(new Truck(truck));
                }
            }

            return lastEvaluation;
        }
        else {
            lastEvaluation.revert();
            return null;
        }
    }

    public int getBestEnergy() {
        return bestEnergy;
    }

    public List<Truck> getBestTrucksList() {
        return bestTrucksList;
    }

    public Evaluation getBestEvaluation() {
        return bestEvaluation;
    }

}
