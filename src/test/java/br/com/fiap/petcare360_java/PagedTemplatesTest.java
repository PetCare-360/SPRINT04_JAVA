package br.com.fiap.petcare360_java;

import java.util.List;
import java.util.Map;
import java.util.Locale;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.web.csrf.DefaultCsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PagedTemplatesTest {
    @ParameterizedTest
    @CsvSource({"tutor,consultas", "vet,consultas", "admin,consultas",
            "tutor,mensagens", "vet,mensagens", "admin,mensagens",
            "tutor,recomendacoes", "vet,recomendacoes", "admin,recomendacoes"})
    void listPagesRenderPaginationForEveryRole(String role, String section) throws Exception {
        var resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML");
        resolver.setCharacterEncoding("UTF-8");
        var engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);
        var messages = new ResourceBundleMessageSource();
        messages.setBasename("messages");
        messages.setDefaultEncoding("UTF-8");
        engine.setTemplateEngineMessageSource(messages);
        var views = new ThymeleafViewResolver();
        views.setTemplateEngine(engine);
        var mvc = MockMvcBuilders.standaloneSetup(new Pages()).setViewResolvers(views).build();

        String html = mvc.perform(get("/test/paged/" + role + "/" + section).locale(Locale.forLanguageTag("pt-BR")))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertTrue(html.contains("?page=0"), section + " previous page");
        assertTrue(html.contains("?page=2"), section + " next page");
        assertTrue(html.contains("Página"));
        if (section.equals("consultas")) assertTrue(html.contains("status=REQUESTED"));
    }

    @Controller
    static class Pages {
        @GetMapping("/test/paged/{role}/{section}")
        ModelAndView page(@PathVariable String role, @PathVariable String section) {
            var view = new ModelAndView(role + "/" + section);
            view.addAllObjects(Map.of("appointments", List.of(), "messages", List.of(),
                    "recommendations", List.of(), "linkedVeterinarians", List.of(),
                    "linkedPets", List.of(), "pets", List.of(), "users", List.of()));
            view.addObject("pageData", new PageImpl<>(List.of(), PageRequest.of(1, 10), 30));
            view.addObject("pageBasePath", "/" + role + "/" + section);
            view.addObject("selectedStatus", section.equals("consultas") ? "REQUESTED" : null);
            view.addObject("_csrf", new DefaultCsrfToken("X-CSRF-TOKEN", "_csrf", "test"));
            return view;
        }
    }
}
