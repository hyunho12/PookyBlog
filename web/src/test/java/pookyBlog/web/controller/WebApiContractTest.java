package pookyBlog.web.controller;

import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;
import org.springframework.web.bind.annotation.RequestParam;

import static org.assertj.core.api.Assertions.assertThat;

class WebApiContractTest {

    @Test
    void publicApiMappingsMatchBffContract() {
        assertMapping(UserAuthProxyController.class, "signUp", "POST", "/api/auth/signup");
        assertMapping(UserAuthProxyController.class, "login", "POST", "/api/auth/login");
        assertMapping(UserAuthProxyController.class, "logout", "POST", "/api/auth/logout");
        assertMapping(UserAuthProxyController.class, "me", "GET", "/api/auth/me");

        assertMapping(PostProxyController.class, "getPosts", "GET", "/api/posts");
        assertMapping(PostProxyController.class, "getPost", "GET", "/api/posts/{postId}");
        assertMapping(PostProxyController.class, "createPost", "POST", "/api/posts");
        assertMapping(PostProxyController.class, "updatePost", "PATCH", "/api/posts/{postId}");
        assertMapping(PostProxyController.class, "deletePost", "DELETE", "/api/posts/{postId}");
        assertMapping(PostController.class, "getPost", "GET", "/posts/getPost/{id}");

        assertMapping(CommentProxyController.class, "getComments", "GET", "/api/posts/{postId}/comments");
        assertMapping(CommentProxyController.class, "createComment", "POST", "/api/posts/{postId}/comments");
        assertMapping(CommentProxyController.class, "updateComment", "PUT", "/api/comments/{commentId}");
        assertMapping(CommentProxyController.class, "deleteComment", "DELETE", "/api/comments/{commentId}");

        assertMapping(LikeProxyController.class, "likePost", "POST", "/api/posts/{postId}/likes");
        assertMapping(LikeProxyController.class, "unlikePost", "DELETE", "/api/posts/{postId}/likes");
        assertMapping(LikeProxyController.class, "countLikes", "GET", "/api/posts/{postId}/likes/count");

        assertMapping(ViewProxyController.class, "increaseView", "POST", "/api/posts/{postId}/views");
        assertMapping(ViewProxyController.class, "countViews", "GET", "/api/posts/{postId}/views/count");
    }

    @Test
    void browserCannotChooseUserIdForLikeOrViewMutation() {
        assertThat(method(LikeProxyController.class, "likePost").getParameters())
                .noneMatch(parameter -> parameter.isAnnotationPresent(RequestParam.class));
        assertThat(method(LikeProxyController.class, "unlikePost").getParameters())
                .noneMatch(parameter -> parameter.isAnnotationPresent(RequestParam.class));
        assertThat(method(ViewProxyController.class, "increaseView").getParameters())
                .noneMatch(parameter -> parameter.isAnnotationPresent(RequestParam.class));
    }

    private void assertMapping(Class<?> controller, String methodName, String httpMethod, String expectedPath) {
        Method method = method(controller, methodName);
        RequestMapping baseMapping = controller.getAnnotation(RequestMapping.class);
        String basePath = baseMapping == null ? "" : firstPath(baseMapping.value());

        Map<String, Class<? extends Annotation>> mappings = Map.of(
                "GET", org.springframework.web.bind.annotation.GetMapping.class,
                "POST", org.springframework.web.bind.annotation.PostMapping.class,
                "PUT", org.springframework.web.bind.annotation.PutMapping.class,
                "PATCH", org.springframework.web.bind.annotation.PatchMapping.class,
                "DELETE", org.springframework.web.bind.annotation.DeleteMapping.class
        );
        Annotation mapping = method.getAnnotation(mappings.get(httpMethod));

        assertThat(mapping).as("%s %s mapping", controller.getSimpleName(), methodName).isNotNull();
        String methodPath = firstPath(readValue(mapping));
        assertThat(basePath + methodPath).isEqualTo(expectedPath);
    }

    private Method method(Class<?> controller, String methodName) {
        return java.util.Arrays.stream(controller.getDeclaredMethods())
                .filter(candidate -> candidate.getName().equals(methodName))
                .findFirst().orElseThrow();
    }

    private String[] readValue(Annotation annotation) {
        try {
            return (String[]) annotation.annotationType().getMethod("value").invoke(annotation);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    private String firstPath(String[] paths) {
        return paths.length == 0 ? "" : paths[0];
    }
}
