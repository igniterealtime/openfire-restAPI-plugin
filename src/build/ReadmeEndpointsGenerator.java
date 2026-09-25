/*
 * Copyright (C) 2026 Ignite Realtime Foundation. All rights reserved.
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

import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.tags.Tag;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Generates the endpoint documentation in readme.md from the OpenAPI specification, which in turn is generated from the
 * OpenAPI annotations in the source code. This keeps the endpoint documentation in sync with the implementation.
 *
 * The generated documentation replaces all text between the {@link #BEGIN_MARKER} and {@link #END_MARKER} lines in the
 * readme. Text outside of these markers is not modified.
 *
 * This is a single-file Java program that is executed by the Maven build. It is not part of the plugin.
 *
 * Usage: {@code java -cp <classpath> ReadmeEndpointsGenerator.java <openapi.json> <readme.md>}
 */
public class ReadmeEndpointsGenerator
{
    static final String BEGIN_MARKER = "<!-- BEGIN GENERATED ENDPOINTS";
    static final String END_MARKER = "<!-- END GENERATED ENDPOINTS -->";

    /** The prefix under which the REST API is exposed, relative to the root of the Openfire admin console. */
    static final String CONTEXT_ROOT = "/plugins";

    /** Order in which tags are documented. Tags that are not listed here are documented after these, alphabetically. */
    static final List<String> TAG_ORDER = List.of("Users", "User Group", "Chat service", "Chat room", "Client Sessions", "Message", "Message Archive", "Security Audit Log", "Statistics", "System", "Clustering");

    /** Responses that apply to (nearly) all endpoints. These are documented once, instead of for each endpoint. */
    static final Map<String, String> GENERIC_RESPONSES = Map.of(
        "401", "Web service authentication failed.",
        "500", "Unexpected, generic error condition."
    );

    static final List<PathItem.HttpMethod> METHOD_ORDER = List.of(PathItem.HttpMethod.GET, PathItem.HttpMethod.POST, PathItem.HttpMethod.PUT, PathItem.HttpMethod.PATCH, PathItem.HttpMethod.DELETE, PathItem.HttpMethod.HEAD, PathItem.HttpMethod.OPTIONS, PathItem.HttpMethod.TRACE);

    record Endpoint(String path, PathItem.HttpMethod method, Operation operation) {}

    public static void main(String[] args) throws Exception
    {
        if (args.length != 2) {
            System.err.println("Usage: java ReadmeEndpointsGenerator.java <openapi.json> <readme.md>");
            System.exit(1);
        }
        final Path specFile = Path.of(args[0]);
        final Path readmeFile = Path.of(args[1]);

        final OpenAPI openAPI = Json.mapper().readValue(specFile.toFile(), OpenAPI.class);
        final String generated = generate(openAPI);

        final String readme = Files.readString(readmeFile, StandardCharsets.UTF_8);
        final int begin = readme.indexOf(BEGIN_MARKER);
        final int end = readme.indexOf(END_MARKER);
        if (begin < 0 || end < begin) {
            throw new IllegalStateException("Unable to find the '" + BEGIN_MARKER + "' and '" + END_MARKER + "' markers in " + readmeFile);
        }
        final int beginLineEnd = readme.indexOf('\n', begin) + 1;
        final String updated = readme.substring(0, beginLineEnd) + "\n" + generated.strip() + "\n\n" + readme.substring(end);

        if (updated.equals(readme)) {
            System.out.println("Endpoint documentation in " + readmeFile + " is up to date.");
        } else {
            Files.writeString(readmeFile, updated, StandardCharsets.UTF_8);
            System.out.println("Updated the endpoint documentation in " + readmeFile + ".");
        }
    }

    static String generate(final OpenAPI openAPI)
    {
        // Group all endpoints by their (first) tag.
        final Map<String, List<Endpoint>> endpointsByTag = new TreeMap<>(Comparator.comparingInt((String tag) -> TAG_ORDER.contains(tag) ? TAG_ORDER.indexOf(tag) : TAG_ORDER.size()).thenComparing(Comparator.naturalOrder()));
        openAPI.getPaths().forEach((path, pathItem) -> pathItem.readOperationsMap().forEach((method, operation) -> {
            final String tag = operation.getTags() == null || operation.getTags().isEmpty() ? "Other" : operation.getTags().get(0);
            endpointsByTag.computeIfAbsent(tag, t -> new ArrayList<>()).add(new Endpoint(path, method, operation));
        }));

        final Map<String, String> tagDescriptions = new LinkedHashMap<>();
        if (openAPI.getTags() != null) {
            for (final Tag tag : openAPI.getTags()) {
                tagDescriptions.put(tag.getName(), tag.getDescription());
            }
        }

        final StringBuilder out = new StringBuilder();
        out.append("# REST Endpoints\n\n");
        out.append("The paths of all endpoints below are relative to the root of the Openfire admin console, for example `http://example.org:9090`.\n\n");
        out.append("In addition to the responses that are documented for each endpoint, every endpoint can respond with:\n\n");
        GENERIC_RESPONSES.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(e -> out.append("- `").append(e.getKey()).append("`: ").append(e.getValue()).append("\n"));
        out.append("\n");
        out.append("Interactive documentation of these endpoints is available in the Openfire admin console, via the link on the REST API settings page (Server > Server Settings > REST API).\n");

        endpointsByTag.forEach((tag, endpoints) -> {
            out.append("\n# ").append(tag).append("\n\n");
            final String tagDescription = tagDescriptions.get(tag);
            if (tagDescription != null && !tagDescription.isBlank()) {
                out.append(tagDescription.trim()).append("\n");
            }

            // Keep endpoints for the same resource (sharing the first path segment after the API prefix) together, and
            // document resources that have the most general (shortest) paths first.
            final Map<String, Integer> resourceDepth = new TreeMap<>();
            endpoints.forEach(e -> resourceDepth.merge(resource(e.path()), depth(e.path()), Math::min));
            endpoints.sort(Comparator.comparingInt((Endpoint e) -> resourceDepth.get(resource(e.path())))
                .thenComparing(e -> resource(e.path()))
                .thenComparing(Endpoint::path)
                .thenComparingInt(e -> METHOD_ORDER.indexOf(e.method())));
            for (final Endpoint endpoint : endpoints) {
                appendEndpoint(out, endpoint);
            }
        });
        return out.toString().replaceAll("\n{3,}", "\n\n");
    }

    static void appendEndpoint(final StringBuilder out, final Endpoint endpoint)
    {
        final Operation operation = endpoint.operation();
        out.append("\n## ").append(Optional.ofNullable(operation.getSummary()).orElse(endpoint.method() + " " + endpoint.path())).append("\n\n");
        out.append("> **").append(endpoint.method()).append("** ").append(CONTEXT_ROOT).append(endpoint.path()).append("\n\n");
        if (Boolean.TRUE.equals(operation.getDeprecated())) {
            out.append("**Deprecated:** this endpoint may be removed in a future version.\n\n");
        }
        if (operation.getDescription() != null && !operation.getDescription().isBlank()) {
            out.append(operation.getDescription().trim()).append("\n\n");
        }

        final List<Parameter> parameters = Optional.ofNullable(operation.getParameters()).orElse(List.of());
        if (!parameters.isEmpty()) {
            out.append("**Parameters**\n\n");
            out.append("| Name | Located in | Required | Description | Default value |\n");
            out.append("|------|------------|----------|-------------|---------------|\n");
            for (final Parameter parameter : parameters) {
                String description = Optional.ofNullable(parameter.getDescription()).orElse("");
                if (parameter.getExample() != null) {
                    description = (description.isBlank() ? "" : description.trim() + " ") + "Example: `" + parameter.getExample() + "`";
                } else if (parameter.getExamples() != null && !parameter.getExamples().isEmpty()) {
                    final List<String> examples = new ArrayList<>();
                    parameter.getExamples().values().forEach(example -> examples.add("`" + example.getValue() + "`" + (example.getDescription() == null || example.getDescription().isBlank() ? "" : " (" + example.getDescription().trim() + ")")));
                    description = (description.isBlank() ? "" : description.trim() + " ") + "Examples: " + String.join(", ", examples);
                }
                final Object defaultValue = parameter.getSchema() != null ? parameter.getSchema().getDefault() : null;
                out.append("| ").append(parameter.getName())
                    .append(" | ").append(parameter.getIn())
                    .append(" | ").append(Boolean.TRUE.equals(parameter.getRequired()) ? "yes" : "no")
                    .append(" | ").append(cell(description))
                    .append(" | ").append(defaultValue == null ? "" : "`" + cell(defaultValue.toString()) + "`")
                    .append(" |\n");
            }
            out.append("\n");
        }

        final RequestBody requestBody = operation.getRequestBody();
        if (requestBody != null) {
            out.append("**Request body**");
            out.append(Boolean.TRUE.equals(requestBody.getRequired()) ? " (required)" : " (optional)");
            out.append(": ").append(describeContent(requestBody.getContent()));
            if (requestBody.getDescription() != null && !requestBody.getDescription().isBlank()) {
                out.append(" - ").append(requestBody.getDescription().trim());
            }
            out.append("\n\n");
        }

        if (operation.getResponses() != null) {
            final Map<String, ApiResponse> responses = new TreeMap<>(operation.getResponses());
            responses.entrySet().removeIf(e -> Objects.equals(GENERIC_RESPONSES.get(e.getKey()), e.getValue().getDescription()));
            if (!responses.isEmpty()) {
                out.append("**Responses**\n\n");
                out.append("| Status | Description | Response body |\n");
                out.append("|--------|-------------|---------------|\n");
                responses.forEach((status, response) -> out.append("| ").append(status)
                    .append(" | ").append(cell(Optional.ofNullable(response.getDescription()).orElse("")))
                    .append(" | ").append(response.getContent() == null || response.getContent().isEmpty() ? "" : describeContent(response.getContent()))
                    .append(" |\n"));
                out.append("\n");
            }
        }
    }

    /** Describes the data type of content, for example "`UserEntity` (XML or JSON)". */
    static String describeContent(final Content content)
    {
        if (content == null || content.isEmpty()) {
            return "none";
        }
        final List<String> formats = new ArrayList<>();
        String type = null;
        for (final Map.Entry<String, MediaType> entry : content.entrySet()) {
            final String format = switch (entry.getKey()) {
                case "application/xml" -> "XML";
                case "application/json" -> "JSON";
                case "text/plain" -> "plain text";
                case "*/*" -> null;
                default -> entry.getKey();
            };
            if (format != null) {
                formats.add(format);
            }
            if (type == null && entry.getValue().getSchema() != null) {
                type = describeSchema(entry.getValue().getSchema());
            }
        }
        final StringBuilder result = new StringBuilder(type == null ? "unspecified" : type);
        if (!formats.isEmpty()) {
            result.append(" (").append(String.join(" or ", formats)).append(")");
        }
        return result.toString();
    }

    @SuppressWarnings("rawtypes")
    static String describeSchema(final Schema schema)
    {
        if (schema.get$ref() != null) {
            return "`" + schema.get$ref().substring(schema.get$ref().lastIndexOf('/') + 1) + "`";
        }
        if ("array".equals(schema.getType()) && schema.getItems() != null) {
            return "array of " + describeSchema(schema.getItems());
        }
        return schema.getType() == null ? "unspecified" : schema.getType();
    }

    /** Returns the path without the API prefix, for example "users/{username}" for "/restapi/v1/users/{username}". */
    static String relativePath(final String path)
    {
        return path.replaceFirst("^/restapi/v\\d+/", "");
    }

    /** Returns the name of the resource that is addressed by a path, for example "users" for "/restapi/v1/users/{username}". */
    static String resource(final String path)
    {
        return relativePath(path).split("/")[0];
    }

    /** Returns the number of path segments of a path, excluding the API prefix. */
    static int depth(final String path)
    {
        return relativePath(path).split("/").length;
    }

    /** Makes text safe for use in a markdown table cell. */
    static String cell(final String text)
    {
        return text.trim().replace("|", "\\|").replaceAll("\\s*\\R\\s*", "<br>");
    }
}
