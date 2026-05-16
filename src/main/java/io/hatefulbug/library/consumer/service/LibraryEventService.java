package io.hatefulbug.library.consumer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.hatefulbug.library.consumer.entity.Book;
import io.hatefulbug.library.consumer.entity.LibraryEvent;
import io.hatefulbug.library.consumer.repository.BookRepository;
import io.hatefulbug.library.consumer.repository.LibraryEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class LibraryEventService {

    private final LibraryEventRepository libraryEventRepository;
    private final BookRepository bookRepository;
    private final ObjectMapper objectMapper;

    public LibraryEventService(LibraryEventRepository libraryEventRepository,  BookRepository bookRepository, ObjectMapper objectMapper) {
        this.libraryEventRepository = libraryEventRepository;
        this.bookRepository = bookRepository;
        this.objectMapper = objectMapper;
    }

    public void processLibraryEvent(ConsumerRecord<String, String> record)
            throws JsonProcessingException {

        LibraryEvent libraryEvent = objectMapper.readValue(record.value(), LibraryEvent.class);
        if (libraryEvent.getLibraryEventType() == null) {
            throw new IllegalArgumentException("LibraryEventType cannot be null");
        }
        switch (libraryEvent.getLibraryEventType()) {
            case NEW -> save(libraryEvent);
            case UPDATE -> {
                validation(libraryEvent);
                save(libraryEvent);
            }
            default -> throw new IllegalArgumentException(
                    "Unsupported LibraryEventType: "
                            + libraryEvent.getLibraryEventType()
            );
        }
    }

    private void save(LibraryEvent libraryEvent) {
        Book book = bookRepository.getBooksByBookId(libraryEvent.getBook().getBookId());
        if (book == null) {
            book = bookRepository.save(libraryEvent.getBook());
            libraryEvent.setBook(book);
        }
        book.setLibraryEvent(libraryEvent);
        libraryEventRepository.save(libraryEvent);
    }

    private void validation(LibraryEvent libraryEvent) {
        if (libraryEvent.getLibraryEventId() == null) {
            throw new IllegalArgumentException("Library  Event Id cannot be null");
        }
        Optional<LibraryEvent> optional = libraryEventRepository.findById(libraryEvent.getLibraryEventId());
        if (!optional.isPresent()) {
            throw new IllegalArgumentException("Library  Event with id " + libraryEvent.getLibraryEventId() + " not found");
        }
        log.info("Validation is successful for library event with id {}", libraryEvent.getLibraryEventId());

    }

}
