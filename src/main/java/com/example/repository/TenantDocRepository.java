package com.example.repository;

import com.example.model.TenantDocument;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TenantDocRepository implements PanacheRepository<TenantDocument> {
}
