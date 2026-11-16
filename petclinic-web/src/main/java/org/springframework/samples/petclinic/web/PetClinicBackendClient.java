package org.springframework.samples.petclinic.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.web.PetClinicModels.OwnerDto;
import org.springframework.samples.petclinic.web.PetClinicModels.OwnerRequest;
import org.springframework.samples.petclinic.web.PetClinicModels.VetDto;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Service
public class PetClinicBackendClient {

    private static final ParameterizedTypeReference<List<OwnerDto>> OWNER_LIST = new ParameterizedTypeReference<>() {
    };
    private static final ParameterizedTypeReference<List<VetDto>> VET_LIST = new ParameterizedTypeReference<>() {
    };

    private final RestClient restClient;
    private final String backendUrl;

    public PetClinicBackendClient(
            RestClient.Builder builder,
            @Value("${petclinic.backend.url:http://localhost:9966/petclinic}") String backendUrl) {
        String normalized = backendUrl == null ? "" : backendUrl.trim();
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        this.backendUrl = normalized;
        this.restClient = builder.baseUrl(normalized).build();
    }

    public String backendUrl() {
        return this.backendUrl;
    }

    public List<OwnerDto> listOwners(String lastName) {
        try {
            List<OwnerDto> owners = this.restClient.get()
                    .uri(uriBuilder -> {
                        var builder = uriBuilder.path("/api/owners");
                        if (lastName != null && !lastName.isBlank()) {
                            builder = builder.queryParam("lastName", lastName.trim());
                        }
                        return builder.build();
                    })
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(OWNER_LIST);
            return owners == null ? List.of() : owners;
        } catch (HttpClientErrorException.NotFound ex) {
            return List.of();
        }
    }

    public OwnerDto getOwner(int id) {
        return this.restClient.get()
                .uri("/api/owners/{id}", id)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(OwnerDto.class);
    }

    public OwnerDto createOwner(OwnerRequest request) {
        return this.restClient.post()
                .uri("/api/owners")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    throw new IllegalStateException("Backend owner creation failed with HTTP " + res.getStatusCode().value());
                })
                .body(OwnerDto.class);
    }

    public List<VetDto> listVets() {
        List<VetDto> vets = this.restClient.get()
                .uri("/api/vets")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(VET_LIST);
        return vets == null ? List.of() : vets;
    }
}
