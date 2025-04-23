package com.example.service;

import com.example.dto.DocumentDTO;

import java.util.UUID;

public interface DocumentService {

    DocumentDTO getDocument(UUID id,String tenantId);

    DocumentDTO createDocument(DocumentDTO document,String tenantId);

    String processDocument(String documentId,String tenantId);


}
