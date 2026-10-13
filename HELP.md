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

## Deploy to Render with MySQL

The repository includes a Render Blueprint (`render.yaml`) and a Java 17 Dockerfile.
Create a Render Blueprint from the GitHub repository, then provide these secret
environment values for the web service:

- `SPRING_DATASOURCE_URL`: MySQL JDBC URL, for example
  `jdbc:mysql://db.example.com:3306/library?sslMode=REQUIRED`
- `SPRING_DATASOURCE_USERNAME`: MySQL username
- `SPRING_DATASOURCE_PASSWORD`: MySQL password

The MySQL database must be reachable from Render. Do not commit credentials or
put them in `render.yaml`; enter them in the Render dashboard. Local development
continues to use in-memory H2 when `SPRING_DATASOURCE_URL` is not set.
