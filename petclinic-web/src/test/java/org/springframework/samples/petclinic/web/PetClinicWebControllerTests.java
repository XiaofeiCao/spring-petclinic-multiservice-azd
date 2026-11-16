package org.springframework.samples.petclinic.web;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.web.PetClinicModels.OwnerDto;
import org.springframework.samples.petclinic.web.PetClinicModels.OwnerRequest;
import org.springframework.samples.petclinic.web.PetClinicModels.SpecialtyDto;
import org.springframework.samples.petclinic.web.PetClinicModels.VetDto;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PetClinicWebController.class)
class PetClinicWebControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PetClinicBackendClient backend;

    @Test
    void homePageRenders() throws Exception {
        given(this.backend.backendUrl()).willReturn("http://localhost:9966/petclinic");

        this.mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Welcome to Spring PetClinic")));
    }

    @Test
    void ownersPageListsBackendOwners() throws Exception {
        given(this.backend.listOwners(null)).willReturn(List.of(
                new OwnerDto(1, "George", "Franklin", "110 W. Liberty St.", "Madison", "6085551023", List.of())));

        this.mockMvc.perform(get("/owners"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("George Franklin")));
    }

    @Test
    void createOwnerFormRedirectsToOwnerDetail() throws Exception {
        given(this.backend.createOwner(any(OwnerRequest.class))).willReturn(
                new OwnerDto(11, "Ada", "Lovelace", "12 Analytical Way", "London", "02079460123", List.of()));

        this.mockMvc.perform(post("/owners")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("firstName", "Ada")
                        .param("lastName", "Lovelace")
                        .param("address", "12 Analytical Way")
                        .param("city", "London")
                        .param("telephone", "02079460123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "/owners/11"));
    }

    @Test
    void frontendJsonApiCreatesAndListsVets() throws Exception {
        given(this.backend.listVets()).willReturn(List.of(
                new VetDto(1, "James", "Carter", List.of(new SpecialtyDto(1, "radiology")))));
        given(this.backend.createOwner(any(OwnerRequest.class))).willReturn(
                new OwnerDto(12, "Grace", "Hopper", "99 Compiler Rd", "Arlington", "7035550199", List.of()));

        this.mockMvc.perform(get("/api/frontend/vets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName").value("James"));

        this.mockMvc.perform(post("/api/frontend/owners")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"Grace","lastName":"Hopper","address":"99 Compiler Rd","city":"Arlington","telephone":"7035550199"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(12));
    }
}
