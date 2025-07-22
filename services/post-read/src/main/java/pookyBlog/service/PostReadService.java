package pookyBlog.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pookyBlog.client.CommentClient;
import pookyBlog.client.LikeClient;
import pookyBlog.client.PostClient;
import pookyBlog.client.ViewClient;
import pookyBlog.event.Event;
import pookyBlog.event.EventPayload;
import pookyBlog.repository.PostQueryModel;
import pookyBlog.repository.PostQueryModelRepository;
import pookyBlog.response.PostReadResponse;
import pookyBlog.service.event.handler.EventHandler;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostReadService {
    private final PostClient postClient;
    private final CommentClient commentClient;
    private final LikeClient likeClient;
    private final ViewClient viewClient;
    private final PostQueryModelRepository postQueryModelRepository;
    private final List<EventHandler> eventHandlers;

    public void handleEvent(Event<EventPayload> event){
        for(EventHandler eventHandler : eventHandlers){
            if(eventHandler.supports(event)){
                eventHandler.handle(event);
            }
        }
    }

    public PostReadResponse read(Long postId){
        PostQueryModel postQueryModel = postQueryModelRepository.read(postId)
                .or(() -> fetch(postId))
                .orElseThrow();

        return PostReadResponse.from(
                postQueryModel,
                viewClient.count(postId)
        );
    }

    private Optional<PostQueryModel> fetch(Long postId){
        Optional<PostQueryModel> postQueryModelOptional = postClient.get(postId)
                .map(post-> PostQueryModel.create(
                        post,
                        commentClient.count(postId),
                        likeClient.count(postId)
                ));
        postQueryModelOptional.ifPresent(postQueryModel -> postQueryModelRepository.create(postQueryModel, Duration.ofDays(1)));
        log.info("[PostReadService.fetch] fetch data, postId ={}, isPresent={}", postId, postQueryModelOptional.isPresent());
        return postQueryModelOptional;
    }
}
