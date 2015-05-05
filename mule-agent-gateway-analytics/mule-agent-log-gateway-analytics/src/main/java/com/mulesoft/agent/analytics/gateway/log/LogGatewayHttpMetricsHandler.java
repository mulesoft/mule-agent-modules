package com.mulesoft.agent.analytics.gateway.log;

import com.mulesoft.agent.common.builders.MapMessageBuilder;
import com.mulesoft.agent.common.builders.MessageBuilder;
import com.mulesoft.agent.common.internalhandlers.AbstractLogInternalHandler;
import com.mulesoft.agent.configuration.Configurable;
import com.mulesoft.agent.configuration.Type;
import com.mulesoft.agent.domain.analytics.gateway.GatewayHttpMetric;

import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.message.MapMessage;

import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

/**
 * <p>
 * The Log Internal handler will send Gateway HTTP Metrics to a configurable Log file.
 * </p>
 */

@Named("mule.agent.analytics.gateway.log")
@Singleton
public class LogGatewayHttpMetricsHandler extends AbstractLogInternalHandler<List<GatewayHttpMetric>>
{

    /**
     * <p>
     * A log4j2 PatternLayout (https://logging.apache.org/log4j/2.x/manual/layouts.html#PatternLayout).
     * You can print the properties of the object using the %map{key} notation, for example: %map{timestamp}
     * Default: null, so all the properties will be used as a JSON object.
     * </p>
     */
    @Configurable(type = Type.DYNAMIC)
    public String pattern;

    @Override
    protected void buildLogMessage(Logger internalLogger, List<GatewayHttpMetric> message)
    {
        for (GatewayHttpMetric metric : message)
        {
            internalLogger.info(createMapMessage(metric));
        }
    }

    @Override
    protected MessageBuilder<MapMessage> getMessageBuilder()
    {
        return new MapMessageBuilder(this.getTimestampGetterName(), this.dateFormatPattern, GatewayHttpMetric.class);
    }
}
