package net.digitaledge.watchfile;

import java.util.Collection;

import org.elasticsearch.common.component.LifecycleComponent;
import org.elasticsearch.common.inject.Module;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.rest.RestModule;

import com.google.common.collect.Lists;

import net.digitaledge.watchfile.module.watchfileModule;
import net.digitaledge.watchfile.rest.watchfileRestAction;
import net.digitaledge.watchfile.service.watchfileService;

public class watchfilePlugin extends Plugin {
    @Override
    public String name() {
        return "watchfilePlugin";
    }

    @Override
    public String description() {
        return "This is a watchfile plugin.";
    }

    public void onModule(final RestModule module) {
        module.addRestAction(watchfileRestAction.class);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Collection<Class<? extends LifecycleComponent>> nodeServices() {
        final Collection<Class<? extends LifecycleComponent>> services = Lists.newArrayList();
        services.add(watchfileService.class);
        return services;
    }
}
