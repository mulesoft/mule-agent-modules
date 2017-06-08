package com.mulesoft.agent.eventtracker.cloudhub.source;

/**
 * Configuration for Cloudhub S3.
 */
class CloudhubS3Configuration
{

    /**
     * The key that used to decrypt AWS access and secret keys in system properties.
     */
    private String encryptionKey;

    /**
     * S3 connection timeout.
     */
    private int connectionTimeout;

    /**
     * S3 socket timeout.
     */
    private int socketTimeout;

    /**
     * max connections to S3.
     */
    private int maxConnections;

    public String getEncryptionKey()
    {
        return encryptionKey;
    }

    public void setEncryptionKey(String encryptionKey)
    {
        this.encryptionKey = encryptionKey;
    }

    public int getConnectionTimeout()
    {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout)
    {
        this.connectionTimeout = connectionTimeout;
    }

    public int getSocketTimeout()
    {
        return socketTimeout;
    }

    public void setSocketTimeout(int socketTimeout)
    {
        this.socketTimeout = socketTimeout;
    }

    public int getMaxConnections()
    {
        return maxConnections;
    }

    public void setMaxConnections(int maxConnections)
    {
        this.maxConnections = maxConnections;
    }

}
