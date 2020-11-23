package com.redhat.pantheon.extension;

import com.redhat.pantheon.extension.events.document.DocumentVersionPublishedEvent;
import com.redhat.pantheon.extension.events.document.DocumentVersionUnpublishedEvent;
import com.redhat.pantheon.model.assembly.AssemblyVersion;
import com.redhat.pantheon.model.module.ModuleVersion;
import org.apache.sling.event.jobs.JobBuilder;
import org.apache.sling.event.jobs.JobManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_MOCKS;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith({MockitoExtension.class})
class EventsTest {

    @Mock(answer = Answers.RETURNS_MOCKS)
    JobManager jobManager;


    @Test
    void fireModuleVersionPublishedEvent() {
        // Given
        Events events = new Events(jobManager);
        JobBuilder jobBuilder = mock(JobBuilder.class, RETURNS_MOCKS);
        lenient().when(jobManager.createJob(anyString())).thenReturn(jobBuilder);
        lenient().when(jobBuilder.properties(anyMap())).thenReturn(jobBuilder);

        // When
        events.fireEvent(new DocumentVersionPublishedEvent(mock(ModuleVersion.class)), 15);

        // Then
        verify(jobBuilder, times(1)).properties(anyMap());
        verify(jobBuilder, times(1)).schedule();
        verify(jobManager, times(1)).createJob(eq(Events.EVENT_TOPIC_NAME));
    }

    @Test
    void fireModuleVersionUnpublishEvent() {
        // Given
        Events events = new Events(jobManager);
        JobBuilder jobBuilder = mock(JobBuilder.class, RETURNS_MOCKS);
        lenient().when(jobManager.createJob(anyString())).thenReturn(jobBuilder);
        lenient().when(jobBuilder.properties(anyMap())).thenReturn(jobBuilder);

        // When
        events.fireEvent(new DocumentVersionUnpublishedEvent(mock(ModuleVersion.class), null), 15);

        // Then
        verify(jobBuilder, times(1)).properties(anyMap());
        verify(jobBuilder, times(1)).schedule();
        verify(jobManager, times(1)).createJob(eq(Events.EVENT_TOPIC_NAME));
    }

    @Test
    void fireAssemblyVersionPublishedEvent() {
        // Given
        Events events = new Events(jobManager);
        JobBuilder jobBuilder = mock(JobBuilder.class, RETURNS_MOCKS);
        lenient().when(jobManager.createJob(anyString())).thenReturn(jobBuilder);
        lenient().when(jobBuilder.properties(anyMap())).thenReturn(jobBuilder);

        // When
        events.fireEvent(new DocumentVersionPublishedEvent(mock(AssemblyVersion.class)), 15);

        // Then
        verify(jobBuilder, times(1)).properties(anyMap());
        verify(jobBuilder, times(1)).schedule();
        verify(jobManager, times(1)).createJob(eq(Events.EVENT_TOPIC_NAME));
    }

    @Test
    void fireAssemblyVersionUnpublishEvent() {
        // Given
        Events events = new Events(jobManager);
        JobBuilder jobBuilder = mock(JobBuilder.class, RETURNS_MOCKS);
        lenient().when(jobManager.createJob(anyString())).thenReturn(jobBuilder);
        lenient().when(jobBuilder.properties(anyMap())).thenReturn(jobBuilder);

        // When
        events.fireEvent(new DocumentVersionUnpublishedEvent(mock(AssemblyVersion.class), null), 15);

        // Then
        verify(jobBuilder, times(1)).properties(anyMap());
        verify(jobBuilder, times(1)).schedule();
        verify(jobManager, times(1)).createJob(eq(Events.EVENT_TOPIC_NAME));
    }
}