package com.api.app_location.rest;

import com.api.app_location.dto.CoffeWorkDTO;
import com.api.app_location.entity.CoffeWork;
import com.api.app_location.service.CoffeWorkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api")
public class CoffeWorkController {

    @Autowired
    private CoffeWorkService coffeWorkService;

    @GetMapping("/list")
    public List<CoffeWorkDTO> listAll() {
        return coffeWorkService.listAll();
    }

    @GetMapping("/filter-name")
    public List<CoffeWorkDTO> listName(@RequestParam String name) {
        return coffeWorkService.listName(name);
    }

    @GetMapping("/list/best-coffe")
    public List<CoffeWorkDTO> listBest() {
        return coffeWorkService.bestCoffes();
    }

    @PostMapping("/add-coffe")
    public ResponseEntity<CoffeWork> addCoffe(@RequestBody CoffeWorkDTO coffeDTO) {
            CoffeWork saveCoffe = coffeWorkService.save(coffeDTO);
            return new ResponseEntity<>(saveCoffe, HttpStatus.CREATED);
    }

    @GetMapping("/closest/coffe")
    public List<CoffeWorkDTO> closestCoffe(@RequestParam double latitude, @RequestParam double longitude ) {
        return coffeWorkService.nearestCoffeeShops(latitude,longitude );
    }
}
