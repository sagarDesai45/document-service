package com.example.rest;

import com.example.dto.DocumentDTO;
import com.example.util.TokenUtil;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@QuarkusTest
public class RestControllerIT {



    @Test
    void testCreateDocument_success() {

        DocumentDTO doc = new DocumentDTO();
        doc.setTitle("Integration Test Doc");
        doc.setContent("This is a test document.");
        String tenantId=UUID.randomUUID().toString();

        given()
                .header("X-Tenant-Id", tenantId)
                .contentType(ContentType.JSON)
                .body(doc)
                .when()
                .post("/documents")
                .then()
                .statusCode(201)
                .body("title", equalTo("Integration Test Doc"));
    }

    @Test
    void testGetDocument_success() {
        String tenantId=UUID.randomUUID().toString();
        String docId=TokenUtil.createDocumentAndReturnId(tenantId);
        UUID documentId = UUID.fromString(docId);

        given()
                .header("X-Tenant-Id", tenantId)
                .when()
                .get("/documents/" + documentId)
                .then()
                .statusCode(200)
                .body("id", equalTo(documentId.toString()));
    }

    @Test
    void testGetDocument_notSuccess() {

        UUID documentId = UUID.fromString("96c4a993-faa6-4000-a259-e1e327d4d725");

        given()
                .header("X-Tenant-Id", UUID.randomUUID().toString())
                .when()
                .get("/documents/" + documentId)
                .then()
                .statusCode(500);
    }

}
