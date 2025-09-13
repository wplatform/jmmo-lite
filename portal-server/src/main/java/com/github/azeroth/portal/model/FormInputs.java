package com.github.azeroth.portal.model;


import com.github.azeroth.utils.JsonUtil;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class FormInputs {

    public static String DEFAULT_FORM_INPUTS;

    static {
        // set up form inputs
        FormInputs result = FormInputs.builder()
                .inputs(new ArrayList<>(3))
                .type("LOGIN_FORM")
                .build();
        result.getInputs().add(FormInput.builder()
                .input_id("account_name")
                .type("text")
                .label("E-mail")
                .max_length(64).build()
        );
        result.getInputs().add(FormInput.builder()
                .input_id("password")
                .type("password")
                .label("Password")
                .max_length(16).build()
        );
        result.getInputs().add(FormInput.builder()
                .input_id("log_in_submit")
                .type("submit")
                .label("Log In").build()
        );
        DEFAULT_FORM_INPUTS = JsonUtil.toJson(result);
    }

    private String type;
    private List<FormInput> inputs;

    @Data
    @Builder
    public static class FormInput {
        private String input_id;
        private String type;
        private String label;
        private Integer max_length;

    }
}
