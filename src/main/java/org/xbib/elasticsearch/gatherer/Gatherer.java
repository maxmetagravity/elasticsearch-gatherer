package org.xbib.elasticsearch.gatherer;

import org.elasticsearch.plugins.Plugin;
import org.xbib.elasticsearch.gatherer.job.Job;
import org.xbib.elasticsearch.gatherer.job.JobExecutionListener;

import java.io.IOException;

public interface Gatherer extends Plugin {

    /**
     * The name of this Gatherer
     * @return the name of the gatherer
     */
    String name();

    /**
     * Perform asynchronous execution of a job
     * @param job the job
     * @param listener a job execution listener with the result of the execution
     * @throws GathererException
     */
    void execute(Job job, JobExecutionListener listener) throws GathererException;

    /**
     * Get number of pending jobs
     * @return the number of pending jobs
     */
    int getPendingJobs();

    /**
     * Wait for all pending jobs
     * @throws GathererException
     */
    void waitForPendingJobs() throws GathererException;

    /**
     * Suspend gathering. All current jobs are halted.
     * @throws GathererException
     */
    void suspend() throws GathererException;

    /**
     * Resume gathering. Halted jobs are restarted.
     * @throws GathererException
     */
    void resume() throws GathererException;

    /**
     * Close gatherer
     * @throws IOException
     */
    void close() throws IOException;

    /**
     * The shutdown is called when this gatherer is about to be removed from the system.
     * @throws IOException
     */
    void shutdown() throws IOException;

}
