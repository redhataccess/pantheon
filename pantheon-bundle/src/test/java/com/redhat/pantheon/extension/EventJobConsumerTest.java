package com.redhat.pantheon.extension;

import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.consumer.JobConsumer.JobResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.osgi.framework.InvalidSyntaxException;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class})
class EventJobConsumerTest {

    @Mock
    Job job;

    @Test
    void process() throws Exception {
        // Given
        Event event = mock(Event.class, Answers.RETURNS_MOCKS);
        EventProcessingExtension extension = mock(EventProcessingExtension.class);
        lenient().when(job.getProperty(Event.class.getName(), Event.class)).thenReturn(event);
        TestEventJobConsumer jobConsumer = new TestEventJobConsumer(extension);

        // When
        JobResult result = jobConsumer.process(job);

        // Then
        assertEquals(JobResult.OK, result);
        verify(extension, times(1)).processEvent(eq(event));
    }

    @Test
    void processExtensionWhichThrowsException() throws Exception {
        // Given
        Event event = mock(Event.class, Answers.RETURNS_MOCKS);
        EventProcessingExtension extension1 = mock(EventProcessingExtension.class);
        EventProcessingExtension extension2 = mock(EventProcessingExtension.class);
        lenient().when(job.getProperty(Event.class.getName(), Event.class)).thenReturn(event);
        lenient().doThrow(new Exception()).when(extension1).processEvent(eq(event));
        lenient().doThrow(new Exception()).when(extension2).processEvent(eq(event));
        TestEventJobConsumer jobConsumer = new TestEventJobConsumer(extension1, extension2);

        // When
        JobResult result = jobConsumer.process(job);

        // Then
        assertEquals(JobResult.OK, result);
        verify(extension1, times(1)).processEvent(eq(event));
        verify(extension2, times(1)).processEvent(eq(event));
    }

    @Test
    void processWithNoEvent() {
        // Given
        TestEventJobConsumer jobConsumer = new TestEventJobConsumer();

        // When
        JobResult result = jobConsumer.process(job);

        // Then
        assertEquals(JobResult.CANCEL, result);
    }

    private static class TestEventJobConsumer extends EventJobConsumer<EventProcessingExtension> {

        private final EventProcessingExtension[] extensions;

        public TestEventJobConsumer(EventProcessingExtension ... extensions) {
            super(EventProcessingExtension.class);
            this.extensions = extensions;
        }

        @Override
        protected Collection<EventProcessingExtension> getExtensions() {
            return Arrays.asList(extensions);
        }
    }
}