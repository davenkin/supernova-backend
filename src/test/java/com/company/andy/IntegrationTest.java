package com.company.andy;

import com.company.andy.common.event.DomainEvent;
import com.company.andy.common.event.DomainEventType;
import com.company.andy.common.event.consume.EventConsumer;
import com.company.andy.common.event.publish.PublishingDomainEvent;
import com.company.andy.common.model.actor.Actor;
import com.company.andy.support.testid.TestIdExtension;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.client.RestTestClient;
import tools.jackson.databind.ObjectMapper;

import java.util.function.Consumer;

import static com.company.andy.common.utils.CommonUtils.mongoConcatFields;
import static com.company.andy.common.utils.CommonUtils.requireNonBlank;
import static java.util.Base64.getEncoder;
import static java.util.Objects.requireNonNull;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.data.domain.Sort.by;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Slf4j
@ActiveProfiles("it-embedded")
//@ActiveProfiles("it-local")
@AutoConfigureRestTestClient
@ExtendWith(TestIdExtension.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
public abstract class IntegrationTest {

    @Autowired
    protected MongoTemplate mongoTemplate;

    @Autowired
    protected StringRedisTemplate stringRedisTemplate;

    @Autowired
    protected EventConsumer eventConsumer;

    @Autowired
    protected Environment environment;

    @Autowired
    protected RestTestClient restTestClient;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected CacheManager cacheManager;

    protected <T extends DomainEvent> T latestDomainEventFor(String arId, DomainEventType type, Class<T> eventClass) {
        requireNonBlank(arId, "arId must not be blank.");
        requireNonNull(type, "type must not be null.");
        requireNonNull(eventClass, "eventClass must not be null.");

        Query query = query(where(mongoConcatFields(PublishingDomainEvent.Fields.event, DomainEvent.Fields.arId)).is(arId)
                .and(mongoConcatFields(PublishingDomainEvent.Fields.event, DomainEvent.Fields.type)).is(type))
                .with(by(DESC, PublishingDomainEvent.Fields.raisedAt));
        PublishingDomainEvent domainEvent = mongoTemplate.findOne(query, PublishingDomainEvent.class);
        return domainEvent == null ? null : eventClass.cast(domainEvent.getEvent());
    }

    // put the actor json as fake JWT bearer header to ensure end-to-end HTTP integration testing
    protected Consumer<HttpHeaders> authHeaderOf(Actor actor) {
        return headers -> headers.setBearerAuth(getEncoder().encodeToString(objectMapper.writeValueAsString(actor).getBytes()));
    }
}
