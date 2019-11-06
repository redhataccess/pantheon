package com.redhat.pantheon.use;

import com.redhat.pantheon.use.DateFormatter;
import org.apache.sling.testing.mock.sling.junit5.SlingContextExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Calendar;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith({SlingContextExtension.class, MockitoExtension.class})
public class DateFormatterTest {

    @Test
    @DisplayName("Date dateFormatter must not be null")
    public void testFormatDate() throws Exception {
        //Given
        DateFormatter dateFormatter = new DateFormatter("", Calendar.getInstance());

        //When

        //Then
        assertEquals(true,dateFormatter.value != null );
    }
}
