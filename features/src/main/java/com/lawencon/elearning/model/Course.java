package com.lawencon.elearning.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lawencon.model.BaseMaster;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author : Galih Dika Permana
 *
 **/
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "tb_m_courses")
public class Course extends BaseMaster {
  private static final long serialVersionUID = 1L;

  @Column(unique = true, nullable = false, length = 100)
  private String code;

  @Column(nullable = false)
  private String descripton;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_course_type", nullable = false)
  private CourseType courseType;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_teacher", nullable = false)
  private Teacher teacher;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_category", nullable = false)
  private CourseCategory category;

  @Column(nullable = false)
  private Integer capacity;

  @Column(name = "period_start", nullable = false)
  private LocalDateTime periodStart;

  @Column(name = "period_end", nullable = false)
  private LocalDateTime periodEnd;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private CourseStatus status;

  @JsonIgnore
  @ManyToMany(mappedBy = "courses", fetch = FetchType.LAZY)
  private Set<Student> student = new HashSet<>();

}