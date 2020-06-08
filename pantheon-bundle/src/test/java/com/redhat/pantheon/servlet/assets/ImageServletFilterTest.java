package com.redhat.pantheon.servlet.assets;

import com.redhat.pantheon.conf.GlobalConfig;
import org.apache.sling.testing.mock.sling.junit5.SlingContext;
import org.apache.sling.testing.mock.sling.junit5.SlingContextExtension;
import org.apache.sling.testing.mock.sling.servlet.MockRequestDispatcherFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;

import java.util.Base64;

import static org.apache.jackrabbit.JcrConstants.JCR_DATA;
import static org.apache.jackrabbit.JcrConstants.JCR_PRIMARYTYPE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith({SlingContextExtension.class, MockitoExtension.class})
class ImageServletFilterTest {

    SlingContext sc = new SlingContext();

    @Mock
    FilterChain filterChain;

    @Mock
    MockRequestDispatcherFactory requestDispatcherFactory;

    @Test
    void doFilter() throws Exception {
        // Given
        String imagePath = "/path/to/my/image.png";
        sc.build()
                .resource(imagePath)
                .commit();
        String imageUrl = Base64.getUrlEncoder().encodeToString(imagePath.getBytes());
        RequestDispatcher requestDispatcher = mock(RequestDispatcher.class);
        sc.request().setRequestDispatcherFactory(requestDispatcherFactory);
        sc.request().setPathInfo(GlobalConfig.IMAGE_PATH_PREFIX + "/" + imageUrl);
        when(requestDispatcherFactory.getRequestDispatcher(anyString(), any())).thenReturn(requestDispatcher);
        ImageServletFilter filter = new ImageServletFilter();

        // When
        filter.init(mock(FilterConfig.class));
        filter.doFilter(sc.request(), sc.response(), filterChain);

        // Then
        verify(requestDispatcherFactory).getRequestDispatcher(eq(imagePath), any());
        verify(requestDispatcher).forward(any(), any());
    }
}
