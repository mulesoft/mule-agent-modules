/**
 * (c) 2003-2014 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */

package com.mulesoft.agent.monitoring.publisher.ingest;

import com.google.common.collect.Lists;
import com.mulesoft.agent.domain.monitoring.Metric;
import com.mulesoft.agent.domain.monitoring.SupportedJMXBean;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class MetricClassificationTestCases {

    private static final String IGNORED_METRIC_NAME = "this metric should be left out";

    public List<Metric> emptyList()
    {
        return new LinkedList<Metric>();
    }

    public List<Metric> someNullsTestCase()
    {
        List<Metric> testCase = Lists.newLinkedList();
        SupportedJMXBean[] supportedJMXBeans = SupportedJMXBean.values();
        for (SupportedJMXBean bean : supportedJMXBeans) {
            for (int i = 0; i < 10; i++) {
                int rnd = new Random().nextInt(3);

                if (rnd == 0) {
                    testCase.add(metric(bean.getMetricName(), 5d));
                } else if (rnd == 1) {
                    testCase.add(metric(bean.getMetricName(), null));
                } else if (rnd == 2) {
                    testCase.add(null);
                }
            }
        }

        for (int i = 0; i < 10; i++) {
            int rnd = new Random().nextInt(3);

            if (rnd == 0) {
                testCase.add(metric(IGNORED_METRIC_NAME, 5d));
            } else if (rnd == 1) {
                testCase.add(metric(IGNORED_METRIC_NAME, null));
            } else if (rnd == 2) {
                testCase.add(null);
            }
        }
        return testCase;
    }

    public List<Metric> completeTestCase(double value)
    {
        List<Metric> testCase = Lists.newLinkedList();
        SupportedJMXBean[] supportedJMXBeans = SupportedJMXBean.values();
        for (SupportedJMXBean bean : supportedJMXBeans) {
            for (int i = 0; i < 10; i++) {
                testCase.add(metric(bean.getMetricName(), value));
            }
        }

        for (int i = 0; i < 10; i++) {
            testCase.add(metric(IGNORED_METRIC_NAME, value));
        }

        return testCase;
    }

    private Metric metric(String name, Double value)
    {
        return new Metric(new Date().getTime(), name, value);
    }

}
