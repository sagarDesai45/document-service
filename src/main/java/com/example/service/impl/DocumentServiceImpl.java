package com.example.service.impl;
import com.example.context.SecurityContext;
import com.example.model.Document;
import com.example.model.Tenant;
import com.example.service.DocumentService;
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
public class DocumentServiceImpl implements DocumentService {

    private final Map<UUID, Document> documentStore = new ConcurrentHashMap<>();
    private final Map<String, List<UUID>> tenantDocuments = new ConcurrentHashMap<>();


    @Inject
    SecurityContext securityContext;


    public Document createDocument(Document document) {

        Tenant tenant = getValidTentant();

        UUID id = UUID.randomUUID();
        document.setId(id.toString());
        documentStore.put(id, document);

        // Associate document with tenant
        tenantDocuments.computeIfAbsent(tenant.getId(), k -> new ArrayList<>()).add(id);

        return document;
    }

    public Optional<Document> getDocument(UUID id) {

        Tenant tenant = getValidTentant();

        // Check if the document is associated with the tenant
        List<UUID> tenantDocIds = tenantDocuments.getOrDefault(tenant.getId(), Collections.emptyList());
        if (tenantDocIds.contains(id)) {
            return Optional.ofNullable(documentStore.get(id));
        }

        return Optional.empty();
    }

    public String processDocument(String documentId) {

        Tenant tenant = getValidTentant();


        // Check if the document is associated with the tenant
        List<UUID> tenantDocIds = tenantDocuments.getOrDefault(tenant.getId(), Collections.emptyList());
        if (tenantDocIds.contains(UUID.fromString(documentId))) {
            return "Document processed";
        }

        throw new SecurityException("You don't have access");
    }

    private Tenant getValidTentant()
    {

        String tenantId = securityContext.getCurrentTenantId();
        if (Objects.isNull(tenantId) || tenantId.isEmpty()) {
            throw new RuntimeException("Something went wrong");
        }

        return new Tenant(tenantId,"New Tentant");
    }

}
