package io.github.mac_genius.pakkit.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Serialize {
    int order();
    int size() default -1;
    boolean includeListSize() default false;
    boolean includeObjectSize() default false;
}
