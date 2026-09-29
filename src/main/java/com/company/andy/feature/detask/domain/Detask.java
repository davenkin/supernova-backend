package com.company.andy.feature.detask.domain;

import com.company.andy.feature.detask.domain.action.Action;

import java.util.List;

public class Detask {
    private String id;
    private String deviceId;
    private boolean requireRestart;
    private DetaskStatus status;
    private DetaskDetail detail;
    private List<Action> actions;
}
