package io.hatefulbug.library.consumer.repository;

import io.hatefulbug.library.consumer.entity.LibraryEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LibraryEventRepository extends JpaRepository<LibraryEvent, UUID> {
}
