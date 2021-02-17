package com.lawencon.elearning.service.impl;

import com.lawencon.base.BaseServiceImpl;
import com.lawencon.elearning.dao.StudentDao;
import com.lawencon.elearning.dto.admin.DashboardStudentResponseDto;
import com.lawencon.elearning.dto.admin.RegisteredStudentMonthlyDto;
import com.lawencon.elearning.dto.course.CourseResponseDTO;
import com.lawencon.elearning.dto.student.RegisterStudentDTO;
import com.lawencon.elearning.dto.student.StudentByCourseResponseDTO;
import com.lawencon.elearning.dto.student.StudentDashboardDTO;
import com.lawencon.elearning.dto.student.StudentReportDTO;
import com.lawencon.elearning.dto.student.StudentUpdateRequestDto;
import com.lawencon.elearning.error.DataIsNotExistsException;
import com.lawencon.elearning.error.InternalServerErrorException;
import com.lawencon.elearning.model.DetailExam;
import com.lawencon.elearning.model.Gender;
import com.lawencon.elearning.model.Role;
import com.lawencon.elearning.model.Roles;
import com.lawencon.elearning.model.Student;
import com.lawencon.elearning.model.User;
import com.lawencon.elearning.service.CourseService;
import com.lawencon.elearning.service.DetailExamService;
import com.lawencon.elearning.service.RoleService;
import com.lawencon.elearning.service.StudentService;
import com.lawencon.elearning.service.UserService;
import com.lawencon.elearning.util.TransactionNumberUtils;
import com.lawencon.elearning.util.ValidationUtil;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * @author WILLIAM
 *
 */
@Service
public class StudentServiceImpl extends BaseServiceImpl implements StudentService {

  @Autowired
  private StudentDao studentDao;

  @Autowired
  private CourseService courseService;

  @Autowired
  private UserService userService;

  @Autowired
  private ValidationUtil validationUtil;

  @Autowired
  private RoleService roleService;

  @Autowired
  private DetailExamService detailExamService;

  @Override
  public void insertStudent(RegisterStudentDTO data) throws Exception {
    validationUtil.validate(data);
    Student student = new Student();
    student.setCode(TransactionNumberUtils.generateStudentCode());
    student.setPhone(data.getPhone());
    student.setGender(Gender.valueOf(data.getGender()));

    User user = new User();
    user.setFirstName(data.getFirstName());
    user.setLastName(data.getLastName());
    user.setEmail(data.getEmail());
    user.setUsername(data.getUsername());
    user.setPassword(data.getPassword());
    user.setCreatedAt(LocalDateTime.now());

    Role role = roleService.findByCode(Roles.STUDENT.getCode());
    user.setRole(role);
    student.setUser(user);

    try {
      begin();
      userService.addUser(user);
      studentDao.insertStudent(student, null);
      commit();
    } catch (Exception e) {
      e.printStackTrace();
      rollback();
      throw e;
    }

  }

  @Override
  public Student getStudentById(String id) throws Exception {
    validationUtil.validateUUID(id);
    return Optional.ofNullable(studentDao.getStudentById(id))
        .orElseThrow(() -> new DataIsNotExistsException("id", id));
  }

  @Override
  public void updateStudentProfile(StudentUpdateRequestDto request) throws Exception {
    validationUtil.validate(request);

    User userDb = Optional.ofNullable(userService.getById(request.getUpdatedBy()))
        .orElseThrow(() -> new DataIsNotExistsException("user id", request.getUpdatedBy()));

    if (!userDb.getRole().getCode().equalsIgnoreCase(Roles.ADMIN.getCode())
        && !userDb.getRole().getCode().equalsIgnoreCase(Roles.STUDENT.getCode())) {
      throw new IllegalAccessException("You are unauthorized");
    }

    Student prevStudent = Optional.ofNullable(studentDao.getStudentById(request.getId()))
        .orElseThrow(() -> new DataIsNotExistsException("student id", request.getId()));
    System.out.println(userDb.getRole().getCode());
    if (userDb.getRole().getCode().equalsIgnoreCase(Roles.STUDENT.getCode())) {
      System.out.println("galih 12");
      if (!userDb.getId().equalsIgnoreCase(prevStudent.getUser().getId())) {
        System.out.println("galih 2");
        throw new IllegalAccessException("You Are Unauthorized");
      }
    }

    Student newStudent = new Student();
    newStudent.setCode(prevStudent.getCode());
    newStudent.setCourses(prevStudent.getCourses());
    newStudent.setIsActive(prevStudent.getIsActive());
    newStudent.setUser(prevStudent.getUser());
    newStudent.setPhone(request.getPhone());
    newStudent.setGender(request.getGender());
    newStudent.setUpdatedBy(request.getUpdatedBy());

    User user = new User();
    user.setId(prevStudent.getUser().getId());
    user.setFirstName(request.getFirstName());
    user.setLastName(request.getLastName());
    user.setUsername(request.getUsername());
    user.setUpdatedBy(request.getUpdatedBy());

    try {
      begin();
      userService.updateUser(user);
      setupUpdatedValue(newStudent, () -> prevStudent);
      studentDao.updateStudentProfile(newStudent, null);
      commit();
    } catch (Exception e) {
      rollback();
      throw new InternalServerErrorException(e.fillInStackTrace());
    }
  }

  @Override
  public void deleteStudent(String studentId, String updatedBy) throws Exception {
    validationUtil.validateUUID(studentId, updatedBy);
    try {
      begin();
      Student student = getStudentById(studentId);
      userService.deleteById(student.getUser().getId());
      studentDao.deleteStudentById(student.getId());
      commit();
    } catch (Exception e) {
      e.printStackTrace();
      if (e.getMessage().equals("ID Not Found")) {
        throw new DataIsNotExistsException(e.getMessage());
      }
      updateIsActive(studentId, updatedBy);
    }
  }

  @Override
  public void updateIsActive(String id, String userId) throws Exception {
    begin();
    studentDao.updateIsActive(id, userId);
    commit();
  }

  @Override
  public Student getStudentByIdUser(String id) throws Exception {
    validationUtil.validateUUID(id);
    return Optional.ofNullable(studentDao.getStudentByIdUser(id))
        .orElseThrow(() -> new DataIsNotExistsException("user id", id));
  }

  @Override
  public StudentDashboardDTO getStudentDashboard(String id) throws Exception {
    validationUtil.validateUUID(id);
    Student std = Optional.ofNullable(studentDao.getStudentDashboard(id))
        .orElseThrow(() -> new DataIsNotExistsException("id", id));
    StudentDashboardDTO stdDashboard = new StudentDashboardDTO();
    stdDashboard.setCreatedAt(std.getCreatedAt());
    stdDashboard.setEmail(std.getUser().getEmail());
    stdDashboard.setFirstName(std.getUser().getFirstName());
    stdDashboard.setGender(std.getGender());
    stdDashboard.setId(std.getId());
    stdDashboard.setIdPhoto(std.getUser().getUserPhoto().getId());
    stdDashboard.setLastName(std.getUser().getLastName());
    stdDashboard.setPhone(std.getPhone());
    stdDashboard.setUsername(std.getUser().getUsername());
    return stdDashboard;
  }

  @Override
  public List<CourseResponseDTO> getStudentCourse(String id) throws Exception {
    validationUtil.validateUUID(id);
    Optional.ofNullable(studentDao.getStudentById(id))
        .orElseThrow(() -> new DataIsNotExistsException("id", id));
    List<CourseResponseDTO> listResult = courseService.getCourseByStudentId(id);
    if (listResult.isEmpty()) {
      throw new DataIsNotExistsException("You haven't select any course");
    }
    return listResult;
  }

  @Override
  public List<StudentByCourseResponseDTO> getListStudentByIdCourse(String idCourse)
      throws Exception {
    validationUtil.validateUUID(idCourse);
    List<Student> listStudent = studentDao.getListStudentByIdCourse(idCourse);
    List<StudentByCourseResponseDTO> listDto = new ArrayList<>();
    for (Student element : listStudent) {
      StudentByCourseResponseDTO studentDto = new StudentByCourseResponseDTO();
      studentDto.setId(element.getId());
      studentDto.setCode(element.getCode());
      studentDto.setEmail(element.getUser().getEmail());
      studentDto.setFirstName(element.getUser().getFirstName());
      studentDto.setLastName((element.getUser().getLastName()));
      studentDto.setGender(String.valueOf(element.getGender()));
      studentDto.setPhone(element.getPhone());
      listDto.add(studentDto);
    }
    return listDto;
  }

  public void RegisterCourse(String student, String course) throws Exception {
    courseService.registerCourse(student, course);
  }

  @Override
  public List<Student> getAllStudentByCourseId(String idCourse) throws Exception {
    validationUtil.validateUUID(idCourse);
    return Optional.ofNullable(studentDao.getListStudentByIdCourse(idCourse))
        .orElseThrow(() -> new DataIsNotExistsException("course id", idCourse));
  }

  @Override
  public List<StudentReportDTO> getStudentExamReport(String studentId) throws Exception {
    validationUtil.validateUUID(studentId);
    List<DetailExam> listDetail = detailExamService.getStudentExamReport(studentId);
    List<StudentReportDTO> listResult = new ArrayList<>();
    if (listDetail.isEmpty()) {
      throw new DataIsNotExistsException("id", studentId);
    }
    for (DetailExam detailExam : listDetail) {
      StudentReportDTO studentDTO = new StudentReportDTO();
      studentDTO.setCourseName(
          detailExam.getExam().getModule().getCourse().getCourseType().getName());
      studentDTO
          .setModuleName(detailExam.getExam().getModule().getSubject().getSubjectName());
      studentDTO.setExamType(detailExam.getExam().getExamType().toString());
      studentDTO.setExamTitle(detailExam.getExam().getTitle());
      studentDTO.setDateExam(detailExam.getExam().getTrxDate().toString());
      studentDTO.setGrade(detailExam.getGrade());
      listResult.add(studentDTO);
    }

    return listResult;
  }

  @Override
  public List<StudentDashboardDTO> getAll() throws Exception {
    List<Student> studentList = studentDao.findAll();
    if (studentList.isEmpty()) {
      throw new DataIsNotExistsException("There is no student yet.");
    }
    return studentList.stream()
        .map(student -> new StudentDashboardDTO(
            student.getId(),
            student.getCode(),
            student.getUser().getUsername(),
            student.getUser().getFirstName(),
            student.getUser().getLastName(),
            student.getUser().getEmail(),
            student.getPhone(),
            student.getGender(),
            student.getUser().getUserPhoto().getId(),
            student.getCreatedAt(),
            student.getIsActive()))
        .collect(Collectors.toList());
  }

  @Override
  public DashboardStudentResponseDto getStudentDataForAdmin() throws Exception {
    DashboardStudentResponseDto studentDashboardData = studentDao.countStudentData();
    studentDashboardData.setRegisteredToCourse(courseService.getRegisterStudent());
    studentDashboardData.setVerified(0);
    return studentDashboardData;
  }

  @Override
  public Integer countTotalStudent() throws Exception {
    return studentDao.countTotalStudent();
  }

  @Override
  public List<RegisteredStudentMonthlyDto> countRegisteredStudentMonthly() throws Exception {
    List<RegisteredStudentMonthlyDto> listDto =
        studentDao.countTotalRegisteredStudent();
    if (listDto.isEmpty()) {
      throw new DataIsNotExistsException("No register student yet");
    }
    return listDto;
  }

}
