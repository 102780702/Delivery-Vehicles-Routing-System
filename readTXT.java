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
        // for (Map.Entry<Coordinate, CoordinateInfo> entry : coordinateInfoMap.entrySet()) 
        // {
        //     System.out.println("Coordinate: " + entry.getKey());
        //     System.out.println("Capacity: " + entry.getValue().getCapacity());
        //     System.out.println("Weight: " + entry.getValue().getWeight());
        //     System.out.println();
        // }

        Map<Coordinate, CoordinateInfo> areaA = new HashMap<>();
        Map<Coordinate, CoordinateInfo> areaB = new HashMap<>();
        Map<Coordinate, CoordinateInfo> areaC = new HashMap<>();
        Map<Coordinate, CoordinateInfo> areaD = new HashMap<>();

        Coordinate centerWarehouse = new Coordinate(1.532302, 110.357173);

        for (Map.Entry<Coordinate, CoordinateInfo> entry : coordinateInfoMap.entrySet())
        {
            if(entry.getKey().getLatitude() > centerWarehouse.getLatitude() && entry.getKey().getLongitude() < centerWarehouse.getLongitude())
            {
                areaA.put(entry.getKey(), entry.getValue());
            }
            else if(entry.getKey().getLatitude() > centerWarehouse.getLatitude() && entry.getKey().getLongitude() > centerWarehouse.getLongitude())
            {
                areaB.put(entry.getKey(), entry.getValue());
            }
            else if(entry.getKey().getLatitude() < centerWarehouse.getLatitude() && entry.getKey().getLongitude() < centerWarehouse.getLongitude())
            {
                areaC.put(entry.getKey(), entry.getValue());
            }
            else if(entry.getKey().getLatitude() < centerWarehouse.getLatitude() && entry.getKey().getLongitude() > centerWarehouse.getLongitude())
            {
                areaD.put(entry.getKey(), entry.getValue());
            }
        }
        
        areaA.forEach((coordinate, info) -> {
            System.out.println("Coordinate: " + coordinate + ", Capacity: " + info.getCapacity() + ", Weight: " + info.getWeight());
        });
        areaB.forEach((coordinate, info) -> {
            System.out.println("Coordinate: " + coordinate + ", Capacity: " + info.getCapacity() + ", Weight: " + info.getWeight());
        });
        areaC.forEach((coordinate, info) -> {
            System.out.println("Coordinate: " + coordinate + ", Capacity: " + info.getCapacity() + ", Weight: " + info.getWeight());
        });
        areaD.forEach((coordinate, info) -> {
            System.out.println("Coordinate: " + coordinate + ", Capacity: " + info.getCapacity() + ", Weight: " + info.getWeight());
        });

        // for (Map.Entry<Coordinate, CoordinateInfo> entry : areaA.entrySet()) {
        //     Coordinate coordinate = entry.getKey();
        //     CoordinateInfo info = entry.getValue();
        //     System.out.println("Coordinate: " + coordinate + ", Capacity: " + info.getCapacity() + ", Weight: " + info.getWeight());
        // }
        // for (Map.Entry<Coordinate, CoordinateInfo> entry : areaB.entrySet()) {
        //     Coordinate coordinate = entry.getKey();
        //     CoordinateInfo info = entry.getValue();
        //     System.out.println("Coordinate: " + coordinate + ", Capacity: " + info.getCapacity() + ", Weight: " + info.getWeight());
        // }
        // for (Map.Entry<Coordinate, CoordinateInfo> entry : areaC.entrySet()) {
        //     Coordinate coordinate = entry.getKey();
        //     CoordinateInfo info = entry.getValue();
        //     System.out.println("Coordinate: " + coordinate + ", Capacity: " + info.getCapacity() + ", Weight: " + info.getWeight());
        // }
        // for (Map.Entry<Coordinate, CoordinateInfo> entry : areaD.entrySet()) {
        //     Coordinate coordinate = entry.getKey();
        //     CoordinateInfo info = entry.getValue();
        //     System.out.println("Coordinate: " + coordinate + ", Capacity: " + info.getCapacity() + ", Weight: " + info.getWeight());
        // }
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
