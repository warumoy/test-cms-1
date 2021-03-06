package org.seasar.cms.pluggable;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;

public interface Configuration {

    String PROJECTSTATUS_DEVELOP = "develop";

    String PROJECTSTATUS_RELEASE = "release";

    String KEY_PROJECTSTATUS = "projectStatus";

    String KEY_S2CONTAINER_DISABLE_HOTDEPLOY = "s2container.disableHotdeploy";

    String getProperty(String key);

    String getProperty(String key, String defaultValue);

    Enumeration<String> propertyNames();

    void setProperty(String key, String value);

    void removeProperty(String key);

    void save(OutputStream out, String header) throws IOException;

    boolean equalsProjectStatus(String status);

    boolean isUnderDevelopment();
}
