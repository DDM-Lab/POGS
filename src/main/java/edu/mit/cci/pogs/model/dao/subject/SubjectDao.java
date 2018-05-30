package edu.mit.cci.pogs.model.dao.subject;
 
import java.util.List;
 
import edu.mit.cci.pogs.model.dao.api.Dao;
import edu.mit.cci.pogs.model.jooq.tables.pojos.Subject;
 
public interface SubjectDao extends Dao<Subject, Long> {
 
    List<Subject> list();
    List<Subject> listBySessionId( Long sessionId);
}
 