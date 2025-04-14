package com.example.rest;

import com.example.model.Document;
import com.example.service.DocumentService;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
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
@Authenticated
public class RestController {

    @Inject
    private DocumentService documentService;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed("admin")
    public Response createDocument(Document document) {
        if (document == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid JSON payload").build();
        }
        Document createdDocument = documentService.createDocument(document);
        return Response.status(Response.Status.CREATED).entity(createdDocument).build();
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({"admin","viewer"})
    public Response getDocument(@PathParam("id") UUID id) {
        return documentService.getDocument(id)
                .map(Response::ok)
                .orElse(Response.status(Response.Status.FORBIDDEN))
                .build();
    }
}
