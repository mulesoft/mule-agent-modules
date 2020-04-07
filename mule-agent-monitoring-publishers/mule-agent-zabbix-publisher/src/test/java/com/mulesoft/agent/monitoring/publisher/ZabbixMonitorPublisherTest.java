/**
 * (c) 2003-2014 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */

package com.mulesoft.agent.monitoring.publisher;

import com.mulesoft.agent.exception.AgentEnableOperationException;
import com.mulesoft.agent.domain.monitoring.Metric;
import com.mulesoft.agent.services.OnOffSwitch;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class ZabbixMonitorPublisherTest
{
    private OnOffSwitch enabledSwitch = mock(OnOffSwitch.class);

    public ZabbixMonitorPublisher publisher = new ZabbixMonitorPublisher(enabledSwitch);
    @Before
    public void setup()
    {
        publisher.host = "com.mulesoft.agent";
        publisher.zabbixServer = "0.0.0.0";
        publisher.zabbixPort = 10051;
        when(enabledSwitch.isEnabled()).thenReturn(true);
    }

    @Test
    public void defaultConstructorMustExistForGuice() throws NoSuchMethodException
    {
        new ZabbixMonitorPublisher();
        assertTrue(ZabbixMonitorPublisher.class.getConstructor().getAnnotation(Inject.class) != null);
    }

    @Test
    public void ifDisabledDoNothing() throws AgentEnableOperationException
    {
        when(enabledSwitch.isEnabled()).thenReturn(false);
        ArrayList<Metric> testMetrics = new ArrayList<Metric>();
        Metric mockedMetric = mock(Metric.class);
        testMetrics.add(mockedMetric);
        Assert.assertFalse(publisher.handle(testMetrics));
        verify(mockedMetric, times(0)).getName();
        verify(mockedMetric, times(0)).getValue();
        when(enabledSwitch.isEnabled()).thenReturn(true);
    }

    @Test
    public void failWhenAddressIsWrong()
    {
        ArrayList<Metric> testMetrics = new ArrayList<Metric>();
        Metric mockedMetric = mock(Metric.class);
        testMetrics.add(mockedMetric);
        Assert.assertFalse(publisher.flush(Collections.singletonList(testMetrics)));
        verify(mockedMetric, times(0)).getName();
        verify(mockedMetric, times(0)).getValue();
    }

    @Test
    public void doHandle() throws IOException, InterruptedException
    {
        ArrayList<Metric> testMetrics = new ArrayList<Metric>();
        Metric mockedMetric = mock(Metric.class);
        when(mockedMetric.getName()).thenReturn("aName");
        when(mockedMetric.getValue()).thenReturn(0);
        testMetrics.add(mockedMetric);
        int openPort = getOpenPort(7777,7999);
        publisher.zabbixServer = "127.0.0.1";
        publisher.zabbixPort = openPort;
        ServerSocket serverSocket = new ServerSocket(openPort);
        TestSocketThread testSocketThread = new TestSocketThread(serverSocket);
        testSocketThread.start();
        Assert.assertTrue(publisher.handle(testMetrics));
        verify(mockedMetric, times(1)).getName();
        verify(mockedMetric, times(1)).getValue();
        publisher.zabbixServer = "0.0.0.0";
        publisher.zabbixPort = 10051;
        testSocketThread.join(200);
        serverSocket.close();
    }

    private static int getOpenPort(int rangeMin, int rangeMax) {
        int scanStart = rangeMin + (int) (Math.random() * (rangeMax - rangeMin));
        int port = scanStart;
        while(!available(port)){
            port++;
            if(port >= rangeMax)
                port = rangeMin;
            if(port == scanStart)
                return -1;
        }

        return port;
    }

    private static boolean available(int port) {
        ServerSocket ss = null;
        try {
            ss = new ServerSocket(port);
            ss.setReuseAddress(true);
            return true;
        } catch (IOException e) {
        } finally {
            if (ss != null) {
                try
                {
                    ss.close();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    private class TestSocketThread extends Thread
    {
        private ServerSocket serverSocket;

        public TestSocketThread(ServerSocket serverSocket)
        {

            this.serverSocket = serverSocket;
        }

        public void run()
        {
            try
            {
                Socket connectionSocket = serverSocket.accept();
            }
            catch (Exception e)
            {
            }
        }
    }
}
