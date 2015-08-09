package me.scai.utilities;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import me.scai.utilities.clienttypes.ClientTypeMajor;
import me.scai.utilities.clienttypes.ClientTypeMinor;
import org.junit.Before;
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
    private WorkerClientInfo wkrClientInfo;

    @Before
    public void beforeTest() {
        wkrClientInfo = null;
        try {
            wkrClientInfo = new WorkerClientInfo(
                    InetAddress.getLocalHost(),
                    InetAddress.getLocalHost().getHostName(),
                    ClientTypeMajor.API,
                    ClientTypeMinor.API_UnitTest
            );
        }
        catch (UnknownHostException uhExc) {
            wkrClientInfo = new WorkerClientInfo(null, null, ClientTypeMajor.API, ClientTypeMinor.API_UnitTest);
        }
    }

    @Test
    public void testWorkerPool() throws InterruptedException {

        final int maxNumWorkers = 8;
        final long workerTimeout = 4 * 1000;

        WorkerPool wp = new WorkerPoolImpl(maxNumWorkers, workerTimeout);

        assertEquals(wp.getNumAvailableSlots(), maxNumWorkers);
        assertEquals(wp.getWorkerTimeout(), workerTimeout);

        /* Register a new worker */
        String wkrId = wp.registerWorker(new ConcreteWorker(), wkrClientInfo);

        assertEquals(wp.getNumAvailableSlots(), maxNumWorkers - 1);

        /* Wait for a period of time that shouldn't lead to expiration */
        Thread.sleep(workerTimeout / 2);

        /* Verify that getWorkersInfo works */
        Map<String, WorkerClientInfo> workersInfo = wp.getWorkersClientInfo();
        assertEquals(workersInfo.size(), 1);
        assertNotNull(workersInfo.get(wkrId));
        assertEquals(workersInfo.get(wkrId).getClientIPAddress(), wkrClientInfo.getClientIPAddress());
        assertEquals(workersInfo.get(wkrId).getClientHostName(), wkrClientInfo.getClientHostName());

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
        String[] workerIds = new String[maxNumWorkers];

        for (int i = 0; i < maxNumWorkers; ++i) {
            String newWkrId = wp.registerWorker(new ConcreteWorker(), wkrClientInfo);
            workerIds[i] = newWkrId;

            assertNotNull(newWkrId);

            int numSlots = wp.getNumAvailableSlots();
            assertEquals(numSlots, maxNumWorkers - 1 - i);
        }

        /* If you attempt to register a new worker now, it should lead to failure */
        String newWkrId1 = wp.registerWorker(new ConcreteWorker(), wkrClientInfo);
        assertNull(newWkrId1);

        /* Remove a worker */
        assertEquals(0, wp.getNumNormallyRemovedWorkers());

        for (int i = 0; i < 4; ++i) {
            wp.removeWorker(workerIds[i]);
            assertEquals(i + 1, wp.getNumNormallyRemovedWorkers());
        }

        /* Wait for all workers to expire */
        Thread.sleep(workerTimeout + 100);
        purgeList = wp.purge();
        assertNotNull(purgeList);
        assertEquals(purgeList.size(), maxNumWorkers - wp.getNumNormallyRemovedWorkers());
    }
}
