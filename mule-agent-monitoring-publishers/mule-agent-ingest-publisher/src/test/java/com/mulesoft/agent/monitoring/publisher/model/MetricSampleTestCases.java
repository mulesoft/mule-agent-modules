package com.mulesoft.agent.monitoring.publisher.model;

import com.google.common.collect.Lists;
import com.mulesoft.agent.domain.monitoring.Metric;

import java.util.Date;
import java.util.List;

class MetricSampleTestCases {

    List<Metric> aCoupleOfNulls()
    {
        return Lists.newArrayList(
                metric(2d), null, metric(4.5d), metric(20d), metric(null)
        );
    }

    List<Metric> complete()
    {
        return Lists.newArrayList(
                metric(2d), metric(4.5d), metric(20d)
        );
    }

    private Metric metric(Double value)
    {
        return new Metric(new Date().getTime(), "", value);
    }

}
