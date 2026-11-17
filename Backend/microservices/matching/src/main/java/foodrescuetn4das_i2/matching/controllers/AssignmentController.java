package foodrescuetn4das_i2.matching.controllers;

import foodrescuetn4das_i2.matching.entity.DriverAssignment;
import foodrescuetn4das_i2.matching.service.MatchingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/assignments")
@Tag(name = "Driver assignments")
public class AssignmentController {
    private final MatchingService service;
    public AssignmentController(MatchingService service) { this.service = service; }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancel active assignment to permit reassignment")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Assignment cancelled"), @ApiResponse(responseCode = "404", description = "Assignment not found"), @ApiResponse(responseCode = "409", description = "Assignment cannot be cancelled")})
    public PickupController.AssignmentResponse cancel(@PathVariable UUID id,
             @RequestHeader(value = "X-Actor-Id", required = false) String actor) {
        return PickupController.AssignmentResponse.from(service.cancelAssignment(id, actor));
    }
}
