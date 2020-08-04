package thesis.testing.system.archivingTests;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import thesis.testing.system.archivingMocks.Archiver;
import thesis.testing.system.archivingMocks.ArchivingApiClient;
import thesis.testing.system.archivingMocks.DataPacket;
import thesis.testing.system.oldbMocks.DataPoint;
import thesis.testing.utils.ArchivingException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ArchiverTests {

    private final static String ID_FORMAT = "%s%d";
    private final static String DP_BASE_ID = "dp_";

    private DataPoint dp1 = new DataPoint(String.format(ID_FORMAT, DP_BASE_ID, 1), 1, true);
    private DataPoint dp2 = new DataPoint(String.format(ID_FORMAT, DP_BASE_ID, 2), 2, false);
    private DataPoint dp3 = new DataPoint(String.format(ID_FORMAT, DP_BASE_ID, 3), 3, true);
    private List<UUID> testingPacketsIds = new ArrayList<>();

    private Archiver archiver;
    private ArchivingApiClient client;

    public ArchiverTests() throws ArchivingException {
        archiver = new Archiver();
        client = new ArchivingApiClient();
    }

    @BeforeMethod
    public void setup() {
        testingPacketsIds = client.storeData(Arrays.asList(new DataPoint[]{dp1,dp2,dp3}));
    }

    @AfterMethod
    public void cleanup(){
        client.cleanData();
    }

    /**
     * Tests if exception is thrown when getting invalid command
     * @throws ArchivingException
     */
    @Test(expectedExceptions = {ArchivingException.class})
    public void invalidCommandTest() throws ArchivingException {
        archiver.executeCommand(archiver.parseCommand(new String[]{"command", "param1", "param2"}));
    }

    /**
     * Tests if exception is thrown for store command with invalid params
     * @throws ArchivingException
     */
    @Test(expectedExceptions = {ArchivingException.class})
    public void validStoreCommandInvalidParamsTest() throws ArchivingException {
        archiver.executeCommand(archiver.parseCommand(new String[]{"store", "param1", "param2", "param3"}));
    }

    /**
     * Tests if exception is thrown for get command with invalid params
     * @throws ArchivingException
     */
    @Test(expectedExceptions = {ArchivingException.class})
    public void validGetCommandInvalidParamsTest() throws ArchivingException {
        archiver.executeCommand(archiver.parseCommand(new String[]{"get", "param1", "param2", "param3"}));
    }

    /**
     * Tests if exception is thrown for search command with invalid params
     * @throws ArchivingException
     */
    @Test(expectedExceptions = {ArchivingException.class})
    public void validSearchCommandInvalidParamsTest() throws ArchivingException {
        archiver.executeCommand(archiver.parseCommand(new String[]{"search", "param1", "param2", "param3"}));
    }

    /**
     * Tests if retrieving existing data packets works
     */
    @Test
    public void getValidDataPacketsTest() {
        List<DataPacket> dataPackets = archiver.displayData(testingPacketsIds);
        Assert.assertEquals(dataPackets.size(),3);
        Assert.assertEquals(dataPackets.get(0).getDataPoint().getValue(),dp1.getValue());
        Assert.assertEquals(dataPackets.get(1).getDataPoint().getPointId(),dp2.getPointId());
    }

    /**
     * Tests if retrieval of non-existing data packet triggers an exception
     * @throws ArchivingException
     */
    @Test(expectedExceptions = {ArchivingException.class})
    public void getInvalidDataPacketsTest() throws ArchivingException {
        archiver.displayData(Arrays.asList(new UUID[]{UUID.randomUUID()}));
    }

    /**
     * Tests if storing valid data point works
     */
    @Test
    public void storeValidDataPointTest() {
       List<UUID> ids = archiver.storeData(Arrays.asList(new DataPoint[]{new DataPoint("dp_42",42,true)}));
       Assert.assertTrue(!ids.isEmpty());
    }

    /**
     * Tests if exception is thrown when trying to store invalid data point
     * @throws ArchivingException
     */
    @Test(expectedExceptions = {ArchivingException.class})
    public void storeInvalidDataPointTest() throws ArchivingException {
        archiver.storeData(Arrays.asList(new DataPoint[]{new DataPoint("dp_24",-5,false)}));
    }

    /**
     * Tests if searching for packets by point id works
     */
    @Test
    public void searchValidDataPacketsDpTest(){
        List<UUID> ids = archiver.queryDataPacketSearch("pointId:dp_1");
        Assert.assertTrue(!ids.isEmpty());
    }

    /**
     * Tests if searching for packets by date works
     */
    @Test
    public void searchValidDataPacketsDateTest(){
        List<UUID> ids = archiver.queryDataPacketSearch(String.format("date:%s", LocalDate.now().toString()));
        Assert.assertTrue(!ids.isEmpty());
    }

    /**
     * Tests if exception is thrown when providing invalid query
     * @throws ArchivingException
     */
    @Test(expectedExceptions = {ArchivingException.class})
    public void searchInvalidDataPackets() throws ArchivingException {
        archiver.queryDataPacketSearch("invalid query");
    }

}
