package main;

import Evaluation.Evaluation;
import utils.FileUtils;
import heuristiek_impl.Heuristieken;
import objects.*;

import java.util.ArrayList;
import java.util.List;

public class Main {

    private static final String inputFilename = "tvh_problem_3.txt";
    private static final String outputFilename = "tvh_problem_3_own_solution.txt";

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
    public static Evaluation result;

    public static void main(String[] args) {

        //input file inlezen
        FileUtils.readFromFile(inputFilename);

        //constructive heuristiek uitvoeren
        Heuristieken.constructieveHeuristiek();

        //Zo kan je dan aan de weight en de afstand
        result = new Evaluation(trucksList);
        System.out.println("total distance: " + result.getTotalDistance());
        System.out.println("total weight: " + result.getWeight());
        System.out.println("feasable: " + result.isFeasable());

        //perturbative heuristiek uitvoeren
        //Heuristieken.perturbatieveHeuristiek(); //ofzo iets


        //schrijf output
        FileUtils.writeOutputFile(outputFilename, inputFilename);



    }

}
