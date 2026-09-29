package com.company.andy.feature.jobinstance;

import com.company.andy.feature.detask.domain.DetaskSummary;
import com.company.andy.feature.jobdefinition.JobDefinitionDetail;

import java.util.Map;

public class JobInstance {
    private String id;
    private String jobDefinitionId;
    private JobDefinitionDetail jobDefinitionDetail;
    private JobInstanceStatus status;
    private Map<String, DetaskSummary> detasks;
}
