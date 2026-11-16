package org.springframework.samples.petclinic.web;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class PetClinicModels {

    private PetClinicModels() {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PetTypeDto(Integer id, String name) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PetDto(Integer id, String name, String birthDate, PetTypeDto type) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OwnerDto(
            Integer id,
            String firstName,
            String lastName,
            String address,
            String city,
            String telephone,
            List<PetDto> pets) {

        public OwnerDto {
            pets = pets == null ? List.of() : List.copyOf(pets);
        }
    }

    public record OwnerRequest(
            @NotBlank @Size(max = 30) String firstName,
            @NotBlank @Size(max = 30) String lastName,
            @NotBlank @Size(max = 255) String address,
            @NotBlank @Size(max = 80) String city,
            @NotBlank @Size(max = 20) @Pattern(regexp = "^[0-9]+$") String telephone) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SpecialtyDto(Integer id, String name) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record VetDto(Integer id, String firstName, String lastName, List<SpecialtyDto> specialties) {

        public VetDto {
            specialties = specialties == null ? List.of() : List.copyOf(specialties);
        }
    }
}
