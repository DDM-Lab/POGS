

class Survey {
    constructor(pogsPlugin) {
        this.pogsPlugin = pogsPlugin;
        this.replacements  = [];
        this.replacements.push(this.pogsPlugin.getTeammatesDisplayNames());
        this.replacements.push(this.pogsPlugin.getOtherTeammates());
        this.replacements.push(this.pogsPlugin.getLastTask());
        this.replacements.push(this.pogsPlugin.getTaskList());
        this.replacements.push(this.pogsPlugin.getOtherTasks());
        this.replacements.push(this.pogsPlugin.getSessionName());

        this.fields = [];
        this.globalFieldIndex = 0;
    }
    resolveVariablesForNetworkQuestions(surveyItem){
        var regex = new RegExp(/\${.*}/gi);
        var allVariables = ['\\${allTeammates}','\\${otherTeamates}', '\\${lastTaskName}',
                            '\\${allTasksNames}','\\${otherTasksNames}', '\\${sessionName}'];

        var replacements = this.replacements;

        // [['m01', 'm02', 'm03'],
        //     ['m02', 'm03'],
        //     "Last task name",
        //     ["tast 1", "task 2","task 3"],
        //     ["task 2","task3"],
        //     "session one"]

        if(surveyItem.question.match(regex)) {

            for(var i=0; i < allVariables.length; i ++) {

                var replacer = "";
                if(surveyItem.question.match(new RegExp(allVariables[i] ,'gi'))) {
                    if(replacements[i].constructor === Array) {

                       for(var j =0 ; j < replacements[i].length; j ++) {
                           replacer += replacements[i][j];
                           if(j + 1 != replacements[i].length){
                               replacer += ", ";
                           }
                       }

                    } else {
                        replacer = replacements[i];
                    }
                    surveyItem.question =
                        surveyItem.question.replace( new RegExp( allVariables[i] ,'gi'), replacer);
                }
            }
        }
        if(!surveyItem.value){
            return surveyItem;
        }

        if (surveyItem.value.constructor === Array) {
            console.log("value is array");
            if (surveyItem.value !== undefined && surveyItem.value.length > 0) {
                for (var i = 0; i < surveyItem.value.length; i++) {

                    if (surveyItem.value[i].match(new RegExp(regex, 'gi'))) {

                        for (var j = 0; j < allVariables.length; j++) {
                            if (surveyItem.value[i].match(new RegExp(allVariables[j], 'gi'))) {

                                if (replacements[i].constructor === Array) {
                                    surveyItem.value = [];

                                    for (var k = 0; k < replacements[j].length; k++) {
                                        surveyItem.value.push(replacements[j][k]);
                                    }
                                    return surveyItem;
                                } else {
                                    surveyItem.value[i] =
                                        surveyItem.value.replace(new RegExp(allVariables[j], 'gi'),
                                                                 replacements[j]);
                                }
                            }
                        }
                    }

                }
            }
        } else {

            if (surveyItem.value.constructor === Object) {
                console.log("value is object");
                for (let i = 0; i < surveyItem.value.columns.length; i++) {

                    if (surveyItem.value.columns[i].match(new RegExp(regex, 'gi'))) {

                        for (let j = 0; j < allVariables.length; j++) {
                            if (surveyItem.value.columns[i].match(new RegExp(allVariables[j], 'gi'))) {

                                if (replacements[i].constructor === Array) {
                                    surveyItem.value.columns = [];

                                    for (let k = 0; k < replacements[j].length; k++) {
                                        surveyItem.value.columns.push(replacements[j][k]);
                                    }
                                    return surveyItem;
                                } else {
                                    surveyItem.value.columns[i] =
                                        surveyItem.value.columns.replace(new RegExp(allVariables[j], 'gi'),
                                                                 replacements[j]);
                                }
                            }
                        }
                    }
                }
                for (let i = 0; i < surveyItem.value.rows.length; i++) {

                    if (surveyItem.value.rows[i].match(new RegExp(regex, 'gi'))) {

                        for (let j = 0; j < allVariables.length; j++) {
                            if (surveyItem.value.rows[i].match(new RegExp(allVariables[j], 'gi'))) {

                                if (replacements[i].constructor === Array) {
                                    surveyItem.value.rows = [];

                                    for (let k = 0; k < replacements[j].length; k++) {
                                        surveyItem.value.rows.push(replacements[j][k]);
                                    }
                                    return surveyItem;
                                } else {
                                    surveyItem.value.rows[i] =
                                        surveyItem.value.rows.replace(new RegExp(allVariables[j], 'gi'),
                                                                         replacements[j]);
                                }
                            }
                        }
                    }
                }
            }
        }
        return surveyItem;
    }
    getPogsPlugin(){
        return this.pogsPlugin;
    }
    registerListenerAndGetFieldId(fieldImpl){

        let field = this.globalFieldIndex;
        //console.log("Field: --------" + this.globalFieldIndex);
        this.fields[field] = fieldImpl;
        this.globalFieldIndex = this.globalFieldIndex + 1;
        return field;
    }
    setupSurvey(surveyBluePrint){
    console.info("starting survey setup...");
        /*
            JSON format
            [
              {"question":"Survey question1?",
                "type": "text",
                "placeholder":"question1 placeholder",
                "default": "whatever"
              },
              {"question":"Survey question2?",
                "type": "radio/check",
                "placeholder":"question2 placeholder"
                "options": [...],
                "default": "whatever"
              },
              {"question":"Survey question3?",
                "type": "select",
                "placeholder":"question3 placeholder",
                "options": [...],
                "default": "whatever"
              }
            ]

        */
        var surveyValues = $.parseJSON(surveyBluePrint);
        var self = this;

        var str = '';
        $.each(surveyValues,function(i,e){
            console.log(e);
            e = this.resolveVariablesForNetworkQuestions(e);

            if(e.type == "text"){ // setup text question
                this.fields.push(new InputField(this,e));
            }
            else if(e.type == "radio"){ // setup radio question
                this.fields.push(new RadioField(this,e));
            }
            else if(e.type == "select") {
                this.fields.push(new SelectField(this,e));
            }
            else if(e.type == "checkbox") {
                this.fields.push(new CheckboxField(this,e));
            }
            if(e.type == "introduction") {
                this.fields.push(new InformationField(this,e));
            }else if(e.type == "radiotable"){ // setup radio question
                this.fields.push(new RadioTableField(this,e));
            }

            //console.log(i + '----'+ JSON.stringify(e));
        }.bind(this));

    }

    broadcastReceived(message){
        let attrName = message.content.attributeName;
        let index = attrName
            .replace(SURVEY_CONST.FIELD_NAME, "")
            .replace(SURVEY_TRANSIENT.CLICK_RADIO_NOT_LOG,"")
            .replace(SURVEY_TRANSIENT.FOCUS_IN_CELL,"")
            .replace(SURVEY_TRANSIENT.CLICK_CHECKBOX_NOT_LOG,"")
            .replace(SURVEY_TRANSIENT.MOUSE_OVER_FIELD,"")
            .replace(SURVEY_TRANSIENT.MOUSE_OUT_OF_FIELD,"");

        if(this.fields.length > index) {
            if(message.sender != this.pogsPlugin.subjectId) {
                this.fields[index].broadcastReceived(message);
            }
        }
    }

}

var surveyPlugin = pogs.createPlugin('surveyTaskPlugin',function(){

    console.info("Survey Plugin Loaded");

    var survey = new Survey(this);
    // get config attributes from task plugin
    survey.setupSurvey(this.getStringAttribute("surveyBluePrint"));
    this.subscribeTaskAttributeBroadcast(survey.broadcastReceived.bind(survey))

});