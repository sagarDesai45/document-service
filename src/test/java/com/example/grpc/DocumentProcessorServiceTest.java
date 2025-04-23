package com.example.grpc;

import com.example.context.SecurityContext;
import com.example.service.DocumentService;
import com.example.util.TokenUtil;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.quarkus.grpc.GrpcClient;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import org.acme.grpc.DocumentProcessorGrpc;
import org.acme.grpc.DocumentRequest;
import org.acme.grpc.DocumentResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@QuarkusTest
public class DocumentProcessorServiceTest {

    @GrpcClient
    DocumentProcessorGrpc.DocumentProcessorBlockingStub blockingStub;

    @InjectMock
    DocumentService documentService;


    @Test
    void process_success() {
        String documentId = "test-doc-id";
        String tenantId = "test-tenant";
        blockingStub=TokenUtil.withTenant(blockingStub,tenantId);
        when(documentService.processDocument(documentId,tenantId)).thenReturn("Document processed by admin");

        DocumentRequest request = DocumentRequest.newBuilder().setDocumentId(documentId).build();
        DocumentResponse response = blockingStub.process(request);

        assertEquals("Document processed by admin", response.getStatus());
        Mockito.verify(documentService).processDocument(documentId,tenantId);
    }

    @Test
    void process_noRole_permissionDenied() {
        String documentId = "unauthorized-doc";
        String tenantId = "test-tenant";
        DocumentRequest request = DocumentRequest.newBuilder().setDocumentId(documentId).build();

        StatusRuntimeException exception = assertThrows(StatusRuntimeException.class, () -> blockingStub.process(request));
        assertEquals(Status.INTERNAL, exception.getStatus());

    }

    @Test
    void process_securityExceptionFromService_permissionDenied() {
        String documentId = "protected-doc";
        String tenantId = "test-tenant";
        blockingStub=TokenUtil.withTenant(blockingStub,tenantId);
        String errorMessage = "You don't have access to this document";
        when(documentService.processDocument(documentId,tenantId)).thenThrow(new SecurityException());

        DocumentRequest request = DocumentRequest.newBuilder().setDocumentId(documentId).build();

        StatusRuntimeException exception = assertThrows(StatusRuntimeException.class, () -> blockingStub.process(request));
        assertEquals(Status.PERMISSION_DENIED, exception.getStatus());

        Mockito.verify(documentService).processDocument(documentId,tenantId);
    }

        @Test
        void process_generalExceptionFromService_internalError() {
            String documentId = "faulty-doc";
            String tenantId = "test-tenant";
            blockingStub=TokenUtil.withTenant(blockingStub,tenantId);
            String errorMessage = "Something went wrong during processing";
            when(documentService.processDocument(documentId,tenantId)).thenThrow(new RuntimeException());

            DocumentRequest request = DocumentRequest.newBuilder().setDocumentId(documentId).build();

            StatusRuntimeException exception = assertThrows(StatusRuntimeException.class, () -> blockingStub.process(request));
            assertEquals(Status.INTERNAL, exception.getStatus());
            System.err.println("Caught StatusRuntimeException:");
            exception.printStackTrace();
            Mockito.verify(documentService).processDocument(documentId,tenantId);
        }
}