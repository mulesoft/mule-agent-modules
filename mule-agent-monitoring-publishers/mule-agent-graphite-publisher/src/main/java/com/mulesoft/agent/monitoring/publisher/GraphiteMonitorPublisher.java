/**
 * (c) 2003-2020 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */

package com.mulesoft.agent.monitoring.publisher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;

import com.mulesoft.agent.buffer.BufferedHandler;
import com.mulesoft.agent.configuration.Configurable;
import com.mulesoft.agent.domain.monitoring.Metric;
import com.mulesoft.agent.services.OnOffSwitch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * <p>
 * Handler that publishes JMX information obtained from the Monitoring Service to a running Graphite instance.
 * Utilizes Graphite plaintext protocol.
 * </p>
 */
@Named("mule.agent.graphite.jmx.internal.handler")
@Singleton
public class GraphiteMonitorPublisher extends BufferedHandler<ArrayList<Metric>>
{
    private static final Logger LOGGER = LogManager.getLogger(GraphiteMonitorPublisher.class);

    /**
     * Constant to convert milliseconds to seconds.
     */
    private static final long MILLIS_TO_SECS = 1000L;

    /**
     * <p>
     * Prefix used to identify metrics as defined in Graphite's Carbon configuration.
     * </p>
     */
    @Configurable(value = "mule", description = "Prefix used to identify metrics as defined in Graphite's Carbon configuration.")
    String metricPrefix;

    /**
     * <p>
     * Address corresponding to Graphite's Carbon server.
     * </p>
     */
    @Configurable(value = "0.0.0.0", description = "Address corresponding to Graphite's Carbon server.")
    String graphiteServer;

    /**
     * <p>
     * Port corresponding to Graphite's Carbon server.
     * </p>
     */
    @Configurable(value = "2003", description = "Port corresponding to Graphite's Carbon server.")
    int graphitePort;

    @Inject
    public GraphiteMonitorPublisher()
    {
        super();
    }

    public GraphiteMonitorPublisher(OnOffSwitch enabledSwitch)
    {
        super();
        this.enabledSwitch = enabledSwitch;
    }

    @Override
    public boolean canHandle(@NotNull ArrayList<Metric> metrics)
    {
        return true;
    }

    @Override
    public boolean flush(@NotNull Collection<ArrayList<Metric>> listOfMetrics)
    {
        Socket graphiteConnection = null;
        OutputStreamWriter out = null;
        BufferedReader in = null;
        try
        {
            graphiteConnection = new Socket(graphiteServer, graphitePort);

            for (List<Metric> metrics : listOfMetrics)
            {

                for (Metric metric : metrics)
                {

                    StringBuilder message = new StringBuilder();
                    message.append(metricPrefix);
                    message.append(".");
                    message.append(metric.getName().replaceAll("\\s", "").replace(":", ""));
                    message.append(" ");
                    message.append(metric.getValue());
                    message.append(" ");
                    message.append((int) (metric.getTimestamp() / MILLIS_TO_SECS));
                    message.append("\n");

                    out = new OutputStreamWriter(graphiteConnection.getOutputStream());

                    out.write(message.toString());
                    out.flush();

                    in = new BufferedReader(new InputStreamReader(graphiteConnection.getInputStream()));
                    LOGGER.debug("Message sent to Graphite: " + message.toString());

                }
            }

            graphiteConnection.close();
        }
        catch (IOException e)
        {
            LOGGER.warn("Failed to establish connection to Graphite");
            return false;
        }
        finally
        {
            try
            {
                if (in != null)
                {
                    in.close();
                }
                if (out != null)
                {
                    out.close();
                }
                if (graphiteConnection != null)
                {
                    graphiteConnection.close();
                }
            }
            catch (IOException e)
            {

            }

        }
        return true;
    }
}
