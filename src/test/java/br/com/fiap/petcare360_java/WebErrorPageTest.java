package br.com.fiap.petcare360_java;

import br.com.fiap.petcare360_java.controller.WebFormController;
import br.com.fiap.petcare360_java.exception.ApiException;
import br.com.fiap.petcare360_java.exception.WebExceptionHandler;
import br.com.fiap.petcare360_java.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class WebErrorPageTest {
    @Test
    void rejectedAppointmentFormShowsHtmlErrorWithStatus() throws Exception {
        var appointments = mock(AppointmentService.class);
        doThrow(new ApiException(HttpStatus.CONFLICT, "Esta consulta não pode mais ser cancelada"))
                .when(appointments).cancel(5L);
        var controller = new WebFormController(mock(PetService.class), mock(MessageService.class),
                appointments, mock(RecommendationService.class), mock(VeterinarianLinkService.class),
                mock(CurrentUserService.class), mock(jakarta.validation.Validator.class));

        var resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML");
        resolver.setCharacterEncoding("UTF-8");
        var messages = new ResourceBundleMessageSource();
        messages.setBasename("messages");
        messages.setDefaultEncoding("UTF-8");
        var engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);
        engine.setTemplateEngineMessageSource(messages);
        var views = new ThymeleafViewResolver();
        views.setTemplateEngine(engine);

        var mvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new WebExceptionHandler(messages))
                .setViewResolvers(views).build();
        String html = mvc.perform(post("/appointments/5/cancelar"))
                .andExpect(status().isConflict()).andReturn().getResponse().getContentAsString();
        assertTrue(html.contains("<!doctype html>"));
        assertTrue(html.contains("Esta consulta não pode mais ser cancelada"));
    }
}
