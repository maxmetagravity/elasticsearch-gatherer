
package org.xbib.elasticsearch.plugin.gatherer;

import org.elasticsearch.action.ActionModule;
import org.elasticsearch.cluster.settings.ClusterDynamicSettingsModule;
import org.elasticsearch.common.component.LifecycleComponent;
import org.elasticsearch.common.inject.Module;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.plugins.AbstractPlugin;
import org.elasticsearch.rest.RestModule;
import org.xbib.elasticsearch.action.deploy.DeployAction;
import org.xbib.elasticsearch.action.deploy.DeployService;
import org.xbib.elasticsearch.action.deploy.TransportDeployAction;
import org.xbib.elasticsearch.action.gatherer.GathererAction;
import org.xbib.elasticsearch.action.gatherer.TransportGathererAction;
import org.xbib.elasticsearch.gatherer.GathererModule;
import org.xbib.elasticsearch.gatherer.GathererService;
import org.xbib.elasticsearch.gatherer.state.GathererState;
import org.xbib.elasticsearch.rest.action.deploy.RestDeployAction;
import org.xbib.elasticsearch.rest.action.gatherer.RestGathererAction;

import java.util.Collection;

import static org.elasticsearch.common.collect.Lists.newArrayList;

/**
 * The gatherer plugin is initialized at node startup by Elasticsearch
 */
public class GathererPlugin extends AbstractPlugin {

    public final static String NAME = "gatherer";

    private final Settings settings;

    public GathererPlugin(Settings settings) {
        this.settings = settings;
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public String description() {
        return "Gatherer plugin";
    }

    @Override
    public Collection<Class<? extends Module>> modules() {
        Collection<Class<? extends Module>> modules = newArrayList();
        if (settings.getAsBoolean("gatherer.enabled", true)) {
            modules.add(GathererModule.class);
            //modules.add(DeployModule.class);
        }
        return modules;
    }

    @Override
    public Collection<Class<? extends LifecycleComponent>> services() {
        Collection<Class<? extends LifecycleComponent>> services = newArrayList();
        if (settings.getAsBoolean("gatherer.enabled", true)) {
            services.add(DeployService.class);
            services.add(GathererService.class);
        }
        return services;
    }

    @Override
    public Settings additionalSettings() {
        return ImmutableSettings.Builder.EMPTY_SETTINGS;
    }

    public void onModule(ActionModule module) {
        module.registerAction(DeployAction.INSTANCE, TransportDeployAction.class);
        module.registerAction(GathererAction.INSTANCE, TransportGathererAction.class);
    }

    public void onModule(ClusterDynamicSettingsModule module) {
        module.addDynamicSettings(GathererState.PLUGIN_GATHERER_STATE);
    }

    public void onModule(RestModule module) {
        module.addRestAction(RestDeployAction.class);
        module.addRestAction(RestGathererAction.class);
    }
}
