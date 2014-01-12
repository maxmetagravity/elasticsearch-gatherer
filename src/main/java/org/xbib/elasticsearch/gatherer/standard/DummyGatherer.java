package org.xbib.elasticsearch.gatherer.standard;

import org.xbib.elasticsearch.gatherer.AbstractGatherer;
import org.xbib.elasticsearch.gatherer.GathererException;
import org.xbib.elasticsearch.gatherer.job.JobExecutionListener;
import org.xbib.elasticsearch.gatherer.job.Job;

public class DummyGatherer extends AbstractGatherer {

    public DummyGatherer() {
    }

    @Override
    public String name() {
        return "dummy-gatherer";
    }

    @Override
    public String description() {
        return "Dummy gatherer";
    }

    public void execute(Job job, JobExecutionListener listener) throws GathererException {
        logger.info("Hello world, I'm executing a job");
    }

    public int getPendingJobs() {
        return 0;
    }

}
