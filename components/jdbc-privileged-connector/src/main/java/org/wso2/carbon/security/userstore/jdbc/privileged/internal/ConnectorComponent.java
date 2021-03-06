/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.security.userstore.jdbc.privileged.internal;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.datasource.core.api.DataSourceService;
import org.wso2.carbon.security.caas.user.core.constant.UserCoreConstants;
import org.wso2.carbon.security.caas.user.core.store.connector.CredentialStoreConnectorFactory;
import org.wso2.carbon.security.caas.user.core.store.connector.IdentityStoreConnectorFactory;
import org.wso2.carbon.security.caas.user.core.util.PasswordHandler;
import org.wso2.carbon.security.userstore.jdbc.privileged.connector.factory
        .JDBCPrivilegedCredentialStoreConnectorFactory;
import org.wso2.carbon.security.userstore.jdbc.privileged.connector.factory.JDBCPrivilegedIdentityStoreConnectorFactory;
import org.wso2.carbon.security.userstore.jdbc.privileged.util.DatabaseUtil;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;

/**
 * OSGi component for carbon security connectors.
 * @since 1.0.0
 */
@Component(
        name = "org.wso2.carbon.security.userstore.jdbc.privileged.internal.ConnectorComponent",
        immediate = true
)
public class ConnectorComponent {

    private static final Logger log = LoggerFactory.getLogger(ConnectorComponent.class);

    /**
     * Register user store connectors as OSGi services.
     * @param bundleContext Bundle Context.
     */
    @Activate
    public void registerCarbonSecurityConnectors(BundleContext bundleContext) {

        Dictionary<String, String> connectorProperties = new Hashtable<>();

        connectorProperties.put("connector-type", "JDBCPrivilegedIdentityStore");
        bundleContext.registerService(IdentityStoreConnectorFactory.class, new
                        JDBCPrivilegedIdentityStoreConnectorFactory(),
                connectorProperties);

        connectorProperties = new Hashtable<>();
        connectorProperties.put("connector-type", "JDBCPrivilegedCredentialStore");
        bundleContext.registerService(CredentialStoreConnectorFactory.class, new
                        JDBCPrivilegedCredentialStoreConnectorFactory(),
                connectorProperties);

        log.info("JDBC user store bundle successfully activated.");
    }

    @Reference(
            name = "org.wso2.carbon.datasource.DataSourceService",
            service = DataSourceService.class,
            cardinality = ReferenceCardinality.AT_LEAST_ONE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterDataSourceService"
    )
    protected void registerDataSourceService(DataSourceService service, Map<String, String> properties) {

        if (service == null) {
            log.error("Data source service is null. Registering data source service is unsuccessful.");
            return;
        }

        DatabaseUtil.getInstance().setDataSourceService(service);

        if (log.isDebugEnabled()) {
            log.debug("Data source service registered successfully.");
        }
    }

    protected void unregisterDataSourceService(DataSourceService service) {

        if (log.isDebugEnabled()) {
            log.debug("Data source service unregistered.");
        }
        DatabaseUtil.getInstance().setDataSourceService(null);
    }

    @Reference(
            name = "org.wso2.carbon.security.caas.user.core.util.PasswordHandler",
            service = PasswordHandler.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterPasswordHandler"
    )
    protected void registerPasswordHandler(PasswordHandler passwordHandler, Map<String, String> properties) {

        if (passwordHandler != null) {
            DatabaseUtil.getInstance().setPasswordHandler(properties.get(UserCoreConstants.PASSWORD_HANDLER_NAME),
                    passwordHandler);
            if (log.isDebugEnabled()) {
                log.debug("Password handler for name {} registered.", properties.get(UserCoreConstants
                        .PASSWORD_HANDLER_NAME));
            }
        }
    }

    protected void unregisterPasswordHandler(PasswordHandler passwordHandler, Map<String, String> properties) {

        DatabaseUtil.getInstance().setPasswordHandler(properties.get(UserCoreConstants.PASSWORD_HANDLER_NAME), null);

        if (log.isDebugEnabled()) {
            log.debug("Password handler for name {} unregistered.", properties.get(UserCoreConstants
                    .PASSWORD_HANDLER_NAME));
        }
    }
}
