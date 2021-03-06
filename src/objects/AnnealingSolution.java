package objects;

import Evaluation.Evaluation;

import java.util.ArrayList;
import java.util.List;

import static main.Main.random;
import static main.Main.solution;
import static main.Main.trucksList;

public class AnnealingSolution {

    private int lastEnergy = Integer.MAX_VALUE;
    private Evaluation lastEvaluation = new Evaluation(); //new Evaluation(solution.getLastEvaluation());

    public Evaluation evaluate(double T, double alfa, Truck... trucks) {

        lastEvaluation.deltaEvaluate(trucks);

        int deltaE = lastEvaluation.getTotalDistance() - lastEnergy;
        if (lastEvaluation.isReallyFeasable() && lastEvaluation.isFeasable() && (deltaE < 0 || Math.exp(-alfa*deltaE/T) > random.nextDouble())) {
            lastEnergy = lastEvaluation.getTotalDistance();

            if(lastEnergy < solution.getBestCost()) {
                solution.setBestCost(lastEnergy);
                solution.setLastEvaluation(new Evaluation(lastEvaluation));
                List<Truck> tmp = new ArrayList<>();
                for (Truck truck : trucksList) {
                    tmp.add(new Truck(truck));
                }
                solution.setBestTrucksList(tmp);
            }

            return lastEvaluation;
        }
        else {
            lastEvaluation.revert();
            return null;
        }
    }
}
