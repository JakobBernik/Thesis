package thesis.testing.system.oldbMocks;

import thesis.testing.utils.ArchivingException;
import thesis.testing.utils.OldbException;

/**
 * Enables communication with online database.
 */
public interface OldbAPI {

    /**
     * creates new data point for database to operate on
     * @param dataPoint to be added to storage
     * @return true if point was added, false otherwise
     * @throws OldbException if there is a problem when adding data point
     */
    boolean addDataPoint(DataPoint dataPoint) throws OldbException;

    /**
     * Delete existing data point
     * @param name of data point to be deleted
     * @return true if point was deleted, false otherwise
     * @throws OldbException if there is a problem when deleting data point
     */
    boolean deleteDataPoint(String name) throws OldbException;

    /**
     * Retrieve existing data point
     * @param name of data point to retrieve
     * @return data point with specified name
     * @throws OldbException if there is a problem when retrieving data point
     */
    DataPoint getDataPoint(String name) throws OldbException;

}
