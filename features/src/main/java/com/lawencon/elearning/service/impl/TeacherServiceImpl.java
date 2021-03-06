package com.lawencon.elearning.service.impl;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.SortedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.lawencon.base.BaseServiceImpl;
import com.lawencon.elearning.dao.TeacherDao;
import com.lawencon.elearning.dto.UpdateIsActiveRequestDTO;
import com.lawencon.elearning.dto.admin.DashboardTeacherResponseDto;
import com.lawencon.elearning.dto.experience.ExperienceResponseDto;
import com.lawencon.elearning.dto.teacher.DashboardTeacherDTO;
import com.lawencon.elearning.dto.teacher.TeacherForAdminDTO;
import com.lawencon.elearning.dto.teacher.TeacherProfileDTO;
import com.lawencon.elearning.dto.teacher.TeacherReportResponseDTO;
import com.lawencon.elearning.dto.teacher.TeacherRequestDTO;
import com.lawencon.elearning.dto.teacher.UpdateTeacherRequestDTO;
import com.lawencon.elearning.error.DataIsNotExistsException;
import com.lawencon.elearning.error.IllegalRequestException;
import com.lawencon.elearning.model.Course;
import com.lawencon.elearning.model.Module;
import com.lawencon.elearning.model.Role;
import com.lawencon.elearning.model.Roles;
import com.lawencon.elearning.model.Teacher;
import com.lawencon.elearning.model.User;
import com.lawencon.elearning.service.CourseService;
import com.lawencon.elearning.service.DetailExamService;
import com.lawencon.elearning.service.ExperienceService;
import com.lawencon.elearning.service.ModuleService;
import com.lawencon.elearning.service.RoleService;
import com.lawencon.elearning.service.StudentCourseService;
import com.lawencon.elearning.service.TeacherService;
import com.lawencon.elearning.service.UserService;
import com.lawencon.elearning.util.ValidationUtil;

/**
 * @author Dzaky Fadhilla Guci
 */

@Service
public class TeacherServiceImpl extends BaseServiceImpl implements TeacherService {

  @Autowired
  private TeacherDao teacherDao;

  @Autowired
  private ExperienceService experienceService;

  @Autowired
  private RoleService roleService;

  @Autowired
  private UserService userService;

  @Autowired
  private ValidationUtil validUtil;

  @Autowired
  private CourseService courseService;

  @Autowired
  private ModuleService moduleService;

  @Autowired
  private StudentCourseService studentCourseService;

  @Autowired
  private DetailExamService detailExamService;

  @Override
  public List<TeacherForAdminDTO> getAllTeachers() throws Exception {
    List<Teacher> teacherList = teacherDao.getAllTeachers();
    if (teacherList.isEmpty()) {
      return Collections.emptyList();
    }

    List<TeacherForAdminDTO> responseList = new ArrayList<>();
    teacherList.forEach(teacher -> {
      responseList.add(new TeacherForAdminDTO(
          teacher.getId(),
          teacher.getCode(),
          teacher.getUser().getUsername(),
          teacher.getUser().getFirstName(),
          teacher.getUser().getLastName(),
          teacher.getTitleDegree(),
          teacher.getUser().getEmail(),
          teacher.getPhone(),
          teacher.getGender(),
          teacher.getUser().getUserPhoto().getId(),
          teacher.getIsActive()));
    });
    return responseList;
  }

  @Override
  public List<TeacherForAdminDTO> allTeachersForAdmin() throws Exception {
    return Optional.ofNullable(teacherDao.allTeachersForAdmin())
        .orElse(Collections.emptyList());
  }

  @Override
  public void saveTeacher(TeacherRequestDTO data) throws Exception {
    validUtil.validate(data);
    User userValidate = Optional.ofNullable(userService.getById(data.getCreatedBy()))
        .orElseThrow(() -> new DataIsNotExistsException("id", data.getCreatedBy()));

    if (!userValidate.getRole().getCode().equals(Roles.ADMIN.getCode())) {
      throw new IllegalAccessException(
          String.format("Illegal access role with Id : %s.", data.getCreatedBy()));
    }

    User user = new User();
    user.setFirstName(data.getFirstName());
    user.setLastName(data.getLastName());
    user.setUsername(data.getUsername());
    user.setPassword(data.getPassword());
    user.setEmail(data.getEmail());
    user.setCreatedBy(data.getCreatedBy());

    Role role = roleService.findByCode(Roles.TEACHER.getCode());
    user.setRole(role);

    Teacher teacher = new Teacher();
    teacher.setCode(data.getCode());
    teacher.setPhone(data.getPhone());
    teacher.setGender(data.getGender());
    teacher.setTitleDegree(data.getTitleDegree());
    teacher.setCreatedAt(LocalDateTime.now());
    teacher.setCreatedBy(data.getCreatedBy());

    try {
      begin();
      userService.addUser(user);
      teacher.setUser(user);
      teacherDao.saveTeacher(teacher, null);
      commit();
    } catch (Exception e) {
      e.printStackTrace();
      rollback();
      throw e;
    }
  }

  @Override
  public Teacher findTeacherById(String id) throws Exception {
    validUtil.validateUUID(id);
    return Optional.ofNullable(teacherDao.findTeacherById(id))
        .orElseThrow(() -> new DataIsNotExistsException("id", id));
  }

  @Override
  public TeacherProfileDTO findTeacherByIdCustom(String id) throws Exception {
    validUtil.validateUUID(id);
    TeacherProfileDTO teacher = Optional.ofNullable(teacherDao.findTeacherByIdCustom(id))
        .orElseThrow(() -> new DataIsNotExistsException("id", id));

    List<ExperienceResponseDto> experiences = experienceService.getAllByTeacherId(id);
    teacher.setExperiences(experiences);
    return teacher;
  }

  @Override
  public void updateTeacher(UpdateTeacherRequestDTO data) throws Exception {
    validUtil.validate(data);

    User userValidate = Optional.ofNullable(userService.getById(data.getUpdatedBy()))
        .orElseThrow(() -> new DataIsNotExistsException("id", data.getUpdatedBy()));

    if (!(userValidate.getRole().getCode().equals(Roles.ADMIN.getCode())
        || (userValidate.getRole().getCode().equals(Roles.TEACHER.getCode())
        && teacherDao.validateTeacherUpdatedBy(data.getId(), data.getUpdatedBy()) == 1))) {
      throw new IllegalAccessException("Role not match");
    }

    Teacher teacherDB = Optional.ofNullable(findTeacherById(data.getId()))
        .orElseThrow(() -> new DataIsNotExistsException("id", data.getId()));

    teacherDB.setPhone(data.getPhone());
    teacherDB.setTitleDegree(data.getTitleDegree());
    teacherDB.setGender(data.getGender());
    teacherDB.setUpdatedBy(data.getUpdatedBy());
    teacherDB.setUpdatedAt(LocalDateTime.now());

    User user = new User();
    user.setId(teacherDB.getUser().getId());
    user.setUsername(data.getUsername());
    user.setFirstName(data.getFirstName());
    user.setLastName(data.getLastName());
    user.setUpdatedBy(data.getUpdatedBy());

    try {
      begin();
      userService.updateUser(user);
      teacherDao.updateTeacher(teacherDB, null);
      commit();
    } catch (Exception e) {
      e.printStackTrace();
      rollback();
      throw e;
    }
  }

  @Override
  public Teacher getByUserId(String userId) throws Exception {
    validUtil.validateUUID(userId);
    return Optional.ofNullable(teacherDao.findByUserId(userId))
        .orElseThrow(() -> new DataIsNotExistsException("User Id", userId));
  }

  @Override
  public void deleteTeacherById(String teacherId) throws Exception {
    validUtil.validateUUID(teacherId);

    try {
      begin();
      teacherDao.deleteTeacherById(teacherId);
      String idUser = teacherDao.getUserId(teacherId);
      if (idUser != null) {
        userService.deleteById(idUser);
      }
      commit();
    } catch (Exception e) {
      e.printStackTrace();
      rollback();
      if (e.getMessage().contains("referenced")) {
        throw new IllegalRequestException("Teacher already used");
      }
      throw e;
    }
  }

  @Override
  public void updateIsActive(UpdateIsActiveRequestDTO deleteReq) throws Exception {
    validUtil.validate(deleteReq);

    try {
      begin();
      teacherDao.updateIsActive(deleteReq.getId(), deleteReq.getUpdatedBy(), deleteReq.getStatus());
      String idUser = teacherDao.getUserId(deleteReq.getId());
      if (idUser != null) {
        userService.updateActivateStatus(idUser, deleteReq.getStatus(), deleteReq.getUpdatedBy());
      }
      commit();

    } catch (Exception e) {
      e.printStackTrace();
      rollback();
      throw e;
    }
  }

  @Override
  public Teacher findByIdForCourse(String id) throws Exception {
    validUtil.validateUUID(id);
    return Optional.ofNullable(teacherDao.findByIdForCourse(id))
        .orElseThrow(() -> new DataIsNotExistsException("id", id));
  }

  @Override
  public List<DashboardTeacherDTO> getTeacherDashboard(String id) throws Exception {
    SortedMap<Course, Integer[]> resultMap = courseService.getTeacherCourse(id);
    if (resultMap.isEmpty()) {
      return Collections.emptyList();
    }

    List<DashboardTeacherDTO> listResult = new ArrayList<>();
    resultMap.keySet().forEach(course -> {
      DashboardTeacherDTO dashboard = new DashboardTeacherDTO();
      dashboard.setId(course.getId());
      dashboard.setCode(course.getCode());
      dashboard.setName(course.getCourseType().getName());
      dashboard.setDescription(course.getDescription());
      dashboard.setCapacity(course.getCapacity());
      dashboard.setTotalStudent(resultMap.get(course)[0]);
      dashboard.setTotalModule(resultMap.get(course)[1]);
      dashboard.setPeriodEnd(course.getPeriodEnd());
      dashboard.setPeriodStart(course.getPeriodStart());
      listResult.add(dashboard);
    });
    return listResult;
  }

  @Override
  public List<TeacherReportResponseDTO> getTeacherDetailCourseReport(String moduleId)
      throws Exception {
    validUtil.validateUUID(moduleId);
    Module module = moduleService.getModuleById(moduleId);
    if (module == null) {
      throw new DataIsNotExistsException("module id", moduleId);
    }
    List<TeacherReportResponseDTO> listResult = teacherDao.getTeacherDetailCourseReport(moduleId);
    if (listResult.isEmpty()) {
      TeacherReportResponseDTO teacherDTO = new TeacherReportResponseDTO();
      teacherDTO.setStudentFirstName(null);
      teacherDTO.setStudentLastName(null);
      listResult.add(teacherDTO);
    }

    NumberFormat numberFormat = new DecimalFormat("#.00");
    listResult.forEach(val -> {
      Integer totalAssignment = 0;
      Double avgScore = 0.0;
      Integer totalExam = 0;
      try {
        totalExam = teacherDao.getTotalExamByModuleId(moduleId);
         totalAssignment =
            detailExamService.getTotalAssignmentStudent(moduleId, val.getStudentId());
         avgScore =
            detailExamService.getAvgScoreAssignmentStudent(moduleId, val.getStudentId());
        val.setTotalExam(totalExam);
        val.setAvgScore(avgScore);
        val.setTotalAssignment(totalAssignment);
      } catch (Exception e) {
        e.printStackTrace();
      }
      
      String studentName = val.getStudentFirstName() + " " + val.getStudentLastName();
      val.setStudentFirstName(studentName);
      val.setStudentLastName("");
      if (val.getAvgScore() == null) {
        val.setAvgScore(0.0);
      } else {
        val.setAvgScore(avgScore / (double) totalExam);
      }

      val.setNotAssignment(totalExam - totalAssignment);
      val.setAvgScore(Double.valueOf(numberFormat.format(val.getAvgScore().doubleValue())));
    });

    return listResult;
  }

  @Override
  public DashboardTeacherResponseDto getDashboardTeacher() throws Exception {
    Integer experienceTeachers = teacherDao.countTeachersHaveExperience();
    DashboardTeacherResponseDto responseResult =
        Optional.ofNullable(teacherDao.countTotalTeachers())
            .orElseThrow(() -> new IllegalRequestException("Failed get count data teachers"));
    responseResult.setExperienced(experienceTeachers);

    return responseResult;
  }

  @Override
  public void verifyRegisterStudentCourse(String studentCourseId, String teacherId, String email)
      throws Exception {
    validUtil.validateUUID(studentCourseId, teacherId);
    Teacher teacher = teacherDao.findTeacherById(teacherId);
    studentCourseService.getStudentCourseById(studentCourseId);
    if (teacher == null) {
      throw new DataIsNotExistsException("teacher id" + teacherId);
    }
    studentCourseService.verifyRegisterCourse(studentCourseId, teacher.getUser().getId(), email);
  }

}
