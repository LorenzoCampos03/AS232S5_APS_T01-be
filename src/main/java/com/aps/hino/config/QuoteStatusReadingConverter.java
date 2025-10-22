package com.aps.hino.config;

import com.aps.hino.model.QuoteStatus;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.stereotype.Component;

@ReadingConverter
@Component
public class QuoteStatusReadingConverter implements Converter<String, QuoteStatus> {

    @Override
    public QuoteStatus convert(String source) {
        if (source == null) return null;
        for (QuoteStatus status : QuoteStatus.values()) {
            if (status.toString().equalsIgnoreCase(source)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Valor inválido para QuoteStatus: " + source);
    }
}
