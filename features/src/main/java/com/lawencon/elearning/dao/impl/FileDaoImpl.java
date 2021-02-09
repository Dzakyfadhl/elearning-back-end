package com.lawencon.elearning.dao.impl;

import java.util.List;
import org.springframework.stereotype.Repository;
import com.lawencon.base.BaseDaoImpl;
import com.lawencon.elearning.dao.FileDao;
import com.lawencon.elearning.model.File;

/**
 * @author Rian Rivaldo
 */
@Repository
public class FileDaoImpl extends BaseDaoImpl<File> implements FileDao {

  @Override
  public void create(File file) throws Exception {
    save(file, null, null, false, false);
  }

  @Override
  public File findById(String id) throws Exception {
    return getById(id);
  }

  @Override
  public void updateFile(File file) throws Exception {
    save(file, null, null, true, true);
  }

  @Override
  public List<File> getAllFile() throws Exception {
    return getAll();
  }

}
