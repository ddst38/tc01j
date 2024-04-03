package fr.cnam.toni.usecase.controller.rest.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.cnam.toni.starter.core.exceptions.ClientException;
import fr.cnam.toni.starter.core.exceptions.CommonProblemType;
import fr.cnam.toni.starter.core.exceptions.ProblemType;
import fr.cnam.toni.starter.rest.responses.ValueResponse;
import fr.cnam.toni.usecase.business.api.BeneficiaireService;
import fr.cnam.toni.usecase.business.model.Beneficiaire;
import fr.cnam.toni.usecase.controller.rest.BeneficiaireResourceAdapter;
import fr.cnam.toni.usecase.controller.rest.DefaultBeneficiairesApiDelegate;
import jakarta.annotation.security.RolesAllowed;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test Unitaires & Intégration du controlleur REST: composant constitué des classes générées par OpenAPI-Generator:<br>
 * - {@link BeneficiairesApi},<br>
 * - {@link BeneficiairesController},<br>
 * - {@link BeneficiairesApiDelegate},<br>
 * Et de notre implémentation de l'interface <code>BeneficiairesApiDelegate</code>: {@link DefaultBeneficiairesApiDelegate}
 * <p>
 * C'est un test unitaire dans le sens ou on ne teste pas les autres composants de l'application, mais d'intégration dans le sens ou on
 * teste effectivement tout le comportement de ce composant en l'intégrant avec une couche MVC afin de pouvoir vérifier le bon comportement
 * de toutes les annotations de notre controlleur. Un pure test unitaire de notre controlleur n'aurait aucun intérêt.
 * <p>
 * Cependant on ne teste pas ici la transformations des exceptions en réponses JSON, qui est réalisée dans un {@link ControllerAdvice}, donc
 * en dehors de ce composant.
 * <p>
 * Attention par défaut avec {@link WebMvcTest} la sécurité par défaut sécurise toutes les URL en autorisant l'accès qu'aux users
 * authentifés Attention cependant, cette configuration de test ne permet pas de tester les annotations @{@link PreAuthorize}
 * / @{@link Secured} / @{@link RolesAllowed} car la sécurité sur les méthode n'est pas activée par {@link WebMvcTest}. Il est possible de
 * le faire en utilisant @{@link ContextConfiguration} et une inner classe @{@link Configuration} qui devra instancier le controller et
 * porter l'annotation {@link EnableGlobalMethodSecurity} avec prePostEnabled = true, secured = true.<br>
 * => <b>Toutefois nous déconseillons de faire porter par les controlleurs les annotations de sécurité</b>, mais plutot de les faire porter
 * par la couche service, afin de pouvoir mutualiser cette sécurité dans le cas de rajout de nouvelles couches d'exposition (SOAP par
 * exemple)
 *
 * @author CNAM DDSI DAMSI
 */
@WebMvcTest(controllers = BeneficiairesApiController.class)
@TestPropertySource(properties = {
        "openapi.beneficiaires.base-path: /"
})
@Import(DefaultBeneficiairesApiDelegate.class)
class BeneficiairesApiControllerTest {

    /**
     * Predicat pour faciliter les assertions sur les exceptions du style<br>
     * {@code assertThatThrownBy(() -> {...}).getCause().matches(isClientExceptionWithType(xxx));}
     */
    static Predicate<Throwable> isClientExceptionWithType(ProblemType type) {
        return t -> t instanceof ClientException && ((ClientException) t).getType() == type;
    }

    private static final String BENEF_PATH = "/beneficiaires";
    private static final String BENEF_ID_PATH = BENEF_PATH + "/{id}";

    @Autowired
    MockMvc mvc;

    @MockBean
    BeneficiaireService service;

    @Autowired
    ObjectMapper mapper;

    BeneficiaireResourceAdapter adapter = new BeneficiaireResourceAdapter();

    Beneficiaire benef1;
    Beneficiaire benef2;

    @BeforeEach
    void setUp() throws Exception {
        benef1 = Beneficiaire.of("1234567890123", LocalDate.of(2000, 12, 31), 1, "Söse", "Kaiser");
        benef2 = Beneficiaire.of("1234567890123", LocalDate.of(2000, 12, 31), 2, "Söse", "Junior");
    }

    // Avec WebMvcTest toute les URLs sont protégées et ne sont accessibles qu'avec le rôle ROLE_USER
    // ~ @WithMockUser injecte un tel user dans le contexte de sécurité
    // ----------------------------------------------------------------------------------------------------------------
    @Test
    @WithMockUser
    void givenServiceReturnsCreatedBenef_whenCreate_then201_withBenef() throws Exception {
        // Given
        given(service.createBeneficiaire(any(Beneficiaire.class))).willReturn(benef1.withId(1L));
        // When
        mvc.perform(post(BENEF_PATH).content(mapper.writeValueAsString(adapter.toRes(benef1)))
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
                // Then
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith(BENEF_PATH + "/1")))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(mapper.writeValueAsString(
                        ValueResponse.of(adapter.toRes(benef1.withId(1L))))));
        // Verify
//        verify(service, times(1)).createBeneficiaire(benef1);
    }

    @Test
    @WithMockUser
    void givenServiceReturnsBenef_whenReadById_then200_withBenef() throws Exception {
        // Given
        given(service.readBeneficiaire(anyLong())).willReturn(Optional.of(benef1.withId(1L)));
        // When
        mvc.perform(get(BENEF_ID_PATH, 1L))
                // Then
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(mapper.writeValueAsString(
                        ValueResponse.of(adapter.toRes(benef1.withId(1L))))));
        // Verify
        verify(service, times(1)).readBeneficiaire(1L);
    }

    @Test
    @WithMockUser
    void givenServiceReturnsEmpty_whenReadById_thenClientException() throws Exception {
        // Given
        given(service.readBeneficiaire(anyLong())).willReturn(Optional.empty());
        // When
        assertThatThrownBy(() -> {
            mvc.perform(get(BENEF_ID_PATH, 1L));
        })
                // Then
                .getCause()
                .matches(isClientExceptionWithType(CommonProblemType.RESSOURCE_NON_TROUVEE));
        // Verify
        verify(service, times(1)).readBeneficiaire(1L);
    }

    @Test
    @WithMockUser
    void givenServiceReturnsBenef_whenFindByKeys_then200_withBenef() throws Exception {
        // Given
        given(service.findOneBeneficiaire(anyString(), any(LocalDate.class), anyInt()))
                .willReturn(Optional.of(benef1.withId(1L)));
        // When
        URI uri = UriComponentsBuilder.fromPath(BENEF_PATH).query("matricule={m}&date-naissance={d}&rang={r}")
                .buildAndExpand(benef1.getMatricule(), benef1.getDateNaissance().format(DateTimeFormatter.ISO_DATE), benef1.getRang())
                .toUri();
        mvc.perform(get(uri))
                // Then
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(mapper.writeValueAsString(
                        ValueResponse.of(adapter.toResList(Arrays.asList(benef1.withId(1L)))))));
        // Verify
        verify(service, times(1)).findOneBeneficiaire(benef1.getMatricule(), benef1.getDateNaissance(), benef1.getRang());
    }

    @Test
    @WithMockUser
    void givenServiceReturnsEmpty_whenFindByKeys_then200_withBodyWithEmptyArray() throws Exception {
        // Given
        given(service.findOneBeneficiaire(anyString(), any(LocalDate.class), anyInt()))
                .willReturn(Optional.empty());
        // When
        URI uri = UriComponentsBuilder.fromPath(BENEF_PATH).query("matricule={m}&date-naissance={d}&rang={r}")
                .buildAndExpand(benef1.getMatricule(), benef1.getDateNaissance().format(DateTimeFormatter.ISO_DATE), benef1.getRang())
                .toUri();

        mvc.perform(get(uri))
                // Then
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.value").isArray())
                .andExpect(jsonPath("$.value").isEmpty());

        // Verify
        verify(service, times(1)).findOneBeneficiaire(benef1.getMatricule(), benef1.getDateNaissance(), benef1.getRang());
    }


    @Test
    @WithMockUser
    void givenServiceReturns2Benef_whenFindByNir_then200_with2Benef() throws Exception {
        // Given
        given(service.findAllBeneficiaires(anyString())).willReturn(Arrays.asList(benef1.withId(1L), benef2.withId(2L)));
        // When
        URI uri = UriComponentsBuilder.fromPath(BENEF_PATH).query("matricule={m}")
                .buildAndExpand(benef1.getMatricule()).toUri();
        mvc.perform(get(uri))
                // Then
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(mapper.writeValueAsString(
                        ValueResponse.of(adapter.toResList(Arrays.asList(benef1.withId(1L), benef2.withId(2L)))))));
        // Verify
        verify(service, times(1)).findAllBeneficiaires(benef1.getMatricule());
    }

    @Test
    @WithMockUser
    void givenServiceReturnsNoBenef_whenFindByNir_then200_withBodyWithEmptyArray() throws Exception {
        // Given
        given(service.findAllBeneficiaires(anyString())).willReturn(Collections.emptyList());
        // When
        URI uri = UriComponentsBuilder.fromPath(BENEF_PATH).query("matricule={m}")
                .buildAndExpand(benef1.getMatricule()).toUri();
        mvc.perform(get(uri))
                // Then
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.value").isArray())
                .andExpect(jsonPath("$.value").isEmpty());
        // Verify
        verify(service, times(1)).findAllBeneficiaires(benef1.getMatricule());
    }

    @Test
    @WithMockUser
    void givenServiceUpdatesBenef_whenUpdateByKeys_then200_withBenef() throws Exception {
        // Given
        given(service.updateBeneficiaire(any(Beneficiaire.class))).willReturn(benef1.withId(1L));
        // When
        mvc.perform(put(BENEF_PATH)
                .content(mapper.writeValueAsString(adapter.toRes(benef1)))
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
                // Then
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(mapper.writeValueAsString(
                        ValueResponse.of(adapter.toRes(benef1.withId(1L))))));
        // Verify
        verify(service, times(1)).updateBeneficiaire(benef1);
    }

    @Test
    @WithMockUser
    void givenServiceUpdatesBenef_whenUpdateById_then200_withBenef() throws Exception {
        // Given
        given(service.updateBeneficiaire(anyLong(), any(Beneficiaire.class))).willReturn(benef1.withId(2L));
        // When
        mvc.perform(put(BENEF_ID_PATH, "2")
                .content(mapper.writeValueAsString(adapter.toRes(benef1.withId(99L))))
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
                // Then
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(mapper.writeValueAsString(
                        ValueResponse.of(adapter.toRes(benef1.withId(2L))))));
        // Verify
        verify(service, times(1)).updateBeneficiaire(2L, benef1);
    }

    @Test
    @WithMockUser
    void givenServiceDeleteReturnsTrue_whenDeleteById_then204_withoutBody() throws Exception {
        // Given
        given(service.deleteBeneficiaire(anyLong())).willReturn(Boolean.TRUE);
        // When
        mvc.perform(delete(BENEF_ID_PATH, 1L)
                .with(csrf()))
                // Then
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        // Verify
        verify(service, times(1)).deleteBeneficiaire(1L);
    }

    @Test
    @WithMockUser
    void givenServiceDeleteReturnsFalse_whenDeleteById_thenClientException() throws Exception {
        // Given
        given(service.deleteBeneficiaire(anyLong())).willReturn(Boolean.FALSE);
        // When
        assertThatThrownBy(() -> {
            mvc.perform(delete(BENEF_ID_PATH, 1L)
                    .with(csrf()));
        })
                // Then
                .getCause()
                .matches(isClientExceptionWithType(CommonProblemType.RESSOURCE_NON_TROUVEE));
        // Verify
        verify(service, times(1)).deleteBeneficiaire(1L);
    }
}
