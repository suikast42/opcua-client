package com.opcua.client.start;

import com.opcua.client.connection.OpcUaClientTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * Created by mj on 2018/1/11.
 *
 */
@Service
public class OpcUaService {

    @Autowired
    private OpcUaClientTemplate opcUaClientTemplate;

    @Autowired
    private UaConnectionListener uaConnectionListener;

    @PostConstruct
    public void opcuaClientConnect() {
        opcUaClientTemplate.addConnectionListener(uaConnectionListener);
        opcUaClientTemplate.connectAlwaysInBackend();
    }

}
