package thesis.testing.system;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import thesis.testing.system.archivingMocks.DataPacket;
import thesis.testing.system.oldbMocks.DataPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Mock implementation of persistent storage for service Simulator
 */
public class Storage {

    private final static String DP_QUERY = "pointId:";
    private final static String DATE_QUERY = "date:";

    private Storage storageMock;

    public Storage() {
        storageMock = Mockito.mock(Storage.class);
    }

    /**
     * Returns stored data packet
     *
     * @param packetId that defines packet to be returned
     * @return data packet
     */
    public DataPacket getData(UUID packetId) {
        return storageMock.getData(packetId);
    }

    /**
     * Virtually stores data packet and defines behaviour of all other methods in interaction with this data packet
     *
     * @param dataPoint to be stored
     */
    public UUID storeData(DataPoint dataPoint) {
        System.out.println("Inside STORAGE");
        //Create new DataPacket
        final DataPacket dataPacket = new DataPacket(dataPoint);
        System.out.println(dataPacket.getCapturedDate());
        //defines behaviour of get method when called
        Mockito.when(storageMock.getData(dataPacket.getPacketId())).thenReturn(dataPacket);

        System.out.println("Data packet with id " + dataPacket.getPacketId() + " stored.");

        //defines behaviour of search when date is passed.
        //Multiple packets can match, and since we overwrite the behaviour of a method,
        //we need to include previous behaviour aswell
        List<UUID> dataListDate = storageMock.searchData(String.format("%s%s",DATE_QUERY,dataPacket.getCapturedDate().toString()));
        if(dataListDate == null){
            dataListDate = new ArrayList<>();
        }
        dataListDate.add(dataPacket.getPacketId());
        final List<UUID> returnDateList = dataListDate;
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return returnDateList;
            }
        }).when(storageMock).searchData(String.format("%s%s",DATE_QUERY,dataPacket.getCapturedDate().toString()));

        //defines behaviour of search when point is passed.
        //Multiple packets can match, and since we overwrite the behaviour of a method,
        //we need to include previous behaviour aswell
        List<UUID> dataListDp = storageMock.searchData(String.format("%s%s",DP_QUERY,dataPacket.getDataPoint().getPointId()));
        if(dataListDp == null){
            dataListDp = new ArrayList<>();
        }
        dataListDp.add(dataPacket.getPacketId());
        final List<UUID> returnDpList = dataListDp;
        System.out.println();
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return returnDpList;
            }
        }).when(storageMock).searchData(String.format("%s%s",DP_QUERY,dataPacket.getDataPoint().getPointId()));

        return dataPacket.getPacketId();
    }

    /**
     * Searches for data based on passed query
     *
     * @param query that defines data to be returned
     * @return list of id's for packets that matched the query
     */
    public List<UUID> searchData(String query) {
        return storageMock.searchData(query);
    }

    /**
     * Wipes the data stored(resets the mock)
     */
    public void cleanData(){
        Mockito.reset(storageMock);
    }

}
