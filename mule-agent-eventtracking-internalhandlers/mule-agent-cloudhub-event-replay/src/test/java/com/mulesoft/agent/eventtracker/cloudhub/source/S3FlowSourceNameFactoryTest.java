package com.mulesoft.agent.eventtracker.cloudhub.source;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class S3FlowSourceNameFactoryTest
{

    private String appId = "appId";

    S3FlowSourceNameFactory factory;

    @Before
    public void setUp() throws Exception
    {
        factory = new S3FlowSourceNameFactory(appId);
    }

    @Test
    public void testName()
    {
        assertEquals("ch-appName-appId/messageId/flowName", factory.build("appName", "messageId", "flowName"));
    }

    @Test(expected = NullPointerException.class)
    public void testNullAppId()
    {
        new S3FlowSourceNameFactory(null);
    }

    @Test(expected = NullPointerException.class)
    public void testNullAppName()
    {
        factory.build(null, "messageId", "flowName");
    }

    @Test(expected = NullPointerException.class)
    public void testNullMessageId()
    {
        factory.build("appName", null, "flowName");
    }

    @Test(expected = NullPointerException.class)
    public void testNullFlowName()
    {
        factory.build("appName", "messageId", null);
    }
}
