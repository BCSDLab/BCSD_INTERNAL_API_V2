package com.bcsdlab.bcsdinternalapiv2.reservation.controller.dto.response;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

public record MonthlyOccupancyResponse(
        YearMonth month,
        List<Day> days
) {

    public record Day(LocalDate date, int reservedMinutes) {
    }

    public static MonthlyOccupancyResponse of(YearMonth month, List<Day> days) {
        return new MonthlyOccupancyResponse(month, days);
    }
}
