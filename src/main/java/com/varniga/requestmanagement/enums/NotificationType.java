package com.varniga.requestmanagement.enums;

public enum NotificationType {

    // EXISTING TYPES
    TICKET_CREATED,
    TICKET_ASSIGNED,
    STATUS_UPDATED,
    APPROVED,
    REJECTED,
    COMMENT_ADDED,

    // SLA RELATED (NEW)
    SLA_ALERT,        // generic (keep this)
    SLA_WARNING,      // 50%
    SLA_ESCALATION,   // 75%
    SLA_BREACH        // 100%
}