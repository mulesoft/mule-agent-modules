package com.mulesoft.agent.monitoring.publisher.ingest.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mulesoft.agent.domain.monitoring.Metric;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * This class classifies metrics by their name.
 * </p>
 */
public class MetricClassification
{

    private static final Logger LOGGER = LoggerFactory.getLogger(MetricClassification.class);
    private final Map<String, List<Metric>> classification = Maps.newHashMap();

    public MetricClassification(List<String> keys, List<Metric> sample) {
        super();
        classify(keys, sample);
    }

    private void classify(List<String> keys, List<Metric> sample)
    {
        if (sample == null || keys == null)
        {
            return;
        }
        LOGGER.info("classifying {} metrics for {} keys.", sample.size(), keys.size());
        for (Metric metric : sample)
        {
            if (metric == null || metric.getValue() == null || StringUtils.isBlank(metric.getName()))
            {
                continue;
            }
            for (String classifier : keys)
            {
                if (classifier == null || !classifier.equalsIgnoreCase(metric.getName()))
                {
                    continue;
                }

                String key = metric.getName();
                List<Metric> class_ = classification.get(key);
                if (class_ == null)
                {
                    class_ = Lists.newLinkedList();
                    classification.put(key, class_);
                }
                class_.add(metric);
                break;
            }
        }
        LOGGER.info("classification map ended up with {} pairs", classification.size());
        List<String> absentKeys = Lists.newLinkedList();
        for (String key : keys) {
            if (!classification.keySet().contains(key)) {
                absentKeys.add(key);
            }
        }
        LOGGER.warn("absent keys: " + absentKeys.toString());
    }

    public Map<String, List<Metric>> getClassification()
    {
        return this.classification;
    }

    public List<Metric> getMetrics(String key)
    {
        List<Metric> metrics = this.classification.get(key);
        return metrics != null ? metrics : Lists.<Metric>newLinkedList();
    }
}
