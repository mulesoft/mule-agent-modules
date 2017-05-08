package com.mulesoft.agent.eventtracker.cloudhub.tracking;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mulesoft.agent.eventtracker.cloudhub.InsightEvent;
import com.mulesoft.agent.eventtracker.cloudhub.InsightException;
import com.mulesoft.agent.eventtracker.cloudhub.InsightSendingException;
import com.mulesoft.agent.handlers.exception.InitializationException;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.ListenableFuture;
import com.ning.http.client.Request;
import com.ning.http.client.Response;

public class CloudhubInsightClientTest
{

    CloudhubInsightClient client;
    AsyncHttpClient httpClient;

    ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
    Response response = mock(Response.class);

    @Before
    public void setUp() throws Exception
    {
        System.setProperty("application.id", "appId");
        System.setProperty("ion.api.token", "appToken");
        System.setProperty("platform.services.endpoint", "http://platform");
        client = new CloudhubInsightClient(1000, 2000);
        client.init();
        httpClient = spy(client.getHttpClient());
        client.setHttpClient(httpClient);

        @SuppressWarnings("unchecked")
        ListenableFuture<Response> future = mock(ListenableFuture.class);
        when(future.get()).thenReturn(response);
        doReturn(future).when(httpClient).executeRequest(any(Request.class));
        when(response.getStatusCode()).thenReturn(200);
    }

    @Test
    public void testHttpClientConfig()
    {
        assertTrue(httpClient.getConfig().isCompressionEnforced());
        assertEquals(1000, httpClient.getConfig().getConnectTimeout());
        assertEquals(2000, httpClient.getConfig().getRequestTimeout());
    }

    @Test(expected = InitializationException.class)
    public void testMissingAppId() throws InitializationException
    {
        System.clearProperty("application.id");
        client.init();
    }

    @Test(expected = InitializationException.class)
    public void testMissingAppToken() throws InitializationException
    {
        System.clearProperty("ion.api.token");
        client.init();
    }

    @Test(expected = InitializationException.class)
    public void testMissingPlatformHost() throws InitializationException
    {
        System.clearProperty("platform.services.endpoint");
        client.init();
    }

    @Test
    public void testSendInsightEvents() throws JsonParseException, JsonMappingException, IOException, InsightException
    {
        List<InsightEvent> events = createEvents();
        client.sendInsight(events);

        verify(httpClient, times(1)).executeRequest(requestCaptor.capture());

        assertEquals("PUT", requestCaptor.getValue().getMethod());
        assertEquals("http://platform/v2/tracking/appId/events", requestCaptor.getValue().getUrl());
        assertEquals(3, requestCaptor.getValue().getHeaders().size());
        assertEquals(Arrays.asList("application/json; charset=UTF-8"),
                requestCaptor.getValue().getHeaders().get("Content-Type"));
        assertEquals(Arrays.asList("appToken"), requestCaptor.getValue().getHeaders().get("X-ION-Authenticate"));
        assertEquals(Arrays.asList("appId"), requestCaptor.getValue().getHeaders().get("X-ION-Application"));
        assertEquals("UTF-8", requestCaptor.getValue().getBodyEncoding());
        List<Map<String, Object>> body = new ObjectMapper().readValue(requestCaptor.getValue().getStringData(),
                new TypeReference<List<Map<String, Object>>>()
                {
                });
        assertEquals(1, body.size());
        assertEquals(events.get(0).getId(), body.get(0).get("id"));
        assertEquals(events.get(0).getMessageId(), body.get(0).get("messageId"));
        assertEquals(events.get(0).getName(), body.get(0).get("name"));
        assertEquals(events.get(0).getFlowName(), body.get(0).get("flowName"));
        assertEquals(events.get(0).getType(), body.get(0).get("type"));
        assertEquals(events.get(0).getTimestamp() == null ? null : events.get(0).getTimestamp().intValue(),
                body.get(0).get("timestamp"));
        assertEquals(Collections.singletonMap("cKey", "cValue"), body.get(0).get("customProperties"));
        assertEquals(Collections.singletonMap("sKey", "sValue"), body.get(0).get("systemProperties"));
    }

    @Test(expected = InsightSendingException.class)
    public void testBadRequestResponse() throws InsightException
    {
        when(response.getStatusCode()).thenReturn(400);
        client.sendInsight(createEvents());
    }

    @Test(expected = InsightSendingException.class)
    public void testInternalErrorResponse() throws InsightException
    {
        when(response.getStatusCode()).thenReturn(500);
        client.sendInsight(createEvents());
    }

    @Test(expected = InsightSendingException.class)
    public void testFailedToExecute() throws InsightException
    {
        doThrow(ExecutionException.class).when(httpClient).executeRequest(any(Request.class));
        client.sendInsight(createEvents());
    }

    @Test(expected = InsightSendingException.class)
    public void testInterrupted() throws InsightException
    {
        doThrow(InterruptedException.class).when(httpClient).executeRequest(any(Request.class));
        client.sendInsight(createEvents());
    }

    private List<InsightEvent> createEvents()
    {
        return Arrays.asList(new InsightEvent.Builder().withId("id")
                .withMessageId("messageId")
                .withName("name")
                .withFlowName("flowName")
                .withType("type")
                .withTimestamp(0l)
                .withCustomProperties(Collections.singletonMap("cKey", "cValue"))
                .withSystemProperties(Collections.singletonMap("sKey", "sValue"))
                .build());
    }
}
