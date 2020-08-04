package thesis.testing.system.oldbMocks;

import java.util.UUID;

/**
 * Data representation for oldb
 */
public class DataPoint {

    private final String pointId;
    private int value;
    private boolean isValid;

    public DataPoint(String pointName, int v, boolean valid) {
        pointId = pointName;
        value = v;
        isValid = valid;
    }

    public String getPointId() {
        return pointId;
    }

    public int getValue() {

        return value;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean valid) {
        isValid = valid;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
