package thesis.testing.system.archivingMocks;

import thesis.testing.system.oldbMocks.DataPoint;

import java.time.LocalDate;
import java.util.Date;
import java.util.UUID;

/**
 * Contains data captured from the data source with the time when it was captured.
 */
public class DataPacket {

    private final UUID packetId;
    private DataPoint dataPoint;
    private final LocalDate capturedDate;

    public DataPacket(DataPoint point){
        this.packetId = UUID.randomUUID();
        this.dataPoint = point;
        capturedDate = LocalDate.now();
    }

    public UUID getPacketId() {
        return packetId;
    }

    public DataPoint getDataPoint() {
        return dataPoint;
    }

    public LocalDate getCapturedDate() {
        return capturedDate;
    }

}
