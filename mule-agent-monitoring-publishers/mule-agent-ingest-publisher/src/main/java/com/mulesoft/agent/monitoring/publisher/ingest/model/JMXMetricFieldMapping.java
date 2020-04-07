/**
 * (c) 2003-2014 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */

package com.mulesoft.agent.monitoring.publisher.ingest.model;

import com.google.common.base.Preconditions;
import com.mulesoft.agent.domain.monitoring.SupportedJMXBean;
import org.apache.commons.lang.NotImplementedException;

import java.util.EnumMap;
import java.util.Map;

/**
 * Mapping from {@link SupportedJMXBean} to Ingest API field name.
 */
public final class JMXMetricFieldMapping
{

    private static final String TENURED_GEN_TOTAL = "tenured-gen-total";
    private static final String TENURED_GEN_USAGE = "tenured-gen-usage";
    private static final String TENURED_GEN_COMMITTED = "tenured-gen-committed";

    private static final String EDEN_TOTAL = "eden-total";
    private static final String EDEN_USAGE = "eden-usage";
    private static final String EDEN_COMMITTED = "eden-committed";

    private static final String SURVIVOR_TOTAL = "survivor-total";
    private static final String SURVIVOR_USAGE = "survivor-usage";
    private static final String SURVIVOR_COMMITTED = "survivor-committed";

    private static final String GC_YOUNG_GEN_TIME = "gc-par-new-time";
    private static final String GC_YOUNG_GEN_COUNT = "gc-par-new-count";

    private static final String GC_OLD_GEN_TIME = "gc-mark-sweep-time";
    private static final String GC_OLD_GEN_COUNT = "gc-mark-sweep-count";

    private static final Map<SupportedJMXBean, String> mappings = new EnumMap<>(SupportedJMXBean.class);

    static
    {
        mappings.put(SupportedJMXBean.HEAP_USAGE, "memory-usage");
        mappings.put(SupportedJMXBean.HEAP_COMMITTED, "memory-committed");
        mappings.put(SupportedJMXBean.HEAP_TOTAL, "memory-total");

        mappings.put(SupportedJMXBean.AVAILABLE_PROCESSORS, "available-processors");
        mappings.put(SupportedJMXBean.LOAD_AVERAGE, "load-average");
        mappings.put(SupportedJMXBean.CPU_USAGE, "cpu-usage");

        mappings.put(SupportedJMXBean.EDEN_USAGE, EDEN_USAGE);
        mappings.put(SupportedJMXBean.G1_EDEN_USAGE, EDEN_USAGE);
        mappings.put(SupportedJMXBean.PS_EDEN_USAGE, EDEN_USAGE);
        mappings.put(SupportedJMXBean.PAR_EDEN_USAGE, EDEN_USAGE);

        mappings.put(SupportedJMXBean.EDEN_COMMITTED, EDEN_COMMITTED);
        mappings.put(SupportedJMXBean.G1_EDEN_COMMITTED, EDEN_COMMITTED);
        mappings.put(SupportedJMXBean.PS_EDEN_COMMITTED, EDEN_COMMITTED);
        mappings.put(SupportedJMXBean.PAR_EDEN_COMMITTED, EDEN_COMMITTED);

        mappings.put(SupportedJMXBean.EDEN_TOTAL, EDEN_TOTAL);
        mappings.put(SupportedJMXBean.G1_EDEN_TOTAL, EDEN_TOTAL);
        mappings.put(SupportedJMXBean.PS_EDEN_TOTAL, EDEN_TOTAL);
        mappings.put(SupportedJMXBean.PAR_EDEN_TOTAL, EDEN_TOTAL);

        mappings.put(SupportedJMXBean.SURVIVOR_USAGE, SURVIVOR_USAGE);
        mappings.put(SupportedJMXBean.G1_SURVIVOR_USAGE, SURVIVOR_USAGE);
        mappings.put(SupportedJMXBean.PS_SURVIVOR_USAGE, SURVIVOR_USAGE);
        mappings.put(SupportedJMXBean.PAR_SURVIVOR_USAGE, SURVIVOR_USAGE);

        mappings.put(SupportedJMXBean.SURVIVOR_COMMITTED, SURVIVOR_COMMITTED);
        mappings.put(SupportedJMXBean.G1_SURVIVOR_COMMITTED, SURVIVOR_COMMITTED);
        mappings.put(SupportedJMXBean.PS_SURVIVOR_COMMITTED, SURVIVOR_COMMITTED);
        mappings.put(SupportedJMXBean.PAR_SURVIVOR_COMMITTED, SURVIVOR_COMMITTED);

        mappings.put(SupportedJMXBean.SURVIVOR_TOTAL, SURVIVOR_TOTAL);
        mappings.put(SupportedJMXBean.G1_SURVIVOR_TOTAL, SURVIVOR_TOTAL);
        mappings.put(SupportedJMXBean.PS_SURVIVOR_TOTAL, SURVIVOR_TOTAL);
        mappings.put(SupportedJMXBean.PAR_SURVIVOR_TOTAL, SURVIVOR_TOTAL);

        mappings.put(SupportedJMXBean.TENURED_GEN_USAGE, TENURED_GEN_USAGE);
        mappings.put(SupportedJMXBean.G1_OLD_GEN_USAGE, TENURED_GEN_USAGE);
        mappings.put(SupportedJMXBean.PS_OLD_GEN_USAGE, TENURED_GEN_USAGE);
        mappings.put(SupportedJMXBean.CMS_OLD_GEN_USAGE, TENURED_GEN_USAGE);

        mappings.put(SupportedJMXBean.TENURED_GEN_COMMITTED, TENURED_GEN_COMMITTED);
        mappings.put(SupportedJMXBean.G1_OLD_GEN_COMMITTED, TENURED_GEN_COMMITTED);
        mappings.put(SupportedJMXBean.PS_OLD_GEN_COMMITTED, TENURED_GEN_COMMITTED);
        mappings.put(SupportedJMXBean.CMS_OLD_GEN_COMMITTED, TENURED_GEN_COMMITTED);

        mappings.put(SupportedJMXBean.TENURED_GEN_TOTAL, TENURED_GEN_TOTAL);
        mappings.put(SupportedJMXBean.G1_OLD_GEN_TOTAL, TENURED_GEN_TOTAL);
        mappings.put(SupportedJMXBean.PS_OLD_GEN_TOTAL, TENURED_GEN_TOTAL);
        mappings.put(SupportedJMXBean.CMS_OLD_GEN_TOTAL, TENURED_GEN_TOTAL);

        mappings.put(SupportedJMXBean.CODE_CACHE_USAGE, "code-cache-usage");
        mappings.put(SupportedJMXBean.CODE_CACHE_COMMITTED, "code-cache-committed");
        mappings.put(SupportedJMXBean.CODE_CACHE_TOTAL, "code-cache-total");

        mappings.put(SupportedJMXBean.COMPRESSED_CLASS_SPACE_USAGE, "compressed-class-space-usage");
        mappings.put(SupportedJMXBean.COMPRESSED_CLASS_SPACE_COMMITTED, "compressed-class-space-committed");
        mappings.put(SupportedJMXBean.COMPRESSED_CLASS_SPACE_TOTAL, "compressed-class-space-total");

        mappings.put(SupportedJMXBean.METASPACE_USAGE, "metaspace-usage");
        mappings.put(SupportedJMXBean.METASPACE_COMMITTED, "metaspace-committed");
        mappings.put(SupportedJMXBean.METASPACE_TOTAL, "metaspace-total");

        mappings.put(SupportedJMXBean.CLASS_LOADING_TOTAL, "class-loading-total");
        mappings.put(SupportedJMXBean.CLASS_LOADING_LOADED, "class-loading-loaded");
        mappings.put(SupportedJMXBean.CLASS_LOADING_UNLOADED, "class-loading-unloaded");

        mappings.put(SupportedJMXBean.THREADING_COUNT, "thread-count");

        mappings.put(SupportedJMXBean.GC_COPY_TIME, GC_YOUNG_GEN_TIME);
        mappings.put(SupportedJMXBean.GC_PAR_NEW_TIME, GC_YOUNG_GEN_TIME);
        mappings.put(SupportedJMXBean.GC_PS_SCAVENGE_TIME, GC_YOUNG_GEN_TIME);
        mappings.put(SupportedJMXBean.GC_G1_YOUNG_GENERATION_TIME, GC_YOUNG_GEN_TIME);

        mappings.put(SupportedJMXBean.GC_COPY_COUNT, GC_YOUNG_GEN_COUNT);
        mappings.put(SupportedJMXBean.GC_PAR_NEW_COUNT, GC_YOUNG_GEN_COUNT);
        mappings.put(SupportedJMXBean.GC_PS_SCAVENGE_COUNT, GC_YOUNG_GEN_COUNT);
        mappings.put(SupportedJMXBean.GC_G1_YOUNG_GENERATION_COUNT, GC_YOUNG_GEN_COUNT);

        mappings.put(SupportedJMXBean.GC_MARK_SWEEP_TIME, GC_OLD_GEN_TIME);
        mappings.put(SupportedJMXBean.GC_PS_MARK_SWEEP_TIME, GC_OLD_GEN_TIME);
        mappings.put(SupportedJMXBean.GC_G1_OLD_GENERATION_TIME, GC_OLD_GEN_TIME);

        mappings.put(SupportedJMXBean.GC_MARK_SWEEP_COUNT, GC_OLD_GEN_COUNT);
        mappings.put(SupportedJMXBean.GC_PS_MARK_SWEEP_COUNT, GC_OLD_GEN_COUNT);
        mappings.put(SupportedJMXBean.GC_G1_OLD_GENERATION_COUNT, GC_OLD_GEN_COUNT);
    }

    private JMXMetricFieldMapping()
    {
    }

    /**
     * Resolve the API field name from a SupportedJMXBean.
     *
     * @param bean jmx bean to resolve from.
     * @return Corresponding API field name.
     */
    public static String from(SupportedJMXBean bean)
    {
        Preconditions.checkArgument(bean != null);
        String fieldName = mappings.get(bean);
        if (fieldName == null)
        {
            throw new NotImplementedException("Bean " + bean.name() + " does not have a mapping implemented.");
        }
        return fieldName;
    }
}