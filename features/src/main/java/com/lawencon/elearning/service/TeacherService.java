package com.lawencon.elearning.service;

import java.util.List;
import com.lawencon.elearning.dto.UpdateIsActiveRequestDTO;
import com.lawencon.elearning.dto.admin.DashboardTeacherResponseDto;
import com.lawencon.elearning.dto.teacher.DashboardTeacherDTO;
import com.lawencon.elearning.dto.teacher.TeacherForAdminDTO;
import com.lawencon.elearning.dto.teacher.TeacherProfileDTO;
import com.lawencon.elearning.dto.teacher.TeacherReportResponseDTO;
import com.lawencon.elearning.dto.teacher.TeacherRequestDTO;
import com.lawencon.elearning.dto.teacher.UpdateTeacherRequestDTO;
import com.lawencon.elearning.model.Teacher;

/**
 * @author Dzaky Fadhilla Guci
 */

public interface TeacherService {

  List<TeacherForAdminDTO> getAllTeachers() throws Exception;

  List<TeacherForAdminDTO> allTeachersForAdmin() throws Exception;

  void saveTeacher(TeacherRequestDTO data) throws Exception;

  Teacher findTeacherById(String id) throws Exception;

  TeacherProfileDTO findTeacherByIdCustom(String id) throws Exception;

  void updateIsActive(UpdateIsActiveRequestDTO deleteReq) throws Exception;

  void updateTeacher(UpdateTeacherRequestDTO data) throws Exception;

  Teacher getByUserId(String userId) throws Exception;

  void deleteTeacherById(String teacherId) throws Exception;

  Teacher findByIdForCourse(String id) throws Exception;

  List<DashboardTeacherDTO> getTeacherDashboard(String id) throws Exception;

  List<TeacherReportResponseDTO> getTeacherDetailCourseReport(String moduleId) throws Exception;

  DashboardTeacherResponseDto getDashboardTeacher() throws Exception;

  void verifyRegisterStudentCourse(String studentCourseId, String teacherId, String email)
      throws Exception;

}
