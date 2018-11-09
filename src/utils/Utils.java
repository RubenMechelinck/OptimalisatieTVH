package utils;

import objects.Location;

import static main.Main.distanceMatrix;

/**
 * Created by ruben on 9/11/18.
 */
public class Utils {

    //get distance from par:a to par:b
    public static int getDistance(Location a, Location b){
        //deze methode is de meest complexe methode in deze software!
        return distanceMatrix[a.getIndex()][b.getIndex()];
    }
}
