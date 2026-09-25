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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import org.jivesoftware.openfire.plugin.rest.CustomJacksonMapperProvider;

import java.io.StringWriter;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

/**
 * Generates the documentation of the endpoints and data types in readme.md from the OpenAPI specification, which in
 * turn is generated from the OpenAPI annotations in the source code. This keeps the documentation in sync with the
 * implementation.
 *
 * The generated documentation replaces all text between pairs of marker lines in the readme, like
 * {@code <!-- BEGIN GENERATED ENDPOINTS ... -->} and {@code <!-- END GENERATED ENDPOINTS -->}. Text outside of these
 * markers is not modified.
 *
 * Example request bodies are generated from the examples in the specification, and are then converted into the entity
 * classes of the plugin and back, using the same JSON and XML serialization as the plugin. This guarantees that the
 * examples use the actual format of the REST API: when an example cannot be processed, generation fails.
 *
 * This is a single-file Java program that is executed by the Maven build. It is not part of the plugin.
 *
 * Usage: {@code java -cp <classpath> ReadmeGenerator.java <openapi.json> <readme.md>}
 */
public class ReadmeGenerator
{
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

    /** Packages that contain the classes that are used as request and response bodies. */
    static final List<String> ENTITY_PACKAGES = List.of("org.jivesoftware.openfire.plugin.rest.entity", "org.jivesoftware.openfire.plugin.rest.exceptions");

    /** The value used in examples for date/time values, which do not have an example defined in the annotations. */
    static final String EXAMPLE_DATE_TIME = "2026-01-31T12:34:56.789Z";

    record Endpoint(String path, PathItem.HttpMethod method, Operation operation) {}

    final OpenAPI openAPI;
    final ObjectMapper mapper = new CustomJacksonMapperProvider().getContext(Object.class);

    ReadmeGenerator(final OpenAPI openAPI)
    {
        this.openAPI = openAPI;
    }

    public static void main(String[] args) throws Exception
    {
        if (args.length != 2) {
            System.err.println("Usage: java ReadmeGenerator.java <openapi.json> <readme.md>");
            System.exit(1);
        }
        final Path specFile = Path.of(args[0]);
        final Path readmeFile = Path.of(args[1]);

        // Make the generated output independent of the environment that the build runs in.
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        Locale.setDefault(Locale.ROOT);

        final ReadmeGenerator generator = new ReadmeGenerator(Json.mapper().readValue(specFile.toFile(), OpenAPI.class));
        generator.verifyPropertyNames();

        final String readme = Files.readString(readmeFile, StandardCharsets.UTF_8);
        String updated = replaceSection(readme, "ENDPOINTS", generator.generateEndpoints());
        updated = replaceSection(updated, "DATA TYPES", generator.generateDataTypes());

        if (updated.equals(readme)) {
            System.out.println("Generated documentation in " + readmeFile + " is up to date.");
        } else {
            Files.writeString(readmeFile, updated, StandardCharsets.UTF_8);
            System.out.println("Updated the generated documentation in " + readmeFile + ".");
        }
    }

    /** Replaces the text between the BEGIN and END markers of a section with new content. */
    static String replaceSection(final String readme, final String section, final String content)
    {
        final String beginMarker = "<!-- BEGIN GENERATED " + section;
        final String endMarker = "<!-- END GENERATED " + section + " -->";
        final int begin = readme.indexOf(beginMarker);
        final int end = readme.indexOf(endMarker);
        if (begin < 0 || end < begin) {
            throw new IllegalStateException("Unable to find the '" + beginMarker + "' and '" + endMarker + "' markers in the readme.");
        }
        final int beginLineEnd = readme.indexOf('\n', begin) + 1;
        return readme.substring(0, beginLineEnd) + "\n" + content.strip().replaceAll("\n{3,}", "\n\n") + "\n\n" + readme.substring(end);
    }

    // ---------------------------------------------------------------------------------------------------------------
    // Endpoints
    // ---------------------------------------------------------------------------------------------------------------

    String generateEndpoints() throws Exception
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
        out.append("The paths of all endpoints below are relative to the root of the Openfire admin console, for example `http://example.org:9090`. The data types that are used by these endpoints are described in [Data types](#data-types).\n\n");
        out.append("In addition to the responses that are documented for each endpoint, every endpoint can respond with:\n\n");
        GENERIC_RESPONSES.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(e -> out.append("- `").append(e.getKey()).append("`: ").append(e.getValue()).append("\n"));
        out.append("\n");
        out.append("Interactive documentation of these endpoints is available in the Openfire admin console, via the link on the REST API settings page (Server > Server Settings > REST API).\n");

        for (final Map.Entry<String, List<Endpoint>> entry : endpointsByTag.entrySet()) {
            final String tag = entry.getKey();
            final List<Endpoint> endpoints = entry.getValue();
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
        }
        return out.toString();
    }

    void appendEndpoint(final StringBuilder out, final Endpoint endpoint) throws Exception
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
            appendExamples(out, requestBody.getContent());
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

    /** Appends example XML and JSON representations of content, if the content is an entity of the plugin. */
    void appendExamples(final StringBuilder out, final Content content) throws Exception
    {
        if (content == null) {
            return;
        }

        // Use examples that are explicitly defined for the content, when available.
        final Map<String, String> explicitExamples = new LinkedHashMap<>();
        content.forEach((mediaType, value) -> {
            if (value.getExample() != null) {
                explicitExamples.put(mediaType, value.getExample().toString());
            } else if (value.getExamples() != null && !value.getExamples().isEmpty()) {
                explicitExamples.put(mediaType, String.valueOf(value.getExamples().values().iterator().next().getValue()));
            }
        });
        if (!explicitExamples.isEmpty()) {
            out.append("<details>\n<summary>Example request body</summary>\n\n");
            explicitExamples.forEach((mediaType, example) -> {
                final String language = mediaType.endsWith("xml") ? "xml" : mediaType.endsWith("json") ? "json" : "";
                out.append("`Content-Type: ").append(mediaType).append("`:\n\n```").append(language).append("\n").append(example.strip()).append("\n```\n\n");
            });
            out.append("</details>\n\n");
            return;
        }

        final Schema<?> schema = content.values().stream().map(MediaType::getSchema).filter(Objects::nonNull).findFirst().orElse(null);
        if (schema == null || schema.get$ref() == null) {
            return;
        }
        final Class<?> entityClass = findEntityClass(refName(schema.get$ref()));
        if (entityClass == null || Modifier.isAbstract(entityClass.getModifiers())) {
            return;
        }

        // Convert the example into an instance of the entity class, then serialize that instance like the plugin does.
        final Object entity = mapper.treeToValue(example(schema, 0), entityClass);

        out.append("<details>\n<summary>Example request bodies</summary>\n\n");
        if (content.containsKey("application/xml")) {
            final Marshaller marshaller = JAXBContext.newInstance(entityClass).createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
            final StringWriter xml = new StringWriter();
            marshaller.marshal(entity, xml);
            out.append("XML (`Content-Type: application/xml`):\n\n```xml\n").append(xml.toString().strip()).append("\n```\n\n");
        }
        if (content.containsKey("application/json")) {
            out.append("JSON (`Content-Type: application/json`):\n\n```json\n").append(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(entity).strip()).append("\n```\n\n");
        }
        out.append("</details>\n\n");
    }

    /** Builds an example JSON value for a schema, based on the examples that are defined in the specification. */
    JsonNode example(final Schema<?> schema, final int depth)
    {
        if (depth > 10) {
            throw new IllegalStateException("Schema nesting is too deep (recursive schema?)");
        }
        if (schema.get$ref() != null) {
            return example(resolve(schema.get$ref()), depth + 1);
        }
        if (schema.getExample() != null) {
            return mapper.valueToTree(schema.getExample());
        }
        if ("array".equals(schema.getType()) && schema.getItems() != null) {
            final ArrayNode array = JsonNodeFactory.instance.arrayNode();
            array.add(example(schema.getItems(), depth + 1));
            return array;
        }
        if (schema.getProperties() != null) {
            final ObjectNode object = JsonNodeFactory.instance.objectNode();
            schema.getProperties().forEach((name, property) -> object.set(name, example(property, depth + 1)));
            return object;
        }
        if ("date-time".equals(schema.getFormat())) {
            return JsonNodeFactory.instance.textNode(EXAMPLE_DATE_TIME);
        }
        if (schema.getEnum() != null && !schema.getEnum().isEmpty()) {
            return mapper.valueToTree(schema.getEnum().get(0));
        }
        return switch (Optional.ofNullable(schema.getType()).orElse("")) {
            case "integer", "number" -> JsonNodeFactory.instance.numberNode(0);
            case "boolean" -> JsonNodeFactory.instance.booleanNode(false);
            default -> JsonNodeFactory.instance.textNode("string");
        };
    }

    /**
     * Verifies that the names of the properties in the specification are the names that are used in JSON. These can
     * differ, as JSON serialization uses JAXB annotations when there are no Jackson annotations, which the generator of
     * the specification does not do. The difference is fixed by adding a Jackson annotation (like
     * {@code @JsonProperty}) that uses the name that is used in JSON.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    void verifyPropertyNames()
    {
        final List<String> problems = new ArrayList<>();
        final Map<String, Schema> schemas = new TreeMap<>(Optional.ofNullable(openAPI.getComponents().getSchemas()).orElse(Map.of()));
        schemas.forEach((name, schema) -> {
            final Class<?> entityClass = findEntityClass(name);
            if (entityClass == null || schema.getProperties() == null) {
                return;
            }
            final Set<String> jsonNames = mapper.getSerializationConfig().introspect(mapper.constructType(entityClass)).findProperties().stream()
                .filter(BeanPropertyDefinition::couldSerialize)
                .map(BeanPropertyDefinition::getName)
                .collect(Collectors.toSet());
            final Set<String> specNames = new TreeSet<String>(schema.getProperties().keySet());
            specNames.removeAll(jsonNames);
            if (!specNames.isEmpty()) {
                problems.add(name + ": " + specNames + " (names used in JSON: " + new TreeSet<>(jsonNames) + ")");
            }
        });
        if (!problems.isEmpty()) {
            throw new IllegalStateException("The OpenAPI specification uses property names that are not used in JSON. Add a @JsonProperty annotation with the name that is used in JSON to:\n" + String.join("\n", problems));
        }
    }

    static Class<?> findEntityClass(final String name)
    {
        for (final String pkg : ENTITY_PACKAGES) {
            try {
                return Class.forName(pkg + "." + name);
            } catch (ClassNotFoundException e) {
                // Try the next package.
            }
        }
        return null;
    }

    // ---------------------------------------------------------------------------------------------------------------
    // Data types
    // ---------------------------------------------------------------------------------------------------------------

    @SuppressWarnings({"rawtypes", "unchecked"})
    String generateDataTypes()
    {
        final StringBuilder out = new StringBuilder();
        out.append("## Data types\n\n");
        out.append("These are the data types that are used in the request and response bodies of the endpoints. The name of a field is the name that is used in JSON. When XML uses a different name, it is mentioned in the description of the field.\n\n");
        out.append("Date/time values are represented as an ISO-8601 formatted text in XML (for example: `" + EXAMPLE_DATE_TIME + "`), and as the number of milliseconds since the Unix epoch in JSON (for example: `1769862896789`). In JSON request bodies, the ISO-8601 format can also be used.\n");

        final Map<String, Schema> schemas = new TreeMap<>(Optional.ofNullable(openAPI.getComponents().getSchemas()).orElse(Map.of()));
        schemas.forEach((name, schema) -> {
            out.append("\n### ").append(name).append("\n\n");
            if (schema.getDescription() != null && !schema.getDescription().isBlank()) {
                out.append(schema.getDescription().trim()).append("\n\n");
            }
            if (schema.getXml() != null && schema.getXml().getName() != null) {
                out.append("XML root element: `<").append(schema.getXml().getName()).append(">`\n\n");
            }
            final Map<String, Schema> properties = schema.getProperties();
            if (properties == null || properties.isEmpty()) {
                return;
            }
            final List<String> required = Optional.ofNullable((List<String>) schema.getRequired()).orElse(List.of());
            out.append("| Field | Type | Required | Description |\n");
            out.append("|-------|------|----------|-------------|\n");
            properties.forEach((propertyName, property) -> out.append("| ").append(propertyName)
                .append(" | ").append(describeSchema(property))
                .append(" | ").append(required.contains(propertyName) ? "yes" : "no")
                .append(" | ").append(cell(describeProperty(propertyName, property)))
                .append(" |\n"));
        });
        return out.toString();
    }

    /** Describes a property of a data type, including its XML representation, allowed values and example. */
    @SuppressWarnings("rawtypes")
    static String describeProperty(final String propertyName, final Schema property)
    {
        final List<String> parts = new ArrayList<>();
        if (property.getDescription() != null && !property.getDescription().isBlank()) {
            parts.add(sentence(property.getDescription()));
        }
        final List<?> allowed = property.getEnum() != null ? property.getEnum() : (property.getItems() != null ? property.getItems().getEnum() : null);
        if (allowed != null && !allowed.isEmpty()) {
            parts.add("Allowed values: " + allowed.stream().map(v -> "`" + v + "`").collect(Collectors.joining(", ")) + ".");
        }
        final String xml = describeXml(propertyName, property);
        if (xml != null) {
            parts.add(xml);
        }
        final Object example = property.getExample() != null ? property.getExample() : (property.getItems() != null ? property.getItems().getExample() : null);
        if (example != null) {
            parts.add("Example: `" + example + "`");
        }
        return String.join(" ", parts);
    }

    /** Describes how a property is represented in XML, when that differs from its (JSON) name. */
    @SuppressWarnings("rawtypes")
    static String describeXml(final String propertyName, final Schema property)
    {
        final String xmlName = property.getXml() != null && property.getXml().getName() != null ? property.getXml().getName() : propertyName;
        if (property.getXml() != null && Boolean.TRUE.equals(property.getXml().getAttribute())) {
            return "In XML, this is the `" + xmlName + "` attribute.";
        }
        if ("array".equals(property.getType()) && property.getItems() != null) {
            final Schema items = property.getItems();
            final String itemName = items.getXml() != null && items.getXml().getName() != null ? items.getXml().getName() : null;
            if (property.getXml() != null && Boolean.TRUE.equals(property.getXml().getWrapped())) {
                return itemName == null ? "In XML, the items are wrapped in the `<" + xmlName + ">` element." : "In XML, items are represented as `<" + itemName + ">` elements, wrapped in the `<" + xmlName + ">` element.";
            }
            return "In XML, items are represented as `<" + xmlName + ">` elements.";
        }
        return xmlName.equals(propertyName) ? null : "In XML, this is the `<" + xmlName + ">` element.";
    }

    /** Returns text as a sentence that ends with a punctuation mark. */
    static String sentence(final String text)
    {
        final String trimmed = text.trim();
        return trimmed.matches(".*[.!?:]$") ? trimmed : trimmed + ".";
    }

    // ---------------------------------------------------------------------------------------------------------------
    // Utilities
    // ---------------------------------------------------------------------------------------------------------------

    Schema<?> resolve(final String ref)
    {
        final Schema<?> schema = openAPI.getComponents().getSchemas().get(refName(ref));
        if (schema == null) {
            throw new IllegalStateException("Unable to resolve schema reference: " + ref);
        }
        return schema;
    }

    static String refName(final String ref)
    {
        return ref.substring(ref.lastIndexOf('/') + 1);
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

    /** Describes the type of a schema, linking to the documentation of data types. */
    @SuppressWarnings("rawtypes")
    static String describeSchema(final Schema schema)
    {
        if (schema.get$ref() != null) {
            final String name = refName(schema.get$ref());
            return "[" + name + "](#" + name.toLowerCase(Locale.ROOT) + ")";
        }
        if ("array".equals(schema.getType()) && schema.getItems() != null) {
            return "array of " + describeSchema(schema.getItems());
        }
        if (schema.getFormat() != null && "date-time".equals(schema.getFormat())) {
            return "date-time";
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
