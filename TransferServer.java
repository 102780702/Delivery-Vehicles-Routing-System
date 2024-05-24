import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.google.gson.Gson;
import fi.iki.elonen.NanoHTTPD;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// need varibles and set method to store output data, remember to recompile

public class TransferServer extends NanoHTTPD 
{

    public TransferServer(int port) 
    {
        super(port);
    }

    @Override
    public Response serve(IHTTPSession session) {
        File directory = new File("../Delivery-Vehicles-Routing-System"); 
        File[] files = directory.listFiles();

        int maxRouteNumber = -1;
        Pattern pattern = Pattern.compile("DA(\\d+)_Route(\\d+)\\.txt");
        for (File file : files) {
            if (file.isFile() && file.getName().endsWith(".txt")) {
                // Check if the file matches the pattern
                Matcher matcher = pattern.matcher(file.getName());
                if (matcher.matches()) {
                    // Extract route number from the filename
                    int routeNumber = Integer.parseInt(matcher.group(2));

                    // Update maxRouteNumber and fileWithMaxRoute if applicable
                    if (routeNumber > maxRouteNumber) {
                        maxRouteNumber = routeNumber;
                    }
                }
            }
        }

        List<Map<String, List<List<Coordinate>>>> dataList = new ArrayList<>();

        for (int RouteIndex = 1; RouteIndex <= maxRouteNumber; RouteIndex++) {
        Map<String, List<List<Coordinate>>> map = new HashMap<>();

        File directory2 = new File("../Delivery-Vehicles-Routing-System"); 
        File[] listOfFiles = directory2.listFiles();

        for (File file : listOfFiles) {
            if (file.isFile() && file.getName().contains("_Route" + RouteIndex) && file.getName().endsWith(".txt")) {
                String fileName = file.getName();
                int daNumber;
                int routeNumber; 


                String fileNameWithoutExtension = fileName.substring(0, fileName.lastIndexOf("."));
                int routeIndex = fileNameWithoutExtension.indexOf("Route");
                if (routeIndex != -1) {
                    int startIndex = routeIndex + 5;
                    int endIndex = fileNameWithoutExtension.length();
                    String routeNumberStr = fileNameWithoutExtension.substring(startIndex, endIndex);
                    if (routeNumberStr.matches("\\d+")) {
                        routeNumber = Integer.parseInt(routeNumberStr);
                        daNumber = Integer.parseInt(fileName.substring(2, 3));
                        String areaKey = "area1" + (char) ('A' + daNumber - 1);
                        String coordinateKey = "coordinate1" + (char) ('A' + daNumber - 1);
                        String superareaKey = "Area_" + (char) ('A' + daNumber - 1);
            
                        switch (daNumber) {
                            case 1:
                                List<List<Coordinate>> areaData = new ArrayList<>();
                                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                                    String line;
                                    List<Coordinate> coordinateData = new ArrayList<>();
                                    while ((line = reader.readLine()) != null) {
                                        String[] parts = line.split(",");
                                        double latitude = Double.parseDouble(parts[0]);
                                        double longitude = Double.parseDouble(parts[1]);
                                        coordinateData.add(new Coordinate(latitude, longitude));
                                    }
                                    areaData.add(coordinateData);
                                    map.put(superareaKey, areaData);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                break;
                            case 2:
                                List<List<Coordinate>> areaData2 = new ArrayList<>();
                                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                                    String line;
                                    List<Coordinate> coordinateData2 = new ArrayList<>();
                                    while ((line = reader.readLine()) != null) {
                                        String[] parts = line.split(",");
                                        double latitude = Double.parseDouble(parts[0]);
                                        double longitude = Double.parseDouble(parts[1]);
                                        coordinateData2.add(new Coordinate(latitude, longitude));
                                    }
                                    areaData2.add(coordinateData2);
                                    map.put(superareaKey, areaData2);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                break;
                            case 3:
                                List<List<Coordinate>> areaData3 = new ArrayList<>();
                                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                                    String line;
                                    List<Coordinate> coordinateData3 = new ArrayList<>();
                                    while ((line = reader.readLine()) != null) {
                                        String[] parts = line.split(",");
                                        double latitude = Double.parseDouble(parts[0]);
                                        double longitude = Double.parseDouble(parts[1]);
                                        coordinateData3.add(new Coordinate(latitude, longitude));
                                    }
                                    areaData3.add(coordinateData3);
                                    map.put(superareaKey, areaData3);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                break;
                            case 4:
                                List<List<Coordinate>> areaData4 = new ArrayList<>();
                                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                                    String line;
                                    List<Coordinate> coordinateData4 = new ArrayList<>();
                                    while ((line = reader.readLine()) != null) {
                                        String[] parts = line.split(",");
                                        double latitude = Double.parseDouble(parts[0]);
                                        double longitude = Double.parseDouble(parts[1]);
                                        coordinateData4.add(new Coordinate(latitude, longitude));
                                    }
                                    areaData4.add(coordinateData4);
                                    map.put(superareaKey, areaData4);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                break;
                            // Add more cases for other DA values if needed
                        }
                        System.out.println("Content of dataList: " + dataList);
                        System.err.println("File name does contain 'Route' substring: " + fileName);
                        System.err.println("File name No extension: " + fileNameWithoutExtension);
                        System.err.println("'Route' index: " + routeIndex);
                        System.err.println("'Start' index: " + startIndex);
                        System.err.println("'End' index: " + endIndex);
                        System.err.println("Route number string: " + routeNumberStr);
                        System.err.println("Route number: " + routeNumber);
                        System.err.println("DA number: " + daNumber);
                        System.err.println("area key: " + areaKey);
                        System.err.println("coordinate key: " + coordinateKey);
                        System.err.println("area: " + superareaKey);
                    } else {
                        // Handle invalid file name format
                        System.err.println("Invalid file name format: " + fileName);
                        continue;
                    }
                } else {
                    // Handle file name without "Route" substring
                    System.err.println("File name does not contain 'Route' substring: " + fileName);
                    continue;
                }
            }
        }
        dataList.add(map);
        }
        Gson gson = new Gson();
        String data = gson.toJson(dataList);

        Response response = newFixedLengthResponse(Response.Status.OK, "application/json", data);
        response.addHeader("Access-Control-Allow-Origin", "*");
        return response;
    }

    public static void main(String[] args) 
    {
        int port = 8080;
        try 
        {
            TransferServer server = new TransferServer(port);
            server.start();
            System.out.println("Server started on port " + port);
            // Keep the server running until interrupted
            while (true) 
            {
                Thread.sleep(1000); // Sleep for a while to avoid high CPU usage
            }
        } 
        catch (IOException | InterruptedException e) 
        {
            e.printStackTrace();
        }
    }
}