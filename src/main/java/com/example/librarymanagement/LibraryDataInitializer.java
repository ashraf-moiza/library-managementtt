package com.example.librarymanagement;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LibraryDataInitializer {

	@Bean
	CommandLineRunner seedBooks(BookRepository bookRepository) {
		return args -> {
			if (bookRepository.count() == 0) {
				bookRepository.saveAll(List.of(
						new Book("The Great Gatsby", "F. Scott Fitzgerald"),
						new Book("Clean Code", "Robert C. Martin"),
						new Book("1984", "George Orwell"),
						new Book("To Kill a Mockingbird", "Harper Lee"),
						new Book("The Hobbit", "J. R. R. Tolkien")));
			}
		};
	}
}
