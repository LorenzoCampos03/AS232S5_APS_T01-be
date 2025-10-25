package com.aps.hino.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.convert.CustomConversions;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import java.util.List;

@Configuration
public class DatabaseConfig {

    @Bean
    public R2dbcCustomConversions r2dbcCustomConversions(
            QuoteStatusReadingConverter quoteStatusReadingConverter,
            QuoteStatusWritingConverter quoteStatusWritingConverter
    ) {
        return new R2dbcCustomConversions(
                CustomConversions.StoreConversions.NONE,
                List.of(quoteStatusReadingConverter, quoteStatusWritingConverter)
        );
    }
}
