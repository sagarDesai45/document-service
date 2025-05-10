package com.example.rest;

import com.example.dto.DocumentDTO;
import com.example.exception.CustomException;
import com.example.service.DocumentService;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@QuarkusTest
public class RestControllerTest {

    @InjectMock
    DocumentService documentService;

    @Inject
    RestController restController;

    private DocumentDTO testDocumentDTO;

    private UUID tenantId;
    private UUID documentId;

    @BeforeEach
    void setUp()
    {
        tenantId=UUID.randomUUID();
        documentId=UUID.randomUUID();
        testDocumentDTO =
                DocumentDTO.builder().id(documentId.toString()).title("Test Document")
                        .tenantId(tenantId.toString()).content("Test Content").build();
    }

    @Test
    void createDocument_success() {

        when(documentService.createDocument(Mockito.any(DocumentDTO.class), eq(tenantId.toString()))).thenReturn(testDocumentDTO);

        Response response=restController.createDocument(tenantId.toString(),testDocumentDTO);

        assertEquals(Response.Status.CREATED.getStatusCode(),response.getStatus());
    }

    @Test
    void getDocument_success() {

        when(documentService.getDocument(documentId,tenantId.toString())).thenReturn(testDocumentDTO);

        Response response=restController.getDocument(documentId,tenantId.toString());

        assertEquals(Response.Status.OK.getStatusCode(),response.getStatus());
    }

    @Test
    void getDocument_withNoTenant() {
        UUID documentId = UUID.randomUUID();
        when(documentService.getDocument(documentId,"")).thenThrow(new CustomException("something went wrong",
                HttpResponseStatus.BAD_REQUEST.code()));

        CustomException thrown = assertThrows(
                CustomException.class,
                () -> restController.getDocument(documentId, ""),
                "something went wrong"
        );

        assertEquals("something went wrong", thrown.getMessage());
        assertEquals(HttpResponseStatus.BAD_REQUEST.code(), thrown.getHttpStatus());
    }

    @Test
    void getDocument_accessDenied() {
        UUID documentId = UUID.randomUUID();

        when(documentService.getDocument(documentId, tenantId.toString()))
                .thenThrow(new CustomException("you don't have access", HttpResponseStatus.FORBIDDEN.code()));

        CustomException thrown = assertThrows(
                CustomException.class,
                () -> restController.getDocument(documentId, tenantId.toString()),
                "Expected getDocument to throw CustomException"
        );

        assertEquals("you don't have access", thrown.getMessage());
        assertEquals(HttpResponseStatus.FORBIDDEN.code(), thrown.getHttpStatus());
    }

    @Test
    void getDocument_notFound() {
        UUID documentId = UUID.randomUUID();

        when(documentService.getDocument(documentId,tenantId.toString())).thenThrow(new CustomException("not found",
                HttpResponseStatus.BAD_REQUEST.code()));

        CustomException thrown = assertThrows(
                CustomException.class,
                () -> restController.getDocument(documentId, tenantId.toString()),
                "not found"
        );

        assertEquals("not found", thrown.getMessage());
        assertEquals(HttpResponseStatus.BAD_REQUEST.code(), thrown.getHttpStatus());
    }
}