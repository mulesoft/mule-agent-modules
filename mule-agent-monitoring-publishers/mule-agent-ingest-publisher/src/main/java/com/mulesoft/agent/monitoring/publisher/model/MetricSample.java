package com.mulesoft.agent.monitoring.publisher.model;

import java.util.Date;

/**
 * Created by svinci on 6/28/16.
 */
public interface MetricSample {

    Date getDate();

    Double getMin();

    Double getMax();

    Double getSum();

    Double getAvg();

    Double getCount();

}
