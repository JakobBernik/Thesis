package thesis.testing.system.oldbMocks;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import thesis.testing.utils.ChangeListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Simulates data Source and changes on set of data
 */
public class OldbSimulator implements Runnable,OldbAPI {

    private final static String ID_FORMAT = "%s%d";
    private final static String DP_BASE_ID = "dp_";

    private DataPoint dp1 = new DataPoint(String.format(ID_FORMAT, DP_BASE_ID, 1), 1, true);
    private DataPoint dp2 = new DataPoint(String.format(ID_FORMAT, DP_BASE_ID, 2), 2, false);
    private DataPoint dp3 = new DataPoint(String.format(ID_FORMAT, DP_BASE_ID, 3), 3, true);
    private OldbSimulator oldbSimulatorMock;
    private ChangeListener subscriber;

    public OldbSimulator() {
        oldbSimulatorMock = Mockito.mock(OldbSimulator.class);
        //setup default data points that are stored
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return new ArrayList<>();
            }
        }).when(oldbSimulatorMock).getAllDataPoints();

        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                addDataPoint(dp1);
                addDataPoint(dp2);
                addDataPoint(dp3);
                return null;
            }
        }).when(oldbSimulatorMock).setupDefaultData();

        //When new update is run randomly update existing data point
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                double change = Math.random();
                List<DataPoint> dataPoints = oldbSimulatorMock.getAllDataPoints();
                if (dataPoints.isEmpty()){
                    System.out.println("No data points in database.");
                    return null;
                }

                int indx =(int)(change*dataPoints.size());
                DataPoint dp = dataPoints.get(indx);
                dataPoints.set(indx,dp);
                dp.setValue((int) (change*change*42));
                // override data retrieval methods after update
                Mockito.doAnswer(new Answer() {
                    @Override
                    public Object answer(InvocationOnMock invocation) throws Throwable {
                        return dataPoints;
                    }
                }).when(oldbSimulatorMock).getAllDataPoints();

                Mockito.doAnswer(new Answer() {
                    @Override
                    public Object answer(InvocationOnMock invocation) throws Throwable {
                        return dp;
                    }
                }).when(oldbSimulatorMock).getDataPoint(dp.getPointId());

                System.out.println("changed dp: "+dp.getPointId()+", value: "+dp.getValue());
                subscriber.dataChange(dp);
                return null;
            }
        }).when(oldbSimulatorMock).run();
    }

    public void subscribe(ChangeListener changeListener) {
        subscriber = changeListener;
    }

    public void unsubscribe() {
        subscriber = null;
    }

    @Override
    public void run() {
        oldbSimulatorMock.run();
    }


    @Override
    public boolean addDataPoint(DataPoint dataPoint) {
        List<DataPoint> dataPoints = oldbSimulatorMock.getAllDataPoints();
        if (dataPoints.contains(dataPoint)) return false;
        dataPoints.add(dataPoint);

        //override old behaviour
        final List<DataPoint> newDataPoints = dataPoints;
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return newDataPoints;
            }
        }).when(oldbSimulatorMock).getAllDataPoints();

        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return dataPoint;
            }
        }).when(oldbSimulatorMock).getDataPoint(dataPoint.getPointId());

        //mock behaviour when point is deleted
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {

                boolean isDeleted = false;
                List<DataPoint> dataPointsDelete = oldbSimulatorMock.getAllDataPoints();
                for (DataPoint dp: dataPointsDelete){
                    if (dp.getPointId().equals(dataPoint.getPointId())){
                        isDeleted = true;
                        dataPointsDelete.remove(dp);
                        Mockito.doAnswer(new Answer() {
                            @Override
                            public Object answer(InvocationOnMock invocation) throws Throwable {
                                return null;
                            }
                        }).when(oldbSimulatorMock).getDataPoint(dataPoint.getPointId());

                        break;
                    }
                }
                Mockito.doAnswer(new Answer() {
                    @Override
                    public Object answer(InvocationOnMock invocation) throws Throwable {
                        return dataPointsDelete;
                    }
                }).when(oldbSimulatorMock).getAllDataPoints();
                return isDeleted;
            }

        }).when(oldbSimulatorMock).deleteDataPoint(dataPoint.getPointId());

        return true;
    }

    @Override
    public boolean deleteDataPoint(String name) {
        return oldbSimulatorMock.deleteDataPoint(name);
    }

    @Override
    public DataPoint getDataPoint(String name) {
        return oldbSimulatorMock.getDataPoint(name);
    }

    /**
     * Retrieves stored data points
     * @return all data points "stored" in database
     */
    public List<DataPoint> getAllDataPoints(){
        return oldbSimulatorMock.getAllDataPoints();
    }

    /**
     * Adds default data for simulator
     */
    public void setupDefaultData(){
        oldbSimulatorMock.setupDefaultData();
    }
}
