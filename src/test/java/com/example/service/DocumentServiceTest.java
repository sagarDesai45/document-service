package com.example.service;


import com.example.context.SecurityContext;
import com.example.dto.DocumentDTO;
import com.example.exception.CustomException;
import com.example.mapper.DocumentMapper;
import com.example.model.Document;
import com.example.model.Tenant;
import com.example.model.TenantDocument;
import com.example.service.impl.DocumentServiceImpl;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@QuarkusTest
@Transactional
class DocumentServiceTest {

    @Inject
    private DocumentServiceImpl documentService;

    @InjectMock
    private SecurityContext securityContext;

    @InjectMock
    private DocumentMapper documentMapper;

    private Tenant testTenant;
    private Document testDocument;
    private DocumentDTO testDocumentDTO;
    private UUID testTenantId;
    private UUID testDocumentId;

    @BeforeEach
    void setUp() {
        testTenantId = UUID.randomUUID();
        testDocumentId = UUID.randomUUID();

        testTenant = Tenant.builder().id(testTenantId).name("Test Tenant").build();
        testDocument = Document.builder().id(testDocumentId).title("Test Document").content("Test Content").build();
        testDocumentDTO =
                DocumentDTO.builder().id(testDocumentId.toString()).title("Test Document").content("Test Content").build();

        Mockito.when(securityContext.getCurrentTenantId()).thenReturn(testTenantId.toString());
        Mockito.when(documentMapper.toDto(any(Document.class))).thenReturn(testDocumentDTO);
    }

    @Test
    void createDocument_success() {

        Mockito.when(securityContext.getCurrentTenantId()).thenReturn(testTenantId.toString());
        Mockito.when(documentMapper.toDto(any(Document.class))).thenReturn(testDocumentDTO);
        DocumentDTO inputDocumentDTO = DocumentDTO.builder().title("New Document").content("New Content").build();
        Document expectedNewDocument = Document.builder().title("New Document").content("New Content").build();
        TenantDocument expectedTenantDocument = TenantDocument.builder().tenant(testTenant).document(expectedNewDocument).build();

        DocumentDTO actualDocumentDTO = documentService.createDocument(inputDocumentDTO);

        assertEquals(testDocumentDTO, actualDocumentDTO);

    }

    @Test
    void createDocument_newTenant() {
        UUID newTenantId = UUID.randomUUID();
        when(securityContext.getCurrentTenantId()).thenReturn(newTenantId.toString());

        DocumentDTO inputDocumentDTO = DocumentDTO.builder().title("New Document").content("New Content").build();

        DocumentDTO actualDocumentDTO = documentService.createDocument(inputDocumentDTO);

        assertEquals(testDocumentDTO, actualDocumentDTO);

    }

    @Test
    void createDocument_noTenantId() {

        when(securityContext.getCurrentTenantId()).thenReturn(null);

        DocumentDTO inputDocumentDTO = DocumentDTO.builder().title("New Document").content("New Content").build();

        RuntimeException exception = assertThrows(RuntimeException.class, () -> documentService.createDocument(inputDocumentDTO));
        assertEquals("Something went wrong", exception.getMessage());
    }

    @Test
    void createDocument_emptyTenantId() {

        when(securityContext.getCurrentTenantId()).thenReturn("");

        DocumentDTO inputDocumentDTO = DocumentDTO.builder().title("New Document").content("New Content").build();
        RuntimeException exception = assertThrows(RuntimeException.class, () -> documentService.createDocument(inputDocumentDTO));
        assertEquals("Something went wrong", exception.getMessage());

    }

    @Test
    void getDocument_tenantDocumentNotFound() {

        CustomException exception = assertThrows(CustomException.class, () -> documentService.getDocument(testDocumentId));
        assertEquals("Document not found", exception.getMessage());
        assertEquals(HttpResponseStatus.BAD_REQUEST.code(), exception.getHttpStatus());

    }
    @Test
    void processDocument_tenantDocumentNotFound() {

        String documentIdString = testDocumentId.toString();

        CustomException exception = assertThrows(CustomException.class, () -> documentService.processDocument(documentIdString));
        assertEquals("Document not found", exception.getMessage());
        assertEquals(HttpResponseStatus.BAD_REQUEST.code(), exception.getHttpStatus());

    }

}