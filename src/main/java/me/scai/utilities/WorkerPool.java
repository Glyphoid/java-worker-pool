package me.scai.utilities;

import java.util.List;
import java.util.Date;
import java.util.Map;

/**
 * Created by scai on 4/11/2015.
 */
public interface WorkerPool {
    public String registerWorker(PooledWorker wkr, WorkerClientInfo wkrInfo);

    /**
     * Set previous worker ID, for situations of "relayed" worker
     *
     * @param workerId
     * @param prevWorkerId
     *
     */
    public void setPreviousWorkerId(String workerId, String prevWorkerId);

    /**
     * Get previous worker ID
     *
     * @param workerId
     * @return
     */
    public String getPreviousWorkerId(String workerId);

    /* Register new worker
     *
     * @return   identifier (UUID) of the newly created worker, if successful.
     *           null, if failed.
     */

    /* Remove a worker specified by worker ID
     *
     * @param    identifier (UUID) of the worker to delete
     */
    public void removeWorker(String workerId);

    /* Increment worker message count
     *
     * @param    worker ID
     * @return   current message count after the incrementing
     * @throws   IllegalArgumentException on invalid worker ID
     */
    public int incrementMessageCount(String workerId);

    /* Get the current message count
     *
     * @param    worker ID
     * @return   current message count
     * @throws   IllegalArgumentException on invalid worker ID
     */
    public int getMessageCount(String workerId);

    /* Get the current average message rate, defined as n_M / (t - t0)
     *     where in n_M is the current total number of messages, t is the current time,
     *     and t0 is the time of worker registration
     *
     * @param    worker ID
     * @return   current average message rate (s ^ -1)
     * @throws   IllegalArgumentException on invalid worker ID
     */
    public float getCurrentAverageMessageRate(String workerId);

    /* Get the current average message rate, defined as n_M / (t - t0)
     *     where in n_M is the current total number of messages, t is the current time,
     *     and t0 is the time of worker registration
     *
     * @param    worker ID
     * @return   current average message rate (s ^ -1)
     * @throws   IllegalArgumentException on invalid worker ID
     */
    public float getEffectiveAverageMessageRate(String workerId);

    /* Clear all workers in the pool */
    public void clearWorkers();

    /* Get a worker by ID
     *
     * @param     identifier (UUID) of the worker to get
     * @return    worker (reference)
     * @throws    IllegalArgumentException if worker ID is invalid
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

    /* Get the total number of workers ever registered
    *
    * @return   Total number of workers ever registered
    * */
    public int getNumEverCreatedWorkers();

    /* Get the total number of workers that have been purged
    *
    * @return   Number of workers that have been purged
    * */
    public int getNumPurgedWorkers();

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

    /* Get list of workers */
    public Map<String, WorkerClientInfo> getWorkersClientInfo();

    /* Get timestamp of creation */
    public Date getCreatedTimestamp(String workerId);

    /* Get timestamp of last use */
    public Date getLastUseTimestamp(String workerId);

    /* Get number of normally removed workers */
    public int getNumNormallyRemovedWorkers();
}
