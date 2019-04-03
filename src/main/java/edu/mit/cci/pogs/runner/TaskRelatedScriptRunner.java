package edu.mit.cci.pogs.runner;

import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;

import javax.script.ScriptContext;

import edu.mit.cci.pogs.model.dao.executablescript.ExecutableScriptDao;
import edu.mit.cci.pogs.model.dao.subject.SubjectDao;
import edu.mit.cci.pogs.model.dao.taskplugin.TaskPlugin;
import edu.mit.cci.pogs.model.jooq.tables.pojos.Subject;
import edu.mit.cci.pogs.model.jooq.tables.pojos.TaskConfiguration;
import edu.mit.cci.pogs.runner.wrappers.SessionWrapper;
import edu.mit.cci.pogs.runner.wrappers.TaskWrapper;
import edu.mit.cci.pogs.service.CompletedTaskAttributeService;
import edu.mit.cci.pogs.service.SubjectService;
import edu.mit.cci.pogs.service.TaskExecutionAttributeService;
import edu.mit.cci.pogs.service.TaskService;
import edu.mit.cci.pogs.service.TeamService;

public abstract class TaskRelatedScriptRunner extends AbstractJavascriptRunner {

    protected TaskWrapper taskWrapper;

    protected SessionWrapper session;

    protected TaskPlugin taskPlugin;

    protected TaskConfiguration taskConfiguration;

    @Autowired
    protected CompletedTaskAttributeService completedTaskAttributeService;

    @Autowired
    protected TaskExecutionAttributeService taskExecutionAttributeService;

    @Autowired
    protected SubjectDao subjectDao;

    @Autowired
    protected TeamService teamService;

    @Autowired
    protected TaskService taskService;

    @Autowired
    protected SubjectService subjectService;

    @Autowired
    protected ExecutableScriptDao executableScriptDao;

    @Override
    public void setupVariableBindings() {
        //get all subjects + subjects attributes

        boolean isSoloTask = false;
        if(this.getCompletedTask().getSubjectId()!=null){
            isSoloTask = true;
            Subject subject =  subjectDao.get(this.getCompletedTask().getSubjectId());

            this.getEngine().put("subject", teamService.getSubjectJsonObject(subject));
        } else {
            this.getEngine().put("subject", null);
        }

        this.getEngine().put("isSoloTask", isSoloTask);

        JSONArray teamMates = teamService.getTeamatesJSONObject(
                teamService.getTeamSubjectsFromCompletedTask(getCompletedTask().getSubjectId(),
                        getCompletedTask().getTeamId()));

        this.getEngine().put("teammates", teamMates.toString());


        JSONArray taskAttr = taskExecutionAttributeService.
                listExecutionAttributesAsJsonArray(taskWrapper.getId());

        //get all task_execution_attribute
        this.getEngine().put("taskConfigurationAttributes", taskAttr.toString());
        System.out.println("taskConfigurationAttributes : " + taskAttr.toString());

        JSONArray completedTaskAttributes = completedTaskAttributeService
                .listCompletedTaskAttributesForCompletedTask(this.getCompletedTask().getId());

        this.getEngine().put("completedTaskAttributes", completedTaskAttributes.toString());
        System.out.println("completedTaskAttributes : " + completedTaskAttributes.toString());


    }

    protected void retreiveSubjectAttributesToAdd() {
        String subjectAttributesToAddJson = (String) this.getEngine()
                .getBindings(ScriptContext.ENGINE_SCOPE).get("subjectAttributesToAdd");
        subjectService.createOrUpdateSubjectsAttributes(subjectAttributesToAddJson);
    }

    protected void retrieveCompletedTaskAttributesToAdd() {
        String attributesToAddJson = (String) this.getEngine().getBindings(
                ScriptContext.ENGINE_SCOPE).get("completedTaskAttributesToAdd");
        completedTaskAttributeService.createCompletedTaskAttributesFromJsonString(
                attributesToAddJson,this.getCompletedTask().getId());
    }

    public TaskWrapper getTaskWrapper() {
        return taskWrapper;
    }

    public void setTaskWrapper(TaskWrapper taskWrapper) {
        this.taskWrapper = taskWrapper;
    }

    public SessionWrapper getSession() {
        return session;
    }

    public void setSession(SessionWrapper session) {
        this.session = session;
    }

    public TaskPlugin getTaskPlugin() {
        return taskPlugin;
    }

    public void setTaskPlugin(TaskPlugin taskPlugin) {
        this.taskPlugin = taskPlugin;
    }
}