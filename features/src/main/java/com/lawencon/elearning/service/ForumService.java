package com.lawencon.elearning.service;

import java.util.List;
import com.lawencon.elearning.model.Forum;

/**
 *  @author Dzaky Fadhilla Guci
 */

public interface ForumService {

  List<Forum> getAllForums() throws Exception;

  void saveForum(Forum data) throws Exception;

  void updateForum(Forum data) throws Exception;

  Forum findForumById(String id) throws Exception;

  List<Forum> getByModuleId(String moduleId) throws Exception;

}