package com.example.service.impl;


import com.example.context.SecurityContext;
import com.example.dto.DocumentDTO;
import com.example.exception.CustomException;
import com.example.restclient.CacheService;
import com.example.service.DocumentService;
import io.quarkus.arc.properties.IfBuildProperty;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import jakarta.ws.rs.WebApplicationException;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.UUID;


@ApplicationScoped
@IfBuildProperty(name = "document.storage.type",stringValue = "redis")
public class RedisDocumentServiceImpl implements DocumentService {

    @Inject
    @RestClient
    CacheService cacheService;

    @Inject
    SecurityContext securityContext;

    @Override
    public DocumentDTO createDocument(DocumentDTO document) {
        String tenantId= securityContext.getCurrentTenantId();
        document.setTenantId(tenantId);
        DocumentDTO doc;
        try {
            doc=cacheService.createDocument(document);
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
        DocumentDTO doc;
        try {
            doc=cacheService.getDocument(documentId,tenantId);
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
