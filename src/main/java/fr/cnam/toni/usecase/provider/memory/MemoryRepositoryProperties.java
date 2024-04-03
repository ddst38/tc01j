package fr.cnam.toni.usecase.provider.memory;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import fr.cnam.toni.usecase.business.model.Beneficiaire;

/**
 * Classe POJO annotée @{@link ConfigurationProperties} que spring boot utilisera pour stocker toutes les properties ayant le préfixe donné
 * dans l'annotation.
 * <p>
 * Ainsi on obtiendra une liste de {@link Beneficiaire} qui sera utilisable par le Repository correspondant
 *
 * @author CNAM DDSI DAMSI
 */
@Component
@Validated
@ConfigurationProperties(prefix = "usecase.provider.memory")
public class MemoryRepositoryProperties {

    /**
     * Liste des bénéficiaires à charger en mémoire au démarrage pour le repository {@link MemoryBeneficiaireRepository}.
     */
    @Valid
    private final List<Beneficiaire> beneficiaires = new ArrayList<>(10);

    /**
     * @return une liste de {@link Beneficiaire}
     */
    public List<Beneficiaire> getBeneficiaires() {
        return beneficiaires;
    }
}
