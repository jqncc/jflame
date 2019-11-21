package org.jflame.apidoc.model;

import org.jflame.apidoc.enums.ParamPos;

/**
 * 接口参数描述
 *
 * @author yucan.zhang
 */
public class ApiParam extends ApiElement {

    private static final long serialVersionUID = -6264640107374056998L;

    private ParamPos pos;

    public ParamPos getPos() {
        return pos;
    }

    public void setPos(ParamPos pos) {
        this.pos = pos;
    }

}
