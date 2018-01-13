package com.opcua.client.start;

import com.opcua.client.util.OpcUaUtil;
import com.prosysopc.ua.client.MonitoredDataItem;
import com.prosysopc.ua.client.MonitoredDataItemListener;
import org.opcfoundation.ua.builtintypes.DataValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Created by mj on 2018/1/11.
 *
 */
@Component
public class AutomationLineRobotStatusListener  implements MonitoredDataItemListener {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void onDataChange(MonitoredDataItem monitoredDataItem, DataValue dataValue, DataValue dataValueNew) {
        logger.info("monitoredDataItem: {}", monitoredDataItem.getNodeId().getValue());
        if (!OpcUaUtil.isNewNodeValueValid(monitoredDataItem.getNodeId(), dataValue, dataValueNew)) {
            return;
        }

//        short preFlags = dataValue.getValue().shortValue();
//        short newFlags = dataValueNew.getValue().shortValue();
//
//        logger.info("monitoredDataItem: {} , {}", preFlags, newFlags);

    }


}
