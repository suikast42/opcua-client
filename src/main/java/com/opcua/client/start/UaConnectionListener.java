package com.opcua.client.start;

import com.opcua.client.connection.OpcUaClientException;
import com.opcua.client.connection.OpcUaClientTemplate;
import com.opcua.client.connection.OpcUaSubscribeNodes;
import com.prosysopc.ua.client.MonitoredDataItemListener;
import org.opcfoundation.ua.builtintypes.NodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by mj on 2018/1/11.
 *
 */
@Component
public class UaConnectionListener implements OpcUaClientConnectionListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(UaConnectionListener.class);

    private static final Map<String, MonitoredDataItemListener> LISTENER_MAP = new ConcurrentHashMap<>();

    @Autowired
    private OpcUaSubscribeNodes opcUaSubscribeNodes;

    @Autowired
    private OpcUaClientTemplate opcUaClientTemplate;

    @Autowired
    private AutomationLineRobotStatusListener automationLineRobotStatusListener;

    @Override
    public void onConnected() {
        LOGGER.info("---------->>>>> opcua client connect success <<<<<-----------");

        subscribeNodesValue(opcUaSubscribeNodes.getAutomationLineRobotStatusSubscribeNodes(), automationLineRobotStatusListener);

    }

    private synchronized void subscribeNodesValue(List<String> StrList, MonitoredDataItemListener listener) {
        try {
            for (String nodeIdStr : StrList) {
                if (LISTENER_MAP.containsKey(nodeIdStr + ":" + listener.getClass().toString())) {
                    return;
                }
                LOGGER.debug("add listener:{}", nodeIdStr + ":" + listener.getClass().toString());
                opcUaClientTemplate.subscribeNodeValue(new NodeId(2, nodeIdStr), listener);
                LISTENER_MAP.put(nodeIdStr + ":" + listener.getClass().toString(), listener);
            }
        } catch (OpcUaClientException e) {
            LOGGER.error("OpcUa Client Exception when subscribeNodesValue", e);
        }
    }


}
