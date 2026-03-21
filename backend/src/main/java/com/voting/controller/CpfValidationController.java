package com.voting.controller;

import com.voting.dto.VotingDTOs;
import com.voting.facade.CpfValidationFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Users", description = "User/CPF validation endpoints (Bonus Task 1)")
public class CpfValidationController {

    private final CpfValidationFacade cpfValidationFacade;

    @GetMapping("/{cpf}/voting-eligibility")
    @Operation(summary = "Check if a user is eligible to vote by CPF")
    public ResponseEntity<?> checkEligibility(@PathVariable String cpf) {
        log.info("GET /api/v1/users/{}/voting-eligibility", "***");
        try {
            VotingDTOs.CpfValidationResponse response = cpfValidationFacade.validate(cpf);
            return ResponseEntity.ok(response);
        } catch (CpfValidationFacade.CpfNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(VotingDTOs.ErrorResponse.builder()
                            .status(404)
                            .error("Not Found")
                            .message(ex.getMessage())
                            .timestamp(java.time.LocalDateTime.now())
                            .build());
        }
    }
}
