package com.redhat.pantheon.use;

import com.redhat.pantheon.servlet.ServletUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlatformDataTest {

    @Test
    @DisplayName("Jar buildDate must not be set at build time")
    public void testJarBuildDate() throws Exception {
        //Given
        PlatformData data = new PlatformData();

        //When

        //Then
        assertEquals(true,data.getJarBuildDate().contains("Unable to determine build date") );
    }
}