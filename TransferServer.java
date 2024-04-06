import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.google.gson.Gson;
import fi.iki.elonen.NanoHTTPD;
import java.io.IOException;


// need varibles, set method to store output data, remember to recompile

public class TransferServer extends NanoHTTPD {

    public TransferServer(int port) {
        super(port);
    }

    @Override
    public Response serve(IHTTPSession session) {
        List<Map<String, Object>> dataList = new ArrayList<>();

        Map<String, Object> map1 = new HashMap<>();
        map1.put("name", "John");
        map1.put("age", 30);
        dataList.add(map1);

        Map<String, Object> map2 = new HashMap<>();
        map2.put("name", "Alice");
        map2.put("age", 25);
        dataList.add(map2);

        Gson gson = new Gson();
        String jsonArray = gson.toJson(dataList);

        Response response = newFixedLengthResponse(Response.Status.OK, "application/json", jsonArray);
        response.addHeader("Access-Control-Allow-Origin", "*");
        return response;
    }


    // @Override
    // public Response serve(IHTTPSession session) {
    //     List<Map<String, Object>> dataList = new ArrayList<>();

    //     Map<String, Object> map1 = new HashMap<>();
    //     map1.put("name", "John");
    //     map1.put("age", 30);
    //     dataList.add(map1);

    //     Map<String, Object> map2 = new HashMap<>();
    //     map2.put("name", "Alice");
    //     map2.put("age", 25);
    //     dataList.add(map2);

    //     Gson gson = new Gson();
    //     String jsonArray = gson.toJson(dataList);

    //     return newFixedLengthResponse(Response.Status.OK, "application/json", jsonArray);
    // }

    // public static void main(String[] args) {
    //     int port = 8080;
    //     try {
    //         TransferServer server = new TransferServer(port);
    //         server.start();
    //         System.out.println("Server started on port " + port);
    //     } catch (IOException e) {
    //         e.printStackTrace();
    //     }
    // }
    public static void main(String[] args) {
        int port = 8080;
        try {
            TransferServer server = new TransferServer(port);
            server.start();
            System.out.println("Server started on port " + port);
            // Keep the server running until interrupted
            while (true) {
                Thread.sleep(1000); // Sleep for a while to avoid high CPU usage
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}