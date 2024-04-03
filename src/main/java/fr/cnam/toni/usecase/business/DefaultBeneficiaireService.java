package fr.cnam.toni.usecase.business;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import fr.cnam.toni.usecase.business.api.BeneficiaireService;
import fr.cnam.toni.usecase.business.model.Beneficiaire;
import fr.cnam.toni.usecase.business.spi.BeneficiaireRepository;


/**
 * Implémentation de l'interface {@link BeneficiaireService}.
 *
 * Partie métier de l'application, c'est ici que doivent se retrouver les règles de gestion.
 * <p>
 * De plus, c'est ici que doivent se trouver les annotations de sécurité sur les méthodes pour restreindre les accès en fonctions des
 * {@link GrantedAuthority} et/ou des attributs de l'utilisateur (ou client) courant.
 *
 * @author CNAM DDSI DAMSI
 */
@Service
public class DefaultBeneficiaireService implements BeneficiaireService {

    /** Logger de la classe. */
    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * Reposiory de persistence des {@link Beneficiaire}
     */
    @Qualifier("restBeneficiaireRepository")
    private final BeneficiaireRepository repository;

    /**
     * Constructeur avec paramètre.
     */
    public DefaultBeneficiaireService(BeneficiaireRepository repository) {
        this.repository = repository;
    }

    @Override
//    @PreAuthorize("hasRole('BENEF_WRITE') OR hasAuthority('PERM_BENEF_CREATE')")
    public Beneficiaire createBeneficiaire(Beneficiaire beneficiaire) {
        return repository.createBeneficiaire(beneficiaire.withId(null));
    }

    @Override
//    @PreAuthorize("hasRole('BENEF_READ')")
    public Optional<Beneficiaire> readBeneficiaire(Long id) {
        return repository.readBeneficiaire(id);
    }

    @Override
//    @PreAuthorize("hasRole('BENEF_READ')")
    public Optional<Beneficiaire> findOneBeneficiaire(String matricule,
            LocalDate dateNaissance, int rang) {
        return repository.findOneBeneficiaire(matricule, dateNaissance, rang);
    }

    @Override
//    @PreAuthorize("hasRole('BENEF_READ')")
    public List<Beneficiaire> findAllBeneficiaires(String matricule) {

        List<Beneficiaire> response = repository.findAllBeneficiaires(matricule);
        LOGGER.debug("Bénéficiaires récupérés (prénoms): {}",
                () -> response.stream().map(Beneficiaire::getPrenom).collect(Collectors.joining(", ")));
        return response;
    }

    @Override
//    @PreAuthorize("hasRole('BENEF_WRITE') OR hasAuthority('PERM_BENEF_UPDATE')")
    public Beneficiaire updateBeneficiaire(Beneficiaire updatedBeneficiaire) {
        return repository.updateBeneficiaire(updatedBeneficiaire.withId(null));
    }

    @Override
//    @PreAuthorize("hasRole('BENEF_WRITE') OR hasAuthority('PERM_BENEF_UPDATE')")
    public Beneficiaire updateBeneficiaire(Long id, Beneficiaire updatedBeneficiaire) {
        return repository.updateBeneficiaire(id, updatedBeneficiaire.withId(null));
    }

    @Override
//    @PreAuthorize("hasRole('BENEF_WRITE') OR hasAuthority('PERM_BENEF_DELETE')")
    public boolean deleteBeneficiaire(Long id) {
        return repository.deleteBeneficiaire(id);
    }
}
