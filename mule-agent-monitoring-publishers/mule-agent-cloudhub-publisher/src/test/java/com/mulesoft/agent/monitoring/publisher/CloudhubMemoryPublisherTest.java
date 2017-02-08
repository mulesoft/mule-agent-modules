package com.mulesoft.agent.monitoring.publisher;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.mulesoft.agent.domain.monitoring.Metric;
import com.mulesoft.agent.domain.monitoring.SupportedJMXBean;
import com.mulesoft.agent.services.OnOffSwitch;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CloudhubMemoryPublisherTest {

    @Test
    public void testPercentageCalculation() {
        CloudhubMemoryPublisher.MemorySnapshot ms = new CloudhubMemoryPublisher.MemorySnapshot(100L, 30L, 34343443L);
        assertEquals(30D, ms.memoryPercentUsed, 1D);
    }
}
