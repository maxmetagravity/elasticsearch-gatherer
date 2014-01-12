package org.xbib.elasticsearch.gatherer.state;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.List;

import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.ClusterChangedEvent;
import org.elasticsearch.cluster.ClusterService;
import org.elasticsearch.cluster.ClusterStateListener;
import org.elasticsearch.common.collect.ImmutableList;
import org.elasticsearch.common.collect.ImmutableList.Builder;
import org.elasticsearch.common.component.AbstractComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.monitor.jvm.JvmInfo;
import org.elasticsearch.transport.Transport;
import org.elasticsearch.transport.TransportInfo;
import org.xbib.elasticsearch.gatherer.job.JobEvent;

import static org.elasticsearch.common.xcontent.ToXContent.EMPTY_PARAMS;
import static org.elasticsearch.common.xcontent.XContentFactory.xContent;
import static org.elasticsearch.common.xcontent.XContentParser.Token.END_ARRAY;
import static org.elasticsearch.common.xcontent.XContentType.JSON;

public class GathererState extends AbstractComponent implements ClusterStateListener {

    public static final String PLUGIN_GATHERER_STATE = "plugin.gatherer.state";

    private final Client client;

    private final ClusterService clusterService;

    private final String gathererId;

    @Inject
    public GathererState(Settings settings, Client client,
                         ClusterService clusterService,
                         Transport transport) {
        super(settings);
        this.client = client;
        this.clusterService = clusterService;
        this.gathererId =  transport.boundAddress() != null ?
            "gatherer-" + transport.boundAddress().toString() :
            "gatherer-" + clusterService.localNode().getId();
    }

    public String getId() {
        return gathererId;
    }

    public List<JobEvent> get() throws IOException {
        return get(PLUGIN_GATHERER_STATE);
    }

    public void add(JobEvent status) throws IOException {
        add(PLUGIN_GATHERER_STATE, get(), status);
    }

    public void remove(JobEvent status) throws IOException {
        remove(PLUGIN_GATHERER_STATE, get(), status);
    }

    public void update(JobEvent status) throws IOException {
        update(PLUGIN_GATHERER_STATE, get(), status);
    }

    private List<JobEvent> get(String name) throws IOException {
        return parseSetting(getSetting(name));
    }

    private void add(String name, List<JobEvent> values, JobEvent status) throws IOException {
        String value = generateSetting(ImmutableList.<JobEvent>builder()
                .addAll(values)
                .add(status)
                .build());
        updateSetting(name, value);
    }

    private void remove(String name, List<JobEvent> values, JobEvent status) throws IOException {
        Builder<JobEvent> updatedValues = ImmutableList.builder();
        for (JobEvent value : values) {
            if (!value.equals(status)) {
                updatedValues.add(value);
            }
        }
        String value = generateSetting(updatedValues.build());
        updateSetting(name, value);
    }

    private void update(String name, List<JobEvent> values, JobEvent status) throws IOException {
        Builder<JobEvent> updatedValues = ImmutableList.builder();
        for (JobEvent value : values) {
            if (value.equals(status)) {
                updatedValues.add(status);
            } else {
                updatedValues.add(value);
            }
        }
        String value = generateSetting(updatedValues.build());
        updateSetting(name, value);
    }

    private Settings getSettings() {
        return clusterService.state().getMetaData().persistentSettings();
    }

    private String getSetting(String name) {
        return getSettings().get(name, "[]");
    }

    private void updateSetting(String name, String value) {
        client.admin().cluster().prepareUpdateSettings()
                .setPersistentSettings(ImmutableSettings.builder()
                        .put(getSettings())
                        .put(name, value)
                        .build())
                .execute()
                .actionGet();
    }

    private List<JobEvent> parseSetting(String value) throws IOException {
        XContentParser parser = xContent(JSON).createParser(value);
        Builder<JobEvent> builder = ImmutableList.builder();
        parser.nextToken();
        while (parser.nextToken() != END_ARRAY) {
            JobEvent status = new JobEvent();
            builder.add(status.fromXContent(parser));
        }
        return builder.build();
    }

    private String generateSetting(List<JobEvent> values) throws IOException {
        XContentBuilder builder = XContentFactory.jsonBuilder();
        builder.startArray();
        for (JobEvent value : values) {
            value.toXContent(builder, EMPTY_PARAMS);
        }
        builder.endArray();
        return builder.string();
    }

    @Override
    public void clusterChanged(ClusterChangedEvent event) {
        logger.info("cluster changed {}", event.state().prettyPrint());

    }
}