package com.mulesoft.agent.monitoring.publisher.ingest.model;

import java.util.Date;

/**
 * Interface of a MetricSample that gives us the possibility to implement decorators.
 */
public interface MetricSample
{

    Date getDate();

    Double getMin();

    Double getMax();

    Double getSum();

    Double getAvg();

    Double getCount();

}
