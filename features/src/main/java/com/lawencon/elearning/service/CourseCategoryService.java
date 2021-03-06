package com.lawencon.elearning.service;

import java.util.List;
import com.lawencon.elearning.dto.UpdateIsActiveRequestDTO;
import com.lawencon.elearning.dto.course.category.CourseCategoryCreateRequestDTO;
import com.lawencon.elearning.dto.course.category.CourseCategoryResponseDTO;
import com.lawencon.elearning.dto.course.category.CourseCategoryUpdateRequestDTO;

/**
 * @author : Galih Dika Permana
 */

public interface CourseCategoryService {

  List<CourseCategoryResponseDTO> getListCourseCategory() throws Exception;

  void insertCourseCategory(CourseCategoryCreateRequestDTO courseCategory) throws Exception;

  void updateCourseCategory(CourseCategoryUpdateRequestDTO courseCategory) throws Exception;

  void deleteCourseCategory(String id) throws Exception;

  void updateIsActive(UpdateIsActiveRequestDTO data) throws Exception;
}
