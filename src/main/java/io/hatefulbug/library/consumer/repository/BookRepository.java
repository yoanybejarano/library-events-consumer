package io.hatefulbug.library.consumer.repository;

import io.hatefulbug.library.consumer.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BookRepository extends JpaRepository<Book, UUID> {
    Book getBooksByBookId(UUID bookId);
}
