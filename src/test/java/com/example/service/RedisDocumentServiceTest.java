package com.example.service;

import com.example.context.SecurityContext;
import com.example.dto.DocumentDTO;
import com.example.exception.CustomException;
import com.example.restclient.CacheService;
import com.example.service.impl.RedisDocumentServiceImpl;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class RedisDocumentServiceTest {

    @Mock
    CacheService cacheService;

    @Mock
    SecurityContext securityContext;

    @InjectMocks
    RedisDocumentServiceImpl redisService;

    private UUID tenantId;
    private UUID documentId;

    @BeforeEach
    void setUp()
    {
        tenantId=UUID.randomUUID();
        documentId=UUID.randomUUID();
        Mockito.when(securityContext.getCurrentTenantId()).thenReturn(tenantId.toString());
    }

    @Test
    void creatDocument_Success()
    {
        DocumentDTO document=new DocumentDTO();
        document.setId(UUID.randomUUID().toString());
        document.setContent("Redis Content");
        document.setTitle("Redis Title");

        Mockito.when(cacheService.createDocument(any())).thenReturn(document);

        DocumentDTO doc=redisService.createDocument(document);
        assertNotNull(doc.getId());
        assertEquals("Redis Title", doc.getTitle());

    }

    @Test
    void createDocument_cacheServiceThrowsWebApplicationException() {
        WebApplicationException webException = new WebApplicationException("Cache service error", Response.Status.BAD_REQUEST);
        Mockito.when(cacheService.createDocument(any(DocumentDTO.class))).thenThrow(webException);

        CustomException thrown = assertThrows(CustomException.class, () -> redisService.createDocument(new DocumentDTO()));

        assertEquals("Cache service error", thrown.getMessage());
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), thrown.getHttpStatus());
    }

    @Test
    void createDocument_cacheServiceThrowsGenericException() {
        RuntimeException runtimeException = new RuntimeException("Generic cache error");
        Mockito.when(cacheService.createDocument(any(DocumentDTO.class))).thenThrow(runtimeException);

        CustomException thrown = assertThrows(CustomException.class, () -> redisService.createDocument(new DocumentDTO()));

        assertEquals("Generic cache error", thrown.getMessage());
        assertEquals(500, thrown.getHttpStatus());
    }

    @Test
    void getDocument_Success()
    {
        DocumentDTO retrievedDTO = new DocumentDTO();
        retrievedDTO.setId(documentId.toString());
        retrievedDTO.setTitle("Retrieved Document");
        retrievedDTO.setContent("Retrieved Content");
        retrievedDTO.setTenantId(tenantId.toString());

        Mockito.when(cacheService.getDocument(documentId, tenantId)).thenReturn(retrievedDTO);

        DocumentDTO result=redisService.getDocument(documentId);
        assertNotNull(result);
        assertEquals(documentId.toString(),retrievedDTO.getId());
    }

    @Test
    void getDocument_cacheServiceError() {
        WebApplicationException webException = new WebApplicationException("Document not found in cache",
                Response.status(404).build());
        Mockito.when(cacheService.getDocument(documentId, tenantId)).thenThrow(webException);

        CustomException exception = assertThrows(CustomException.class, () -> redisService.getDocument(documentId));
        assertEquals("Document not found in cache", exception.getMessage());
        assertEquals(404, exception.getHttpStatus());
    }

    @Test
    void getDocument_genericError() {
        RuntimeException runtimeException = new RuntimeException("Cache unavailable");
        Mockito.when(cacheService.getDocument(documentId, tenantId)).thenThrow(runtimeException);

        CustomException exception = assertThrows(CustomException.class, () -> redisService.getDocument(documentId));
        assertEquals("Cache unavailable", exception.getMessage());
        assertEquals(500, exception.getHttpStatus());
    }
}
