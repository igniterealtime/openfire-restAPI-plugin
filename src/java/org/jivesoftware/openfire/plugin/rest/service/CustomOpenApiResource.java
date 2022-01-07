/*
 * Copyright (C) 2022 Ignite Realtime Foundation. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jivesoftware.openfire.plugin.rest.service;

import io.swagger.v3.jaxrs2.integration.resources.BaseOpenApiResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.container.PluginMetadataHelper;
import org.jivesoftware.openfire.plugin.rest.RESTServicePlugin;

import javax.servlet.ServletConfig;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.*;
import java.util.Collections;

/**
 * Configuration for the REST API.
 *
 * @author Guus der Kinderen, guus.der.kinderen@gmail.com
 */
@Path("restapi/v1/")
public class CustomOpenApiResource extends BaseOpenApiResource {
    @Context
    ServletConfig config;

    @Context
    Application app;

    public CustomOpenApiResource() {
        final RESTServicePlugin plugin = (RESTServicePlugin) XMPPServer.getInstance().getPluginManager().getPluginByName("REST API").orElse(null);
        final String version = plugin != null ? PluginMetadataHelper.getVersion(plugin).getVersionString() : "(unknown)";

        openApiConfiguration = new SwaggerConfiguration();

        final OpenAPI openAPI = new OpenAPI()
            // 'server' is needed to be able to add the additional '/plugins' context root.
            .servers(Collections.singletonList(new Server().url("/plugins")))

            .info(new Info()
                .description("This is the documentation for a REST API of the Openfire Real-time communication server.")
                .title("Openfire REST API")
                .contact(new Contact()
                    .name("Ignite Realtime Foundation")
                    .url("https://www.igniterealtime.org")
                )
                .version(version)
                .license(new License()
                    .name("Apache 2.0")
                    .url("http://www.apache.org/licenses/LICENSE-2.0.html"))
            );

        openAPI.components(new Components());

        final String key;
        if (plugin == null || !"basic".equals(plugin.getHttpAuth())) {
            key = "Secret key auth";
            final SecurityScheme apiKeyScheme = new SecurityScheme();
            apiKeyScheme.setDescription("Authenticate using the Secret Key as configured in the Openfire admin console.");
            apiKeyScheme.setType(SecurityScheme.Type.APIKEY);
            apiKeyScheme.setName("Authorization");
            apiKeyScheme.setIn(SecurityScheme.In.HEADER);
            openAPI.getComponents().addSecuritySchemes(key, apiKeyScheme);
        } else {
            key = "Admin Console account";
            final SecurityScheme basicAuthScheme = new SecurityScheme();
            basicAuthScheme.setDescription("Authenticate using a valid admin account for Openfire admin console.");
            basicAuthScheme.setType(SecurityScheme.Type.HTTP);
            basicAuthScheme.setScheme("basic");
            openAPI.getComponents().addSecuritySchemes(key, basicAuthScheme);
        }
        final SecurityRequirement securityItem = new SecurityRequirement();
        securityItem.addList(key);
        openAPI.addSecurityItem(securityItem);

        ((SwaggerConfiguration)openApiConfiguration).openAPI(openAPI);
    }

    @GET
    @Path("openapi.yaml")
    @Produces({"application/yaml"})
    @Operation(hidden = true)
    public Response getOpenApiYaml(@Context HttpHeaders headers,
                               @Context UriInfo uriInfo) throws Exception {

        return super.getOpenApi(headers, config, app, uriInfo, "yaml");
    }

    @GET
    @Path("openapi.json")
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(hidden = true)
    public Response getOpenApiJson(@Context HttpHeaders headers,
                               @Context UriInfo uriInfo) throws Exception {

        return super.getOpenApi(headers, config, app, uriInfo, "json");
    }
}
