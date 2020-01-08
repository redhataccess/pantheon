package com.redhat.pantheon.extension;

import com.redhat.pantheon.extension.events.ModuleVersionPublishedEvent;
import com.redhat.pantheon.model.module.ModuleVersion;
import org.apache.sling.event.jobs.JobBuilder;
import org.apache.sling.event.jobs.JobManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class})
class EventsTest {

    @Mock(answer = Answers.RETURNS_MOCKS)
    JobManager jobManager;

    @Mock
    ModuleVersion moduleVersion;

    @Test
    void fireModuleVersionPublishedEvent() {
        // Given
        Events events = new Events(jobManager);
        JobBuilder jobBuilder = mock(JobBuilder.class, RETURNS_MOCKS);
        lenient().when(jobManager.createJob(anyString())).thenReturn(jobBuilder);
        lenient().when(jobBuilder.properties(anyMap())).thenReturn(jobBuilder);

        // When
        events.fireEvent(new ModuleVersionPublishedEvent("/a/odule/version/path"), 15);

        // Then
        verify(jobBuilder, times(1)).properties(anyMap());
        verify(jobBuilder, times(1)).add();
        verify(jobManager, times(1)).createJob(eq(Events.EVENT_TOPIC_NAME));
    }
}