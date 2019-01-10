package com.epages.restdocs;

import org.springframework.http.HttpHeaders;

public class AbsentHeaderDescriptor extends VerifyHeaderDescriptor {

    /**
     * Creates a new {@code AbsentHeaderDescriptor} describing the header with the given
     * {@code name} that should be verified for absense.
     *
     * @param name the name
     * @see HttpHeaders
     */
    public AbsentHeaderDescriptor(String name) {
        super(name);
        this.absent = true;
    }

}
