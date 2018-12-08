package main;

import heuristiek_impl.Heuristieken;
import objects.*;
import utils.FileUtils;

import java.util.ArrayList;
import java.util.List;

public class Main {

    private static final String inputFilename = "tvh_problem_4.txt";
    private static final String outputFilename = "tvh_problem_4_own_solution.txt";

    //voorlopig opslaan als classe later mss niet nodig?

    //lijst van alle locaties (depot + klanten)
    public static List<Location> locationList = new ArrayList<>();
    //lijst van locaties van depots
    public static List<Depot> depots = new ArrayList<>();
    public static List<Truck> trucksList = new ArrayList<>();
    public static List<MachineType> machineTypeList = new ArrayList<>();
    public static List<Machine> machineList = new ArrayList<>();
    public static List<Request> requestList = new ArrayList<>();
    public static int[][] timeMatrix = new int[0][0];
    public static int[][] distanceMatrix = new int[0][0];
    public static Solution solution;
    public static int Tmax=4000;

    public static void main(String[] args) {

        //input file inlezen
        FileUtils.readFromFile(inputFilename);

        //object dat huidige en beste oplossing bijhoud
        solution = new Solution();

        //constructive heuristiek uitvoeren
        Heuristieken.constructieveHeuristiek();
        solution.evaluate();
/*
        for(Truck t: trucksList)
            t.setSize();

        for(Truck truck: trucksList){
            int count = 0;
            for(Request request: truck.getRoute()){
                if(request.getPair() == null){
                    count++;
                }
            }
            System.out.println(count);
        }
*/
        Heuristieken.perturbatieveHeuristiek();

        //schrijf output
        FileUtils.writeOutputFile(outputFilename, inputFilename, solution);



    }

}
