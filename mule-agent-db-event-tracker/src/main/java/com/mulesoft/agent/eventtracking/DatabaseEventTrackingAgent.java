package com.mulesoft.agent.eventtracking;

import com.mchange.v2.c3p0.DataSources;
import com.mulesoft.agent.buffer.BufferedHandler;
import com.mulesoft.agent.configuration.Configurable;
import com.mulesoft.agent.domain.tracking.AgentTrackingNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.sql.DataSource;
import java.lang.annotation.Annotation;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;

@Named("database.agent.eventtracking")
@Singleton
public class DatabaseEventTrackingAgent extends BufferedHandler<AgentTrackingNotification> {
    private final static Logger LOGGER = LoggerFactory.getLogger(DatabaseEventTrackingAgent.class);

    @Configurable
    String driver;

    @Configurable
    String jdbcUrl;

    @Configurable
    String user;

    @Configurable
    String pass;

    @Configurable("MULE_EVENTS")
    String table;

    private DataSource pooledDataSource;
    private String insertStatement;

    @Override
    public void postConfigurable(){
        super.postConfigurable();
        LOGGER.trace("Starting the DatabaseEventTrackingAgent");

        if(isNullOrWhiteSpace(driver)
                ||isNullOrWhiteSpace(jdbcUrl)
                ||isNullOrWhiteSpace(user)
                ||isNullOrWhiteSpace(pass)){

            LOGGER.error("Please review the DatabaseEventTrackingAgent (database.agent.eventtracking) configuration; " +
                    "You must configure the following properties: driver, jdbcUrl, user and pass.");

            setEnabled(false);
            return;
        }

        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            LOGGER.error(String.format("The DatabaseEventTrackingAgent (database.agent.eventtracking) couldn't load the database driver '%s'. " +
                    "Did you copy the JAR driver to the {MULE_HOME}/plugins/mule-agent-plugin/lib?", driver), e);
            setEnabled(false);
            return;
        }

        try {
            DataSource unpooled = DataSources.unpooledDataSource(jdbcUrl,user,pass);
            pooledDataSource = DataSources.pooledDataSource( unpooled );
        } catch (SQLException e) {
            LOGGER.error(String.format("There was an error on the connection to the DataBase. Please review your agent configuration."), e);
            setEnabled(false);
            return;
        }

        insertStatement = String.format("INSERT INTO %s (Action, Application, MuleMessage, MuleMessageId, NotificationType, Path, ResourceIdentifier, Timestamp, Source) " +
                "VALUES (?,?,?,?,?,?,?,?,?)", table);
        LOGGER.trace("Insert SQL Statement: " + insertStatement);
    }

    @Override
    protected boolean canHandle(AgentTrackingNotification message) {
        return true;
    }

    @Override
    protected boolean flush(Collection<AgentTrackingNotification> messages) {
        if(isEnabled()){
            LOGGER.trace(String.format("Flushing %s notifications.", messages.size()));

            dump(messages);
            Connection connection = null;
            try{
                connection = pooledDataSource.getConnection();
                PreparedStatement statement = null;
                try{
                    statement = connection.prepareStatement(insertStatement);
                    for(AgentTrackingNotification notification : messages){
                        statement.setString(1, notification.getAction());
                        statement.setString(2, notification.getApplication());
                        statement.setString(3, notification.getMuleMessage());
                        statement.setString(4, notification.getMuleMessageId());
                        statement.setString(5, notification.getNotificationType());
                        statement.setString(6, notification.getPath());
                        statement.setString(7, notification.getResourceIdentifier());
                        statement.setLong(8, notification.getTimestamp());
                        statement.setString(9, notification.getSource());
                        statement.addBatch();
                    }

                    statement.executeBatch();
                } finally {
                    if(statement != null) statement.close();
                }

                return true;
            } catch (SQLException e) {
                LOGGER.warn("Couldn't insert the tracking notifications.",e);
                return false;
            } finally {
                if(connection != null) try {
                    connection.close();
                } catch (SQLException e) {
                    LOGGER.error("Error closing the database.", e);
                }
            }
        }

        return false;
    }

    public static boolean isNullOrWhiteSpace(String a) {
        return a == null || (a.length() > 0 && a.trim().length() <= 0);
    }

    private void dump(Collection<AgentTrackingNotification> notifications){
            for (AgentTrackingNotification notification : notifications){
                LOGGER.info("Action: " + notification.getAction());
                LOGGER.info("Date: " + new Date());
                LOGGER.info("Action: " + notification.getAction());
                LOGGER.info("Application: " + notification.getApplication());
                LOGGER.info("MuleMessage: " + notification.getMuleMessage());
                LOGGER.info("MuleMessageId: " + notification.getMuleMessageId());
                LOGGER.info("NotificationType: " + notification.getNotificationType());
                LOGGER.info("Path: " + notification.getPath());
                LOGGER.info("ResourceIdentifier: " + notification.getResourceIdentifier());
                LOGGER.info("Timestamp: " + notification.getTimestamp());
                LOGGER.info("Source: " + notification.getSource());
                for(Annotation annotation : notification.getAnnotations()){
                    LOGGER.info("\tAnnotationType: " + annotation.annotationType());
                    LOGGER.info("\tClass: " + annotation.getClass());
                }
                LOGGER.info("--------------------------------------------------------");
            }
    }
}
