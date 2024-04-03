package fr.cnam.toni.usecase.provider.memory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.atIndex;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import fr.cnam.toni.starter.core.exceptions.ClientException;
import fr.cnam.toni.starter.core.exceptions.CommonProblemType;
import fr.cnam.toni.usecase.business.model.Beneficiaire;
import fr.cnam.toni.usecase.business.spi.BeneficiaireRepository;

/**
 * Tests Unitaire du provider {@link MemoryBeneficiaireRepository}
 * <p>
 * On rajoute une annotation @{@link DirtiesContext} sur chaque méthode qui modifie les données du repository pour que chaque méthode ait un
 * état de départ identique.
 *
 * @author CNAM DDSI DAMSI
 *
 */
@SpringJUnitConfig
@TestPropertySource(properties = {
        "usecase.provider.memory.beneficiaires[0].matricule: 1234567890123",
        "usecase.provider.memory.beneficiaires[0].dateNaissance: 1999-12-31",
        "usecase.provider.memory.beneficiaires[0].rang: 1",
        "usecase.provider.memory.beneficiaires[0].nom: Frangin",
        "usecase.provider.memory.beneficiaires[0].prenom: ainé"
})
class MemoryBeneficiaireRepositoryTest {

    @Configuration
    @EnableConfigurationProperties(MemoryRepositoryProperties.class)
    public static class Config {
        @Bean
        public LocalDateConverter dateConverter() {
            return new LocalDateConverter();
        }

        @Bean
        public BeneficiaireRepository repository(MemoryRepositoryProperties initProps) {
            return new MemoryBeneficiaireRepository(initProps);
        }
    }

    @Autowired
    protected BeneficiaireRepository repository;

    /** Bénéficiaire correspondant à celui pré-initialisé dans le cache du repository */
    private Beneficiaire benefExistant;

    /** Bénéficiaire non-existant dans le cache du repository */
    private Beneficiaire benef1;

    @BeforeEach
    void setUp() throws Exception {
        benefExistant = Beneficiaire.of("1234567890123", LocalDate.of(1999, 12, 31), 1, "Frangin", "ainé");
        benef1 = Beneficiaire.of("1234567890123", LocalDate.of(1999, 12, 31), 2, "Frangin", "cadet");
    }

    @Test
    void contextLoads() {
        System.out.println("Test context loaded successfully");
    }

    @Test
    @DirtiesContext
    @SuppressWarnings("unchecked")
    void givenBenefDoesntExist_whenCreate_thenCanFindItAfter() {
        repository.createBeneficiaire(benef1);
        Optional<Beneficiaire> optionalFind =
                repository.findOneBeneficiaire(benef1.getMatricule(), benef1.getDateNaissance(), benef1.getRang());

        assertThat(optionalFind)
                .isPresent()
                .contains(benef1)
                .get()
                .extracting(Beneficiaire::getId, Beneficiaire::getNom, Beneficiaire::getPrenom)
                .containsExactly(2L, benef1.getNom(), benef1.getPrenom());
    }

    @Test
    void givenBenefExists_whenCreateSameBenef_thenException() {
        assertThatThrownBy(() -> {
            repository.createBeneficiaire(benefExistant);
        }).isInstanceOf(ClientException.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    void givenBenefExists_whenFindOneBeneficiaire_thenOk() {
        Optional<Beneficiaire> optionalFind =
                repository.findOneBeneficiaire(benefExistant.getMatricule(), benefExistant.getDateNaissance(), benefExistant.getRang());

        assertThat(optionalFind)
                .isPresent()
                .contains(benefExistant)
                .get()
                .extracting(Beneficiaire::getId, Beneficiaire::getNom, Beneficiaire::getPrenom)
                .containsExactly(1L, benefExistant.getNom(), benefExistant.getPrenom());
    }

    @Test
    void givenBenefDoesntExist_whenFindOneBeneficiaire_thenException() {
        assertThat(repository.findOneBeneficiaire(benef1.getMatricule(), benef1.getDateNaissance(), benef1.getRang()))
                .isEmpty();
    }

    @Test
    void givenBenefExistsWithUniqueNir_whenFindAllBeneficiaires_thenGetOneBenef() {
        List<Beneficiaire> findList =
                repository.findAllBeneficiaires(benefExistant.getMatricule());

        assertThat(findList)
                .hasSize(1)
                .containsExactly(benefExistant)
                .satisfies(ben -> {
                    assertThat(ben.getId()).isEqualTo(1L);
                    assertThat(ben.getNom()).isEqualTo(benefExistant.getNom());
                    assertThat(ben.getPrenom()).isEqualTo(benefExistant.getPrenom());
                }, atIndex(0));
    }

    @Test
    @DirtiesContext
    void given2BenefExistWithSameNir_whenFindAllBeneficiaires_thenGet2Benefs() {
        repository.createBeneficiaire(benef1);

        List<Beneficiaire> findList =
                repository.findAllBeneficiaires(benefExistant.getMatricule());

        assertThat(findList)
                .hasSize(2)
                .contains(benef1, benefExistant)
                .satisfies(ben -> {
                    assertThat(ben.getId()).isEqualTo(1L);
                    assertThat(ben.getNom()).isEqualTo(benefExistant.getNom());
                    assertThat(ben.getPrenom()).isEqualTo(benefExistant.getPrenom());
                }, atIndex(findList.indexOf(benefExistant)))
                .satisfies(ben -> {
                    assertThat(ben.getId()).isEqualTo(2L);
                    assertThat(ben.getNom()).isEqualTo(benef1.getNom());
                    assertThat(ben.getPrenom()).isEqualTo(benef1.getPrenom());
                }, atIndex(findList.indexOf(benef1)));
    }

    @Test
    @DirtiesContext
    void given2BenefExistWithSameNirAndOthersWithDifferent_whenFindAllBeneficiaires_thenGet2Benefs() {
        Beneficiaire benef2 = new Beneficiaire(benef1);
        benef2.setMatricule("1770797012345");
        repository.createBeneficiaire(benef1);
        repository.createBeneficiaire(benef2);

        List<Beneficiaire> findList =
                repository.findAllBeneficiaires(benefExistant.getMatricule());

        assertThat(findList)
                .hasSize(2)
                .contains(benef1, benefExistant)
                .satisfies(ben -> {
                    assertThat(ben.getId()).isEqualTo(1L);
                    assertThat(ben.getNom()).isEqualTo(benefExistant.getNom());
                    assertThat(ben.getPrenom()).isEqualTo(benefExistant.getPrenom());
                }, atIndex(findList.indexOf(benefExistant)))
                .satisfies(ben -> {
                    assertThat(ben.getId()).isEqualTo(2L);
                    assertThat(ben.getNom()).isEqualTo(benef1.getNom());
                    assertThat(ben.getPrenom()).isEqualTo(benef1.getPrenom());
                }, atIndex(findList.indexOf(benef1)));
    }

    @Test
    @DirtiesContext
    void givenBenefExist_whenUpdateNomWithNullPrenom_thenOnlyNomIsModified() {
        Beneficiaire benefUpdateCommand = new Beneficiaire(benefExistant);
        benefUpdateCommand.setNom("modified");
        benefUpdateCommand.setPrenom(null);

        Beneficiaire updated = repository.updateBeneficiaire(benefUpdateCommand);
        assertThat(updated)
                .isEqualTo(benefExistant)
                .extracting(Beneficiaire::getId, Beneficiaire::getNom, Beneficiaire::getPrenom)
                .containsExactly(1L, benefUpdateCommand.getNom(), benefExistant.getPrenom());
    }

    @Test
    @DirtiesContext
    void givenBenefExist_whenUpdatePrenomWithNullNom_thenOnlyPrenomIsModified() {
        Beneficiaire benefUpdateCommand = new Beneficiaire(benefExistant);
        benefUpdateCommand.setNom(null);
        benefUpdateCommand.setPrenom("modified");

        Beneficiaire updated = repository.updateBeneficiaire(benefUpdateCommand);
        assertThat(updated)
                .isEqualTo(benefExistant)
                .extracting(Beneficiaire::getId, Beneficiaire::getNom, Beneficiaire::getPrenom)
                .containsExactly(1L, benefExistant.getNom(), benefUpdateCommand.getPrenom());
    }

    @Test
    @DirtiesContext
    void givenBenefExist_whenUpdateNamAndPrenom_thenBothAreModified() {
        Beneficiaire benefUpdateCommand = new Beneficiaire(benefExistant);
        benefUpdateCommand.setNom("NewNom");
        benefUpdateCommand.setPrenom("NewPrenom");

        Beneficiaire updated = repository.updateBeneficiaire(benefUpdateCommand);
        assertThat(updated)
                .isEqualTo(benefExistant)
                .extracting(Beneficiaire::getId, Beneficiaire::getNom, Beneficiaire::getPrenom)
                .containsExactly(1L, benefUpdateCommand.getNom(), benefUpdateCommand.getPrenom());
    }

    @Test
    void givenBenefDoesntExist_whenUpdate_thenException() {
        assertThatThrownBy(() -> {
            repository.updateBeneficiaire(benef1);
        })
                .isInstanceOf(ClientException.class)
                .extracting("type")
                .isEqualTo(CommonProblemType.RESSOURCE_NON_TROUVEE);
    }

    @Test
    @DirtiesContext
    void givenBenefExist_whenUpdateByIdWithoutIdInBenef_thenBenefReplaced() {
        Long idBenefExistant = repository
                .findOneBeneficiaire(benefExistant.getMatricule(), benefExistant.getDateNaissance(), benefExistant.getRang()).get().getId();

        Beneficiaire updated = repository.updateBeneficiaire(idBenefExistant, benef1);
        assertThat(updated)
                .isEqualTo(benef1)
                .extracting(Beneficiaire::getId, Beneficiaire::getNom, Beneficiaire::getPrenom)
                .containsExactly(idBenefExistant, benef1.getNom(), benef1.getPrenom());
    }

    @Test
    @DirtiesContext
    void givenBenefExist_whenUpdateByIdWithDifferentIdInBenef_thenBenefReplacedAndIdInBenefIgnored() {
        Long idBenefExistant = repository
                .findOneBeneficiaire(benefExistant.getMatricule(), benefExistant.getDateNaissance(), benefExistant.getRang()).get().getId();

        Beneficiaire updated = repository.updateBeneficiaire(idBenefExistant, benef1.withId(99L));
        assertThat(updated)
                .isEqualTo(benef1)
                .extracting(Beneficiaire::getId, Beneficiaire::getNom, Beneficiaire::getPrenom)
                .containsExactly(idBenefExistant, benef1.getNom(), benef1.getPrenom());
    }

    @Test
    @DirtiesContext
    void givenBenefDoesntExistWithExpectedId_whenUpdateById_thenException() {
        assertThatThrownBy(() -> {
            repository.updateBeneficiaire(99L, benef1);
        })
                .isInstanceOf(ClientException.class)
                .extracting("type")
                .isEqualTo(CommonProblemType.RESSOURCE_NON_TROUVEE);
    }

    @Test
    @DirtiesContext
    void givenBenefExists_whenDeleteById_thenOkAndCantReadAnymore() {
        Long idBenefExistant = repository
                .findOneBeneficiaire(benefExistant.getMatricule(), benefExistant.getDateNaissance(), benefExistant.getRang()).get().getId();
        repository.deleteBeneficiaire(idBenefExistant);
        assertThat(repository.readBeneficiaire(idBenefExistant)).isEmpty();
    }

    @Test
    void givenBenefDoesntExist_whenDeleteById_thenFalse() {
        Boolean deleted = repository.deleteBeneficiaire(99L);
        assertThat(deleted).isFalse();
    }

}
