package com.lawencon.elearning.service;

import java.util.List;
import com.lawencon.elearning.dto.UpdateIsActiveRequestDTO;
import com.lawencon.elearning.dto.admin.DashboardStudentResponseDto;
import com.lawencon.elearning.dto.admin.RegisteredStudentMonthlyDto;
import com.lawencon.elearning.dto.course.CourseResponseDTO;
import com.lawencon.elearning.dto.student.RegisterStudentDTO;
import com.lawencon.elearning.dto.student.StudentByCourseResponseDTO;
import com.lawencon.elearning.dto.student.StudentDashboardDTO;
import com.lawencon.elearning.dto.student.StudentReportDTO;
import com.lawencon.elearning.dto.student.StudentUpdateRequestDto;
import com.lawencon.elearning.model.Student;

/**
 * @author WILLIAM
 */
public interface StudentService {

  void insertStudent(RegisterStudentDTO data) throws Exception;

  Student getStudentById(String id) throws Exception;

  Student getStudentProfile(String id) throws Exception;

  void updateStudentProfile(StudentUpdateRequestDto request) throws Exception;

  void deleteStudent(String id) throws Exception;

  void updateIsActive(UpdateIsActiveRequestDTO updateRequest) throws Exception;

  Student getStudentByIdUser(String id) throws Exception;

  StudentDashboardDTO getStudentDashboard(String id) throws Exception;

  List<CourseResponseDTO> getStudentCourse(String id) throws Exception;

  List<StudentByCourseResponseDTO> getListStudentByIdCourse(String idCourse) throws Exception;

  List<Student> getAllStudentByCourseId(String idCourse) throws Exception;

  List<StudentReportDTO> getStudentExamReport(String studentId) throws Exception;

  List<StudentDashboardDTO> getAll() throws Exception;

  DashboardStudentResponseDto getStudentDataForAdmin() throws Exception;

  Integer countTotalStudent() throws Exception;

  List<RegisteredStudentMonthlyDto> countRegisteredStudentMonthly() throws Exception;

}
