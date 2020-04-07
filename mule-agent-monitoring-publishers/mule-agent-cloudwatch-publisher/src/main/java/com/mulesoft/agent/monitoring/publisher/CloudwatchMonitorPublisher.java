/**
 * (c) 2003-2014 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */

package com.mulesoft.agent.monitoring.publisher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchAsyncClient;
import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.amazonaws.services.cloudwatch.model.PutMetricDataRequest;
import com.mulesoft.agent.buffer.BufferedHandler;
import com.mulesoft.agent.configuration.Configurable;
import com.mulesoft.agent.domain.monitoring.Metric;
import com.mulesoft.agent.services.OnOffSwitch;

/**
 * <p>
 * Handler that publishes JMX information obtained from the Monitoring Service to CloudWatch.
 * </p>
 */
@Named("cloudwatch.agent.monitor.publisher")
@Singleton
public class CloudwatchMonitorPublisher extends BufferedHandler<ArrayList<Metric>>
{
    /**
     * <p>
     * Namespace of the metrics as defined in CloudWatch.
     * </p>
     */
    @Configurable("com.mulesoft.agent")
    String namespace;

    /**
     * <p>
     * Access Key used for CloudWatch authentication.
     * </p>
     */
    @Configurable("missingAccessKey")
    String accessKey;

    /**
     * <p>
     * Secret Key used for CloudWatch authentication.
     * </p>
     */
    @Configurable("missingSecretKey")
    String secretKey;

    @Inject
    public CloudwatchMonitorPublisher()
    {
        super();
    }

    public CloudwatchMonitorPublisher(OnOffSwitch enabledSwitch)
    {
        super();
        this.enabledSwitch = enabledSwitch;
    }

    @Override
    public boolean canHandle(@NotNull ArrayList<Metric> metrics)
    {
        return true;
    }

    @Override
    public boolean flush(@NotNull Collection<ArrayList<Metric>> listOfMetrics)
    {
        AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        AmazonCloudWatchAsyncClient cloudWatchClient = new AmazonCloudWatchAsyncClient(credentials);

        for (List<Metric> metrics : listOfMetrics)
        {
            List<MetricDatum> cloudWatchMetrics = transformMetrics(metrics);
            PutMetricDataRequest putMetricDataRequest = new PutMetricDataRequest();
            putMetricDataRequest.setMetricData(cloudWatchMetrics);
            putMetricDataRequest.setNamespace(namespace);
            cloudWatchClient.putMetricData(putMetricDataRequest);
        }

        return true;
    }

    /**
     * <p>
     * Transforms the metrics from the Metric domain object o the MetricDatum type used by the AWS SDK.
     * </p>
     * @param metrics The list of Metric objects.
     * @return The converted list of MetricDatum objects.
     */
    private static List<MetricDatum> transformMetrics(List<Metric> metrics)
    {
        List<MetricDatum> cloudWatchMetrics = new LinkedList<>();
        for (Metric metric : metrics)
        {
            MetricDatum cloudwatchMetric = new MetricDatum();
            cloudwatchMetric.setMetricName(metric.getName());
            cloudwatchMetric.setValue(metric.getValue().doubleValue());
            cloudwatchMetric.setTimestamp(new Date(metric.getTimestamp()));
            cloudWatchMetrics.add(cloudwatchMetric);
        }
        return cloudWatchMetrics;
    }
}
