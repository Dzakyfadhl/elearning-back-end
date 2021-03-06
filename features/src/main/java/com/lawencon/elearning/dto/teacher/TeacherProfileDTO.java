package com.lawencon.elearning.dto.teacher;

import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.lawencon.elearning.dto.experience.ExperienceResponseDto;
import com.lawencon.elearning.model.Gender;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *  @author Dzaky Fadhilla Guci
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeacherProfileDTO {

  private String id;

  private String username;

  private String firstName;

  private String lastName;

  private String email;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime createdAt;

  private Gender gender;

  private String phone;

  private String titleDegree;

  private String photoId;

  private List<ExperienceResponseDto> experiences;

}
