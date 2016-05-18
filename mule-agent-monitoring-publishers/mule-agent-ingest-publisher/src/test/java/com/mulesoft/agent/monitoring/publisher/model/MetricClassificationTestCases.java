package com.mulesoft.agent.monitoring.publisher.model;

import com.google.common.collect.Lists;
import com.mulesoft.agent.domain.monitoring.Metric;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

class MetricClassificationTestCases {

    static final String MEMORY_USAGE_NAME = "java.lang:type=Memory:heap used";
    static final String CPU_USAGE_NAME = "java.lang:type=OperatingSystem:CPU";
    static final String MEMORY_TOTAL_NAME = "java.lang:type=Memory:heap total";
    private static final String IGNORED_METRIC_NAME = "this metric should be left out";

    Collection<List<Metric>> collectionOfEmptyLists()
    {
        Collection<List<Metric>> collection = new LinkedList<>();
        for (int i = 0; i < 1000; i++)
        {
            collection.add(new LinkedList<Metric>());
        }
        return collection;
    }

    Collection<List<Metric>> someNullsTestCase()
    {
        List<Metric> elements1 = Lists.newArrayList(
                metric(null, 3.4d),
                metric(MEMORY_TOTAL_NAME, null),
                null,
                metric(CPU_USAGE_NAME, 8.1d),
                metric(MEMORY_USAGE_NAME, 9.2d),
                metric(null, null),
                metric(MEMORY_USAGE_NAME, 0.7d),
                metric(CPU_USAGE_NAME, 5.2d),
                metric(MEMORY_TOTAL_NAME, 15.3d),
                metric(MEMORY_USAGE_NAME, null),
                metric(MEMORY_TOTAL_NAME, 5d)
        );
        List<Metric> elements2 = Lists.newArrayList(
                metric(MEMORY_USAGE_NAME, 5d),
                metric(null, 8.12d),
                null,
                metric(MEMORY_USAGE_NAME, 10d),
                metric(IGNORED_METRIC_NAME, 1d),
                metric(CPU_USAGE_NAME, 0.1d)
        );
        return Lists.newArrayList(elements1, elements2, null);
    }

    Collection<List<Metric>> completeTestCase()
    {
        List<Metric> elements1 = Lists.newArrayList(
                metric(CPU_USAGE_NAME, 3.4d),
                metric(MEMORY_TOTAL_NAME, 3.9d),
                metric(IGNORED_METRIC_NAME, 1d),
                metric(CPU_USAGE_NAME, 8.1d),
                metric(MEMORY_USAGE_NAME, 9.2d),
                metric(MEMORY_TOTAL_NAME, 9.9d),
                metric(MEMORY_USAGE_NAME, 0.7d),
                metric(CPU_USAGE_NAME, 5.2d),
                metric(MEMORY_TOTAL_NAME, 15.3d),
                metric(MEMORY_USAGE_NAME, 2.9d),
                metric(MEMORY_TOTAL_NAME, 5d)
        );
        List<Metric> elements2 = Lists.newArrayList(
                metric(MEMORY_USAGE_NAME, 5d),
                metric(MEMORY_TOTAL_NAME, 8.12d),
                metric(MEMORY_USAGE_NAME, 7.7d),
                metric(MEMORY_USAGE_NAME, 10d),
                metric(IGNORED_METRIC_NAME, 1d),
                metric(CPU_USAGE_NAME, 0.1d)
        );
        return Lists.newArrayList(elements1, elements2);
    }

    private Metric metric(String name, Double value)
    {
        return new Metric(new Date().getTime(), name, value);
    }

}
