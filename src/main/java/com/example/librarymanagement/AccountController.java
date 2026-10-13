package com.example.librarymanagement;

import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AccountController {

	private final LibraryUserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public AccountController(LibraryUserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@GetMapping("/login")
	public String login() {
		return "login";
	}

	@GetMapping("/register")
	public String register() {
		return "register";
	}

	@PostMapping("/register")
	public String createAccount(@RequestParam String username, @RequestParam String password,
			@RequestParam String confirmPassword, Model model) {
		String normalizedUsername = username.trim().toLowerCase(Locale.ROOT);
		if (!normalizedUsername.matches("[a-z0-9._-]{3,64}")) {
			model.addAttribute("error", "Choose a username with 3–64 letters, numbers, dots, dashes, or underscores.");
			model.addAttribute("username", username.trim());
			return "register";
		}
		if (password.length() < 8) {
			model.addAttribute("error", "Your password must be at least 8 characters long.");
			model.addAttribute("username", username.trim());
			return "register";
		}
		if (!password.equals(confirmPassword)) {
			model.addAttribute("error", "Those passwords do not match. Please try again.");
			model.addAttribute("username", username.trim());
			return "register";
		}
		if (userRepository.existsByUsername(normalizedUsername)) {
			model.addAttribute("error", "That username is already in use. Try signing in instead.");
			model.addAttribute("username", username.trim());
			return "register";
		}

		userRepository.save(new LibraryUser(normalizedUsername, passwordEncoder.encode(password)));
		return "redirect:/login?registered";
	}
}
