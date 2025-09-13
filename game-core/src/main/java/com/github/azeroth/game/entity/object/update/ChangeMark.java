package com.github.azeroth.game.entity.object.update;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ChangeMark {

    FieldType type() default FieldType.OBJECT;
    int blockBit() default -1;
    int bit();
    int size() default -1;
    int firstElementBit() default -1;
}
