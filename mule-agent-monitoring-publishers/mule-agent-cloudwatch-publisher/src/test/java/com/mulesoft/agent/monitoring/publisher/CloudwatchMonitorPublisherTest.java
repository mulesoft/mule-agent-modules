/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
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
