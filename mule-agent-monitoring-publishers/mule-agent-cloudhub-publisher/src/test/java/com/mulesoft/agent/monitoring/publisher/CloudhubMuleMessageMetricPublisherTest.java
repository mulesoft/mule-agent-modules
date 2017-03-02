package com.mulesoft.agent.monitoring.publisher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.mulesoft.agent.domain.monitoring.ApplicationMetrics;
import com.mulesoft.agent.domain.monitoring.FlowMetrics;
import com.mulesoft.agent.domain.monitoring.GroupedApplicationsMetrics;
import com.mulesoft.agent.domain.monitoring.Metric;
import com.mulesoft.agent.monitoring.publisher.client.DefaultCloudhubPlatformClient;
import com.mulesoft.agent.monitoring.publisher.factory.MuleMessageSnapshotFactory;

import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CloudhubMuleMessageMetricPublisherTest {

    private DefaultCloudhubPlatformClient client = Mockito.mock(DefaultCloudhubPlatformClient.class);
    private CloudhubMuleMessageMetricPublisher publisher = Mockito.spy(new CloudhubMuleMessageMetricPublisher(client,
            new MuleMessageSnapshotFactory()));

    @Test
    public void testSendStats() {
        Mockito.doReturn(true)
               .when(client).sendMessagesStats(Mockito.any(MuleMessageSnapshotFactory.MuleMessageSnapshot.class));
        Collection<GroupedApplicationsMetrics> input = setup();

        assertTrue("Flush should complete", publisher.flush(input));
        Mockito.verify(client).sendMessagesStats(Mockito.any(MuleMessageSnapshotFactory.MuleMessageSnapshot.class));
    }

    @Test
    public void testSendStatsException() {
        Collection<GroupedApplicationsMetrics> input = setup();
        Mockito.doThrow(new RuntimeException())
               .when(client).sendMessagesStats(Mockito.any(MuleMessageSnapshotFactory.MuleMessageSnapshot.class));

        assertFalse("Flush should fail", publisher.flush(input));
    }

    private Collection<GroupedApplicationsMetrics> setup() {
        Metric messageCount = new Metric(1486668276960L, "messageCount", 95L);

        List<Metric> metrics = new ArrayList<>(Collections.singletonList(messageCount));
        ApplicationMetrics appMetrics = new ApplicationMetrics("test", metrics, (List<FlowMetrics>) null);

        List<ApplicationMetrics> listAppMetrics = new ArrayList<>(Collections.singletonList(appMetrics));

        GroupedApplicationsMetrics gam = new GroupedApplicationsMetrics(listAppMetrics);
        return new ArrayList<>(Collections.singletonList(gam));
    }
}
