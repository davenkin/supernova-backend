package com.company.andy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

import java.time.ZoneId;

import static com.company.andy.common.utils.Constants.CHINA_TIME_ZONE;
import static java.util.TimeZone.getTimeZone;
import static java.util.TimeZone.setDefault;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SpringBootWebApplication {

    static void main(String[] args) {
        setDefault(getTimeZone(ZoneId.of(CHINA_TIME_ZONE)));
        SpringApplication.run(SpringBootWebApplication.class, args);
    }
}
