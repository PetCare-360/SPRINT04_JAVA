package br.com.fiap.petcare360_java.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PetBasicRequest(
        @NotBlank(message = "{validation.pet.name.required}")
        @Size(max = 120, message = "{validation.name.size}")
        String name,
        @NotNull(message = "{validation.age.required}")
        @Min(value = 0, message = "{validation.age.positive}")
        @Max(value = 40, message = "{validation.age.range}")
        Integer age,
        @NotNull(message = "{validation.weight.required}")
        @DecimalMin(value = "0.10", message = "{validation.weight.positive}")
        @DecimalMax(value = "200.00", message = "{validation.weight.range}")
        BigDecimal weight,
        @NotBlank(message = "{validation.breed.required}")
        @Size(max = 120, message = "{validation.breed.size}")
        String breed,
        @NotBlank(message = "{validation.species.required}")
        @Size(max = 40, message = "{validation.species.size}")
        String species,
        @NotBlank(message = "{validation.collar.required}")
        @Size(max = 80, message = "{validation.collar.size}")
        String deviceId) {
}
