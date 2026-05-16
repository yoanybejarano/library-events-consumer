package io.hatefulbug.library.consumer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.hatefulbug.library.consumer.entity.Book;
import io.hatefulbug.library.consumer.entity.LibraryEvent;
import io.hatefulbug.library.consumer.entity.LibraryEventType;
import io.hatefulbug.library.consumer.repository.BookRepository;
import io.hatefulbug.library.consumer.repository.LibraryEventRepository;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LibraryEventServiceTest {

    @Mock
    private LibraryEventRepository libraryEventRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private LibraryEventService libraryEventService;

    private ConsumerRecord<String, String> consumerRecord;
    private LibraryEvent libraryEvent;
    private Book book;

    @BeforeEach
    void setUp() {

        String json = """
                {
                    "libraryEventId":"2b2aff88-2458-413d-9a15-4f0f0c29802c",
                    "libraryEventType":"NEW",
                    "book":{
                        "bookId":"49360e13-4e00-4bbf-8638-a5043cd96861",
                        "title":"The shadow over Innsmouth",
                        "author":"H.P. Lovecraft"
                    }
                }
                """;

        consumerRecord =
                new ConsumerRecord<>("library_events", 0, 0, "key", json);

        book = Book.builder()
                .bookId(UUID.fromString("49360e13-4e00-4bbf-8638-a5043cd96861"))
                .title("The shadow over Innsmouth")
                .author("H.P. Lovecraft")
                .build();

        libraryEvent = LibraryEvent.builder()
                .libraryEventId(
                        UUID.fromString("2b2aff88-2458-413d-9a15-4f0f0c29802c")
                )
                .libraryEventType(LibraryEventType.NEW)
                .book(book)
                .build();
    }

    @Test
    void processLibraryEvent_new() throws JsonProcessingException {

        // given
        when(objectMapper.readValue(
                consumerRecord.value(),
                LibraryEvent.class
        )).thenReturn(libraryEvent);

        when(bookRepository.getBooksByBookId(book.getBookId()))
                .thenReturn(null);

        when(bookRepository.save(book))
                .thenReturn(book);

        // when
        libraryEventService.processLibraryEvent(consumerRecord);

        // then
        verify(objectMapper, times(1))
                .readValue(consumerRecord.value(), LibraryEvent.class);

        verify(bookRepository, times(1))
                .getBooksByBookId(book.getBookId());

        verify(bookRepository, times(1))
                .save(book);

        verify(libraryEventRepository, times(1))
                .save(libraryEvent);
    }

    @Test
    void processLibraryEvent_update() throws JsonProcessingException {

        // given
        libraryEvent.setLibraryEventType(LibraryEventType.UPDATE);

        when(objectMapper.readValue(
                consumerRecord.value(),
                LibraryEvent.class
        )).thenReturn(libraryEvent);

        when(libraryEventRepository.findById(
                libraryEvent.getLibraryEventId()
        )).thenReturn(Optional.of(libraryEvent));

        when(bookRepository.getBooksByBookId(book.getBookId()))
                .thenReturn(book);

        // when
        libraryEventService.processLibraryEvent(consumerRecord);

        // then
        verify(libraryEventRepository, times(1))
                .findById(libraryEvent.getLibraryEventId());

        verify(bookRepository, times(1))
                .getBooksByBookId(book.getBookId());

        verify(libraryEventRepository, times(1))
                .save(libraryEvent);
    }

    @Test
    void processLibraryEvent_update_libraryEventId_null()
            throws JsonProcessingException {

        // given
        libraryEvent.setLibraryEventType(LibraryEventType.UPDATE);
        libraryEvent.setLibraryEventId(null);

        when(objectMapper.readValue(
                consumerRecord.value(),
                LibraryEvent.class
        )).thenReturn(libraryEvent);

        // when & then
        assertThrows(
                IllegalArgumentException.class,
                () -> libraryEventService.processLibraryEvent(consumerRecord)
        );

        verify(libraryEventRepository, never())
                .save(any());
    }

    @Test
    void processLibraryEvent_update_libraryEvent_not_found()
            throws JsonProcessingException {

        // given
        libraryEvent.setLibraryEventType(LibraryEventType.UPDATE);

        when(objectMapper.readValue(
                consumerRecord.value(),
                LibraryEvent.class
        )).thenReturn(libraryEvent);

        when(libraryEventRepository.findById(
                libraryEvent.getLibraryEventId()
        )).thenReturn(Optional.empty());

        // when & then
        assertThrows(
                IllegalArgumentException.class,
                () -> libraryEventService.processLibraryEvent(consumerRecord)
        );

        verify(libraryEventRepository, never())
                .save(any());
    }

    @Test
    void processLibraryEvent_unsupported_event_type()
            throws JsonProcessingException {

        // given
        libraryEvent.setLibraryEventType(null);

        when(objectMapper.readValue(
                consumerRecord.value(),
                LibraryEvent.class
        )).thenReturn(libraryEvent);

        // when & then
        assertThrows(
                IllegalArgumentException.class,
                () -> libraryEventService.processLibraryEvent(consumerRecord)
        );
    }
}
