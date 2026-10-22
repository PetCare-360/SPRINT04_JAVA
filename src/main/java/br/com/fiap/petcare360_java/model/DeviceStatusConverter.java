package br.com.fiap.petcare360_java.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class DeviceStatusConverter implements AttributeConverter<DeviceStatusEnum, String> {

    @Override
    public String convertToDatabaseColumn(DeviceStatusEnum status) {
        if (status == null) return null;
        return switch (status) {
            case ACTIVE -> "A";
            case INACTIVE -> "I";
            case MAINTENANCE -> "M";
        };
    }

    @Override
    public DeviceStatusEnum convertToEntityAttribute(String value) {
        if (value == null) return null;
        return switch (value) {
            case "A" -> DeviceStatusEnum.ACTIVE;
            case "I" -> DeviceStatusEnum.INACTIVE;
            case "M" -> DeviceStatusEnum.MAINTENANCE;
            default -> throw new IllegalArgumentException("Status de dispositivo desconhecido: " + value);
        };
    }
}
