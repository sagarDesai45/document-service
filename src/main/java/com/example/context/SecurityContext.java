package com.example.context;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.List;

@RequestScoped
public class SecurityContext {

    @Inject
    SecurityIdentity securityIdentity;

    @Inject
    JsonWebToken jwt;

    public String getCurrentTenantId() {
        // First try to get as string
        String tenantId = jwt.getClaim("tenant_id");
        if (tenantId != null) {
            return tenantId;
        }

        // Try to get as list (Keycloak sometimes stores attributes as lists)
        List<String> tenantIds = jwt.getClaim("tenant_id");
        if (tenantIds != null && !tenantIds.isEmpty()) {
            return tenantIds.get(0);
        }

        return null;
    }

    public boolean hasRole(String role) {
        return securityIdentity.hasRole(role);
    }
}