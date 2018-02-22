package io.swagger.v3.parser.processors;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.CookieParameter;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.PathParameter;
import io.swagger.v3.oas.models.parameters.QueryParameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.parser.ResolverCache;
import io.swagger.v3.parser.models.RefFormat;
import mockit.FullVerifications;
import mockit.Injectable;
import mockit.Mocked;
import mockit.StrictExpectations;
import org.testng.annotations.Test;


import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.assertEquals;


public class ParameterProcessorTest {


    @Injectable
    ResolverCache cache;

    @Injectable
    OpenAPI openAPI;

    @Mocked
    SchemaProcessor modelProcessor;

    @Test
    public void testProcessParameters_TypesThatAreNotRefOrBody(@Injectable final HeaderParameter headerParameter,
                                                               @Injectable final QueryParameter queryParameter,
                                                               @Injectable final CookieParameter cookieParameter,
                                                               @Injectable final PathParameter pathParameter) throws Exception {
        expectedModelProcessorCreation();

        final List<Parameter> processedParameters = new ParameterProcessor(cache, openAPI)
                .processParameters(Arrays.<Parameter>asList(headerParameter,
                        queryParameter,
                        cookieParameter,
                        pathParameter));

        new FullVerifications() {{
        }};

        assertEquals(processedParameters.size(), 5);
        assertEquals(processedParameters.get(0), headerParameter);
        assertEquals(processedParameters.get(1), queryParameter);
        assertEquals(processedParameters.get(2), cookieParameter);
        assertEquals(processedParameters.get(3), pathParameter);
       // assertEquals(processedParameters.get(4), formParameter);
    }

    @Test
    public void testProcessParameters_RefToHeader(
            @Injectable final HeaderParameter resolvedHeaderParam) throws Exception {
        expectedModelProcessorCreation();

        final String ref = "#/components/parameters/foo";
        Parameter refParameter = new Parameter().$ref(ref);

        expectLoadingRefFromCache(ref, RefFormat.INTERNAL, resolvedHeaderParam);

        final List<Parameter> processedParameters = new ParameterProcessor(cache, openAPI)
                .processParameters(Arrays.<Parameter>asList(refParameter));

        new FullVerifications(){{}};

        assertEquals(processedParameters.size(), 1);
        assertEquals(processedParameters.get(0), resolvedHeaderParam);
    }

    private void expectLoadingRefFromCache(final String ref, final RefFormat refFormat,
                                           final Parameter resolvedParam) {
        new StrictExpectations() {{
            cache.loadRef(ref, refFormat, Parameter.class);
            times = 1;
            result = resolvedParam;
        }};
    }

    @Test
    public void testProcessParameters_BodyParameter(@Injectable final Schema bodyParamSchema) throws Exception {

        expectedModelProcessorCreation();

        RequestBody bodyParameter = new RequestBody().content(new Content().addMediaType("*/*",new MediaType().schema(bodyParamSchema)));

        expectModelProcessorInvoked(bodyParamSchema);

        new RequestBodyProcessor(cache, openAPI).processRequestBody(bodyParameter);

        new FullVerifications(){{}};

        /*assertEquals(processedParameters.size(), 1);
        assertEquals(processedParameters.get(0), bodyParameter);*/
    }

    private void expectModelProcessorInvoked(@Injectable final Schema bodyParamSchema) {
        new StrictExpectations(){{
            modelProcessor.processSchema(bodyParamSchema); times=1;
        }};
    }

    /*@Test
    public void testProcessParameters_RefToBodyParam(@Injectable final Model bodyParamSchema) throws Exception {
        expectedModelProcessorCreation();

        final String ref = "#/parameters/foo";
        RefParameter refParameter = new RefParameter(ref);
        final BodyParameter resolvedBodyParam = new BodyParameter().schema(bodyParamSchema);

        expectLoadingRefFromCache(ref, RefFormat.INTERNAL, resolvedBodyParam);
        expectModelProcessorInvoked(bodyParamSchema);

        final List<Parameter> processedParameters = new ParameterProcessor(cache, swagger)
                .processParameters(Arrays.<Parameter>asList(refParameter));

        new FullVerifications(){{}};

        assertEquals(processedParameters.size(), 1);
        assertEquals(processedParameters.get(0), resolvedBodyParam);
    }*/

    private void expectedModelProcessorCreation() {
        new StrictExpectations() {{
            new SchemaProcessor(cache, openAPI);
            times = 1;
            result = modelProcessor;
        }};
    }
}
