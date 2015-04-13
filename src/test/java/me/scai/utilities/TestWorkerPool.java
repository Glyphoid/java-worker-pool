package me.scai.utilities;

import java.util.List;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;

/**
 * Created by scai on 4/11/2015.
 */
class ConcreteWorker implements PooledWorker {
    /* Constructor */
    public ConcreteWorker() {

    }
}

public class TestWorkerPool {
    @Test
    public void testWorkerPool() throws InterruptedException {
        final int maxNumWorkers = 8;
        final long workerTimeout = 4 * 1000;

        WorkerPool wp = new WorkerPoolImpl(maxNumWorkers, workerTimeout);

        assertEquals(wp.getNumAvailableSlots(), maxNumWorkers);
        assertEquals(wp.getWorkerTimeout(), workerTimeout);

        /* Register a new worker */
        String wkrId = wp.registerWorker(new ConcreteWorker());
        assertEquals(wp.getNumAvailableSlots(), maxNumWorkers - 1);

        /* Wait for a period of time that shouldn't lead to expiration */
        Thread.sleep(workerTimeout / 2);

        /* Verify that the worker is not purged */
        List<String> purgeList = wp.purge();
        assertNull(purgeList);
        assertEquals(wp.getNumAvailableSlots(), maxNumWorkers - 1);

        /* Wait for longer, for expiration */
        Thread.sleep(workerTimeout);

        /* Verify that the worker has expired and been purged */
        purgeList = wp.purge();
        assertNotNull(purgeList);
        assertEquals(purgeList.size(), 1);
        assertEquals(purgeList.get(0), wkrId);
        assertEquals(wp.getNumAvailableSlots(), maxNumWorkers);

        /* Exhaust the worker pool */
        for (int i = 0; i < maxNumWorkers; ++i) {
            String newWkrId = wp.registerWorker(new ConcreteWorker());
            assertNotNull(newWkrId);

            int numSlots = wp.getNumAvailableSlots();
            assertEquals(numSlots, maxNumWorkers - 1 - i);
        }

        /* If you attempt to register a new worker now, it should lead to failure */
        String newWkrId1 = wp.registerWorker(new ConcreteWorker());
        assertNull(newWkrId1);

        /* Wait for all workers to expire */
        Thread.sleep(workerTimeout + 100);
        purgeList = wp.purge();
        assertNotNull(purgeList);
        assertEquals(purgeList.size(), maxNumWorkers);
    }
}
