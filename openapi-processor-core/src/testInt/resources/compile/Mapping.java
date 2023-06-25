package annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.CLASS;

@Documented
@Retention(CLASS)
@Target({METHOD})
public @interface Mapping {
    String value();
}
