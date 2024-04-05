import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class readTXT {
    public static void main(String[] args) 
    {
        String fileName = "coordinates.txt";
        List<Coordinate> coordinates = readCoordinatesFromFile(fileName);
        
        // print out coordinates in terminal 
        for (Coordinate coordinate : coordinates) 
        {
            System.out.println(coordinate);
        }
    }

    // read file function
    public static List<Coordinate> readCoordinatesFromFile(String fileName) 
    {
        List<Coordinate> coordinates = new ArrayList<>();
        BufferedReader reader;
        try 
        {
            reader = new BufferedReader(new FileReader(fileName));
            String line = reader.readLine();
            while (line != null) 
            {
                String[] parts = line.split(",");
                if (parts.length == 2) 
                {
                    double latitude = Double.parseDouble(parts[0]);
                    double longitude = Double.parseDouble(parts[1]);
                    coordinates.add(new Coordinate(latitude, longitude));
                } else 
                {
                    System.err.println("Invalid coordinate format: " + line);
                }
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) 
        {
            e.printStackTrace();
        }
        return coordinates;
    }
}

// coordinate class
class Coordinate 
{
    private double latitude;
    private double longitude;

    public Coordinate(double latitude, double longitude) 
    {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() 
    {
        return latitude;
    }

    public double getLongitude() 
    {
        return longitude;
    }

    @Override
    public String toString() 
    {
        return "Latitude: " + latitude + ", Longitude: " + longitude;
    }
}
