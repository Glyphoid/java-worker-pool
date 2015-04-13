package me.scai.utilities;

import java.util.List;
import java.util.Date;

/**
 * Created by scai on 4/11/2015.
 */
public interface WorkerPool {
    /* Register new worker
     *
     * @param
     * @return   identifier (UUID) of the newly created worker, if successful.
     *           null, if failed.
     */
    public String registerWorker(PooledWorker worker);

    /* Remove a worker specified by worker ID
     *
     * @param     identifier (UUID) of the worker to delete
     */
    public void removeWorker(String workerId);

    /* Clear all workers in the pool */
    public void clearWorkers();

    /* Get a worker by ID
     *
     * @param     identifier (UUID) of the worker to get
     * @return    worker (reference)
     */
    public PooledWorker getWorker(String workerId);

    /* Update timestamp */
    public void updateWorkerTimestamp(String workerId, Date timestamp);

    /* Purge: check the status of all workers, remove ones that have expired
     *
     *
     * @return    List of the workers that have been purged
     * */
    public List<String> purge();

    /* Get the current number of workers
     *
     * @return    The current number of workers
     */
    public int getCurrNumWorkers();

    /* Get number of available slots
     *
     * @return    Current number of available seats: max number of workers - current number of workers
     */
    public int getNumAvailableSlots();

    /* Get the maximum number of workers */
    public int getMaxNumWorkers();

    /* Set the maximum number of workers */
    public void setMaxNumWorkers(int tMaxNumWorkers);

    /* Get worker timeout */
    public long getWorkerTimeout();

    /* Set worker timeout */
    public void setWorkerTimeout(long timeoutMillis);


}
