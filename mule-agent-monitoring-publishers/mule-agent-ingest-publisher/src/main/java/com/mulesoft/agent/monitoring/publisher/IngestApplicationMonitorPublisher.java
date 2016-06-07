package com.mulesoft.agent.monitoring.publisher;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mulesoft.agent.AgentEnableOperationException;
import com.mulesoft.agent.configuration.Configurable;
import com.mulesoft.agent.configuration.PostConfigure;
import com.mulesoft.agent.domain.monitoring.ApplicationMetrics;
import com.mulesoft.agent.domain.monitoring.GroupedApplicationsMetrics;
import com.mulesoft.agent.domain.monitoring.Metric;
import com.mulesoft.agent.monitoring.publisher.ingest.builder.IngestApplicationMetricPostBodyBuilder;
import com.mulesoft.agent.monitoring.publisher.ingest.model.IngestApplicationMetricPostBody;
import com.mulesoft.agent.monitoring.publisher.ingest.model.IngestMetric;
import com.mulesoft.agent.monitoring.publisher.model.IngestApplicationMetric;
import com.mulesoft.agent.monitoring.publisher.model.MetricClassification;
import com.mulesoft.agent.monitoring.publisher.model.MetricSample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.*;

/**
 * <p>
 * Handler that publishes Application Information information obtained from the Monitoring Service to a running Ingest API instance.
 * </p>
 */
@Singleton
@Named("mule.agent.ingest.application.metrics.internal.handler")
public class IngestApplicationMonitorPublisher extends IngestMonitorPublisher<GroupedApplicationsMetrics>
{

    private final static Logger LOGGER = LoggerFactory.getLogger(IngestApplicationMonitorPublisher.class);

    private static final String MESSAGE_COUNT_NAME = "messageCount";
    private static final String RESPONSE_TIME_NAME = "responseTime";
    private static final String ERROR_COUNT_NAME = "errorCount";

    private static final List<String> keys = Lists.newArrayList(MESSAGE_COUNT_NAME, RESPONSE_TIME_NAME, ERROR_COUNT_NAME);

    @Configurable("10000")
    private Long applicationPublishTimeOut;
    @Configurable("MILLISECONDS")
    private TimeUnit applicationPublishTimeUnit;

    @Inject
    private IngestApplicationMetricPostBodyBuilder appMetricBuilder;
    private ExecutorService executor;

    @Override
    @PostConfigure
    public void postConfigurable() throws AgentEnableOperationException {
        ThreadFactory threadFactory = new ThreadFactoryBuilder().setDaemon(true).setPriority(Thread.MIN_PRIORITY).setNameFormat("monitoring-application-publisher-%d").build();
        this.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), threadFactory);
    }

    private List<IngestApplicationMetric> processApplicationMetrics(Collection<GroupedApplicationsMetrics> collection)
    {
        Map<String, List<Metric>> metricsByApplicationName = Maps.newHashMap();

        for (GroupedApplicationsMetrics metrics : collection) {
            for (Map.Entry<String, ApplicationMetrics> entry : metrics.getMetricsByApplicationName().entrySet()) {
                List<Metric> processed = metricsByApplicationName.get(entry.getKey());
                if (processed == null)
                {
                    processed = Lists.newLinkedList();
                    metricsByApplicationName.put(entry.getKey(), processed);
                }
                processed.addAll(entry.getValue().getMetrics());
            }
        }

        List<IngestApplicationMetric> result = Lists.newLinkedList();
        Date now = new Date();

        for (Map.Entry<String, List<Metric>> entry : metricsByApplicationName.entrySet())
        {
            MetricClassification classification = new MetricClassification(keys, entry.getValue());
            IngestMetric cpuUsageSample = metricBuilder.build(new MetricSample(now, classification.getMetrics(MESSAGE_COUNT_NAME)));
            IngestMetric memoryUsageSample = metricBuilder.build(new MetricSample(now, classification.getMetrics(RESPONSE_TIME_NAME)));
            IngestMetric memoryTotalSample = metricBuilder.build(new MetricSample(now, classification.getMetrics(ERROR_COUNT_NAME)));
            result.add(new IngestApplicationMetric(entry.getKey(), appMetricBuilder.build(cpuUsageSample, memoryUsageSample, memoryTotalSample)));
        }
        return result;
    }

    protected boolean send(Collection<GroupedApplicationsMetrics> collection)
    {
        try
        {
            List<IngestApplicationMetric> metrics = this.processApplicationMetrics(collection);
            final CountDownLatch latch = new CountDownLatch(metrics.size());

            final List<Boolean> results = Collections.synchronizedList(Lists.<Boolean>newLinkedList());
            for (final IngestApplicationMetric metric : metrics)
            {
                this.executor.submit(new Runnable() {
                    @Override
                    public void run() {
                        try
                        {
                            boolean result = client.postApplicationMetrics(metric.getApplicationName(), metric.getBody());
                            results.add(result);
                        }
                        catch (Exception e)
                        {
                            LOGGER.info("could not publish application metrics for " + metric.getApplicationName());
                            results.add(false);
                        }
                        finally
                        {
                            latch.countDown();
                        }
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
