package io.agora.api.example.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Example {
    /**
     * @return example index
     */
    int index();

    /**
     * @return group name
     */
    String group() default "";

    /**
     * @return example name
     */
    String name();

    /**
     * @return action ID
     */
    int actionId();

    /**
     * @return tips ID
     */
    String tipsId();
}
