package com.lawencon.elearning.dto.module;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import com.lawencon.elearning.dto.ScheduleRequestDTO;
import lombok.Data;

/**
 * @author : Galih Dika Permana
 */
@Data
public class ModulRequestDTO {

  @NotBlank
  private String moduleCode;
  @NotBlank
  private String moduleCreatedBy;
  @NotBlank
  private String moduleTittle;
  @NotBlank
  private String moduleDescription;

  @NotBlank
  private String courseId;

  @NotBlank
  private String subjectId;

  @NotNull
  private ScheduleRequestDTO scheduleRequestDTO;
}
