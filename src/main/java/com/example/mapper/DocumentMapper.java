package com.example.mapper;

import com.example.dto.DocumentDTO;
import com.example.model.Document;
import org.mapstruct.Mapper;

@Mapper(componentModel = "cdi")
public interface DocumentMapper {

    DocumentDTO toDto(Document entity);
}
