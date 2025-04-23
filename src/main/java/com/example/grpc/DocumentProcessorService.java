package com.example.grpc;


import com.example.context.TenantIdInterceptor;
import com.example.service.DocumentService;
import io.grpc.Status;
import io.quarkus.grpc.GrpcService;
import io.quarkus.grpc.RegisterInterceptor;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.annotation.security.RolesAllowed;

import jakarta.inject.Inject;
import jakarta.ws.rs.HeaderParam;
import org.acme.grpc.DocumentProcessor;

import org.acme.grpc.DocumentRequest;
import org.acme.grpc.DocumentResponse;





@GrpcService
@RegisterInterceptor(TenantIdInterceptor.class)
public class DocumentProcessorService  implements DocumentProcessor {

    @Inject
    DocumentService documentService;


    @Override
    public Uni<DocumentResponse> process(DocumentRequest request) {
        String tenantId = TenantIdInterceptor.TENANT_ID_CTX_KEY.get();

                    return Uni.createFrom().item(() -> request)
                            .emitOn(Infrastructure.getDefaultWorkerPool())
                            .map(req -> {
                                try {

                                    String status = documentService.processDocument(req.getDocumentId(),tenantId);
                                    return DocumentResponse.newBuilder()
                                            .setStatus(status)
                                            .build();
                                } catch (SecurityException e) {
                                    System.out.println("Security Exception: " + e.getMessage());
                                    throw Status.PERMISSION_DENIED
                                            .withDescription(e.getMessage())
                                            .asRuntimeException();
                                } catch (Exception e) {
                                    System.out.println("General Exception: " + e.getMessage());
                                    throw Status.INTERNAL
                                            .withDescription(e.getMessage())
                                            .asRuntimeException();
                                }
                            });

    }

}



