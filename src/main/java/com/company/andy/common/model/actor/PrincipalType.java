package com.company.andy.common.model.actor;

public enum PrincipalType {
    MEMBER,// Members under an org
    SUPERVISOR,// Platform users which does belong to any org
    ORG_SERVICE_CLIENT, // Service client only representing an org and can access resources only under that org
    PLATFORM_SERVICE_CLIENT, // Service client representing the platform and can access platform level resources
    ROBOT,
    WEBHOOK,
    ANONYMOUS
}
