package pookyBlog.web.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class PageErrorMvcTest {

    @Test
    void missingDetailRenders404Page() throws Exception {
        pageMvc(downstream(HttpStatus.NOT_FOUND))
                .perform(get("/posts/getPost/999"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error/404"));
    }

    @Test
    void missingUpdateRenders404Page() throws Exception {
        pageMvc(downstream(HttpStatus.NOT_FOUND))
                .perform(get("/posts/update/999"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error/404"));
    }

    @Test
    void pageFailureRenders500Page() throws Exception {
        pageMvc(downstream(HttpStatus.INTERNAL_SERVER_ERROR))
                .perform(get("/posts/update/999"))
                .andExpect(status().isInternalServerError())
                .andExpect(view().name("error/500"));
    }

    @Test
    void apiDownstream404KeepsJsonContract() throws Exception {
        WebClient client = WebClient.builder().exchangeFunction(request -> Mono.just(
                ClientResponse.create(HttpStatus.NOT_FOUND)
                        .header("Content-Type", "application/json")
                        .body("{\"code\":\"POST_NOT_FOUND\",\"message\":\"missing post\"}")
                        .build())).build();
        PostProxyController controller = new PostProxyController(client, client, null);
        var mvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new DownstreamExceptionHandler(), new PageExceptionHandler())
                .build();
        var asyncResult = mvc.perform(get("/api/posts/999")).andReturn();
        mvc.perform(asyncDispatch(asyncResult))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("POST_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("missing post"));
    }

    private MockMvc pageMvc(WebClient postClient) {
        WebClient unused = WebClient.builder().exchangeFunction(request -> Mono.empty()).build();
        PostController controller = new PostController(postClient, unused, unused, unused, unused, unused);
        return MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new DownstreamExceptionHandler(), new PageExceptionHandler())
                .build();
    }

    private WebClient downstream(HttpStatus status) {
        return WebClient.builder().exchangeFunction(request -> Mono.just(
                ClientResponse.create(status).build())).build();
    }
}
