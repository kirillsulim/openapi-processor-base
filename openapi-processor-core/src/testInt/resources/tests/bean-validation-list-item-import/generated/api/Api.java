/*
 * This class is auto generated by https://github.com/hauner/openapi-processor-core.
 * TEST ONLY.
 */

package generated.api;

import annotation.Mapping;
import annotation.Parameter;
import java.util.List;
import javax.validation.constraints.Pattern;

public interface Api {

    @Mapping("/test")
    void getTest(@Parameter List<@Pattern(regexp = ".*") String> patternParam);

}
