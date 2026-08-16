package com.bcsdlab.bcsdinternalapiv2.auth.service;

public record PasswordResetRequestedEvent(Long tokenId, String email, String resetUrl) {
}
