package org.apache.usergrid.notifications.apns;

import org.apache.usergrid.persistence.entities.Notification;
import org.apache.usergrid.persistence.entities.Notifier;
import org.apache.usergrid.services.notifications.apns.APNsNotification;
import org.apache.usergrid.services.notifications.apns.APNsAdapter;
import org.apache.usergrid.services.notifications.ConnectionException;
import org.apache.usergrid.services.notifications.NotificationsService;
import org.apache.usergrid.services.notifications.ProviderAdapter;
import org.apache.usergrid.services.notifications.TaskTracker;

import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.usergrid.persistence.EntityManager;
import org.apache.usergrid.services.ServicePayload;

public class MockSuccessfulProviderAdapter implements ProviderAdapter {

    private static ProviderAdapter realProviderAdapter;

    public static void install(NotificationsService ns) {
        install(ns, false);
    }

    public static void install(NotificationsService ns, boolean doAsync) {
        if (realProviderAdapter != null)
            realProviderAdapter = ns.providerAdapters.get("apple");
        ns.providerAdapters.put("apple", new MockSuccessfulProviderAdapter(
                doAsync));
    }

    public static void uninstall(NotificationsService ns) {
        if (realProviderAdapter != null) {
            ns.providerAdapters.put("apple", realProviderAdapter);
        }
    }

    private ExecutorService pool;

    public MockSuccessfulProviderAdapter() {
    }

    public MockSuccessfulProviderAdapter(boolean async) {
        if (async) {
            pool = Executors
                    .newFixedThreadPool(APNsAdapter.MAX_CONNECTION_POOL_SIZE);
        }
    }

    @Override
    public void testConnection(Notifier notifier) throws ConnectionException {
    }

    @Override
    public String translatePayload(Object payload) throws Exception {
        return payload.toString();
    }

    @Override
    public Map<String, Date> getInactiveDevices(Notifier notifier,
            EntityManager em) throws Exception {
        return null;
    }

    @Override
    public void validateCreateNotifier(ServicePayload payload) throws Exception {
    }

    @Override
    public void doneSendingNotifications() throws Exception {
    }

    @Override
    public void sendNotification(final String providerId,
            final Notifier notifier, final Object payload,
            final Notification notification, final TaskTracker tracker)
            throws Exception {

        final APNsNotification apnsNotification = APNsNotification.create(
                providerId, payload.toString(), notification, tracker);

        if (pool == null) {
            apnsNotification.messageSent();
        } else {
            pool.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(new Random().nextInt(100));
                        apnsNotification.messageSent();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}
