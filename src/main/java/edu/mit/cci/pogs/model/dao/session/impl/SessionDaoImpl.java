package edu.mit.cci.pogs.model.dao.session.impl;
 
import edu.mit.cci.pogs.model.dao.api.AbstractDao;
import edu.mit.cci.pogs.model.dao.session.SessionDao;
import edu.mit.cci.pogs.model.dao.session.SessionScheduleType;
import edu.mit.cci.pogs.model.dao.session.SessionStatus;
import edu.mit.cci.pogs.model.jooq.tables.pojos.Session;
import edu.mit.cci.pogs.model.jooq.tables.records.SessionRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SelectQuery;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
 
import static edu.mit.cci.pogs.model.jooq.Tables.SESSION;
 
@Repository
public class SessionDaoImpl extends AbstractDao<Session, Long, SessionRecord> implements SessionDao {
 
    private final DSLContext dslContext;
 
    @Autowired
    public SessionDaoImpl(DSLContext dslContext) {
        super(dslContext, SESSION, SESSION.ID, Session.class);
        this.dslContext = dslContext;
    }
 
    public List<Session> list(){
 
        final SelectQuery<Record> query = dslContext.select()
                .from(SESSION).getQuery();
 
        return query.fetchInto(Session.class);
    }
    public List<Session> listByStudyId(Long studyId){
        final SelectQuery<Record> query = dslContext.select()
                .from(SESSION).getQuery();

        query.addConditions(SESSION.STUDY_ID.eq(studyId));
        return query.fetchInto(Session.class);
    }

    public List<Session> listPerpetualCurrentlyAccpeting(long initWindow) {

        final SelectQuery<Record> query = dslContext.select()
                .from(SESSION).getQuery();
        Timestamp timestamp = new Timestamp(new Date().getTime() + initWindow);
        query.addConditions(SESSION.PERPETUAL_END_DATE.greaterThan(timestamp));
        query.addConditions(SESSION.PERPETUAL_START_DATE.lessThan(new Timestamp(new Date().getTime())));
        query.addConditions(SESSION.STATUS.eq(SessionStatus.NOTSTARTED.getId().toString()));
        query.addConditions(SESSION
                .SESSION_SCHEDULE_TYPE.eq(SessionScheduleType.PERPETUAL.getId().toString()).or(
                SESSION.SESSION_SCHEDULE_TYPE.eq(SessionScheduleType.PERPETUAL_LANDING_PAGE.getId().toString())
        ));
        query.addOrderBy(SESSION.PERPETUAL_START_DATE);
        return query.fetchInto(Session.class);
    }

    public List<Session> listStartsIn(long initWindow) {

        final SelectQuery<Record> query = dslContext.select()
                .from(SESSION).getQuery();
        Timestamp timestamp = new Timestamp(new Date().getTime() + initWindow);
        query.addConditions(SESSION.SESSION_START_DATE.lessThan(timestamp));
        query.addConditions(SESSION.SESSION_START_DATE.greaterThan(new Timestamp(new Date().getTime())));
        query.addConditions(SESSION.STATUS.eq(SessionStatus.NOTSTARTED.getId().toString()));
        query.addConditions(SESSION.SESSION_SCHEDULE_TYPE.eq(SessionScheduleType.SCHEDULED_DATE.getId().toString()));
        query.addOrderBy(SESSION.SESSION_START_DATE);
        return query.fetchInto(Session.class);
    }

    public Session getSessionByFullName(String fullName) {

        final SelectQuery<Record> query = dslContext.select()
                .from(SESSION).getQuery();

        query.addConditions(SESSION.FULL_SESSION_NAME.eq(fullName));
        query.addOrderBy(SESSION.PERPETUAL_START_DATE);
        List<Session> ret = query.fetchInto(Session.class);
        if(ret!=null && !ret.isEmpty()){
            return ret.get(0);
        }
        return null;
    }

    @Override
    public List<Session> listByParentSessionId(Long id) {
        final SelectQuery<Record> query = dslContext.select()
                .from(SESSION).getQuery();
        query.addConditions(SESSION.PARENT_SESSION_ID.eq(id));
        query.addOrderBy(SESSION.SESSION_START_DATE);
        return query.fetchInto(Session.class);
    }

}
 
