package omaprojekti.happyplants.Webcontroller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import omaprojekti.happyplants.Domain.Cutting;
import omaprojekti.happyplants.Domain.Plant;
import omaprojekti.happyplants.Domain.PlantRepository;
import omaprojekti.happyplants.Domain.Species;
import omaprojekti.happyplants.Domain.SpeciesRepository;

@Controller
public class PlantController {

    @Autowired
    private PlantRepository plantRepository;

    @Autowired
    private SpeciesRepository speciesRepository;

    /* Listaa kaikki tietokannasta löytyvät kasvit */
    @GetMapping("/plantlist")
    public String listPlants(Model model) {
        model.addAttribute("plants", plantRepository.findAll());
        return "plantlist";
    }

    /* Hakee valitun kasvin pistokaslistan */
    @GetMapping("/plantcuttinglist/{id}")
    // @PreAuthorize("hasAuthority('ADMIN')")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER')")
    public String plantCuttingList(@PathVariable("id") Long plantId, Model model) {

        Plant plant = plantRepository.findById(plantId).orElse(null);
        if (plant != null) {
            List<Cutting> plantCuttingList = plant.getCuttings();
            model.addAttribute("plant", plant);
            model.addAttribute("plantcuttinglist", plantCuttingList);
        }
        return "plantcuttinglist";
    }

    /* Poista valittu kasvi tietokannasta id:n perusteella */
    @GetMapping("/deleteplant/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String deletePlant(@PathVariable("id") Long plantId, Model model) {
        plantRepository.deleteById(plantId);
        return "redirect:/plantlist";
    }

    /* Muokkaa valitun kasvin tietoja id:n perusteella */
    @GetMapping("/editplant/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String editPlant(@PathVariable("id") long plantId, Model model) {
        Plant plant = plantRepository.findById(plantId).orElse(null);
        if (plant != null) {
            model.addAttribute("plant", plant);
            model.addAttribute("species", speciesRepository.findAll());
        }
        return "editplant";
    }

    /* Lisää uusi kasvi */
    @GetMapping("/addplant")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String addPlant(Model model) {
        model.addAttribute("plant", new Plant());
        model.addAttribute("species", speciesRepository.findAll());
        return "addplant";
    }

    /* Tallenna uusi kasvi */
    @PostMapping("/saveplant")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String savePlant(Plant plant) {
        plantRepository.save(plant);
        return "redirect:/plantlist";
    }

    /* Päivitä muokatun kasvin tiedot vanhojen tilalle */
    @PostMapping("/updateplant")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String updatePlant(@ModelAttribute("plant") Plant updatedPlant) {
        Long plantId = updatedPlant.getPlantId();
        Plant currentPlant = plantRepository.findById(plantId).orElse(null);

        if (currentPlant != null) {
            currentPlant.setPlantName(updatedPlant.getPlantName());
            currentPlant.setLightRequirement(updatedPlant.getLightRequirement());
            currentPlant.setPlantDescription(updatedPlant.getPlantDescription());
            Species selectedPlantSpecies = speciesRepository.findById(updatedPlant.getSpecies().getSpeciesId())
                    .orElse(null);
            currentPlant.setSpecies(selectedPlantSpecies);

            plantRepository.save(currentPlant);
        }
        return "redirect:/plantlist";
    }

}
