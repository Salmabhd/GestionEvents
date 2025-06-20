package com.example.projetsdr.converter;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;
import jakarta.faces.convert.FacesConverter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@FacesConverter("localDateTimeConverter")
public class LocalDateTimeConverter implements Converter<LocalDateTime> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public LocalDateTime getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        try {
            // Essayer le format datetime-local HTML5
            if (value.contains("T")) {
                return LocalDateTime.parse(value, FORMATTER);
            }
            // Essayer d'autres formats possibles
            else if (value.contains("/")) {
                return LocalDateTime.parse(value, DISPLAY_FORMATTER);
            }
            // Format par défaut
            else {
                return LocalDateTime.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            }
        } catch (DateTimeParseException e) {
            throw new ConverterException("Impossible de convertir '" + value + "' en LocalDateTime. Format attendu: yyyy-MM-dd'T'HH:mm");
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, LocalDateTime value) {
        if (value == null) {
            return "";
        }

        try {
            return value.format(FORMATTER);
        } catch (Exception e) {
            throw new ConverterException("Impossible de convertir LocalDateTime en String: " + e.getMessage());
        }
    }
}