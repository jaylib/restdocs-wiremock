package com.epages.restdocs;

import java.util.Arrays;
import java.util.List;

class AbsentHeadersSnippet {

    private List<AbsentHeaderDescriptor> descriptors;

    AbsentHeadersSnippet(AbsentHeaderDescriptor... descriptors) {
        this.descriptors = Arrays.asList(descriptors);
    }

    List<AbsentHeaderDescriptor> getHeaderDescriptors() {
        return this.descriptors;
    }
}
