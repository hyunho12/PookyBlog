package pookyBlog.common.snowflake;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

@Component
public class Snowflake {
	private static final int UNUSED_BITS = 1;
	private static final int EPOCH_BITS = 41;
	private static final int NODE_ID_BITS = 10;
	private static final int SEQUENCE_BITS = 12;

	private static final long maxNodeId = (1L << NODE_ID_BITS) - 1;
	private static final long maxSequence = (1L << SEQUENCE_BITS) - 1;

	private final long nodeId;
	// UTC = 2024-01-01T00:00:00Z
	private final long startTimeMillis = 1704067200000L;

	private long lastTimeMillis = startTimeMillis;
	private long sequence = 0L;

	public Snowflake() {
		this(0L);
	}

	@Autowired
	public Snowflake(@Value("${snowflake.worker-id:0}") long nodeId) {
		if (nodeId < 0 || nodeId > maxNodeId) {
			throw new IllegalArgumentException("snowflake.worker-id must be between 0 and " + maxNodeId);
		}
		this.nodeId = nodeId;
	}

	public synchronized long nextId() {
		long currentTimeMillis = System.currentTimeMillis();

		if (currentTimeMillis < lastTimeMillis) {
			throw new IllegalStateException("Invalid Time");
		}

		if (currentTimeMillis == lastTimeMillis) {
			sequence = (sequence + 1) & maxSequence;
			if (sequence == 0) {
				currentTimeMillis = waitNextMillis(currentTimeMillis);
			}
		} else {
			sequence = 0;
		}

		lastTimeMillis = currentTimeMillis;

		return ((currentTimeMillis - startTimeMillis) << (NODE_ID_BITS + SEQUENCE_BITS))
			| (nodeId << SEQUENCE_BITS)
			| sequence;
	}

	private long waitNextMillis(long currentTimestamp) {
		while (currentTimestamp <= lastTimeMillis) {
			currentTimestamp = System.currentTimeMillis();
		}
		return currentTimestamp;
	}
}
