package pookyBlog.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import pookyBlog.Service.HotPostService;
import pookyBlog.Service.response.HotPostResponse;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class HotPostController {
    private final HotPostService hotPostService;

    @GetMapping("/hot-posts/date/{dateStr}")
    public List<HotPostResponse> getAll(@PathVariable("dateStr") String dateStr){
        return hotPostService.getAll(dateStr);
    }
}
