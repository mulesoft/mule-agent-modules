/**
 * (c) 2003-2020 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */

package com.mulesoft.agent.monitoring.publisher;

import com.mulesoft.agent.exception.AgentEnableOperationException;
import com.mulesoft.agent.domain.monitoring.Metric;
import com.mulesoft.agent.services.OnOffSwitch;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class CloudwatchMonitorPublisherTest
{
    private OnOffSwitch enabledSwitch = mock(OnOffSwitch.class);

    public CloudwatchMonitorPublisher publisher = new CloudwatchMonitorPublisher(enabledSwitch);

    @Before
    public void setup()
    {
        publisher.accessKey = "missingAccessKey";
        publisher.secretKey = "missingSecretKey";
        when(enabledSwitch.isEnabled()).thenReturn(true);
    }

    @Test
    public void defaultConstructorMustExistForGuice() throws NoSuchMethodException
    {
        new CloudwatchMonitorPublisher();
        assertTrue(CloudwatchMonitorPublisher.class.getConstructor().getAnnotation(Inject.class) != null);
    }

    @Test
    public void disableEnablePublisher() throws AgentEnableOperationException
    {
        publisher.enable(false);
        verify(enabledSwitch, times(1)).switchTo(false);
        publisher.enable(true);
        verify(enabledSwitch, times(1)).switchTo(true);
        publisher.isEnabled();
        verify(enabledSwitch, times(1)).isEnabled();
    }

    @Test
    public void ifDisabledDoNothing() throws AgentEnableOperationException
    {
        when(enabledSwitch.isEnabled()).thenReturn(false);
        ArrayList<Metric> testMetrics = new ArrayList<>();
        Metric mockedMetric = mock(Metric.class);
        testMetrics.add(mockedMetric);
        Assert.assertFalse(publisher.handle(testMetrics));
        verify(mockedMetric, times(0)).getName();
        verify(mockedMetric, times(0)).getValue();
        when(enabledSwitch.isEnabled()).thenReturn(true);
    }
}
