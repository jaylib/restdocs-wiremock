package com.epages.restdocs;

import static com.epages.restdocs.WireMockDocumentation.*;
import static com.google.common.collect.ImmutableMap.of;
import static java.util.Collections.emptyMap;
import static java.util.Collections.list;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.restdocs.generate.RestDocumentationGenerator.ATTRIBUTE_NAME_URL_TEMPLATE;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

import java.io.IOException;
import java.net.URI;
import java.util.*;

import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.restdocs.generate.RestDocumentationGenerator;
import org.springframework.restdocs.operation.Operation;
import org.springframework.restdocs.operation.OperationRequest;
import org.springframework.restdocs.operation.OperationRequestFactory;
import org.springframework.restdocs.operation.OperationResponse;
import org.springframework.restdocs.operation.OperationResponseFactory;
import org.springframework.restdocs.operation.RequestConverter;
import org.springframework.restdocs.operation.ResponseConverter;
import org.springframework.restdocs.snippet.Snippet;
import org.springframework.restdocs.templates.TemplateFormat;
import org.springframework.restdocs.test.ExpectedSnippet;
import org.springframework.restdocs.test.OperationBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;

public class WireMockJsonSnippetTest {

    private static final TemplateFormat FORMAT = WireMockJsonSnippet.TEMPLATE_FORMAT;

    @Rule
    public ExpectedSnippet expectedSnippet = new ExpectedSnippet(FORMAT);

    @SuppressWarnings("unchecked")
    private final RequestConverter<Object> requestConverter = Mockito.mock(RequestConverter.class);

    @SuppressWarnings("unchecked")
    private final ResponseConverter<Object> responseConverter = Mockito.mock(ResponseConverter.class);

    private final Object request = new Object();

    private final Object response = new Object();

    private final OperationRequest operationRequest = new OperationRequestFactory()
            .create(URI.create("http://localhost:8080"), null, null, new HttpHeaders(), null, null);

    private final OperationResponse operationResponse = new OperationResponseFactory().create(null, null, null);

    private final Snippet snippet = Mockito.mock(Snippet.class);

    @Test
    public void basicHandling() throws IOException {
        given(this.requestConverter.convert(this.request)).willReturn(this.operationRequest);
        given(this.responseConverter.convert(this.response)).willReturn(this.operationResponse);
        HashMap<String, Object> configuration = new HashMap<>();
        new RestDocumentationGenerator<>("id", this.requestConverter, this.responseConverter, this.snippet)
                .handle(this.request, this.response, configuration);
        verifySnippetInvocation(this.snippet, configuration);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void simpleRequest() throws IOException {
        this.expectedSnippet.expectWireMockJson("simple-request").withContents(
                (Matcher<String>)
                        sameJSONAs(new ObjectMapper().writeValueAsString(expectedJsonForSimpleRequest())));
        wiremockJson().document(operationBuilder("simple-request").request("http://localhost/").method("GET").build());
    }

    private ImmutableMap<String, ImmutableMap<String, ? extends Object>> expectedJsonForSimpleRequest() {
        return of( //
                "request", //
                of("method", "GET", "urlPath", "/"), //
                "response", //
                of("headers", emptyMap(), "body", "", "status", 200));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void simpleRequestWithUriTemplate() throws IOException {
        this.expectedSnippet.expectWireMockJson("simple-request").withContents(
                (Matcher<String>)
                        sameJSONAs(new ObjectMapper().writeValueAsString(expectedJsonForSimpleRequestWithUrlPattern())));
        wiremockJson().document(operationBuilder("simple-request")
                .attribute(ATTRIBUTE_NAME_URL_TEMPLATE, "http://localhost/some/{id}/other")
                .request("http://localhost/some/123-qbc/other")
                .method("GET").build());
    }

    private ImmutableMap<String, ImmutableMap<String, ? extends Object>> expectedJsonForSimpleRequestWithUrlPattern() {
        return of( //
                "request", //
                of("method", "GET", "urlPathPattern", "/some/[^/]+/other"), //
                "response", //
                of("headers", emptyMap(), "body", "", "status", 200));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void simpleRequestWithUriTemplateAndResponseTemplate() throws IOException {
        this.expectedSnippet.expectWireMockJson("simple-request")
                .withContents((Matcher<String>) sameJSONAs(
                        new ObjectMapper().writeValueAsString(expectedJsonForSimpleRequestWithUrlPatternAndResponseTemplate())));

        OperationBuilder operationBuilder = operationBuilder("simple-request")
                .attribute(ATTRIBUTE_NAME_URL_TEMPLATE, "http://localhost/some/{id}/other");
        operationBuilder.request("http://localhost/some/123-qbc/other")
                .method("GET").build();
        operationBuilder.response().status(200).content("{\"id\": \"some\"}");
        wiremockJson(templatedResponseField("id").replacedWithUriTemplateVariableValue("id"))
                .document(operationBuilder.build());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void simpleRequestWithUriTemplateAndResponseTemplate1() throws IOException {
        this.expectedSnippet.expectWireMockJson("simple-request")
                .withContents((Matcher<String>) sameJSONAs(
                        new ObjectMapper().writeValueAsString(expectedJsonForSimpleRequestWithUrlPatternAndResponseTemplate())));

        OperationBuilder operationBuilder = operationBuilder("simple-request")
                .attribute(ATTRIBUTE_NAME_URL_TEMPLATE, "http://localhost/some/{id}/other");
        operationBuilder.request("http://localhost/some/123-qbc/other")
                .method("GET").build();
        operationBuilder.response().status(200).content("{\"id\": \"some\"}");
        wiremockJson(idFieldReplacedWithPathParameterValue())
                .document(operationBuilder.build());
    }

    private ImmutableMap<String, ?> expectedJsonForSimpleRequestWithUrlPatternAndResponseTemplate() {
        return of(
                "request", of(
                        "method", "GET",
                        "urlPathPattern", "/some/[^/]+/other"
                ),
                "response", of(
                        "headers", of("Content-Length", "14"),
                        "body", "{\"id\":\"{{request.requestLine.pathSegments.[1]}}\"}",
                        "transformers", singletonList("response-template"),
                        "status", 200)
        );
    }

    @SuppressWarnings("unchecked")
    @Test
    public void getRequestWithParams() throws IOException {
        this.expectedSnippet.expectWireMockJson("get-request").withContents(
                (Matcher<String>) sameJSONAs(new ObjectMapper().writeValueAsString(
                        of( //
                                "request", //
                                of("method", "GET", "urlPath", "/foo", "queryParameters", //
                                        of("a", of("equalTo", "b")), "headers", of("Accept", of("contains", "json"))), //
                                "response", //
                                of("headers", emptyMap(), "body", "", "status", 200))
                )));
        wiremockJson().document(operationBuilder("get-request").request("http://localhost/foo?a=b&c=").method("GET")
                .header("Accept", "application/json").build());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void postRequest() throws IOException {
        this.expectedSnippet.expectWireMockJson("post-request").withContents((Matcher<String>)
                sameJSONAs(new ObjectMapper().writeValueAsString(
                        of( //
                                "request", //
                                of("method", "POST", "urlPath", "/", "headers", of("Content-Type", of("contains", "uri-list"), "Content-Length", of("contains", "15"))), //
                                "response", //
                                of("headers", //
                                        of("Content-Length", "16", "Content-Type", "text/plain"),
                                        "body", "response-content", "status", 200)))));
        OperationBuilder operationBuilder = operationBuilder("post-request");
        operationBuilder.response().content("response-content").header("Content-Type", "text/plain").build();
        wiremockJson().document(operationBuilder.request("http://localhost/").method("POST")
                .header("Content-Type", "text/uri-list").content("http://some.uri").build());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void customMediaType() throws IOException {
        this.expectedSnippet.expectWireMockJson("custom-mediatype").withContents((Matcher<String>)
                sameJSONAs(new ObjectMapper().writeValueAsString(
                        of( //
                                "request", //
                                of("method", "GET", "urlPath", "/foo",
                                        "headers", of("Accept", of("contains", "json"))), //
                                "response", //
                                of("headers", emptyMap(), "body", "", "status", 200))
                )));
        wiremockJson().document(operationBuilder("custom-mediatype").request("http://localhost/foo").method("GET")
                .header("Accept", "application/com.carlosjgp.myservice+json; version=1.0").build());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void includeAllHeadersExceptHost() throws IOException {
        this.expectedSnippet.expectWireMockJson("include-headers").withContents((Matcher<String>)
                sameJSONAs(new ObjectMapper().writeValueAsString(
                        of( //
                                "request", //
                                of("method", "GET", "urlPath", "/foo",
                                        "headers", of("Accept", of("contains", "json"), "X-App-SessionToken", of("contains", "16"))), //
                                "response", //
                                of("headers", emptyMap(), "body", "", "status", 200))
                )));
        wiremockJson().document(operationBuilder("include-headers").request("http://localhost/foo").method("GET")
                .header("Accept", "application/json").header("X-App-SessionToken", "16").build());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void verifyHeaders() throws IOException {
        this.expectedSnippet.expectWireMockJson("verify-headers").withContents((Matcher<String>)
                sameJSONAs(new ObjectMapper().writeValueAsString(
                        of( //
                                "request", //
                                of("method", "GET", "urlPath", "/foo",
                                        "headers", of("Accept", of("contains", "json"), "X-App-SessionToken", of("absent", true))), //
                                "response", //
                                of("headers", emptyMap(), "body", "", "status", 200))
                )));
        wiremockJson(verifyHeader(absentHeaderDescriptor("X-App-SessionToken"))).document(operationBuilder("verify-headers").request("http://localhost/foo").method("GET")
                .header("Accept", "application/json").build());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void includeBodyRequestMatcher() throws IOException {

        String requestBody = "{\"email\": \"test@test.de\"}";
        ArrayList<ImmutableMap> bodyPatterns = new ArrayList<>();
        bodyPatterns.add(of("equalToJson", requestBody));

        ArrayList<ImmutableMap> multipartPatterns = new ArrayList<>();
        multipartPatterns.add(of("matchingType", "ANY"));
        multipartPatterns.add(of("bodyPatterns", bodyPatterns));


        this.expectedSnippet.expectWireMockJson("include-request-body-matcher").withContents((Matcher<String>)
                sameJSONAs(new ObjectMapper().writeValueAsString(
                        of( //
                                "request", //
                                of("method", "POST", "urlPath", "/foo",
                                        "headers", of("Accept", of("contains", "json"), "X-App-SessionToken", of("contains", "16"), "Content-Length", of("contains", String.valueOf(requestBody.length()))),
                                        "multipartPatterns", list(Collections.enumeration(multipartPatterns))
                                        ), //
                                "response", //
                                of("headers", emptyMap(), "body", "", "status", 200))
                )));
        wiremockJson().document(operationBuilder("include-request-body-matcher").request("http://localhost/foo").method("POST")
                .header("Accept", "application/json")
                .header("X-App-SessionToken", "16")
                .content( requestBody)
                .build());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldNotIncludeRequestBodyMatcher() throws IOException {

        this.expectedSnippet.expectWireMockJson("exclude-request-body-matcher").withContents((Matcher<String>)
                sameJSONAs(new ObjectMapper().writeValueAsString(
                        of( //
                                "request", //
                                of("method", "POST", "urlPath", "/foo",
                                        "headers", of("Accept", of("contains", "json"), "X-App-SessionToken", of("contains", "16"))
                                ), //
                                "response", //
                                of("headers", emptyMap(), "body", "", "status", 200))
                )));
        wiremockJson().document(operationBuilder("exclude-request-body-matcher").request("http://localhost/foo").method("POST")
                .header("Accept", "application/json")
                .header("X-App-SessionToken", "16")
                .build());
    }

    public OperationBuilder operationBuilder(String name) {
        return new OperationBuilder(name, this.expectedSnippet.getOutputDirectory(), FORMAT);
    }

    private void verifySnippetInvocation(Snippet snippet, Map<String, Object> attributes) throws IOException {
        verifySnippetInvocation(snippet, attributes, 1);
    }

    private void verifySnippetInvocation(Snippet snippet, Map<String, Object> attributes, int times)
            throws IOException {
        ArgumentCaptor<Operation> operation = ArgumentCaptor.forClass(Operation.class);
        verify(snippet, Mockito.times(times)).document(operation.capture());
        assertThat(this.operationRequest, is(equalTo(operation.getValue().getRequest())));
        assertThat(this.operationResponse, is(equalTo(operation.getValue().getResponse())));
        assertThat(attributes, is(equalTo(operation.getValue().getAttributes())));
    }
}
