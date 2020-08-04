package thesis.testing.system;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import thesis.testing.system.archivingMocks.DataPacket;
import thesis.testing.system.oldbMocks.DataPoint;
import thesis.testing.system.oldbMocks.OldbSimulator;
import thesis.testing.utils.ChangeListener;
import thesis.testing.utils.Configuration;
import thesis.testing.utils.DaemonFactory;
import thesis.testing.utils.Transporter;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.ArrayList;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.mockito.Matchers.any;

//Simulates simple service that manages communication between parts of the testing system
public class ServiceSimulator implements ChangeListener {

    private final static String RESPONSE_404_PACKET = "No dataPackets found.";
    private final static String RESPONSE_404_POINT = "No dataPoint found.";
    private final ServiceSimulator serviceSimulatorMock;
    private final ScheduledExecutorService executorService;
    private final OldbSimulator oldbSimulator;
    private final Storage storage;

    public ServiceSimulator(Configuration config) {
        serviceSimulatorMock = Mockito.mock(ServiceSimulator.class);
        //setup storage for data
        storage = new Storage();
        //sets up source of data, subscribe and run periodically
        oldbSimulator = new OldbSimulator();
        //fill simulator with default data to update, if configuration is default
        if (config.getPointIdMatch().equals("") && config.isCheckValid() && config.getValueCap() == -1){
            oldbSimulator.setupDefaultData();
        }
        subscribeToChanges();
        //Define captures the point on call to notify
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                DataPoint dataPoint = (DataPoint) invocationOnMock.getArguments()[0];
                if (dataPoint.getPointId().equals(config.getPointIdMatch()) || config.getPointIdMatch().isEmpty()) {
                    if (config.isCheckValid()) {
                        if (dataPoint.isValid() && (config.getValueCap() == -1 || config.getValueCap() >= dataPoint.getValue())) {
                            storage.storeData(dataPoint);
                        }
                    } else {
                        if (config.getValueCap() == -1 || config.getValueCap() >= dataPoint.getValue()) {
                            storage.storeData(dataPoint);
                        }
                    }
                }
                return null;
            }
        }).when(serviceSimulatorMock).dataChange(any(DataPoint.class));

        //setup archiving handler behaviour
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return new HttpHandler() {
                    @Override
                    public void handle(HttpExchange exchange) throws IOException {

                        //read request data
                        final String requestBody = readRequestData(exchange.getRequestBody());
                        System.out.println("Request Body:" + requestBody);
                        Transporter transporter = new Gson().fromJson(requestBody, Transporter.class);
                        OutputStream os = exchange.getResponseBody();
                        exchange.getResponseHeaders().set("Content-Type", "application/json");
                        switch (transporter.getAction()) {
                            case STORE:
                                //store data and return ids
                                DataPoint[] dataPoints = new Gson().fromJson(transporter.getData(), DataPoint[].class);
                                List<UUID> packetIdsStore = new ArrayList<>();
                                for (DataPoint point : dataPoints) {
                                    UUID packetId = storage.storeData(point);
                                    if (packetId != null) packetIdsStore.add(packetId);
                                }
                                System.out.println("Storing data...");
                                //returned ids of stored packets
                                if (packetIdsStore.isEmpty()) {
                                    //404 and response
                                    exchange.sendResponseHeaders(404, RESPONSE_404_PACKET.length());
                                    os.write(RESPONSE_404_PACKET.getBytes());
                                    os.close();
                                    return;
                                }
                                String responseStore = new Gson().toJson(packetIdsStore);
                                exchange.sendResponseHeaders(200, responseStore.length());
                                os.write(responseStore.getBytes());
                                os.close();
                                break;
                            case GET:
                                //
                                UUID[] packetIdsGet = new Gson().fromJson(transporter.getData(), UUID[].class);
                                List<DataPacket> dataPackets = new ArrayList<>();
                                for (UUID packetId : packetIdsGet) {
                                    DataPacket dataPacket = storage.getData(packetId);
                                    if (dataPacket != null) dataPackets.add(dataPacket);
                                }
                                System.out.println("getting data...");
                                if (dataPackets.isEmpty()) {
                                    exchange.sendResponseHeaders(404, RESPONSE_404_PACKET.length());
                                    os.write(RESPONSE_404_PACKET.getBytes());
                                    os.close();
                                    return;
                                }

                                //return data packets
                                String responseGet = new Gson().toJson(dataPackets);
                                exchange.sendResponseHeaders(200, responseGet.length());
                                os.write(responseGet.getBytes());
                                os.close();
                                break;
                            case SEARCH:
                                List<UUID> packetIdsSearch = storage.searchData(transporter.getData());
                                System.out.println("searching for data...");
                                if (packetIdsSearch.isEmpty()) {
                                    exchange.sendResponseHeaders(404, RESPONSE_404_PACKET.length());
                                    os.write(RESPONSE_404_PACKET.getBytes());
                                    os.close();
                                    return;
                                }
                                String responseSearch = new Gson().toJson(packetIdsSearch);
                                exchange.sendResponseHeaders(200, responseSearch.length());
                                os.write(responseSearch.getBytes());
                                os.close();
                                break;
                            case DELETE:
                                storage.cleanData();
                                String responseDelete = "deleted";
                                exchange.sendResponseHeaders(200, responseDelete.length());
                                os.write(responseDelete.getBytes());
                                os.close();
                                break;
                            default:
                                String response = "Unknown command:" + transporter.getAction().toString();
                                exchange.sendResponseHeaders(404, response.length());
                                os.write(response.getBytes());
                                os.close();
                                break;
                        }
                    }
                };
            }
        }).when(serviceSimulatorMock).getArchivingHandler();

        //setup oldb handler behaviour
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return new HttpHandler() {
                    @Override
                    public void handle(HttpExchange exchange) throws IOException {
                        final String requestBody = readRequestData(exchange.getRequestBody());
                        System.out.println("Request Body:" + requestBody);
                        Transporter transporter = new Gson().fromJson(requestBody, Transporter.class);
                        OutputStream os = exchange.getResponseBody();
                        exchange.getResponseHeaders().set("Content-Type", "application/json");
                        switch (transporter.getAction()) {
                            case STORE: //create new data point
                                DataPoint dataPoint = new Gson().fromJson(transporter.getData(), DataPoint.class);
                                boolean statusAdd = oldbSimulator.addDataPoint(dataPoint);
                                if (!statusAdd) {
                                    exchange.sendResponseHeaders(404, RESPONSE_404_POINT.length());
                                    os.write(RESPONSE_404_POINT.getBytes());
                                    os.close();
                                    return;
                                }
                                String responseAdd = "Data point added to oldb.";
                                exchange.sendResponseHeaders(200, responseAdd.length());
                                os.write(responseAdd.getBytes());
                                os.close();
                                break;
                            case GET: // get specified data point
                                String pointIdGet = new Gson().fromJson(transporter.getData(), String.class);
                                DataPoint dp = oldbSimulator.getDataPoint(pointIdGet);
                                if (dp == null) {
                                    exchange.sendResponseHeaders(404, RESPONSE_404_POINT.length());
                                    os.write(RESPONSE_404_POINT.getBytes());
                                    os.close();
                                    return;
                                }
                                //return data packets
                                String responseGet = new Gson().toJson(dp);
                                exchange.sendResponseHeaders(200, responseGet.length());
                                os.write(responseGet.getBytes());
                                os.close();
                                break;
                            case DELETE: //delete specified dataPoint
                                String pointIdDelete = new Gson().fromJson(transporter.getData(), String.class);
                                boolean statusDelete = oldbSimulator.deleteDataPoint(pointIdDelete);
                                if (!statusDelete){
                                    exchange.sendResponseHeaders(404, RESPONSE_404_POINT.length());
                                    os.write(RESPONSE_404_POINT.getBytes());
                                    os.close();
                                    return;
                                }
                                String responseDelete = "Data point deleted.";
                                exchange.sendResponseHeaders(200, responseDelete.length());
                                os.write(responseDelete.getBytes());
                                os.close();
                                break;
                            default:
                                String response = "Unknown command:" + transporter.getAction().toString();
                                exchange.sendResponseHeaders(404, response.length());
                                os.write(response.getBytes());
                                os.close();
                                break;

                        }
                    }
                };
            }
        }).when(serviceSimulatorMock).getOldbHandler();

        //Define start method beahaviour
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                //Setup server to handle archiver requests
                HttpServer server = HttpServer.create(new InetSocketAddress(4242), 0);
                server.createContext("/archiving", getArchivingHandler());
                server.createContext("/oldb",getOldbHandler());
                server.setExecutor(null);
                server.start();
                System.out.println("service waiting for connections...");
                return null;
            }
        }).when(serviceSimulatorMock).start();

        executorService = Executors.newSingleThreadScheduledExecutor(new DaemonFactory());
        executorService.scheduleAtFixedRate(oldbSimulator, 2, 3, TimeUnit.SECONDS);
    }

    public static void main(String[] args) throws Exception {
        Configuration defaultConfiguration = new Configuration(true, -1, "");

        if (args.length >= 3) {
            String id = args[0];
            boolean check = Boolean.parseBoolean(args[1]);
            int cap = Integer.parseInt(args[2]);
            defaultConfiguration.setPointIdMatch(id);
            defaultConfiguration.setCheckValid(check);
            defaultConfiguration.setValueCap(cap);
        }

        new ServiceSimulator(defaultConfiguration).start();
    }

    public ServiceSimulator getMock() {
        return serviceSimulatorMock;
    }

    //set listener for data changes
    public void subscribeToChanges() {
        oldbSimulator.subscribe(this);
    }

    public Storage getStorage() {
        return storage;
    }

    @Override
    public void dataChange(DataPoint point) {
        getMock().dataChange(point);
    }

    public HttpHandler getArchivingHandler() {
        return serviceSimulatorMock.getArchivingHandler();
    }

    public HttpHandler getOldbHandler() {
        return serviceSimulatorMock.getOldbHandler();
    }

    public void start() {
        serviceSimulatorMock.start();
    }

    private String readRequestData(InputStream requestBody) throws IOException {
        final InputStreamReader streamReader = new InputStreamReader(requestBody, "utf-8");
        final BufferedReader bufferedReader = new BufferedReader(streamReader);
        final StringBuilder builder = new StringBuilder();
        int bCode;
        while ((bCode = bufferedReader.read()) != -1) {
            builder.append((char) bCode);
        }
        bufferedReader.close();
        streamReader.close();
        return builder.toString();
    }
}
