package com.mulesoft.agent.monitoring.publisher;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Test;

import com.mulesoft.agent.common.internalhandler.cloudhub.CloudhubPlatformClient;
import com.mulesoft.agent.domain.monitoring.ApplicationMetrics;
import com.mulesoft.agent.domain.monitoring.FlowMetrics;
import com.mulesoft.agent.domain.monitoring.GroupedApplicationsMetrics;
import com.mulesoft.agent.domain.monitoring.Metric;

public class CloudhubMuleMessageMetricPublisherTest {

    private CloudhubPlatformClient client = mock(CloudhubPlatformClient.class);
    private CloudhubMuleMessageMetricPublisher publisher = new CloudhubMuleMessageMetricPublisher(client);
    private Collection<GroupedApplicationsMetrics> input;

    @Test
    public void testGenerateSnapshot() {
        setup();
        doAnswer(invocation -> null)
                .when(client).sendMessagesStats(any());
        publisher.flush(input);

        assertNotNull("Snapshot should be created", publisher.getLastSnapshot());
        assertTrue("Should match input", publisher.getLastSnapshot().messageCount == 95L);
        assertTrue("Should match input", publisher.getLastSnapshot().timestamp == 1486668276960L);
    }

    private void setup() {
        Metric messageCount = new Metric(1486668276960L, "messageCount", new Number() {

            @Override
            public int intValue() {
                return 0;
            }

            @Override
            public long longValue() {
                return 95L;
            }

            @Override
            public float floatValue() {
                return 0;
            }

            @Override
            public double doubleValue() {
                return 0;
            }
        });

        List<Metric> metrics = new ArrayList<>(Arrays.asList(messageCount));
        ApplicationMetrics appMetrics = new ApplicationMetrics("test", metrics, (List<FlowMetrics>) null);

        List<ApplicationMetrics> listAppMetrics = new ArrayList<>(Arrays.asList(appMetrics));

        GroupedApplicationsMetrics gam = new GroupedApplicationsMetrics(listAppMetrics);
        List<GroupedApplicationsMetrics> l = new ArrayList<>(Arrays.asList(gam));
        input = l;
    }
}
