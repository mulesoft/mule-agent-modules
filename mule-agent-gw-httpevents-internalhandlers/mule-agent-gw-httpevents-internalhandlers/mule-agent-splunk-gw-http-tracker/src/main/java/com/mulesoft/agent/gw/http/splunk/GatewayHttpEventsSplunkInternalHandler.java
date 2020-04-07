/**
 * (c) 2003-2014 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */

package com.mulesoft.agent.gw.http.splunk;

import com.mulesoft.agent.common.internalhandler.AbstractSplunkInternalHandler;
import com.mulesoft.mule.runtime.gw.api.analytics.AnalyticsHttpEvent;

import javax.inject.Named;
import javax.inject.Singleton;

/**
 * <p>
 * The Splunk Internal handler will store all the HTTP Events produced from the Mule API Gateway in Splunk instance.
 * </p>
 */
@Singleton
@Named("mule.agent.gw.http.handler.splunk")
public class GatewayHttpEventsSplunkInternalHandler extends AbstractSplunkInternalHandler<AnalyticsHttpEvent>
{
}
