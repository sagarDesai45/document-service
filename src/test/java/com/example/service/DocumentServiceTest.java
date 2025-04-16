package com.example.service;


import com.example.context.SecurityContext;
import com.example.dto.DocumentDTO;
import com.example.exception.CustomException;
import com.example.mapper.DocumentMapper;
import com.example.model.Document;
import com.example.model.Tenant;
import com.example.model.TenantDocument;
import com.example.repository.DocumentRepository;
import com.example.repository.TenantDocRepository;
import com.example.repository.TenantRepository;
import com.example.service.impl.DocumentServiceImpl;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
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

    @InjectMock
    private TenantRepository tenantRepository;

    @InjectMock
    private DocumentRepository documentRepository;

    @InjectMock
    private TenantDocRepository tenantDocRepository;

    private Tenant testTenant;
    private Document testDocument;
    private DocumentDTO testDocumentDTO;

    private TenantDocument tenantDocument;
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
        tenantDocument=TenantDocument.builder().id(UUID.randomUUID()).document(testDocument).tenant(testTenant).build();

        Mockito.when(securityContext.getCurrentTenantId()).thenReturn(testTenantId.toString());
        Mockito.when(documentMapper.toDto(any(Document.class))).thenReturn(testDocumentDTO);
        doNothing().when(tenantRepository).persist(testTenant);
        doNothing().when(tenantDocRepository).persist(tenantDocument);
        doNothing().when(documentRepository).persist(testDocument);
    }

    @Test
    void createDocument_success() {

        DocumentDTO inputDocumentDTO = DocumentDTO.builder().title("Test Document").content("Test Content").build();

        DocumentDTO actualDocumentDTO = documentService.createDocument(inputDocumentDTO);

        assertNotNull(actualDocumentDTO);
        assertEquals(actualDocumentDTO.getTitle(),inputDocumentDTO.getTitle());

    }

    @Test
    void createDocument_newTenant() {
        UUID newTenantId = UUID.randomUUID();
        when(securityContext.getCurrentTenantId()).thenReturn(newTenantId.toString());

        DocumentDTO inputDocumentDTO = DocumentDTO.builder().title("Test Document").content("Test Content").build();

        DocumentDTO actualDocumentDTO = documentService.createDocument(inputDocumentDTO);

        assertNotNull(actualDocumentDTO);
        assertEquals(actualDocumentDTO.getTitle(),inputDocumentDTO.getTitle());

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
    void getDocument_tenantDocumentFound() {

        Mockito.when(tenantDocRepository.list("document.id = ?1 ",testDocumentId)).thenReturn(List.of(tenantDocument));
        DocumentDTO documentDTO=documentService.getDocument(testDocumentId);
        assertEquals(testDocumentId.toString(),documentDTO.getId());

    }

    @Test
    void getDocument_tenantDocumentNotAccessed() {

        Mockito.when(securityContext.getCurrentTenantId()).thenReturn(UUID.randomUUID().toString());
        Mockito.when(tenantDocRepository.list("document.id = ?1 ",testDocumentId)).thenReturn(List.of(tenantDocument));
        CustomException exception = assertThrows(CustomException.class, () -> documentService.getDocument(testDocumentId));
        assertEquals(HttpResponseStatus.FORBIDDEN.code(), exception.getHttpStatus());

    }
    @Test
    void processDocument_tenantDocumentNotFound() {

        String documentIdString = testDocumentId.toString();

        CustomException exception = assertThrows(CustomException.class, () -> documentService.processDocument(documentIdString));
        assertEquals("Document not found", exception.getMessage());
        assertEquals(HttpResponseStatus.BAD_REQUEST.code(), exception.getHttpStatus());

    }

    @Test
    void processDocument_tenantDocumentFound() {

        Mockito.when(tenantDocRepository.list("document.id = ?1 ",testDocumentId)).thenReturn(List.of(tenantDocument));
        String processMessage=documentService.processDocument(testDocumentId.toString());
        assertNotNull(processMessage);

    }

    @Test
    void processDocument_tenantDocumentNotAccessed() {

        Mockito.when(securityContext.getCurrentTenantId()).thenReturn(UUID.randomUUID().toString());
        Mockito.when(tenantDocRepository.list("document.id = ?1 ",testDocumentId)).thenReturn(List.of(tenantDocument));
        CustomException exception = assertThrows(CustomException.class,
                () -> documentService.processDocument(testDocumentId.toString()));
        assertEquals(HttpResponseStatus.FORBIDDEN.code(), exception.getHttpStatus());

    }

}