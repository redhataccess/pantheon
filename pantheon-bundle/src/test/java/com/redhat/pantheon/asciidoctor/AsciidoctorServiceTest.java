package com.redhat.pantheon.asciidoctor;

import com.redhat.pantheon.conf.GlobalConfig;
import com.redhat.pantheon.sling.ServiceResourceResolverProvider;
import org.asciidoctor.Asciidoctor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class})
class AsciidoctorServiceTest {

    @Test
    void generatePdf() throws IOException {
        AsciidoctorPool pool = mock(AsciidoctorPool.class);
        when(pool.borrowObject()).thenReturn(Asciidoctor.Factory.create());

        AsciidoctorService asciidoctorService = new AsciidoctorService(
                mock(GlobalConfig.class),
                pool,
                mock(ServiceResourceResolverProvider.class)
        );

//        System.out.println(
//                asciidoctorService.generatePdf(null).getAbsolutePath());
    }
}