import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class readTXT 
{
    public static void main(String[] args) 
    {
        String fileName = "coordinates.txt";
        Map<Coordinate, CoordinateInfo> coordinateInfoMap = readCoordinatesFromFile(fileName);

        // print out coordinates and info in terminal
        for (Map.Entry<Coordinate, CoordinateInfo> entry : coordinateInfoMap.entrySet()) 
        {
            System.out.println("Coordinate: " + entry.getKey());
            System.out.println("Capacity: " + entry.getValue().getCapacity());
            System.out.println("Weight: " + entry.getValue().getWeight());
            System.out.println();
        }

        List<Coordinate> coordinateList = new ArrayList<>(coordinateInfoMap.keySet());
        List<Coordinate> areaA = new ArrayList<>();
        List<Coordinate> areaB = new ArrayList<>();
        List<Coordinate> areaC = new ArrayList<>();
        List<Coordinate> areaD = new ArrayList<>();

    
        Coordinate centerWarehouse = new Coordinate(1.532302, 110.357173);

        for (Coordinate coordinate : coordinateList) 
        {
            if(coordinate.getLatitude() > centerWarehouse.getLatitude() && coordinate.getLongitude() < centerWarehouse.getLongitude())
            {
                areaA.add(coordinate);
            }
            else if(coordinate.getLatitude() > centerWarehouse.getLatitude() && coordinate.getLongitude() > centerWarehouse.getLongitude())
            {
                areaB.add(coordinate);
            }
            else if(coordinate.getLatitude() < centerWarehouse.getLatitude() && coordinate.getLongitude() < centerWarehouse.getLongitude())
            {
                areaC.add(coordinate);
            }
            else if(coordinate.getLatitude() < centerWarehouse.getLatitude() && coordinate.getLongitude() > centerWarehouse.getLongitude())
            {
                areaD.add(coordinate);
            }
        }

        System.out.println("Area A:");
        for (Coordinate coordinate : areaA) {
            System.out.println("Latitude: " + coordinate.getLatitude() + ", Longitude: " + coordinate.getLongitude());
        }

        System.out.println("Area B:");
        for (Coordinate coordinate : areaB) {
            System.out.println("Latitude: " + coordinate.getLatitude() + ", Longitude: " + coordinate.getLongitude());
        }

        System.out.println("Area C:");
        for (Coordinate coordinate : areaC) {
            System.out.println("Latitude: " + coordinate.getLatitude() + ", Longitude: " + coordinate.getLongitude());
        }

        System.out.println("Area D:");
        for (Coordinate coordinate : areaD) {
            System.out.println("Latitude: " + coordinate.getLatitude() + ", Longitude: " + coordinate.getLongitude());
        }
    }

    // read file function
    public static Map<Coordinate, CoordinateInfo> readCoordinatesFromFile(String fileName) 
    {
        Map<Coordinate, CoordinateInfo> coordinateInfoMap = new HashMap<>();
        BufferedReader reader;
        try 
        {
            reader = new BufferedReader(new FileReader(fileName));
            String line = reader.readLine();
            while (line != null) 
            {
                String[] parts = line.split(",");
                if (parts.length == 4) 
                {
                    double latitude = Double.parseDouble(parts[0]);
                    double longitude = Double.parseDouble(parts[1]);
                    int capacity = Integer.parseInt(parts[2]);
                    int weight = Integer.parseInt(parts[3]);
                    Coordinate coordinate = new Coordinate(latitude, longitude);
                    CoordinateInfo coordinateInfo = new CoordinateInfo(capacity, weight);
                    coordinateInfoMap.put(coordinate, coordinateInfo);
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
