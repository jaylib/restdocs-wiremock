package com.epages.restdocs;
import org.springframework.restdocs.snippet.AbstractDescriptor;

public class QueryParameterDescriptor extends AbstractDescriptor<QueryParameterDescriptor> {

    protected Boolean ignoreValue;
    protected String name;

    /**
     * Creates a new {@code HeaderDescriptor} describing the header with the given
     * {@code name}.
     *
     * @param name the name
     * @param ignoreValue defines if the value of the query parameter should not be matched
     */
    public QueryParameterDescriptor(String name, Boolean ignoreValue) {
        this.name = name;
        this.ignoreValue = ignoreValue;
    }

    public Boolean getIgnoreValue() {
        return this.ignoreValue;
    }

    public String getName() {
        return this.name;
    }
}