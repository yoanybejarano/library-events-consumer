package io.hatefulbug.library.consumer.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LibraryEventConsumer {

    @KafkaListener(topics = {"library_events"})
    public void consume(String payload) {
        log.info(payload);
    }

}
