package com.lazydash.audio.visualiser.system.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class NotificationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationService.class);

    private List<EventHandler> eventHandlers = new LinkedList<>();
    private static NotificationService notificationService = new NotificationService();

    private NotificationService() {
    }

    public static NotificationService getInstance() {
        return notificationService;
    }

    public synchronized void emit(String eventId, String message) {
        for (EventHandler eventHandler : eventHandlers) {
            if (eventHandler.getEventId().equals(eventId)) {
                eventHandler.getEventCallback().run(message);
            }
        }
    }

    public synchronized void register(String id, String eventName, EventCallback eventCallback) {
        Optional<EventHandler> first = eventHandlers.stream()
                .filter(eventHandler ->
                        (eventHandler.getHandlerId() + eventHandler.getEventId()).equals(id + eventName))
                .findFirst();

        if (first.isPresent()) {
            first.get().setEventCallback(eventCallback);

        } else {
            eventHandlers.add(new EventHandler(id, eventName, eventCallback));
        }

        LOGGER.debug("eventHandlers size: " + eventHandlers.size());

    }

}
