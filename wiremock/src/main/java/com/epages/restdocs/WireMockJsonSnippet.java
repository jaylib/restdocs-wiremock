package com.epages.restdocs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.http.HttpMethod;
import org.springframework.restdocs.RestDocumentationContext;
import org.springframework.restdocs.headers.HeaderDescriptor;
import org.springframework.restdocs.operation.*;
import org.springframework.restdocs.snippet.RestDocumentationContextPlaceholderResolverFactory;
import org.springframework.restdocs.snippet.Snippet;
import org.springframework.restdocs.snippet.StandardWriterResolver;
import org.springframework.restdocs.snippet.WriterResolver;
import org.springframework.restdocs.templates.TemplateFormat;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriTemplate;


import java.io.IOException;
import java.io.Writer;
import java.util.*;

import static java.util.Collections.singletonList;
import static org.springframework.restdocs.generate.RestDocumentationGenerator.ATTRIBUTE_NAME_URL_TEMPLATE;

final class WireMockJsonSnippet implements Snippet {

    private static final String SNIPPET_NAME = "wiremock-stub";

    static final TemplateFormat TEMPLATE_FORMAT = new TemplateFormat() {

        @Override
        public String getId() {
            return "json";
        }

        @Override
        public String getFileExtension() {
            return "json";
        }
    };

    private List<ResponseFieldTemplateDescriptor> responseFieldTemplateDescriptors;

    private QueryParameterSnippets queryParameterSnippets;

    private VerifyHeaderSnippets verifyHeaderSnippets;

    WireMockJsonSnippet(VerifyHeaderSnippets snippet) {
        this.responseFieldTemplateDescriptors = Arrays.asList();
        this.verifyHeaderSnippets = snippet;
        this.queryParameterSnippets = new QueryParameterSnippets();
    }

    WireMockJsonSnippet(QueryParameterSnippets queryParameterSnippets) {
        this.responseFieldTemplateDescriptors = Arrays.asList();
        this.queryParameterSnippets = queryParameterSnippets;

    }

    WireMockJsonSnippet(VerifyHeaderSnippets snippets, QueryParameterSnippets queryParameterSnippets) {
        this.responseFieldTemplateDescriptors = Arrays.asList();
        this.verifyHeaderSnippets = snippets;
        this.queryParameterSnippets = queryParameterSnippets;
    }

    WireMockJsonSnippet(ResponseFieldTemplateDescriptor... responseFieldTemplateDescriptors) {
        this.responseFieldTemplateDescriptors = Arrays.asList(responseFieldTemplateDescriptors);
        this.queryParameterSnippets = new QueryParameterSnippets();
    }

    @Override
    public void document(Operation operation) throws IOException {
        RestDocumentationContext context = (RestDocumentationContext) operation.getAttributes()
                .get(RestDocumentationContext.class.getName());
        WriterResolver writerResolver = new StandardWriterResolver(
                new RestDocumentationContextPlaceholderResolverFactory(), "UTF-8", TEMPLATE_FORMAT);
        try (Writer writer = writerResolver.resolve(operation.getName(), SNIPPET_NAME, context)) {
            writer.append(toJsonString(operation));
        }
    }

    private String toJsonString(Operation operation) throws JsonProcessingException {
        return new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT).writeValueAsString(createModel(operation));
    }

    protected Map<Object, Object> createModel(Operation operation) {
        OperationResponse response = operation.getResponse();

        Maps.Builder<Object, Object> requestBuilder = Maps.builder()
                .put("method", operation.getRequest().getMethod());

        urlPathOrUrlPattern(operation, requestBuilder);
        addRequestBodyMatchers(operation, requestBuilder);

        Maps.Builder<Object, Object> responseBuilder = Maps.builder()
                .put("status", response.getStatus().value()).put("headers", responseHeaders(response))
                .put("body", responseBody(operation));

        if (!responseFieldTemplateDescriptors.isEmpty()) {
            responseBuilder.put("transformers", singletonList("response-template"));
        }

        Map<Object, Object> queryParams = queryParams(operation);
        if (!queryParams.isEmpty()) {
            requestBuilder.put("queryParameters", queryParams);
        }

        Map<Object, Object> headers = requestHeaders(operation.getRequest());
        if (!headers.isEmpty()) {
            requestBuilder.put("headers", headers);
        }

        return Maps.builder().put("request", requestBuilder.build()).put("response", responseBuilder.build())
                .build();
    }

    /**
     * If ATTRIBUTE_NAME_URL_TEMPLATE is present use it to build a urlPattern instead of a urlPath.
     * <p>
     * This allows for more flexible request matching when the path contains variable elements.
     * <p>
     * ATTRIBUTE_NAME_URL_TEMPLATE is present if the urlTemplate factore methods of RestDocumentationRequestBuilders are used.
     *
     * @param operation
     * @param requestBuilder
     */
    private void urlPathOrUrlPattern(Operation operation, Maps.Builder<Object, Object> requestBuilder) {
        String urlTemplate = (String) operation.getAttributes().get(ATTRIBUTE_NAME_URL_TEMPLATE);
        if (StringUtils.isEmpty(urlTemplate)) {
            requestBuilder.put("urlPath", operation.getRequest().getUri().getRawPath());
        } else {
            UriTemplate uriTemplate = new UriTemplate(urlTemplate);
            UriComponentsBuilder uriTemplateBuilder = UriComponentsBuilder.fromUriString(urlTemplate);
            Maps.Builder<String, String> uriVariables = Maps.builder();
            for (String variableName : uriTemplate.getVariableNames()) {
                uriVariables.put(variableName, "[^/]+");
            }
            String uriPathRegex = uriTemplateBuilder.buildAndExpand(uriVariables.build()).getPath();
            requestBuilder.put("urlPathPattern", uriPathRegex);
        }
    }

    private void addRequestBodyMatchers(Operation operation, Maps.Builder<Object, Object> requestBuilder) {

        if (operation.getRequest().getMethod() != HttpMethod.POST) {
            return;
        }
        String contentString = operation.getRequest().getContentAsString();
        if (contentString.isEmpty()) {
            return;
        }
        ArrayList<Map<Object, Object>> bodyPatterns = new ArrayList<>();
        bodyPatterns.add(Maps.builder()
                .put("equalToJson", contentString)
                .put("ignoreExtraElements", true)
                .put("ignoreArrayOrder", true)
                .build());

        requestBuilder.put("bodyPatterns", bodyPatterns);
    }

    private Map<Object, Object> responseHeaders(OperationResponse response) {
        Maps.Builder<Object, Object> responseHeaders = Maps.builder();
        for (Map.Entry<String, List<String>> e : response.getHeaders().entrySet()) {
            List<String> values = e.getValue();
            if (!values.isEmpty()) {
                responseHeaders.put(e.getKey(), values.get(0));
            }
        }
        return responseHeaders.build();
    }

    private Map<Object, Object> queryParams(Operation operation) {
        Maps.Builder<Object, Object> queryParams = Maps.builder();

        Parameters queryStringParameters = new QueryStringParser().parse(operation.getRequest().getUri());

        for (Map.Entry<String, List<String>> e : queryStringParameters.entrySet()) {
            List<String> values = e.getValue();
            if (!values.isEmpty()) {
                Boolean ignoreValue = false;
                for (QueryParameterDescriptor q: this.queryParameterSnippets.getDescriptors()) {
                    if (q.getName().equals(e.getKey()) && q.ignoreValue == true) {
                        ignoreValue = true;
                    }
                }
                if (ignoreValue) {
                    queryParams.put(e.getKey(), Maps.of("absent", false));
                } else {
                    queryParams.put(e.getKey(), Maps.of("equalTo", values.get(0)));
                }
            }
        }

        return queryParams.build();
    }

    private String responseBody(Operation operation) {
        OperationResponse response = operation.getResponse();
        String urlTemplateString = (String) operation.getAttributes().get(ATTRIBUTE_NAME_URL_TEMPLATE);

        return new ResponseTemplateProcessor(responseFieldTemplateDescriptors,
                urlTemplateString != null ? new UriTemplate(urlTemplateString) : null,
                response.getContentAsString()).replaceTemplateFields();
    }

    private Map<Object, Object> requestHeaders(OperationRequest request) {
        Maps.Builder<Object, Object> requestHeaders = Maps.builder();
        for (Map.Entry<String, List<String>> e : request.getHeaders().entrySet()) {
            List<String> values = e.getValue();
            if ("content-type".equalsIgnoreCase(e.getKey()) || "accept".equalsIgnoreCase(e.getKey())) {
                if (!values.isEmpty()) {
                    String mediaType = values.get(0);
                    // TODO : use proper MediaType parsing
                    if (mediaType.contains("+")) {
                        mediaType = mediaType.replaceAll(".*[+]", "");
                    } else {
                        mediaType = mediaType.replaceAll(".*/", "");
                    }
                    if (mediaType.contains(";")) {
                        mediaType = mediaType.replaceAll(";.*", "");
                    }
                    requestHeaders.put(e.getKey(), Maps.of("contains", mediaType));
                }
            } else {
                if (!values.isEmpty()) {
                    if (!"host".equalsIgnoreCase(e.getKey())) {
                        requestHeaders.put(e.getKey(), Maps.of("contains", values.get(0)));
                    }
                }
            }

            if (this.verifyHeaderSnippets != null) {
                for (VerifyHeaderDescriptor descriptor : this.verifyHeaderSnippets.getHeaderDescriptors()) {
                    if (descriptor.getAbsent()) {
                        requestHeaders.put(descriptor.getName(), Maps.of("absent", true));
                    }
                }
            }

        }
        String contentString = request.getContentAsString();
        if (contentString.isEmpty() == false) {
            requestHeaders.remove("Content-Length");
        }
        return requestHeaders.build();
    }

    // Small helper from swapping out guava.
    private abstract static class Maps {
        private Maps() {
        }

        private static <K, V> Map<K, V> of(K k1, V v1) {
            HashMap<K, V> map = new HashMap<>();
            map.put(k1, v1);
            return map;
        }

        private static <K, V> Builder<K, V> builder() {
            return new Builder<>();
        }

        private static class Builder<K, V> {
            private final Map<K, V> map;

            private Builder() {
                map = new HashMap<>();
            }

            private Builder<K, V> put(K k, V v) {
                map.put(k, v);
                return this;
            }

            private Builder<K, V> remove(K k) {
                map.remove(k);
                return this;
            }

            private Map<K, V> build() {
                return map;
            }
        }
    }

}
