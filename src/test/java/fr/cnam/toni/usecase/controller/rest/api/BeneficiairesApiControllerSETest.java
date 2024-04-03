package fr.cnam.toni.usecase.controller.rest.api;

import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.web.util.UriTemplate;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * System Embedded Test (SETest): lance l'application et exécute des tests par envoi de requêtes REST.
 * <p>
 * L'application est lancée sur un port aléatoire pour pouvoir être utilisée en environnement d'intégration continue.
 * <p>
 * Ce test n'a pas de dépendances à des ressources externes, il peut-donc être exécuté par défaut dans la phase 'test' de maven.<br>
 * Dans le cas contraire, on aurait ajouté le tag JUnit @{@link Tag} avec la valeur "exclude-from-ci": pour ne pas l'exécuter par défaut via
 * mvn test ou mvn install, et donc pas dans les jobs Jenkins de la PIC.
 * <p>
 * Le code des tests ne se trouve pas dans cette classe mais dans une classe ancêtre afin de partager le code avec les tests SIT (sytem
 * integration tests).
 * <p>
 * Dans cette classe en ne trouve donc que la config spécifique au mode embarqué.
 *
 * @author CNAM DDSI DAMSI
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestMethodOrder(OrderAnnotation.class)
@TestInstance(Lifecycle.PER_CLASS)
@Disabled
class BeneficiairesApiControllerSETest extends ABeneficiairesApiControllerSysTests { // NOSONAR - les tests sont hérités

    /** Port du serveur à tester */
    @LocalServerPort
    private int port;

    /** URL path des resources bénéficiaires */
    @Value("${openapi.beneficiaires.base-path}/beneficiaires")
    private String controllerRootPath;

    @Autowired
    TestRestTemplate restTemplate;

    /**
     * On instancie les objets clients nécessaires à la classe parent pour créer ses requêtes HTTP.
     *
     * @throws URISyntaxException en cas de syntaxe d'URLs
     */
    @PostConstruct
    private void postConstruct() throws URISyntaxException {
        String rootUrlString = "http://localhost:" + port + controllerRootPath;
        uriResources = new URI(rootUrlString);
        uriTemplateId = new UriTemplate(rootUrlString + "/{id}");
        uriTemplateNir = new UriTemplate(rootUrlString + "?matricule={m}");
        uriTemplateKeys = new UriTemplate(rootUrlString + "?matricule={m}&date-naissance={d}&rang={r}");
    }
}
