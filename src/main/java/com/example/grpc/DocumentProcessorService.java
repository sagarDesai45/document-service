package com.example.grpc;


import com.example.service.DocumentService;
import io.grpc.Status;
import io.quarkus.grpc.GrpcService;
import io.quarkus.security.identity.CurrentIdentityAssociation;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.annotation.security.RolesAllowed;

import jakarta.inject.Inject;
import org.acme.grpc.DocumentProcessor;

import org.acme.grpc.DocumentRequest;
import org.acme.grpc.DocumentResponse;





@GrpcService
public class DocumentProcessorService  implements DocumentProcessor {

    @Inject
    DocumentService documentService;


    @Override
    @RolesAllowed({"admin","viewer"})
    public Uni<DocumentResponse> process(DocumentRequest request) {

                    return Uni.createFrom().item(() -> request)
                            .emitOn(Infrastructure.getDefaultWorkerPool())
                            .map(req -> {
                                try {

                                    String status = documentService.processDocument(req.getDocumentId());
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



