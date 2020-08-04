package thesis.testing.system;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import thesis.testing.system.archivingMocks.ArchivingApiClient;
import thesis.testing.system.archivingMocks.DataPacket;
import thesis.testing.system.oldbMocks.DataPoint;
import thesis.testing.system.oldbMocks.OldbApiClient;
import thesis.testing.utils.ArchivingException;
import thesis.testing.utils.OldbException;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Tests behaviour of whole system.
 */
public class SystemTests {

    /*Service configuration for this tests should be:
    *   - pointIdMatch: dp_42
    *   - checkValid: true
    *   - valueCap: -1
    *
    * Running tests with run scripts already configures everything
    * */
    private final DataPoint subDataPoint = new DataPoint("dp_42",42,true);
    private final DataPoint nonsubDataPoint = new DataPoint("dp_24",24,false);

    private OldbApiClient oldbApiClient;
    private ArchivingApiClient archivingApiClient;
    
    
    public SystemTests() throws OldbException, ArchivingException {
        oldbApiClient = new OldbApiClient();
        archivingApiClient = new ArchivingApiClient();
    }

    /**
     * Test that newly added point to oldb via oldbApiClient will not be archived and accessible from
     * storage via archivingApiClient, as it doesn't match capture configuration
     */
    @Test
    public void nonsubscribedPointAdditionTest() throws OldbException {
        //add new data point that does not match configuration to oldb and check if it is only accessible via oldbApiClient.
        oldbApiClient.addDataPoint(nonsubDataPoint);
        try {
            //wait for service to try to archive data point
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            Assert.fail();
            e.printStackTrace();
        }
        Assert.assertThrows(ArchivingException.class,() -> archivingApiClient.searchData("pointId:dp_24"));
        DataPoint dataPoint = oldbApiClient.getDataPoint(nonsubDataPoint.getPointId());
        Assert.assertTrue(dataPoint != null);
        Assert.assertEquals(dataPoint.getPointId(),nonsubDataPoint.getPointId());
        oldbApiClient.deleteDataPoint(nonsubDataPoint.getPointId());
    }

    /**
     * Test that newly added point to oldb via oldbApiClient will be archived and accessible from
     * storage via archivingApiClient, as it matches capture configuration.
     */
    @Test
    public void subscribedPointAdditionTest(){
           //add new data point that matches configuration to oldb and check if it is archived and accessible via archivingApiClient.
        oldbApiClient.addDataPoint(subDataPoint);
        try {
            //wait for service to archive data point
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            Assert.fail();
            e.printStackTrace();
        }
        List<UUID> packetIds = archivingApiClient.searchData("pointId:dp_42");
        Assert.assertTrue(!packetIds.isEmpty());
        List<DataPacket> dataPackets = archivingApiClient.getData(packetIds);
        Assert.assertTrue(!dataPackets.isEmpty());
        Assert.assertEquals(dataPackets.get(0).getDataPoint().getPointId(),subDataPoint.getPointId());
    }

}
