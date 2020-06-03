package org.jflame.context.auth.model;

public class SimpleUrlPermission implements UrlPermission {

    private static final long serialVersionUID = -6009903673537476277L;

    private String code;
    private String[] urls;

    public SimpleUrlPermission() {
    }

    public SimpleUrlPermission(String code, String url) {
        this.code = code;
        this.urls = new String[] { url };
    }

    public SimpleUrlPermission(String code, String[] urls) {
        this.code = code;
        this.urls = urls;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String[] getUrls() {
        return urls;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setUrls(String[] urls) {
        this.urls = urls;
    }

}
