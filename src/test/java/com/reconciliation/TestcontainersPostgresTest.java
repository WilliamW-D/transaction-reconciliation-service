package com.reconciliation;

import com.reconciliation.dto.ReconciliationRequestDto;
import com.reconciliation.entity.ReconciliationBatch;
import com.reconciliation.enums.BatchStatus;
import com.reconciliation.service.ReconciliationService;
import com.reconciliation.service.SampleDataGeneratorService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
class TestcontainersPostgresTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("test_reconciliation")
            .withUsername("test_user")
            .withPassword("test_pass");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
    }

    @Autowired
    private SampleDataGeneratorService sampleDataGeneratorService;

    @Autowired
    private ReconciliationService reconciliationService;

    @Test
    @DisplayName("Verify Flyway migrations and reconciliation pipeline on live PostgreSQL Docker container via Testcontainers")
    void testPostgresReconciliationPipeline() {
        assertTrue(postgres.isRunning());

        // 1. Generate sample mock transactions directly into PostgreSQL
        sampleDataGeneratorService.generateSampleDataSet();

        // 2. Execute reconciliation batch against PostgreSQL
        ReconciliationRequestDto req = new ReconciliationRequestDto();
        req.setBatchName("Testcontainers Postgres Run");

        ReconciliationBatch batch = reconciliationService.runReconciliation(req);

        assertNotNull(batch);
        assertEquals(BatchStatus.COMPLETED, batch.getStatus());
        assertTrue(batch.getMatchedCount() > 0);
        assertTrue(batch.getTotalInternalCount() > 0);
    }
}
