package com.pookyBlog.pookyBlog.Dto.Request;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import static java.lang.Math.max;
import static java.lang.Math.min;

@Getter
@Setter
@Builder
public class PostSearch {

    private static final int MAX_SIZE = 500;

    @Builder.Default
    private Integer page = 1;

    @Builder.Default
    private Integer size = 10;

    public long getOffset(){
        return (long) (max(page, 1) - 1) * min(size,MAX_SIZE);
    }
}
