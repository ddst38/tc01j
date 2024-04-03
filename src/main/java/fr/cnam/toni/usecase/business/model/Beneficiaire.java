package fr.cnam.toni.usecase.business.model;

import java.time.LocalDate;
import java.util.Objects;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Modèle métier Beneficiaire.
 * <p>
 * Le matricule, la date de naissance et le rang servent d'identifiants métier d'un bénéficiaire.
 *
 * @author CNAM DDSI DAMSI
 */
public class Beneficiaire {

    public static final int MATRICULE_SIZE = 13;
    public static final int RANG_MIN_VALUE = 1;
    public static final int NOM_MIN_SIZE = 1;
    public static final int NOM_MAX_SIZE = 25;
    public static final int PRENOM_MIN_SIZE = 1;
    public static final int PRENOM_MAX_SIZE = 25;

    private Long id;

    @NotEmpty
    @Size(min = MATRICULE_SIZE, max = MATRICULE_SIZE)
    private String matricule;

    @NotNull
    private LocalDate dateNaissance;

    @NotNull
    @Min(value = RANG_MIN_VALUE)
    private Integer rang;

    @NotEmpty
    @Size(min = NOM_MIN_SIZE, max = NOM_MAX_SIZE)
    private String nom;

    @NotEmpty
    @Size(min = PRENOM_MIN_SIZE, max = PRENOM_MAX_SIZE)
    private String prenom;

    /**
     * Constructeur par défaut nécessaire pour les frameworks de désérialisation.<br>
     * On le laisse en private car ne doit pas être utilisé dans le cade applicatif.
     */
    private Beneficiaire() {
        this(null, null, null, null, null, null);
    }

    /**
     * Constructeur avec toutes les propriétés d'un bénéficiaire
     */
    public Beneficiaire(Long id, String matricule, LocalDate dateNaissance, Integer rang, String nom, String prenom) {
        this.id = id;
        this.matricule = matricule;
        this.dateNaissance = dateNaissance;
        this.rang = rang;
        this.nom = nom;
        this.prenom = prenom;
    }

    /**
     * Constructeur par copie d'un bénéficiaire existant
     */
    public Beneficiaire(Beneficiaire beneficiaire) {
        super();
        id = beneficiaire.getId();
        matricule = beneficiaire.getMatricule();
        dateNaissance = beneficiaire.getDateNaissance();
        rang = beneficiaire.getRang();
        nom = beneficiaire.getNom();
        prenom = beneficiaire.getPrenom();
    }

    /**
     * Construit un bénéficiaire sans setter l'id.
     */
    public static Beneficiaire of(String matricule, LocalDate dateNaissance, int rang, String nom, String prenom) {
        return new Beneficiaire(null, matricule, dateNaissance, rang, nom, prenom);
    }

    /**
     * Retourne une instance de {@link Beneficiaire} identique à this mais en y ajoutant un Id
     */
    public Beneficiaire withId(Long id) {
        return new Beneficiaire(id, matricule, dateNaissance, rang, nom, prenom);
    }

    /**
     * Getter de l'id.
     *
     * @return l'id
     */
    public Long getId() {
        return id;
    }

    /**
     * Getter de la dateNaissance.
     *
     * @return La dateNaissance
     */
    public LocalDate getDateNaissance() {
        return dateNaissance;
    }

    /**
     * Getter du matricule.
     *
     * @return Le matricule
     */
    public String getMatricule() {
        return matricule;
    }

    /**
     * Getter du nom.
     *
     * @return Le nom
     */
    public String getNom() {
        return nom;
    }

    /**
     * Getter du prenom.
     *
     * @return Le prenom
     */
    public String getPrenom() {
        return prenom;
    }

    /**
     * Getter du rang.
     *
     * @return Le rang
     */
    public int getRang() {
        return rang;
    }

    /**
     * Set l'id du bénéficiaire
     *
     * @param id L'id à affecter
     */
    public void setId(final Long id) {
        this.id = id;
    }

    /**
     * Set la dateNaissance du bénéficiaire
     *
     * @param dateNaissance La dateNaissance à affecter
     */
    public void setDateNaissance(final LocalDate dateNaissance) {
        this.dateNaissance = dateNaissance;
    }

    /**
     * Set le matricule du bénéficiaire
     *
     * @param matricule Le matricule à affecter
     */
    public void setMatricule(final String matricule) {
        this.matricule = matricule;
    }

    /**
     * Set le nom du bénéficiaire
     *
     * @param nom Le nom à affecter
     */
    public void setNom(final String nom) {
        this.nom = nom;
    }

    /**
     * Set le prénom du bénéficiaire
     *
     * @param prenom Le prenom à affecter
     */
    public void setPrenom(final String prenom) {
        this.prenom = prenom;
    }

    /**
     * Set le rang du bénéficiaire
     *
     * @param rang Le rang a affecter
     */
    public void setRang(final int rang) {
        this.rang = rang;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Beneficiaire other = (Beneficiaire) obj;
        return Objects.equals(dateNaissance, other.dateNaissance) && Objects.equals(matricule, other.matricule) && rang == other.rang;
    }

    @Override
    public int hashCode() {
        return Objects.hash(dateNaissance, matricule, rang);
    }

    @Override
    public String toString() {
        return String.format("Beneficiaire[id=%d, matricule=%s, dateNaissance=%s, rang=%d, nom=%s, prenom=%s]",
                id, matricule, dateNaissance, rang, nom, prenom);
    }

}
