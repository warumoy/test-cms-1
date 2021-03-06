package org.seasar.cms.ymir.extension.creator.impl;

import java.io.File;

import org.seasar.cms.ymir.extension.creator.PathMetaData;
import org.seasar.cms.ymir.extension.creator.SourceCreator;
import org.seasar.cms.ymir.extension.creator.Template;

public class LazyPathMetaData implements PathMetaData {

    private SourceCreator sourceCreator_;

    private String path_;

    private String method_;

    private String forwardPath_;

    private boolean deniedLoaded_;

    private boolean denied_;

    private String componentName_;

    private boolean componentNameLoaded_;

    private String className_;

    private boolean classNameLoaded_;

    private String actionName_;

    private boolean actionNameLoaded_;

    private String defaultPath_;

    private boolean defaultPathLoaded_;

    private File sourceFile_;

    private boolean sourceFileLoaded_;

    private File baseSourceFile_;

    private boolean baseSourceFileLoaded_;

    private Template template_;

    private boolean templateLoaded_;

    public LazyPathMetaData(SourceCreator sourceCreator, String path,
            String method, String forwardPath) {

        sourceCreator_ = sourceCreator;
        path_ = path;
        method_ = method;
        forwardPath_ = strip(forwardPath);
    }

    String strip(String path) {
        if (path == null) {
            return null;
        }
        int question = path.indexOf('?');
        if (question < 0) {
            return path;
        } else {
            return path.substring(0, question);
        }
    }

    public String getMethod() {

        return method_;
    }

    public String getPath() {

        return path_;
    }

    public String getForwardPath() {

        return forwardPath_;
    }

    public boolean isDenied() {

        if (!deniedLoaded_) {
            denied_ = sourceCreator_.isDenied(path_, method_);
            defaultPathLoaded_ = true;
        }
        return denied_;
    }

    public String getComponentName() {

        if (!componentNameLoaded_) {
            componentName_ = sourceCreator_.getComponentName(path_, method_);
            componentNameLoaded_ = true;
        }
        return componentName_;
    }

    public String getClassName() {

        if (!classNameLoaded_) {
            className_ = sourceCreator_.getClassName(getComponentName());
            classNameLoaded_ = true;
        }
        return className_;
    }

    public String getActionName() {

        if (!actionNameLoaded_) {
            actionName_ = sourceCreator_.getActionName(path_, method_);
            actionNameLoaded_ = true;
        }
        return actionName_;
    }

    public File getBaseSourceFile() {

        if (!baseSourceFileLoaded_) {
            baseSourceFile_ = sourceCreator_.getSourceFile(getClassName()
                    + "Base");
            baseSourceFileLoaded_ = true;
        }
        return baseSourceFile_;
    }

    public File getSourceFile() {

        if (!sourceFileLoaded_) {
            sourceFile_ = sourceCreator_.getSourceFile(getClassName());
            sourceFileLoaded_ = true;
        }
        return sourceFile_;
    }

    public Template getTemplate() {

        if (!templateLoaded_) {
            if (forwardPath_ != null) {
                template_ = sourceCreator_.getTemplate(forwardPath_);
            } else {
                template_ = null;
            }
            templateLoaded_ = true;
        }
        return template_;
    }
}
