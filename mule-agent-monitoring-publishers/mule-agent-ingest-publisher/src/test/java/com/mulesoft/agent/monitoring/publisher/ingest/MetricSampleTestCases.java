/**
 * (c) 2003-2020 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */

package com.mulesoft.agent.monitoring.publisher.ingest;

import com.google.common.collect.Lists;
import com.mulesoft.agent.domain.monitoring.Metric;

import java.util.Date;
import java.util.List;

public class MetricSampleTestCases {

    public List<Metric> aCoupleOfNulls()
    {
        return Lists.newArrayList(
                metric(2d), null, metric(4.5d), metric(20d), metric(null)
        );
    }

    public List<Metric> complete()
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
