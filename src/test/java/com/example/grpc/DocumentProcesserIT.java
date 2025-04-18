package com.example.grpc;

import com.example.dto.DocumentDTO;
import com.example.util.TokenUtil;
import io.grpc.ClientInterceptor;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.MetadataUtils;
import io.quarkus.grpc.GrpcClient;
import io.quarkus.test.junit.QuarkusTest;
import org.acme.grpc.DocumentProcessorGrpc;
import org.acme.grpc.DocumentRequest;
import org.acme.grpc.DocumentResponse;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;


@QuarkusTest
public class DocumentProcesserIT {

    @GrpcClient("document")
    DocumentProcessorGrpc.DocumentProcessorBlockingStub stub;


    @Test
    public void testProcessDocument_withUnauthorizedRole_shouldFail() {
        String uuid = UUID.randomUUID().toString();

        DocumentRequest request = DocumentRequest.newBuilder()
                .setDocumentId(uuid)
                .build();

        String token = "invalid-token";

        try {
            withToken(token).process(request);
            fail("Expected UNAUTHENTICATED or PERMISSION_DENIED");
        } catch (StatusRuntimeException e) {
            assertTrue(
                    e.getStatus().getCode() == Status.Code.UNAUTHENTICATED
            );
        }
    }

    @Test
    public void testProcessDocument_withNotExistDocument() {
        String uuid = UUID.randomUUID().toString();

        DocumentRequest request = DocumentRequest.newBuilder()
                .setDocumentId(uuid)
                .build();

        String token = TokenUtil.getAccessToken("testadmin", "admin@123");

        try {
            withToken(token).process(request);
            fail("Expected INTERNAL for non-existent document");
        } catch (StatusRuntimeException e) {
            assertEquals(Status.Code.INTERNAL, e.getStatus().getCode());
        }
    }

    @Test
    public void testProcessDocument_withAdminRole_shouldSucceed() {

        String token = TokenUtil.getAccessToken("testadmin", "admin@123");
        String documentId = TokenUtil.createDocumentAndReturnId(token);

        DocumentRequest request = DocumentRequest.newBuilder()
                .setDocumentId(documentId)
                .build();

        DocumentResponse response = assertDoesNotThrow(() -> withToken(token).process(request));

        assertNotNull(response);

    }

    private DocumentProcessorGrpc.DocumentProcessorBlockingStub withToken(String token) {
        Metadata metadata = new Metadata();
        Metadata.Key<String> AUTHORIZATION_KEY = Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER);
        metadata.put(AUTHORIZATION_KEY, "Bearer " + token);

        ClientInterceptor interceptor = MetadataUtils.newAttachHeadersInterceptor(metadata);
        return stub.withInterceptors(interceptor);
    }


}
