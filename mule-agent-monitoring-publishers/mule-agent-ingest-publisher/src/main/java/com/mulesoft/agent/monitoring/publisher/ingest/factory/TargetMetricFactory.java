package com.mulesoft.agent.monitoring.publisher.ingest.factory;

import com.google.common.base.Preconditions;
import com.mulesoft.agent.domain.monitoring.SupportedJMXBean;
import com.mulesoft.agent.monitoring.publisher.ingest.builder.IngestMetricBuilder;
import com.mulesoft.agent.monitoring.publisher.ingest.model.MetricClassification;
import com.mulesoft.agent.monitoring.publisher.ingest.model.MetricSample;
import com.mulesoft.agent.monitoring.publisher.ingest.model.api.IngestMetric;

import javax.inject.Inject;
import java.util.List;

/**
 * Abstract IngestMetric factory, meant to resolve an IngestMetric from a MetricClassification and a SupportedJMXBean.
 */
public abstract class TargetMetricFactory {

    /**
     * Ingest metric builder.
     */
    @Inject
    protected IngestMetricBuilder metricBuilder;

    /**
     * Evaluates the given JMX bean to resolve whether this instance is meant to be used on a metric collected from it.
     *
     * @param bean Bean to be evaluated.
     * @return True if the current instance was made to handle the given bean.
     */
    public boolean appliesForMetric(SupportedJMXBean bean) {
        return getSupportedMetrics().contains(bean);
    }

    /**
     * @param classification Classification from which to extract the metrics corresponding to the given bean.
     * @param bean JMX bean from which the metrics that are going to be processed were extracted.
     * @return A new IngestMetric resolved from the metrics that are extracted from the classification with the given bean.
     */
    public IngestMetric apply(MetricClassification classification, SupportedJMXBean bean) {
        Preconditions.checkArgument(appliesForMetric(bean),
                String.format("Invoked factory does not apply for %s, make sure you are checking with appliesForMetric first.", bean.name()));
        if (classification != null) {
            MetricSample sample = doApply(classification, bean);
            return metricBuilder.build(sample);
        }
        return null;
    }

    /**
     * @return The list of JMX beans that are meant to be processed by this factory.
     */
    abstract List<SupportedJMXBean> getSupportedMetrics();

    /**
     * Custom logic to process the classification and the given bean.
     *
     * @param classification Classification from which to extract the metrics corresponding to the given bean.
     * @param bean JMX bean from which the metrics that are going to be processed were extracted.
     * @return A new MetricSample resolved from the metrics that are extracted from the classification with the given bean.
     */
    abstract MetricSample doApply(MetricClassification classification, SupportedJMXBean bean);

}
