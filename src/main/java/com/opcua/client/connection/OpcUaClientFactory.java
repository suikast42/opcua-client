package com.opcua.client.connection;


import com.prosysopc.ua.client.UaClient;

/**
 * Created by mj on 2018/1/11.
 *
 */
public interface OpcUaClientFactory {

    UaClient createUaClient() throws OpcUaClientException;

    String getUaAddress();

}
