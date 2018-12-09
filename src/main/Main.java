package main;

import heuristiek_impl.Heuristieken;
import objects.*;
import utils.FileUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static utils.Utils.stillRemainingTime;

public class Main {

    private static String inputFilename;// = "tvh_problem_8.txt";
    private static String outputFilename;// = "tvh_problem_8_own_solution.txt";
    public static long seed;
    public static long time;
    public static long startTimeMillis;

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
    public static Random random;
    public static int Tmax = 400;

    public static void main(String[] args) {
        startTimeMillis = System.currentTimeMillis();

        if(!parseArgs(args))
            return;

        //input file inlezen
        FileUtils.readFromFile(inputFilename);

        boolean restart = true;

        while(restart && stillRemainingTime()){
            System.out.println("Restart contructive heuristiek.");

            //object dat huidige en beste oplossing bijhoud
            solution = new Solution();

            //constructive heuristiek uitvoeren
            Heuristieken.constructieveHeuristiek();
            solution.evaluate();

            restart = Heuristieken.perturbatieveHeuristiek();

            //nieuwe seed zodat niet weer in zelfde probleem komt
            seed++;
        }

        //schrijf output
        FileUtils.writeOutputFile(outputFilename, inputFilename, solution);
        System.out.println("Program finished.");

    }

    private static boolean parseArgs(String[] args){
        try {
            String[] args0 = args[0].split("--problem=");
            String[] args1 = args[1].split("--solution=");
            String[] args2 = args[2].split("--seed=");
            String[] args3 = args[3].split("--time=");

            if (args0.length == 2 && args1.length == 2 && args2.length == 2 && args3.length == 2) {
                inputFilename = args0[1];
                outputFilename = args1[1];
                seed = Long.parseLong(args2[1]);
                time = Long.parseLong(args3[1]) * 1000;
                random = new Random(seed);
                return true;
            }
        }
        catch (Exception ignored) {
        }

        System.out.println("Wrong amount of parameters or wrong syntax. Exiting program.");
        return false;
    }

}
