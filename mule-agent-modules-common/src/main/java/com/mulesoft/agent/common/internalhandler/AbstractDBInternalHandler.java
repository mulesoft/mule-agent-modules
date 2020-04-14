/**
 * (c) 2003-2020 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */

package com.mulesoft.agent.common.internalhandler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collection;

import com.mulesoft.agent.buffer.BufferConfiguration;
import com.mulesoft.agent.buffer.BufferExhaustedAction;
import com.mulesoft.agent.buffer.BufferType;
import com.mulesoft.agent.buffer.BufferedHandler;
import com.mulesoft.agent.configuration.Configurable;
import com.mulesoft.agent.configuration.Password;
import com.mulesoft.agent.configuration.Type;
import com.mulesoft.agent.handlers.exception.InitializationException;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.Serializable;

/**
 * Database internal handler.
 *
 * @param <T> Message type.
 */
public abstract class AbstractDBInternalHandler<T extends Serializable> extends BufferedHandler<T>
{
    private static final Logger LOGGER = LogManager.getLogger(AbstractDBInternalHandler.class);
    private static final int DEFAULT_BUFFER_RETRY_COUNT = 3;
    private static final long DEFAULT_BUFFER_FLUSH_FREQUENCY = 10000L;
    private static final int DEFAULT_BUFFER_MAXIMUM_CAPACITY = 1000;
    private static final boolean DEFAULT_BUFFER_DISCARD_MESSAGES_ON_FLUSH_FAILURE = false;

    /**
     * <p>
     * JDBC driver to use to communicate with the database server.
     * </p>
     */
    @Configurable(type = Type.DYNAMIC)
    String driver;

    /**
     * <p>
     * JDBC URL to connect to the database server.
     * </p>
     */
    @Configurable(type = Type.DYNAMIC)
    String jdbcUrl;

    /**
     * <p>
     * Username in the database server.
     * </p>
     */
    @Configurable(type = Type.DYNAMIC)
    String user;

    /**
     * <p>
     * Password for the database user.
     * </p>
     */
    @Password
    @Configurable(type = Type.DYNAMIC)

    String pass;

    protected abstract void insert(Connection connection, Collection<T> messages) throws SQLException;

    @Override
    protected boolean canHandle(T message)
    {
        return true;
    }

    @Override
    protected boolean flush(Collection<T> messages)
    {
        LOGGER.debug(String.format("Flushing %s messages.", messages.size()));

        Connection connection = null;
        try
        {
            connection = DriverManager.getConnection(this.jdbcUrl, this.user, this.pass);
            connection.setAutoCommit(false);
            try
            {
                insert(connection, messages);
                connection.commit();
            }
            catch (Exception ex)
            {
                LOGGER.error("There was an error inserting the messages. Rolling back the transaction.", ex);
                try
                {
                    connection.rollback();
                }
                catch (SQLException sqlEx)
                {
                    LOGGER.error("There was an error while rolling back the transaction.", sqlEx);
                }
            }

            return true;
        }
        catch (Throwable e)
        {
            LOGGER.error("Couldn't insert the tracking notifications.", e);
            return false;
        }
        finally
        {
            if (connection != null)
            {
                try
                {
                    connection.close();
                }
                catch (SQLException e)
                {
                    LOGGER.error("Error closing the database.", e);
                }
            }
        }
    }

    @Override
    public void initialize() throws InitializationException
    {
        LOGGER.debug("Configuring the Common DB Internal Handler...");

        if (StringUtils.isEmpty(this.driver)
                || StringUtils.isEmpty(this.jdbcUrl))
        {
            throw new InitializationException("Please review configuration; "
                    + "you must configure the following properties: driver and jdbcUrl.");
        }

        try
        {
            Class.forName(driver);
        }
        catch (ClassNotFoundException e)
        {
            throw new InitializationException(String.format("Couldn't load the database driver '%s'. "
                    + "Did you copy the JAR driver to the {MULE_HOME}/plugins/mule-agent-plugin/lib?", driver), e);
        }

        try
        {
            LOGGER.debug("Testing database connection...");
            DriverManager.getConnection(this.jdbcUrl, this.user, this.pass).close();
            LOGGER.debug("Database connection OK!.");
        }
        catch (SQLException e)
        {
            throw new InitializationException("There was an error on the connection to the DataBase. "
                    + "Please review your agent configuration.", e);
        }

        LOGGER.debug("Successfully configured the Common DB Internal Handler.");
        super.initialize();
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
            defaultBuffer.setDiscardMessagesOnFlushFailure(DEFAULT_BUFFER_DISCARD_MESSAGES_ON_FLUSH_FAILURE);
            defaultBuffer.setWhenExhausted(BufferExhaustedAction.FLUSH);
            return defaultBuffer;
        }
    }

    public String getDriver()
    {
        return driver;
    }

    public void setDriver(String driver)
    {
        this.driver = driver;
    }

    public String getJdbcUrl()
    {
        return jdbcUrl;
    }

    public void setJdbcUrl(String jdbcUrl)
    {
        this.jdbcUrl = jdbcUrl;
    }

    public String getUser()
    {
        return user;
    }

    public void setUser(String user)
    {
        this.user = user;
    }

    public String getPass()
    {
        return pass;
    }

    public void setPass(String pass)
    {
        this.pass = pass;
    }
}
