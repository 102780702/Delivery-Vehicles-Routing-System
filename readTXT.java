import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class readTXT 
{
    private static int calculateDistance(Coordinate loc1, Coordinate loc2) {
        double lat1 = Math.toRadians(loc1.getLatitude());
        double lon1 = Math.toRadians(loc1.getLongitude());
        double lat2 = Math.toRadians(loc2.getLatitude());
        double lon2 = Math.toRadians(loc2.getLongitude());
        
        double dlon = lon2 - lon1;
        double dlat = lat2 - lat1;
        
        double a = Math.pow(Math.sin(dlat / 2), 2) + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(dlon / 2), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        // earth radius in meters
        double radius = 6371000; // 6371 km converted to meters
        
        return (int) Math.round(radius * c);
    }
    

    public static List<Integer> calculateDistances(List<Coordinate> locations) {
        List<Integer> distances = new ArrayList<>();
        int n = locations.size();
        
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                Coordinate loc1 = locations.get(i);
                Coordinate loc2 = locations.get(j);
                int distance = calculateDistance(loc1, loc2);
                distances.add(distance);
            }
        }
        
        return distances;
    }

    public static void printDistanceReference(List<Coordinate> locations, List<Integer> distances) {
        int n = locations.size();
        int index = 0;
        
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                Coordinate loc1 = locations.get(i);
                Coordinate loc2 = locations.get(j);
                System.out.println("[ " +loc1.getLatitude() + ", " + loc1.getLongitude() + " ] and [" +loc2.getLatitude() + ", " + loc2.getLongitude() + "]: " + distances.get(index).toString() + "m");
                index++;
            }
        }
    }

    public static void printTravelPrices(int[][] travelPrices, int numberOfCoordinates){
        for(int i = 0; i<numberOfCoordinates; i++){
            for(int j=0; j<numberOfCoordinates; j++){
                System.out.print(travelPrices[i][j]);
                if(travelPrices[i][j]/10 == 0)
                    System.out.print("  ");
                else
                    System.out.print(' ');
            }
            System.out.println("");
        }
    }

    public static List<Integer> convertSalesmanGenomeToIntList(SalesmanGenome genome) {
        List<Integer> integerSequence = new ArrayList<>();
        
        integerSequence.add(genome.getStartingCity());
        
        for (int gene : genome.getGenome()) {
            integerSequence.add(gene);
        }
        
        integerSequence.add(genome.getStartingCity());
        
        return integerSequence;
    }

    public static void main(String[] args) 
    {
        String fileName = "coordinates.txt";
        Map<Coordinate, Integer> coordinateInfoMap = readCoordinatesFromFile(fileName);

        // print out coordinates and info in terminal
        // for (Map.Entry<Coordinate, CoordinateInfo> entry : coordinateInfoMap.entrySet()) 
        // {
        //     System.out.println("Coordinate: " + entry.getKey());
        //     System.out.println("Capacity: " + entry.getValue().getCapacity());
        //     System.out.println("Weight: " + entry.getValue().getWeight());
        //     System.out.println();
        // }

        List<Coordinate> coordinateList = new ArrayList<>(coordinateInfoMap.keySet());
        Coordinate centerWarehouse = new Coordinate(1.532302, 110.357173);
        coordinateList.add(0, centerWarehouse);
        
        // for (Coordinate coordinate : coordinateList) {
        //     System.out.println("Coordinate: " + coordinate.getLatitude());
        // }

        List<Integer> distances = calculateDistances(coordinateList);
        printDistanceReference(coordinateList, distances);

        // int count = 1;
        // for (int distance : distances) {
        //     System.out.println("Distance " + count + ": " + distance + " km");
        //     count++;
        // }

        int numberOfCoordinates = coordinateList.size();
        int[][] travelPrices = new int[numberOfCoordinates][numberOfCoordinates];
        int index = 0;

        for (int i = 0; i < numberOfCoordinates; i++) {
            for (int j = 0; j < numberOfCoordinates; j++) {
                if (i != j) {
                    if (travelPrices[i][j] == 0) {
                        travelPrices[i][j] = distances.get(index);
                        travelPrices[j][i] = travelPrices[i][j];
                        index ++;
                    }
                }
            }
        }

        printTravelPrices(travelPrices,numberOfCoordinates); // print matrix

        UberSalesmensch geneticAlgorithm = new UberSalesmensch(numberOfCoordinates, SelectionType.ROULETTE, travelPrices, 0, 0);
        SalesmanGenome result = geneticAlgorithm.optimize();
        System.out.println(result);
        List<Integer> resultList = convertSalesmanGenomeToIntList(result);
        System.out.println(resultList);

        List<Coordinate> shortestPath = new ArrayList<>();        ;

        for (int i: resultList) {
            shortestPath.add(coordinateList.get(i));
        }

        // Map<Coordinate, CoordinateInfo> areaA = new HashMap<>();
        // Map<Coordinate, CoordinateInfo> areaB = new HashMap<>();
        // Map<Coordinate, CoordinateInfo> areaC = new HashMap<>();
        // Map<Coordinate, CoordinateInfo> areaD = new HashMap<>();

        // Coordinate centerWarehouse = new Coordinate(1.532302, 110.357173);

        // for (Map.Entry<Coordinate, CoordinateInfo> entry : coordinateInfoMap.entrySet())
        // {
        //     if(entry.getKey().getLatitude() > centerWarehouse.getLatitude() && entry.getKey().getLongitude() < centerWarehouse.getLongitude())
        //     {
        //         areaA.put(entry.getKey(), entry.getValue());
        //     }
        //     else if(entry.getKey().getLatitude() > centerWarehouse.getLatitude() && entry.getKey().getLongitude() > centerWarehouse.getLongitude())
        //     {
        //         areaB.put(entry.getKey(), entry.getValue());
        //     }
        //     else if(entry.getKey().getLatitude() < centerWarehouse.getLatitude() && entry.getKey().getLongitude() < centerWarehouse.getLongitude())
        //     {
        //         areaC.put(entry.getKey(), entry.getValue());
        //     }
        //     else if(entry.getKey().getLatitude() < centerWarehouse.getLatitude() && entry.getKey().getLongitude() > centerWarehouse.getLongitude())
        //     {
        //         areaD.put(entry.getKey(), entry.getValue());
        //     }
        // }
        
        // areaA.forEach((coordinate, info) -> {
        //     System.out.println("Coordinate: " + coordinate + ", Capacity: " + info.getCapacity() + ", Weight: " + info.getWeight());
        // });
        // areaB.forEach((coordinate, info) -> {
        //     System.out.println("Coordinate: " + coordinate + ", Capacity: " + info.getCapacity() + ", Weight: " + info.getWeight());
        // });
        // areaC.forEach((coordinate, info) -> {
        //     System.out.println("Coordinate: " + coordinate + ", Capacity: " + info.getCapacity() + ", Weight: " + info.getWeight());
        // });
        // areaD.forEach((coordinate, info) -> {
        //     System.out.println("Coordinate: " + coordinate + ", Capacity: " + info.getCapacity() + ", Weight: " + info.getWeight());
        // });
    }

    // read file function
    public static Map<Coordinate, Integer> readCoordinatesFromFile(String fileName) 
    {
        Map<Coordinate, Integer> coordinateInfoMap = new HashMap<>();
        BufferedReader reader;
        try 
        {
            reader = new BufferedReader(new FileReader(fileName));
            String line = reader.readLine();
            while (line != null) 
            {
                String[] parts = line.split(",");
                if (parts.length == 3) 
                {
                    double latitude = Double.parseDouble(parts[0]);
                    double longitude = Double.parseDouble(parts[1]);
                    int capacity = Integer.parseInt(parts[2]);
                    Coordinate coordinate = new Coordinate(latitude, longitude);
                    coordinateInfoMap.put(coordinate, capacity);
                } 
                else 
                {
                    System.err.println("Invalid format: " + line);
                }
                line = reader.readLine();
            }
            reader.close();
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }
        return coordinateInfoMap;
    }
}
