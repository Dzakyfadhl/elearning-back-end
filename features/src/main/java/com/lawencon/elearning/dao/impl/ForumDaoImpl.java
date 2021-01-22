package com.lawencon.elearning.dao.impl;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Repository;
import com.lawencon.elearning.dao.CustomBaseDao;
import com.lawencon.elearning.dao.ForumDao;
import com.lawencon.elearning.model.Forum;
import com.lawencon.elearning.model.Role;
import com.lawencon.elearning.model.User;
import com.lawencon.util.Callback;

/**
 * @author Dzaky Fadhilla Guci
 */

@Repository
public class ForumDaoImpl extends CustomBaseDao<Forum> implements ForumDao {

  @Override
  public List<Forum> getAllForums() throws Exception {
    return getAll();
  }

  @Override
  public void saveForum(Forum data, Callback before) throws Exception {
    save(data, before, null, true, true);
  }

  @Override
  public Forum findForumById(String id) throws Exception {
    return getById(id);
  }

  @Override
  public List<Forum> getByModuleId(String id) throws Exception {
    String sql = bBuilder(
        "SELECT trf.trx_number ,  trf.\"content\" , trf.created_at,  ",
        "tmu.id AS id_user, tmr.id AS id_role , tmr.code AS role_code FROM tb_r_forums trf ",
        "INNER JOIN tb_m_users tmu ON trf.id_user = tmu.id  ",
        "INNER JOIN tb_m_roles tmr ON tmr.id = tmu.id_role  ",
        "WHERE id_module =?1")
            .toString();

    List<Forum> listResult = new ArrayList<>();

    List<?> listObj = createNativeQuery(sql).setParameter(1, id).getResultList();

    listObj.forEach(val -> {
      Object[] objArr = (Object[]) val;
      Forum forum = new Forum();
      forum.setTrxNumber((String) objArr[0]);
      forum.setContent((String) objArr[1]);
      Timestamp inDate = (Timestamp) objArr[2];
      forum.setCreatedAt((LocalDateTime) inDate.toLocalDateTime());

      User u = new User();
      u.setId((String) objArr[3]);

      Role r = new Role();
      r.setId((String) objArr[4]);
      r.setCode((String) objArr[5]);
      u.setRole(r);

      forum.setUser(u);

      listResult.add(forum);
    });
    return listResult.size() > 0 ? listResult : null;
  }


}
