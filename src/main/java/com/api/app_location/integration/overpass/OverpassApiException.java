package com.api.app_location.integration.overpass;

public class OverpassApiException extends RuntimeException {

    private final int statusCode;
    private final String responseBody;

    public OverpassApiException(int statusCode, String responseBody) {
        super("Overpass API returned HTTP status " + statusCode);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getResponseBody() {
        return responseBody;
    }
}
