package objects;

import Evaluation.Evaluation;

import java.util.ArrayList;
import java.util.List;

import static main.Main.trucksList;

public class AnnealingSolution {
    private int energy = Integer.MAX_VALUE;;
    private List<Truck> truckArrayList = new ArrayList<>();
    private Evaluation lastEvaluation;

    public Evaluation getEvaluation() {
        return lastEvaluation;
    }

    public void setEvaluation(Evaluation evaluation) {
        this.lastEvaluation = evaluation;
    }

    public Evaluation evaluate(Truck... trucks) {

        if(trucks.length != 0){
            lastEvaluation.deltaEvaluate(trucks);
            //lastEvaluation= new Evaluation();
        }
        else {
            //Zo kan je dan aan de weight en de afstand
            lastEvaluation= new Evaluation();
            //System.out.println("total distance: " + result.getTotalDistance());
            //System.out.println("total weight: " + result.getWeight());
            //System.out.println("feasable: " + result.isFeasable());
        }
        //if feasable en betere afstand => in best steken
        if (lastEvaluation.isFeasable()) {
            //eenmaal een feasable oplossing gevonden => altijd verder zoeken op feasable pad

            energy = lastEvaluation.getTotalDistance();
            truckArrayList = new ArrayList<>();
            for (Truck truck : trucksList)
                truckArrayList.add(new Truck(truck));
            return lastEvaluation;
        } else{
            //System.out.println(lastEvaluation.getTotalDistance());
            lastEvaluation.revert();
            return null;
        }
    }

    public int getEnergy() {
        return energy;
    }

    public void setEnergy(int energy) {
        this.energy = energy;
    }

    public List<Truck> getTrucks() {

        return truckArrayList;
    }

    public void setTrucks(List<Truck> trucks) {
        this.truckArrayList = trucks;
    }
}
