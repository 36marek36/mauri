package com.example.mauri.model.dto.update;

import com.example.mauri.enums.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateRoleDTO {
    private String userId;
    @NotNull(message = "Rola je povinná")
    private Role updateUserRole;
}
