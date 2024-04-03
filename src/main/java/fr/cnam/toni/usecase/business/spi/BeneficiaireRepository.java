package fr.cnam.toni.usecase.business.spi;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import fr.cnam.toni.usecase.business.model.Beneficiaire;

/**
 * Interface d'accès au Repository de {@link Beneficiaire}.
 *
 * Dans la mesure du possible, cette interface est définie dans la couche business, car c'est le métier qui définit comment il souhaite
 * s'adresser à sa couche de persistance. De cette façon il est possible de modifier la couche de persistance sans impacter la couche
 * métier.
 * <p>
 * Si on définit une interface c'est qu'on souhaite se donner la possibilité d'avoir plusieurs implémentations, ou de changer
 * d'implémentation, il faut donc une interface invariante, qui ne soit pas imposée par l'implémentation.
 *
 * @author CNAM DDSI DAMSI
 */
public interface BeneficiaireRepository {

    /**
     * Crée un bénéficiaire.
     *
     * @param beneficiaire L'entité {@link Beneficiaire} à persister. Si l'id est renseigné, il est ignoré
     * @return le {@link Beneficiaire} créé avec son id
     */
    Beneficiaire createBeneficiaire(Beneficiaire beneficiaire);

    /**
     * Recherche d'un bénéficiaire.
     *
     * @param matricule Le matricule du bénéficiaire
     * @param dateNaissance La date de Naissance du bénéficiaire
     * @param rang Le rang du bénéficiaire
     * @return le {@link Beneficiaire} trouvé avec son id wrappé dans un {@link Optional}, ou un Optional vide si non trouvé
     */
    Optional<Beneficiaire> findOneBeneficiaire(String matricule, LocalDate dateNaissance, int rang);

    /**
     * Lit un bénéficiaire.
     *
     * @param id l'identifiant du bénéficiaire
     * @return le {@link Beneficiaire} trouvé avec son id wrappé dans un {@link Optional}, ou un Optional vide si non trouvé
     */
    Optional<Beneficiaire> readBeneficiaire(Long id);

    /**
     * Recherche les bénéficiaires ayant un <code>matricule<code> donné..
     *
     * @param matricule Le matricule des bénéficiaires
     * @return la liste des {@link Beneficiaire} trouvés avec leurs id.
     */
    List<Beneficiaire> findAllBeneficiaires(String matricule);

    /**
     * Met à jours le nom et/ou le prénom d'un bénéficiaire identifié par son tuple matricule / dateNaissance / rang.
     * <p>
     * Le nom et le prénom du bénéficiaire sont mis à jour en base, avec les nouvelles valeurs fournies dans le paramètre
     * <code>beneficiaire</code>, pour celles qui sont non nulles.
     *
     * @param beneficiaire le {@link Beneficiaire} avec la nouvelle valeur du nom et/ou prénom. Si l'id est renseigné, il est ignoré
     * @return le {@link Beneficiaire} avec son id après modification
     */
    Beneficiaire updateBeneficiaire(Beneficiaire beneficiaire);

    /**
     * Met à jour les champs du bénéficiaire identifié par <code>id</code>, avec ceux du {@link Beneficiaire} <code>beneficiaire</code>
     * donné (hors id).
     * <p>
     * Cette méthode permet donc de remplacer un bénéficiaire par un autre.
     *
     * @param id l'identifiant du bénéficiaire à remplacer
     * @param beneficiaire le {@link Beneficiaire} qui remplace celui existant
     * @return le {@link Beneficiaire} avec son id après modification
     */
    Beneficiaire updateBeneficiaire(Long id, Beneficiaire beneficiaire);

    /**
     * Supprime un bénéficiaire.
     *
     * @param id l'identifiant du bénéficiaire
     * @return true si le bénéficiaire existait et a été supprimé, false s'il n'existait pas
     */
    boolean deleteBeneficiaire(Long id);

}
