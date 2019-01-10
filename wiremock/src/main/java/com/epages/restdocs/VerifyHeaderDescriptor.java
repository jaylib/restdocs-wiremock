package com.epages.restdocs;

import org.springframework.http.HttpHeaders;
import org.springframework.restdocs.headers.HeaderDescriptor;

public abstract class VerifyHeaderDescriptor extends HeaderDescriptor {

    protected Boolean absent;

    /**
     * Creates a new {@code HeaderDescriptor} describing the header with the given
     * {@code name}.
     *
     * @param name the name
     * @see HttpHeaders
     */
    public VerifyHeaderDescriptor(String name) {
        super(name);
        this.absent = false;
    }

    public Boolean getAbsent() {
        return this.absent;
    }
}