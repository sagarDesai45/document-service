package com.example.service.impl;


import com.example.context.SecurityContext;
import com.example.dto.DocumentDTO;
import com.example.exception.CustomException;
import com.example.restclient.CacheService;
import com.example.service.DocumentService;
import io.netty.util.internal.StringUtil;
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


    @Override
    public DocumentDTO createDocument(DocumentDTO document,String tenantId) {
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
    public DocumentDTO getDocument(UUID id,String tenantId) {
        DocumentDTO doc=getDocumentFromRedis(id,tenantId);
        return doc;
    }

    @Override
    public String processDocument(String documentId,String tenantId) {
        DocumentDTO doc=getDocumentFromRedis(UUID.fromString(documentId),tenantId);
        return "document processed";
    }

    private DocumentDTO getDocumentFromRedis(UUID documentId,String xTenantId)
    {
        if(StringUtil.isNullOrEmpty(xTenantId))
        {
            throw new RuntimeException("Something went wrong");
        }

        UUID tenantId= UUID.fromString(xTenantId);

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
