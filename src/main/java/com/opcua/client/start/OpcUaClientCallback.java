package com.opcua.client.start;

/**
 * Created by mj on 2018/1/11.
 */
public interface OpcUaClientCallback<T> {

    T performAction() throws Exception;

}
