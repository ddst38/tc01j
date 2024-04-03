package fr.cnam.toni.usecase.controller.rest;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import fr.cnam.toni.usecase.business.model.Beneficiaire;
import fr.cnam.toni.usecase.controller.rest.model.BeneficiaireResource;

public class BeneficiaireResourceAdapter {

    /**
     * Fonction de transformation d'un {@link BeneficiaireResource} en {@link Beneficiaire}.
     */
    public Function<BeneficiaireResource, Beneficiaire> resToBiz() {
        return resource -> Optional.ofNullable(resource)
                .map(r -> new Beneficiaire(
                        r.getId(),
                        r.getMatricule(), r.getDateNaissance(), r.getRang(),
                        r.getNom(), r.getPrenom()))
                .orElse(null);
    }

    /**
     * Fonction de transformation d'un {@link Beneficiaire} en {@link BeneficiaireResource}.
     */
    public Function<Beneficiaire, BeneficiaireResource> bizToRes() {
        return bizObject -> Optional.ofNullable(bizObject)
                .map(bo -> new BeneficiaireResource(
                        bo.getMatricule(),bo.getDateNaissance(),bo.getRang(),bo.getNom(),bo.getPrenom()))
                .orElse(null);
    }

    /**
     * transformation d'un {@link BeneficiaireResource} en {@link Beneficiaire}.
     */
    public Beneficiaire toBiz(BeneficiaireResource resource) {
        return resToBiz().apply(resource);
    }

    /**
     * transformation d'un {@link Beneficiaire} en {@link BeneficiaireResource}.
     */
    public BeneficiaireResource toRes(Beneficiaire bizObject) {
        return bizToRes().apply(bizObject);
    }

    /**
     * transformation d'une liste de {@link Beneficiaire} en liste de {@link BeneficiaireResource}.
     */
    public List<BeneficiaireResource> toResList(List<Beneficiaire> bizList) {
        return bizList.stream().map(bizToRes()).collect(Collectors.toList());
    }
}
