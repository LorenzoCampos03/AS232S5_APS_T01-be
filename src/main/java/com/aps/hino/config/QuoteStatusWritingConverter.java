package com.aps.hino.config;

import com.aps.hino.model.QuoteStatus;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.stereotype.Component;

@WritingConverter
@Component
public class QuoteStatusWritingConverter implements Converter<QuoteStatus, String> {

    @Override
    public String convert(QuoteStatus source) {
        return source.toString();
    }
}
