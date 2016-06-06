/*
 * (c) 2003-2014 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */

package com.mulesoft.agent.eventtracker.analytics;

import com.mulesoft.agent.domain.tracking.AgentTrackingNotification;

/**
 * <p>
 * Representation of the event types supported by the Analytics service.
 * </p>
 */
public enum AnalyticsEventType {

    ASYNC_START("AsyncMessageNotification", "async process scheduled"),
    ASYNC_END("AsyncMessageNotification", "async process complete"),

    COMPONENT_PRE_INVOKE("ComponentMessageNotification", "component pre invoke"),
    COMPONENT_POST_INVOKE("ComponentMessageNotification", "component post invoke"),

    MESSAGE_RECEIVE("EndpointMessageNotification", "receive"),
    MESSAGE_RESPONSE("EndpointMessageNotification", "response"),

    MESSAGE_DISPATCH_BEGIN("EndpointMessageNotification", "begin dispatch"),
    MESSAGE_DISPATCH_END("EndpointMessageNotification", "end dispatch"),

    MESSAGE_SEND_BEGIN("EndpointMessageNotification", "begin send"),
    MESSAGE_SEND_END("EndpointMessageNotification", "end send"),

    MESSAGE_REQUEST_BEGIN("EndpointMessageNotification", "begin request"),
    MESSAGE_REQUEST_END("EndpointMessageNotification", "end request"),

    EXCEPTION_PROCESS_START("ExceptionStrategyNotification", "exception strategy process start"),
    EXCEPTION_PROCESS_END("ExceptionStrategyNotification", "exception strategy process end"),

    MESSAGE_PROCESSOR_PRE_INVOKE("MessageProcessorNotification", "message processor pre invoke"),
    MESSAGE_PROCESSOR_POST_INVOKE("MessageProcessorNotification", "message processor post invoke"),

    PROCESS_START("PipelineMessageNotification", "pipeline process start"),
    PROCESS_COMPLETE("PipelineMessageNotification", "pipeline process complete"),
    PROCESS_END("PipelineMessageNotification", "pipeline request message processing end"),

    TRANSACTION_BEGIN("TransactionNotification", "begin"),
    TRANSACTION_COMMIT("TransactionNotification", "commit"),
    TRANSACTION_ROLLBACK("TransactionNotification", "rollback"),

    CUSTOM_EVENT_ACTION("EventNotification", "custom event"),

    EXCEPTION_ACTION("ExceptionNotification", "exception");

    /**
     * <p>
     * The type of notification represented by an event.
     * </p>
     */
    private String notificationType;

    /**
     * <p>
     * The action represented by an event.
     * </p>
     */
    private String action;

    /**
     * <p>
     * Creates an analytics event type from a notification type and an associated action.
     * </p>
     * @param notificationType The type of notification represented by an event.
     * @param action The action represented by an event.
     */
    AnalyticsEventType(String notificationType, String action) {
        this.notificationType = notificationType;
        this.action = action;
    }

    /**
     * <p>
     * Returns the event type corresponding to the provided {@link AgentTrackingNotification}.
     * </p>
     * @param notification The notfication to retrieve the type for.
     * @return The Analytics event type for the provided notification.
     */
    public static AnalyticsEventType getAnalyticsEventType(AgentTrackingNotification notification) {
        String notificationType = notification.getNotificationType();
        String action = notification.getAction();

        for (AnalyticsEventType type : AnalyticsEventType.values()) {
            if (type.notificationType.equals(notificationType) && type.action.equals(action)) {
                return type;
            }
        }
        return null;
    }
}
