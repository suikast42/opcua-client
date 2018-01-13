package com.opcua.client.config;

import com.opcua.client.connection.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by mj on 2018/1/11.
 *
 */
@Configuration
public class OpcUaConfiguration {

    @Bean
    OpcUaClientFactory opcUaClientFactory(OpcUaProperties opcUaProperties) {
        AutoReconnectUaClientFactory opcUaClientFactory = new AutoReconnectUaClientFactory();
        opcUaClientFactory.setUaAddress(opcUaProperties.getAddress());
        return opcUaClientFactory;
    }

    @Bean/*(destroyMethod = "close")*/
    OpcUaClientTemplate opcUaClientTemplate(OpcUaClientFactory opcUaClientFactory, OpcUaProperties opcUaProperties)
            throws OpcUaClientException {
        return new OpcUaClientTemplate(opcUaClientFactory, opcUaProperties);
    }

    @Bean
    OpcUaSubscribeNodes opcUaSubscribeNodes(OpcUaProperties opcUaProperties) throws OpcUaClientException {
        return new OpcUaSubscribeNodes(opcUaProperties);
    }



}
