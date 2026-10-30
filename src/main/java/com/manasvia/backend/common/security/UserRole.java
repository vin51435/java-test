package com.manasvia.backend.common.security;

/**
 * Access and authorization roles for accounts in the Manasvia ecosystem.
 */
public enum UserRole {
    /**
     * Anonymous guest user identified by device fingerprint.
     * Has access to baseline features, daily lens widget, and introductory onboarding.
     */
    GUEST,

    /**
     * Authenticated and verified account (e.g. Google OAuth2).
     * Has cloud-synced soul profile, cross-device history, and full reflection access.
     */
    USER,

    /**
     * Premium seeker account with an active Google Play subscription entitlement.
     * Has unlimited AI reflections, real-time wisdom deep dives, and priority latency.
     */
    PREMIUM
}
