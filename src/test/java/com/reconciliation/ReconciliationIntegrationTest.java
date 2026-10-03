package com.reconciliation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reconciliation.dto.DiscrepancyResolutionDto;
import com.reconciliation.dto.ReconciliationRequestDto;
import com.reconciliation.entity.MatchResult;
import com.reconciliation.entity.ReconciliationBatch;
import com.reconciliation.repository.MatchResultRepository;

import com.reconciliation.service.SampleDataGeneratorService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
@Transactional
class ReconciliationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SampleDataGeneratorService sampleDataGeneratorService;

    @Autowired
    private MatchResultRepository matchResultRepository;

    @Test
    @DisplayName("Full Reconciliation Workflow: Generate sample data, run batch, inspect results, and resolve discrepancy")
    void testFullReconciliationWorkflow() throws Exception {
        // 1. Generate sample data
        sampleDataGeneratorService.generateSampleDataSet();

        // 2. Trigger reconciliation batch
        ReconciliationRequestDto req = new ReconciliationRequestDto();
        req.setBatchName("Integration Test Run");

        String responseJson = mockMvc.perform(post("/api/v1/reconciliation/run")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andReturn().getResponse().getContentAsString();

        ReconciliationBatch batch = objectMapper.readValue(responseJson, ReconciliationBatch.class);
        assertNotNull(batch.getId());
        assertTrue(batch.getMatchedCount() > 0);
        assertTrue(batch.getMismatchCount() > 0);

        // 3. Fetch batch results via API
        mockMvc.perform(get("/api/v1/reconciliation/batches/" + batch.getId() + "/results"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        // 4. Resolve a discrepancy
        List<MatchResult> discrepancies = matchResultRepository.findByBatchId(batch.getId()).stream()
                .filter(r -> r.getMatchStatus().name().equals("DISCREPANCY"))
                .toList();

        assertFalse(discrepancies.isEmpty());
        MatchResult targetDiscrepancy = discrepancies.get(0);

        DiscrepancyResolutionDto resolution = new DiscrepancyResolutionDto("Approved processor fee variance", "TEST_AUDITOR");

        mockMvc.perform(post("/api/v1/reconciliation/results/" + targetDiscrepancy.getId() + "/resolve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resolution)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resolved").value(true))
                .andExpect(jsonPath("$.resolvedBy").value("TEST_AUDITOR"));
    }
}
