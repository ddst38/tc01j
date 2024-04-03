package fr.cnam.toni.usecase.business;

import fr.cnam.toni.usecase.business.api.BeneficiaireService;
import fr.cnam.toni.usecase.business.model.Beneficiaire;
import fr.cnam.toni.usecase.business.spi.BeneficiaireRepository;
import jakarta.validation.ConstraintViolationException;
import org.assertj.core.api.AbstractThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
//import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test unitaire du service métier {@link DefaultBeneficiaireService}
 * <p>
 * Inclut les tests des annotations de sécurités, et des contraintes de validation.
 *
 * @author CNAM DDSI DAMSI
 */
@SpringBootTest
class DefaultBeneficiaireServiceTest {

    @MockBean
    BeneficiaireRepository repository;

    @Autowired
    BeneficiaireService service;

    @Configuration
//    @EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
    @ImportAutoConfiguration(value = {ValidationAutoConfiguration.class})
    public static class Config {

        @Bean
        public BeneficiaireService service(BeneficiaireRepository repository) {
            return new DefaultBeneficiaireService(repository);
        }
    }

    Beneficiaire benef1;

    @BeforeEach
    void setUp() throws Exception {
        benef1 = Beneficiaire.of("1234567890123", LocalDate.of(2000, 12, 31), 1, "Söse", "Kaiser");
    }

    @Test
    @WithAnonymousUser
    void givenAnonymousUsr_whenCreateBeneficiaire_thenException() {
        assertThatThrownBy(() -> {
            service.createBeneficiaire(benef1);
        }).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @WithMockUser(roles = "XXX")
    void givenUsrNotAllowed_whenCreateBeneficiaire_thenException() {
        assertThatThrownBy(() -> {
            service.createBeneficiaire(benef1);
        }).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @WithMockUser(roles = {"XXX", "BENEF_WRITE"})
    void givenUsrRoleWrite_whenCreateBeneficiaire_thenOk() {
        service.createBeneficiaire(benef1);
        verify(repository, times(1)).createBeneficiaire(benef1);
    }

    @Test
    @WithMockUser(authorities = {"ROLE_XXX", "PERM_BENEF_CREATE"})
    void givenUsrPermCreate_whenCreateBeneficiaire_thenOk() {
        service.createBeneficiaire(benef1);
        verify(repository, times(1)).createBeneficiaire(benef1);
    }

    @Test
    @WithAnonymousUser
    void givenAnonymousUsr_whenUpdateBeneficiaire_thenException() {
        assertThatThrownBy(() -> {
            service.updateBeneficiaire(benef1);
        }).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @WithMockUser(roles = "XXX")
    void givenUsrNotAllowed_whenUpdateBeneficiaire_thenException() {
        assertThatThrownBy(() -> {
            service.updateBeneficiaire(benef1);
        }).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @WithMockUser(roles = {"XXX", "BENEF_WRITE"})
    void givenUsrRoleWrite_whenUpdateBeneficiaire_thenOk() {
        when(repository.updateBeneficiaire(any(Beneficiaire.class))).thenReturn(benef1);
        Beneficiaire req = Beneficiaire.of("1234567890123", LocalDate.now(), 2, "xxx", "yyy");
        Beneficiaire ret = service.updateBeneficiaire(req);
        verify(repository, times(1)).updateBeneficiaire(req);
        assertThat(ret)
                .isEqualTo(benef1)
                .extracting(Beneficiaire::getNom, Beneficiaire::getPrenom)
                .containsExactly(benef1.getNom(), benef1.getPrenom());
    }

    @Test
    @WithMockUser(authorities = {"ROLE_XXX", "PERM_BENEF_UPDATE"})
    void givenUsrPermUpdate_whenUpdateBeneficiaire_thenOk() {
        service.updateBeneficiaire(benef1);
        verify(repository, times(1)).updateBeneficiaire(benef1);
    }

    @Test
    @WithAnonymousUser
    void givenAnonymousUsr_whenFindOneBeneficiaire_thenException() {
        assertThatThrownBy(() -> {
            service.findOneBeneficiaire(benef1.getMatricule(), benef1.getDateNaissance(), benef1.getRang());
        }).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @WithMockUser(roles = {"XXX", "BENEF_WRITE"})
    void givenUsrNotAllowed_whenFindOneBeneficiaire_thenException() {
        assertThatThrownBy(() -> {
            service.findOneBeneficiaire(benef1.getMatricule(), benef1.getDateNaissance(), benef1.getRang());
        }).isInstanceOf(AccessDeniedException.class);
    }

    @SuppressWarnings("unchecked")
    @Test
    @WithMockUser(roles = {"XXX", "BENEF_READ"})
    void givenUsrRoleRead_whenFindOneBeneficiaire_thenOk() {
        when(repository.findOneBeneficiaire(anyString(), any(LocalDate.class), anyInt())).thenReturn(Optional.ofNullable(benef1));
        Optional<Beneficiaire> optionalRet = service.findOneBeneficiaire("1234567890123", LocalDate.now(), 9);
        verify(repository, times(1)).findOneBeneficiaire("1234567890123", LocalDate.now(), 9);
        assertThat(optionalRet)
                .isPresent()
                .contains(benef1)
                .get()
                .extracting(Beneficiaire::getNom, Beneficiaire::getPrenom)
                .containsExactly(benef1.getNom(), benef1.getPrenom());
    }

    @Test
    @WithAnonymousUser
    void givenAnonymousUsr_whenFindAllBeneficiaires_thenException() {
        assertThatThrownBy(() -> {
            service.findAllBeneficiaires(benef1.getMatricule());
        }).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @WithMockUser(roles = {"XXX", "BENEF_WRITE"})
    void givenUsrNotAllowed_whenFindAllBeneficiaires_thenException() {
        assertThatThrownBy(() -> {
            service.findAllBeneficiaires(benef1.getMatricule());
        }).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @WithMockUser(roles = {"XXX", "BENEF_READ"})
    void givenUsrRoleRead_whenFindAllBeneficiaires_thenOk() {
        Beneficiaire benef2 = new Beneficiaire(benef1);
        benef2.setRang(2);
        List<Beneficiaire> benefList = Arrays.asList(benef1.withId(1L), benef2.withId(2L));

        when(repository.findAllBeneficiaires(anyString())).thenReturn(benefList);
        List<Beneficiaire> ret = service.findAllBeneficiaires("1234567890123");
        verify(repository, times(1)).findAllBeneficiaires("1234567890123");

        assertThat(ret)
                .hasSize(2)
                .containsExactly(benef1, benef2)
                .satisfies(ben -> {
                    assertThat(ben.getId()).isEqualTo(1L);
                    assertThat(ben.getNom()).isEqualTo(benef1.getNom());
                    assertThat(ben.getPrenom()).isEqualTo(benef1.getPrenom());
                }, atIndex(0))
                .satisfies(ben -> {
                    assertThat(ben.getId()).isEqualTo(2L);
                    assertThat(ben.getNom()).isEqualTo(benef2.getNom());
                    assertThat(ben.getPrenom()).isEqualTo(benef2.getPrenom());
                }, atIndex(1));
    }

    // Tests de validation des contraintes
    // -------------------------------------------------------------------------

    // ~ Tests des contraintes de validation @Valid des POJO

    /**
     * @return la liste des {@link Arguments} à passer aux Tests paramétrés
     *         whenCreateBeneficiaireWithOneWrongField_thenValidationErrorForThisField et
     *         whenUpdateBeneficiaireWithOneWrongField_thenValidationErrorForThisField: <br>
     *         une liste de paires (Beneficiaire ayant un champ ne respectant pas les contraintes de validation définies dans la classe
     *         {@link Beneficiaire}) / le nom du champ ne respectant pas la contrainte
     */
    private static Stream<Arguments> provideBeneficiairesWithOneWrongField() {
        Beneficiaire benefOk = Beneficiaire.of("1234567890123", LocalDate.of(2000, 1, 1), 1, "TOU", "toké");
        Beneficiaire benef1NirNull = new Beneficiaire(benefOk);
        benef1NirNull.setMatricule(null);
        Beneficiaire benef2NirEmpty = new Beneficiaire(benefOk);
        benef2NirEmpty.setMatricule("");
        Beneficiaire benef3NirShort = new Beneficiaire(benefOk);
        benef3NirShort.setMatricule("12345");
        Beneficiaire benef4NirLong = new Beneficiaire(benefOk);
        benef4NirLong.setMatricule(benefOk.getMatricule() + "9");

        Beneficiaire benef5DateNaiNull = new Beneficiaire(benefOk);
        benef5DateNaiNull.setDateNaissance(null);

        Beneficiaire benef6Rang0 = new Beneficiaire(benefOk);
        benef6Rang0.setRang(0);
        Beneficiaire benef7RangLessThan0 = new Beneficiaire(benefOk);
        benef7RangLessThan0.setRang(-7);

        Beneficiaire benef8NomNull = new Beneficiaire(benefOk);
        benef8NomNull.setNom(null);
        Beneficiaire benef9NomVide = new Beneficiaire(benefOk);
        benef9NomVide.setNom("");

        Beneficiaire benef10PrenomNull = new Beneficiaire(benefOk);
        benef10PrenomNull.setPrenom(null);
        Beneficiaire benef11PrenomVide = new Beneficiaire(benefOk);
        benef11PrenomVide.setPrenom("");

        return Stream.of(
                Arguments.of(benef1NirNull, "matricule"),
                Arguments.of(benef2NirEmpty, "matricule"),
                Arguments.of(benef3NirShort, "matricule"),
                Arguments.of(benef4NirLong, "matricule"),
                Arguments.of(benef5DateNaiNull, "dateNaissance"),
                Arguments.of(benef6Rang0, "rang"),
                Arguments.of(benef7RangLessThan0, "rang"),
                Arguments.of(benef8NomNull, "nom"),
                Arguments.of(benef9NomVide, "nom"),
                Arguments.of(benef10PrenomNull, "prenom"),
                Arguments.of(benef11PrenomVide, "prenom")
        //
        );
    }

    @ParameterizedTest
    @WithMockUser(roles = {"BENEF_WRITE"})
    @MethodSource("provideBeneficiairesWithOneWrongField")
    void whenCreateBeneficiaireWithOneWrongField_thenValidationErrorForThisField(
            Beneficiaire benef, String wrongFieldName) {

        assertThatThrownBy(() -> {
            service.createBeneficiaire(benef);
        })
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining(wrongFieldName);
    }

    @ParameterizedTest
    @WithMockUser(roles = {"BENEF_WRITE"})
    @MethodSource("provideBeneficiairesWithOneWrongField")
    void whenUpdateBeneficiaireWithOneWrongField_thenValidationErrorForThisField(
            Beneficiaire benef, String wrongFieldName) {

        assertThatThrownBy(() -> {
            service.updateBeneficiaire(benef);
        })
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining(wrongFieldName);
    }

    @Test
    @WithMockUser(roles = {"BENEF_WRITE"})
    void whenCreateBeneficiaireWithAllFieldsWrong_thenValidationErrorForAllFields() {
        Beneficiaire benef = Beneficiaire.of("123", null, 0, "", null);

        assertThatThrownBy(() -> {
            service.createBeneficiaire(benef);
        })
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("matricule")
                .hasMessageContaining("dateNaissance")
                .hasMessageContaining("rang")
                .hasMessageContaining("nom")
                .hasMessageContaining("prenom");
    }

    @Test
    @WithMockUser(roles = {"BENEF_WRITE"})
    void whenUpdateBeneficiaireWithAllFieldsWrong_thenValidationErrorForAllFields() {
        Beneficiaire benef = Beneficiaire.of("123", null, 0, "", null);

        assertThatThrownBy(() -> {
            service.updateBeneficiaire(benef);
        })
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("matricule")
                .hasMessageContaining("dateNaissance")
                .hasMessageContaining("rang")
                .hasMessageContaining("nom")
                .hasMessageContaining("prenom");
    }

    // ~ Tests des contraintes de validation jakarta.validation.constraints.*
    // sur des paramètres simples (non POJO)
    // -------------------------------------------------------------------------

    /**
     * @return la liste des {@link Arguments} à passer aux Tests paramétrés
     *         whenCreateBeneficiaireWithOneWrongField_thenValidationErrorForThisField et
     *         whenUpdateBeneficiaireWithOneWrongField_thenValidationErrorForThisField: <br>
     *         une liste de paires (Beneficiaire ayant un champ ne respectant pas les contraintes de validation définies dans la classe
     *         {@link Beneficiaire}) / le nom du champ ne respectant pas la contrainte
     */
    private static Stream<Arguments> provideBeneficiaireKeysWithValidationProblems() {
        String validNir = "1234567890123";
        LocalDate validDateNaissance = LocalDate.of(1999, 12, 31);
        int validRang = 1;

        String matricule = "arg0";
        String dateNaissance = "arg1";
        String rang = "arg2";

        return Stream.of(
                Arguments.of(validNir, validDateNaissance, 0, Arrays.asList(rang)),
                Arguments.of(validNir, validDateNaissance, -1, Arrays.asList(rang)),
                Arguments.of(validNir, null, validRang, Arrays.asList(dateNaissance)),
                Arguments.of(validNir + "1", validDateNaissance, validRang, Arrays.asList(matricule)),
                Arguments.of(validNir.substring(1), validDateNaissance, validRang, Arrays.asList(matricule)),
                Arguments.of("", validDateNaissance, validRang, Arrays.asList(matricule)),
                Arguments.of(null, validDateNaissance, validRang, Arrays.asList(matricule)),
                Arguments.of(validNir, null, 0, Arrays.asList(dateNaissance, rang)),
                Arguments.of("", validDateNaissance, 0, Arrays.asList(matricule, rang)),
                Arguments.of(null, null, validRang, Arrays.asList(matricule, dateNaissance))
        //
        );
    }

    @ParameterizedTest
    @MethodSource("provideBeneficiaireKeysWithValidationProblems")
    @WithMockUser(roles = {"BENEF_READ"})
    void whenFindOneBeneficiaireWithWrongKeys_thenValidationError(
            String matricule, LocalDate dateNaissance, int rang,
            List<String> fieldsWithValidationProblem) {

        AbstractThrowableAssert<?, ? extends Throwable> assertion = assertThatThrownBy(() -> {
            service.findOneBeneficiaire(matricule, dateNaissance, rang);
        })
                .isInstanceOf(ConstraintViolationException.class);

        fieldsWithValidationProblem.stream().forEach(field -> assertion.hasMessageContaining(field));
    }

    @Test
    @WithMockUser(roles = {"BENEF_READ"})
    void whenFindAllBeneficiairesWithWrongNir_thenValidationError() {

        String matricule = "arg0";

        assertThatThrownBy(() -> {
            service.findAllBeneficiaires(null);
        })
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining(matricule);

        assertThatThrownBy(() -> {
            service.findAllBeneficiaires("");
        })
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining(matricule);
    }

}
