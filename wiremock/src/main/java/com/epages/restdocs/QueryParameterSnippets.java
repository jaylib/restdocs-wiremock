package com.epages.restdocs;

import java.util.Arrays;
import java.util.List;

public class QueryParameterSnippets {

    private List<QueryParameterDescriptor> descriptors;

    public QueryParameterSnippets(QueryParameterDescriptor... descriptors) {
        this.descriptors = Arrays.asList(descriptors);
    }

    public List<QueryParameterDescriptor> getDescriptors() {
        return this.descriptors;
    }
}
