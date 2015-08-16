package me.scai.utilities;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;

import me.scai.utilities.clienttypes.ClientTypeMajor;
import me.scai.utilities.clienttypes.ClientTypeMinor;
import org.junit.Before;
import org.junit.Test;
import sun.jdbc.odbc.ee.PoolWorker;

import static org.junit.Assert.*;

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
    public void testInvalidWorkerId() {
        final String fakeWorkerId = "foo-bar-qux-unlikely-to-be";

        WorkerPool wp = new WorkerPoolImpl(8, 4L * 1000);

        boolean invalidWorkerIdExceptionThrown;

        /* Make sure that the right types of exceptions are thrown under invalid worker ID arguments */
        invalidWorkerIdExceptionThrown = false;
        try {
            PooledWorker pw = wp.getWorker(fakeWorkerId);
        } catch (IllegalArgumentException exc) {
            invalidWorkerIdExceptionThrown = true;
        }
        assertTrue(invalidWorkerIdExceptionThrown);

        invalidWorkerIdExceptionThrown = false;
        try {
            int mc = wp.getMessageCount(fakeWorkerId);
        } catch (IllegalArgumentException exc) {
            invalidWorkerIdExceptionThrown = true;
        }
        assertTrue(invalidWorkerIdExceptionThrown);

        invalidWorkerIdExceptionThrown = false;
        try {
            wp.incrementMessageCount(fakeWorkerId);
        } catch (IllegalArgumentException exc) {
            invalidWorkerIdExceptionThrown = true;
        }
        assertTrue(invalidWorkerIdExceptionThrown);

        invalidWorkerIdExceptionThrown = false;
        try {
            float amr = wp.getCurrentAverageMessageRate(fakeWorkerId);
        } catch (IllegalArgumentException exc) {
            invalidWorkerIdExceptionThrown = true;
        }
        assertTrue(invalidWorkerIdExceptionThrown);

        invalidWorkerIdExceptionThrown = false;
        try {
            float emr = wp.getEffectiveAverageMessageRate(fakeWorkerId);
        } catch (IllegalArgumentException exc) {
            invalidWorkerIdExceptionThrown = true;
        }
        assertTrue(invalidWorkerIdExceptionThrown);


    }

    @Test
    public void testWorkerPool() throws InterruptedException {

        final int maxNumWorkers = 8;
        final long workerTimeout = 4L * 1000;

        WorkerPool wp = new WorkerPoolImpl(maxNumWorkers, workerTimeout);

        assertEquals(wp.getNumAvailableSlots(), maxNumWorkers);
        assertEquals(wp.getWorkerTimeout(), workerTimeout);

        /* Register a new worker */
        String wkrId = wp.registerWorker(new ConcreteWorker(), wkrClientInfo);

        /* Verify that available slots has decreased by 1 */
        assertEquals(wp.getNumAvailableSlots(), maxNumWorkers - 1);

        /* Verify that the initial message count is zero */
        assertEquals(0, wp.getMessageCount(wkrId));

        /* Increment and verify message count */
        for (int i = 0; i < 10; ++i) {
            wp.incrementMessageCount(wkrId);
            assertEquals(i + 1, wp.getMessageCount(wkrId));
        }

        assertTrue(wp.getCurrentAverageMessageRate(wkrId) > 0.0f);
        assertTrue(wp.getEffectiveAverageMessageRate(wkrId) > 0.0f);

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

    @Test
    public void testPreviousWorkerId() {
        WorkerPool wp = new WorkerPoolImpl(8, 4000L);

        String wkrId = wp.registerWorker(new ConcreteWorker(), wkrClientInfo);

        // Previous worker ID has not been set yet
        assertNull(wp.getPreviousWorkerId(wkrId));

        // Set and verify previous worker ID
        final String prevWorkId = wkrId + "_prev";
        wp.setPreviousWorkerId(wkrId, wkrId + "_prev");

        assertEquals(prevWorkId, wp.getPreviousWorkerId(wkrId));

        // Remove worker ID and then getPreviousWorkerId should throw IllegalArgumentException
        wp.removeWorker(wkrId);

        boolean exceptionThrown = false;
        try {
            String prev = wp.getPreviousWorkerId(wkrId);
        } catch (IllegalArgumentException exc) {
            exceptionThrown = true;
        }

        assertTrue(exceptionThrown);

    }
}
