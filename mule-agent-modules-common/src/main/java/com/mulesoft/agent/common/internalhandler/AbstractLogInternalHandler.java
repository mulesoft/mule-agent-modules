package com.mulesoft.agent.common.internalhandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mulesoft.agent.AgentEnableOperationException;
import com.mulesoft.agent.buffer.BufferConfiguration;
import com.mulesoft.agent.buffer.BufferExhaustedAction;
import com.mulesoft.agent.buffer.BufferType;
import com.mulesoft.agent.buffer.BufferedHandler;
import com.mulesoft.agent.common.internalhandler.serializer.DefaultObjectMapperFactory;
import com.mulesoft.agent.configuration.Configurable;
import com.mulesoft.agent.configuration.PostConfigure;
import com.mulesoft.agent.configuration.Type;
import com.mulesoft.agent.handlers.InitializableInternalMessageHandler;
import com.mulesoft.agent.handlers.exception.InitializationException;
import com.mulesoft.agent.services.OnOffSwitch;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.zip.Deflater;

public abstract class AbstractLogInternalHandler<T> extends BufferedHandler<T>
{
    private final static Logger LOGGER = LoggerFactory.getLogger(AbstractLogInternalHandler.class);
    public static String MULE_HOME_PLACEHOLDER = "$MULE_HOME";
    public static String PATTERN_LAYOUT = "%m%n";

    private static final int DEFAULT_BUFFER_RETRY_COUNT = 3;
    private static final long DEFAULT_BUFFER_FLUSH_FREQUENCY = 10000L;
    private static final int DEFAULT_BUFFER_MAXIMUM_CAPACITY = 1000;
    private static final boolean DEFAULT_DISCARD_ON_FAILURE = false;

    private String className = this.getClass().getName();
    private String loggerName = className + "." + "logger";
    private String appenderName = className + "." + "appender";
    private String contextName = className + "." + "context";
    private Configuration logConfiguration;
    private LoggerConfig loggerConfig;
    private Appender appender;
    private LoggerContext logContext;
    private ObjectMapper objectMapper;

    protected org.apache.logging.log4j.core.Logger internalLogger;
    protected OnOffSwitch enabledSwitch;

    /**
     * <p>
     * Flag to identify if the Internal Handler is enabled or not.
     * Default: false
     * </p>
     */
    @Configurable(value = "false")
    public boolean enabled;

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
     * Default: true
     * </p>
     */
    @Configurable(value = "true", type = Type.DYNAMIC)
    public boolean immediateFlush;

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
     * Default: yyyy-MM-dd'T'HH:mm:ss.SZ
     * </p>
     */
    @Configurable(value = "yyyy-MM-dd'T'HH:mm:ss.SZ", type = Type.DYNAMIC)
    public String dateFormatPattern;

    protected abstract String getFileName();

    protected abstract String getFilePattern();

    protected ObjectMapper getObjectMapper()
    {
        return this.objectMapper;
    }

    @Override
    public BufferConfiguration getBuffer()
    {
        if (buffer != null)
        {
            return buffer;
        }
        else
        {
            BufferConfiguration defaultBuffer = new BufferConfiguration();
            defaultBuffer.setType(BufferType.MEMORY);
            defaultBuffer.setRetryCount(DEFAULT_BUFFER_RETRY_COUNT);
            defaultBuffer.setFlushFrequency(DEFAULT_BUFFER_FLUSH_FREQUENCY);
            defaultBuffer.setMaximumCapacity(DEFAULT_BUFFER_MAXIMUM_CAPACITY);
            defaultBuffer.setDiscardMessagesOnFlushFailure(DEFAULT_DISCARD_ON_FAILURE);
            defaultBuffer.setWhenExhausted(BufferExhaustedAction.FLUSH);
            return defaultBuffer;
        }
    }

    @Override
    protected boolean canHandle(T message)
    {
        return true;
    }

    @Override
    protected boolean flush(Collection<T> collection)
    {
        try
        {
            for (T message : collection)
            {
                this.internalLogger.info(objectMapper.writeValueAsString(message));
            }
            return true;
        }
        catch (Exception e)
        {
            LOGGER.error("There was an error logging the object.", e);
            return false;
        }
    }

    @Override
    public void initialize() throws InitializationException
    {
        LOGGER.debug("Configuring the Common Log Internal Handler...");
        super.initialize();

        // Check if we should disable the loggers
        if (this.logContext != null)
        {
            this.appender.stop();
            this.loggerConfig.removeAppender(appenderName);
            this.logConfiguration.removeLogger(loggerName);
        }

        try
        {
            this.logContext = new LoggerContext(contextName);
            this.logConfiguration = logContext.getConfiguration();

            Layout<? extends Serializable> layout = PatternLayout.createLayout(PATTERN_LAYOUT, null, null, null, true, true, null, null);
            String dayTrigger = TimeUnit.DAYS.toMillis(this.daysTrigger) + "";
            String sizeTrigger = (this.mbTrigger * 1024 * 1024) + "";
            TimeBasedTriggeringPolicy timeBasedTriggeringPolicy = TimeBasedTriggeringPolicy.createPolicy(dayTrigger, "true");
            SizeBasedTriggeringPolicy sizeBasedTriggeringPolicy = SizeBasedTriggeringPolicy.createPolicy(sizeTrigger);
            CompositeTriggeringPolicy policy = CompositeTriggeringPolicy.createPolicy(timeBasedTriggeringPolicy, sizeBasedTriggeringPolicy);
            DefaultRolloverStrategy strategy = DefaultRolloverStrategy.createStrategy("7", "1", "7",
                    Deflater.DEFAULT_COMPRESSION + "", this.logConfiguration);

            String fileName = this.getFileName().replace(MULE_HOME_PLACEHOLDER, System.getProperty("mule.home"));
            String filePattern = this.getFilePattern().replace(MULE_HOME_PLACEHOLDER, System.getProperty("mule.home"));
            this.appender = RollingRandomAccessFileAppender.createAppender(fileName, filePattern, "true",
                    this.appenderName, this.immediateFlush + "", this.bufferSize + "",
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
            throw new InitializationException("There was an error configuring the AbstractLogInternalHandler internal handler.", e);
        }

        this.objectMapper = new DefaultObjectMapperFactory(this.dateFormatPattern).create();

        LOGGER.debug("Successfully configured the Common Log Internal Handler.");
    }
}
