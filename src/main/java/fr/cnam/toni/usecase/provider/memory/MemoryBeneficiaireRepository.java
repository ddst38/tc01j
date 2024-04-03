package fr.cnam.toni.usecase.provider.memory;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import fr.cnam.toni.starter.core.exceptions.ClientException;
import fr.cnam.toni.starter.core.exceptions.CommonProblemType;
import fr.cnam.toni.usecase.business.model.Beneficiaire;
import fr.cnam.toni.usecase.business.spi.BeneficiaireRepository;


/**
 * Provider de type {@link Repository} implémentant l'interface {@link BeneficiaireRepository}, utilisant un simple cache mémoire.
 *
 * @author CNAM DDSI DAMSI
 */
@Repository
@Primary
public class MemoryBeneficiaireRepository implements BeneficiaireRepository {

    /** Logger */
    private static final Logger LOGGER = LogManager.getLogger();

    /** La base mémoire de bénéficiaires */
    private final Map<Long, Beneficiaire> cache;

    /** Dernier Id utilisé pour insérer dans la base */
    private long lastId = 0L;

    /**
     * Constructeur du repository avec dépendances.
     */
    public MemoryBeneficiaireRepository(MemoryRepositoryProperties initProperties) {
        // On initialise notre cache mémoire avec une map synchronisée basée
        // sur une copie de la liste configurée
        cache = Collections.synchronizedMap(new HashMap<Long, Beneficiaire>());

        initProperties.getBeneficiaires().stream()
                .forEach(beneficiaire -> {
                    lastId++;
                    cache.put(lastId, beneficiaire.withId(lastId));
                });


        LOGGER.debug("Cache mémoire initialisé avec {}",
                () -> cache.entrySet().stream().map(e -> e.getValue().toString())
                        .collect(Collectors.joining(",", "{", "}")));
    }

    @Override
//    @MethodLog
    public Beneficiaire createBeneficiaire(Beneficiaire beneficiaire) {

        if (!cache.containsValue(beneficiaire)) {
            lastId++;
            Beneficiaire newBenef = beneficiaire.withId(lastId);
            cache.put(lastId, newBenef);
            return new Beneficiaire(newBenef); // pour retourner une copie
        } else {
            throw new ClientException(CommonProblemType.RESSOURCE_DEJA_EXISTANTE);
        }
    }

    @Override
    public Optional<Beneficiaire> findOneBeneficiaire(String matricule, LocalDate dateNaissance, int rang) {

        Beneficiaire benefWithKeys = Beneficiaire.of(matricule, dateNaissance, rang, null, null);

        return cache.values().stream()
                .filter(b -> b.equals(benefWithKeys))
                .map(Beneficiaire::new) // pour retourner une copie
                .findAny();
    }

    @Override
    public List<Beneficiaire> findAllBeneficiaires(String matricule) {

        return cache.values().stream()
                .filter(b -> b.getMatricule().equals(matricule))
                .map(Beneficiaire::new) // pour retourner une copie
                .collect(Collectors.toList());
    }

    @Override
    public Beneficiaire updateBeneficiaire(Beneficiaire beneficiaire) {

        Beneficiaire benefInDb = cache.values().stream()
                .filter(b -> b.equals(beneficiaire))
                .findAny()
                .orElseThrow(() -> new ClientException(CommonProblemType.RESSOURCE_NON_TROUVEE));

        if (beneficiaire.getNom() != null) {
            benefInDb.setNom(beneficiaire.getNom());
        }
        if (beneficiaire.getPrenom() != null) {
            benefInDb.setPrenom(beneficiaire.getPrenom());
        }

        return new Beneficiaire(benefInDb);
    }

    @Override
    public Beneficiaire updateBeneficiaire(Long id, Beneficiaire beneficiaire) {

        if (cache.containsKey(id)) {
            cache.put(id, beneficiaire.withId(id));
        } else {
            throw new ClientException(CommonProblemType.RESSOURCE_NON_TROUVEE);
        }

        return new Beneficiaire(cache.get(id));
    }

    @Override
    public boolean deleteBeneficiaire(Long id) {
        return cache.remove(id) != null;
    }

    @Override
    public Optional<Beneficiaire> readBeneficiaire(Long id) {
        return Optional.ofNullable(cache.get(id))
                .map(Beneficiaire::new); // pour retourner une copie
    }

}
