import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.google.gson.Gson;
import fi.iki.elonen.NanoHTTPD;
import java.io.IOException;

// need varibles and set method to store output data, remember to recompile

public class TransferServer extends NanoHTTPD 
{
    List<String> keyNames = Arrays.asList("Area_A", "Area_B", "Area_C", "Area_D");

    // Json list
    List<Map<String, List<List<Coordinate>>>> dataList = new ArrayList<>();

    private void createNewMap() 
    {
        Map<String, List<List<Coordinate>>> temp = new HashMap<>();
        List<Coordinate> areaA = new ArrayList<>();
        List<Coordinate> areaB = new ArrayList<>();
        List<Coordinate> areaC = new ArrayList<>();
        List<Coordinate> areaD = new ArrayList<>();
        List<List<Coordinate>> AreaA = new ArrayList<>();
        List<List<Coordinate>> AreaB = new ArrayList<>();
        List<List<Coordinate>> AreaC = new ArrayList<>();
        List<List<Coordinate>> AreaD = new ArrayList<>();
        List<List<List<Coordinate>>> areas = new ArrayList<>();

        AreaA.add(areaA);
        AreaB.add(areaB);
        AreaC.add(areaC);
        AreaD.add(areaD);

        areas.add(AreaA);
        areas.add(AreaB);
        areas.add(AreaC);
        areas.add(AreaD);
        
        for (int i = 0; i < areas.size(); i++ ) {
            temp.put(keyNames.get(i), areas.get(i));
        }
        
        dataList.add(temp);
    }

    public TransferServer(int port) 
    {
        super(port);
    }

    public void passDataToServer(List<List<Coordinate>> batchesOdShortestPath) 
    {
        if(dataList.isEmpty()) 
        {
            createNewMap();
        }
        
        int index = 0;
        boolean inserted = false;
        for (Map<String, List<List<Coordinate>>> map : dataList) 
        {
            List<List<Coordinate>> allLists = new ArrayList<>();
            
            for (List<List<Coordinate>> listOfLists : map.values()) {
                allLists.addAll(listOfLists);
            }

            boolean full = true;
            List<Coordinate> tempList = new ArrayList<>();
            for (List<Coordinate> list : allLists) 
            {
                if(list.isEmpty()) 
                {
                    if (inserted == true) 
                    {
                        full = true;
                        break;
                    }
                    else 
                    {
                        full = false;
                        tempList = list;
                        break;
                    }
                }
            }

            if(full == false) 
            {
                if (batchesOdShortestPath.get(index) != null) 
                {
                    tempList.addAll(batchesOdShortestPath.get(index));
                    index++;
                    inserted = true;
                }
            }
            else
            {
                for (int i = 0; i < dataList.size(); i++) 
                {
                    if (i == dataList.size() - 1) 
                    {
                        if(map == dataList.get(i))
                        {
                            createNewMap();
                        }
                    }
                }
                inserted = false;
            }
        }
    }

    @Override
    public Response serve(IHTTPSession session) 
    {
        // Test data, DAs should format the data like this

        //  2nd batch delivery (ABCD)
          Map<String, List<List<Coordinate>>> map2 = new HashMap<>();

         List<List<Coordinate>> area2A = new ArrayList<>();
         List<List<Coordinate>> area2B = new ArrayList<>();
        
         List<Coordinate> coordinates2A = new ArrayList<>();
         List<Coordinate> coordinates2B = new ArrayList<>();

        // C,D......

        // Area A
        coordinates2A.add(new Coordinate(1.532302,110.357173));
        coordinates2A.add(new Coordinate(1.487123,110.341599));
        coordinates2A.add(new Coordinate(1.507103,110.360874));

        // Area B
        coordinates2B.add(new Coordinate(1.532302,110.357173));
        coordinates2B.add(new Coordinate(1.555485,110.342379));
        coordinates2B.add(new Coordinate(1.588484,110.360216));

        area2A.add(coordinates2A);
        area2B.add(coordinates2B);

        map2.put("Area_A", area2A);
        map2.put("Area_B", area2B);

        // put batches into json list
        // dataList.add(map1);
        dataList.add(map2);

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