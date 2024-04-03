package fr.cnam.toni.usecase.controller.rest;

import java.net.URI;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import fr.cnam.toni.starter.core.exceptions.ClientException;
import fr.cnam.toni.starter.core.exceptions.CommonProblemType;
import fr.cnam.toni.starter.core.exceptions.ProblemType;
import fr.cnam.toni.starter.core.exceptions.ServiceException;
import fr.cnam.toni.usecase.business.api.BeneficiaireService;
import fr.cnam.toni.usecase.business.model.Beneficiaire;
import fr.cnam.toni.usecase.controller.rest.api.BeneficiairesApi;
import fr.cnam.toni.usecase.controller.rest.api.BeneficiairesApiController;
import fr.cnam.toni.usecase.controller.rest.api.BeneficiairesApiDelegate;
import fr.cnam.toni.usecase.controller.rest.model.BeneficiaireListResponse;
import fr.cnam.toni.usecase.controller.rest.model.BeneficiaireResource;
import fr.cnam.toni.usecase.controller.rest.model.BeneficiaireResponse;

/**
 * Classe déléguée du Controller REST de gestion des bénéficiaires. Le Controller REST est généré par OpenAPI-Generator (cf. classes /
 * interfaces {@link BeneficiairesApi}, {@link BeneficiairesApiController} et {@link BeneficiairesApiDelegate}).
 * <p>
 * Cette classe implémente le traitement effectif des appels REST, alors que les classes générées sont porteuses de la JavaDoc, et surtout
 * de toutes les annotations liées à Spring MVC.
 * <p>
 * Pour renvoyer des erreurs / problèmes au client contenant un body avec le détail du probème, on privilégiera des remontées d'exception du
 * type {@link ClientException} pour les erreurs imputables au client, ou {@link ServiceException} pour les erreurs imputables au service.
 * Ces exceptions contiennent un {@link ProblemType}, qui est utilisé par les gestionnaires d'exceptions {@link ControllerAdvice} (cf.
 * classe *ExceptionHandler) fournis par le starter-kit ({@link DefaultExceptionHandler} {@link FallBackExceptionHandler} et autres) pour
 * récupérer dans la configuration sk.problem.responses.* les propriétés (codes, messages etc) à inclure dans le body. Vous pouvez
 * évidemment créer vos propres handlers d'exception et/ou simplement créer et configurer vos propres {@link ProblemType} (cf.
 * {@link CommonProblemType}).
 *
 * @author CNAM DDSI DAMSI
 */
@Component
public class DefaultBeneficiairesApiDelegate implements BeneficiairesApiDelegate {

    /**
     * L'objet request qui sera injecté sous forme de proxy automatiquement, et donne accès à la reqête courante.
     * <p>
     * C'est le moyen d'avoir accès à la request, sans l'ajouter aux arguments des méthodes (façon standard avec Spring), ce qui est rendu
     * impossible par le fait que la classe courante implémente une interface générée que l'on ne peut donc pas modifier.
     */
    private final NativeWebRequest request;

    /** Le service métier : injecté via le constructeur */
    private final BeneficiaireService service;

    /** Adapteur entre modèle de l'API et modèle métier */
    private final BeneficiaireResourceAdapter adapter = new BeneficiaireResourceAdapter();

    /**
     * Constructeur avec dépendances.
     */
    public DefaultBeneficiairesApiDelegate(NativeWebRequest request, BeneficiaireService service) {
        this.request = request;
        this.service = service;
    }

    @Override
    public Optional<NativeWebRequest> getRequest() {
        return Optional.ofNullable(request);
    }

    @Override
    public ResponseEntity<BeneficiaireResponse> create(BeneficiaireResource resource) {

        Beneficiaire result = service.createBeneficiaire(adapter.toBiz(resource));

        URI location = ServletUriComponentsBuilder.fromCurrentRequestUri().path("/{id}")
                .buildAndExpand(result.getId()).toUri();

        return ResponseEntity.created(location).body(
                new BeneficiaireResponse().value(adapter.toRes(result)));
    }

    @Override
    public ResponseEntity<BeneficiaireResponse> read(Long id) {

        return service.readBeneficiaire(id)
                .map(ben -> ResponseEntity.ok().body(
                        new BeneficiaireResponse().value(adapter.toRes(ben))))
                .orElseThrow(
                        () -> new ClientException(CommonProblemType.RESSOURCE_NON_TROUVEE));
    }

    @Override
    public ResponseEntity<BeneficiaireListResponse> find(String matricule, LocalDate dateNaissance, Integer rang) {

        List<Beneficiaire> result;

        if (dateNaissance != null && rang != null) { // recherche par matricule /date /rang
            result = service.findOneBeneficiaire(matricule, dateNaissance, rang)
                    .map(Collections::singletonList)
                    .orElseGet(Collections::emptyList);
        } else if (dateNaissance == null && rang == null) { // recherche par matricule
            result = service.findAllBeneficiaires(matricule);
        } else { // cas non prévu
            throw new ClientException(CommonProblemType.DONNEES_INVALIDES);
        }

        return ResponseEntity.ok().body(
                new BeneficiaireListResponse().value(adapter.toResList(result)));
    }

    @Override
    public ResponseEntity<BeneficiaireResponse> update(BeneficiaireResource resource) {

        return ResponseEntity.ok().body(
                new BeneficiaireResponse().value(
                        adapter.toRes(
                                service.updateBeneficiaire(
                                        adapter.toBiz(resource)))));
    }

    @Override
    public ResponseEntity<BeneficiaireResponse> replace(Long id, BeneficiaireResource resource) {

        return ResponseEntity.ok().body(
                new BeneficiaireResponse().value(
                        adapter.toRes(
                                service.updateBeneficiaire(id,
                                        adapter.toBiz(resource)))));
    }

    @Override
    public ResponseEntity<Void> delete(Long id) {

        if (service.deleteBeneficiaire(id)) {
            return ResponseEntity.noContent().build();
        } else {
            throw new ClientException(CommonProblemType.RESSOURCE_NON_TROUVEE);
        }
    }
}
