package org.xbib.elasticsearch.gatherer;

import org.elasticsearch.ElasticSearchException;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.ClusterService;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.plugins.AbstractPlugin;
import org.xbib.elasticsearch.gatherer.state.GathererState;

import java.io.IOException;

/**
 * A base class for gatherers that provides the most used functions.
 */
public abstract class AbstractGatherer extends AbstractPlugin implements Gatherer {

    protected final ESLogger logger = ESLoggerFactory.getLogger("gatherer");

    protected Settings settings;

    protected ClusterService clusterService;

    protected Client client;

    protected GathererState state;

    private BulkProcessor bulkProcessor;

    private int bulkActions;

    private ByteSizeValue bulkSize;

    private TimeValue flushInterval;

    private int concurrentRequests;

    public AbstractGatherer() {
        super();
    }

    public Gatherer start() throws ElasticSearchException {
        //this.logger = Loggers.getLogger(getClass(), settings.globalSettings(), riverName);
        this.bulkActions = settings.getAsInt("bulk_actions", 1000);
        this.bulkSize = settings.getAsBytesSize("bulk_size", new ByteSizeValue(5, ByteSizeUnit.MB));
        this.flushInterval = settings.getAsTime("flush_interval", TimeValue.timeValueSeconds(5));
        this.concurrentRequests = settings.getAsInt("concurrent_requests", 4);
        bulkProcessor = BulkProcessor.builder(client, new BulkListener())
                .setBulkActions(bulkActions)
                .setBulkSize(bulkSize)
                .setFlushInterval(flushInterval)
                .setConcurrentRequests(concurrentRequests)
                .build();
        return this;
    }


    /**
     * Wait for all pending jobs
     * @throws GathererException
     */
    public void waitForPendingJobs() throws GathererException {
        // TODO
    }

    /**
     * Suspend gathering. All current jobs are halted.
     * @throws GathererException
     */
    public void suspend() throws GathererException {
        // TODO
    }

    /**
     * Resume gathering. Halted jobs are restarted.
     * @throws GathererException
     */
    public void resume() throws GathererException {
        // TODO
    }

    /**
     * Close gatherer
     * @throws java.io.IOException
     */
    public void close() throws IOException {
        // TODO
    }

    /**
     * The shutdown is called when this gatherer is about to be removed from the system.
     * @throws IOException
     */
    public void shutdown() throws IOException {
        // TODO
    }

    class BulkListener implements BulkProcessor.Listener {

        @Override
        public void beforeBulk(long executionId, BulkRequest request) {
            if (logger.isTraceEnabled()) {
                logger.trace("[{}] executing [{}]/[{}]", executionId, request.numberOfActions(), new ByteSizeValue(request.estimatedSizeInBytes()));
            }
        }

        @Override
        public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
            if (logger.isTraceEnabled()) {
                logger.trace("[{}] executed  [{}]/[{}], took [{}]", executionId, request.numberOfActions(), new ByteSizeValue(request.estimatedSizeInBytes()), response.getTook());
            }
            if (response.hasFailures()) {
                logger.warn("[{}] failed to execute bulk request: {}", executionId, response.buildFailureMessage());
            }
        }

        @Override
        public void afterBulk(long executionId, BulkRequest request, Throwable e) {
            logger.warn("[{}] failed to execute bulk request", e, executionId);
        }
    }
}
