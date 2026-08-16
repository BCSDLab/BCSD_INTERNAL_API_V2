package com.bcsdlab.bcsdinternalapiv2.member.service;

import java.time.Instant;

public record AccountCreatedEvent(Long memberId, String email, String studentNumber, String temporaryPassword,
                                   String loginUrl, Instant passwordChangedAt) {
}
