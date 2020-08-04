package thesis.testing.utils;

import thesis.testing.system.oldbMocks.DataPoint;

/**
 * Used for listener pattern between service and online database
 */
public interface ChangeListener {

    void dataChange(DataPoint point);
}
