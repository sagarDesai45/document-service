package com.example.restclient;

import com.example.dto.DocumentDTO;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.UUID;

@Path("/cache")
@RegisterRestClient(configKey = "document-caching")
public interface CacheService {

    @POST
    DocumentDTO createDocument(DocumentDTO  document);

    @GET
    DocumentDTO getDocument(@QueryParam("documentId") UUID  documentId,@QueryParam("tenantId") UUID tenantId);

}
