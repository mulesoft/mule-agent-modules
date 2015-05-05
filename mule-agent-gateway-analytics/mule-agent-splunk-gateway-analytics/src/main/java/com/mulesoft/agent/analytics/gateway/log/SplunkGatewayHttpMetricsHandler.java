package com.mulesoft.agent.analytics.gateway.log;

import com.mulesoft.agent.common.builders.MapMessageBuilder;
import com.mulesoft.agent.common.builders.MessageBuilder;
import com.mulesoft.agent.common.internalhandlers.AbstractSplunkInternalHandler;
import com.mulesoft.agent.configuration.Configurable;
import com.mulesoft.agent.configuration.Type;
import com.mulesoft.agent.domain.analytics.gateway.GatewayHttpMetric;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.message.MapMessage;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

/**
 * <p>
 * The Splunk Internal handler will send Gateway HTTP Metrics to Splunk.
 * </p>
 */

@Named("mule.agent.analytics.gateway.splunk")
@Singleton
public class SplunkGatewayHttpMetricsHandler extends AbstractSplunkInternalHandler<List<GatewayHttpMetric>>
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
    protected String getPattern ()
    {
        return this.pattern;
    }

    protected void buildSplunkMessage(OutputStream output, List<GatewayHttpMetric> metrics)
        throws IOException {
        for (GatewayHttpMetric metric : metrics)
        {
            MapMessage mapMessage = createMapMessage(metric);

            // Defer the creation of the layout until the MapMessage is created
            if (getLayout() == null)
            {
                initializeLayout(mapMessage);
            }

            LogEvent event = getLogEventFactory().createEvent(this.getClass().getName(), null,
                                                              this.getClass().getName(), Level.INFO,
                                                              mapMessage, null, null);
            output.write(getLayout().toByteArray(event));
        }
    }


    @Override
    protected MessageBuilder<MapMessage> getMessageBuilder()
    {
        return new MapMessageBuilder(this.getTimestampGetterName(), this.dateFormatPattern, GatewayHttpMetric.class);
    }
}
