package fr.cnam.toni.usecase.controller.rest.api;

import org.jsmart.zerocode.core.domain.LoadWith;
import org.jsmart.zerocode.core.domain.TestMapping;
import org.jsmart.zerocode.core.domain.TestMappings;
import org.jsmart.zerocode.jupiter.extension.ParallelLoadExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@LoadWith("load_generation.properties")
@ExtendWith({ParallelLoadExtension.class})
class BeneficiairesApiControllerLoadTest {

    @Test
    @DisplayName("givenServiceReturnsCreatedBenef_whenCreate_then201_withBenef")
    @TestMappings({
            @TestMapping(testClass = BeneficiairesApiControllerTest.class, testMethod = "givenServiceReturnsCreatedBenef_whenCreate_then201_withBenef")
    })
    void givenServiceReturnsCreatedBenef_whenCreate_then201_withBenef() {
        // This space remains empty
    }
}
