package com.example.service;

import com.example.context.SecurityContext;
import com.example.dto.DocumentDTO;
import com.example.exception.CustomException;
import com.example.restclient.CacheService;
import com.example.service.impl.RedisDocumentServiceImpl;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.acme.grpc.document.caching.Document;
import org.acme.grpc.document.caching.DocumentCachingGrpc;
import org.acme.grpc.document.caching.GetDocumentRequest;
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
    DocumentCachingGrpc.DocumentCachingBlockingStub documentService;


    @InjectMocks
    RedisDocumentServiceImpl redisService;

    private UUID tenantId;
    private UUID documentId;

    @BeforeEach
    void setUp()
    {
        tenantId=UUID.randomUUID();
        documentId=UUID.randomUUID();
    }

    @Test
    void creatDocument_Success()
    {
        DocumentDTO documentDto=new DocumentDTO();
        documentDto.setId(UUID.randomUUID().toString());
        documentDto.setContent("Redis Content");
        documentDto.setTitle("Redis Title");

        Document document=Document.newBuilder().setId(UUID.randomUUID().toString())
                        .setTitle("Redis Title").setContent("Redis Content").build();

        Mockito.when(documentService.create(any(Document.class))).thenReturn(document);

        DocumentDTO doc=redisService.createDocument(documentDto,tenantId.toString());
        assertNotNull(doc.getId());
        assertEquals("Redis Title", doc.getTitle());

    }

    @Test
    void createDocument_cacheServiceThrowsWebApplicationException() {
        WebApplicationException webException = new WebApplicationException("Cache service error", Response.Status.BAD_REQUEST);
        Mockito.when(documentService.create(any(Document.class))).thenThrow(webException);

        DocumentDTO documentDto=new DocumentDTO();
        documentDto.setId(UUID.randomUUID().toString());
        documentDto.setContent("Redis Content");
        documentDto.setTitle("Redis Title");

        CustomException thrown = assertThrows(CustomException.class, () -> redisService.createDocument(documentDto,
                tenantId.toString()));

        assertEquals("Cache service error", thrown.getMessage());
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), thrown.getHttpStatus());
    }

    @Test
    void createDocument_cacheServiceThrowsGenericException() {
        RuntimeException runtimeException = new RuntimeException("Generic cache error");
        Mockito.when(documentService.create(any(Document.class))).thenThrow(runtimeException);

        DocumentDTO documentDto=new DocumentDTO();
        documentDto.setId(UUID.randomUUID().toString());
        documentDto.setContent("Redis Content");
        documentDto.setTitle("Redis Title");

        CustomException thrown = assertThrows(CustomException.class, () -> redisService.createDocument(documentDto,
                tenantId.toString()));

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

        GetDocumentRequest docRequest=GetDocumentRequest.newBuilder()
                        .setDocumentId(documentId.toString()).setTenantId(tenantId.toString()).build();

        Document document=Document.newBuilder().setId(UUID.randomUUID().toString())
                .setTitle("Retrieved Document").setContent("Retrieved Content")
                .setTenantId(tenantId.toString()).setId(documentId.toString()).build();

        Mockito.when(documentService.getDocument(docRequest)).thenReturn(document);

        DocumentDTO result=redisService.getDocument(documentId,tenantId.toString());
        assertNotNull(result);
        assertEquals(documentId.toString(),retrievedDTO.getId());
    }

    @Test
    void getDocument_cacheServiceError() {
        WebApplicationException webException = new WebApplicationException("Document not found in cache",
                Response.status(404).build());
        GetDocumentRequest docRequest=GetDocumentRequest.newBuilder()
                .setDocumentId(documentId.toString()).setTenantId(tenantId.toString()).build();
        Mockito.when(documentService.getDocument(docRequest)).thenThrow(webException);

        CustomException exception = assertThrows(CustomException.class, () -> redisService.getDocument(documentId,
                tenantId.toString()));
        assertEquals("Document not found in cache", exception.getMessage());
        assertEquals(404, exception.getHttpStatus());
    }

    @Test
    void getDocument_genericError() {
        RuntimeException runtimeException = new RuntimeException("Cache unavailable");
        GetDocumentRequest docRequest=GetDocumentRequest.newBuilder()
                .setDocumentId(documentId.toString()).setTenantId(tenantId.toString()).build();
        Mockito.when(documentService.getDocument(docRequest)).thenThrow(runtimeException);

        CustomException exception = assertThrows(CustomException.class, () -> redisService.getDocument(documentId,
                tenantId.toString()));
        assertEquals("Cache unavailable", exception.getMessage());
        assertEquals(500, exception.getHttpStatus());
    }
}
