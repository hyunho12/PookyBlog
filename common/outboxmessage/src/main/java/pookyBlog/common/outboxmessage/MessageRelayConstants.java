package pookyBlog.common.outboxmessage;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MessageRelayConstants {
    public static final int SHARD_COUNT = 4;
}
