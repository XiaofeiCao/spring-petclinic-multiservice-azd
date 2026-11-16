package org.springframework.samples.petclinic.web;

import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.web.PetClinicModels.OwnerDto;
import org.springframework.samples.petclinic.web.PetClinicModels.OwnerRequest;
import org.springframework.samples.petclinic.web.PetClinicModels.VetDto;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class PetClinicWebController {

    private final PetClinicBackendClient backend;

    public PetClinicWebController(PetClinicBackendClient backend) {
        this.backend = backend;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("backendUrl", this.backend.backendUrl());
        return "index";
    }

    @GetMapping("/owners")
    public String owners(@RequestParam(name = "lastName", required = false) String lastName, Model model) {
        model.addAttribute("lastName", lastName == null ? "" : lastName);
        model.addAttribute("owners", this.backend.listOwners(lastName));
        model.addAttribute("ownerForm", new OwnerRequest("", "", "", "", ""));
        return "owners";
    }

    @PostMapping("/owners")
    public String createOwner(
            @Valid @ModelAttribute("ownerForm") OwnerRequest ownerForm,
            BindingResult bindingResult,
            Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("lastName", "");
            model.addAttribute("owners", this.backend.listOwners(null));
            return "owners";
        }
        OwnerDto created = this.backend.createOwner(ownerForm);
        return "redirect:/owners/" + created.id();
    }

    @GetMapping("/owners/{id}")
    public String ownerDetail(@PathVariable("id") int id, Model model) {
        model.addAttribute("owner", this.backend.getOwner(id));
        return "owner-detail";
    }

    @GetMapping("/vets")
    public String vets(Model model) {
        model.addAttribute("vets", this.backend.listVets());
        return "vets";
    }

    @GetMapping("/api/frontend/owners")
    @ResponseBody
    public List<OwnerDto> apiOwners(@RequestParam(name = "lastName", required = false) String lastName) {
        return this.backend.listOwners(lastName);
    }

    @PostMapping("/api/frontend/owners")
    @ResponseBody
    public ResponseEntity<OwnerDto> apiCreateOwner(@Valid @RequestBody OwnerRequest request) {
        OwnerDto created = this.backend.createOwner(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/api/frontend/owners/{id}")
    @ResponseBody
    public OwnerDto apiOwnerDetail(@PathVariable("id") int id) {
        return this.backend.getOwner(id);
    }

    @GetMapping("/api/frontend/vets")
    @ResponseBody
    public List<VetDto> apiVets() {
        return this.backend.listVets();
    }
}
