package org.seasar.cms.ymir.response.scheme.impl;

import org.seasar.cms.ymir.Response;
import org.seasar.cms.ymir.impl.ForwardResponse;
import org.seasar.cms.ymir.response.scheme.Strategy;

public class ForwardStrategy implements Strategy {

    private static final String SCHEME = "forward";

    public String getScheme() {

        return SCHEME;
    }

    public Response constructResponse(String path, Object component) {

        return new ForwardResponse(path);
    }
}