package com.epages.restdocs;
import org.springframework.restdocs.snippet.AbstractDescriptor;

public class QueryParameterDescriptor extends AbstractDescriptor<QueryParameterDescriptor> {

    protected String name;
    protected String matchAgainst;


    /**
     * Creates a new {@code HeaderDescriptor} describing the header with the given
     * {@code name}.
     *
     * @param name the name
     * @param matchAgainst defines if the value of the query parameter should not be matched
     */
    public QueryParameterDescriptor(String name, String matchAgainst) {
        this.name = name;
        this.matchAgainst = matchAgainst;
    }

    public String getMatchAgainst() {
        return this.matchAgainst;
    }

    public String getName() {
        return this.name;
    }
}