package com.mulesoft.agent.monitoring.publisher;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mulesoft.agent.AgentEnableOperationException;
import com.mulesoft.agent.configuration.Configurable;
import com.mulesoft.agent.configuration.NotAvailableOn;
import com.mulesoft.agent.configuration.PostConfigure;
import com.mulesoft.agent.domain.monitoring.ApplicationMetrics;
import com.mulesoft.agent.domain.monitoring.FlowMetrics;
import com.mulesoft.agent.domain.monitoring.GroupedApplicationsMetrics;
import com.mulesoft.agent.domain.monitoring.Metric;
import com.mulesoft.agent.monitoring.publisher.ingest.builder.IngestApplicationMetricPostBodyBuilder;
import com.mulesoft.agent.monitoring.publisher.ingest.model.IngestApplicationMetricPostBody;
import com.mulesoft.agent.monitoring.publisher.ingest.model.IngestMetric;
import com.mulesoft.agent.monitoring.publisher.model.DefaultMetricSample;
import com.mulesoft.agent.monitoring.publisher.ingest.model.IngestFlowMetrics;
import com.mulesoft.agent.monitoring.publisher.model.IngestApplicationMetric;
import com.mulesoft.agent.monitoring.publisher.model.MetricClassification;
import com.mulesoft.agent.services.OnOffSwitch;
import com.ning.http.client.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.*;

import static com.mulesoft.agent.domain.RuntimeEnvironment.ON_PREM;
import static com.mulesoft.agent.domain.RuntimeEnvironment.STANDALONE;

/**
 * <p>
 * Handler that publishes Application information obtained from the Monitoring Service to a running Ingest API instance.
 * </p>
 */
@Singleton
@Named("mule.agent.ingest.application.metrics.internal.handler")
@NotAvailableOn(environment = {ON_PREM, STANDALONE})
public class IngestApplicationMonitorPublisher extends IngestMonitorPublisher<GroupedApplicationsMetrics>
{

    private final static Logger LOGGER = LogManager.getLogger(IngestApplicationMonitorPublisher.class);

    /**
     * Message count metric name.
     */
    private static final String MESSAGE_COUNT_NAME = "messageCount";

    /**
     * Response time metric name.
     */
    private static final String RESPONSE_TIME_NAME = "responseTime";

    /**
     * Error count metric name.
     */
    private static final String ERROR_COUNT_NAME = "errorCount";

    /**
     * Convenient array with all the handled metric names.
     */
    private static final List<String> keys = Lists.newArrayList(MESSAGE_COUNT_NAME, RESPONSE_TIME_NAME, ERROR_COUNT_NAME);

    /**
     * Time the publisher will wait before failing if application metrics weren't send yet. Defaults to 10000.
     */
    @Configurable("10000")
    private Long applicationPublishTimeOut;

    /**
     * Defines the unit in which the time out is represented. Defaults to MILLISECONDS.
     */
    @Configurable("MILLISECONDS")
    private TimeUnit applicationPublishTimeUnit;

    /**
     * Defines whether this publisher is enabled or not.
     */
    @Configurable("true")
    private Boolean enabled;

    /**
     * Ingest application post body builder.
     */
    @Inject
    private IngestApplicationMetricPostBodyBuilder appMetricBuilder;

    /**
     * Executor to send application metrics in parallel.
     */
    private ExecutorService executor;

    /**
     * Publisher setup once plugin configuration is done.
     *
     * @throws AgentEnableOperationException when an exception is thrown while trying to enable the publisher.
     */
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

    /**
     * Extract, and build flow metrics from an application.
     *
     * @param applicationMetrics Application metrics from which to extract the flow metrics.
     * @return Flow metrics by their names extracted from the given application.
     */
    private Map<String, IngestFlowMetrics> extractFlowMetrics(ApplicationMetrics applicationMetrics) {
        Map<String, IngestFlowMetrics> result = Maps.newHashMap();

        for (Map.Entry<String, FlowMetrics> entry : applicationMetrics.getFlowMetrics().entrySet()) {

            FlowMetrics metrics = entry.getValue();

            MetricClassification classification = new MetricClassification(keys, metrics.getMetrics());

            List<Metric> messageCountMetrics = classification.getMetrics(MESSAGE_COUNT_NAME);
            List<Metric> responseTimeMetrics = classification.getMetrics(RESPONSE_TIME_NAME);
            List<Metric> errorCountMetrics = classification.getMetrics(ERROR_COUNT_NAME);

            IngestMetric messageCountSample = metricBuilder.build(new DefaultMetricSample(messageCountMetrics));

            Set<IngestMetric> messageCount = messageCountMetrics != null && messageCountMetrics.size() > 0 ?
                    Sets.newHashSet(messageCountSample) :
                    Sets.<IngestMetric>newHashSet();

            boolean avgMessageCountIsNotZero = messageCountSample.getAvg() != 0d;

            Set<IngestMetric> responseTime = responseTimeMetrics != null && responseTimeMetrics.size() > 0 && avgMessageCountIsNotZero ?
                    Sets.newHashSet(metricBuilder.build(new DefaultMetricSample(responseTimeMetrics))) :
                    Sets.<IngestMetric>newHashSet();

            Set<IngestMetric> errorCount = errorCountMetrics != null && errorCountMetrics.size() > 0 && avgMessageCountIsNotZero ?
                    Sets.newHashSet(metricBuilder.build(new DefaultMetricSample(errorCountMetrics))) :
                    Sets.<IngestMetric>newHashSet();

            result.put(entry.getKey(), new IngestFlowMetrics(messageCount, responseTime, errorCount));

        }

        return result;
    }

    /**
     * Process the buffer's contents and build the bodies to be posted to Ingest API.
     *
     * @param collection Buffer contents.
     * @return Processed application metrics ready to be sent to ingest API.
     */
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

                Set<IngestMetric> messageCount = messageCountMetrics != null && messageCountMetrics.size() > 0 ?
                        Sets.newHashSet(messageCountSample) :
                        Sets.<IngestMetric>newHashSet();

                boolean avgMessageCountIsNotZero = messageCountSample.getAvg() != 0d;

                Set<IngestMetric> responseTime = responseTimeMetrics != null && responseTimeMetrics.size() > 0 && avgMessageCountIsNotZero ?
                        Sets.newHashSet(metricBuilder.build(new DefaultMetricSample(responseTimeMetrics))) :
                        Sets.<IngestMetric>newHashSet();

                Set<IngestMetric> errorCount = errorCountMetrics != null && errorCountMetrics.size() > 0 && avgMessageCountIsNotZero ?
                        Sets.newHashSet(metricBuilder.build(new DefaultMetricSample(errorCountMetrics))) :
                        Sets.<IngestMetric>newHashSet();

                Map<String, IngestFlowMetrics> flows = this.extractFlowMetrics(appMetrics);

                if (bodyByApplicationName.get(applicationName) == null)
                {
                    IngestApplicationMetricPostBody body = appMetricBuilder.build(messageCount, responseTime, errorCount, flows);
                    bodyByApplicationName.put(applicationName, new IngestApplicationMetric(applicationName, body));
                }
                else
                {
                    IngestApplicationMetricPostBody body = bodyByApplicationName.get(applicationName).getBody();
                    body.getMessageCount().addAll(messageCount);
                    body.getResponseTime().addAll(responseTime);
                    body.getErrorCount().addAll(errorCount);
                    if (body.getFlows().isEmpty())
                    {
                        body.setFlows(flows);
                    }
                    else
                    {
                        for (Map.Entry<String, IngestFlowMetrics> entry : flows.entrySet())
                        {
                            IngestFlowMetrics justFound = entry.getValue();
                            IngestFlowMetrics existing = body.getFlows().get(entry.getKey());
                            if (existing == null)
                            {
                                body.getFlows().put(entry.getKey(), justFound);
                            }
                            else
                            {
                                existing.getMessageCount().addAll(justFound.getMessageCount());
                                existing.getResponseTime().addAll(justFound.getResponseTime());
                                existing.getErrorCount().addAll(justFound.getErrorCount());
                            }
                        }
                    }
                }
            }
        }

        return bodyByApplicationName.values();
    }

    /**
     * Grab and process the contents of the buffer and send them to Ingest API.
     *
     * @param collection Buffer contents.
     * @return Whether the run was successful or not.
     */
    @Override
    protected boolean send(Collection<GroupedApplicationsMetrics> collection)
    {
        LOGGER.debug("publishing application metrics to ingest api.");
        try
        {
            Collection<IngestApplicationMetric> metrics = this.processApplicationMetrics(collection);
            final CountDownLatch latch = new CountDownLatch(metrics.size());

            final List<Integer> statusCodes = Collections.synchronizedList(Lists.<Integer>newLinkedList());
            for (final IngestApplicationMetric metric : metrics)
            {
                this.executor.submit(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            Response httpResponse = client.postApplicationMetrics(metric.getApplicationName(), metric.getBody());
                            if (isSuccessStatusCode(httpResponse.getStatusCode()))
                            {
                                LOGGER.debug("successfully published application metrics for " + metric.getApplicationName());
                            }
                            else
                            {
                                LOGGER.warn("Could not publish app metrics for " + metric.getApplicationName() + " to Ingest.");
                            }
                            statusCodes.add(httpResponse.getStatusCode());
                        }
                        catch (Exception e)
                        {
                            LOGGER.warn(String.format("could not publish application metrics for %s, cause: %s", metric.getApplicationName(), e.getMessage()));
                            LOGGER.debug("Error: ", e);
                            statusCodes.add(500);
                        }
                        finally
                        {
                            latch.countDown();
                        }
                    }
                });
            }
            latch.await(applicationPublishTimeOut, applicationPublishTimeUnit);

            boolean successful = true;
            for (int statusCode : statusCodes)
            {
                if (!isSuccessStatusCode(statusCode))
                {
                    successful = false;
                }
            }
            if (successful)
            {
                LOGGER.debug("Published app metrics to Ingest successfully");
            }
            else
            {
                LOGGER.warn("Some metrics for applications could not be published.");
            }
            return successful;
        }
        catch (Exception e)
        {
            LOGGER.warn(String.format("Could not publish application metrics to Ingest, cause: %s", e.getMessage()));
            LOGGER.debug("Error: ", e);
            return false;
        }
    }
}
