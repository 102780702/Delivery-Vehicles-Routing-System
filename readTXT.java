import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
