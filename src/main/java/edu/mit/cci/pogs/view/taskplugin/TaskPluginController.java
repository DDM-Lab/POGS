package edu.mit.cci.pogs.view.taskplugin;

import edu.mit.cci.pogs.config.AuthUserDetailsService;
import edu.mit.cci.pogs.model.dao.researchgroup.ResearchGroupDao;
import edu.mit.cci.pogs.model.jooq.tables.pojos.*;
import edu.mit.cci.pogs.service.TaskConfigurationService;
import edu.mit.cci.pogs.view.researchgroup.beans.ResearchGroupRelationshipBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

import edu.mit.cci.pogs.model.dao.dictionary.DictionaryDao;
import edu.mit.cci.pogs.model.dao.executablescript.ExecutableScriptDao;
import edu.mit.cci.pogs.model.dao.executablescript.ScriptType;
import edu.mit.cci.pogs.model.dao.taskconfiguration.TaskConfigurationDao;
import edu.mit.cci.pogs.model.dao.taskexecutionattribute.TaskExecutionAttributeDao;
import edu.mit.cci.pogs.model.dao.taskplugin.TaskPlugin;
import edu.mit.cci.pogs.service.TaskExecutionAttributeService;
import edu.mit.cci.pogs.utils.MessageUtils;
import edu.mit.cci.pogs.view.taskplugin.beans.TaskPluginConfigBean;

@Controller
@RequestMapping(value = "/admin/taskplugins")
public class TaskPluginController {

    @Autowired
    private TaskConfigurationDao taskConfigurationDao;

    @Autowired
    private TaskExecutionAttributeService taskExecutionAttributeService;

    @Autowired
    private TaskExecutionAttributeDao taskExecutionAttributeDao;

    @Autowired
    private TaskConfigurationService taskConfigurationService;

    @Autowired
    private ExecutableScriptDao executableScriptDao;

    @Autowired
    private DictionaryDao dictionaryDao;

    @Autowired
    private ResearchGroupDao researchGroupDao;

    @GetMapping
    public String getTaskPlugins(Model model) {

        model.addAttribute("taskPluginsList", TaskPlugin.getAllTaskPlugins());
        return "taskplugin/taskplugin-list";
    }

    @ModelAttribute("executableScripts")
    public List<ExecutableScript> getExecutableScripts(){
        List<ExecutableScript> taskConfigurationList = new ArrayList<>();
        for(ScriptType st: getScriptTypes()){
            taskConfigurationList.addAll(executableScriptDao.listByScriptType(st));
        }
        return taskConfigurationList;
    }

    @ModelAttribute("dictionaries")
    public List<Dictionary> getDictionaries(){
        return dictionaryDao.list();
    }

    @ModelAttribute("executableScriptTypes")
    private List<ScriptType> getScriptTypes() {
        return ScriptType.getAllScriptTypes();
    }


    @GetMapping("{taskPluginName}")
    public String getTaskPlugin(@PathVariable("taskPluginName") String taskPluginName, Model model) {


        TaskPlugin taskPlugin = getTaskPlugin(taskPluginName);

        if (taskPlugin != null) {
            model.addAttribute("taskPlugin", taskPlugin);
        }
        //get task plugins configurations
        List<TaskConfiguration> taskConfigurationList = taskConfigurationDao.
                listTaskConfigurationsByNameWithUserGroup(taskPlugin.getTaskPluginName(), AuthUserDetailsService.getLoggedInUser());
        model.addAttribute("taskConfigurationList", taskConfigurationList);

        return "taskplugin/taskplugin-display";
    }

    private TaskPlugin getTaskPlugin(@PathVariable("taskPluginName") String taskPluginName) {
        List<TaskPlugin> allAvailableTP = TaskPlugin.getAllTaskPlugins();
        TaskPlugin taskPlugin = null;
        for (TaskPlugin tp : allAvailableTP) {
            if (tp.getTaskPluginName().equals(taskPluginName)) {
                taskPlugin = tp;
                break;
            }
        }
        return taskPlugin;
    }



    @PostMapping("{taskPluginName}")
    public String saveConfiguration(@PathVariable("taskPluginName") String taskPluginName,
                                    @ModelAttribute TaskPluginConfigBean taskPluginConfigBean,
                                    RedirectAttributes redirectAttributes) {


        TaskConfiguration taskConfiguration = taskConfigurationService.createOrUpdate(taskPluginConfigBean);

        if (taskPluginConfigBean.getId() == null) {

            taskPluginConfigBean.setId(taskConfiguration.getId());

            if(taskPluginConfigBean.getAttributes()!=null) {
                for (TaskExecutionAttribute tea : taskPluginConfigBean.getAttributes()) {
                    tea.setId(null);
                }
            }
            MessageUtils.addSuccessMessage("Plugin configuration created successfully!", redirectAttributes);
        } else {

            MessageUtils.addSuccessMessage("Plugin configuration updated successfully!", redirectAttributes);
        }
        taskExecutionAttributeService.createOrUpdateTaskExecutionAttribute(taskPluginConfigBean);


        return "redirect:/admin/taskplugins/" + taskPluginName + "/" + taskPluginConfigBean.getId();
    }

    @GetMapping("{taskPluginName}/{configurationId}")
    public String getTaskConfig(@PathVariable("taskPluginName") String taskPluginName,
                                @PathVariable("configurationId") Long configurationId, Model model) {

        TaskPlugin taskPlugin = getTaskPlugin(taskPluginName);
        TaskConfiguration taskConfiguration = taskConfigurationDao.get(configurationId);
        List<TaskExecutionAttribute> taskExecutionAttributes = taskExecutionAttributeDao.listByTaskConfigurationId(configurationId);

        TaskPluginConfigBean tpcb = new TaskPluginConfigBean(taskConfiguration);
        tpcb.setAttributes(taskExecutionAttributes);

        List<TaskConfigurationHasResearchGroup> test = taskConfigurationService.listTaskConfigurationyHasResearchGroupByTaskConfigurationId(configurationId);
        tpcb.setResearchGroupRelationshipBean(new ResearchGroupRelationshipBean());
        tpcb.getResearchGroupRelationshipBean().setTaskConfigurationHasResearchSelectedValues(taskConfigurationService.listTaskConfigurationyHasResearchGroupByTaskConfigurationId(configurationId));

        setupModelAttributesForPlugin(model, taskPlugin, tpcb);


        return "taskplugin/taskpluginconfig-edit";

    }

    @GetMapping("{taskPluginName}/createConfiguration")
    public String getNewPluginConfig(@PathVariable("taskPluginName") String taskPluginName,
                                     Model model) {

        TaskPlugin taskPlugin = getTaskPlugin(taskPluginName);

        TaskPluginConfigBean tpcb = new TaskPluginConfigBean();
        tpcb.setTaskPluginName(taskPluginName);

        setupModelAttributesForPlugin(model, taskPlugin, tpcb);

        return "taskplugin/taskpluginconfig-edit";

    }

    private void setupModelAttributesForPlugin(Model model, TaskPlugin taskPlugin, TaskPluginConfigBean tpcb) {
        if (taskPlugin != null) {
            model.addAttribute("taskPlugin", taskPlugin);
            model.addAttribute("taskPluginConfigBean", tpcb);
        }

        model.addAttribute("taskCss", taskPlugin.getTaskCSSContent());
        if (taskPlugin.hasLibsDir()) {
            model.addAttribute("taskLibJs", taskPlugin.getLibsDirContent());
        }
        model.addAttribute("taskEditJs", taskPlugin.getTaskEditJsContent());
        model.addAttribute("taskEditHtml", taskPlugin.getTaskEditHtmlContent());
    }

    @ModelAttribute("researchGroups")
    public List<ResearchGroup> getAllResearchGroups() {

        List<ResearchGroup> res = researchGroupDao.list();
        return res;
    }
}
