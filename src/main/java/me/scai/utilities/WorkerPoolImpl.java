package me.scai.utilities;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import java.util.UUID;

/**
 * Created by scai on 4/11/2015.
 */
public class WorkerPoolImpl implements WorkerPool {
    /* Member variables */
    private AtomicInteger maxNumWorkers = new AtomicInteger();  /* The maximum number of workers */
    private AtomicLong workerTimeoutMillis = new AtomicLong();  /* Timeout for workers */

    private Map<String, PooledWorker> workers = new ConcurrentHashMap<>();
    private Map<String, WorkerClientInfo> workersClientInfo = new ConcurrentHashMap<>();
    private Map<String, Date> workerCreatedTimestamps = new ConcurrentHashMap<>(); /* Time stamps of creation */
    private Map<String, Date> workerTimestamps = new ConcurrentHashMap<>(); /* Time stamps of last usage */

    private Map<String, String> previousWorkerIds = new ConcurrentHashMap<>();

    private Map<String, Integer> workerMessageCounts = new ConcurrentHashMap<>();

    /* For keeping track of purged workers */
    private ConcurrentLinkedDeque<String> purgedWorkers = new ConcurrentLinkedDeque<>();
//    private Map<String, PooledWorker> purgedWorkers = new ConcurrentHashMap<>();
    private Map<String, WorkerClientInfo> purgedWorkersInfo = new ConcurrentHashMap<>();
    private Map<String, Date> purgedTimestamps = new ConcurrentHashMap<>();

    private ConcurrentLinkedDeque<String> nonPurgeRemovedWorkers = new ConcurrentLinkedDeque<>();
//    private Map<String, PooledWorker> nonPurgeRemovedWorkers = new ConcurrentHashMap<>();
    private Map<String, WorkerClientInfo> nonPurgeRemovedWorkersInfo = new ConcurrentHashMap<>();
    private Map<String, Date> nonPurgeRemovedTimestamps = new ConcurrentHashMap<>();

    private int numEverCreatedWorkers = 0;
    private int numNormallyRemovedWorkers = 0; /* Number of workers that have been removed normally (not purged) */

    /* Constructor */
    public WorkerPoolImpl(final int tMaxNumWorkers, final long tWorkerTimeoutMillis) {
        maxNumWorkers.set(tMaxNumWorkers);
        workerTimeoutMillis.set(tWorkerTimeoutMillis);
    }

    @Override
    /* @return    Worker ID (UUID) if registration is successful
     *            null if registration is unsuccessful ()
     */
    public synchronized String registerWorker(PooledWorker wkr, WorkerClientInfo wkrClientInfo) {
        purge();

        String newWkrId = null;
        if (getNumAvailableSlots() > 0) {
            newWkrId = UUID.randomUUID().toString();

            workers.put(newWkrId, wkr);
            workersClientInfo.put(newWkrId, wkrClientInfo);

            workerMessageCounts.put(newWkrId, 0);

            Date now = new Date();
            workerCreatedTimestamps.put(newWkrId, now);
            workerTimestamps.put(newWkrId, now);         /* Initial timestamp is the time of registration */

            numEverCreatedWorkers ++;
        }

        return newWkrId;
    }


    @Override
    public void setPreviousWorkerId(String workerId, String prevWorkerId) {
        if ( !workers.containsKey(workerId) ) {
            throw new IllegalArgumentException("Invalid worker ID: \"" + workerId + "\"");
        }

        previousWorkerIds.put(workerId, prevWorkerId);
    }


    @Override
    public String getPreviousWorkerId(String workerId) {
        if ( !workers.containsKey(workerId) ) {
            throw new IllegalArgumentException("Invalid worker ID: \"" + workerId + "\"");
        }

        if (!previousWorkerIds.containsKey(workerId)) {
            return null;
        } else {
            return previousWorkerIds.get(workerId);
        }
    }

    private synchronized void removeWorker(String wkrId, boolean isPurge) {
        /* Actually perform the purge */
        if (workers.containsKey(wkrId)) {
            /* Book-keeping */
            WorkerClientInfo purgedClientInfo = workersClientInfo.get(wkrId);

            /* Do not preserve obsolete reference, so that the worker can be garbage-collected */
            if (isPurge) {
                purgedWorkers.add(wkrId);
                purgedWorkersInfo.put(wkrId, purgedClientInfo);
                purgedTimestamps.put(wkrId, new Date());
            } else {
                nonPurgeRemovedWorkers.add(wkrId); /* So that the worker can be garbage-collected */
                nonPurgeRemovedWorkersInfo.put(wkrId, purgedClientInfo);
                nonPurgeRemovedTimestamps.put(wkrId, new Date());

                numNormallyRemovedWorkers++;
            }

            workers.remove(wkrId);
            workersClientInfo.remove(wkrId);
            workerCreatedTimestamps.remove(wkrId);
            workerTimestamps.remove(wkrId);

            if (previousWorkerIds.containsKey(wkrId)) {
                previousWorkerIds.remove(wkrId);
            }

        }


    }

    @Override
    public synchronized void removeWorker(String wkrId) {
        removeWorker(wkrId, false); /* The public-interface function is for non-purge (user) removal of workers */
    }

    @Override
    public synchronized void clearWorkers() {
        workers.clear();
        workersClientInfo.clear();
        workerCreatedTimestamps.clear();
        workerTimestamps.clear();
    }

    @Override
    public PooledWorker getWorker(String workerId) {
        if ( !workers.containsKey(workerId) ) {
            throw new IllegalArgumentException("Invalid worker ID: \"" + workerId + "\"");
        }

        return workers.get(workerId);
    }

    @Override
    public synchronized void updateWorkerTimestamp(String workerId, Date timestamp) {
        if (workers.containsKey(workerId)) {
            workerTimestamps.put(workerId, timestamp);
        }
    }

    @Override
    public synchronized int getNumAvailableSlots() {
        return maxNumWorkers.get() - workers.size();
    }

    @Override
    public synchronized List<String> purge() {
        List<String> purgeList = new ArrayList<>();

        long timeout = workerTimeoutMillis.get();
        Date nowDate = new Date();
        long now = nowDate.getTime();
        for (Map.Entry<String, PooledWorker> wkrEntry : workers.entrySet()) {
            String wkrId = wkrEntry.getKey();
            PooledWorker wkr = wkrEntry.getValue();

            long ts = workerTimestamps.get(wkrId).getTime();

            if (now - ts > timeout) {
                removeWorker(wkrId, true);

                purgeList.add(wkrId);
            }
        }

        if (purgeList.isEmpty()) {
            return null;
        }
        else {
            return purgeList;
        }
    }

    @Override
    public synchronized int getCurrNumWorkers() {
        return workers.size();
    }

    @Override
    public synchronized int getMaxNumWorkers() {
        return maxNumWorkers.get();
    }

    @Override
    public synchronized void setMaxNumWorkers(int tMaxNumWorkers) {
        maxNumWorkers.set(tMaxNumWorkers);
    }

    @Override
    public synchronized long getWorkerTimeout() {
        return workerTimeoutMillis.get();
    }

    @Override
    public synchronized void setWorkerTimeout(long timeoutMillis) {
        workerTimeoutMillis.set(timeoutMillis);
    }

    @Override
    public synchronized Map<String, WorkerClientInfo> getWorkersClientInfo() {
        purge();

        return workersClientInfo;
    }

    @Override
    public synchronized Date getCreatedTimestamp(String workerId) {
        return workerCreatedTimestamps.get(workerId);
    }

    @Override
    public synchronized Date getLastUseTimestamp(String workerId) {
        return workerTimestamps.get(workerId);
    }


    @Override
    public synchronized int getNumEverCreatedWorkers() {
        return numEverCreatedWorkers;
    }

    @Override
    public synchronized int getNumPurgedWorkers() {
        return purgedWorkers.size();
    }

    @Override
    public synchronized int getNumNormallyRemovedWorkers() {
        return numNormallyRemovedWorkers;
    }

    @Override
    public synchronized int incrementMessageCount(String workerId) {
        if (!workerMessageCounts.containsKey(workerId)) {
            throw new IllegalArgumentException("Invalid worker ID: \"" + workerId + "\"");
        }

        int c = workerMessageCounts.get(workerId);
        workerMessageCounts.put(workerId, ++c);

        return c;
    }

    @Override
    public synchronized int getMessageCount(String workerId) {
        if (!workerMessageCounts.containsKey(workerId)) {
            throw new IllegalArgumentException("Invalid worker ID: \"" + workerId + "\"");
        }

        return workerMessageCounts.get(workerId);
    }

    @Override
    public synchronized float getCurrentAverageMessageRate(String workerId) {
        if (!workerMessageCounts.containsKey(workerId)) {
            throw new IllegalArgumentException("Invalid worker ID: \"" + workerId + "\"");
        }

        final long t  = new Date().getTime();
        final long t0 = workerCreatedTimestamps.get(workerId).getTime();

        return ((float) workerMessageCounts.get(workerId)) / ((float) (t - t0)) * 1000.0f;
    }


    @Override
    public synchronized float getEffectiveAverageMessageRate(String workerId) {
        if (!workerMessageCounts.containsKey(workerId)) {
            throw new IllegalArgumentException("Invalid worker ID: \"" + workerId + "\"");
        }

        final long t1 = getLastUseTimestamp(workerId).getTime();
        final long t0 = workerCreatedTimestamps.get(workerId).getTime();

        return ((float) workerMessageCounts.get(workerId)) / ((float) (t1 - t0)) * 1000.0f;
    }

}
