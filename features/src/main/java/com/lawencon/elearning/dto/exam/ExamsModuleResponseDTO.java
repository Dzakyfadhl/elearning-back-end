package com.lawencon.elearning.dto.exam;

import java.time.LocalDateTime;
import com.lawencon.elearning.model.ExamType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *  @author Dzaky Fadhilla Guci
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExamsModuleResponseDTO {

  private String id;
  private String code;
  private String description;
  private ExamType type;
  private LocalDateTime startTime;
  private LocalDateTime endTime;
  private String fileId;

}