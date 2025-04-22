package com.api.app_location.mapper;

import com.api.app_location.dto.CoffeWorkDTO;
import com.api.app_location.entity.CoffeWork;
import org.mapstruct.Mapper;
import java.util.List;

@Mapper(componentModel = "spring")
public interface CoffeWorkMapper {

    CoffeWorkDTO toDTO(CoffeWork entity);

    CoffeWork toEntity(CoffeWorkDTO dto);

    List<CoffeWorkDTO> toDTOList(List<CoffeWork> entityList);

    List<CoffeWork> toEntityList(List<CoffeWorkDTO> dtoList);
}