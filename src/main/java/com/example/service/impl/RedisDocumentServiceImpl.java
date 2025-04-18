package com.example.service.impl;


import com.example.context.SecurityContext;
import com.example.dto.DocumentDTO;
import com.example.exception.CustomException;
import com.example.restclient.CacheService;
import com.example.service.DocumentService;
import io.quarkus.arc.properties.IfBuildProperty;
import io.quarkus.grpc.GrpcClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import jakarta.ws.rs.WebApplicationException;
import org.acme.grpc.document.caching.Document;
import org.acme.grpc.document.caching.DocumentCaching;
import org.acme.grpc.document.caching.DocumentCachingGrpc;
import org.acme.grpc.document.caching.GetDocumentRequest;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.UUID;


@ApplicationScoped
@IfBuildProperty(name = "document.storage.type",stringValue = "redis")
public class RedisDocumentServiceImpl implements DocumentService {

    @Inject
    @RestClient
    CacheService cacheService;

    @GrpcClient("document-caching")
    DocumentCachingGrpc.DocumentCachingBlockingStub documentCachingGrpc;

    @Inject
    SecurityContext securityContext;

    @Override
    public DocumentDTO createDocument(DocumentDTO document) {
        String tenantId= securityContext.getCurrentTenantId();
        document.setTenantId(tenantId);

        Document documentRequest=Document.newBuilder().setContent(document.getContent())
                .setTitle(document.getTitle())
                .setTenantId(tenantId).build();

        DocumentDTO doc=new DocumentDTO();
        try {
            Document response=documentCachingGrpc.create(documentRequest);
            doc.setId(response.getId());
            doc.setContent(response.getContent());
            doc.setTitle(response.getTitle());
            doc.setTenantId(response.getTenantId());
        }
        catch (WebApplicationException webEx)
        {
            throw new CustomException(webEx.getMessage(), webEx.getResponse().getStatus());
        }
        catch (Exception ex)
        {
            throw new CustomException(ex.getMessage(),500);
        }
        return doc;
    }

    @Override
    public DocumentDTO getDocument(UUID id) {
        String tenantId= securityContext.getCurrentTenantId();
        DocumentDTO doc=getDocumentFromRedis(id,UUID.fromString(tenantId));
        return doc;
    }

    @Override
    public String processDocument(String documentId) {
        String tenantId= securityContext.getCurrentTenantId();
        DocumentDTO doc=getDocumentFromRedis(UUID.fromString(documentId),UUID.fromString(tenantId));
        return "document processed";
    }

    private DocumentDTO getDocumentFromRedis(UUID documentId,UUID tenantId)
    {
        GetDocumentRequest request= GetDocumentRequest.newBuilder()
                .setDocumentId(documentId.toString())
                .setTenantId(tenantId.toString()).build();
        DocumentDTO doc=new DocumentDTO();
        try {
            Document response=documentCachingGrpc.getDocument(request);
            doc.setId(response.getId());
            doc.setContent(response.getContent());
            doc.setTitle(response.getTitle());
            doc.setTenantId(response.getTenantId());
        }
        catch (WebApplicationException webEx)
        {
            throw new CustomException(webEx.getMessage(), webEx.getResponse().getStatus());
        }
        catch (Exception ex)
        {
            throw new CustomException(ex.getMessage(),500);
        }

        return doc;
    }

}
