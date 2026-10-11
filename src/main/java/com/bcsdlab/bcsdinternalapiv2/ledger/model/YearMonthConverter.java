package com.bcsdlab.bcsdinternalapiv2.ledger.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.time.LocalDate;
import java.time.YearMonth;

/** 월은 DB에 매월 1일 DATE로 저장한다(CHECK로 1일만 허용). */
@Converter
public class YearMonthConverter implements AttributeConverter<YearMonth, LocalDate> {

    @Override
    public LocalDate convertToDatabaseColumn(YearMonth month) {
        return month == null ? null : month.atDay(1);
    }

    @Override
    public YearMonth convertToEntityAttribute(LocalDate date) {
        return date == null ? null : YearMonth.from(date);
    }
}
