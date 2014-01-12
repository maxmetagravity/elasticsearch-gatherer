
package org.xbib.elasticsearch.gatherer.job;

public interface JobExecutionListener {

    void beforeBegin(Job job);

    void onSuccess(Job job);

    void onFailure(Job job);
}
