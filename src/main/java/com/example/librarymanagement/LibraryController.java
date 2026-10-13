package com.example.librarymanagement;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

@Controller
public class LibraryController {

	private final BookRepository bookRepository;
	private final BorrowRecordRepository borrowRecordRepository;

	public LibraryController(BookRepository bookRepository, BorrowRecordRepository borrowRecordRepository) {
		this.bookRepository = bookRepository;
		this.borrowRecordRepository = borrowRecordRepository;
	}

	@GetMapping("/")
	public String home(@AuthenticationPrincipal UserDetails user, Model model,
			@RequestParam(required = false) String status) {
		model.addAttribute("availableBooks", bookRepository.findByAvailable(true));
		model.addAttribute("activeRecords",
				borrowRecordRepository.findByReturnDateIsNullAndBorrowerNameOrderByDueDateAsc(user.getUsername()));
		model.addAttribute("username", user.getUsername());
		if ("issued".equals(status)) {
			model.addAttribute("notice", "Book issued successfully. It has been added to your loans.");
		} else if ("returned".equals(status)) {
			model.addAttribute("notice", "Book returned successfully. Thank you!");
		}
		return "index";
	}

	@PostMapping("/issue")
	@Transactional
	public String issueBook(@RequestParam Long bookId, @RequestParam int days,
			@AuthenticationPrincipal UserDetails user) {
		if (days < 1 || days > 90) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose a loan duration between 1 and 90 days.");
		}

		Book book = findBook(bookId);
		if (!book.isAvailable()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "This book is already issued.");
		}

		book.setAvailable(false);
		bookRepository.save(book);

		LocalDate issueDate = LocalDate.now();
		BorrowRecord record = new BorrowRecord(book, user.getUsername(), issueDate, issueDate.plusDays(days));
		borrowRecordRepository.save(record);
		return "redirect:/?status=issued";
	}

	@PostMapping("/return")
	@Transactional
	public String returnBook(@RequestParam Long recordId, @AuthenticationPrincipal UserDetails user) {
		BorrowRecord record = borrowRecordRepository.findByIdAndBorrowerName(recordId, user.getUsername())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Your active loan was not found."));
		if (record.getReturnDate() != null) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "This book has already been returned.");
		}

		record.setReturnDate(LocalDate.now());
		record.getBook().setAvailable(true);
		borrowRecordRepository.save(record);
		bookRepository.save(record.getBook());
		return "redirect:/?status=returned";
	}

	private Book findBook(Long bookId) {
		Optional<Book> book = bookRepository.findById(bookId);
		return book.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found."));
	}
}
