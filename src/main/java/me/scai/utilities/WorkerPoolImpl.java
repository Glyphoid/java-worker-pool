package me.scai.utilities;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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

    private Map<String, PooledWorker> workers  = new ConcurrentHashMap<>();
    private Map<String, Date> workerTimestamps = new ConcurrentHashMap<>();

    /* For keeping track of purged workers */
    private Map<String, PooledWorker> purgedWorkers = new ConcurrentHashMap<>();
    private Map<String, Date> purgedTimestamps = new ConcurrentHashMap<>();


    /* Constructor */
    public WorkerPoolImpl(final int tMaxNumWorkers, final long tWorkerTimeoutMillis) {
        maxNumWorkers.set(tMaxNumWorkers);
        workerTimeoutMillis.set(tWorkerTimeoutMillis);
    }

    @Override
    /* @return    Worker ID (UUID) if registration is successful
     *            null if registration is unsuccessful ()
     */
    public synchronized String registerWorker(PooledWorker wkr) {
        purge();

        String newWkrId = null;
        if (getNumAvailableSlots() > 0) {
            newWkrId = UUID.randomUUID().toString();

            workers.put(newWkrId, wkr);
            workerTimestamps.put(newWkrId, new Date()); /* Initial timestamp is the time of registration */
        }

        return newWkrId;
    }

    @Override
    public synchronized void removeWorker(String wkrId) {
        if (workers.containsKey(wkrId)) {
            workers.remove(wkrId);
            workerTimestamps.remove(wkrId);
        }
    }

    @Override
    public synchronized void clearWorkers() {
        workers.clear();
        workerTimestamps.clear();
    }

    @Override
    public PooledWorker getWorker(String workerId) {
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
                workers.remove(wkrId);
                workerTimestamps.remove(wkrId);

                purgedWorkers.put(wkrId, wkr);
                purgedTimestamps.put(wkrId, nowDate);

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
    public int getCurrNumWorkers() {
        return workers.size();
    }

    @Override
    public int getMaxNumWorkers() {
        return maxNumWorkers.get();
    }

    @Override
    public void setMaxNumWorkers(int tMaxNumWorkers) {
        maxNumWorkers.set(tMaxNumWorkers);
    }

    @Override
    public long getWorkerTimeout() {
        return workerTimeoutMillis.get();
    }

    @Override
    public void setWorkerTimeout(long timeoutMillis) {
        workerTimeoutMillis.set(timeoutMillis);
    }

}
