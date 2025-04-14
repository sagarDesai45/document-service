package com.example.rest;

import com.example.model.Document;
import com.example.service.DocumentService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@QuarkusTest
public class RestControllerTest {

    @InjectMock
    DocumentService documentService;

    @Test
    @TestSecurity(user = "testUser", roles = {"admin"})
    void createDocument_adminRole_success() {
        Document documentToCreate = new Document();
        documentToCreate.setTitle("Test Document");
        Document createdDocument = new Document();
        createdDocument.setId(UUID.randomUUID().toString());
        createdDocument.setTitle("Test Document");

        when(documentService.createDocument(Mockito.any(Document.class))).thenReturn(createdDocument);

        given()
                .contentType(ContentType.JSON)
                .auth().preemptive().basic("testUser", "password")
                .body(documentToCreate)
                .when()
                .post("/documents")
                .then()
                .statusCode(Response.Status.CREATED.getStatusCode())
                .contentType(ContentType.JSON)
                .body("title", is("Test Document"))
                .body("id", is(createdDocument.getId()));
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"viewer"})
    void createDocument_viewerRole_forbidden() {
        Document documentToCreate = new Document();
        documentToCreate.setTitle("Test Document");

        given()
                .contentType(ContentType.JSON)
                .auth().preemptive().basic("testUser", "password")
                .body(documentToCreate)
                .when()
                .post("/documents")
                .then()
                .statusCode(Response.Status.FORBIDDEN.getStatusCode());

        verifyNoInteractions(documentService);
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"admin", "viewer"})
    void getDocument_adminOrViewerRole_existingDocument_success() {
        UUID documentId = UUID.randomUUID();
        Document existingDocument = new Document();
        existingDocument.setId(documentId.toString());
        existingDocument.setTitle("Existing Document");

        when(documentService.getDocument(documentId)).thenReturn(Optional.of(existingDocument));

        given()
                .auth().preemptive().basic("testUser", "password")
                .when()
                .get("/documents/" + documentId)
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(ContentType.JSON)
                .body("id", is(documentId.toString()))
                .body("title", is("Existing Document"));

        Mockito.verify(documentService).getDocument(documentId);
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"admin", "viewer"})
    void getDocument_adminOrViewerRole_nonExistingDocument_forbidden() {
        UUID documentId = UUID.randomUUID();
        when(documentService.getDocument(documentId)).thenReturn(Optional.empty());

        given()
                .auth().preemptive().basic("testUser", "password")
                .when()
                .get("/documents/" + documentId)
                .then()
                .statusCode(Response.Status.FORBIDDEN.getStatusCode());

        Mockito.verify(documentService).getDocument(documentId);
    }

    @Test
    @TestSecurity(user = "testUser", roles = {}) // No roles
    void getDocument_noRequiredRole_forbidden() {
        UUID documentId = UUID.randomUUID();

        given()
                .auth().preemptive().basic("testUser", "password")
                .when()
                .get("/documents/" + documentId)
                .then()
                .statusCode(Response.Status.FORBIDDEN.getStatusCode());

        verifyNoInteractions(documentService);
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"role"})
    void getDocument_incorrectRole_forbidden() {
        UUID documentId = UUID.randomUUID();

        given()
                .auth().preemptive().basic("testUser", "password")
                .when()
                .get("/documents/" + documentId)
                .then()
                .statusCode(Response.Status.FORBIDDEN.getStatusCode());

        verifyNoInteractions(documentService); // Now correctly verifying the mock
    }
}