package com.epages.restdocs;

import org.springframework.restdocs.snippet.Snippet;

/**
 * Wrapper around the static API from <code>MockMvcRestDocumentation</code>, to
 * integrate generation of WireMock stubs. Most of the static API was deprecated
 * in 0.6.x and removed in 0.7.0.
 */
public abstract class WireMockDocumentation {

    /**
     * Returns a json {@code Snippet} that will generate the WireMock stub from
     * the API operation.
     *
     * @return the json snippet
     * @see {@see MockMvcRestDocumentation}
     */
    public static Snippet wiremockJson(ResponseFieldTemplateDescriptor... responseFieldTemplateDescriptors) {
        return new WireMockJsonSnippet(responseFieldTemplateDescriptors);
    }

    /**
     * Returns a json {@code Snippet} that will generate the WireMock stub from
     * the API operation.
     *
     * @return the json snippet
     * @see {@see MockMvcRestDocumentation}
     */
    public static Snippet wiremockJson(VerifyHeaderSnippets snippet) {
        return new WireMockJsonSnippet(snippet);
    }

    /**
     * Returns a json {@code Snippet} that will generate the WireMock stub from
     * the API operation.
     *
     * @return the json snippet
     * @see {@see MockMvcRestDocumentation}
     */
    public static Snippet wiremockJson(VerifyHeaderSnippets snippet, QueryParameterSnippets snippets) {
        return new WireMockJsonSnippet(snippet);
    }

    /**
     * Returns a json {@code Snippet} that will generate the WireMock stub from
     * the API operation.
     *
     * @return the json snippet
     * @see {@see MockMvcRestDocumentation}
     */
    public static Snippet wiremockJson(QueryParameterSnippets snippets) {
        return new WireMockJsonSnippet(snippets);
    }

    /**
     * Convenience factory method for the common use case of replacing the id with the path parameter
     * Assumes that the id field can be found at the field <code>id</code> and the path contains the id at the second position
     */
    public static ResponseFieldTemplateDescriptor idFieldReplacedWithPathParameterValue() {
        return new ResponseFieldTemplateDescriptor("id").replacedWithWireMockTemplateExpression("request.requestLine.pathSegments.[1]");
    }

    public static ResponseFieldTemplateDescriptor templatedResponseField(String path) {
        return new ResponseFieldTemplateDescriptor(path);
    }

    public static AbsentHeaderDescriptor absentHeaderDescriptor(String name) {
        return new AbsentHeaderDescriptor(name);
    }

    public static VerifyHeaderSnippets verifyHeader(VerifyHeaderDescriptor... descriptors) {
        return new VerifyHeaderSnippets(descriptors);
    }

    public static QueryParameterSnippets queryParameterSnippets(QueryParameterDescriptor... descriptors) {
        return new QueryParameterSnippets(descriptors);
    }

    public static QueryParameterDescriptor queryParameterDescriptor(String name, Boolean ignoreValue) {
        return new QueryParameterDescriptor(name, ignoreValue);
    }
}
