package com.covenantcode.crm.utils;

import com.covenantcode.crm.entity.User;
import com.covenantcode.crm.entity.enums.RoleName;
import lombok.experimental.UtilityClass;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@UtilityClass
public class Utils {

    public Long extractId(Authentication authentication){
        return ((User) authentication.getPrincipal()).getId();
    }

    public static boolean hasRole(User user, RoleName roleName) {
        return user != null
                && user.getRole() != null
                && roleName != null
                && roleName.equals(user.getRole().getName());
    }

    public static boolean isOnlyTeacher(User user) {
        return hasRole(user, RoleName.TEACHER);
    }
}
