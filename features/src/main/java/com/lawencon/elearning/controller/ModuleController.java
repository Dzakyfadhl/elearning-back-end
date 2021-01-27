package com.lawencon.elearning.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import com.lawencon.elearning.service.ModuleService;
import com.lawencon.elearning.util.WebResponseUtils;

/**
 * @author : Galih Dika Permana
 */
@RestController
public class ModuleController {

  @Autowired
  private ModuleService moduleService;

  @GetMapping("/modules/{id}")
  public ResponseEntity<?> getDetailCourse(@PathVariable("id") String id) throws Exception {
    return WebResponseUtils.createWebResponse(moduleService.getDetailCourse(id), HttpStatus.OK);
  }

  @GetMapping("/module/{id}")
  public ResponseEntity<?> getDetailModule(@PathVariable("id") String id) throws Exception {
    return WebResponseUtils.createWebResponse(moduleService.getDetailCourse(id), HttpStatus.OK);
  }
}