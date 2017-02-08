package net.digitaledge.watchfile.module;

import org.elasticsearch.common.inject.AbstractModule;

import net.digitaledge.watchfile.service.watchfileService;

public class watchfileModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(watchfileService.class).asEagerSingleton();
    }
}
