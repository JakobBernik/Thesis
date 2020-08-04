package thesis.testing.system.archivingTests;


import org.testng.annotations.*;
import thesis.testing.system.ServiceSimulator;
import thesis.testing.system.archivingMocks.ArchivingApiDelegate;
import thesis.testing.system.archivingMocks.DataPacket;
import thesis.testing.system.oldbMocks.DataPoint;
import thesis.testing.utils.ArchivingException;
import thesis.testing.utils.DaemonFactory;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Integration test for archiving API
 */
public class ArchivingApiDelegateTest {

    private final static String ID_FORMAT = "%s%d";
    private final static String DP_BASE_ID = "dp_";

    private DataPoint dp1 = new DataPoint(String.format(ID_FORMAT, DP_BASE_ID, 1), 1, true);
    private DataPoint dp2 = new DataPoint(String.format(ID_FORMAT, DP_BASE_ID, 2), 2, false);
    private DataPoint dp3 = new DataPoint(String.format(ID_FORMAT, DP_BASE_ID, 3), 3, true);
    private List<UUID> testingPacketsIds = new ArrayList<>();

    private final ExecutorService executorService;
    private ArchivingApiDelegate client;

    public ArchivingApiDelegateTest() throws ArchivingException {
        executorService = Executors.newSingleThreadExecutor(new DaemonFactory());
        client = new ArchivingApiDelegate();

    }

    /**
     * Launches the service before tests
     */
    public void startService(){
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    ServiceSimulator.main(new String[]{});
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    /**
     * Setup test data for every individual test
     * @throws ArchivingException if there is an error when storing data (should not happen)
     */
    @BeforeMethod
    public void setup() throws ArchivingException {
        testingPacketsIds = client.storeData(new ArrayList<>(Arrays.asList(new DataPoint[]{dp1,dp2,dp3})));
    }

    /**
     * Cleans test data before every tests
     */
    @AfterMethod
    public void cleanup(){
        client.cleanData();
    }

    /**
     * Shutdown executor service
     * @throws InterruptedException
     */
    public void endService() throws InterruptedException {
        executorService.shutdown();
        if (!executorService.awaitTermination(5, TimeUnit.SECONDS)){
            executorService.shutdownNow();
        }
    }

    /**
     * Test if valid data is returned
     */
    @Test
    public void getExistingDataPacketsTest() throws ArchivingException {
        List<DataPacket> packets = client.getData(Collections.singletonList(testingPacketsIds.get(0)));
        assertEquals(packets.isEmpty(),false);
        assertEquals(packets.get(0).getDataPoint().getValue(),dp1.getValue());
        assertEquals(packets.get(0).getPacketId(),testingPacketsIds.get(0));
    }

    /**
     * Test if exception is thrown for requesting non existing data packets
     */
    @Test(expectedExceptions = {ArchivingException.class})
    public void getMissingDataPacketsTest() throws ArchivingException {
        client.getData(Collections.singletonList(UUID.randomUUID()));
    }

    /**
     * Test to store valid data packets to the storage
     */
    @Test
    public void storeValidDataPacketsTest() throws ArchivingException {
        List<UUID> packetIds = client.storeData(Collections.singletonList(new DataPoint("dp_42",42,true)));
        assertTrue(!packetIds.isEmpty());
        List<DataPacket> packets = client.getData(packetIds);
        assertTrue(!packets.isEmpty());
        DataPoint point = packets.get(0).getDataPoint();
        assertEquals(packets.get(0).getPacketId(),packetIds.get(0));
        assertEquals(point.getValue(),42);
        assertEquals(point.getPointId(),"dp_42");
        assertTrue(point.isValid());
    }

    /**
     * Test that exception is throwing when trying to store invalid data packets
     */
    @Test(expectedExceptions = {ArchivingException.class})
    public void storeInvalidDataPacketsTest() throws ArchivingException {
        client.storeData(Collections.singletonList(new DataPoint("dp_6",142,false)));
    }

    /**
     * Test if searching for existing data by point name returns its ids
     */
    @Test
    public void searchValidQueryTestDp() throws ArchivingException {
        List<UUID> packetIds = client.searchData("pointId:dp_2");
        assertTrue(!packetIds.isEmpty());
        assertTrue(packetIds.contains(testingPacketsIds.get(1)));
    }

    /**
     * Test if searching for existing data by date returns its ids
     */
    @Test
    public void searchValidQueryTestDate() throws ArchivingException {
        List<UUID> packetIds = client.searchData("date:"+ LocalDate.now());
        assertTrue(!packetIds.isEmpty());
        assertTrue(packetIds.contains(testingPacketsIds.get(0)));
        assertTrue(packetIds.contains(testingPacketsIds.get(1)));
        assertTrue(packetIds.contains(testingPacketsIds.get(2)));
    }


    /**
     * Test if exception is thrown when trying to execute invalid query
     */
    @Test(expectedExceptions = {ArchivingException.class})
    public void searchInvalidQueryTest() throws ArchivingException {
        client.searchData("invalid query");
    }
}
