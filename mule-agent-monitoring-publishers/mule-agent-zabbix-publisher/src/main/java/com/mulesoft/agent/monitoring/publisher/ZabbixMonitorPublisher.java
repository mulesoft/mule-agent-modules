/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.mulesoft.agent.monitoring.publisher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
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
 * Handler that publishes JMX information obtained from the Monitoring Service to a running Zabbix instance.
 * Utilizes version 2.0 of the Zabbix Sender protocol.
 * </p>
 */
@Named("mule.agent.zabbix.jmx.internal.handler")
@Singleton
public class ZabbixMonitorPublisher extends BufferedHandler<List<Metric>>
{
    private static final Logger LOGGER = LogManager.getLogger(ZabbixMonitorPublisher.class);

    private static final String MESSAGE_START = "{\"request\":\"sender data\",\"data\":[{\"host\":\"";
    private static final String MESSAGE_MIDDLE_LEFT = "\",\"key\":\"";
    private static final String MESSAGE_MIDDLE_RIGHT = "\",\"value\":\"";
    private static final String MESSAGE_END = "\"}]}\n";
    public static final int SHIFT_TWO = 8;
    public static final int SHIFT_FOUR = 16;
    public static final int SHIFT_SIX = 24;
    public static final int BITMASK = 0xFF;
    public static final int SHIFT_TWO_BITMASK = 0x00FF;
    public static final int SHIFT_FOUR_BITMASK = 0x0000FF;
    public static final int SHIFT_SIX_BITMASK = 0x000000FF;

    /**
     * <p>
     * Host name defined in Zabbix for it to recognize the source of the metric.
     * </p>
     */
    @Configurable("com.mulesoft.agent")
    String host;

    /**
     * <p>
     * Address corresponding to the Zabbix server.
     * </p>
     */
    @Configurable("0.0.0.0")
    String zabbixServer;

    /**
     * <p>
     * Port corresponding to the Zabbix server.
     * </p>
     */
    @Configurable("10051")
    int zabbixPort;

    @Inject
    public ZabbixMonitorPublisher()
    {
        super();
    }

    public ZabbixMonitorPublisher(OnOffSwitch enabledSwitch)
    {
        super();
        this.enabledSwitch = enabledSwitch;
    }

    @Override
    public boolean canHandle(@NotNull List<Metric> metrics)
    {
        return true;
    }

    @Override
    public boolean flush(@NotNull Collection<List<Metric>> listOfMetrics)
    {
        Socket zabbixConnection = null;
        OutputStream out = null;
        BufferedReader in = null;
        try
        {
            for (List<Metric> metrics : listOfMetrics)
            {
                for (Metric metric : metrics)
                {
                    zabbixConnection = new Socket(zabbixServer, zabbixPort);

                    StringBuilder message = new StringBuilder();
                    message.append(MESSAGE_START);
                    message.append(host);
                    message.append(MESSAGE_MIDDLE_LEFT);
                    message.append(metric.getName().replaceAll("\\s", "").replace(":", ""));
                    message.append(MESSAGE_MIDDLE_RIGHT);
                    message.append(metric.getValue());
                    message.append(MESSAGE_END);

                    String s = message.toString();

                    byte[] chars = s.getBytes();
                    int length = chars.length;
                    out = zabbixConnection.getOutputStream();
                    out.write(new byte[]{
                            'Z', 'B', 'X', 'D',
                            '\1',
                            (byte) (length & BITMASK),
                            (byte) ((length >> SHIFT_TWO) & SHIFT_TWO_BITMASK),
                            (byte) ((length >> SHIFT_FOUR) & SHIFT_FOUR_BITMASK),
                            (byte) ((length >> SHIFT_SIX) & SHIFT_SIX_BITMASK),
                            '\0', '\0', '\0', '\0'});


                    out.write(chars);
                    out.flush();

                    in = new BufferedReader(new InputStreamReader(zabbixConnection.getInputStream()));
                    LOGGER.debug("Message sent to Zabbix: " + message.toString());

                }
            }
        }
        catch (IOException e)
        {
            LOGGER.warn("Failed to establish connection to Zabbix", e);
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
                if (zabbixConnection != null)
                {
                    zabbixConnection.close();
                }
            }
            catch (IOException e)
            {

            }

        }
        return true;
    }
}
