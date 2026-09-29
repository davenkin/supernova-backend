package com.company.andy.common.event;

import com.company.andy.common.configuration.profile.DisableForIT;
import com.company.andy.common.event.publish.DomainEventPublisher;
import com.company.andy.common.event.publish.PublishingDomainEvent;
import com.mongodb.client.model.changestream.ChangeStreamDocument;
import com.mongodb.client.model.changestream.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.TopicPartition;
import org.bson.Document;
import org.jspecify.annotations.NullMarked;
import org.springframework.boot.kafka.autoconfigure.DefaultKafkaConsumerFactoryCustomizer;
import org.springframework.boot.kafka.autoconfigure.DefaultKafkaProducerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.messaging.ChangeStreamRequest;
import org.springframework.data.mongodb.core.messaging.DefaultMessageListenerContainer;
import org.springframework.data.mongodb.core.messaging.MessageListener;
import org.springframework.data.mongodb.core.messaging.MessageListenerContainer;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;
import org.springframework.util.backoff.ExponentialBackOff;
import tools.jackson.databind.json.JsonMapper;

import static com.company.andy.common.utils.Constants.PUBLISHING_EVENT_COLLECTION;

@Slf4j
@NullMarked
@DisableForIT
@Configuration(proxyBeanMethods = false)
public class EventConfiguration {
    private static final String dltSuffix = "-dlt";

    // Automatically triggered on DomainEvent insertion in MongoDB,
    // then publish staged DomainEvents to messaging middleware
    @Bean(destroyMethod = "stop")
    MessageListenerContainer mongoDomainEventChangeStreamListenerContainer(
            MongoTemplate mongoTemplate,
            TaskExecutor taskExecutor,
            DomainEventPublisher domainEventPublisher) {
        MessageListenerContainer container = new DefaultMessageListenerContainer(mongoTemplate, taskExecutor);

        // Get notified on DomainEvent insertion in MongoDB, then publish staged DomainEvents to messaging middleware
        container.register(ChangeStreamRequest.builder(
                        (MessageListener<ChangeStreamDocument<Document>, PublishingDomainEvent>) message -> {
                            domainEventPublisher.publishStagedDomainEvents(100);
                        })
                .collection(PUBLISHING_EVENT_COLLECTION)
                .filter(new Document("$match", new Document("operationType", OperationType.INSERT.getValue())))
                .build(), PublishingDomainEvent.class);
        container.start();
        return container;
    }

    @Bean
    public DefaultKafkaProducerFactoryCustomizer defaultKafkaProducerFactoryCustomizer(JsonMapper jsonMapper) {
        return producerFactory -> producerFactory.setValueSerializer(new JacksonJsonSerializer<>(jsonMapper));
    }

    @Bean
    public DefaultKafkaConsumerFactoryCustomizer defaultKafkaConsumerFactoryCustomizer(JsonMapper jsonMapper) {
        return consumerFactory -> {
            JacksonJsonDeserializer valueDeserializer = new JacksonJsonDeserializer<>(jsonMapper);
            valueDeserializer.addTrustedPackages("*");

            // Must wrap the JsonDeserializer into an ErrorHandlingDeserializer, otherwise deserialization error will result in endless message retry
            consumerFactory.setValueDeserializer(new ErrorHandlingDeserializer<>(valueDeserializer));
        };
    }

    @Bean
    public DefaultErrorHandler defaultErrorHandler(KafkaTemplate<String, Object> kafkaTemplate) {
        ExponentialBackOff backOff = new ExponentialBackOff(500L, 2);
        backOff.setMaxAttempts(2); // the message will be processed at most [2 + 1 = 3] times
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, ex) -> {
                    String dlt = record.topic() + dltSuffix;
                    log.error("Error consuming message[key={}], moving to dead letter topic[{}].", record.key(), dlt, ex);
                    return new TopicPartition(dlt, record.partition());
                }
        );
        return new DefaultErrorHandler(recoverer, backOff);
    }
}


