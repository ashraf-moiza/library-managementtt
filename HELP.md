# Library Management

This Spring Boot application uses Java 17, Maven, Thymeleaf, Spring Data JPA, and
an in-memory H2 database. Create an account from the sign-in page; passwords are
stored as BCrypt hashes. Each reader sees and manages only their own loans.

## Run

From the project directory:

```powershell
.\mvnw.cmd spring-boot:run
```

Open [http://localhost:8080](http://localhost:8080). The library is protected by
sign-in, and new readers can register from the sign-in page. The in-memory
database (including reader accounts) is recreated on each application start
and is preloaded with sample books.

## Test

```powershell
.\mvnw.cmd clean test
```

The application calculates late fees at $2.00 per overdue day. The H2 console
is available at [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
while the application is running. Use JDBC URL `jdbc:h2:mem:librarydb` and
username `sa` (no password).
