package com.mulesoft.agent.eventtracking;

import com.mchange.v2.c3p0.DataSources;
import com.mulesoft.agent.AgentEnableOperationException;
import com.mulesoft.agent.buffer.BufferedHandler;
import com.mulesoft.agent.configuration.Configurable;
import com.mulesoft.agent.domain.tracking.AgentTrackingNotification;
import com.sun.deploy.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.sql.DataSource;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;

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
        }

        try {
            DataSource unpooled = DataSources.unpooledDataSource(jdbcUrl,user,pass);
            pooledDataSource = DataSources.pooledDataSource( unpooled );
        } catch (SQLException e) {
            LOGGER.error(String.format("There was an error on the connection to the DataBase. Please review your agent configuration."), e);
            setEnabled(false);
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
        dump(messages);

        try(Connection connection = pooledDataSource.getConnection()){
            try(PreparedStatement statement = connection.prepareStatement(insertStatement)){
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
            }
        } catch (SQLException e) {
            LOGGER.warn("Couldn't insert the tracking notifications.",e);
        }

        return true;
    }

    public static boolean isNullOrWhiteSpace(String a) {
        return a == null || (a.length() > 0 && a.trim().length() <= 0);
    }

    private void dump(Collection<AgentTrackingNotification> notifications){
        try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("C:\\dbagent-debug.txt", true)))) {
            for (AgentTrackingNotification notification : notifications){
                out.println("Action: " + notification.getAction());
                out.println("Application: " + notification.getApplication());
                out.println("MuleMessage: " + notification.getMuleMessage());
                out.println("MuleMessageId: " + notification.getMuleMessageId());
                out.println("NotificationType: " + notification.getNotificationType());
                out.println("Path: " + notification.getPath());
                out.println("ResourceIdentifier: " + notification.getResourceIdentifier());
                out.println("Timestamp: " + notification.getTimestamp());
                out.println("Source: " + notification.getSource());
                for(Annotation annotation : notification.getAnnotations()){
                    out.println("\tAnnotationType: "+annotation.annotationType());
                    out.println("\tClass: "+annotation.getClass());
                }
                out.println("--------------------------------------------------------");
            }
        }catch (IOException e) {
            //
        }
    }
}
