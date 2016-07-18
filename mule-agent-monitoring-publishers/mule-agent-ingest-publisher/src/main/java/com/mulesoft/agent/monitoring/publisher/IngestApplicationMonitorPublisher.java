package com.mulesoft.agent.monitoring.publisher;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
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
import com.mulesoft.agent.monitoring.publisher.model.DefaultMetricSample;
import com.mulesoft.agent.monitoring.publisher.model.IngestApplicationMetric;
import com.mulesoft.agent.monitoring.publisher.model.MetricClassification;
import com.mulesoft.agent.services.OnOffSwitch;
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
    @Configurable("true")
    private Boolean enabled;

    @Inject
    private IngestApplicationMetricPostBodyBuilder appMetricBuilder;
    private ExecutorService executor;


    @Override
    public void enable(boolean state)
            throws AgentEnableOperationException
    {
        this.enabledSwitch.switchTo(state);
    }

    @Override
    public boolean isEnabled()
    {
        return this.enabledSwitch.isEnabled();
    }

    @PostConfigure
    public void postConfigurable() throws AgentEnableOperationException
    {
        if (this.enabledSwitch == null)
        {
            this.enabledSwitch = OnOffSwitch.newNullSwitch(this.enabled);
        }
        ThreadFactory threadFactory = new ThreadFactoryBuilder().setDaemon(true).setPriority(Thread.MIN_PRIORITY).setNameFormat("monitoring-application-publisher-%d").build();
        this.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), threadFactory);
    }

    private Collection<IngestApplicationMetric> processApplicationMetrics(Collection<GroupedApplicationsMetrics> collection)
    {
        Map<String, IngestApplicationMetric> bodyByApplicationName = Maps.newHashMap();

        for (GroupedApplicationsMetrics metrics : collection)
        {
            for (ApplicationMetrics appMetrics : metrics.getMetricsByApplicationName().values())
            {
                String applicationName = appMetrics.getApplicationName();
                List<Metric> applicationMetrics = appMetrics.getMetrics();

                LOGGER.debug("processing " + applicationMetrics.size()  + " metrics for " + applicationName);

                MetricClassification classification = new MetricClassification(keys, applicationMetrics);

                List<Metric> messageCountMetrics = classification.getMetrics(MESSAGE_COUNT_NAME);
                List<Metric> responseTimeMetrics = classification.getMetrics(RESPONSE_TIME_NAME);
                List<Metric> errorCountMetrics = classification.getMetrics(ERROR_COUNT_NAME);

                IngestMetric messageCountSample = metricBuilder.build(new DefaultMetricSample(messageCountMetrics));

                Set<IngestMetric> messageCount = messageCountMetrics != null ?
                        Sets.newHashSet(messageCountSample) :
                        Sets.<IngestMetric>newHashSet();

                Set<IngestMetric> responseTime = responseTimeMetrics != null && messageCountSample.getAvg() != 0d ?
                        Sets.newHashSet(metricBuilder.build(new DefaultMetricSample(responseTimeMetrics))) :
                        Sets.<IngestMetric>newHashSet();

                Set<IngestMetric> errorCount = responseTimeMetrics != null && messageCountSample.getAvg() != 0d ?
                        Sets.newHashSet(metricBuilder.build(new DefaultMetricSample(errorCountMetrics))) :
                        Sets.<IngestMetric>newHashSet();

                if (bodyByApplicationName.get(applicationName) == null)
                {
                    IngestApplicationMetricPostBody body = appMetricBuilder.build(messageCount, responseTime, errorCount);
                    bodyByApplicationName.put(applicationName, new IngestApplicationMetric(applicationName, body));
                }
                else
                {
                    IngestApplicationMetricPostBody body = bodyByApplicationName.get(applicationName).getBody();
                    body.getMessageCount().addAll(messageCount);
                    body.getResponseTime().addAll(responseTime);
                    body.getErrorCount().addAll(errorCount);
                }
            }
        }

        return bodyByApplicationName.values();
    }

    protected boolean send(Collection<GroupedApplicationsMetrics> collection)
    {
        LOGGER.info("publishing application metrics to ingest api.");
        try
        {
            Collection<IngestApplicationMetric> metrics = this.processApplicationMetrics(collection);
            final CountDownLatch latch = new CountDownLatch(metrics.size());

            final List<Boolean> results = Collections.synchronizedList(Lists.<Boolean>newLinkedList());
            for (final IngestApplicationMetric metric : metrics)
            {
                this.executor.submit(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            boolean result = client.postApplicationMetrics(metric.getApplicationName(), metric.getBody());
                            if (result)
                            {
                                LOGGER.info("successfully published application metrics for " + metric.getApplicationName());
                            }
                            else
                            {
                                LOGGER.error("could not publish application metrics for " + metric.getApplicationName());
                            }
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

            boolean result = !results.contains(false);
            if (result)
            {
                LOGGER.info("Published app metrics to Ingest successfully");
            }
            else
            {
                LOGGER.error("Some metrics for applications could not be published.");
            }
            return result;
        }
        catch (Exception e)
        {
            LOGGER.error("Could not publish application metrics to Ingest: ", e);
            return false;
        }
    }
}
