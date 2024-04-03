package fr.cnam.toni.usecase.controller.rest.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.util.UriTemplate;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * System Integration Test (SIT): nécessite que l'application soit lancée auparavant avec profil par défaut (H2) ou postgres (contactant une
 * base PostgreSQL réelle)
 * <p>
 * Rappel: Les classes de test en *IT ne sont pas exécutées par le build standard Maven.<br>
 * Pour les lancer:<br>
 * - mvn verify -Pfailsafe<br>
 * - mvn verify -Pfailsafe -Dspring.profiles.active=&lt;profile spring de test - ex: test-st3&gt;<br>
 * Pour les lancer (avec uniquement la compile + execution des SITs):<br>
 * - mvn test-compile failsafe:integration-test failsafe:verify<br>
 * - mvn test-compile failsafe:integration-test failsafe:verify -Dspring.profiles.active=&lt;profile spring de test - ex: test-st3&gt;
 * <p>
 * <b>ATTENTION:</b> bien qu'aucune méthode @{@link Test} ne soit nécessaire dans cette classe (toutes les méthodes de test sont dans la
 * classe ancêtre, il est impératif d'en inclure au moins une (même si elle ne comporte pas de code):<br>
 * - en effet, cette classe comportant une inner-class @{@link Configuration}, cette classe et potentiellement utilisable dans dans tout
 * test qui effectuerait des {@link ComponentScan} (par exemple: des test {@link SpringBootTest} sans inner-class Configuration).<br>
 * - Afin que Spring identifie cette classe comme un test, et qu'il n'inclue jamais sa Configuration lors d'un autre test, il faut inclure
 * au moins une méthode annotée @Test
 *
 * @author CNAM DDSI DAMSI
 */
@SpringBootTest
@TestMethodOrder(OrderAnnotation.class)
@TestInstance(Lifecycle.PER_CLASS)
@Disabled
class BeneficiairesApiControllerSIT extends ABeneficiairesApiControllerSysTests {

    /**
     * Configuration du test:
     * <p>
     * On crée les objets {@link TestRestTemplate} et {@link ObjectMapper} qui seront injectés dans le parent.
     */
    @Configuration
    @ImportAutoConfiguration(value = {
            RestTemplateAutoConfiguration.class,
            JacksonAutoConfiguration.class})
    protected static class TestConfig {

        @Bean
        public TestRestTemplate template(RestTemplateBuilder builder) {
            return new TestRestTemplate(builder);
        }
    }

    @Value("${sk.test.system.url.rest.public}/beneficiaires")
    String rootUrl;

    @Autowired
    TestRestTemplate restTemplate;

    /**
     * On instancie les objets clients nécessaires à la classe parent pour créer ses requêtes HTTP.
     *
     * @throws URISyntaxException en cas de syntaxe d'URLs
     */
    @PostConstruct
    public void setupUrlObjects() throws URISyntaxException {
        uriResources = new URI(rootUrl);
        uriTemplateId = new UriTemplate(rootUrl + "/{id}");
        uriTemplateNir = new UriTemplate(rootUrl + "?matricule={m}");
        uriTemplateKeys = new UriTemplate(rootUrl + "?matricule={m}&date-naissance={d}&rang={r}");
    }

    @Test
    @SuppressWarnings("java:S2699")
    void dontReuseMyConfig() {
        // rien à faire: cf. javadoc de la classe.
    }
}

