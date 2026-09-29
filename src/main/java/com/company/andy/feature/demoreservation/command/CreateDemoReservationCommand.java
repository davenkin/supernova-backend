package com.company.andy.feature.demoreservation.command;

import com.company.andy.common.validation.mobilenumber.MobileNumber;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

// Command objects are the mutation data you send to the controller,
// which usually results in state changes in the system.

public record CreateDemoReservationCommand(
        @Schema(description = "Contact mobile number")
        @NotBlank @MobileNumber String mobileNumber) {
}
