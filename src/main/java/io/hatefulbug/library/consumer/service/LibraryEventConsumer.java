package io.hatefulbug.library.consumer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;

@Component
@Slf4j
public class LibraryEventConsumer {

    private final LibraryEventService libraryEventService;
    public CountDownLatch latch = new CountDownLatch(1);

    public LibraryEventConsumer(LibraryEventService libraryEventService) {
        this.libraryEventService = libraryEventService;
    }

    @KafkaListener(topics = {"library_events"})
    public void consume(ConsumerRecord<String, String> record) {
        try {
            libraryEventService.processLibraryEvent(record);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }
        latch.countDown();
        log.info("Record processed: {}", record.value());
    }

}
