package org.engicodes.userservice.model;

import lombok.Getter;

@Getter
public enum SubscriptionStatus {
    FREE("FREE_USER"),
    PREMIUM_MONTHLY("PREMIUM_USER_FOR_1_MONTH"),
    PREMIUM_YEARLY("PREMIUM_USER_FOR_1_YEAR"),
    EXPIRED("SUBSCRIPTION_EXPIRED"),
    CANCELLED("SUBSCRIPTION_CANCELLED");

    private final String statusInfo;

    SubscriptionStatus(String statusInfo) {
        this.statusInfo = statusInfo;
    }
}