package net.digitaledge.watchfile.service;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.common.component.AbstractLifecycleComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;

import net.digitaledge.watchfile.WatchFileWorker;

public class watchfileService extends AbstractLifecycleComponent<watchfileService> {

	private Thread thread;
	
    @Inject
    public watchfileService(final Settings settings) {
        super(settings);
        logger.info("CREATE watchfileService");

        // TODO Your code..
    }

    @Override
    protected void doStart() throws ElasticsearchException {
        logger.info("START watchfileService");
        WatchFileWorker worker = new WatchFileWorker(settings, logger);
        logger.info("watchfileService  starting");
        thread = new Thread(worker);
        thread.start();
        logger.info("watchfileService  started");
    }

    @Override
    protected void doStop() throws ElasticsearchException {
        logger.info("Stopping watchalertService");

        if(thread == null)
        {
        	logger.debug("thread is null");
            return;
        }
        try {
        	logger.debug("Shutting down thread");
        	if(thread.isAlive())
        		thread.stop();
        
        	thread.destroy();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    protected void doClose() throws ElasticsearchException {
        logger.info("CLOSE watchfileService");

        // TODO Your code..
    }

}
