package org.jflame.apidoc.enums;

public enum MediaType {
    APPLICATION_JSON("application/json"), APPLICATION_XML("application/xml"), FORM_URLENCODED(
            "x-www-form-urlencoded"), TEXT_PLAIN("text/plain");

    private String value;

    private MediaType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
