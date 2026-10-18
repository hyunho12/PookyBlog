package pookyBlog.web.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.server.ResponseStatusException;

@ControllerAdvice(assignableTypes = {PostController.class, UserViewController.class})
public class PageExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ModelAndView handleStatus(ResponseStatusException exception, HttpServletResponse response) {
        if (exception.getStatusCode().value() == 404) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return new ModelAndView("error/404");
        }
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        return new ModelAndView("error/500");
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleUnexpected(Exception exception, HttpServletResponse response) {
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        return new ModelAndView("error/500");
    }
}
