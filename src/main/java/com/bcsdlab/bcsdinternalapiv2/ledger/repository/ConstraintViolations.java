package com.bcsdlab.bcsdinternalapiv2.ledger.repository;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;

/**
 * 전역 핸들러는 DataIntegrityViolationException을 400으로 응답한다. 409로 바꿔야 하는 곳은 위반한 제약
 * 이름을 확인해 그 제약일 때만 바꾸고, 나머지는 그대로 던진다.
 */
public final class ConstraintViolations {

    private ConstraintViolations() {
    }

    public static boolean isViolationOf(DataIntegrityViolationException e, String constraintName) {
        for (Throwable cause = e; cause != null; cause = cause.getCause()) {
            if (cause instanceof ConstraintViolationException violation
                    && constraintName.equalsIgnoreCase(violation.getConstraintName())) {
                return true;
            }
            if (cause.getMessage() != null && cause.getMessage().contains(constraintName)) {
                return true;
            }
        }
        return false;
    }
}
