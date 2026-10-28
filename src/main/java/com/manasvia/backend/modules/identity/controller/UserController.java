package com.manasvia.backend.modules.identity.controller;

import com.manasvia.backend.common.response.ApiResponse;
import com.manasvia.backend.common.security.UserPrincipal;
import com.manasvia.backend.modules.identity.dto.UserProfileResponse;
import com.manasvia.backend.modules.identity.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public REST Controller managing authenticated user account resources.
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "01. Users & Accounts", description = "User profile inspection, account state, and preferences")
public class UserController {

    private final UserService userService;

    /**
     * Retrieves account profile details for the currently authenticated session.
     *
     * @param principal Injected user identity from verified Bearer JWT.
     * @return Standard {@link ApiResponse} wrapping user account profile details.
     */
    @GetMapping("/me")
    @Operation(
            summary = "Fetch authenticated user account profile",
            description = "Returns current session identity details extracted from the validated Bearer JWT.",
            security = @SecurityRequirement(name = "BearerAuth")
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "User profile retrieved successfully."
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Missing, expired, or invalid Bearer token."
            )
    })
    public ResponseEntity<ApiResponse<UserProfileResponse>> getCurrentUser(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        UserProfileResponse response = userService.getUserProfile(principal.id());
        return ResponseEntity.ok(ApiResponse.ok("User profile retrieved successfully", response));
    }
}
