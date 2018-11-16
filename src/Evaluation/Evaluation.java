package Evaluation;

import objects.Request;
import objects.Truck;
import java.util.List;

/**
 * Created by Joran on 13/11/18.
 */
public class Evaluation {
    private int totalDistance=0;
    private int weight=0;
    private boolean isFeasable = true;

    private int COST_DRIVE_LIMIT = 10000;
    private int COST_CAPACITY = 10000;
    private int COST_NO_DROP_COLLECT_COUPLE = 10000;
    private int COST_MULTIPLE_TRUCKS_PER_REQUEST = 10000;
    private int COST_NO_DEPOT_ENDPOINT = 10000;
    private int COST_ENDCAP_NOT_ZERO = 10000;

    public Evaluation(List<Truck> trucks){

        for(Truck t: trucks){

            //Calculate the total distance
            totalDistance+=t.getTotaleAfstandTruck();

            //Een boete van 10000 indien een truck over zijn rijlimiet gaat
            if(t.getTotaleTijdGereden()>t.getTRUCK_WORKING_TIME()){
                weight+=COST_DRIVE_LIMIT;
                isFeasable = false;
            }

            //Kijken of truck over zijn capaciteit gaat, zoja -> Straf van 10.000 per keer
            int tempCapacity=0;

            for(Request r: t.getRoute()){
                if(r.isDrop()){
                    //Voor capaciteit te checken
                    tempCapacity-=r.getMachine().getMachineType().getVolume();

                    //Altijd kijken of de pickup hiervan ook aanwezig is!
                    if(noPickUp(t,r)){
                        weight+=COST_NO_DROP_COLLECT_COUPLE;
                        isFeasable = false;
                    }
                }
                else{
                    tempCapacity+=r.getMachine().getMachineType().getVolume();

                    //Altijd kijken of de drop van deze pickup ook aanwezig is!
                    if(noDrop(t,r)){
                        weight+=COST_NO_DROP_COLLECT_COUPLE;
                        isFeasable = false;
                    }
                }


                if(tempCapacity>t.getTRUCK_CAPACITY()){
                    weight+=COST_CAPACITY;
                    isFeasable = false;
                }

                //Elke request wordt maar gedaan door 1 enkele truck, anders +10000
                if(doubleRequests(r,trucks)){
                    weight+=COST_MULTIPLE_TRUCKS_PER_REQUEST;
                    isFeasable = false;
                }
            }

            if(t.getRoute().size()>0) {
                if (!t.getRoute().get(t.getRoute().size() - 1).getLocation().equals(t.getEindlocatie())) {
                    weight += COST_NO_DEPOT_ENDPOINT;
                    isFeasable = false;
                }
            }

            //op einde is capaciteit ook weer 0
            if(tempCapacity!=0){
                weight+=COST_ENDCAP_NOT_ZERO;
                isFeasable = false;
            }
        }
    }

    public boolean doubleRequests(Request r,List<Truck> trucks){
        int requests=0;
        for(Truck t:trucks){
            if(t.getRoute().contains(r)){
                requests++;
            }
        }
        if(requests>1){
            return true;
        }
        return false;
    }

    public boolean noPickUp(Truck t, Request r){
        boolean noPickUp=true;
        for(Request req:t.getRoute()){
            if(req.getMachine().getMachineId()==r.getMachine().getMachineId()){
                if(!req.isDrop()){
                    noPickUp=false;
                }
            }
        }
        return noPickUp;
    }

    public boolean noDrop(Truck t, Request r){
        boolean noDrop=true;
        for(Request req:t.getRoute()){
            if(req.getMachine().getMachineId()==r.getMachine().getMachineId()){
                if(req.isDrop()){
                    noDrop=false;
                }
            }
        }
        return noDrop;
    }

    public int getTotalDistance(){
        return totalDistance;
    }

    public int getWeight(){
        return weight;
    }

    public boolean isFeasable() {
        return isFeasable;
    }
}