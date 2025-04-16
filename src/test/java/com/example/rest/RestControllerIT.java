package com.example.rest;

import com.example.dto.DocumentDTO;
import com.example.util.TokenUtil;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@QuarkusTest
public class RestControllerIT {

    @Test
    void testCreateDocument_withAdminRole_shouldSucceed() {
        String token = TokenUtil.getAccessToken("testadmin", "admin@123");

        DocumentDTO doc = new DocumentDTO();
        doc.setTitle("Integration Test Doc");
        doc.setContent("This is a test document.");

        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(doc)
                .when()
                .post("/documents")
                .then()
                .statusCode(201)
                .body("title", equalTo("Integration Test Doc"));
    }

    @Test
    void testGetDocument_withViewerRole_shouldSucceed() {

        UUID documentId = UUID.fromString("96c4a993-faa6-4000-a259-e1e327d4d725");

        String token = TokenUtil.getAccessToken("testadmin", "admin@123");

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/documents/" + documentId)
                .then()
                .statusCode(200)
                .body("id", equalTo(documentId.toString()));
    }

    @Test
    void testGetDocument_withDifferentTenant_shouldNotSuccess() {

        UUID documentId = UUID.fromString("96c4a993-faa6-4000-a259-e1e327d4d725");

        String token = TokenUtil.getAccessToken("testviewer", "admin@123");

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/documents/" + documentId)
                .then()
                .statusCode(500);
    }

    @Test
    void testCreateDocument_withoutToken_shouldFail() {
        DocumentDTO doc = new DocumentDTO();
        doc.setTitle("Unauthorized Doc");
        doc.setContent("No token here");

        given()
                .contentType(ContentType.JSON)
                .body(doc)
                .when()
                .post("/documents")
                .then()
                .statusCode(401);
    }
}
