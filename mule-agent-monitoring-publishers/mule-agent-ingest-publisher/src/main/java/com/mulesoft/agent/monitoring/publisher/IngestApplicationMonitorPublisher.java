package com.mulesoft.agent.monitoring.publisher;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mulesoft.agent.AgentEnableOperationException;
import com.mulesoft.agent.configuration.PostConfigure;
import com.mulesoft.agent.domain.monitoring.Metric;
import com.mulesoft.agent.monitoring.publisher.ingest.builder.IngestApplicationMetricPostBodyBuilder;
import com.mulesoft.agent.monitoring.publisher.ingest.model.IngestApplicationMetricPostBody;
import com.mulesoft.agent.monitoring.publisher.ingest.model.IngestMetric;
import com.mulesoft.agent.monitoring.publisher.model.MetricClassification;
import com.mulesoft.agent.monitoring.publisher.model.MetricSample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * <p>
 * Handler that publishes Application Information information obtained from the Monitoring Service to a running Ingest API instance.
 * </p>
 */
@Singleton
@Named("mule.agent.ingest.application.metrics.internal.handler")
public class IngestApplicationMonitorPublisher extends IngestMonitorPublisher<Map<String, List<Metric>>>
{

    private final static Logger LOGGER = LoggerFactory.getLogger(IngestApplicationMonitorPublisher.class);

    private static final Long TIME_OUT_DEFAULT = 10000L;
    private static final TimeUnit TIME_UNIT_DEFAULT = TimeUnit.MILLISECONDS;

    private static final String MESSAGE_COUNT_NAME = "messageCount";
    private static final String RESPONSE_TIME_NAME = "responseTime";
    private static final String ERROR_COUNT_NAME = "errorCount";
    private static final List<String> keys = Lists.newArrayList(MESSAGE_COUNT_NAME, RESPONSE_TIME_NAME, ERROR_COUNT_NAME);

    private Long applicationPublishTimeOut;
    private TimeUnit applicationPublishTimeUnit;

    @Inject
    private IngestApplicationMetricPostBodyBuilder appMetricBuilder;
    private ExecutorService executor;

    @Override
    @PostConfigure
    public void postConfigurable() throws AgentEnableOperationException {
        super.postConfigurable();

        this.applicationPublishTimeOut = TIME_OUT_DEFAULT;
        String systemConfiguredTimeOut = System.getProperty("application.metric.publish.timeout");
        if (systemConfiguredTimeOut != null)
        {
            try {
                this.applicationPublishTimeOut = Long.valueOf(systemConfiguredTimeOut);
            } catch (NumberFormatException e)
            {
                LOGGER.warn("Received invalid value for application.metric.publish.timeout: " + systemConfiguredTimeOut);
            }
        }

        this.applicationPublishTimeUnit = TIME_UNIT_DEFAULT;
        String systemConfiguredTimeUnit = System.getProperty("application.metric.publish.timeout.unit");
        if (systemConfiguredTimeUnit != null)
        {
            try {
                this.applicationPublishTimeUnit = TimeUnit.valueOf(systemConfiguredTimeUnit);
            } catch (NumberFormatException e)
            {
                LOGGER.warn("application.metric.publish.timeout.unit: " + systemConfiguredTimeUnit);
            }
        }

        ThreadFactory threadFactory = new ThreadFactory()
        {
            @Override
            public Thread newThread(Runnable r)
            {
                Thread t = new Thread(r);
                t.setDaemon(true);
                t.setPriority(Thread.MIN_PRIORITY);
                return t;
            }
        };
        this.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), threadFactory);
    }

    private Map<String, IngestApplicationMetricPostBody> processApplicationMetrics(Collection<Map<String, List<Metric>>> collection)
    {
        Map<String, List<Metric>> metricsByApplicationName = Maps.newHashMap();

        for (Map<String, List<Metric>> metrics : collection) {
            for (Map.Entry<String, List<Metric>> entry : metrics.entrySet()) {
                List<Metric> processed = metricsByApplicationName.get(entry.getKey());
                if (processed == null)
                {
                    processed = Lists.newLinkedList();
                    metricsByApplicationName.put(entry.getKey(), processed);
                }
                processed.addAll(entry.getValue());
            }
        }

        Map<String, IngestApplicationMetricPostBody> bodies = Maps.newHashMap();
        Date now = new Date();

        for (Map.Entry<String, List<Metric>> entry : metricsByApplicationName.entrySet())
        {
            MetricClassification classification = new MetricClassification(keys, entry.getValue());
            IngestMetric cpuUsageSample = metricBuilder.build(new MetricSample(now, classification.getMetrics(MESSAGE_COUNT_NAME)));
            IngestMetric memoryUsageSample = metricBuilder.build(new MetricSample(now, classification.getMetrics(RESPONSE_TIME_NAME)));
            IngestMetric memoryTotalSample = metricBuilder.build(new MetricSample(now, classification.getMetrics(ERROR_COUNT_NAME)));
            bodies.put(entry.getKey(), appMetricBuilder.build(cpuUsageSample, memoryUsageSample, memoryTotalSample));
        }
        return bodies;
    }

    protected boolean send(Collection<Map<String, List<Metric>>> collection)
    {
        try
        {
            Map<String, IngestApplicationMetricPostBody> applicationBodies = this.processApplicationMetrics(collection);
            CountDownLatch latch = new CountDownLatch(applicationBodies.size());

            final List<Boolean> results = Lists.newLinkedList();
            for (final Map.Entry<String, IngestApplicationMetricPostBody> entry : applicationBodies.entrySet())
            {
                this.executor.submit(new Runnable() {
                    @Override
                    public void run() {
                        boolean result = IngestApplicationMonitorPublisher.this.client.postApplicationMetrics(entry.getKey(), entry.getValue());
                        results.add(result);
                    }
                });
            }
            latch.await(applicationPublishTimeOut, applicationPublishTimeUnit);

            return !results.contains(false);
        }
        catch (Exception e)
        {
            LOGGER.error("Could not publish application metrics to Ingest: ", e);
            return false;
        }
    }
}
