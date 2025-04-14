package com.example.service;


import com.example.context.SecurityContext;
import com.example.model.Document;
import com.example.model.Tenant;
import com.example.service.impl.DocumentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @InjectMocks
    private DocumentServiceImpl documentService;

    @Mock
    private SecurityContext securityContext;

    private Tenant testTenant;
    private String tenantId;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID().toString();
        testTenant = new Tenant(tenantId, "Test Tenant");
        when(securityContext.getCurrentTenantId()).thenReturn(tenantId);
    }

    @Test
    void createDocument_success() {
        Document document = new Document();
        document.setTitle("Test Document");
        Document createdDocument = documentService.createDocument(document);

        assertNotNull(createdDocument.getId());
        assertEquals("Test Document", createdDocument.getTitle());
    }


    @Test
    void getDocument_nonExistingDocument_returnsEmptyOptional() {
        Optional<Document> retrievedDocument = documentService.getDocument(UUID.randomUUID());
        assertFalse(retrievedDocument.isPresent());
    }

    @Test
    void getDocument_documentForDifferentTenant_returnsEmptyOptional() {
        // Create a document under the current tenant
        Document document = new Document();
        UUID documentId = UUID.randomUUID();
        document.setId(documentId.toString());
        documentService.createDocument(document);

        // Mock a different tenant
        when(securityContext.getCurrentTenantId()).thenReturn(UUID.randomUUID().toString());

        // Try to retrieve the document with the different tenant context
        Optional<Document> retrievedDocument = documentService.getDocument(documentId);
        assertFalse(retrievedDocument.isPresent());
    }


    @Test
    void processDocument_documentForDifferentTenant_throwsSecurityException() {
        // Create a document under the current tenant
        Document document = new Document();
        UUID documentId = UUID.randomUUID();
        document.setId(documentId.toString());
        documentService.createDocument(document);

        // Mock a different tenant
        when(securityContext.getCurrentTenantId()).thenReturn(UUID.randomUUID().toString());

        // Try to process the document with the different tenant context
        assertThrows(SecurityException.class, () -> documentService.processDocument(documentId.toString()));
    }


}