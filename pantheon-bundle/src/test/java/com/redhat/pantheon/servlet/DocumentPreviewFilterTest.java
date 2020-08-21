package com.redhat.pantheon.servlet;

import com.redhat.pantheon.conf.GlobalConfig;
import com.redhat.pantheon.servlet.assets.ImageServletFilter;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.apache.sling.testing.mock.sling.junit5.SlingContext;
import org.apache.sling.testing.mock.sling.junit5.SlingContextExtension;
import org.apache.sling.testing.mock.sling.servlet.MockRequestDispatcherFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import java.util.Base64;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({SlingContextExtension.class, MockitoExtension.class})
public class DocumentPreviewFilterTest {

    SlingContext sc = new SlingContext(ResourceResolverType.JCR_OAK);

    @Mock
    FilterChain filterChain;

    @Mock
    MockRequestDispatcherFactory requestDispatcherFactory;

    @Test
    void forwardDocumentVariantDraft() throws Exception {
        // Given
        sc.build()
                .resource("/moduleA/en_US/variants/test-atts",
                        "jcr:primaryType", "pant:moduleVariant")
                .commit();
        RequestDispatcher requestDispatcher = mock(RequestDispatcher.class);
        sc.request().setRequestDispatcherFactory(requestDispatcherFactory);
        String uuid = sc.request().getResourceResolver().getResource("/moduleA/en_US/variants/test-atts")
                .getValueMap().get("jcr:uuid", String.class);
        sc.request().setPathInfo("/pantheon/preview/latest/" + uuid);
        when(requestDispatcherFactory.getRequestDispatcher(anyString(), any())).thenReturn(requestDispatcher);
        DocumentPreviewFilter filter = new DocumentPreviewFilter();

        // When
        filter.init(mock(FilterConfig.class));
        filter.doFilter(sc.request(), sc.response(), filterChain);

        // Then
        verify(requestDispatcherFactory).getRequestDispatcher(eq("/moduleA/en_US/variants/test-atts.preview/latest"), any());
        verify(requestDispatcher).forward(any(), any());
    }

    @Test
    void forwardDocumentVariantReleased() throws Exception {
        // Given
        sc.build()
                .resource("/moduleA/en_US/variants/test-atts",
                        "jcr:primaryType", "pant:moduleVariant")
                .commit();
        RequestDispatcher requestDispatcher = mock(RequestDispatcher.class);
        sc.request().setRequestDispatcherFactory(requestDispatcherFactory);
        String uuid = sc.request().getResourceResolver().getResource("/moduleA/en_US/variants/test-atts")
                .getValueMap().get("jcr:uuid", String.class);
        sc.request().setPathInfo("/pantheon/preview/released/" + uuid);
        when(requestDispatcherFactory.getRequestDispatcher(anyString(), any())).thenReturn(requestDispatcher);
        DocumentPreviewFilter filter = new DocumentPreviewFilter();

        // When
        filter.init(mock(FilterConfig.class));
        filter.doFilter(sc.request(), sc.response(), filterChain);

        // Then
        verify(requestDispatcherFactory).getRequestDispatcher(eq("/moduleA/en_US/variants/test-atts.preview/released"), any());
        verify(requestDispatcher).forward(any(), any());
    }

    @Test
    void forwardDocumentDraft() throws Exception {
        // Given
        sc.build()
                .resource("/moduleA",
                        "jcr:primaryType", "pant:module")
                .commit();
        RequestDispatcher requestDispatcher = mock(RequestDispatcher.class);
        sc.request().setRequestDispatcherFactory(requestDispatcherFactory);
        String uuid = sc.request().getResourceResolver().getResource("/moduleA")
                .getValueMap().get("jcr:uuid", String.class);
        sc.request().setPathInfo("/pantheon/preview/latest/" + uuid);
        when(requestDispatcherFactory.getRequestDispatcher(anyString(), any())).thenReturn(requestDispatcher);
        DocumentPreviewFilter filter = new DocumentPreviewFilter();

        // When
        filter.init(mock(FilterConfig.class));
        filter.doFilter(sc.request(), sc.response(), filterChain);

        // Then
        verify(requestDispatcherFactory).getRequestDispatcher(eq("/moduleA.preview/latest"), any());
        verify(requestDispatcher).forward(any(), any());
    }

    @Test
    void forwardDocumentReleased() throws Exception {
        // Given
        sc.build()
                .resource("/moduleA",
                        "jcr:primaryType", "pant:module")
                .commit();
        RequestDispatcher requestDispatcher = mock(RequestDispatcher.class);
        sc.request().setRequestDispatcherFactory(requestDispatcherFactory);
        String uuid = sc.request().getResourceResolver().getResource("/moduleA")
                .getValueMap().get("jcr:uuid", String.class);
        sc.request().setPathInfo("/pantheon/preview/released/" + uuid);
        when(requestDispatcherFactory.getRequestDispatcher(anyString(), any())).thenReturn(requestDispatcher);
        DocumentPreviewFilter filter = new DocumentPreviewFilter();

        // When
        filter.init(mock(FilterConfig.class));
        filter.doFilter(sc.request(), sc.response(), filterChain);

        // Then
        verify(requestDispatcherFactory).getRequestDispatcher(eq("/moduleA.preview/released"), any());
        verify(requestDispatcher).forward(any(), any());
    }
}
