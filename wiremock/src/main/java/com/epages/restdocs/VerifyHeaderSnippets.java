package com.epages.restdocs;

import java.util.Arrays;
import java.util.List;

public class VerifyHeaderSnippets {

    private List<VerifyHeaderDescriptor> descriptors;

    public VerifyHeaderSnippets(VerifyHeaderDescriptor... descriptors) {
        this.descriptors = Arrays.asList(descriptors);
    }

    public List<VerifyHeaderDescriptor> getHeaderDescriptors() {
        return this.descriptors;
    }
}
