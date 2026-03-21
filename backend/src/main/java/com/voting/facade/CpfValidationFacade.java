package com.voting.facade;

import com.voting.dto.VotingDTOs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * Facade/Client Fake that simulates an external CPF validation service.
 * Bonus Task 1 implementation.
 *
 * Normal behavior (cpf.validation.strict=true, default):
 *   1. Valida estrutura do CPF pelo algoritmo brasileiro
 *   2. Simula serviço externo rejeitando ~50% aleatoriamente (404)
 *   3. Retorna ABLE_TO_VOTE ou UNABLE_TO_VOTE aleatoriamente
 *
 * Perf profile (cpf.validation.strict=false):
 *   - Bypassa todas as validações e sempre retorna ABLE_TO_VOTE
 *   - Permite que o Gatling use CPFs sequenciais sem bloqueio
 *   - Foca o teste na performance do sistema de votação em si
 */
@Component
@Slf4j
public class CpfValidationFacade {

    private static final Random RANDOM = new Random();

    @Value("${cpf.validation.strict:true}")
    private boolean strictValidation;

    public VotingDTOs.CpfValidationResponse validate(String cpf) {
        // Modo perf: bypass total — foca no throughput de votos
        if (!strictValidation) {
            return VotingDTOs.CpfValidationResponse.builder()
                    .status("ABLE_TO_VOTE")
                    .build();
        }

        log.debug("Validating CPF: {}", maskCpf(cpf));
        String cleanCpf = cpf.replaceAll("[^0-9]", "");

        // Step 1: validação estrutural
        if (!isStructurallyValid(cleanCpf)) {
            log.warn("CPF {} is structurally invalid", maskCpf(cpf));
            throw new CpfNotFoundException("Invalid CPF: " + maskCpf(cpf));
        }

        // Step 2: serviço externo rejeita ~50% aleatoriamente
        if (RANDOM.nextBoolean()) {
            log.debug("External service rejected CPF {}", maskCpf(cpf));
            throw new CpfNotFoundException("CPF not found in external service: " + maskCpf(cpf));
        }

        // Step 3: elegibilidade aleatória
        String status = RANDOM.nextBoolean() ? "ABLE_TO_VOTE" : "UNABLE_TO_VOTE";
        log.debug("CPF {} result: {}", maskCpf(cpf), status);

        return VotingDTOs.CpfValidationResponse.builder().status(status).build();
    }

    private boolean isStructurallyValid(String cpf) {
        if (cpf == null || cpf.length() != 11) return false;
        if (cpf.chars().distinct().count() == 1) return false;

        int sum = 0;
        for (int i = 0; i < 9; i++)
            sum += Character.getNumericValue(cpf.charAt(i)) * (10 - i);
        int first = 11 - (sum % 11);
        if (first >= 10) first = 0;
        if (first != Character.getNumericValue(cpf.charAt(9))) return false;

        sum = 0;
        for (int i = 0; i < 10; i++)
            sum += Character.getNumericValue(cpf.charAt(i)) * (11 - i);
        int second = 11 - (sum % 11);
        if (second >= 10) second = 0;
        return second == Character.getNumericValue(cpf.charAt(10));
    }

    private String maskCpf(String cpf) {
        if (cpf == null || cpf.length() < 6) return "***";
        String clean = cpf.replaceAll("[^0-9]", "");
        if (clean.length() >= 11)
            return clean.substring(0, 3) + ".***.***-" + clean.substring(9);
        return cpf.substring(0, 3) + "***";
    }

    public static class CpfNotFoundException extends RuntimeException {
        public CpfNotFoundException(String message) { super(message); }
    }
}
