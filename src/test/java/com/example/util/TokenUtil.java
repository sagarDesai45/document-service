package com.example.util;

import com.example.dto.DocumentDTO;
import io.grpc.ClientInterceptor;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.acme.grpc.DocumentProcessorGrpc;

public class TokenUtil {

    public static String createDocumentAndReturnId(String tenantId) {

        DocumentDTO doc = new DocumentDTO();
        doc.setTitle("Test gRPC");
        doc.setContent("Test gRPC Content");

        return RestAssured.given()
                .header("X-Tenant-Id", tenantId )
                .contentType(io.restassured.http.ContentType.JSON)
                .body(doc)
                .when()
                .post("/documents")
                .then()
                .statusCode(201)
                .extract()
                .path("id");
    }

    public static DocumentProcessorGrpc.DocumentProcessorBlockingStub withTenant(
            DocumentProcessorGrpc.DocumentProcessorBlockingStub blockingStub,String tenantId) {
        Metadata metadata = new Metadata();
        Metadata.Key<String> TENANT_HEADER = Metadata.Key.of("X-Tenant-Id", Metadata.ASCII_STRING_MARSHALLER);
        metadata.put(TENANT_HEADER, tenantId);

        ClientInterceptor interceptor = MetadataUtils.newAttachHeadersInterceptor(metadata);

        return blockingStub.withInterceptors(interceptor);
    }
}
