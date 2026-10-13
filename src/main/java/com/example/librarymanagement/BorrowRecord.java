package com.example.librarymanagement;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class BorrowRecord {

	private static final BigDecimal DAILY_FINE = new BigDecimal("2.00");

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "book_id", nullable = false)
	private Book book;

	private String borrowerName;

	private LocalDate issueDate;

	private LocalDate dueDate;

	private LocalDate returnDate;

	protected BorrowRecord() {
	}

	public BorrowRecord(Book book, String borrowerName, LocalDate issueDate, LocalDate dueDate) {
		this.book = book;
		this.borrowerName = borrowerName;
		this.issueDate = issueDate;
		this.dueDate = dueDate;
	}

	public BigDecimal calculateLateFee() {
		LocalDate assessedDate = returnDate == null ? LocalDate.now() : returnDate;
		if (dueDate == null || !assessedDate.isAfter(dueDate)) {
			return BigDecimal.ZERO.setScale(2);
		}
		long overdueDays = ChronoUnit.DAYS.between(dueDate, assessedDate);
		return DAILY_FINE.multiply(BigDecimal.valueOf(overdueDays));
	}

	public Long getId() {
		return id;
	}

	public Book getBook() {
		return book;
	}

	public void setBook(Book book) {
		this.book = book;
	}

	public String getBorrowerName() {
		return borrowerName;
	}

	public void setBorrowerName(String borrowerName) {
		this.borrowerName = borrowerName;
	}

	public LocalDate getIssueDate() {
		return issueDate;
	}

	public void setIssueDate(LocalDate issueDate) {
		this.issueDate = issueDate;
	}

	public LocalDate getDueDate() {
		return dueDate;
	}

	public void setDueDate(LocalDate dueDate) {
		this.dueDate = dueDate;
	}

	public LocalDate getReturnDate() {
		return returnDate;
	}

	public void setReturnDate(LocalDate returnDate) {
		this.returnDate = returnDate;
	}
}
