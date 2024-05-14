import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.google.gson.Gson;
import fi.iki.elonen.NanoHTTPD;
import java.io.IOException;

// need varibles and set method to store output data, remember to recompile

public class TransferServer extends NanoHTTPD 
{

    public TransferServer(int port) 
    {
        super(port);
    }

    @Override
    public Response serve(IHTTPSession session) 
    {
        // Test data, algrorithm should format the data like this

        // Json list
        List<Map<String, List<List<Coordinate>>>> dataList = new ArrayList<>();

        // 1st batch delivery (ABCD)
        Map<String, List<List<Coordinate>>> map1 = new HashMap<>();

        List<List<Coordinate>> area1A = new ArrayList<>();
        List<List<Coordinate>> area1B = new ArrayList<>();
        List<List<Coordinate>> area1C = new ArrayList<>();
        List<List<Coordinate>> area1D = new ArrayList<>();
        
        List<Coordinate> coordinates1A = new ArrayList<>();
        List<Coordinate> coordinates1B = new ArrayList<>();
        List<Coordinate> coordinates1C = new ArrayList<>();
        List<Coordinate> coordinates1D = new ArrayList<>();

        coordinates1A.add(new Coordinate(1.532302,110.357173));
        coordinates1A.add(new Coordinate(1.535546,110.358202));
        coordinates1A.add(new Coordinate(1.520725,110.354431));

        coordinates1B.add(new Coordinate(1.532302,110.357173));
        coordinates1B.add(new Coordinate(1.526855,110.369588));
        coordinates1B.add(new Coordinate(1.511337,110.352239));

        coordinates1C.add(new Coordinate(1.532302,110.357173));
        coordinates1C.add(new Coordinate(1.588484,110.360216));
        coordinates1C.add(new Coordinate(1.46668,110.425148));

        coordinates1D.add(new Coordinate(1.532302,110.357173));
        coordinates1D.add(new Coordinate(1.543655,110.338852));
        coordinates1D.add(new Coordinate(1.550192,110.341737));

        area1A.add(coordinates1A);
        area1B.add(coordinates1B);
        area1C.add(coordinates1C);
        area1D.add(coordinates1D);

        map1.put("Area_A", area1A);
        map1.put("Area_B", area1B);
        map1.put("Area_C", area1C);
        map1.put("Area_D", area1D);


        // 2nd batch delivery (ABCD)
        Map<String, List<List<Coordinate>>> map2 = new HashMap<>();

        // Area A coordinate list
        List<List<Coordinate>> area2A = new ArrayList<>();

        // Area B coordinate list
        List<List<Coordinate>> area2B = new ArrayList<>();
        
        // Area A locations need to be deliver
        List<Coordinate> coordinates2A = new ArrayList<>();

        // Area B locations need to be deliver
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
        dataList.add(map1);
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