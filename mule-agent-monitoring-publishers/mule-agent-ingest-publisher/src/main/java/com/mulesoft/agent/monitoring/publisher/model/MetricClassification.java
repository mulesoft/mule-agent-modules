package com.mulesoft.agent.monitoring.publisher.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mulesoft.agent.domain.monitoring.Metric;
import org.apache.commons.lang.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *     This class represents CPU, Memory Usage and Memory Total groups of metrics.
 * </p>
 */
public class MetricClassification
{

    private final Map<String, List<Metric>> classification = Maps.newHashMap();

    public MetricClassification(List<String> keys, Collection<List<Metric>> samples)
    {
        super();
        if (samples == null || keys == null || keys.size() == 0)
        {
            return;
        }
        for (List<Metric> sample : samples)
        {
            if (sample == null)
            {
                continue;
            }
            for (Metric metric : sample)
            {
                if (metric == null || StringUtils.isBlank(metric.getName()))
                {
                    continue;
                }
                if (keys.contains(metric.getName()))
                {
                    String key = metric.getName();
                    List<Metric> class_ = classification.get(key);
                    if (class_ == null) {
                        class_ = Lists.newLinkedList();
                        classification.put(key, class_);
                    }
                    class_.add(metric);
                }
            }
        }
    }

    Map<String, List<Metric>> getClassification()
    {
        return this.classification;
    }

    public List<Metric> getMetrics(String key)
    {
        List<Metric> metrics = this.classification.get(key);
        return metrics != null ? metrics : Lists.<Metric>newLinkedList();
    }

}
