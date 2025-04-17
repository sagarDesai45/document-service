package com.example.util;

import com.example.dto.DocumentDTO;
import io.restassured.RestAssured;
import io.restassured.response.Response;

public class TokenUtil {
    public static String getAccessToken(String username, String password) {
        Response response = RestAssured.given()
                .baseUri("http://localhost:8180")
                .basePath("/realms/secure-docs/protocol/openid-connect/token")
                .contentType("application/x-www-form-urlencoded")
                .formParam("client_id", "secure-docs-client")
                .formParam("client_secret", "0w39aIdj7RloFBLqWrIxqGXOZ8HrO1O7")
                .formParam("grant_type", "password")
                .formParam("username", username)
                .formParam("password", password)
                .post();

        return response.jsonPath().getString("access_token");
    }

    public static String createDocumentAndReturnId(String token) {

        DocumentDTO doc = new DocumentDTO();
        doc.setTitle("Test gRPC");
        doc.setContent("Test gRPC Content");

        return RestAssured.given()
                .header("Authorization", "Bearer " + token)
                .contentType(io.restassured.http.ContentType.JSON)
                .body(doc)
                .when()
                .post("/documents")
                .then()
                .statusCode(201)
                .extract()
                .path("id");
    }
}
