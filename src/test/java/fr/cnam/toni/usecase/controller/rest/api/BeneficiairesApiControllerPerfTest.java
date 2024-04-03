package fr.cnam.toni.usecase.controller.rest.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.noconnor.junitperf.JUnitPerfInterceptor;
import com.github.noconnor.junitperf.JUnitPerfTest;
import fr.cnam.toni.starter.rest.responses.ValueResponse;
import fr.cnam.toni.usecase.business.api.BeneficiaireService;
import fr.cnam.toni.usecase.business.model.Beneficiaire;
import fr.cnam.toni.usecase.controller.rest.BeneficiaireResourceAdapter;
import fr.cnam.toni.usecase.controller.rest.DefaultBeneficiairesApiDelegate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.CoreMatchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BeneficiairesApiController.class)
@TestPropertySource(properties = {
        "openapi.beneficiaires.base-path: /"
})
@Import(DefaultBeneficiairesApiDelegate.class)
@ExtendWith(JUnitPerfInterceptor.class)
class BeneficiairesApiControllerPerfTest {

    private static final String BENEF_PATH = "/beneficiaires";

    @Autowired
    MockMvc mvc;

    @MockBean
    BeneficiaireService service;

    @Autowired
    ObjectMapper mapper;

    @Test
    @JUnitPerfTest(threads = 1, durationMs = 30_000, rampUpPeriodMs = 2_000, warmUpMs = 10_000, maxExecutionsPerSecond = 11_000)
    @WithMockUser
    void givenServiceReturnsCreatedBenef_whenCreate_then201_withBenef() throws Exception {
        // Given
        Beneficiaire beneficiaire = Beneficiaire.of("1234567890123", LocalDate.of(2000, 12, 31), 1, "Söse", "Kaiser");
        BeneficiaireResourceAdapter adapter = new BeneficiaireResourceAdapter();
        given(service.createBeneficiaire(any(Beneficiaire.class))).willReturn(beneficiaire.withId(1L));

        // When
        mvc.perform(post(BENEF_PATH).content(mapper.writeValueAsString(adapter.toRes(beneficiaire)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                // Then
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith(BENEF_PATH + "/1")))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(mapper.writeValueAsString(
                        ValueResponse.of(adapter.toRes(beneficiaire.withId(1L))))));
        // Verify
        verify(service, times(1)).createBeneficiaire(beneficiaire);
    }

}
