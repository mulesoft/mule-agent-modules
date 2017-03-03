package com.mulesoft.agent.monitoring.publisher;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.mulesoft.agent.AgentEnableOperationException;
import com.mulesoft.agent.buffer.BufferedHandler;
import com.mulesoft.agent.configuration.Configurable;
import com.mulesoft.agent.configuration.PostConfigure;
import com.mulesoft.agent.domain.monitoring.ApplicationMetrics;
import com.mulesoft.agent.domain.monitoring.GroupedApplicationsMetrics;
import com.mulesoft.agent.domain.monitoring.Metric;
import com.mulesoft.agent.monitoring.publisher.client.CloudhubPlatformClient;
import com.mulesoft.agent.monitoring.publisher.factory.MuleMessageSnapshotFactory;
import com.mulesoft.agent.services.OnOffSwitch;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Handler that publishes Mule messages count to CloudHub.
 */
@Named("mule.agent.cloudhub.mulemessages.internal.handler")
@Singleton
public class CloudhubMuleMessageMetricPublisher
        extends BufferedHandler<GroupedApplicationsMetrics>
{

    private static final String MULE_MESSAGES_METRIC_NAME = "messageCount";
    private static final Logger LOGGER = LogManager.getLogger(
            CloudhubMuleMessageMetricPublisher.class);

    @Configurable("true")
    private boolean enabled;

    @Inject
    private CloudhubPlatformClient cloudhubClient;
    @Inject
    private MuleMessageSnapshotFactory factory;

    public CloudhubMuleMessageMetricPublisher()
    {
    }

    protected CloudhubMuleMessageMetricPublisher(CloudhubPlatformClient client, MuleMessageSnapshotFactory factory)
    {
        super();
        this.cloudhubClient = client;
        this.factory = factory;
    }

    @PostConfigure
    public void postConfigurable() throws AgentEnableOperationException
    {
        if (this.enabledSwitch == null)
        {
            this.enabledSwitch = OnOffSwitch.newNullSwitch(this.enabled);
        }
    }

    @Override
    protected boolean canHandle(GroupedApplicationsMetrics message)
    {
        return true;
    }

    @Override
    protected boolean flush(Collection<GroupedApplicationsMetrics> messages)
    {
        boolean success = false;
        try
        {
            for (GroupedApplicationsMetrics gam : messages)
            {
                Map<String, ApplicationMetrics> appsWithMetrics = gam.getMetricsByApplicationName();

                if (appsWithMetrics.size() == 0)
                {
                    LOGGER.debug("application deploy in progress or undeployed");
                    continue;
                }
                if (appsWithMetrics.size() > 1)
                {
                    throw new RuntimeException(String.format("There is more than one app running: %s", appsWithMetrics.size()));
                }

                List<Metric> appMetrics = appsWithMetrics.entrySet()
                        .iterator().next()
                        .getValue().getMetrics();

                Metric msgCountMetric = null;
                for (Metric metric : appMetrics)
                {
                    if (MULE_MESSAGES_METRIC_NAME.equals(metric.getName()))
                    {
                        msgCountMetric = metric;
                    }
                }
                if (msgCountMetric == null)
                {
                    LOGGER.info(String.format("No %s metric found", MULE_MESSAGES_METRIC_NAME));
                    continue;
                }
                long messageCount = msgCountMetric.getValue().longValue();
                long timestamp = msgCountMetric.getTimestamp();

                success = cloudhubClient.sendMessagesStats(factory.newSnapshot(messageCount, timestamp));
            }
            return success;
        }
        catch (Exception e)
        {
            LOGGER.error("Error flushing memory metrics to CloudHub: {}", ExceptionUtils.getRootCauseMessage(e));
            LOGGER.debug(e);
            return success;
        }
    }
}
