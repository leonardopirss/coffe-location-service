package com.api.app_location.service;

import com.api.app_location.dao.CoffeWorkRepository;
import com.api.app_location.dto.CoffeWorkDTO;
import com.api.app_location.entity.CoffeWork;
import com.api.app_location.exception.FailedSaveException;
import com.api.app_location.mapper.CoffeWorkMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CoffeWorkService {

    @Autowired
    private CoffeWorkRepository coffeWorkRepository;

    @Autowired
    private CoffeWorkMapper mapper;

    public List<CoffeWorkDTO> listAll() {
        List<CoffeWork> entities = coffeWorkRepository.findAll();
        return mapper.toDTOList(entities);
    }

    public List<CoffeWorkDTO> listName(String name) {
        List<CoffeWork> entities = coffeWorkRepository.findByNameContainingIgnoreCase(name);
        return mapper.toDTOList(entities);
    }

    public List<CoffeWorkDTO> bestCoffes() {
        List<CoffeWork> entities = coffeWorkRepository.bestCoffes();
        return mapper.toDTOList(entities);
    }

    public CoffeWork save(CoffeWorkDTO dto) {
        try {
            CoffeWork mapperEntity = mapper.toEntity(dto);
            return coffeWorkRepository.save(mapperEntity);
        } catch (Exception e) {
            throw new FailedSaveException("Erro ao salvar lugar: " + e.getMessage());
        }
    }

    public List<CoffeWorkDTO> nearestCoffeeShops(double latitude, double longitude){
        try {
            List<CoffeWork> listNearestCoffeeShops = coffeWorkRepository.nearestCoffeeShops(latitude, longitude);
            return mapper.toDTOList(listNearestCoffeeShops);
        } catch (Exception e) {
            throw new FailedSaveException("Erro ao localizar: " + e.getMessage());
        }
    }

}

