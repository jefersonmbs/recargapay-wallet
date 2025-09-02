# Recarga Pay Digital Wallet API

A Spring Boot-based digital wallet service that provides comprehensive financial transaction management with audit capabilities.

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Running the Service](#running-the-service)
- [Testing](#testing)
- [API Documentation](#api-documentation)
- [Configuration](#configuration)
- [Development](#development)

## 🔍 Overview

The Recarga Pay Digital Wallet API is a robust financial service built with Spring Boot 3.5.5 that enables users to manage digital wallets, perform transactions (deposits, withdrawals, transfers), and maintain comprehensive audit trails. The service implements industry-standard patterns including transaction strategies, comprehensive validation, and audit logging.

## ✨ Features

- **Wallet Management**: Create and manage digital wallets for users
- **Transaction Processing**: Support for deposits, withdrawals, and transfers
- **Audit Trail**: Comprehensive transaction auditing with correlation IDs
- **Data Validation**: Robust input validation and error handling
- **Database Migrations**: Automated database schema management with Flyway
- **API Documentation**: Interactive Swagger UI documentation
- **Circuit Breaker**: Resilience4j integration for fault tolerance
- **Transaction History**: Complete transaction history with pagination

## 📋 Prerequisites

Before running the service, ensure you have the following installed:

- **Java 21** or later
- **Docker** and **Docker Compose** (for database)
- **Gradle** (or use the included wrapper)

## 🚀 Installation

### 1. Clone the Repository

```bash
git clone <repository-url>
cd recargapay-wallet
```

### 2. Set Up the Database

Start the PostgreSQL database using Docker Compose:

```bash
docker-compose up -d postgres
```

This will:
- Create a PostgreSQL 15 container
- Set up the `recargapay_wallet` database
- Configure the database with user `postgres` and password `password`
- Expose the database on port `5432`

### 3. Build the Application

Using Gradle wrapper (recommended):

```bash
# Windows
./gradlew build

# Linux/macOS
./gradlew build
```

Or using installed Gradle:

```bash
gradle build
```

## 🏃 Running the Service

### Option 1: Using Gradle (Development)

```bash
# Windows
./gradlew bootRun

# Linux/macOS
./gradlew bootRun
```

### Option 2: Using JAR File

```bash
# Build the JAR
./gradlew bootJar

# Run the JAR
java -jar build/libs/recargapay-wallet-0.0.1-SNAPSHOT.jar
```

### Option 3: Using IDE

Import the project into your IDE (IntelliJ IDEA, Eclipse, VS Code) and run the `RecargapayWalletApplication` main class.

The service will start on **http://localhost:8080** (default port).

## 🧪 Testing

### Run All Tests

```bash
./gradlew test
```

### Run Specific Test Classes

```bash
# Run wallet controller tests
./gradlew test --tests WalletControllerTest

# Run integration tests
./gradlew test --tests "*IntegrationTest"
```

### Test Database

Tests use an in-memory H2 database, so no additional setup is required. Test configuration is located in `src/test/resources/application-test.yml`.

### Generate Test Reports

After running tests, view the HTML report at:
```
build/reports/tests/test/index.html
```

## 📚 API Documentation

Once the service is running, you can access the interactive API documentation:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api-docs

### Key Endpoints

- **Users**: `/api/users` - User management
- **Wallets**: `/api/wallets` - Wallet operations
- **Transactions**: Transaction processing through wallet endpoints

## ⚙️ Configuration

### Database Configuration

The service uses PostgreSQL by default. Key configuration in `application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/recargapay_wallet
    username: postgres
    password: password
```

### Environment Variables

You can override configuration using environment variables:

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://your-host:5432/your-db
export SPRING_DATASOURCE_USERNAME=your-username
export SPRING_DATASOURCE_PASSWORD=your-password
```

### Logging Configuration

Logging levels can be adjusted in `application.yml`:

```yaml
logging:
  level:
    br.com.jefersonmbs.recargapaywallet: INFO
    org.springframework.web: DEBUG
```

## 🛠️ Development

### Project Structure

```
src/
├── main/java/br/com/jefersonmbs/recargapaywallet/
│   ├── api/                 # REST controllers and DTOs
│   ├── domain/              # Business logic, entities, services
│   ├── infrastructure/      # Configuration, interceptors
│   └── RecargapayWalletApplication.java
├── main/resources/
│   ├── application.yml      # Application configuration
│   └── db/migration/        # Flyway database migrations
└── test/                    # Test classes
```

### Key Technologies

- **Spring Boot 3.5.5** - Main framework
- **Spring Data JPA** - Database access
- **PostgreSQL** - Production database
- **H2** - Test database  
- **Flyway** - Database migrations
- **MapStruct** - Object mapping
- **Lombok** - Boilerplate reduction
- **Resilience4j** - Circuit breaker
- **SpringDoc OpenAPI** - API documentation

### Database Migrations

Database schema is managed with Flyway migrations in `src/main/resources/db/migration/`:

- `V1__initial_tables.sql` - Initial schema
- `V2__create_transaction_audit_table.sql` - Audit table
- `V3__add_correlation_id_to_transaction_history.sql` - Correlation ID support

### Adding New Migrations

1. Create a new file: `src/main/resources/db/migration/V{version}__{description}.sql`
2. Write your SQL migration
3. Restart the application - Flyway will apply it automatically

### Code Quality

- Use Lombok annotations to reduce boilerplate
- Follow the existing package structure
- Write tests for new features
- Use the strategy pattern for transaction types
- Implement proper validation and error handling

## 🚨 Troubleshooting

### Common Issues

1. **Database Connection Failed**
   - Ensure PostgreSQL is running: `docker-compose ps`
   - Check database logs: `docker-compose logs postgres`

2. **Port Already in Use**
   - Change the server port in `application.yml`:
     ```yaml
     server:
       port: 8081
     ```

3. **Migration Errors**
   - Check Flyway migration files for syntax errors
   - Ensure migration versions are sequential

4. **Test Failures**
   - Ensure no other instance is running on test ports
   - Check test-specific configuration in `application-test.yml`

### Getting Help

For issues related to:
- **Application**: Check the logs and Spring Boot documentation
- **Database**: Verify PostgreSQL connection and Flyway migrations
- **API**: Use Swagger UI to test endpoints interactively

## 👤 Contact

- **Developer**: Jeferson Martins
- **Email**: jefersonmartinsbsantos@gmail.com

---