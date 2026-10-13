package com.example.librarymanagement;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Long> {

	List<BorrowRecord> findByReturnDateIsNullAndBorrowerNameOrderByDueDateAsc(String borrowerName);

	Optional<BorrowRecord> findByIdAndBorrowerName(Long id, String borrowerName);
}
