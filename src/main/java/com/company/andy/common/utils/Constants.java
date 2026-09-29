package com.company.andy.common.utils;

public class Constants {
    public static final String THIS_SERVICE_NAME = "andy-springboot4-template";
    public static final String MONGO_ID = "_id";
    public static final String ID = "id";
    public static final String TRACE_PARENT = "traceparent";
    public final static String CHINA_TIME_ZONE = "Asia/Shanghai";
    public static final String ANONYMOUS_ROLE = "ANONYMOUS";
    public static final String ROLE_PREFIX = "ROLE_";
    public static final String ORG_ID_HEADER = "x-org-id";
    public static final String KEYCLOAK_SERVICE_ACCOUNT = "keycloak-service-account";
    public static final String JWT_RELAY_REST_CLIENT = "jwtRelayRestClient";
    public static final String SERVICE_CLIENT_REST_CLIENT = "serviceClientRestClient";
    public static final String APPLICATION_TASK_EXECUTOR = "applicationTaskExecutor";
    public static final String THREAD_POOL_TASK_EXECUTOR = "threadPoolTaskExecutor";

    // Event
    public static final String KAFKA_DOMAIN_EVENT_TOPIC = "domain-event-topic";
    public static final String PUBLISHING_EVENT_COLLECTION = "publishing-event";
    public static final String CONSUMING_EVENT_COLLECTION = "consuming-event";

    // Cache
    public static final String CACHE_PREFIX = "Cache:";
    public static final String ORG_EQUIPMENTS_CACHE = "ORG_EQUIPMENTS";
    public static final String SYSTEM_SETTINGS_CACHE = "SYSTEM_SETTINGS";

    // JWT
    public static final String JWT_CLAIM_PREFERRED_USERNAME = "preferred_username";
    public static final String JWT_CLAIM_REALM_ACCESS = "realm_access";
    public static final String JWT_CLAIM_REALM_ACCESS_ROLES = "roles";
    public static final String JWT_CLAIM_AZP = "azp";
    public static final String JWT_CLAIM_SUPERVISOR_ID = "supervisor_id";
    public static final String JWT_CLAIM_PRINCIPAL_TYPE = "principal_type";
    public static final String JWT_CLAIM_ORG_ID = "org_id";
    public static final String JWT_CLAIM_MEMBER_ID = "member_id";

    // Regex
    public static final String MOBILE_NUMBER_REGEX = "^[1]([3-9])[0-9]{9}$";
    public static final String EMAIL_REGEX = "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$";
}
