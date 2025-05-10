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

    @GrpcClient
    DocumentProcessorGrpc.DocumentProcessorBlockingStub stub;


    @Test
    public void testProcessDocument_noTenant() {
        String uuid = UUID.randomUUID().toString();

        DocumentRequest request = DocumentRequest.newBuilder()
                .setDocumentId(uuid)
                .build();

        try {
            stub.process(request);
            fail("something went wrong");
        } catch (StatusRuntimeException e) {
            assertEquals(Status.Code.INTERNAL, e.getStatus().getCode());
        }
    }

    @Test
    public void testProcessDocument_withNotExistDocument() {
        String uuid = UUID.randomUUID().toString();
        stub=TokenUtil.withTenant(stub,uuid);

        DocumentRequest request = DocumentRequest.newBuilder()
                .setDocumentId(uuid)
                .build();


        try {
            stub.process(request);
            fail("Expected INTERNAL for non-existent document");
        } catch (StatusRuntimeException e) {
            assertEquals(Status.Code.INTERNAL, e.getStatus().getCode());
        }
    }

    @Test
    public void testProcessDocument_success() {

        String tenant=UUID.randomUUID().toString();
        String documentId = TokenUtil.createDocumentAndReturnId(tenant);

        stub=TokenUtil.withTenant(stub,tenant);

        DocumentRequest request = DocumentRequest.newBuilder()
                .setDocumentId(documentId)
                .build();

        DocumentResponse response = assertDoesNotThrow(() -> stub.process(request));

        assertNotNull(response);

    }


}
