package com.luongnm93.my_spring_batch.employee.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Request body for triggering the employee import batch job")
public class EmployeeImportRequestDto {

    @NotBlank
    @Schema(description = "Name of the previously uploaded .gz file to import", example = "employees_20260507.csv.gz")
    private String fileName;
}
