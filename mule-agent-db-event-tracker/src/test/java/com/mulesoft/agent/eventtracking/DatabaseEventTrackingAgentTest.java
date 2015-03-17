package com.mulesoft.agent.eventtracking;

import com.mulesoft.agent.domain.tracking.AgentTrackingNotification;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.lang.annotation.Annotation;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@RunWith(Parameterized.class)
public class DatabaseEventTrackingAgentTest{

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                // SQL Server - jTDS Driver
                {   "net.sourceforge.jtds.jdbc.Driver",
                    "jdbc:jtds:sqlserver://EVA:1433/Mule;instance=SQLExpress14;" +
                        "databaseName=Mule;integratedSecurity=true;",
                        "sa",
                        "test",
                        "mule"
                },
                // SQL Server - MS SQL Server
                {   "com.microsoft.sqlserver.jdbc.SQLServerDriver",
                        "jdbc:sqlserver://localhost;" +
                            "instanceName=SQLExpress14;databaseName=Mule;user=sa;password=test;",
                        "sa",
                        "test",
                        "mule"
                },
                // MySQL
                {   "com.mysql.jdbc.Driver",
                        "jdbc:mysql://192.168.61.128/mule",
                        "root",
                        "test",
                        "mule"
                },
                // PostgreSQL
                {   "com.mysql.jdbc.Driver",
                        "jdbc:postgresql://192.168.61.128:5432/Mule",
                        "postgres",
                        "test",
                        "mule"
                },

        });
    }

    public String driver;
    public String jdbcUrl;
    public String user;
    public String pass;
    public String table;

    public DatabaseEventTrackingAgentTest(String driver, String jdbcUrl, String user, String pass, String table){
        this.driver = driver;
        this.jdbcUrl = jdbcUrl;
        this.user = user;
        this.pass = pass;
        this.table = table;
    }

    @Test
    public void test() throws SQLException, ClassNotFoundException {
        DatabaseEventTrackingAgent agent = new DatabaseEventTrackingAgent();
        agent.driver = this.driver;
        agent.jdbcUrl = this.jdbcUrl;
        agent.user = this.user;
        agent.pass = this.pass;
        agent.table = this.table;
        agent.postConfigurable();

        Connection conn = getConnection(agent);
        clearTable(conn, agent);
        List<AgentTrackingNotification> notifications = createNotifications();
        agent.flush(notifications);
        long insertedRecords = countRecords(conn, agent);

        Assert.assertEquals(notifications.size(), insertedRecords);
        conn.close();
    }

    private long countRecords(Connection connection, DatabaseEventTrackingAgent agent) throws SQLException {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + agent.table + ";");
        rs.next();
        long count = rs.getLong(1);
        rs.close();
        st.close();
        return count;
    }

    private void clearTable(Connection connection, DatabaseEventTrackingAgent agent) throws SQLException {
        Statement st = connection.createStatement();
        st.execute("DELETE FROM " + agent.table + ";");
        st.close();
    }

    private Connection getConnection(DatabaseEventTrackingAgent agent) throws ClassNotFoundException, SQLException {
        Class.forName(agent.driver);
        return DriverManager.getConnection(agent.jdbcUrl, agent.user, agent.pass);
    }

    private List<AgentTrackingNotification> createNotifications(){
        List<AgentTrackingNotification> list = new ArrayList<AgentTrackingNotification>();
        for(int i = 0; i < 10; i++){
            list.add(new AgentTrackingNotification.TrackingNotificationBuilder()
                    .action("TEST " + i)
                    .annotations(new ArrayList<Annotation>())
                    .build());
        }
        return list;
    }
}