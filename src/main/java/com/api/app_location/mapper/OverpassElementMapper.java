package com.api.app_location.mapper;

import com.api.app_location.dto.CoffeWorkDTO;
import com.api.app_location.entity.CoffeWork;
import com.api.app_location.integration.overpass.dto.OverpassElement;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring")
public interface OverpassElementMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", expression = "java(tag(element, \"name\"))")
    @Mapping(target = "adress", expression = "java(address(element.tags()))")
    @Mapping(target = "street", expression = "java(street(element.tags()))")
    @Mapping(target = "addressNumber", expression = "java(tag(element, \"addr:housenumber\"))")
    @Mapping(target = "neighborhood", expression = "java(neighborhood(element.tags()))")
    @Mapping(target = "postalCode", expression = "java(tag(element, \"addr:postcode\"))")
    @Mapping(target = "municipality", expression = "java(tag(element, \"addr:city\"))")
    @Mapping(target = "uf", expression = "java(tag(element, \"addr:state\"))")
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "assessment", ignore = true)
    @Mapping(target = "latitude", expression = "java(element.resolvedLatitude())")
    @Mapping(target = "longitude", expression = "java(element.resolvedLongitude())")
    @Mapping(target = "osmType", source = "type")
    @Mapping(target = "osmId", source = "id")
    @Mapping(target = "internetAccess", expression = "java(tag(element, \"internet_access\"))")
    @Mapping(target = "openingHours", expression = "java(tag(element, \"opening_hours\"))")
    CoffeWork toEntity(OverpassElement element);

    List<CoffeWork> toEntityList(List<OverpassElement> elements);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", expression = "java(tag(element, \"name\"))")
    @Mapping(target = "adress", expression = "java(address(element.tags()))")
    @Mapping(target = "street", expression = "java(street(element.tags()))")
    @Mapping(target = "addressNumber", expression = "java(tag(element, \"addr:housenumber\"))")
    @Mapping(target = "neighborhood", expression = "java(neighborhood(element.tags()))")
    @Mapping(target = "postalCode", expression = "java(tag(element, \"addr:postcode\"))")
    @Mapping(target = "municipality", expression = "java(tag(element, \"addr:city\"))")
    @Mapping(target = "uf", expression = "java(tag(element, \"addr:state\"))")
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "assessment", ignore = true)
    @Mapping(target = "latitude", expression = "java(element.resolvedLatitude())")
    @Mapping(target = "longitude", expression = "java(element.resolvedLongitude())")
    @Mapping(target = "osmType", source = "type")
    @Mapping(target = "osmId", source = "id")
    @Mapping(target = "internetAccess", expression = "java(tag(element, \"internet_access\"))")
    @Mapping(target = "openingHours", expression = "java(tag(element, \"opening_hours\"))")
    CoffeWorkDTO toDTO(OverpassElement element);

    List<CoffeWorkDTO> toDTOList(List<OverpassElement> elements);

    default String tag(OverpassElement element, String key) {
        if (element == null) {
            return null;
        }

        return tag(element.tags(), key);
    }

    default String tag(Map<String, String> tags, String key) {
        return tags != null ? tags.get(key) : null;
    }

    default String street(Map<String, String> tags) {
        if (tags == null) {
            return null;
        }

        String street = tags.get("addr:street");
        return street != null ? street : tags.get("addr:place");
    }

    default String neighborhood(Map<String, String> tags) {
        if (tags == null) {
            return null;
        }

        String suburb = tags.get("addr:suburb");
        return suburb != null ? suburb : tags.get("addr:neighbourhood");
    }

    default String address(Map<String, String> tags) {
        if (tags == null) {
            return null;
        }

        String fullAddress = tags.get("addr:full");
        if (fullAddress != null) {
            return fullAddress;
        }

        String street = street(tags);
        String number = tags.get("addr:housenumber");
        String neighborhood = neighborhood(tags);
        String postcode = tags.get("addr:postcode");

        StringBuilder address = new StringBuilder();

        append(address, street);

        if (number != null) {
            append(address, street != null ? "nº " + number : number);
        }

        append(address, neighborhood);

        if (postcode != null) {
            append(address, "CEP " + postcode);
        }

        return !address.isEmpty() ? address.toString() : null;
    }

    default void append(StringBuilder value, String part) {
        if (part == null || part.isBlank()) {
            return;
        }

        if (!value.isEmpty()) {
            value.append(", ");
        }

        value.append(part);
    }
}
