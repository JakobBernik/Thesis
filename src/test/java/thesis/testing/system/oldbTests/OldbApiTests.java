package thesis.testing.system.oldbTests;

import org.testng.Assert;
import org.testng.annotations.Test;
import thesis.testing.system.oldbMocks.DataPoint;
import thesis.testing.system.oldbMocks.OldbApiClient;
import thesis.testing.utils.OldbException;

public class OldbApiTests {

    private final static String ID_FORMAT = "%s%d";
    private final static String DP_BASE_ID = "dp_";

    private DataPoint dp4 = new DataPoint(String.format(ID_FORMAT, DP_BASE_ID, 4), 4, true);
    private DataPoint dp5 = new DataPoint(String.format(ID_FORMAT, DP_BASE_ID, 5), 5, false);
    private DataPoint dp6 = new DataPoint(String.format(ID_FORMAT, DP_BASE_ID, 6), 6, true);

    private OldbApiClient client;

    public OldbApiTests() throws OldbException {
        client = new OldbApiClient();
    }

    /**
     * Test if adding valid data point works
     */
    @Test
    public void addValidDataPointTest() throws OldbException {
        client.addDataPoint(dp4);
        DataPoint dataPoint = client.getDataPoint("dp_4");
        Assert.assertEquals(dataPoint.getPointId(),dp4.getPointId());
        Assert.assertEquals(dataPoint.getValue(),dp4.getValue());
        Assert.assertEquals(dataPoint.isValid(),dp4.isValid());
    }

    /**
     * Test if adding invalid data points throws exception
     * @throws OldbException if data point is invalid
     */
    @Test(expectedExceptions = {OldbException.class})
    public void addInvalidDataPointTest() throws OldbException {
        client.addDataPoint(new DataPoint("dp_7",-3,false));
    }

    /**
     * Test if retrieving existing data point works
     */
    @Test
    public void getExistingDataPointTest() throws OldbException {
        DataPoint dataPoint = client.getDataPoint("dp_2");
        Assert.assertEquals(dataPoint.getPointId(),"dp_2");
        Assert.assertTrue(!dataPoint.isValid());

    }

    /**
     * Test if retriving non-existing data point throws exception
     * @throws OldbException if point doesn't exist
     */
    @Test(expectedExceptions = {OldbException.class})
    public void getMissingDataPointTest() throws OldbException {
        client.getDataPoint("dp_8");
    }


    /**
     * Test if deleting existing data point works
     */
    @Test
    public void deleteExistingDataPointTest(){
        boolean status = client.deleteDataPoint("dp_1");
        Assert.assertTrue(status);
        Assert.assertThrows(OldbException.class,()-> client.getDataPoint("dp_1"));
    }

    /**
     * Test if deleting non-existing data point throws exception
     * @throws OldbException if data point doesn't exist
     */
    @Test(expectedExceptions = {OldbException.class})
    public void deleteMissingDataPointTest() throws OldbException {
        client.deleteDataPoint("dp_9");
    }





}
