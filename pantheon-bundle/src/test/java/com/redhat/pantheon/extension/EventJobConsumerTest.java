package com.redhat.pantheon.extension;

import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.consumer.JobConsumer.JobResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
        // (partial mock)
        EventJobConsumer jobConsumer = spy(new EventJobConsumer());
        jobConsumer.extensions.add(extension);
        lenient().when(job.getProperty(Event.class.getName(), Event.class)).thenReturn(event);
        lenient().when(extension.canProcessEvent(event)).thenReturn(true);

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
        lenient().when(extension1.canProcessEvent(event)).thenReturn(true);
        lenient().when(extension2.canProcessEvent(event)).thenReturn(true);
        lenient().doThrow(new Exception()).when(extension1).processEvent(eq(event));
        lenient().doThrow(new Exception()).when(extension2).processEvent(eq(event));
        // (partial mock)
        EventJobConsumer jobConsumer = spy(new EventJobConsumer());
        jobConsumer.extensions.add(extension1);
        jobConsumer.extensions.add(extension2);

        // When
        JobResult result = jobConsumer.process(job);

        // Then
        assertEquals(JobResult.OK, result);
        verify(extension1, times(1)).processEvent(eq(event));
        verify(extension2, times(1)).processEvent(eq(event));
    }

    @Test
    void processExtensionsWhichRejectTheEvent() throws Exception {
        // Given
        Event event = mock(Event.class, Answers.RETURNS_MOCKS);
        EventProcessingExtension extension1 = mock(EventProcessingExtension.class);
        EventProcessingExtension extension2 = mock(EventProcessingExtension.class);
        lenient().when(job.getProperty(Event.class.getName(), Event.class)).thenReturn(event);
        lenient().when(extension1.canProcessEvent(event)).thenReturn(false);
        lenient().when(extension2.canProcessEvent(event)).thenReturn(false);
        // (partial mock)
        EventJobConsumer jobConsumer = spy(new EventJobConsumer());
        jobConsumer.extensions.add(extension1);
        jobConsumer.extensions.add(extension2);

        // When
        JobResult result = jobConsumer.process(job);

        // Then
        assertEquals(JobResult.OK, result);
        verify(extension1, times(0)).processEvent(eq(event));
        verify(extension2, times(0)).processEvent(eq(event));
    }

    @Test
    void processWithNoEvent() {
        // Given
        EventJobConsumer jobConsumer = new EventJobConsumer();

        // When
        JobResult result = jobConsumer.process(job);

        // Then
        assertEquals(JobResult.CANCEL, result);
    }
}