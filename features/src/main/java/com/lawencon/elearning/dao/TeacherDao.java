package com.lawencon.elearning.dao;

import java.util.List;
import com.lawencon.elearning.dto.teacher.TeacherProfileDTO;
import com.lawencon.elearning.model.Teacher;
import com.lawencon.util.Callback;

/**
 * @author Dzaky Fadhilla Guci
 */

public interface TeacherDao {


  List<Teacher> getAllTeachers() throws Exception;

  List<Teacher> allTeachersForAdmin() throws Exception;

  void saveTeacher(Teacher data, Callback before) throws Exception;

  Teacher findTeacherById(String id) throws Exception;

  TeacherProfileDTO findTeacherByIdCustom(String id) throws Exception;

  void updateIsActive(String id, String userId) throws Exception;

  void updateTeacher(Teacher data, Callback before) throws Exception;

  Teacher findByUserId(String userId) throws Exception;

  void deleteTeacherById(String id) throws Exception;

  Teacher findByIdForCourse(String id) throws Exception;

  List<String> checkConstraint(String id) throws Exception;


}
