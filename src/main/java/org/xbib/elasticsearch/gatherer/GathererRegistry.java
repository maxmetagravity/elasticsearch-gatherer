package org.xbib.elasticsearch.gatherer;

import org.elasticsearch.common.inject.Module;

import java.util.Map;

import static org.elasticsearch.common.collect.Maps.newHashMap;

/**
 * Registry for keeping all Gatherer modules and Gatherer instances
 */
public class GathererRegistry {

    private final Map<String, Class<? extends Module>> gathererModules;

    private final Map<String, Gatherer> gatherers;

    public GathererRegistry(Map<String, Class<? extends Module>> gathererModules) {
        this.gathererModules = gathererModules;
        this.gatherers = newHashMap();
    }

    public Class<? extends Module> gathererModule(String name) {
        return gathererModules.get(name);
    }

    public Map<String, Class<? extends Module>> getGathererModules() {
        return gathererModules;
    }

    public void addGatherer(String name, Gatherer gatherer) {
        gatherers.put(name, gatherer);
        for (Class<? extends Module> module : gatherer.modules()) {
            gathererModules.put(name, module);
        }
    }

    public Map<String, Gatherer> getGatherers() {
        return gatherers;
    }

    public String toString() {
        return gatherers.toString();
    }

}
