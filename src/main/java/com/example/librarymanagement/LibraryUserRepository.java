package com.example.librarymanagement;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LibraryUserRepository extends JpaRepository<LibraryUser, Long> {

	boolean existsByUsername(String username);

	Optional<LibraryUser> findByUsername(String username);
}
