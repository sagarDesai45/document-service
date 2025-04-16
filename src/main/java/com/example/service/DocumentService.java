package com.example.service;

import com.example.dto.DocumentDTO;

import java.util.UUID;

public interface DocumentService {

    DocumentDTO getDocument(UUID id);

    DocumentDTO createDocument(DocumentDTO document);

    String processDocument(String documentId);


}
