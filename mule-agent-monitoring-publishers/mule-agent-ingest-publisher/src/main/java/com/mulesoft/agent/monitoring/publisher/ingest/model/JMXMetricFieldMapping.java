package com.mulesoft.agent.monitoring.publisher.ingest.model;

import com.mulesoft.agent.domain.monitoring.SupportedJMXBean;

/**
 * Mapping JMX Bean -> Ingest API field name.
 */
public enum JMXMetricFieldMapping
{

    HEAP_USAGE(SupportedJMXBean.HEAP_USAGE, "memory-usage"),
    HEAP_COMMITTED(SupportedJMXBean.HEAP_COMMITTED, "memory-committed"),
    HEAP_TOTAL(SupportedJMXBean.HEAP_TOTAL, "memory-total"),

    AVAILABLE_PROCESSORS(SupportedJMXBean.AVAILABLE_PROCESSORS, "available-processors"),
    LOAD_AVERAGE(SupportedJMXBean.LOAD_AVERAGE, "load-average"),
    CPU_USAGE(SupportedJMXBean.CPU_USAGE, "cpu-usage"),

    EDEN_USAGE(SupportedJMXBean.EDEN_USAGE, "eden-usage"),
    EDEN_COMMITTED(SupportedJMXBean.EDEN_COMMITTED, "eden-committed"),
    EDEN_TOTAL(SupportedJMXBean.EDEN_TOTAL, "eden-total"),

    SURVIVOR_USAGE(SupportedJMXBean.SURVIVOR_USAGE, "survivor-usage"),
    SURVIVOR_COMMITTED(SupportedJMXBean.SURVIVOR_COMMITTED, "survivor-committed"),
    SURVIVOR_TOTAL(SupportedJMXBean.SURVIVOR_TOTAL, "survivor-total"),

    OLD_GEN_USAGE(SupportedJMXBean.OLD_GEN_USAGE, "old-gen-usage"),
    OLD_GEN_COMMITTED(SupportedJMXBean.OLD_GEN_COMMITTED, "old-gen-committed"),
    OLD_GEN_TOTAL(SupportedJMXBean.OLD_GEN_TOTAL, "old-gen-total"),

    CODE_CACHE_USAGE(SupportedJMXBean.CODE_CACHE_USAGE, "code-cache-usage"),
    CODE_CACHE_COMMITTED(SupportedJMXBean.CODE_CACHE_COMMITTED, "code-cache-committed"),
    CODE_CACHE_TOTAL(SupportedJMXBean.CODE_CACHE_TOTAL, "code-cache-total"),

    COMPRESSED_CLASS_SPACE_USAGE(SupportedJMXBean.COMPRESSED_CLASS_SPACE_USAGE, "compressed-class-space-usage"),
    COMPRESSED_CLASS_SPACE_COMMITTED(SupportedJMXBean.COMPRESSED_CLASS_SPACE_COMMITTED, "compressed-class-space-committed"),
    COMPRESSED_CLASS_SPACE_TOTAL(SupportedJMXBean.COMPRESSED_CLASS_SPACE_TOTAL, "compressed-class-space-total"),

    METASPACE_USAGE(SupportedJMXBean.METASPACE_USAGE, "metaspace-usage"),
    METASPACE_COMMITTED(SupportedJMXBean.METASPACE_COMMITTED, "metaspace-committed"),
    METASPACE_TOTAL(SupportedJMXBean.METASPACE_TOTAL, "metaspace-total"),

    CLASS_LOADING_TOTAL(SupportedJMXBean.CLASS_LOADING_TOTAL, "class-loading-total"),
    CLASS_LOADING_LOADED(SupportedJMXBean.CLASS_LOADING_LOADED, "class-loading-loaded"),
    CLASS_LOADING_UNLOADED(SupportedJMXBean.CLASS_LOADING_UNLOADED, "class-loading-unloaded"),

    THREADING_COUNT(SupportedJMXBean.THREADING_COUNT, "thread-count"),

    GC_MARK_SWEEP_TIME(SupportedJMXBean.GC_MARK_SWEEP_TIME, "gc-mark-sweep-time"),
    GC_MARK_SWEEP_COUNT(SupportedJMXBean.GC_MARK_SWEEP_COUNT, "gc-mark-sweep-count"),

    GC_SCAVENGE_TIME(SupportedJMXBean.GC_SCAVENGE_TIME, "gc-scavenge-time"),
    GC_SCAVENGE_COUNT(SupportedJMXBean.GC_SCAVENGE_COUNT, "gc-scavenge-count");

    /**
     * JMX Bean to be reported to ingest with the field name.
     */
    private final SupportedJMXBean jmxBean;

    /**
     * Ingest API exposed metric name.
     */
    private final String fieldName;

    JMXMetricFieldMapping(SupportedJMXBean jmxBean, String fieldName)
    {
        this.jmxBean = jmxBean;
        this.fieldName = fieldName;
    }

    public SupportedJMXBean getJmxBean()
    {
        return jmxBean;
    }

    public String getFieldName()
    {
        return fieldName;
    }

    /**
     * Resolve the field mapping from a SupportedJMXBean.
     * @param bean jmx bean to resolve from.
     * @return Corresponding field mapping.
     */
    public static JMXMetricFieldMapping forSupportedJMXBean(SupportedJMXBean bean)
    {
        if (bean == null)
        {
            return null;
        }
        for (JMXMetricFieldMapping value : values())
        {
            if (value.getJmxBean() == bean)
            {
                return value;
            }
        }
        return null;
    }

}
