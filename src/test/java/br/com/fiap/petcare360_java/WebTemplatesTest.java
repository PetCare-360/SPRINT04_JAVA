package br.com.fiap.petcare360_java;

import java.time.OffsetDateTime;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Locale;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.stereotype.Controller;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.security.web.csrf.DefaultCsrfToken;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import br.com.fiap.petcare360_java.model.*;
import br.com.fiap.petcare360_java.service.HealthTrendService;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class WebTemplatesTest {
    @ParameterizedTest
    @ValueSource(strings = {"tutor", "vet", "admin"})
    void detailPagesRenderOnlyTheCurrentRoleMenu(String role) throws Exception {
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
        for (String page : List.of("pet-detail", "pet-edit", "appointment-detail", "appointment-edit")) {
            String html = mvc.perform(get("/test/" + role + "/" + page).locale(Locale.ENGLISH))
                    .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
            assertTrue(html.contains("href=\"/" + role + "\""));
            for (String other : List.of("tutor", "vet", "admin")) {
                if (!other.equals(role)) assertFalse(html.contains("href=\"/" + other + "\""), page);
            }
            if (page.equals("appointment-detail")) {
                assertTrue(html.contains("/appointments/1/cancelar"));
                if (!role.equals("tutor")) assertTrue(html.contains("/appointments/1/confirmar"));
                assertTrue(html.contains("Consulta de rotina"));
                assertTrue(html.contains("Golden Retriever"));
                if (role.equals("tutor")) assertTrue(html.contains("/tutor/pets/1/editar"));
                if (role.equals("vet")) assertTrue(html.contains("/vet/pets/1/editar"));
            }
            if (page.equals("pet-detail")) {
                assertTrue(html.contains("Pet details"));
                if (role.equals("admin")) assertFalse(html.contains("class=\"health-trends\""));
                else assertTrue(html.contains("class=\"health-trends\""));
            }
        }
    }

    @Controller
    static class Pages {
        @GetMapping("/test/{role}/{page}")
        ModelAndView page(@PathVariable String role, @PathVariable String page) {
            var tutor = new AppUser();
            tutor.setId(1L);
            tutor.setName("Tutor");
            tutor.setEmail("tutor@example.com");
            var vet = new AppUser();
            vet.setId(2L);
            vet.setName("Veterinario");
            vet.setEmail("vet@example.com");
            var pet = new Pet();
            pet.setId(1L);
            pet.setName("Thor");
            pet.setBreed("Golden Retriever");
            pet.setSpecies("DOG");
            pet.setAge(5);
            pet.setUser(tutor);
            var appointment = new Appointment();
            appointment.setId(1L);
            appointment.setPet(pet);
            appointment.setTutor(tutor);
            appointment.setVeterinarian(vet);
            appointment.setScheduledAt(OffsetDateTime.now());
            appointment.setReason("Consulta de rotina");
            appointment.setStatus(AppointmentStatusEnum.REQUESTED);
            var link = new PetVeterinarian();
            link.setPet(pet);
            link.setVeterinarian(vet);
            var reading = new SensorData();
            reading.setTimestamp(OffsetDateTime.now());
            reading.setTemperature(new BigDecimal("38.5"));
            reading.setHeartRate(120);
            var trend = new HealthTrendService().from(List.of(reading), List.of());
            var view = new ModelAndView(page);
            view.addAllObjects(Map.of("isTutor", role.equals("tutor"), "isVet", role.equals("vet"),
                    "isAdmin", role.equals("admin"), "pet", pet, "appointment", appointment,
                    "links", List.of(link), "linkedVeterinarians", List.of(link),
                    "readings", List.of(reading), "alerts", List.of(), "healthTrend", trend));
            view.addObject("latestReading", reading);
            view.addObject("consultationPet", pet);
            view.addObject("canEditPet", true);
            view.addObject("scheduledAtValue", "2026-09-11T10:00");
            view.addObject("_csrf", new DefaultCsrfToken("X-CSRF-TOKEN", "_csrf", "test"));
            return view;
        }
    }
}
