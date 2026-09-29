package com.company.andy.common.utils;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@Slf4j
@Hidden
@RestController
@RequestMapping
@RequiredArgsConstructor
public class AboutController {
    private static final Instant DEPLOYED_TIME = Instant.now();

    @GetMapping(value = "/about")
    public AboutInfo about() {
        return new AboutInfo("Running! Started at " + DEPLOYED_TIME, Instant.now().toString());
    }

    @GetMapping("/favicon.ico")
    public void dummyFavicon() {
        // nop
    }

    public record AboutInfo(String status, String fetchTime) {
    }
}
