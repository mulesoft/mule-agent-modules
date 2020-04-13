/**
 * (c) 2003-2020 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */

package com.mulesoft.agent.monitoring.publisher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;

import com.googlecode.jsendnsca.Level;
import com.googlecode.jsendnsca.MessagePayload;
import com.googlecode.jsendnsca.NagiosException;
import com.googlecode.jsendnsca.NagiosPassiveCheckSender;
import com.googlecode.jsendnsca.NagiosSettings;
import com.googlecode.jsendnsca.builders.MessagePayloadBuilder;
import com.googlecode.jsendnsca.builders.NagiosSettingsBuilder;
import com.googlecode.jsendnsca.encryption.Encryption;
import com.mulesoft.agent.buffer.BufferedHandler;
import com.mulesoft.agent.configuration.Configurable;
import com.mulesoft.agent.domain.monitoring.Metric;
import com.mulesoft.agent.services.OnOffSwitch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * <p>
 * Handler that publishes JMX information obtained from the Monitoring Service to a running Nagios instance.
 * Utilizes the NCSA protocol.
 * </p>
 */
@Named("mule.agent.nagios.jmx.internal.handler")
@Singleton
public class NagiosMonitorPublisher extends BufferedHandler<ArrayList<Metric>>
{
    private static final Logger LOGGER = LogManager.getLogger(NagiosMonitorPublisher.class);

    @Configurable("com.mulesoft.agent")
    String host;

    @Configurable("0.0.0.0")
    String nagiosServer;

    @Configurable("5667")
    int nagiosPort;

    @Configurable("XOR")
    String encryptionMethod;

    @Configurable
    String password;

    @Inject
    public NagiosMonitorPublisher()
    {
        super();
    }

    public NagiosMonitorPublisher(OnOffSwitch enabledSwitch)
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
    public boolean flush(Collection<ArrayList<Metric>> listOfMetrics)
    {
        Encryption encryption = Encryption.NONE;

        if ("xor".equalsIgnoreCase(this.encryptionMethod))
        {
            encryption = Encryption.XOR;
        }
        if ("triple_des".equalsIgnoreCase(this.encryptionMethod))
        {
            encryption = Encryption.TRIPLE_DES;
        }

        NagiosSettings settings = new NagiosSettingsBuilder()
                .withNagiosHost(this.nagiosServer)
                .withPort(this.nagiosPort)
                .withEncryption(encryption)
                .withPassword(this.password)
                .create();

        NagiosPassiveCheckSender sender = new NagiosPassiveCheckSender(settings);

        for (List<Metric> metrics : listOfMetrics)
        {
            for (Metric metric : metrics)
            {
                MessagePayload payload = new MessagePayloadBuilder()
                        .withHostname(this.host)
                        .withLevel(Level.OK)
                        .withServiceName(metric.getName())
                        .withMessage(metric.getValue().toString())
                        .create();

                try
                {
                    sender.send(payload);
                    LOGGER.debug("Message: " + payload.toString());
                }
                catch (NagiosException e)
                {
                    LOGGER.warn("Could not send metric information to Nagios (NagiosException): " + e.getMessage());
                    LOGGER.debug("NagiosException sending metric information to Nagios", e);
                    return false;
                }
                catch (IOException e)
                {
                    LOGGER.warn("Could not send metric information to Nagios (IOException): " + e.getMessage());
                    LOGGER.debug("IOException sending metric information to Nagios", e);
                    return false;
                }
            }
        }

        return true;
    }
}
