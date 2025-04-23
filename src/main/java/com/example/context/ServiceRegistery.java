package com.example.context;

import io.quarkus.runtime.StartupEvent;
import io.vertx.ext.consul.ConsulClientOptions;
import io.vertx.ext.consul.ServiceOptions;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.consul.ConsulClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class ServiceRegistery {

    @ConfigProperty(name = "consul.host",defaultValue = "localhost") String host;
    @ConfigProperty(name = "consul.port",defaultValue = "8500") int port;

    @ConfigProperty(name = "quarkus.http.port", defaultValue = "8080") int documentPort;
    @ConfigProperty(name = "quarkus.application.name", defaultValue = "document-service") String documentService;

    public void init(@Observes StartupEvent ev, Vertx vertx) {

        ConsulClient client = ConsulClient.create(Vertx.vertx(),
                new ConsulClientOptions().setHost(host).setPort(port));

        client.registerServiceAndAwait(
                new ServiceOptions().setPort(documentPort).setAddress("localhost")
                        .setName(documentService).setId(documentService));

    }

}
