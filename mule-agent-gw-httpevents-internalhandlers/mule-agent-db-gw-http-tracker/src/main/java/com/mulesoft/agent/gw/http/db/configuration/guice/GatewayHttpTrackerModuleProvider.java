/**
 * (c) 2003-2014 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */

package com.mulesoft.agent.gw.http.db.configuration.guice;

import com.google.inject.Binder;
import com.google.inject.TypeLiteral;
import com.mulesoft.agent.configuration.guice.BaseModuleProvider;
import com.mulesoft.agent.gw.http.db.GatewayHttpEventsDBInternalHandler;
import com.mulesoft.mule.runtime.gw.api.analytics.AnalyticsHttpEvent;

/**
 * Guice module provider for this module
 *
 * @since 2.2.0
 */
public class GatewayHttpTrackerModuleProvider extends BaseModuleProvider
{

  @Override
  protected void configureModule(Binder binder)
  {
    bindInternalHandler(binder, GatewayHttpEventsDBInternalHandler.class, new TypeLiteral<AnalyticsHttpEvent>(){});
  }
}
