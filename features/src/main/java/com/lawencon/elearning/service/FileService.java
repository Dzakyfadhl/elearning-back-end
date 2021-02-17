package com.lawencon.elearning.service;

import com.lawencon.elearning.dto.file.FileResponseDto;
import com.lawencon.elearning.model.File;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Rian Rivaldo
 */
public interface FileService {

  FileResponseDto createFile(MultipartFile file, String content) throws Exception;

  List<FileResponseDto> createMultipleFile(List<MultipartFile> files, String content)
      throws Exception;

  File getFileById(String id) throws Exception;

  void updateFile(MultipartFile file, String content) throws Exception;

  void deleteFile(String id) throws Exception;

}
