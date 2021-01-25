package com.lawencon.elearning.dao;

import java.util.List;
import com.lawencon.elearning.model.CourseCategory;
import com.lawencon.util.Callback;

/**
 * @author : Galih Dika Permana
 */

public interface CourseCategoryDao {

  List<CourseCategory> getListCourseCategory() throws Exception;

  void insertCourseCategory(CourseCategory courseCategory, Callback before) throws Exception;

  void updateCourseCategory(CourseCategory courseCategory, Callback before) throws Exception;

  void deleteCourseCategory(String id) throws Exception;

  void updateIsActived(String id) throws Exception;
}