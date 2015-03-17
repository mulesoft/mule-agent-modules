package com.mulesoft.agent.eventtracking;

import com.mulesoft.agent.domain.tracking.AgentTrackingNotification;
import org.junit.Assert;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseEventTrackingAgentTest{

    @Test
    public void CanUseSQLServerMSJdbc() throws ClassNotFoundException, SQLException {
        DatabaseEventTrackingAgent agent = new DatabaseEventTrackingAgent();
        agent.driver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
        agent.jdbcUrl = "jdbc:sqlserver://localhost;" +
                "instanceName=SQLExpress14;databaseName=Mule;user=sa;password=test;";
        agent.user = "sa";
        agent.pass = "test";
        agent.table = "mule";
        agent.postConfigurable();

        Connection conn = getConnection(agent);
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + agent.table);
        rs.next();
        long records = rs.getLong(1);
        agent.flush(createNotifications());

        ResultSet rs2 = st.executeQuery("SELECT COUNT(*) FROM " + agent.table);
        rs2.next();
        long records2 = rs2.getLong(1);

        Assert.assertEquals(records + 10, records2);
        rs.close();
        st.close();
        conn.close();
    }

    @Test
    public void CanUseSQLServerMSJtds()throws ClassNotFoundException, SQLException {
        DatabaseEventTrackingAgent agent = new DatabaseEventTrackingAgent();
        agent.driver = "net.sourceforge.jtds.jdbc.Driver";
        agent.jdbcUrl = "jdbc:jtds:sqlserver://EVA:1433/Mule;instance=SQLExpress14;" +
                "databaseName=Mule;integratedSecurity=true;";
        agent.user = "sa";
        agent.pass = "test";
        agent.table = "mule";
        agent.postConfigurable();

        Connection conn = getConnection(agent);
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + agent.table);
        rs.next();
        long records = rs.getLong(1);
        agent.flush(createNotifications());

        ResultSet rs2 = st.executeQuery("SELECT COUNT(*) FROM " + agent.table);
        rs2.next();
        long records2 = rs2.getLong(1);

        Assert.assertEquals(records + 10, records2);
        rs.close();
        st.close();
        conn.close();
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