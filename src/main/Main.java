package main;

import utils.FileUtils;
import heuristiek_impl.Heuristieken;
import objects.*;

import java.util.ArrayList;
import java.util.List;

public class Main {

    private static final String filename = "tvh_problem_3.txt";

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

    public static void main(String[] args) {

        //input file inlezen
        FileUtils.readFromFile(filename);

        //constructive heuristiek uitvoeren
        Heuristieken.contructieveHeuristiek();

        //perturbative heuristiek uitvoeren
        //Heuristieken.perturbativeHeuristiek(); //ofzo iets


        //schrijf output




    }

}
