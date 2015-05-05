package com.mulesoft.agent.common.internalhandlers;

import com.mulesoft.agent.AgentEnableOperationException;
import com.mulesoft.agent.common.builders.MessageBuilder;
import com.mulesoft.agent.configuration.Configurable;
import com.mulesoft.agent.configuration.PostConfigure;
import com.mulesoft.agent.configuration.Type;
import com.mulesoft.agent.handlers.InternalMessageHandler;
import com.mulesoft.agent.services.OnOffSwitch;
import com.mulesoft.agent.services.OnOffSwitch.OnOffSwitchDisabler;
import com.mulesoft.agent.services.OnOffSwitch.OnOffSwitchEnabler;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.RollingRandomAccessFileAppender;
import org.apache.logging.log4j.core.appender.rolling.CompositeTriggeringPolicy;
import org.apache.logging.log4j.core.appender.rolling.DefaultRolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.SizeBasedTriggeringPolicy;
import org.apache.logging.log4j.core.appender.rolling.TimeBasedTriggeringPolicy;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.message.MapMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.zip.Deflater;

public abstract class AbstractLogInternalHandler<T> implements InternalMessageHandler<T>
{
    private final static Logger LOGGER = LoggerFactory.getLogger(AbstractLogInternalHandler.class);

    private String className = this.getClass().getName();
    private String loggerName = className + "." + "logger";
    private String appenderName = className + "." + "appender";
    private String contextName = className + "." + "context";
    private MessageBuilder<MapMessage> messageBuilder;
    private Configuration logConfiguration;
    private LoggerConfig loggerConfig;
    private Appender appender;
    private LoggerContext logContext;
    private boolean isConfigured = false;

    protected org.apache.logging.log4j.core.Logger internalLogger;
    protected OnOffSwitch enabledSwitch;

    /**
     * <p>
     * Flag to identify if the Internal Handler is enabled or not.
     * Default: true
     * </p>
     */
    @Configurable(value = "true", type = Type.DYNAMIC)
    public boolean enabled;

    /**
     * <p>
     * The name of the file to write to.
     * If the file, or any of its parent directories, do not exist, they will be created.
     * </p>
     */
    @Configurable(type = Type.DYNAMIC)
    public String fileName;

    /**
     * <p>
     * The pattern of the file name of the archived log file.
     * It will accept both a date/time pattern compatible with SimpleDateFormat and/or
     * a %i which represents an integer counter.
     * </p>
     */
    @Configurable(type = Type.DYNAMIC)
    public String filePattern;

    /**
     * <p>
     * Layout pattern used to write the events.
     * For more information see:
     * <a href="https://logging.apache.org/log4j/2.x/manual/layouts.html#PatternLayout">Pattern Layout</a>
     * Default: [%d] %map %n
     * </p>
     */
    @Configurable(value = "[%d] %map %n", type = Type.DYNAMIC)
    public String pattern;

    /**
     * <p>
     * The buffer size in bytes.
     * Default: 262144 (256 * 1024)
     * </p>
     */
    @Configurable(value = "262144", type = Type.DYNAMIC)
    public int bufferSize;

    /**
     * <p>
     * When set to true - the default, each write will be followed by a flush.
     * This will guarantee the data is written to disk but could impact performance.
     * Default: false
     * </p>
     */
    @Configurable(value = "false", type = Type.DYNAMIC)
    public boolean inmediateFlush;

    /**
     * <p>
     * Days to maintain on the current active log file before being rolled over to backup files.
     * Default: 1
     * </p>
     */
    @Configurable(value = "1", type = Type.DYNAMIC)
    public int daysTrigger;

    /**
     * <p>
     * Maximum size that the output file is allowed to reach before being rolled over to backup files.
     * Default: 100 (MB)
     * </p>
     */
    @Configurable(value = "100", type = Type.DYNAMIC)
    public int mbTrigger;

    /**
     * <p>
     * Date format used to format the timestamp.
     * Default: yyyy-MM-dd'T'HH:mm:ssZ
     * </p>
     */
    @Configurable(value = "yyyy-MM-dd'T'HH:mm:ss.SSSZ", type = Type.DYNAMIC)
    public String dateFormatPattern;


    protected MapMessage createMapMessage (Object message)
    {
        MapMessage mapMessage = this.messageBuilder.build(message);
        // Append additional properties
        Map<String, String> augmentation = augmentMapMessage(message);
        if (augmentation != null && !augmentation.isEmpty())
        {
            mapMessage.putAll(augmentation);
        }
        return mapMessage;
    }

    protected String getTimestampGetterName ()
    {
        return null;
    }

    protected Map<String, String> augmentMapMessage (Object message)
    {
        return null;
    }

    public void enable (boolean state) throws AgentEnableOperationException
    {
        this.enabledSwitch.switchTo(state);
    }

    public boolean isEnabled ()
    {
        return this.enabledSwitch.isEnabled();
    }

    protected abstract void buildLogMessage(org.apache.logging.log4j.core.Logger internalLogger, T message);

    protected abstract MessageBuilder<MapMessage> getMessageBuilder();

    @Override
    public boolean handle (T message)
    {
        if (this.isConfigured && this.isEnabled())
        {
            try
            {
                buildLogMessage(this.internalLogger, message);
                return true;
            }
            catch (Exception e)
            {
                LOGGER.error("There was an error logging the object.", e);
                return false;
            }
        }
        return false;
    }

    @PostConfigure
    public void postConfigurable () throws AgentEnableOperationException
    {
        LOGGER.trace("Configuring Log internal handler");
        this.isConfigured = false;

        if (this.enabledSwitch == null)
        {
            this.enabledSwitch = new OnOffSwitch(this.enabled,
                    new OnOffSwitchEnabler()
                    {
                        @Override
                        public void enable () throws AgentEnableOperationException
                        {
                            postConfigurable();
                        }
                    },
                    new OnOffSwitchDisabler()
                    {
                        @Override
                        public void disable () throws AgentEnableOperationException
                        {
                            postConfigurable();
                        }
                    }
            );
        }

        // Check if we should disable the loggers
        if (this.logContext != null)
        {
            this.appender.stop();
            this.loggerConfig.removeAppender(appenderName);
            this.logConfiguration.removeLogger(loggerName);
        }

        if (StringUtils.isEmpty(this.fileName)
                || StringUtils.isEmpty(this.filePattern))
        {
            LOGGER.error("Please review the Log Internal Handler configuration; " +
                    "You must configure at least the following properties: fileName and filePattern.");
            return;
        }

        try
        {
            this.logContext = new LoggerContext(contextName);
            this.logConfiguration = logContext.getConfiguration();

            this.messageBuilder = getMessageBuilder();

            Layout<? extends Serializable> layout = PatternLayout.createLayout(this.pattern, null, null, null, true, true, null, null);
            String dayTrigger = String.valueOf(TimeUnit.DAYS.toMillis(this.daysTrigger));
            String sizeTrigger = String.valueOf(this.mbTrigger * 1024 * 1024);
            TimeBasedTriggeringPolicy timeBasedTriggeringPolicy = TimeBasedTriggeringPolicy.createPolicy(dayTrigger, "true");
            SizeBasedTriggeringPolicy sizeBasedTriggeringPolicy = SizeBasedTriggeringPolicy.createPolicy(sizeTrigger);
            CompositeTriggeringPolicy policy = CompositeTriggeringPolicy.createPolicy(timeBasedTriggeringPolicy, sizeBasedTriggeringPolicy);
            DefaultRolloverStrategy strategy = DefaultRolloverStrategy.createStrategy("7", "1", "7",
                    Deflater.DEFAULT_COMPRESSION + "", this.logConfiguration);

            this.appender = RollingRandomAccessFileAppender.createAppender(this.fileName, this.filePattern, "true",
                    this.appenderName, String.valueOf(this.inmediateFlush), String.valueOf(this.bufferSize),
                    policy, strategy, layout, null, "false", null, null, this.logConfiguration);

            this.appender.start();

            AppenderRef[] ref = new AppenderRef[]{};
            this.loggerConfig = LoggerConfig.createLogger("false", Level.INFO, this.loggerName, "false", ref, null, null, null);
            this.loggerConfig.addAppender(this.appender, null, null);
            this.logConfiguration.addLogger(this.loggerName, this.loggerConfig);

            this.internalLogger = this.logContext.getLogger(this.loggerName);
        }
        catch (Exception e)
        {
            LOGGER.error(String.format("There was an error configuring the Log internal handler for file %s", this.fileName), e);
            return;
        }

        this.isConfigured = true;
        LOGGER.trace(String.format("Successfully configured the log internal handler for file %s", this.fileName));
    }
}
