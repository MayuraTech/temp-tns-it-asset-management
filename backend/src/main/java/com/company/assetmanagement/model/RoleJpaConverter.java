package com.company.assetmanagement.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Maps {@link Role} to the string stored in {@code UserRoles.Role}.
 * The column uses human-readable values ({@link Role#getValue()}) such as {@code Administrator},
 * while the Java enum constants are {@code ADMINISTRATOR}, etc.
 */
@Converter(autoApply = false)
public class RoleJpaConverter implements AttributeConverter<Role, String> {

    @Override
    public String convertToDatabaseColumn(Role role) {
        return role == null ? null : role.getValue();
    }

    @Override
    public Role convertToEntityAttribute(String dbData) {
        return Role.fromPersistedString(dbData);
    }
}
