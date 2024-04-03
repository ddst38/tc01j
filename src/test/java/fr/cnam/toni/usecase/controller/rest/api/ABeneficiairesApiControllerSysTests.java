package fr.cnam.toni.usecase.controller.rest.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.json.BasicJsonTester;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriTemplate;

import com.jayway.jsonpath.JsonPath;

import fr.cnam.toni.starter.rest.responses.Problem;
import fr.cnam.toni.starter.rest.responses.ValueResponse;
import fr.cnam.toni.usecase.controller.rest.model.BeneficiaireResource;

/**
 * Classe abstraite pour les tests "system" de l'application.
 * <p>
 * Elle contient le code des tests qui seront lancés soit:<br>
 * - avec un serveur "embarqué" lancé automatiquement sur port aléatoire<br>
 * classe XxxxApiControllerSETest (SETest = System Embedded Test) quand cela est possible et pertinent<br>
 * - en effectuant des requêtes vers une application lancée au préalable<br>
 * classe XxxxApiControllerSIT (SIT = System Integration Test)<br>
 *
 * @author CNAM DDSI DAMSI
 */
abstract class ABeneficiairesApiControllerSysTests {

    /**
     * Jetons authapp v3 valide jusqu'en juillet 2030
     * <p>
     * stockage sous forme de String pour pouvoir utiliser plusieurs jetons valides générés avec le p12 de plusieurs 'client' différents. En
     * les générant dynamiquement, on ne pourrait pas car un seul .p12 par application.
     */
    protected static final String AUTHAPP_V3_CERT2 = // cert2 à les permissions suivantes ROLE_BENEF_READ
            "Bearer eyJraWQiOiJjZXJ0MiIsInR5cCI6IkpXVCIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiJjZXJ0MiIsImF1ZCI6WyJTS01CX0oiLCJIRE1EX00iXSwibmJmIjo"
                    + "xNTk1NDk2ODg0LCJpc3MiOiJjZXJ0MiIsImV4cCI6MTkxMDg1Njg4NCwiaWF0IjoxNTk1NDk2ODg0LCJqdGkiOiI5YWYwNjEzYy0yYjhmLTQwYzctYmJ"
                    + "mOC0yZjU4YmNkYTM3YzkifQ.jhDUKKo6fZXaZ8YGToOjiY7tMNfNlz8aKLdng8Ar2sE-adhEuamaF-0rA3fpvOouxAQbDwvmwiCrGsmzqOOnGwu9fb3b"
                    + "ftign1rq7PqbXmnMeP9Xj54vOfCB-nbi5gZt5chAKwkjqhCg2ZfjtANPFFHuYS99KwhWMeazP8Q-Myw";


    protected static final String AUTHAPP_V3_CERT1 = // cert1 à les permissions suivantes ROLE_BENEF_READ,ROLE_BENEF_WRITE
            "Bearer eyJraWQiOiJjZXJ0MSIsInR5cCI6IkpXVCIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiJjZXJ0MSIsImF1ZCI6WyJTS01CX0oiLCJIRE1EX00iXSwibmJmIjo"
                    + "xNTk1NDk3MTI1LCJpc3MiOiJjZXJ0MSIsImV4cCI6MTkxMDg1NzEyNSwiaWF0IjoxNTk1NDk3MTI1LCJqdGkiOiI2ZTQxMmMyYS01MjE3LTRhMmItODI"
                    + "4MC1hZWZkYTdiNWYwMGMifQ.OWTW0E0nwvr5u7Ngh82fgMtkbfoRAyASUgayDBe4cmIus3Y_RVjFaAn6M9McI-034pFrIEqQX5ryCj3EaTEUzHk9FYqt"
                    + "D28rZInhTyt9xFouC11Wbzh39G6NDA21DXSrAlta7TzzV4F7mCRhe74upOajkUHsR6JlHvVNddDsReI";

    /** Nom du header pour jeton */
    protected static final String HEADER_V3 = "Authorization";
    /** Nom du header pour CSRF */
    protected static final String HEADER_CSRF = "X-CSRF-TOKEN";

    /**
     * Bénéficiaires à créer dans la BDD
     */
    private static BeneficiaireResource BENEF_NIR1_1;
    private static BeneficiaireResource BENEF_NIR1_2;
    private static BeneficiaireResource BENEF_NIR1_3;
    private static BeneficiaireResource BENEF_NIR2;

    /**
     * le {@link TestRestTemplate} pour effectuer des appels sur le serveur.<br>
     * Ce Template ne remonte pas d'exception sur des erreurs 50X ou 40X
     */
    @Autowired
    private TestRestTemplate restTemplate;

    /** Utilitaire pour les assertions JSON */
    BasicJsonTester jsonTester = new BasicJsonTester(getClass());

    /** l'URI des ressources REST Beneficiaires */
    protected URI uriResources;

    /** l'UriTemplate avec id variable pour les méthodes avec id technique du bénéficiaire */
    protected UriTemplate uriTemplateId;

    /** l'UriTemplate avec query variable pour les méthodes avec clés du benef dans l'URL */
    protected UriTemplate uriTemplateKeys;

    /** l'UriTemplate avec query variable pour les méthodes de recherche par matricule */
    protected UriTemplate uriTemplateNir;


    @BeforeAll
    static void prepare() {
        // géneration de beneficiaires avec des nir uniques en fonction de la date et de l'heure courante
        LocalDateTime dateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMddHHmmss");
        String uniqueNir1 = "1" + dateTime.format(formatter);
        String uniqueNir2 = "2" + uniqueNir1.substring(1);
        BENEF_NIR1_1 =
                new BeneficiaireResource().matricule(uniqueNir1).dateNaissance(dateTime.toLocalDate()).rang(1).nom("BEN").prenom("ef1_1");
        BENEF_NIR1_2 =
                new BeneficiaireResource().matricule(uniqueNir1).dateNaissance(dateTime.toLocalDate()).rang(2).nom("BEN").prenom("ef1_2");
        BENEF_NIR1_3 =
                new BeneficiaireResource().matricule(uniqueNir1).dateNaissance(dateTime.toLocalDate()).rang(3).nom("BEN").prenom("ef1_3");
        BENEF_NIR2 =
                new BeneficiaireResource().matricule(uniqueNir2).dateNaissance(dateTime.toLocalDate()).rang(1).nom("BEN").prenom("ef2");
    }

    @AfterAll
    void cleanUp() {
        // suppression de tous les benef de notre test restant dans la base
        List<String> nirs = Arrays.asList(BENEF_NIR1_1.getMatricule(), BENEF_NIR2.getMatricule());
        nirs.stream()
                .flatMap(nir -> findAllBeneficiaires(nir).stream())
                .forEach(b -> deleteBeneficiaire(b.getId()));
    }

    @Test
    @Order(1)
    void whenCreateNonExitantBenef_then201() {
        // Appel de l'API REST
        BeneficiaireResource benef = BENEF_NIR1_1;
        RequestEntity<BeneficiaireResource> request = RequestEntity
                .post(uriResources)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HEADER_V3, AUTHAPP_V3_CERT1)
                .body(benef);
        ResponseEntity<String> response = restTemplate.exchange(request, String.class);

        // Assertions
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).satisfies(hasBeneficiaire(benef));
    }

    /**
     * Maintenant qu'on a vérifié que la méthode create fonctionne On peut insérer le reste des nir utiles pour nos tests
     */
    @Test
    @Order(2)
    void setUpBenef() {
        // init de la base avec BENEF_NIR1_1
        BeneficiaireResource createdBenef = createBeneficiaire(BENEF_NIR1_2);
        assertThat(createdBenef).usingRecursiveComparison().ignoringFields("id").isEqualTo(BENEF_NIR1_2);
    }

    @Test
    @Order(3)
    void givenNoToken_whenRead_then401() {
        // Appel de l'API REST
        RequestEntity<Void> request = RequestEntity
                .get(uriTemplateKeys.expand(BENEF_NIR1_1.getMatricule(), BENEF_NIR1_1.getDateNaissance(), BENEF_NIR1_1.getRang()))
                .build();
        ResponseEntity<String> response = restTemplate.exchange(request, String.class);

        // Assertions
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).satisfies(hasProblem("stump_4010", false, "être authentifié"));
    }

    @Test
    @Order(4)
    void givenInvalidToken_whenRead_then401() {
        // Appel de l'API REST
        RequestEntity<Void> request = RequestEntity
                .get(uriTemplateKeys.expand(BENEF_NIR1_1.getMatricule(), BENEF_NIR1_1.getDateNaissance(), BENEF_NIR1_1.getRang()))
                .header(HEADER_V3, AUTHAPP_V3_CERT2 + "xxxx")
                .build();
        ResponseEntity<String> response = restTemplate.exchange(request, String.class);

        // Assertions
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).satisfies(hasProblem("stump_4010", false, "être authentifié"));
    }

    @Test
    @Order(5)
    void whenCreateWith2MissingFields_then400with1Pb2Violations() {

        BeneficiaireResource benefInvalide = copyOf(BENEF_NIR1_1).nom(null).prenom("");
        // Appel de l'API REST
        RequestEntity<BeneficiaireResource> request = RequestEntity
                .post(uriResources)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HEADER_V3, AUTHAPP_V3_CERT1)
                .body(benefInvalide);
        ResponseEntity<String> response = restTemplate.exchange(request, String.class);

        // Assertions
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).satisfies(hasProblem("stump_4000", false, "pas valides"))
                .satisfies(hasViolations("beneficiaireResource.nom", false, "pas être nul"))
                .satisfies(hasViolations("beneficiaireResource.prenom", false, "entre 1 et 25"));
    }

    @Test
    @Order(6)
    void whenCreateWithRangIsZero_then400_with1Pb1Violation() {

        BeneficiaireResource benefInvalide = copyOf(BENEF_NIR1_1).rang(0);
        // Appel de l'API REST
        RequestEntity<BeneficiaireResource> request = RequestEntity
                .post(uriResources)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HEADER_V3, AUTHAPP_V3_CERT1)
                .body(benefInvalide);
        ResponseEntity<String> response = restTemplate.exchange(request, String.class);

        // Assertions
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).satisfies(hasProblem("stump_4000", false, "pas valides"))
                .satisfies(hasViolations("beneficiaireResource.rang", false, "supérieur ou égal à 1"));
    }

    @Test
    @Order(7)
    void givenBenefAlreadyExists_whenCreate_then409_with1Pb() {

        BeneficiaireResource benefExistant = copyOf(BENEF_NIR1_1).nom("xxx").prenom("yyy");

        // Appel de l'API REST
        RequestEntity<BeneficiaireResource> request = RequestEntity
                .post(uriResources)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HEADER_V3, AUTHAPP_V3_CERT1)
                .body(benefExistant);
        ResponseEntity<String> response = restTemplate.exchange(request, String.class);

        // Assertions
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody())
                .satisfies(hasProblem("stump_4090", false, "existe déjà"));
    }

    @Test
    @Order(8)
    void given2BenefWithSameNir_whenCreateWithSameNir_then200_andGetByNirGets3Benef() {
        // Appel de l'API REST
        RequestEntity<BeneficiaireResource> request = RequestEntity
                .post(uriResources)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HEADER_V3, AUTHAPP_V3_CERT1)
                .body(BENEF_NIR1_3);
        ResponseEntity<String> response = restTemplate.exchange(request, String.class);

        // Assertions
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).satisfies(hasBeneficiaire(BENEF_NIR1_3));

        // Appel de l'API REST pour récupérer maintenant 3 beneficiaires
        RequestEntity<Void> request2 = RequestEntity
                .get(uriTemplateNir.expand(BENEF_NIR1_3.getMatricule()))
                .header(HEADER_V3, AUTHAPP_V3_CERT1)
                .build();
        response = restTemplate.exchange(request2, String.class);

        // Assertions
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).satisfies(hasBeneficiaireList(BENEF_NIR1_1, BENEF_NIR1_2, BENEF_NIR1_3));
    }

    @Test
    @Order(9)
    void whenReadExistantBenef_then200() {
        // Récupération de l'id du BENEF_NIR1_1
        RequestEntity<Void> request = RequestEntity
                .get(uriTemplateKeys.expand(BENEF_NIR1_1.getMatricule(), BENEF_NIR1_1.getDateNaissance(), BENEF_NIR1_1.getRang()))
                .header(HEADER_V3, AUTHAPP_V3_CERT1)
                .build();
        ResponseEntity<String> response = restTemplate.exchange(request, String.class);
        Integer id = JsonPath.read(response.getBody(), "$.value[0].id");

        // Assertions
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).satisfies(hasBeneficiaireList(BENEF_NIR1_1));

        // Read d'un Bénéficiare depuis son id
        // Appel de l'API REST
        request = RequestEntity
                .get(uriTemplateId.expand(id))
                .header(HEADER_V3, AUTHAPP_V3_CERT1)
                .build();
        response = restTemplate.exchange(request, String.class);

        // Assertions
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).satisfies(hasBeneficiaire(BENEF_NIR1_1));
    }

    @Test
    @Order(10)
    void whenReadNonExistantBenef_then404() {
        // Appel de l'API REST
        RequestEntity<Void> request = RequestEntity
                .get(uriTemplateId.expand(Long.MAX_VALUE))
                .header(HEADER_V3, AUTHAPP_V3_CERT1)
                .build();
        ResponseEntity<String> response = restTemplate.exchange(request, String.class);

        // Assertions
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).satisfies(hasProblem("stump_4040", false, "n'a pu être trouvé"));
    }

    @Test
    @Order(11)
    void whenReadWithIdNull_then400() {
        // Appel de l'API REST
        RequestEntity<Void> request = RequestEntity
                .get(uriTemplateId.expand("null"))
                .header(HEADER_V3, AUTHAPP_V3_CERT1)
                .build();
        ResponseEntity<String> response = restTemplate.exchange(request, String.class);

        // Assertions
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).satisfies(hasProblem("stump_4002", false, "pas au format attendu"));
    }

    @Test
    @Order(12)
    void whenFindOneExistantBenef_then200() {
        // Appel de l'API REST
        RequestEntity<Void> request = RequestEntity
                .get(uriTemplateKeys.expand(BENEF_NIR1_1.getMatricule(), BENEF_NIR1_1.getDateNaissance(), BENEF_NIR1_1.getRang()))
                .header(HEADER_V3, AUTHAPP_V3_CERT1)
                .build();
        ResponseEntity<String> response = restTemplate.exchange(request, String.class);

        // Assertions
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).satisfies(hasBeneficiaireList(BENEF_NIR1_1));
    }

    @Test
    @Order(13)
    void whenFindOneNonExistantBenef_then200_withEmptyList() {
        // Appel de l'API REST
        RequestEntity<Void> request = RequestEntity
                .get(uriTemplateKeys.expand(BENEF_NIR2.getMatricule(), BENEF_NIR2.getDateNaissance(),
                        BENEF_NIR2.getRang()))
                .header(HEADER_V3, AUTHAPP_V3_CERT1)
                .build();
        ResponseEntity<String> response = restTemplate.exchange(request, String.class);

        // Assertions
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).satisfies(hasBeneficiaireList(new BeneficiaireResource[] {}));
    }

    @Test
    @Order(14)
    void whenFindOneWithNirTooLong_then400_with1Pb1Violation() {
        // Appel de l'API REST
        RequestEntity<Void> request = RequestEntity
                .get(uriTemplateKeys.expand(BENEF_NIR1_1.getMatricule() + "99", BENEF_NIR1_1.getDateNaissance(),
                        BENEF_NIR1_1.getRang() + 10))
                .header(HEADER_V3, AUTHAPP_V3_CERT1)
                .build();
        ResponseEntity<String> response = restTemplate.exchange(request, String.class);

        // Assertions
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).satisfies(hasProblem("stump_4000", false, "pas valide"))
                .satisfies(hasViolations("find.matricule", false, "entre 13 et 13"));
    }

    @Test
    @Order(15)
    void whenFindOneWithWrongDateFormat_then400_with1Pb() {
        // Appel de l'API REST
        RequestEntity<Void> request = RequestEntity
                .get(uriTemplateKeys.expand(BENEF_NIR1_1.getMatricule(), "19990131", BENEF_NIR1_1.getRang()))
                .header(HEADER_V3, AUTHAPP_V3_CERT1)
                .build();
        ResponseEntity<String> response = restTemplate.exchange(request, String.class);

        // Assertions
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).satisfies(hasProblem("stump_4002", false, "pas au format attendu"));
    }

    @Test
    @Order(16)
    void whenFindOneWithRangWrongFormat_then400_with1Pb() {
        // Appel de l'API REST
        RequestEntity<Void> request = RequestEntity
                .get(uriTemplateKeys.expand(BENEF_NIR1_1.getMatricule(), BENEF_NIR1_1.getDateNaissance(), "XX"))
                .header(HEADER_V3, AUTHAPP_V3_CERT1)
                .build();
        ResponseEntity<String> response = restTemplate.exchange(request, String.class);

        // Assertions
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).satisfies(hasProblem("stump_4002", false, "pas au format attendu"));
    }

    @Test
    @Order(16)
    void whenFindWithOnly2KeysCase1_then400_with1Pb() throws URISyntaxException {
        // Appel de l'API REST
        RequestEntity<Void> request = RequestEntity
                .get(new URI(uriResources.toString() + "?matricule=1234567890123&rang=2"))
                .header(HEADER_V3, AUTHAPP_V3_CERT1)
                .build();
        ResponseEntity<String> response = restTemplate.exchange(request, String.class);

        // Assertions
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).satisfies(hasProblem("stump_4000", false, "pas valides"));
    }

    @Test
    @Order(16)
    void whenFindWithOnly2KeysCase2_then400_with1Pb() throws URISyntaxException {
        // Appel de l'API REST
        RequestEntity<Void> request = RequestEntity
                .get(new URI(uriResources.toString() + "?matricule=1234567890123&date-naissance=1999-12-31"))
                .header(HEADER_V3, AUTHAPP_V3_CERT1)
                .build();
        ResponseEntity<String> response = restTemplate.exchange(request, String.class);

        // Assertions
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).satisfies(hasProblem("stump_4000", false, "pas valides"));
    }

    @Test
    @Order(17)
    void given2BenefsWithSameNir_whenFindAll_then200_with2Benefs() {
        // Appel de l'API REST
        RequestEntity<Void> request = RequestEntity
                .get(uriTemplateNir.expand(BENEF_NIR1_1.getMatricule()))
                .header(HEADER_V3, AUTHAPP_V3_CERT1)
                .build();
        ResponseEntity<String> response = restTemplate.exchange(request, String.class);

        // Assertions
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).satisfies(hasBeneficiaireList(BENEF_NIR1_1, BENEF_NIR1_2, BENEF_NIR1_3));
    }

    @Test
    @Order(18)
    void givenNirDoesntMatchAnyBenef_whenFindAll_then200_withEmptyListInBody() {
        // Appel de l'API REST
        RequestEntity<Void> request = RequestEntity
                .get(uriTemplateNir.expand("2991297999999"))
                .header(HEADER_V3, AUTHAPP_V3_CERT1)
                .build();
        ResponseEntity<String> response = restTemplate.exchange(request, String.class);

        // Assertions
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).satisfies(hasBeneficiaireList(new BeneficiaireResource[] {}));
    }

    @Test
    @Order(19)
    void givenNirTooShort_whenFindAll_then400_with1Pb1Violation1() {
        // Appel de l'API REST
        RequestEntity<Void> request = RequestEntity
                .get(uriTemplateNir.expand(BENEF_NIR1_1.getMatricule().substring(2)))
                .header(HEADER_V3, AUTHAPP_V3_CERT1)
                .build();
        ResponseEntity<String> response = restTemplate.exchange(request, String.class);

        // Assertions
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).satisfies(hasProblem("stump_4000", false, "pas valide"))
                .satisfies(hasViolations("find.matricule", false, "entre 13 et 13"));
    }

    @Test
    @Order(20)
    void givenEmptyNir_whenFindAll_then400_with1Pb2Violations() {
        // Appel de l'API REST
        RequestEntity<Void> request = RequestEntity
                .get(uriTemplateNir.expand(""))
                .header(HEADER_V3, AUTHAPP_V3_CERT1)
                .build();
        ResponseEntity<String> response = restTemplate.exchange(request, String.class);

        // Assertions
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).satisfies(hasProblem("stump_4000", false, "pas valide"))
                .satisfies(hasViolations("find.matricule", false, "entre 13 et 13"));
    }

    @Test
    @Order(21)
    void whenUpdateByIdExistantBenef_then200_withBody() {
        // Appel de l'API REST pour récupérer l'id du BENEF_NIR1_2
        RequestEntity<Void> request = RequestEntity
                .get(uriTemplateKeys.expand(BENEF_NIR1_2.getMatricule(), BENEF_NIR1_2.getDateNaissance(), BENEF_NIR1_2.getRang()))
                .header(HEADER_V3, AUTHAPP_V3_CERT1)
                .build();
        ResponseEntity<String> response = restTemplate.exchange(request, String.class);
        Integer id = JsonPath.read(response.getBody(), "$.value[0].id");

        // on change tous les champs, pas seulement nom et prénom
        BeneficiaireResource benefUpdate = new BeneficiaireResource()
                .matricule("1234567890123").dateNaissance(LocalDate.now().minusYears(10)).rang(7)
                .nom("Alastäir").prenom("Rêynolds");

        // Appel de l'API REST
        RequestEntity<BeneficiaireResource> requestUpdate = RequestEntity
                .put(uriTemplateId.expand(id))
                .contentType(MediaType.APPLICATION_JSON)
                .header(HEADER_V3, AUTHAPP_V3_CERT1)
                .body(benefUpdate);
        response = restTemplate.exchange(requestUpdate, String.class);

        // Assertions
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).satisfies(hasBeneficiaire(benefUpdate));
    }

    @Test
    @Order(22)
    void whenUpdateByIdInexistantBenef_then404() {

        BeneficiaireResource benefInexistant = copyOf(BENEF_NIR1_3)
                .matricule("1234567890123");

        // Appel de l'API REST
        RequestEntity<BeneficiaireResource> request = RequestEntity
                .put(uriTemplateId.expand(Long.MAX_VALUE))
                .contentType(MediaType.APPLICATION_JSON)
                .header(HEADER_V3, AUTHAPP_V3_CERT1)
                .body(benefInexistant);
        ResponseEntity<String> response = restTemplate.exchange(request, String.class);

        // Assertions
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).satisfies(hasProblem("stump_4040", false, "n'a pu être trouvé"));
    }

    @Test
    @Order(23)
    void whenUpdateByIdWith3MissingFields_then400_with1Pb3Violations() {
        // Appel de l'API REST pour récupérer l'id du BENEF_NIR1_1
        RequestEntity<Void> request = RequestEntity
                .get(uriTemplateKeys.expand(BENEF_NIR1_1.getMatricule(), BENEF_NIR1_1.getDateNaissance(), BENEF_NIR1_1.getRang()))
                .build();
        ResponseEntity<String> response = restTemplate.exchange(request, String.class);

        BeneficiaireResource benefUpdate = copyOf(BENEF_NIR1_1)
                .matricule("xxx")
                .dateNaissance(null)
                .rang(0);

        // Appel de l'API REST
        RequestEntity<BeneficiaireResource> requestUpdate = RequestEntity
                .put(uriResources)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HEADER_V3, AUTHAPP_V3_CERT1)
                .body(benefUpdate);
        response = restTemplate.exchange(requestUpdate, String.class);

        // Assertions
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).satisfies(hasProblem("stump_4000", false, "pas valide"))
                .satisfies(hasViolations("beneficiaireResource.matricule", false, "entre 13 et 13"))
                .satisfies(hasViolations("beneficiaireResource.dateNaissance", false, "pas être nul"))
                .satisfies(hasViolations("beneficiaireResource.rang", false, "supérieur ou égal à 1"));
    }

    @Test
    @Order(24)
    void whenUpdateByKeysExistantBenef_then200_withBody() {

        BeneficiaireResource benefUpdate = copyOf(BENEF_NIR1_1)
                .nom("Nmodified")
                .prenom("Pmodified");

        // Appel de l'API REST
        RequestEntity<BeneficiaireResource> request = RequestEntity
                .put(uriResources)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HEADER_V3, AUTHAPP_V3_CERT1)
                .body(benefUpdate);
        ResponseEntity<String> response = restTemplate.exchange(request, String.class);

        // Assertions
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).satisfies(hasBeneficiaire(benefUpdate));
    }

    @Test
    @Order(25)
    void whenUpdateByKeysInexistantBenef_then404_with1Pb() {

        BeneficiaireResource benefInexistant = copyOf(BENEF_NIR1_3)
                .matricule("1234567890123");

        // Appel de l'API REST
        RequestEntity<BeneficiaireResource> request = RequestEntity
                .put(uriResources)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HEADER_V3, AUTHAPP_V3_CERT1)
                .body(benefInexistant);
        ResponseEntity<String> response = restTemplate.exchange(request, String.class);

        // Assertions
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).satisfies(hasProblem("stump_4040", false, "n'a pu être trouvé"));
    }

    @Test
    @Order(26)
    void whenUpdateByKeysWithoutNom_then400_with1Pb1Violations() {

        BeneficiaireResource benefUpdate = copyOf(BENEF_NIR2)
                .prenom("xxx")
                .nom(null);

        // Appel de l'API REST
        RequestEntity<BeneficiaireResource> request = RequestEntity
                .put(uriResources)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HEADER_V3, AUTHAPP_V3_CERT1)
                .body(benefUpdate);
        ResponseEntity<String> response = restTemplate.exchange(request, String.class);

        // Assertions
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).satisfies(hasProblem("stump_4000", false, "pas valide"))
                .satisfies(hasViolations("beneficiaireResource.nom", false, "pas être nul"));
    }

    @Test
    @Order(27)
    void whenUpdateByKeysWithoutPrenom_then400_with1Pb1Violation() {

        BeneficiaireResource benefUpdate = copyOf(BENEF_NIR2)
                .prenom(null)
                .nom("xxx");

        // Appel de l'API REST
        RequestEntity<BeneficiaireResource> request = RequestEntity
                .put(uriResources)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HEADER_V3, AUTHAPP_V3_CERT1)
                .body(benefUpdate);
        ResponseEntity<String> response = restTemplate.exchange(request, String.class);

        // Assertions
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).satisfies(hasProblem("stump_4000", false, "pas valide"))
                .satisfies(hasViolations("beneficiaireResource.prenom", false, "pas être nul"));
    }

    @Test
    @Order(28)
    void whenDeleteByIdExistantBenef_then204_andNextFindEmpty() {

        // Appel de l'API REST pour récupérer l'id du BENEF_NIR1_3
        RequestEntity<Void> request = RequestEntity
                .get(uriTemplateKeys.expand(BENEF_NIR1_3.getMatricule(), BENEF_NIR1_3.getDateNaissance(), BENEF_NIR1_3.getRang()))
                .header(HEADER_V3, AUTHAPP_V3_CERT1)
                .build();
        ResponseEntity<String> response = restTemplate.exchange(request, String.class);
        Integer returnedBenefId = JsonPath.read(response.getBody(), "$.value[0].id");

        // Appel de l'API REST
        request = RequestEntity
                .delete(uriTemplateId.expand(returnedBenefId))
                .header(HEADER_V3, AUTHAPP_V3_CERT1)
                .build();
        response = restTemplate.exchange(request, String.class);

        // Assertions
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Appel de l'API REST pour tenter de lire après suppression
        request = RequestEntity
                .get(uriTemplateKeys.expand(BENEF_NIR1_3.getMatricule(), BENEF_NIR1_3.getDateNaissance(), BENEF_NIR1_3.getRang()))
                .header(HEADER_V3, AUTHAPP_V3_CERT1)
                .build();
        response = restTemplate.exchange(request, String.class);

        // Assertions
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).satisfies(hasBeneficiaireList(new BeneficiaireResource[] {}));
    }

    @Test
    @Order(29)
    void whenDeleteByIdNonExistantBenef_then404() {
        // Appel de l'API REST
        RequestEntity<Void> request = RequestEntity
                .delete(uriTemplateId.expand(Long.MAX_VALUE))
                .header(HEADER_V3, AUTHAPP_V3_CERT1)
                .build();
        ResponseEntity<String> response = restTemplate.exchange(request, String.class);

        // Assertions
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).satisfies(hasProblem("stump_4040", false, "n'a pu être trouvé"));
    }

    @Test
    @Order(30)
    void whenDeleteByIdWithIdNull_then400_with1Pb() {
        // Appel de l'API REST
        RequestEntity<Void> request = RequestEntity
                .delete(uriTemplateId.expand("null"))
                .header(HEADER_V3, AUTHAPP_V3_CERT1)
                .build();
        ResponseEntity<String> response = restTemplate.exchange(request, String.class);

        // Assertions
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).satisfies(hasProblem("stump_4002", false, "pas au format attendu"));
    }

    // ~ methodes utilitaires
    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Retourne un {@link Consumer} de String vérifiant que la chaine fournie correspond à une String JSON dont la property 'value' contient
     * un {@link BeneficiaireResource} identique à celui donné dans <code>expectedBenef</code> et contenant une property <code>id</code> non
     * vide.
     */
    protected Consumer<String> hasBeneficiaire(BeneficiaireResource expectedBenef) {

        return str -> {
            BasicJsonTester jsonTester = new BasicJsonTester(getClass());
            JsonContent<Object> body = jsonTester.from(str);
            assertThat(body).extractingJsonPathNumberValue("$.value.id").isNotNull();
            assertThat(body).extractingJsonPathStringValue("$.value.matricule").isEqualTo(expectedBenef.getMatricule());
            assertThat(body).extractingJsonPathStringValue("$.value.date-naissance").isEqualTo(expectedBenef.getDateNaissance().toString());
            assertThat(body).extractingJsonPathNumberValue("$.value.rang").isEqualTo(expectedBenef.getRang());
            assertThat(body).extractingJsonPathStringValue("$.value.nom").isEqualTo(expectedBenef.getNom());
            assertThat(body).extractingJsonPathStringValue("$.value.prenom").isEqualTo(expectedBenef.getPrenom());
        };
    }

    /**
     * Retourne un {@link Consumer} de String vérifiant que la chaine fournie correspond à une String JSON dont la property 'value' contient
     * un tableau de {@link BeneficiaireResource}s identiques à ceux donnés dans <code>expectedBenefs</code> et contenant une property
     * <code>id</code> non vide. Si le tableau <code>expectedBenefs</code> est null ou vide, on vérifie que le tableau JSON contetu dans
     * value est vide.
     */
    protected Consumer<String> hasBeneficiaireList(BeneficiaireResource... expectedBenefs) {

        return str -> {
            JsonContent<Object> json = jsonTester.from(str);
            if (expectedBenefs == null || expectedBenefs.length == 0) {
                assertThat(json).extractingJsonPathArrayValue("$.value").isEmpty();
            }
            for (int i = 0; i < expectedBenefs.length; i++) {
                assertThat(json).extractingJsonPathNumberValue("$.value[%d].id", i).isNotNull();
                assertThat(json).extractingJsonPathStringValue("$.value[%d].matricule", i).isEqualTo(expectedBenefs[i].getMatricule());
                assertThat(json).extractingJsonPathStringValue("$.value[%d].date-naissance", i)
                        .isEqualTo(expectedBenefs[i].getDateNaissance().toString());
                assertThat(json).extractingJsonPathNumberValue("$.value[%d].rang", i).isEqualTo(expectedBenefs[i].getRang());
                assertThat(json).extractingJsonPathStringValue("$.value[%d].nom", i).isEqualTo(expectedBenefs[i].getNom());
                assertThat(json).extractingJsonPathStringValue("$.value[%d].prenom", i).isEqualTo(expectedBenefs[i].getPrenom());
            }
        };
    }

    /**
     * Retourne un {@link Consumer} de String vérifiant que la chaine fournie correspond à une String JSON dont la property 'problems'
     * contient un tableau de {@link Problem}s de taille 1 et contenant 1 Problem contenant 1 <code>id</code> non vide, un <code>code</code>
     * et un <code>message</code> correpondant aux paramètres de même nom.
     * <p>
     * Si le paramètre strictOnMessage est à true on vérifie la correspondance exacte du message, sinon on vérifie que la valeur réelle
     * contient celle passée en paramètre.
     */
    protected Consumer<String> hasProblem(String code, boolean strictOnMessage, String message) {
        return str -> {
            JsonContent<Object> json = jsonTester.from(str);
            assertThat(json).extractingJsonPathArrayValue("$.problems").hasSize(1);
            assertThat(json).extractingJsonPathStringValue("$.problems[0].id").isNotEmpty();
            assertThat(json).extractingJsonPathStringValue("$.problems[0].code").isEqualTo(code);
            if (strictOnMessage) {
                assertThat(json).extractingJsonPathStringValue("$.problems[0].message").isEqualTo(message);
            } else {
                assertThat(json).extractingJsonPathStringValue("$.problems[0].message").contains(message);
            }
        };
    }

    protected Consumer<String> hasViolations(String fieldPath, boolean strictOnMessage, Object... messages) {
        return str -> {
            JsonContent<Object> json = jsonTester.from(str);
            if (strictOnMessage) {
                assertThat(json).extractingJsonPathArrayValue("$..details.violations..['%s']", fieldPath)
                        .contains(messages);
            } else {
                Arrays.stream(messages).forEach(expected -> {
                    assertThat(json).extractingJsonPathArrayValue("$..details.violations..['%s']", fieldPath)
                            .anyMatch(actual -> actual.toString().contains(expected.toString()));
                });
            }
        };
    }

    /**
     * Méthode utilisée au setUp pour créer un bénéficiaire en BDD
     *
     * @param benef Le BeneficiaireResource à créer
     * @return le bénéficiaire si ok sinon lève une IllegalStateException
     */
    protected BeneficiaireResource createBeneficiaire(BeneficiaireResource benef) {
        URI uri = UriComponentsBuilder.fromHttpUrl(uriResources.toString()).build().toUri();

        // Authentification
        HttpHeaders headers = new HttpHeaders();
        headers.set(HEADER_V3, AUTHAPP_V3_CERT1);

        ResponseEntity<ValueResponse<BeneficiaireResource>> response =
                restTemplate.exchange(uri, HttpMethod.POST, new HttpEntity<BeneficiaireResource>(benef, headers),
                        new ParameterizedTypeReference<ValueResponse<BeneficiaireResource>>() {});

        return Optional.ofNullable(response.getBody()).map(ValueResponse::getValue).orElseThrow(IllegalStateException::new);
    }

    /**
     * Méthode utilisée au cleanUp pour récupérer la liste des bénéficiaires créés durant les tests
     *
     * @param matricule Le matricule des bénéficiaires à récupérer
     * @return la liste des bénéficiaire correspondant au matricule donné
     */
    protected List<BeneficiaireResource> findAllBeneficiaires(String matricule) {
        URI uri = UriComponentsBuilder.fromHttpUrl(uriResources.toString()).query("matricule={m}")
                .buildAndExpand(matricule)
                .toUri();

        ResponseEntity<ValueResponse<List<BeneficiaireResource>>> response =
                restTemplate.exchange(uri, HttpMethod.GET, null,
                        new ParameterizedTypeReference<ValueResponse<List<BeneficiaireResource>>>() {});

        return Optional.ofNullable(response.getBody()).map(ValueResponse::getValue).orElse(Collections.emptyList());
    }

    /**
     * Méthode utilisée au cleanUp pour supprimer les bénéficiaires créés durant les tests
     *
     * @param id L'id du bénéficiaire à supprimer
     */
    protected void deleteBeneficiaire(Long id) {
        URI uri = UriComponentsBuilder.fromHttpUrl(uriResources.toString()).path("/{id}")
                .buildAndExpand(id)
                .toUri();

        restTemplate.delete(uri);
    }

    protected BeneficiaireResource copyOf(BeneficiaireResource source) {
        return new BeneficiaireResource()
                .matricule(source.getMatricule())
                .dateNaissance(source.getDateNaissance())
                .rang(source.getRang())
                .nom(source.getNom())
                .prenom(source.getPrenom());
    }
}