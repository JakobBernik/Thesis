package thesis.testing.system.oldbMocks;

import com.google.gson.Gson;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import thesis.testing.utils.Command;
import thesis.testing.utils.OldbException;
import thesis.testing.utils.Transporter;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;

import static org.mockito.Matchers.any;

public class OldbApiClient implements OldbAPI {

    private final String URL_PATH = "http://localhost:4242/oldb";
    private HttpURLConnection http;

    private OldbApiClient oldbApiClientMock;

    public OldbApiClient() throws OldbException {
        oldbApiClientMock = Mockito.mock(OldbApiClient.class);

        //Mock connect method behavior. This also internally mocks the behaviour
        // of all other methods that use the connection established
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                URL url = new URL(URL_PATH);
                URLConnection con = url.openConnection();
                http = (HttpURLConnection) con;
                http.setRequestMethod("POST");
                http.setDoOutput(true);
                http.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                http.connect();
                System.out.println("client connected");
                return null;
            }
        }).when(oldbApiClientMock).connect();

        //mock disconnect method behaviour
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                http.disconnect();
                return null;
            }
        }).when(oldbApiClientMock).disconnect();

        //mock add data point method behaviour
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                //get data passsed  , pack it into transporter and send it
                oldbApiClientMock.connect();
                System.out.println("adding data point to online database...");
                DataPoint dataPoint = (DataPoint) invocation.getArguments()[0];
                    if (dataPoint.getValue() < 0){
                        throw new OldbException("Invalid data point.");
                    }
                String jsonData = new Gson().toJson(dataPoint);
                System.out.println("point: "+jsonData);
                final Transporter transporter = new Transporter(Command.STORE,jsonData);
                String requestData = new Gson().toJson(transporter);
                try(OutputStream os = http.getOutputStream()) {
                    os.write(requestData.getBytes());
                }
                // read result and return it
                if (http.getResponseCode() == 200) {
                    InputStream inputStream = http.getInputStream();
                    final Gson gson = new Gson();
                    final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    String response = null;
                    while ((response=reader.readLine())!=null) {
                        System.out.println(response);
                    }
                    oldbApiClientMock.disconnect();
                    return null;
                }
                oldbApiClientMock.disconnect();
                throw new OldbException("failed to add data point.");
            }
        }).when(oldbApiClientMock).addDataPoint(any(DataPoint.class));

        //Mock get data point method behaviour
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                String pointId = (String) invocation.getArguments()[0];
                oldbApiClientMock.connect();
                System.out.println("retrieving data point...");
                String jsonData = new Gson().toJson(pointId);
                System.out.println("pointId: "+jsonData);
                final Transporter transporter = new Transporter(Command.GET,jsonData);
                String requestData = new Gson().toJson(transporter);
                try(OutputStream os = http.getOutputStream()) {
                    os.write(requestData.getBytes());
                }
                // read result and return it
                if (http.getResponseCode() == 200) {
                    InputStream inputStream = http.getInputStream();
                    final Gson gson = new Gson();
                    final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    DataPoint point = gson.fromJson(reader, DataPoint.class);
                    oldbApiClientMock.disconnect();
                    return point;
                }
                oldbApiClientMock.disconnect();
                throw new OldbException("Requested data point do not exist.");
            }
        }).when(oldbApiClientMock).getDataPoint(any(String.class));

        //Mock delete data point method behaviour
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                String pointId = (String) invocation.getArguments()[0];
                oldbApiClientMock.connect();
                String jsonData = new Gson().toJson(pointId);
                System.out.println("pointId: "+jsonData);
                final Transporter transporter = new Transporter(Command.DELETE,jsonData);
                String requestData = new Gson().toJson(transporter);
                try(OutputStream os = http.getOutputStream()) {
                    os.write(requestData.getBytes());
                }
                // read result and return it
                if (http.getResponseCode() == 200) {
                    InputStream inputStream = http.getInputStream();
                    final Gson gson = new Gson();
                    final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    String response = null;
                    while ((response=reader.readLine())!=null) {
                        System.out.println(response);
                    }
                    oldbApiClientMock.disconnect();
                    return true;
                }
                oldbApiClientMock.disconnect();
                throw new OldbException("Requested data point do not exist.");
            }
        }).when(oldbApiClientMock).deleteDataPoint(any(String.class));

        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                cleanupBehaviour();
                return null;
            }
        }).when(oldbApiClientMock).cleanData();
    }

    @Override
    public boolean addDataPoint(DataPoint dataPoint) {
        return oldbApiClientMock.addDataPoint(dataPoint);
    }

    @Override
    public boolean deleteDataPoint(String name) {
        return oldbApiClientMock.deleteDataPoint(name);
    }

    @Override
    public DataPoint getDataPoint(String name) throws OldbException {
        return oldbApiClientMock.getDataPoint(name);
    }

    public void connect(){
        oldbApiClientMock.connect();
    }

    public void disconnect(){
        oldbApiClientMock.disconnect();
    }

    /**
     * Cleans the data in storage
     */
    public void cleanData() {
        oldbApiClientMock.cleanData();
    }

    /**
     * Cleans up mock behaviour. Needs to be redefined.
     */
    public void cleanupBehaviour() {
        Mockito.reset(oldbApiClientMock);
    }
}
