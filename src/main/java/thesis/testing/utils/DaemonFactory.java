package thesis.testing.utils;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Enables creation of daemon threads
 */
public class DaemonFactory implements ThreadFactory {

        public Thread newThread(Runnable r) {
            Thread t = Executors.defaultThreadFactory().newThread(r);
            t.setDaemon(true);
            return t;
        }
}
