package org.xbib.elasticsearch.gatherer;

import org.elasticsearch.common.collect.ImmutableMap;
import org.elasticsearch.common.collect.Maps;
import org.elasticsearch.common.inject.AbstractModule;
import org.elasticsearch.common.inject.Module;
import org.elasticsearch.common.inject.Modules;
import org.elasticsearch.common.inject.SpawnModules;
import org.elasticsearch.common.settings.Settings;

import java.util.Collection;
import java.util.Map;

import static org.elasticsearch.common.collect.Lists.newLinkedList;

public class GathererModule extends AbstractModule implements SpawnModules {

    private final Settings settings;

    private Map<String, Class<? extends Module>> gathererModules = Maps.newHashMap();

    public GathererModule(Settings settings) {
        this.settings = settings;
    }

    @Override
    protected void configure() {
        bind(GathererService.class).asEagerSingleton();
        bind(GathererRegistry.class).toInstance(new GathererRegistry(gathererModules));
    }

    /**
     * Registers a custom gatherer module.
     *
     * @param name the name
     * @param module he module
     */
    public void registerGathererModule(String name, Class<? extends Module> module) {
        gathererModules.put(name, module);
    }

    @Override
    public Iterable<? extends Module> spawnModules() {
        Collection<Module> modules = newLinkedList();
        for (Map.Entry<String, Class<? extends Module>> me : gathererModules.entrySet()) {
            modules.add(Modules.createModule(me.getValue(), settings));
        }
        return modules;
    }
}

