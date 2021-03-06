package com.example.eshop.rest.controllers.base;

import com.example.eshop.rest.dto.BasicErrorDto;
import org.springframework.http.HttpStatus;

public class BasicErrorBuilder {
    private HttpStatus status;
    private String detail;

    private BasicErrorBuilder() {
    }

    public static BasicErrorBuilder newInstance() {
        return new BasicErrorBuilder();
    }

    public BasicErrorBuilder setStatus(HttpStatus status) {
        this.status = status;
        return this;
    }

    public BasicErrorBuilder setDetail(String detail) {
        this.detail = detail;
        return this;
    }

    public BasicErrorDto build() {
        var error = new BasicErrorDto();

        error.setStatus(status.value());
        error.setDetail(detail);

        return error;
    }
}
