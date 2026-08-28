package pookyBlog.view.Service;

import org.junit.jupiter.api.Test;
import pookyBlog.view.Repository.PostViewCountRepository;
import pookyBlog.view.Repository.PostViewDistributedLockRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PostViewServiceTest {
    private final PostViewCountRepository countRepository = mock(PostViewCountRepository.class);
    private final PostViewCountBackUpProcessor backupProcessor = mock(PostViewCountBackUpProcessor.class);
    private final PostViewDistributedLockRepository lockRepository = mock(PostViewDistributedLockRepository.class);
    private final PostViewService service = new PostViewService(countRepository, backupProcessor, lockRepository);

    @Test
    void firstVisitIncreasesAndDuplicateVisitWithinTtlKeepsCount() {
        when(lockRepository.lock(any(), any(), any())).thenReturn(true, false);
        when(countRepository.increase(10L)).thenReturn(6L);
        when(countRepository.read(10L)).thenReturn(6L);

        assertThat(service.increase(10L, 20L)).isEqualTo(6L);
        assertThat(service.increase(10L, 20L)).isEqualTo(6L);

        verify(countRepository).increase(10L);
        verify(backupProcessor, never()).backUp(any(), any());
    }

    @Test
    void batchCountsUseOneRepositoryCallAndPreserveZero() {
        when(countRepository.readAll(List.of(1L, 2L, 3L)))
                .thenReturn(java.util.Map.of(1L, 10L, 2L, 5L, 3L, 0L));

        assertThat(service.counts(List.of(1L, 2L, 3L)))
                .containsEntry(1L, 10L)
                .containsEntry(2L, 5L)
                .containsEntry(3L, 0L);
        verify(countRepository, times(1)).readAll(List.of(1L, 2L, 3L));
        verify(countRepository, never()).read(any());
    }
}
