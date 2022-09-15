package ai.txai.common.api;

import ai.txai.common.utils.ReflectionUtils;

/**
 * Time: 20/05/2022
 * Author Hay
 */
public abstract class BaseApiRepository<S extends BaseApiServer> {

    protected S getApiServer() {
        return ApiServerManager.INSTANCE.getApiServer(ReflectionUtils.getFirstGeneric(this, BaseApiServer.class), ApiConfig.INSTANCE.getBaseUrl());
    }
}
