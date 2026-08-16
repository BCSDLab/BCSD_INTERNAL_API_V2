package com.bcsdlab.bcsdinternalapiv2.auth.controller.dto.response;

public record ResetTokenValidationResponse(boolean valid, String studentNumberMasked) {
}
