package com.api.app_location.service;

import com.api.app_location.dao.CoffeWorkRepository;
import com.api.app_location.dto.CoffeWorkDTO;
import com.api.app_location.entity.CoffeWork;
import com.api.app_location.exception.FailedSaveException;
import com.api.app_location.mapper.CoffeWorkMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class CoffeWorkService {

    @Autowired
    private CoffeWorkRepository coffeWorkRepository;

    @Autowired
    private CoffeWorkMapper mapper;

    public List<CoffeWorkDTO> listAll(int page, int size) throws Exception {
        if (size >= 100) {
            throw new FailedSaveException("Limite máximo de items retornados atingido");
        }
        Pageable pageable = PageRequest.of(page, size);
        return coffeWorkRepository.findAll(pageable).map(mapper::toDTO).getContent();
    }

    public List<CoffeWorkDTO> listName(String name) {
        List<CoffeWork> entities = coffeWorkRepository.findByNameContainingIgnoreCase(name);
        return mapper.toDTOList(entities);
    }

    public List<CoffeWorkDTO> bestCoffes(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return coffeWorkRepository.bestCoffes(pageable).stream().map(mapper::toDTO).toList();
    }

    public CoffeWork save(CoffeWorkDTO dto) {
        try {
            CoffeWork mapperEntity = mapper.toEntity(dto);
            return coffeWorkRepository.save(mapperEntity);
        } catch (Exception e) {
            throw new FailedSaveException("Erro ao salvar café: " + e.getMessage());
        }
    }

    public List<CoffeWorkDTO> nearestCoffeeShops(double latitude, double longitude) {
        try {
            List<CoffeWork> listNearestCoffeeShops = coffeWorkRepository.nearestCoffeeShops(latitude, longitude);
            return mapper.toDTOList(listNearestCoffeeShops);
        } catch (Exception e) {
            throw new FailedSaveException("Erro ao localizar: " + e.getMessage());
        }
    }

    public CoffeWorkDTO delete(Integer id) {
        try {
            if (!coffeWorkRepository.existsById(id)) {
                log.info("id não encontrado: " + id);
                return null;
            }

            CoffeWorkDTO dto = mapper.toDTO(coffeWorkRepository.findById(id).get());
            coffeWorkRepository.deleteById(id);
            return dto;
        } catch (Exception e) {
            throw new FailedSaveException("Erro ao deletar café: " + e.getMessage());
        }
    }
}

