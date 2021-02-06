package com.lawencon.elearning.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lawencon.base.BaseServiceImpl;
import com.lawencon.elearning.dao.ModuleDao;
import com.lawencon.elearning.dto.DeleteMasterRequestDTO;
import com.lawencon.elearning.dto.file.FileCreateRequestDto;
import com.lawencon.elearning.dto.file.FileResponseDto;
import com.lawencon.elearning.dto.module.ModulRequestDTO;
import com.lawencon.elearning.dto.module.ModuleResponseDTO;
import com.lawencon.elearning.dto.module.UpdateModuleDTO;
import com.lawencon.elearning.dto.schedule.ScheduleResponseDTO;
import com.lawencon.elearning.error.DataIsNotExistsException;
import com.lawencon.elearning.error.IllegalRequestException;
import com.lawencon.elearning.model.Course;
import com.lawencon.elearning.model.FileType;
import com.lawencon.elearning.model.Module;
import com.lawencon.elearning.model.Schedule;
import com.lawencon.elearning.model.SubjectCategory;
import com.lawencon.elearning.model.Teacher;
import com.lawencon.elearning.service.CourseService;
import com.lawencon.elearning.service.FileService;
import com.lawencon.elearning.service.ModuleService;
import com.lawencon.elearning.service.ScheduleService;
import com.lawencon.elearning.service.StudentService;
import com.lawencon.elearning.service.SubjectCategoryService;
import com.lawencon.elearning.service.TeacherService;
import com.lawencon.elearning.util.ValidationUtil;

/**
 * 
 * @author WILLIAM
 *
 */
@Service
public class ModuleServiceImpl extends BaseServiceImpl implements ModuleService {

  @Autowired
  private ModuleDao moduleDao;

  @Autowired
  private ScheduleService scheduleService;

  @Autowired
  private ValidationUtil validationUtil;

  @Autowired
  private FileService fileService;

  @Autowired
  private TeacherService teacherService;

  @Autowired
  private CourseService courseService;

  @Autowired
  private SubjectCategoryService subjectCategoryService;

  @Autowired
  private StudentService studentService;

  @Override
  public Module getModuleById(String id) throws Exception {
    validateNullId(id, "id");
    return Optional.ofNullable(moduleDao.getModuleById(id))
        .orElseThrow(() -> new DataIsNotExistsException("id", id));
  }

  @Override
  public List<ModuleResponseDTO> getModuleListByIdCourse(String idCourse, String idStudent)
      throws Exception {
    Optional.ofNullable(courseService.getCourseById(idCourse))
        .orElseThrow(() -> new DataIsNotExistsException("course id", idCourse));
    if (idStudent == null || idStudent.trim().isEmpty()) {
      List<Module> listResult = moduleDao.getDetailCourse(idCourse);
      if (listResult.isEmpty()) {
        throw new DataIsNotExistsException("There is no module yet");
      }
      List<ModuleResponseDTO> listModuleDTO = new ArrayList<>();
      for (int i = 0; i < listResult.size(); i++) {
        ModuleResponseDTO moduleDTO = new ModuleResponseDTO();
        moduleDTO.setId(listResult.get(i).getId());
        moduleDTO.setCode(listResult.get(i).getCode());
        moduleDTO.setTittle(listResult.get(i).getTitle());
        moduleDTO.setDescription(listResult.get(i).getDescription());
        moduleDTO.setSubjectName(listResult.get(i).getSubject().getSubjectName());

        ScheduleResponseDTO scheduleDTO = new ScheduleResponseDTO();
        scheduleDTO.setId(listResult.get(i).getSchedule().getId());
        scheduleDTO.setDate(listResult.get(i).getSchedule().getDate());
        scheduleDTO.setStartTime(listResult.get(i).getSchedule().getStartTime());
        scheduleDTO.setEndTime(listResult.get(i).getSchedule().getEndTime());
        moduleDTO.setSchedule(scheduleDTO);
        listModuleDTO.add(moduleDTO);
      }
      Collections.sort(listModuleDTO, new Comparator<ModuleResponseDTO>() {
        public int compare(ModuleResponseDTO module1, ModuleResponseDTO module2) {
          return module1.getSchedule().getDate().compareTo(module2.getSchedule().getDate());
        }
      });
      return listModuleDTO;
    }
    else {
      Optional.ofNullable(studentService.getStudentById(idStudent))
          .orElseThrow(() -> new DataIsNotExistsException("student id", idStudent));
      List<ModuleResponseDTO> listResult = moduleDao.getDetailCourseStudent(idCourse, idStudent);
      if (listResult.isEmpty()) {
        throw new DataIsNotExistsException("There is no module yet");
      }
      Collections.sort(listResult, new Comparator<ModuleResponseDTO>() {
        public int compare(ModuleResponseDTO module1, ModuleResponseDTO module2) {
          return module1.getSchedule().getDate().compareTo(module2.getSchedule().getDate());
        }
      });
      return listResult;
    }
  }

  @Override
  public void insertModule(List<ModulRequestDTO> data) throws Exception {
    for (ModulRequestDTO element : data) {
      validationUtil.validate(element);
      Schedule schedule = new Schedule();
      schedule.setCreatedBy(element.getScheduleRequestDTO().getScheduleCreatedBy());
      schedule.setDate(element.getScheduleRequestDTO().getScheduleDate());
      schedule.setEndTime(element.getScheduleRequestDTO().getScheduleEnd());
      schedule.setStartTime(element.getScheduleRequestDTO().getScheduleStart());

      Teacher teacher = new Teacher();
      teacher.setId(element.getScheduleRequestDTO().getTeacherId());
      teacher.setVersion(element.getScheduleRequestDTO().getTeacherVersion());
      schedule.setTeacher(teacher);

      Module module = new Module();
      module.setSchedule(schedule);
      module.setCode(element.getModuleCode());
      module.setCreatedAt(LocalDateTime.now());
      module.setCreatedBy(element.getModuleCreatedBy());
      module.setDescription(element.getModuleDescription());
      module.setTitle(element.getModuleTittle());

      Course course = new Course();
      course.setId(element.getCourseId());
      course.setVersion(element.getCourseVersion());
      module.setCourse(course);

      SubjectCategory subject = new SubjectCategory();
      subject.setId(element.getSubjectId());
      subject.setVersion(element.getSubjectVersion());
      module.setSubject(subject);

      try {
        begin();
        scheduleService.saveSchedule(schedule);
        moduleDao.insertModule(module, null);
        commit();
      } catch (Exception e) {
        e.printStackTrace();
        rollback();
        throw e;
      }
    }
  }

  @Override
  public void updateModule(UpdateModuleDTO data) throws Exception {
    validationUtil.validate(data);
    Optional.ofNullable(moduleDao.getModuleById(data.getId()))
        .orElseThrow(() -> new DataIsNotExistsException("id module", data.getId()));
    Optional.ofNullable(teacherService.findTeacherById(data.getScheduleRequestDTO().getTeacherId()))
        .orElseThrow(() -> new DataIsNotExistsException("id teacher",
            data.getScheduleRequestDTO().getTeacherId()));
    Schedule scheduleDb =
        Optional.ofNullable(scheduleService.findScheduleById(data.getIdSchedule()))
            .orElseThrow(() -> new DataIsNotExistsException("id schedule", data.getIdSchedule()));
    Optional.ofNullable(subjectCategoryService.getById(data.getSubjectId()))
        .orElseThrow(() -> new DataIsNotExistsException("id subject", data.getSubjectId()));
    Optional.ofNullable(courseService.getCourseById(data.getCourseId()))
        .orElseThrow(() -> new DataIsNotExistsException("id course", data.getCourseId()));
    Schedule schedule = new Schedule();
    schedule.setId(data.getIdSchedule());
    schedule.setCode(scheduleDb.getCode());
    schedule.setCreatedBy(data.getScheduleRequestDTO().getScheduleCreatedBy());
    schedule.setDate(data.getScheduleRequestDTO().getScheduleDate());
    schedule.setEndTime(data.getScheduleRequestDTO().getScheduleEnd());
    schedule.setStartTime(data.getScheduleRequestDTO().getScheduleStart());

    Teacher teacher = new Teacher();
    teacher.setId(data.getScheduleRequestDTO().getTeacherId());
    teacher.setVersion(data.getScheduleRequestDTO().getTeacherVersion());
    schedule.setTeacher(teacher);

    Module module = new Module();
    module.setSchedule(schedule);
    module.setCode(data.getModuleCode());
    module.setId(data.getId());
    module.setUpdatedBy(data.getUpdatedBy());

    Course course = new Course();
    course.setId(data.getCourseId());
    course.setVersion(data.getCourseVersion());
    module.setCourse(course);

    SubjectCategory subject = new SubjectCategory();
    subject.setId(data.getSubjectId());
    subject.setVersion(data.getSubjectVersion());
    module.setSubject(subject);

    scheduleService.updateSchedule(schedule);
    moduleDao.updateModule(module, null);
  }

  @Override
  public void deleteModule(DeleteMasterRequestDTO data) throws Exception {
    validateNullId(data.getId(), "id");
    validationUtil.validate(data);
    try {
      begin();
      moduleDao.deleteModule(data.getId());
      commit();
    } catch (Exception e) {
      e.printStackTrace();
      if (e.getMessage().equals("ID Not Found")) {
        throw new DataIsNotExistsException(e.getMessage());
      }
      updateIsActive(data.getId(), data.getUpdatedBy());
    }
  }

  @Override
  public void updateIsActive(String id, String userId) throws Exception {
    begin();
    moduleDao.updateIsActive(id, userId);
    commit();
  }

  @Override
  public Module getModuleByIdCustom(String id) throws Exception {
    validateNullId(id, "id");
    return Optional.ofNullable(moduleDao.getModuleByIdCustom(id))
        .orElseThrow(() -> new DataIsNotExistsException("id", id));
  }

  @Override
  public void saveLesson(List<MultipartFile> multiPartFiles, String idUser, String idModule)
      throws Exception {
    validationUtil.validateUUID(idUser);
    validationUtil.validateUUID(idModule);

    if (multiPartFiles == null) {
      throw new IllegalRequestException("File cannot be empty!");
    }

    FileCreateRequestDto contentString = new FileCreateRequestDto();
    contentString.setType(FileType.MODULE_LESSON.toString());
    contentString.setUserId(idUser);
    String content = new ObjectMapper().writeValueAsString(contentString);

    for (MultipartFile file : multiPartFiles) {
      try {
        begin();
        FileResponseDto fileResponseDTO = fileService.createFile(file, content);
        moduleDao.insertLesson(idModule, fileResponseDTO.getId());
        commit();
      } catch (Exception e) {
        e.printStackTrace();
        rollback();
        throw e;
      }
    }
  }

  @Override
  public List<FileResponseDto> getLessonFile(String idModule) throws Exception {
    validateNullId(idModule, "module id");
    Optional.ofNullable(moduleDao.getModuleById(idModule))
        .orElseThrow(() -> new DataIsNotExistsException("id module", idModule));
    System.out.println(idModule);
    List<FileResponseDto> listFileDto = moduleDao.getLessonByIdModule(idModule);
    if (listFileDto.isEmpty()) {
      throw new DataIsNotExistsException("No Lesson File Yet");
    }
    return listFileDto;
  }

  private void validateNullId(String id, String msg) throws Exception {
    if (id == null || id.trim().isEmpty()) {
      throw new IllegalRequestException(msg, id);
    }
  }

}
