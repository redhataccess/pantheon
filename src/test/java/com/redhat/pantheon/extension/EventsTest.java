package com.redhat.pantheon.extension;

import com.redhat.pantheon.model.module.ModuleRevision;
import org.apache.sling.event.jobs.JobManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith({MockitoExtension.class})
class EventsTest {

    @Mock(answer = Answers.RETURNS_MOCKS)
    JobManager jobManager;

    @Mock
    ModuleRevision moduleRevision;

    @Test
    void fireModuleRevisionPublishedEvent() {
        // Given
        Events events = new Events(jobManager);

        // When
        events.fireModuleRevisionPublishedEvent(moduleRevision);

        // Then
        verify(jobManager, times(1)).createJob(eq(Events.MODULE_POST_PUBLISH_EVENT));
    }
}