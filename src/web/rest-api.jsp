<%--
/*
 * Copyright (C) 2022-2026 Ignite Realtime Foundation. All rights reserved.
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
--%>

<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page
    import="java.util.*,
                org.jivesoftware.openfire.XMPPServer,
                org.jivesoftware.util.*,org.jivesoftware.openfire.plugin.rest.RESTServicePlugin,
                org.jivesoftware.openfire.container.PluginManager"
    errorPage="error.jsp"%>
<%@ page import="org.jivesoftware.openfire.container.PluginMetadataHelper" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>

<%-- Define Administration Bean --%>
<jsp:useBean id="admin" class="org.jivesoftware.util.WebManager" />
<c:set var="admin" value="${admin.manager}" />
<%
    admin.init(request, response, session, application, out);
%>

<%
    // Get parameters
    boolean save = request.getParameter("save") != null;
    boolean success = request.getParameter("success") != null;
    String secret = ParamUtils.getParameter(request, "secret");
    boolean enabled = ParamUtils.getBooleanParameter(request, "enabled");
    String authTypeString = ParamUtils.getParameter(request, "authtype");
    String allowedIPs = ParamUtils.getParameter(request, "allowedIPs");
    String customAuthFilterClassName = ParamUtils.getParameter(request, "customAuthFilterClassName");
    boolean loggingEnabled = ParamUtils.getBooleanParameter(request, "loggingEnabled");

    String loadingStatus = null;
    
    final PluginManager pluginManager = admin.getXMPPServer().getPluginManager();
    
    RESTServicePlugin plugin = (RESTServicePlugin) XMPPServer.getInstance().getPluginManager()
            .getPluginByName("REST API").orElse(null);

    // Handle a save
    Map<String, String> errors = new HashMap<>();

    RESTServicePlugin.AuthType authType = null;
    if (save) {
        try {
            authType = RESTServicePlugin.AuthType.valueOf(authTypeString);
        } catch (Exception e) {
            errors.put("authtype", "invalid value");
        }

        if (RESTServicePlugin.AuthType.custom.equals(authType)) {
            loadingStatus = plugin.loadAuthenticationFilter(customAuthFilterClassName);
        }
        if (loadingStatus != null) {
            errors.put("loadingStatus", loadingStatus);
        }

        if (errors.isEmpty())
        {
            boolean is2Reload = RESTServicePlugin.AuthType.custom.equals(authType) || RESTServicePlugin.AuthType.custom.equals(RESTServicePlugin.AUTH_TYPE.getValue());
            RESTServicePlugin.ENABLED.setValue(enabled);
            RESTServicePlugin.SECRET.setValue(secret == null || secret.isEmpty() ? StringUtils.randomString(16) : secret);
            RESTServicePlugin.AUTH_TYPE.setValue(authType);
            RESTServicePlugin.ALLOWED_IPS.setValue(new HashSet<>(StringUtils.stringToCollection(allowedIPs)));
            RESTServicePlugin.CUSTOM_AUTH_FILTER.setValue(customAuthFilterClassName);
            RESTServicePlugin.SERVICE_LOGGING_ENABLED.setValue(loggingEnabled);

            if(is2Reload) {
                String pluginName  = PluginMetadataHelper.getName(plugin);
                String pluginDir = pluginManager.getPluginPath(plugin).getFileName().toString();
                pluginManager.reloadPlugin(pluginDir);
            
                // Log the event
                admin.logEvent("reloaded plugin "+ pluginName, null);
                response.sendRedirect("/plugin-admin.jsp?reloadsuccess=true");
            }
            response.sendRedirect("rest-api.jsp?success=true");
            return;
        }
    }

    secret = RESTServicePlugin.SECRET.getValue();
    enabled = RESTServicePlugin.ENABLED.getValue();
    authType = RESTServicePlugin.AUTH_TYPE.getValue();
    allowedIPs = StringUtils.collectionToString(RESTServicePlugin.ALLOWED_IPS.getValue());
    customAuthFilterClassName = RESTServicePlugin.CUSTOM_AUTH_FILTER.getValue();
    loggingEnabled = RESTServicePlugin.SERVICE_LOGGING_ENABLED.getValue();
%>

<html>
<head>
<title>REST API Properties</title>
<meta name="pageID" content="rest-api" />
</head>
<body>

    <p>Use the form below to enable or disable the REST API and
        configure the authentication.</p>

    <%
        if (success) {
    %>

    <div class="jive-success">
        <table cellpadding="0" cellspacing="0" border="0">
            <tbody>
                <tr>
                    <td class="jive-icon"><img src="images/success-16x16.gif"
                        width="16" height="16" border="0"></td>
                    <td class="jive-icon-label">REST API properties edited
                        successfully.</td>
                </tr>
            </tbody>
        </table>
    </div>
    <br>
    <%
        }
    %>
    
    <%  
        if (errors.get("loadingStatus") != null) { 
    %>
    <div class="jive-error">
        <table cellpadding="0" cellspacing="0" border="0">
            <tbody>
                <tr>
                    <td class="jive-icon"><img src="images/error-16x16.gif"
                        width="16" height="16" border="0"></td>
                    <td class="jive-icon-label"><%= loadingStatus %></td>
                </tr>
            </tbody>
        </table>
    </div>
    <br>
    <%
        }
    %>
    <%
        if (errors.get("authtype") != null) {
    %>
    <div class="jive-error">
        <table cellpadding="0" cellspacing="0" border="0">
            <tbody>
            <tr>
                <td class="jive-icon"><img src="images/error-16x16.gif"
                                           width="16" height="16" border="0"></td>
                <td class="jive-icon-label">Unrecognized authentication type.

                </td>
            </tr>
            </tbody>
        </table>
    </div>
    <br>
    <%
        }
    %>

    <form action="rest-api.jsp?save" method="post">

        <fieldset>
            <legend>REST API</legend>
            <div>
                <p>
                    The REST API can be secured with a shared secret key defined below
                    or a with HTTP basic authentication.<br />Moreover, for extra
                    security you can specify the list of IP addresses that are allowed
                    to use this service.<br />An empty list means that the service can
                    be accessed from any location. Addresses are delimited by commas.
                </p>
                <ul>
                    <input type="radio" name="enabled" value="true" id="rb01"
                        <%=((enabled) ? "checked" : "")%>>
                    <label for="rb01"><b>Enabled</b> - REST API requests will
                        be processed.</label>
                    <br>
                    <input type="radio" name="enabled" value="false" id="rb02"
                        <%=((!enabled) ? "checked" : "")%>>
                    <label for="rb02"><b>Disabled</b> - REST API requests will
                        be ignored.</label>
                    <br>
                    <br>

                    <input type="radio" name="authtype" value="basic"
                        id="http_basic_auth" <%=(RESTServicePlugin.AuthType.basic.equals(authType) ? "checked" : "")%>>
                    <label for="http_basic_auth">HTTP basic auth - REST API
                        authentication with Openfire admin account.</label>
                    <br>
                    <input type="radio" name="authtype" value="secret"
                        id="secretKeyAuth" <%=(RESTServicePlugin.AuthType.secret.equals(authType) ? "checked" : "")%>>
                    <label for="secretKeyAuth">Secret key auth - REST API
                        authentication over specified secret key.</label>
                    <br>
                    <label style="padding-left: 25px" for="text_secret">Secret
                        key:</label>
                    <input type="text" name="secret" value="<%=(secret != null ? secret : "")%>"
                        id="text_secret">
                    <br>
                    <input type="radio" name="authtype" value="custom"
                        id="customFilterAuth" <%=(RESTServicePlugin.AuthType.custom.equals(authType) ? "checked" : "")%>>
                    <label for="customFilterAuth">Custom authentication filter classname - REST API
                        authentication delegates to a custom filter implemented in some other plugin.
                    </label>
                    <div style="margin-left: 20px; margin-top: 5px;"><strong>Note: changing back and forth from custom authentication filter forces the REST API plugin reloading</strong></div>
                    <label style="padding-left: 25px" for="text_secret">Filter 
                        classname:</label>
                    <input type="text" name="customAuthFilterClassName" value="<%= (customAuthFilterClassName != null ? customAuthFilterClassName : "") %>"
                        id="custom_auth_filter_class_name" style="width:70%;padding:4px;">
                    <br>
                    <br>

                    <label for="allowedIPs">Allowed IP Addresses:</label>
                    <textarea name="allowedIPs" cols="40" rows="3" wrap="virtual"><%=((allowedIPs != null) ? allowedIPs : "")%></textarea>
                    <br>
                    <br>

                    <P>Additional Logging</P>
                    <input type="radio" name="loggingEnabled" value="true" id="loggingEnabledInputEnabled"
                        <%=((loggingEnabled) ? "checked" : "")%>>
                    <label for="loggingEnabledInputEnabled"><b>Enabled</b> - Logging Enabled</label>
                    <br>
                    <input type="radio" name="loggingEnabled" value="false" id="loggingEnabledInputDisabled"
                        <%=((!loggingEnabled) ? "checked" : "")%>>
                    <label for="loggingEnabledInputDisabled"><b>Disabled</b> - Logging disabled</label>
                    <br>


                </ul>

                <p>You can find here detailed documentation over the Openfire REST API: 
                    <a href="docs/index.html" target="_blank">REST API Documentation (opens in new tab)</a>
                </p>
            </div>
        </fieldset>

        <br> <br> <input type="submit" value="Save Settings">
    </form>


</body>
</html>
