package thesis.testing.system.archivingMocks;

import com.google.gson.Gson;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import thesis.testing.system.oldbMocks.DataPoint;
import thesis.testing.utils.ArchivingException;
import thesis.testing.utils.Command;
import thesis.testing.utils.Transporter;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;

/**
 * Mockito demo of an API that enables storing, retrieval and searching of dataPackets using service Simulator.
 * Here calls to methods are actually implemented with mockito, but only represent a part in whole service, as calls are further
 * delegated to the service.
 */
public class ArchivingApiClient implements ArchivingAPI{

    private final String URL_PATH = "http://localhost:4242/archiving";
    private HttpURLConnection http;
    // mock of archivingApiClient, to which method calls to ArchivingApiClient are delegated to
    private ArchivingApiClient archivingApiClientMock;

    public ArchivingApiClient() throws ArchivingException {
        archivingApiClientMock = Mockito.mock(ArchivingApiClient.class);

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
        }).when(archivingApiClientMock).connect();

        //mock disconnect method behaviour
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                http.disconnect();
                return null;
            }
        }).when(archivingApiClientMock).disconnect();

        //Mock storeData method behaviour
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                //get data passsed  , pack it into transporter and send it
                archivingApiClientMock.connect();
                System.out.println("store started...");
                List<DataPoint> dataPoints = (List<DataPoint>) invocation.getArguments()[0];
                for (DataPoint point: dataPoints){
                    if (point.getValue() < 0){
                        throw new ArchivingException("Invalid data point.");
                    }
                }
                String jsonData = new Gson().toJson(dataPoints);
                System.out.println("packets: "+jsonData);
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
                    UUID[] packetIds = gson.fromJson(reader, UUID[].class);
                    archivingApiClientMock.disconnect();
                    return new ArrayList(Arrays.asList(packetIds));
                }
                archivingApiClientMock.disconnect();
                throw new ArchivingException("failed to store data packet.");
            }
        }).when(archivingApiClientMock).storeData(anyListOf(DataPoint.class));

        //Mock getData method behaviour
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                List<UUID> packetIds = (List<UUID>) invocation.getArguments()[0];
                archivingApiClientMock.connect();
                System.out.println("get started...");
                String jsonData = new Gson().toJson(packetIds);
                System.out.println("packets: "+jsonData);
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
                    DataPacket[] packets = gson.fromJson(reader, DataPacket[].class);
                    archivingApiClientMock.disconnect();
                    return Arrays.asList(packets);
                }
                archivingApiClientMock.disconnect();
                throw new ArchivingException("Requested packets do not exist.");
            }
        }).when(archivingApiClientMock).getData(anyListOf(UUID.class));

        //Mock queryData method behaviour
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                String query = (String) invocation.getArguments()[0];
                archivingApiClientMock.connect();
                System.out.println("search started...");
                final Transporter transporter = new Transporter(Command.SEARCH,query);
                String requestData = new Gson().toJson(transporter);
                try(OutputStream os = http.getOutputStream()) {
                    os.write(requestData.getBytes());
                }
                if (http.getResponseCode() == 200) {
                    InputStream inputStream = http.getInputStream();
                    final Gson gson = new Gson();
                    final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    UUID[] packetIds = gson.fromJson(reader, UUID[].class);
                    archivingApiClientMock.disconnect();
                    return new ArrayList(Arrays.asList(packetIds));
                }
                archivingApiClientMock.disconnect();
                throw new ArchivingException("No packets were found.");
            }
        }).when(archivingApiClientMock).searchData(any(String.class));

        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                archivingApiClientMock.connect();
                System.out.println("wiping storage...");
                final Transporter transporter = new Transporter(Command.DELETE,"delete");
                String requestData = new Gson().toJson(transporter);
                try(OutputStream os = http.getOutputStream()) {
                    os.write(requestData.getBytes());
                }
                return null;
            }
        }).when(archivingApiClientMock).cleanData();
    }

    /**
     * Virtually stores passed DataPackets for further actions
     *
     * @param dataPoints to be stored
     * @return list of stored packets id's
     */
    public List<UUID> storeData(List<DataPoint> dataPoints) {
        return archivingApiClientMock.storeData(dataPoints);
    }

    /**
     * Retrieves data packets based on passed packet id's
     *
     * @param packetIds of packets to be retrieved
     * @return list of DataPacket objects matching the passed id's
     */
    public List<DataPacket> getData(List<UUID> packetIds) {
        return archivingApiClientMock.getData(packetIds);
    }

    /**
     * Searches for Data over packets based on given query
     *
     * @param query defining search request
     * @return list of packet id's that matched the given query
     */
    public List<UUID> searchData(String query) {
        return archivingApiClientMock.searchData(query);
    }

    /**
     * Cleans the data in storage
     */
    public void cleanData() {
        archivingApiClientMock.cleanData();
    }

    /**
     * Connect to the service
     */
    public void connect() {
        archivingApiClientMock.connect();
    }

    /**
     * Disconnect from the service
     */
    public void disconnect() {
        archivingApiClientMock.disconnect();
    }

    /**
     * Cleans up mock behaviour. Needs to be redefined.
     */
    public void cleanupBehaviour() {
        Mockito.reset(archivingApiClientMock);
    }
}
