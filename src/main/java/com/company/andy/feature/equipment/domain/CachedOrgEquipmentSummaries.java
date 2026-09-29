package com.company.andy.feature.equipment.domain;

import java.util.List;

// Some json deserialization configurations require a wrapper outside List
public record CachedOrgEquipmentSummaries(List<EquipmentSummary> summaries) {
}
