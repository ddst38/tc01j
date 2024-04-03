package fr.cnam.toni.usecase.provider.memory;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import fr.cnam.toni.usecase.business.model.Beneficiaire;

@SpringJUnitConfig
@TestPropertySource(properties = {
        "usecase.provider.memory.beneficiaires[0].matricule: 1234567890123",
        "usecase.provider.memory.beneficiaires[0].dateNaissance: 1999-12-31",
        "usecase.provider.memory.beneficiaires[0].rang: 3",
        "usecase.provider.memory.beneficiaires[0].nom: Frangin",
        "usecase.provider.memory.beneficiaires[0].prenom: troisième"
})
class MemoryRepositoryPropertiesTest {

    @Configuration
    @EnableConfigurationProperties(MemoryRepositoryProperties.class)
    public static class ConfigTest {
        @Bean
        public LocalDateConverter dateConverter() {
            return new LocalDateConverter();
        }
    }

    @Autowired
    MemoryRepositoryProperties memoryprops;

    @Test
    void testGetBeneficiaires() {
        assertThat(memoryprops.getBeneficiaires()).isNotEmpty();
        Beneficiaire benef = memoryprops.getBeneficiaires().get(0);
        assertThat(benef.getMatricule()).isEqualTo("1234567890123");
        assertThat(benef.getDateNaissance()).isEqualTo(LocalDate.of(1999, 12, 31));
        assertThat(benef.getRang()).isEqualTo(3);
        assertThat(benef.getNom()).isEqualTo("Frangin");
        assertThat(benef.getPrenom()).isEqualTo("troisième");
    }
}
