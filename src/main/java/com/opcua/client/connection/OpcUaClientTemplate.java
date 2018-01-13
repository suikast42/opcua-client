package com.opcua.client.connection;

import com.opcua.client.config.OpcUaProperties;
import com.opcua.client.start.OpcUaClientCallback;
import com.opcua.client.start.OpcUaClientConnectionListener;
import com.prosysopc.ua.ServiceException;
import com.prosysopc.ua.StatusException;
import com.prosysopc.ua.client.MonitoredDataItem;
import com.prosysopc.ua.client.MonitoredDataItemListener;
import com.prosysopc.ua.client.Subscription;
import com.prosysopc.ua.client.UaClient;
import org.opcfoundation.ua.builtintypes.NodeId;
import org.opcfoundation.ua.core.Attributes;
import org.opcfoundation.ua.core.MonitoringMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.AlwaysRetryPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.annotation.Async;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by mj on 2018/1/11.
 *
 */
public class OpcUaClientTemplate {


    private static final Logger LOGGER = LoggerFactory.getLogger(OpcUaClientTemplate.class);

    private UaClient uaClient;

    private RetryTemplate retryTemplate;

    private long connBackOffPeriod;

    private List<OpcUaClientConnectionListener> connectionListeners = new ArrayList<>();

    private OpcUaProperties properties;

    public OpcUaClientTemplate(OpcUaClientFactory opcUaClientFactory, OpcUaProperties properties)
            throws OpcUaClientException {
        LOGGER.debug("OpcUaClientTemplate Load.");
        uaClient = opcUaClientFactory.createUaClient();

        connBackOffPeriod = properties.getRetry().getConnBackOffPeriod();

        // 链接后 断开的重连方针
        retryTemplate = new RetryTemplate();
        SimpleRetryPolicy simpleRetryPolicy = new SimpleRetryPolicy();
        //how many attempts
        simpleRetryPolicy.setMaxAttempts(properties.getRetry().getMaxAttempts());
        FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
        //how much time (in milliseconds) before next attempt
        fixedBackOffPolicy.setBackOffPeriod(properties.getRetry().getBackOffPeriod());
        retryTemplate.setBackOffPolicy(fixedBackOffPolicy);
        retryTemplate.setRetryPolicy(simpleRetryPolicy);

        this.properties = properties;
    }

    public void addConnectionListener(OpcUaClientConnectionListener connectionListener) {
        LOGGER.info("add opcua connection listener {}", connectionListener);
        this.connectionListeners.add(connectionListener);
    }

    public boolean subscribeNodeValue(NodeId id, MonitoredDataItemListener dataChangeListener)
            throws OpcUaClientException {
        return execute(() -> doSubscribeNodeValue(id, dataChangeListener));
    }

    private synchronized boolean doSubscribeNodeValue(NodeId id, MonitoredDataItemListener dataChangeListener)
            throws OpcUaClientException {
        try {
            Subscription subscription = new Subscription();
            subscription.setPublishingInterval(properties.getPublishingRate(), TimeUnit.MILLISECONDS);
            MonitoredDataItem item = new MonitoredDataItem(id, Attributes.Value,
                    MonitoringMode.Reporting, subscription.getPublishingInterval());
            item.setDataChangeListener(dataChangeListener);
            subscription.addItem(item);
            uaClient.addSubscription(subscription);
            return true;
        } catch (ServiceException | StatusException e) {
            throw new OpcUaClientException("Error subscribing node " + id + ", value: ", e);
        }
    }

    public <T> T execute(final OpcUaClientCallback<T> callback) throws OpcUaClientException {
        if (this.connectionListeners.size() == 0){
            throw new OpcUaClientException("Error executing, connectionListeners unload, uaClient not init already.");
        }
        try {
            return this.retryTemplate.execute(context -> {
                connect();
                return callback.performAction();
            });
        } catch (Exception e) {
            LOGGER.error("execute()", e);
            throw new OpcUaClientException("Error executing action: ", e);
        }
    }

    public synchronized boolean connect() throws OpcUaClientException {
        if (!uaClient.isConnected()) {
            try {
                LOGGER.info("Connecting ua server...");
                uaClient.connect();
                fireConnectionListeners();
                return true;
            } catch (Exception e) {
                throw new OpcUaClientException("Error connecting ua server: ", e);
            }
        } else {
            return true;
        }
    }

    private void fireConnectionListeners() throws OpcUaClientException {
        LOGGER.info("fireConnectionListeners, length {}", this.connectionListeners.size());
        this.connectionListeners.stream().forEach(OpcUaClientConnectionListener::onConnected);
    }

    // 第一次链接失败重连方针
    @Async
    public void connectAlwaysInBackend() {
        LOGGER.debug("OpcUaClientTemplate connectAlwaysInBackend taskExecutor run.");
        RetryTemplate alwaysRetryTemplate = new RetryTemplate();
        alwaysRetryTemplate.setRetryPolicy(new AlwaysRetryPolicy());
        FixedBackOffPolicy connFixedBackOffPolicy = new FixedBackOffPolicy();
        connFixedBackOffPolicy.setBackOffPeriod(connBackOffPeriod);
        alwaysRetryTemplate.setBackOffPolicy(connFixedBackOffPolicy);

        try {
            alwaysRetryTemplate.execute(context -> connect());
        } catch (OpcUaClientException e) {
            LOGGER.error("Error connecting ua server: ", e);
        }
    }




}
