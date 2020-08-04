package thesis.testing.system.archivingMocks;

import thesis.testing.system.oldbMocks.DataPoint;
import thesis.testing.utils.ArchivingException;

import java.util.List;
import java.util.UUID;

/**
 * Interface for Archiving API used to store, retrieve and search data
 */
public interface ArchivingAPI {

    /**
     * Cleanup data from storage
     */
    void cleanData();

    /**
     * Connect to service
     */
    void connect();

    /**
     * Disconnect from service
     */
    void disconnect();

    /**
     *
     * Virtually stores passed DataPackets for further actions
     * @param dataPoints to be stored
     * @return list of stored packets id's
     * @throws ArchivingException if there is a problem when storing data
     */
    List<UUID> storeData(List<DataPoint> dataPoints) throws ArchivingException;

    /**
     * Retrieves data packets based on passed packet id's
     *
     * @param packetIds of packets to be retrieved
     * @return list of DataPacket objects matching the passed id's
     * @throws ArchivingException if there is a problem when retrieving data
     */
    List<DataPacket> getData(List<UUID> packetIds) throws ArchivingException;

    /**
     * Searches for Data over packets based on given query
     *
     * @param query defining search request
     * @return list of packet id's that matched the given query
     * @throws ArchivingException if there is a problem when searching for data
     */
    List<UUID> searchData(String query) throws ArchivingException;
}
