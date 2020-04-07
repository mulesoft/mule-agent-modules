/**
 * (c) 2003-2014 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */

package com.mulesoft.agent.monitoring.publisher.ingest.configuration.guice;

import com.google.inject.Binder;
import com.google.inject.TypeLiteral;
import com.mulesoft.agent.configuration.guice.BaseModuleProvider;
import com.mulesoft.agent.domain.monitoring.GroupedApplicationsMetrics;
import com.mulesoft.agent.domain.monitoring.Metric;
import com.mulesoft.agent.handlers.InternalMessageHandler;
import com.mulesoft.agent.monitoring.publisher.ingest.IngestApplicationMonitorPublisher;
import com.mulesoft.agent.monitoring.publisher.ingest.IngestTargetMonitorPublisher;
import com.mulesoft.agent.monitoring.publisher.ingest.factory.GarbageCollectionCountMetricFactory;
import com.mulesoft.agent.monitoring.publisher.ingest.factory.GarbageCollectionTimeMetricFactory;
import com.mulesoft.agent.monitoring.publisher.ingest.factory.MemoryMetricFactory;
import com.mulesoft.agent.monitoring.publisher.ingest.factory.PercentageMetricFactory;
import com.mulesoft.agent.monitoring.publisher.ingest.factory.RawMetricFactory;
import com.mulesoft.agent.monitoring.publisher.ingest.factory.TargetMetricFactory;

import java.util.ArrayList;

/**
 * Guice module provider for this module
 *
 * @since 2.2.0
 */
public class IngestPublisherModuleProvider extends BaseModuleProvider
{

  @Override
  protected void configureModule(Binder binder)
  {
    bindInternalHandler(binder, IngestApplicationMonitorPublisher.class, new TypeLiteral<InternalMessageHandler<GroupedApplicationsMetrics>>(){});
    bindInternalHandler(binder, IngestTargetMonitorPublisher.class, new TypeLiteral<InternalMessageHandler<ArrayList<Metric>>>(){});
    bindNamedSingleton(binder, TargetMetricFactory.class, GarbageCollectionCountMetricFactory.class);
    bindNamedSingleton(binder, TargetMetricFactory.class, GarbageCollectionTimeMetricFactory.class);
    bindNamedSingleton(binder, TargetMetricFactory.class, MemoryMetricFactory.class);
    bindNamedSingleton(binder, TargetMetricFactory.class, PercentageMetricFactory.class);
    bindNamedSingleton(binder, TargetMetricFactory.class, RawMetricFactory.class);
  }
}
