package com.github.azeroth.portal.model;

import lombok.Data;

import java.util.List;


@Data
public class LoginForm {
    private String platform_id;
    private String program_id;
    private String version;
    List<FormInputValue> inputs;

    @Data
    public static class FormInputValue {
        private String input_id;
        private String value;
    }
}
