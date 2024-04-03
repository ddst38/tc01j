package fr.cnam.toni.usecase.business.api;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.springframework.lang.NonNull;
import org.springframework.validation.annotation.Validated;

import fr.cnam.toni.usecase.business.model.Beneficiaire;

/**
 * Interface d'appel du service Beneficiaire.
 * <p>
 * Dans cette exemple on montre la possibilité de faire de la validation des paramètres d'entrée avec des contraintes de validation. Il est
 * préférable de les mettre ici en entrée de la couche métier, permettant de ne pas les répéter dans plusieurs controleurs dans le cas de
 * services multi-expositions.
 * <p>
 * Les annotations liées aux contraintes:<br>
 * - {@link Validated} pour activer la vérification des contraintes lors de l'appel de méthodes<br>
 * - toutes celles de jakarta.validation.constraints: {@link NotNull}, {@link NotEmpty}, {@link Size}...<br>
 * - {@link Valid} pour indiquer de valider en cascade les contraintes indiquées dans la classe cible, ou {@link Validated} si on veut
 * préciser un groupe de validation
 *
 * @author CNAM DDSI DAMSI
 */
@Validated
public interface BeneficiaireService {

    /**
     * Crée un {@link Beneficiaire} et retourne le {@link Beneficiaire} avec son id.
     *
     * @param beneficiaire le {@link Beneficiaire} à créer. Si l'id est renseigné, il est ignoré
     * @return le {@link Beneficiaire} créé avec son id. Ne peut pas être null
     */
    @NonNull
    Beneficiaire createBeneficiaire(
            @NotNull @Valid Beneficiaire beneficiaire);

    /**
     * Retourne le {@link Beneficiaire} correspondant à l'<code>id</code> donné.
     *
     * @param id l'id du {@link Beneficiaire} à lire
     * @return le {@link Beneficiaire} wrappé dans un Optional, vide si le bénéficiaire n'existe pas
     */
    Optional<Beneficiaire> readBeneficiaire(@NotNull Long id);

    /**
     * Retourne le {@link Beneficiaire} correspondant au tuple <code>matricule</code> / <code>dateNaissance</code> / <code>rang</code>.
     *
     * @param matricule Le matricule du bénéficiaire
     * @param dateNaissance La date de naissance du bénéficiare
     * @param rang Le rang du bénéficiaire
     * @return le {@link Beneficiaire} wrappé dans un Optional, vide si le bénéficiaire n'existe pas
     */
    Optional<Beneficiaire> findOneBeneficiaire(
            @NotEmpty @Size(min = Beneficiaire.MATRICULE_SIZE, max = Beneficiaire.MATRICULE_SIZE) String matricule,
            @NotNull LocalDate dateNaissance,
            @Min(value = Beneficiaire.RANG_MIN_VALUE) int rang);

    /**
     * Retourne les {@link Beneficiaire}s correspondant au <code>matricule</code>.
     *
     * @param matricule Le matricule servant à la recherche
     * @return la liste des bénéficiaires trouvés - vide si aucun n'est trouvé
     */
    List<Beneficiaire> findAllBeneficiaires(
            @NotEmpty @Size(min = Beneficiaire.MATRICULE_SIZE, max = Beneficiaire.MATRICULE_SIZE) String matricule);

    /**
     * Modifie le {@link Beneficiaire} identifié par son tuple matricule / dateNaissance / rang. Seuls les noms et/ou prénoms peuvent donc
     * être modifiés.
     *
     * @param beneficiaire le {@link Beneficiaire} à modifier. Si l'id est renseigné, il est ignoré
     * @return le {@link Beneficiaire} mis à jour avec son id. Ne peut pas être null.
     */
    @NonNull
    Beneficiaire updateBeneficiaire(
            @NotNull @Valid Beneficiaire beneficiaire);

    /**
     * Modifie le {@link Beneficiaire} identifié par le paramètre <code>id</code>. On peut donc remplacer un bénéficiaire par un autre.
     *
     * @param id l'id du {@link Beneficiaire} à modifier
     * @param beneficiaire le {@link Beneficiaire} à modifier. Si l'id est fourni, il est ignoré
     * @return le {@link Beneficiaire} mis à jour avec son id. Ne peut pas être null.
     */
    @NonNull
    Beneficiaire updateBeneficiaire(
            @NotNull Long id,
            @NotNull @Valid Beneficiaire beneficiaire);

    /**
     * Supprime le {@link Beneficiaire} correspondant à l'<code>id</code> donné.
     *
     * @param id l'id du {@link Beneficiaire} à supprimer
     * @return true si le bénéficiaire a été trouvé et supprimé, false si non trouvé
     */
    boolean deleteBeneficiaire(@NotNull Long id);

}
