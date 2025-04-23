package com.example.rest;

import com.example.dto.DocumentDTO;
import com.example.service.DocumentService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

@Path("/documents")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class RestController {

    @Inject
    private DocumentService documentService;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createDocument(@HeaderParam("X-Tenant-Id") String tenantId, @Valid DocumentDTO document) {
        if (document == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid JSON payload").build();
        }
        DocumentDTO createdDocument = documentService.createDocument(document,tenantId);
        return Response.status(Response.Status.CREATED).entity(createdDocument).build();
    }

    @GET
    @Path("/{id}")
    public Response getDocument(@PathParam("id") UUID id,@HeaderParam("X-Tenant-Id") String tenantId) {
        DocumentDTO docDTO=documentService.getDocument(id,tenantId);
        return Response.status(Response.Status.OK).entity(docDTO).build();

    }
}
