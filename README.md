# Transaction Reconciliation Service 🔄

[![Java 21](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.3-green.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

An enterprise-grade, high-performance automated transaction matching and reconciliation service built with **Java 21**, **Spring Boot 3**, **PostgreSQL**, **JPA/Hibernate**, and **Flyway**.

The system matches internal payment records against external processor settlement batches (e.g., Stripe, Square, Adyen, Chase), identifies discrepancies using configurable multi-pass tolerance rules, and maintains auditable resolution logs.

---

## 🏗 System Architecture

```
Internal Payments CSV ──┐
                        ├──▶ [ Multi-Pass Matching Engine ] ──▶ [ PostgreSQL Audit Database ]
Settlement File CSV ────┘            │
                                     ├──▶ Matched Records (Exact / Fuzzy)
                                     ├──▶ Discrepancies (Amount, Fee, Date)
                                     ├──▶ Missing Records (Orphans)
                                     └──▶ Duplicates Flagged
                                             │
                                             ▼
                                  [ Reconciliation Report & Web UI ]
```

---

## ✨ Key Features

- **⚡ Multi-Pass Rule Pipeline Engine**:
  - **Pass 1 (Exact Match)**: Matches identical reference IDs, currency, amounts, and dates.
  - **Pass 2 (Discrepancy Categorization)**: Identifies variance categories: `AMOUNT_MISMATCH`, `FEE_MISMATCH`, `DATE_MISMATCH`.
  - **Pass 3 (Fuzzy Match)**: Matches transactions across missing reference IDs using amount tolerances and timestamp windows.
  - **Pass 4 (Orphan & Duplicate Detection)**: Flags `MISSING_SETTLEMENT`, `MISSING_INTERNAL`, and `DUPLICATE_TRANSACTION`.

- **🎛 Configurable Matching Tolerances**:
  - Dynamic amount tolerance (e.g., $\pm\$0.05$).
  - Dynamic processor fee tolerance (e.g., $\pm\$0.01$).
  - Dynamic timestamp window (e.g., $\pm 24$ hours / 1440 mins).
  - Strict vs. flexible currency matching.

- **📥 Robust CSV Parser & Duplicate Detection**:
  - Flexible header mapping (`reference_id`, `transaction_id`, `settlement_amount`, etc.).
  - Duplicate detection across internal batches and database history.

- **⏱ Scheduled & Async Background Processing**:
  - Automated background batch execution powered by `@Scheduled` tasks and thread-safe batch locks.

- **🖥 Modern Web Dashboard UI & OpenAPI**:
  - Single-page glassmorphism dashboard at `http://localhost:8080/`.
  - Interactive CSV upload dropzones, live metrics cards, filterable discrepancy table, resolution modal dialogs, and CSV report exports.
  - OpenAPI 3.0 / Swagger UI documentation at `http://localhost:8080/swagger-ui.html`.

- **🧪 Enterprise Testing Strategy**:
  - Unit tests for matching engine & CSV parser.
  - Integration tests with Spring Boot Test & MockMvc.
  - Live PostgreSQL database integration testing via **Testcontainers**.

- **🐳 Production Packaging & CI**:
  - Multi-stage `Dockerfile`.
  - `docker-compose.yml` for local container orchestration.
  - GitHub Actions CI workflow (`.github/workflows/ci.yml`).

---

## 🚀 Quick Start

### Prerequisites
- JDK 21+
- Apache Maven 3.9+
- Docker & Docker Compose (Optional for container deployment)

### Running Locally (H2 Profile - Zero Configuration)
```bash
# Compile and run spring boot app
mvn spring-boot:run
```
Access the Interactive Web Dashboard at: `http://localhost:8088/`  
Access Swagger API Documentation at: `http://localhost:8088/swagger-ui.html`

### Running with Docker & PostgreSQL
```bash
docker-compose up --build -d
```

---

## 📊 Resume Bullets

> **Built a Java/Spring Boot transaction reconciliation service that matched internal payment records against settlement batches, identified discrepancies using configurable business rules, and persisted auditable results in PostgreSQL.**

> **Added REST endpoints, idempotent batch processing, JUnit/Testcontainers integration tests, Docker packaging, and GitHub Actions CI.**

---

## 📄 License
Distributed under the MIT License.
