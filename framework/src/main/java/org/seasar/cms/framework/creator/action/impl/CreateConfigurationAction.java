package org.seasar.cms.framework.creator.action.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.seasar.cms.framework.Configuration;
import org.seasar.cms.framework.Request;
import org.seasar.cms.framework.Response;
import org.seasar.cms.framework.creator.impl.SourceCreatorImpl;
import org.seasar.cms.framework.impl.ConfigurationImpl;

public class CreateConfigurationAction extends AbstractUpdateAction {

    private static final String APP_PROPERTIES_PATH = "src/main/resources/app.properties";

    private static final String PARAMPREFIX_KEY = SourceCreatorImpl.PARAM_PREFIX
        + "key_";

    public CreateConfigurationAction(SourceCreatorImpl sourceCreator) {
        super(sourceCreator);
    }

    public Response act(Request request, String className, File sourceFile,
        File templateFile) {

        String subTask = request.getParameter(PARAM_SUBTASK);
        if ("create".equals(subTask)) {
            return actCreate(request, className, sourceFile, templateFile);
        } else {
            return actDefault(request, className, sourceFile, templateFile);
        }
    }

    Response actDefault(Request request, String className, File sourceFile,
        File templateFile) {

        Map variableMap = new HashMap();
        variableMap.put("request", request);
        variableMap.put("parameters", getParameters(request));
        variableMap.put("configuration", getConfiguration());
        return getSourceCreator().getResponseCreator().createResponse(
            "createConfiguration", variableMap);
    }

    Response actCreate(Request request, String className, File sourceFile,
        File templateFile) {

        String method = request.getParameter(PARAM_METHOD);
        if (method == null) {
            return null;
        }

        Configuration configuration = new ConfigurationImpl();
        for (Iterator itr = request.getParameterNames(); itr.hasNext();) {
            String name = (String) itr.next();
            if (!name.startsWith(PARAMPREFIX_KEY)) {
                continue;
            }
            String value = request.getParameter(name);
            configuration.setProperty(name.substring(PARAMPREFIX_KEY.length()),
                value);
        }

        String projectRoot = configuration
            .getProperty(Configuration.KEY_PROJECTROOT);
        if (projectRoot != null) {
            File file = new File(projectRoot, APP_PROPERTIES_PATH);
            file.getParentFile().mkdirs();
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(file);
                configuration.save(fos, null);
            } catch (IOException ex) {
                throw new RuntimeException("Can't write property file: "
                    + file.getAbsolutePath());
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException ignore) {
                    }
                }
            }
        }

        Map variableMap = new HashMap();
        variableMap.put("request", request);
        variableMap.put("method", method);
        variableMap.put("parameters", getParameters(request));
        return getSourceCreator().getResponseCreator().createResponse(
            "createConfiguration_create", variableMap);
    }

    Configuration getConfiguration() {
        Configuration configuration = getSourceCreator().getConfiguration();
        if (configuration == null) {
            configuration = new ConfigurationImpl();
        }
        return configuration;

    }
}