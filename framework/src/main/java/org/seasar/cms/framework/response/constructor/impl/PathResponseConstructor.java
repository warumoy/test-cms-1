package org.seasar.cms.framework.response.constructor.impl;

import org.seasar.cms.framework.Path;
import org.seasar.cms.framework.Response;
import org.seasar.cms.framework.impl.RedirectResponse;
import org.seasar.cms.framework.impl.VoidResponse;

public class PathResponseConstructor extends AbstractResponseConstructor {

    public Class getTargetClass() {

        return Path.class;
    }

    public Response constructResponse(Object component, Object returnValue) {

        Path path = (Path) returnValue;
        if (path == null) {
            return VoidResponse.INSTANCE;
        }

        return new RedirectResponse(path.toString());
    }
}