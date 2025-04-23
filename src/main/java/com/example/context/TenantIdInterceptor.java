package com.example.context;

import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TenantIdInterceptor implements ServerInterceptor {

    public static final Context.Key<String> TENANT_ID_CTX_KEY = Context.key("X-Tenant-id");
    public static final Metadata.Key<String> TENANT_ID_HEADER =
            Metadata.Key.of("X-Tenant-Id", Metadata.ASCII_STRING_MARSHALLER);

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        String tenantId = headers.get(TENANT_ID_HEADER);
        Context ctx = Context.current().withValue(TENANT_ID_CTX_KEY, tenantId);

        return Contexts.interceptCall(ctx, call, headers, next);
    }
}
