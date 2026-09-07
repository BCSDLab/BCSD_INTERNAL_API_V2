package com.bcsdlab.bcsdinternalapiv2.reservation.controller.dto.request;

import com.bcsdlab.bcsdinternalapiv2.reservation.model.RepeatFrequency;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

public record ReservationCreateRequest(
        @NotNull
        LocalDate date,

        @NotNull
        @Min(0)
        @Max(1440)
        Short start,

        @NotNull
        @Min(0)
        @Max(1440)
        Short end,

        @NotBlank
        @Size(max = 200)
        String purpose,

        @NotNull
        @Min(1)
        Short headcount,

        @Valid
        RepeatOption repeat
) {

    public record RepeatOption(
            @NotNull
            RepeatFrequency frequency,

            @NotEmpty
            List<DayOfWeek> weekdays,

            @NotNull
            LocalDate endDate
    ) {
    }

    public boolean isRepeat() {
        return repeat != null;
    }
}
