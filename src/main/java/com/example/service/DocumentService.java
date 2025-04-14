package com.example.service;

import com.example.model.Document;

import java.util.Optional;
import java.util.UUID;

public interface DocumentService {

    Optional<Document> getDocument(UUID id);

    Document createDocument(Document document);

    String processDocument(String documentId);


}
