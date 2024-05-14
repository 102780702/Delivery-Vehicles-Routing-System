/* 
Source:
Traveling Salesman Problem with Genetic Algorithms in Java
https://github.com/Mentathiel/StackAbuseGeneticTravelingSalesman/blob/master/src/SalesmanGenome.java
https://stackabuse.com/traveling-salesman-problem-with-genetic-algorithms-in-java/
 */

import java.util.*;

public class Main {

    public static void printTravelPrices(int[][] travelPrices, int numberOfCities){
        for(int i = 0; i<numberOfCities; i++){
            for(int j=0; j<numberOfCities; j++){
                System.out.print(travelPrices[i][j]);
                if(travelPrices[i][j]/10 == 0)
                    System.out.print("  ");
                else
                    System.out.print(' ');
            }
            System.out.println("");
        }
    }

    public static void main(String[] args) {
        List<Integer> intList = new ArrayList<>();
        int numberOfCities = 0;
        intList.add(5);
        intList.add(10);

        for (Integer integer : intList) {
            numberOfCities = integer;
            int[][] travelPrices = new int[numberOfCities][numberOfCities];
            for(int i = 0; i<numberOfCities; i++){
                for(int j=0; j<=i; j++){
                    Random rand = new Random();
                    if(i==j)
                        travelPrices[i][j] = 0;
                    else {
                        travelPrices[i][j] = rand.nextInt(100);
                        travelPrices[j][i] = travelPrices[i][j];
                    }
                }
            }

            printTravelPrices(travelPrices,numberOfCities);

            UberSalesmensch geneticAlgorithm = new UberSalesmensch(numberOfCities, SelectionType.ROULETTE, travelPrices, 0, 0);
            SalesmanGenome result = geneticAlgorithm.optimize();
            System.out.println(result);
        }

    }
}

