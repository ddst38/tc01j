package fr.cnam.toni.usecase.provider.rest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import fr.cnam.toni.usecase.business.model.Beneficiaire;
import fr.cnam.toni.usecase.business.spi.BeneficiaireRepository;


@Repository
public class RestBeneficiaireRepository  implements BeneficiaireRepository {

    private RestTemplate restTemplate;

    @Value("${usecase.provider.rest.url}")
    private String urlProviderRest;
    
	public RestBeneficiaireRepository(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
    }

	@Override
	public Beneficiaire createBeneficiaire(Beneficiaire beneficiaire) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<Beneficiaire> findOneBeneficiaire(String matricule, LocalDate dateNaissance, int rang) {
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public Optional<Beneficiaire> readBeneficiaire(Long id) {
		Beneficiaire beneficiaire = restTemplate.getForObject(
				urlProviderRest, Beneficiaire.class);
		return Optional.of(beneficiaire);
	}

	@Override
	public List<Beneficiaire> findAllBeneficiaires(String matricule) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Beneficiaire updateBeneficiaire(Beneficiaire beneficiaire) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Beneficiaire updateBeneficiaire(Long id, Beneficiaire beneficiaire) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean deleteBeneficiaire(Long id) {
		// TODO Auto-generated method stub
		return false;
	}

}
	