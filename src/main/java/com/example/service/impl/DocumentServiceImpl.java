package com.example.service.impl;
import com.example.context.SecurityContext;
import com.example.dto.DocumentDTO;
import com.example.exception.CustomException;
import com.example.mapper.DocumentMapper;
import com.example.model.Document;
import com.example.model.Tenant;
import com.example.model.TenantDocument;
import com.example.repository.DocumentRepository;
import com.example.repository.TenantDocRepository;
import com.example.repository.TenantRepository;
import com.example.service.DocumentService;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.quarkus.arc.DefaultBean;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.ForbiddenException;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
@DefaultBean
public class DocumentServiceImpl implements DocumentService {

    @Inject
    SecurityContext securityContext;

    @Inject
    DocumentMapper documentMapper;

    @Inject
    DocumentRepository documentRepository;

    @Inject
    TenantDocRepository tenantDocRepository;

    @Inject
    TenantRepository tenantRepository;



    @Transactional
    public DocumentDTO createDocument(DocumentDTO document) {

        Tenant tenant = getValidTentant();

        Document newDoc=Document.builder().title(document.getTitle())
                .content(document.getContent()).build();

        documentRepository.persist(newDoc);

        TenantDocument tenantDocument=TenantDocument.builder().tenant(tenant).document(newDoc).build();

        tenantDocRepository.persist(tenantDocument);
        return documentMapper.toDto(newDoc);
    }

    public DocumentDTO getDocument(UUID id) {

        Tenant tenant = getValidTentant();
        Document document=getDocumentFromDb(id,tenant.getId());
        return documentMapper.toDto(document);
    }

    public String processDocument(String documentId) {

        Tenant tenant = getValidTentant();
        Document document=getDocumentFromDb(UUID.fromString(documentId),tenant.getId());
        return "Document Processed";
    }

    private Tenant getValidTentant()
    {

        String tenantId = securityContext.getCurrentTenantId();
        if (Objects.isNull(tenantId) || tenantId.isEmpty()) {
            throw new RuntimeException("Something went wrong");
        }
        UUID tenantUUID=UUID.fromString(tenantId);
        Optional<Tenant> optTenant=tenantRepository.findByIdOptional(tenantUUID);

        // if tenant presernt return it
        if(optTenant.isPresent())
        {
            return optTenant.get();
        }

        //if tenant not preset create new one
        Tenant newTenant=
                Tenant.builder().id(tenantUUID).name("New Tenant").build();

        tenantRepository.persist(newTenant);
        return newTenant;
    }

    private Document getDocumentFromDb(UUID documentId,UUID tenantId)
    {
        List<TenantDocument> tenantDocs= tenantDocRepository.list( "document.id = ?1 ",documentId);

        if(tenantDocs.isEmpty())
        {
            throw new CustomException("Document not found", HttpResponseStatus.BAD_REQUEST.code());
        }

        TenantDocument tenatDoc=
                tenantDocs.stream().filter(tenantDocument -> tenantId.equals(tenantDocument.getTenant().getId()))
                        .findFirst().orElseThrow(()-> new CustomException("You don't have access",
                                HttpResponseStatus.FORBIDDEN.code()));

        return tenatDoc.getDocument();
    }

}
