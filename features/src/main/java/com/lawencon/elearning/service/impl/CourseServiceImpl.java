package com.lawencon.elearning.service.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.SortedMap;
import java.util.function.BiConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.lawencon.base.BaseServiceImpl;
import com.lawencon.elearning.dao.CourseDao;
import com.lawencon.elearning.dto.EmailSetupDTO;
import com.lawencon.elearning.dto.StudentCourseRegisterRequestDTO;
import com.lawencon.elearning.dto.StudentListByCourseResponseDTO;
import com.lawencon.elearning.dto.UpdateIsActiveRequestDTO;
import com.lawencon.elearning.dto.admin.DashboardCourseResponseDto;
import com.lawencon.elearning.dto.course.CourseAdminResponseDTO;
import com.lawencon.elearning.dto.course.CourseCreateRequestDTO;
import com.lawencon.elearning.dto.course.CourseProgressResponseDTO;
import com.lawencon.elearning.dto.course.CourseResponseDTO;
import com.lawencon.elearning.dto.course.CourseUpdateRequestDTO;
import com.lawencon.elearning.dto.course.DashboardCourseResponseDTO;
import com.lawencon.elearning.dto.course.DetailCourseResponseDTO;
import com.lawencon.elearning.dto.course.UpdateStatusRequestDTO;
import com.lawencon.elearning.dto.experience.ExperienceResponseDto;
import com.lawencon.elearning.dto.module.ModuleListReponseDTO;
import com.lawencon.elearning.dto.module.ModuleResponseDTO;
import com.lawencon.elearning.dto.teacher.CourseAttendanceReportByTeacher;
import com.lawencon.elearning.dto.teacher.TeacherForAvailableCourseDTO;
import com.lawencon.elearning.error.DataAlreadyExistException;
import com.lawencon.elearning.error.DataIsNotExistsException;
import com.lawencon.elearning.error.IllegalRequestException;
import com.lawencon.elearning.model.Course;
import com.lawencon.elearning.model.CourseCategory;
import com.lawencon.elearning.model.CourseStatus;
import com.lawencon.elearning.model.CourseType;
import com.lawencon.elearning.model.File;
import com.lawencon.elearning.model.GeneralCode;
import com.lawencon.elearning.model.Roles;
import com.lawencon.elearning.model.Student;
import com.lawencon.elearning.model.Teacher;
import com.lawencon.elearning.model.User;
import com.lawencon.elearning.service.CourseService;
import com.lawencon.elearning.service.EmailService;
import com.lawencon.elearning.service.ExperienceService;
import com.lawencon.elearning.service.GeneralService;
import com.lawencon.elearning.service.ModuleService;
import com.lawencon.elearning.service.StudentCourseService;
import com.lawencon.elearning.service.StudentService;
import com.lawencon.elearning.service.TeacherService;
import com.lawencon.elearning.service.UserService;
import com.lawencon.elearning.util.ValidationUtil;

/**
 * @author : Galih Dika Permana
 */
@Service
public class CourseServiceImpl extends BaseServiceImpl implements CourseService {

  @Autowired
  private CourseDao courseDao;

  @Autowired
  private StudentService studentService;

  @Autowired
  private ModuleService moduleService;

  @Autowired
  private ValidationUtil validateUtil;

  @Autowired
  private ExperienceService experienceService;

  @Autowired
  private UserService userService;

  @Autowired
  private EmailService emailService;

  @Autowired
  private GeneralService generalService;

  @Autowired
  private StudentCourseService studentCourseService;

  @Autowired
  private TeacherService teacherService;

  @Override
  public List<CourseResponseDTO> getAllCourse() throws Exception {
    List<Course> listCourse = courseDao.findAll();
    if (listCourse.isEmpty()) {
      return Collections.emptyList();
    }
    return getAndSetupCourseResponse(listCourse, null);
  }

  @Override
  public void insertCourse(CourseCreateRequestDTO courseDTO) throws Exception {
    validateUtil.validate(courseDTO);
    Course course = new Course();
    course.setCapacity(courseDTO.getCapacity());
    course.setCode(courseDTO.getCode());
    course.setCreatedBy(courseDTO.getCreatedBy());
    course.setDescription(courseDTO.getDescription());

    course.setPeriodStart(courseDTO.getPeriodStart());
    course.setPeriodEnd(courseDTO.getPeriodEnd());

    course.setStatus(CourseStatus.REGISTRATION);

    CourseType courseType = new CourseType();
    courseType.setId(courseDTO.getCourseTypeId());
    course.setCourseType(courseType);

    CourseCategory courseCategory = new CourseCategory();
    courseCategory.setId(courseDTO.getCourseCategoryId());
    course.setCategory(courseCategory);

    Teacher teacher = new Teacher();
    teacher.setId(courseDTO.getTeacherId());
    course.setTeacher(teacher);
    courseDao.insertCourse(course, null);
  }

  @Override
  public void updateCourse(CourseUpdateRequestDTO courseDTO) throws Exception {
    validateUtil.validate(courseDTO);
    User user = userService.getById(courseDTO.getUpdateBy());
    if (!user.getRole().getCode().equals(Roles.ADMIN.getCode()) && !user.getIsActive()) {
      throw new IllegalAccessException("only admin can update data !");
    }
    Course course = new Course();
    course.setId(courseDTO.getId());
    course.setCapacity(courseDTO.getCapacity());
    course.setCode(courseDTO.getCode());
    course.setUpdatedBy(courseDTO.getUpdateBy());
    course.setDescription(courseDTO.getDescription());

    course.setPeriodStart(courseDTO.getPeriodStart());
    course.setPeriodEnd(courseDTO.getPeriodEnd());

    course.setStatus(CourseStatus.valueOf(courseDTO.getStatus()));

    CourseType courseType = new CourseType();
    courseType.setId(courseDTO.getCourseTypeId());
    course.setCourseType(courseType);

    CourseCategory courseCategory = new CourseCategory();
    courseCategory.setId(courseDTO.getCourseCategoryId());
    course.setCategory(courseCategory);

    Teacher teacher = new Teacher();
    teacher.setId(courseDTO.getTeacherId());
    course.setTeacher(teacher);
    Course courseDaoModel = courseDao.getCourseById(course.getId());
    if (courseDaoModel == null) {
      throw new DataIsNotExistsException("id", course.getId());
    }
    setupUpdatedValue(course, () -> courseDaoModel);
    courseDao.updateCourse(course, null);
  }

  @Override
  public void deleteCourse(String id) throws Exception {
    validateUtil.validateUUID(id);
    try {
      begin();
      courseDao.deleteCourse(id);
      commit();
    } catch (Exception e) {
      e.printStackTrace();
      rollback();
      throw e;
    }

  }

  @Override
  public void updateIsActive(UpdateIsActiveRequestDTO request) throws Exception {
    validateUtil.validate(request);
    try {
      begin();
      courseDao.updateIsActive(request.getId(), request.getUpdatedBy(), request.getStatus());
      commit();
    } catch (Exception e) {
      e.printStackTrace();
      rollback();
      throw e;
    }

  }

  @Override
  public List<CourseResponseDTO> getCurrentAvailableCourse(String studentId) throws Exception {
    List<Course> listCourseAvailable = courseDao.getCurrentAvailableCourse();
    List<Course> listCourseByStudent = courseDao.getCourseByStudentId(studentId);

    if (listCourseAvailable.isEmpty()) {
      return Collections.emptyList();
    }
    return getAndSetupCourseResponse(listCourseAvailable, (course, response) -> {
      response.setIsRegist(false);
      listCourseByStudent.forEach(c -> {
        Teacher teacher;
        Boolean studentCourse = false;
        try {
          teacher = teacherService.findTeacherById(c.getTeacher().getId());
          studentCourse = studentCourseService.checkVerifiedCourse(studentId, c.getId());
          TeacherForAvailableCourseDTO teacherDTO = new TeacherForAvailableCourseDTO();
          teacherDTO = response.getTeacher();
          teacherDTO.setTitle(teacher.getTitleDegree());
          response.setTeacher(teacherDTO);
        } catch (Exception e) {
          e.printStackTrace();
        }
        if (course.getId().equals(c.getId()) && studentCourse) {
          response.setIsRegist(true);
        }
      });
    });
  }

  @Override
  public List<CourseResponseDTO> getCourseByStudentId(String id) throws Exception {
    validateUtil.validateUUID(id);
    Student student = studentService.getStudentById(id);
    List<Course> listCourse = courseDao.getCourseByStudentId(student.getId());
    List<CourseProgressResponseDTO> progressList = courseDao
        .getCourseProgressByStudentId(student.getId());
    if (listCourse.isEmpty()) {
      return Collections.emptyList();
    }

    List<CourseResponseDTO> responseList = new ArrayList<>();
    listCourse.forEach(val -> {
      CourseResponseDTO courseDto = new CourseResponseDTO();
      courseDto.setId(val.getId());
      courseDto.setCode(val.getCode());
      courseDto.setTypeName(val.getCourseType().getName());
      courseDto.setCapacity(val.getCapacity());
      courseDto.setCourseStatus(val.getStatus());
      courseDto.setCourseDescription(val.getDescription());
      courseDto.setPeriodStart(val.getPeriodStart());
      courseDto.setPeriodEnd(val.getPeriodEnd());

      TeacherForAvailableCourseDTO teacherDTO = new TeacherForAvailableCourseDTO();
      teacherDTO.setId(val.getTeacher().getId());
      teacherDTO.setCode(val.getTeacher().getCode());
      teacherDTO.setFirstName(val.getTeacher().getUser().getFirstName());
      teacherDTO.setLastName(val.getTeacher().getUser().getLastName());
      teacherDTO.setTitle(val.getTeacher().getTitleDegree());
      File teacherPhoto = val.getTeacher().getUser().getUserPhoto();
      if (teacherPhoto == null || teacherPhoto.getId() == null) {
        teacherDTO.setPhotoId("");
      } else {
        teacherDTO.setPhotoId(val.getTeacher().getUser().getUserPhoto().getId());
      }
      courseDto.setTeacher(teacherDTO);
      courseDto.setCategoryName(val.getCategory().getName());

      progressList.stream()
          .filter(p -> p.getCourseId().equals(val.getId()))
          .findFirst()
          .ifPresent(p -> {
            courseDto.setTotalModule(Optional.of(p.getTotalModule())
                .orElse(0));
            if (courseDto.getTotalModule().equals(0)) {
              courseDto.setModuleComplete(0);
              courseDto.setPercentProgress(0.0);
              return;
            }
            Integer moduleComplete = 0;
            try {
              moduleComplete = courseDao.getModuleCompleteByStudentId(p.getCourseId(), id);
            } catch (Exception e) {
              e.printStackTrace();
            }
            courseDto.setModuleComplete(moduleComplete);
            courseDto.setPercentProgress(Math.floor(
                ((double) courseDto.getModuleComplete() / (double) courseDto.getTotalModule())
                    * 100));
          });
      responseList.add(courseDto);
    });
    return responseList;
  }

  @Override
  public List<CourseAdminResponseDTO> getCourseForAdmin() throws Exception {
    List<Course> listCourse = courseDao.getCourseForAdmin();
    List<CourseAdminResponseDTO> listResult = new ArrayList<>();
    if (listCourse.isEmpty()) {
      return Collections.emptyList();
    }
    listCourse.forEach(val -> {
      CourseAdminResponseDTO data = new CourseAdminResponseDTO();
      data.setId(val.getId());
      data.setCode(val.getCode());
      data.setCategoryName(val.getCategory().getName());
      data.setTypeName(val.getCourseType().getName());
      data.setCapacity(val.getCapacity());
      data.setPeriodStart(val.getPeriodStart());
      data.setStatus(val.getStatus().toString());
      data.setPeriodEnd(val.getPeriodEnd());
      data.setDescription(val.getDescription());
      data.setTypeId(val.getCourseType().getId());
      data.setCategoryId(val.getCategory().getId());
      data.setTeacherId(val.getTeacher().getId());
      data.setActive(val.getIsActive());
      data.setFirstName(val.getTeacher().getUser().getFirstName());
      data.setLastName(val.getTeacher().getUser().getLastName());

      listResult.add(data);
    });
    return listResult;
  }


  @Override
  public void registerCourse(String studentId, String courseId) throws Exception {
    validateUtil.validateUUID(studentId, courseId);
    Student student = studentService.getStudentProfile(studentId);
    Course course = courseDao.getCourseById(courseId);
    if (course == null) {
      throw new DataIsNotExistsException("course id", courseId);
    }
    if (LocalDate.now().isAfter(course.getPeriodStart())) {
      throw new IllegalRequestException("can't register to course when course already on going");
    }
    Integer count = courseDao.checkDataRegisterCourse(courseId, student.getId());
    if (count == 0) {
      Integer registeredCapacity = courseDao.getCapacityCourse(courseId);
      if (registeredCapacity < course.getCapacity()) {
        try {
          StudentCourseRegisterRequestDTO registerDTO = new StudentCourseRegisterRequestDTO();
          registerDTO.setCourseId(courseId);
          registerDTO.setStudentId(studentId);
          registerDTO.setCreatedBy(student.getUser().getId());
          studentCourseService.registerCourse(registerDTO);

          String template = generalService.getTemplateHTML(GeneralCode.REGISTER_COURSE.getCode());
          EmailSetupDTO email = new EmailSetupDTO();
          email.setReceiver(student.getUser().getEmail());
          email.setSubject("Course Registration");
          email.setHeading("Congratulation!");
          email.setBody(template);
          emailService.send(email);
        } catch (Exception e) {
          e.printStackTrace();
          rollback();
          throw e;
        }
      } else {
        throw new IllegalRequestException("capacity already full");
      }
    } else {
      throw new DataAlreadyExistException(
          "You have been registered, but not verified yet. Please wait a moment");
    }
  }

  @Override
  public DetailCourseResponseDTO getDetailCourse(String courseId, String studentId)
      throws Exception {
    validateUtil.validateUUID(courseId);
    DetailCourseResponseDTO detailDTO = new DetailCourseResponseDTO();
    setData(courseId, detailDTO, studentId);
    return detailDTO;
  }

  @Override
  public DashboardCourseResponseDTO getCourseForDashboard(String courseId) throws Exception {
    validateUtil.validateUUID(courseId);
    DashboardCourseResponseDTO detailDTO = new DashboardCourseResponseDTO();
    Course course = courseDao.getCourseById(courseId);
    if (course == null) {
      throw new DataIsNotExistsException("course id", courseId);
    }
    List<ModuleListReponseDTO> listModule = moduleService.getModuleList(courseId);
    detailDTO.setId(course.getId());
    detailDTO.setCode(course.getCode());
    detailDTO.setName(course.getCourseType().getName());
    detailDTO.setCapacity(course.getCapacity());
    detailDTO.setTotalStudent(courseDao.getTotalStudentByIdCourse(courseId));
    detailDTO.setDescription(course.getDescription());
    detailDTO.setPeriodStart(course.getPeriodStart());
    detailDTO.setPeriodEnd(course.getPeriodEnd());
    detailDTO.setModules(listModule);
    return detailDTO;
  }

  @Override
  public List<StudentListByCourseResponseDTO> getStudentByCourseId(String id) throws Exception {
    validateUtil.validateUUID(id);
    Course course = courseDao.getCourseById(id);
    if (course == null) {
      throw new DataIsNotExistsException("id", id);
    }

    return studentCourseService.getListStudentCourseById(id);
  }

  @Override
  public SortedMap<Course, Integer[]> getTeacherCourse(String id) throws Exception {
    validateUtil.validateUUID(id);
    return courseDao.getTeacherCourse(id);
  }

  @Override
  public Course getCourseById(String id) throws Exception {
    validateUtil.validateUUID(id);
    Course course = courseDao.getCourseById(id);
    if (course == null) {
      throw new DataIsNotExistsException("course id" + id);
    }
    return course;
  }

  private void setData(String courseId, DetailCourseResponseDTO detailDTO, String studentId)
      throws Exception {
    Course course = courseDao.getCourseById(courseId);
    if (course == null) {
      throw new DataIsNotExistsException("course id" + courseId);
    }

    List<ModuleResponseDTO> listModule = moduleService.getModuleListByIdCourse(courseId, studentId);
    detailDTO.setId(course.getId());
    detailDTO.setCode(course.getCode());
    detailDTO.setName(course.getCourseType().getName());
    detailDTO.setCapacity(course.getCapacity());
    detailDTO.setTotalStudent(courseDao.getTotalStudentByIdCourse(courseId));
    detailDTO.setDescription(course.getDescription());
    detailDTO.setPeriodStart(course.getPeriodStart());
    detailDTO.setPeriodEnd(course.getPeriodEnd());
    detailDTO.setTeacherFirstName(course.getTeacher().getUser().getFirstName());
    detailDTO.setTeacherLastName(course.getTeacher().getUser().getLastName());
    detailDTO.setIdPhoto(course.getTeacher().getUser().getUserPhoto().getId());
    detailDTO.setModules(listModule);

    if (studentId != null) {
      validateUtil.validateUUID(studentId);
      studentService.getStudentById(studentId);
      setDataWithStudentId(courseId, detailDTO, studentId);
    }
  }

  private void setDataWithStudentId(String courseId, DetailCourseResponseDTO detailDTO,
      String studentId) throws Exception {
    List<ModuleResponseDTO> listModule = moduleService.getModuleListByIdCourse(courseId, studentId);
    detailDTO.setModules(listModule);
  }

  private List<CourseResponseDTO> getAndSetupCourseResponse(List<Course> courseList,
      BiConsumer<Course, CourseResponseDTO> courseConsumer) throws Exception {
    List<CourseResponseDTO> responseList = new ArrayList<>();
    for (Course course : courseList) {
      CourseResponseDTO courseResponse = new CourseResponseDTO();
      if (courseConsumer != null) {
        courseConsumer.accept(course, courseResponse);
      }
      courseResponse.setId(course.getId());
      courseResponse.setCode(course.getCode());
      courseResponse.setTypeName(course.getCourseType().getName());
      courseResponse.setCapacity(course.getCapacity());
      courseResponse.setCourseStatus(course.getStatus());
      courseResponse.setCourseDescription(course.getDescription());
      courseResponse.setPeriodStart(course.getPeriodStart());
      courseResponse.setPeriodEnd(course.getPeriodEnd());

      Teacher teacher = course.getTeacher();
      TeacherForAvailableCourseDTO teacherResponse = new TeacherForAvailableCourseDTO();
      teacherResponse.setId(teacher.getId());
      teacherResponse.setCode(teacher.getCode());
      teacherResponse.setFirstName(teacher.getUser().getFirstName());
      teacherResponse.setLastName(teacher.getUser().getLastName());
      teacherResponse.setTitle(teacher.getTitleDegree());

      List<ExperienceResponseDto> experience = experienceService.getAllByTeacherId(teacher.getId());
      if (experience != null) {
        teacherResponse.setExperience(experience.get(experience.size() - 1).getTitle());
      }

      File teacherPhoto = teacher.getUser().getUserPhoto();
      teacherResponse.setPhotoId(teacherPhoto.getId());

      courseResponse.setTeacher(teacherResponse);
      courseResponse.setCategoryCode(course.getCategory().getCode());
      courseResponse.setCategoryName(course.getCategory().getName());
      responseList.add(courseResponse);
    }
    return responseList;
  }

  @Override
  public DashboardCourseResponseDto dashboardCourseByAdmin() throws Exception {
    DashboardCourseResponseDto dashboardCourse = courseDao.dashboardCourseByAdmin();
    dashboardCourse.setRegisteredStudent(courseDao.getRegisterStudent());
    return dashboardCourse;
  }

  @Override
  public List<CourseAttendanceReportByTeacher> getCourseAttendanceReport(String courseId)
      throws Exception {
    validateUtil.validateUUID(courseId);
    Course course = courseDao.getCourseById(courseId);
    if (course == null) {
      throw new DataIsNotExistsException("course id", courseId);
    }
    List<CourseAttendanceReportByTeacher> listData = courseDao.getCourseAttendanceReport(courseId);
    if (listData.isEmpty()) {
      CourseAttendanceReportByTeacher attendanceReport = new CourseAttendanceReportByTeacher();
      attendanceReport.setModuleName(null);
      attendanceReport.setDate(null);
      listData.add(attendanceReport);
    }
    listData.forEach(val -> {
      try {
        Integer studentPresent = courseDao.getStudentPresentOnModule(val.getModuleId());
        val.setPresent(studentPresent);
        Integer totalStudent = courseDao.getTotalStudentByIdCourse(courseId);
        val.setTotalStudent(totalStudent);
        if (totalStudent == 0) {
          val.setAbsent(0);
        } else {
          val.setAbsent(totalStudent - val.getPresent());
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
    return listData;
  }

  @Override
  public Integer getRegisterStudent() throws Exception {
    return courseDao.getRegisterStudent();
  }

  @Override
  public List<CourseProgressResponseDTO> getCourseProgressByStudentId(String studentId)
      throws Exception {
    validateUtil.validateUUID(studentId);
    studentService.getStudentById(studentId);
    List<CourseProgressResponseDTO> listCourse = courseDao.getCourseProgressByStudentId(studentId);
    if (listCourse.isEmpty()) {
      return Collections.emptyList();
    }
    listCourse.forEach(val -> {
      if (val.getTotalModule() == null || val.getTotalModule() == 0) {
        val.setModuleComplete(0);
        val.setPercentProgress(0.0);
        return;
      }
      Integer moduleComplete = 0;
      try {
        moduleComplete = courseDao.getModuleCompleteByStudentId(val.getCourseId(), studentId);
      } catch (Exception e) {
        e.printStackTrace();
      }
      val.setModuleComplete(moduleComplete);
      val.setPercentProgress(
          Math.floor(((double) val.getModuleComplete() / (double) val.getTotalModule()) * 100));
    });
    return listCourse;
  }

  @Override
  public void updateCoursesStatus(List<UpdateStatusRequestDTO> data) throws Exception {
    begin();
    for (UpdateStatusRequestDTO request : data) {
      validateUtil.validate(request);
      courseDao.updateCoursesStatus(request);
    }
    commit();

  }

}
