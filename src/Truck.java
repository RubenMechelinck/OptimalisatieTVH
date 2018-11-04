import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Truck {

    private int startlocatie;
    private int eindlocatie;
    private double totaleAfstandTruck;
    private LinkedList<Request> route;
    private List<Machine> machineList;

    public Truck(int startlocatie, int eindlocatie) {
        this.startlocatie = startlocatie;
        this.eindlocatie = eindlocatie;
        totaleAfstandTruck =0;
        route = new LinkedList<Request>();
        machineList = new ArrayList<Machine>();
    }

    public int getStartlocatie() {
        return startlocatie;
    }

    public void setStartlocatie(int startlocatie) {
        this.startlocatie = startlocatie;
    }

    public int getEindlocatie() {
        return eindlocatie;
    }

    public void setEindlocatie(int eindlocatie) {
        this.eindlocatie = eindlocatie;
    }

    public double getTotaleAfstandTruck() {
        return totaleAfstandTruck;
    }

    public void setTotaleAfstandTruck(double totaleAfstandTruck) {
        this.totaleAfstandTruck = totaleAfstandTruck;
    }

    public LinkedList<Request> getRoute() {
        return route;
    }

    public void setRoute(LinkedList<Request> route) {
        this.route = route;
    }

    public List<Machine> getMachineList() {
        return machineList;
    }

    public void setMachineList(List<Machine> machineList) {
        this.machineList = machineList;
    }

    public void print(){
        System.out.println("start: "+ startlocatie + " eind: "+eindlocatie );
    }
}
