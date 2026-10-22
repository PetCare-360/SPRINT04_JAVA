package br.com.fiap.petcare360_java.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/** Converte o percentual recebido pela API para as faixas do schema Oracle. */
@Converter
public class ActivityLevelConverter implements AttributeConverter<Integer, String> {

    @Override
    public String convertToDatabaseColumn(Integer percent) {
        if (percent == null) return null;
        if (percent < 0 || percent > 100) throw new IllegalArgumentException("Atividade deve estar entre 0 e 100");
        return percent < 34 ? "LOW" : percent < 67 ? "MEDIUM" : "HIGH";
    }

    @Override
    public Integer convertToEntityAttribute(String level) {
        if (level == null) return null;
        return switch (level) {
            case "LOW" -> 10;
            case "MEDIUM" -> 50;
            case "HIGH" -> 90;
            default -> throw new IllegalArgumentException("Atividade desconhecida: " + level);
        };
    }
}
