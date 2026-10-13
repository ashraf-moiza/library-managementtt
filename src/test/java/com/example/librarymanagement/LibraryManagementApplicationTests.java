package com.example.librarymanagement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class LibraryManagementApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private BookRepository bookRepository;

	@Autowired
	private BorrowRecordRepository borrowRecordRepository;

	@Autowired
	private LibraryUserRepository userRepository;

	@Test
	void registersAuthenticatesAndRestrictsLibraryActionsToSignedInReader() throws Exception {
		borrowRecordRepository.deleteAll();
		userRepository.deleteAll();
		bookRepository.deleteAll();

		mockMvc.perform(get("/"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/login"));

		mockMvc.perform(post("/register")
				.with(csrf())
				.param("username", "Taylor")
				.param("password", "password123")
				.param("confirmPassword", "password123"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/login?registered"));

		LibraryUser registeredUser = userRepository.findByUsername("taylor").orElseThrow();
		assertThat(registeredUser.getPasswordHash()).isNotEqualTo("password123");

		mockMvc.perform(post("/register")
				.with(csrf())
				.param("username", "TAYLOR")
				.param("password", "password123")
				.param("confirmPassword", "password123"))
				.andExpect(status().isOk())
				.andExpect(result -> assertThat(result.getResponse().getContentAsString())
						.contains("already in use"));

		Book overdueBook = bookRepository.save(new Book("Overdue title", "A. Writer"));
		overdueBook.setAvailable(false);
		bookRepository.save(overdueBook);
		borrowRecordRepository.save(new BorrowRecord(
				overdueBook, "taylor", LocalDate.now().minusDays(20), LocalDate.now().minusDays(3)));
		Book otherReaderBook = bookRepository.save(new Book("Other reader title", "C. Writer"));
		otherReaderBook.setAvailable(false);
		bookRepository.save(otherReaderBook);
		BorrowRecord otherReaderRecord = borrowRecordRepository.save(new BorrowRecord(
				otherReaderBook, "jordan", LocalDate.now(), LocalDate.now().plusDays(7)));
		Book availableBook = bookRepository.save(new Book("Available title", "B. Writer"));

		MvcResult loginResult = mockMvc.perform(post("/login")
				.with(csrf())
				.param("username", "Taylor")
				.param("password", "password123"))
				.andExpect(status().is3xxRedirection())
				.andReturn();
		MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);
		assertThat(session).isNotNull();

		mockMvc.perform(get("/").session(session))
				.andExpect(status().isOk())
				.andExpect(result -> assertThat(result.getResponse().getContentAsString())
						.contains("Available title", "Overdue title", "late-fee", "$<span>6.00</span>")
						.doesNotContain("Other reader title"));

		mockMvc.perform(post("/return")
				.session(session)
				.with(csrf())
				.param("recordId", otherReaderRecord.getId().toString()))
				.andExpect(status().isNotFound());

		mockMvc.perform(post("/issue")
				.session(session)
				.with(csrf())
				.param("bookId", availableBook.getId().toString())
				.param("days", "7"))
				.andExpect(status().is3xxRedirection());

		assertThat(bookRepository.findById(availableBook.getId()).orElseThrow().isAvailable()).isFalse();
		BorrowRecord issuedRecord = borrowRecordRepository
				.findByIdAndBorrowerName(
						borrowRecordRepository.findByReturnDateIsNullAndBorrowerNameOrderByDueDateAsc("taylor")
								.stream()
								.filter(record -> record.getBook().getId().equals(availableBook.getId()))
								.findFirst()
								.orElseThrow()
								.getId(),
						"taylor")
				.orElseThrow();
		assertThat(issuedRecord.getDueDate()).isEqualTo(LocalDate.now().plusDays(7));

		mockMvc.perform(post("/return")
				.session(session)
				.with(csrf())
				.param("recordId", issuedRecord.getId().toString()))
				.andExpect(status().is3xxRedirection());

		assertThat(bookRepository.findById(availableBook.getId()).orElseThrow().isAvailable()).isTrue();
		assertThat(borrowRecordRepository.findById(issuedRecord.getId()).orElseThrow().getReturnDate())
				.isEqualTo(LocalDate.now());
	}
}
