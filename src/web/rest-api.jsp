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
<%@ page import="org.jivesoftware.openfire.container.PluginManager" %>
<%@ page import="org.jivesoftware.openfire.plugin.rest.RESTServicePlugin" %>
<%@ page import="org.jivesoftware.util.CookieUtils" %>
<%@ page import="org.jivesoftware.util.ParamUtils" %>
<%@ page import="org.jivesoftware.util.StringUtils" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.HashSet" %>
<%@ page import="java.util.Map" %>
<%@ page errorPage="error.jsp" %>

<%@ taglib uri="admin" prefix="admin" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<jsp:useBean id="webManager" class="org.jivesoftware.util.WebManager" />
<%
    webManager.init(request, response, session, application, out);

    final boolean save = request.getParameter("save") != null;
    final Cookie csrfCookie = CookieUtils.getCookie(request, "csrf");
    final String csrfParam = ParamUtils.getParameter(request, "csrf");

    final Map<String, String> errors = new HashMap<>();

    if (save) {
        if (csrfCookie == null || csrfParam == null || !csrfCookie.getValue().equals(csrfParam)) {
            errors.put("csrf", "");
        }

        final String secret = ParamUtils.getParameter(request, "secret");
        final boolean enabled = ParamUtils.getBooleanParameter(request, "enabled");
        final String authTypeString = ParamUtils.getParameter(request, "authtype");
        final String allowedIPs = ParamUtils.getParameter(request, "allowedIPs");
        final String customAuthFilterClassName = ParamUtils.getParameter(request, "customAuthFilterClassName");
        final boolean loggingEnabled = ParamUtils.getBooleanParameter(request, "loggingEnabled");

        RESTServicePlugin.AuthType authType = null;
        try {
            authType = RESTServicePlugin.AuthType.valueOf(authTypeString);
        } catch (Exception e) {
            errors.put("authtype", "");
        }

        final PluginManager pluginManager = webManager.getXMPPServer().getPluginManager();
        final RESTServicePlugin plugin = (RESTServicePlugin) pluginManager.getPluginByName("REST API").orElseThrow();

        if (RESTServicePlugin.AuthType.custom.equals(authType)) {
            final String loadingStatus = plugin.validateCustomAuthenticationFilter(customAuthFilterClassName);
            if (loadingStatus != null) {
                errors.put("loadingStatus", loadingStatus);
            }
        }

        if (errors.isEmpty()) {
            final boolean requiresReload = RESTServicePlugin.AuthType.custom.equals(authType) || RESTServicePlugin.AuthType.custom.equals(RESTServicePlugin.AUTH_TYPE.getValue());
            RESTServicePlugin.ENABLED.setValue(enabled);
            RESTServicePlugin.SECRET.setValue(secret == null || secret.isEmpty() ? StringUtils.randomString(16) : secret);
            RESTServicePlugin.AUTH_TYPE.setValue(authType);
            RESTServicePlugin.ALLOWED_IPS.setValue(new HashSet<>(StringUtils.stringToCollection(allowedIPs)));
            RESTServicePlugin.CUSTOM_AUTH_FILTER.setValue(customAuthFilterClassName);
            RESTServicePlugin.SERVICE_LOGGING_ENABLED.setValue(loggingEnabled);

            // Log the event
            webManager.logEvent("Edited REST API properties", "enabled=" + enabled + "\nauthType=" + authType + "\nallowedIPs=" + allowedIPs + "\ncustomAuthFilterClassName=" + customAuthFilterClassName + "\nloggingEnabled=" + loggingEnabled);

            if (requiresReload) {
                final String pluginDir = pluginManager.getPluginPath(plugin).getFileName().toString();
                pluginManager.reloadPlugin(pluginDir);

                // Log the event
                webManager.logEvent("Reloaded plugin REST API (in response to authentication configuration change).", null);
                response.sendRedirect("/plugin-admin.jsp?reloadsuccess=true");
                return;
            }
            response.sendRedirect("rest-api.jsp?success=true");
            return;
        }
    }

    final String csrf = StringUtils.randomString(15);
    CookieUtils.setCookie(request, response, "csrf", csrf, -1);
    pageContext.setAttribute("csrf", csrf);
    pageContext.setAttribute("errors", errors);

    pageContext.setAttribute("secret", RESTServicePlugin.SECRET.getValue());
    pageContext.setAttribute("enabled", RESTServicePlugin.ENABLED.getValue());
    pageContext.setAttribute("authType", RESTServicePlugin.AUTH_TYPE.getValue().name());
    pageContext.setAttribute("allowedIPs", StringUtils.collectionToString(RESTServicePlugin.ALLOWED_IPS.getValue()));
    pageContext.setAttribute("customAuthFilterClassName", RESTServicePlugin.CUSTOM_AUTH_FILTER.getValue());
    pageContext.setAttribute("loggingEnabled", RESTServicePlugin.SERVICE_LOGGING_ENABLED.getValue());
    pageContext.setAttribute("allowedIPsCheckSpoofable", RESTServicePlugin.isAllowedIPsCheckSpoofable());
%>

<html>
<head>
    <title><fmt:message key="restapi.settings.title"/></title>
    <meta name="pageID" content="rest-api"/>
</head>
<body>

<c:if test="${param.success eq 'true' and empty errors}">
    <admin:infobox type="success">
        <fmt:message key="restapi.settings.saved"/>
    </admin:infobox>
</c:if>

<c:if test="${allowedIPsCheckSpoofable}">
    <admin:infobox type="warning">
        <fmt:message key="restapi.settings.warning.spoofable">
            <fmt:param value="<a href=\"../../system-admin-console-access.jsp\">"/>
            <fmt:param value="</a>"/>
        </fmt:message>
    </admin:infobox>
</c:if>

<c:forEach var="err" items="${errors}">
    <admin:infobox type="error">
        <c:choose>
            <c:when test="${err.key eq 'csrf'}">
                <fmt:message key="restapi.settings.error.csrf"/>
            </c:when>
            <c:when test="${err.key eq 'authtype'}">
                <fmt:message key="restapi.settings.error.authtype"/>
            </c:when>
            <c:when test="${err.key eq 'loadingStatus'}">
                <c:out value="${err.value}"/>
            </c:when>
            <c:otherwise>
                <fmt:message key="restapi.settings.error.generic"/>
                <c:if test="${not empty err.value}">
                    <c:out value="${err.value}"/>
                </c:if>
                (<c:out value="${err.key}"/>)
            </c:otherwise>
        </c:choose>
    </admin:infobox>
</c:forEach>

<p>
    <fmt:message key="restapi.settings.info"/>
</p>

<form action="rest-api.jsp" method="post">
    <input type="hidden" name="csrf" value="<c:out value="${csrf}"/>">

    <fmt:message key="restapi.settings.service.title" var="serviceTitle"/>
    <admin:contentBox title="${serviceTitle}">
        <p>
            <fmt:message key="restapi.settings.service.info"/>
        </p>
        <table>
            <tbody>
            <tr>
                <td style="width: 1%; white-space: nowrap">
                    <input type="radio" name="enabled" value="true" id="rb01" ${enabled ? 'checked' : ''}>
                </td>
                <td>
                    <label for="rb01"><b><fmt:message key="restapi.settings.service.enabled"/></b> - <fmt:message key="restapi.settings.service.enabled.info"/></label>
                </td>
            </tr>
            <tr>
                <td style="width: 1%; white-space: nowrap">
                    <input type="radio" name="enabled" value="false" id="rb02" ${enabled ? '' : 'checked'}>
                </td>
                <td>
                    <label for="rb02"><b><fmt:message key="restapi.settings.service.disabled"/></b> - <fmt:message key="restapi.settings.service.disabled.info"/></label>
                </td>
            </tr>
            </tbody>
        </table>
    </admin:contentBox>

    <fmt:message key="restapi.settings.auth.title" var="authTitle"/>
    <admin:contentBox title="${authTitle}">
        <p>
            <fmt:message key="restapi.settings.auth.info"/>
        </p>
        <table>
            <tbody>
            <tr>
                <td style="width: 1%; white-space: nowrap; vertical-align: top">
                    <input type="radio" name="authtype" value="basic" id="http_basic_auth" ${authType eq 'basic' ? 'checked' : ''}>
                </td>
                <td>
                    <label for="http_basic_auth"><b><fmt:message key="restapi.settings.auth.basic"/></b> - <fmt:message key="restapi.settings.auth.basic.info"/></label>
                </td>
            </tr>
            <tr>
                <td style="width: 1%; white-space: nowrap; vertical-align: top">
                    <input type="radio" name="authtype" value="secret" id="secretKeyAuth" ${authType eq 'secret' ? 'checked' : ''}>
                </td>
                <td>
                    <label for="secretKeyAuth"><b><fmt:message key="restapi.settings.auth.secret"/></b> - <fmt:message key="restapi.settings.auth.secret.info"/></label>
                    <table>
                        <tr>
                            <td><label for="text_secret"><fmt:message key="restapi.settings.auth.secret.label"/></label></td>
                            <td><input type="text" name="secret" id="text_secret" size="40" value="<c:out value="${secret}"/>"></td>
                        </tr>
                    </table>
                </td>
            </tr>
            <tr>
                <td style="width: 1%; white-space: nowrap; vertical-align: top">
                    <input type="radio" name="authtype" value="custom" id="customFilterAuth" ${authType eq 'custom' ? 'checked' : ''}>
                </td>
                <td>
                    <label for="customFilterAuth"><b><fmt:message key="restapi.settings.auth.custom"/></b> - <fmt:message key="restapi.settings.auth.custom.info"/></label>
                    <table>
                        <tr>
                            <td><label for="custom_auth_filter_class_name"><fmt:message key="restapi.settings.auth.custom.label"/></label></td>
                            <td><input type="text" name="customAuthFilterClassName" id="custom_auth_filter_class_name" size="60" value="<c:out value="${customAuthFilterClassName}"/>"></td>
                        </tr>
                    </table>
                    <p><strong><fmt:message key="restapi.settings.auth.custom.reload-note"/></strong></p>
                </td>
            </tr>
            </tbody>
        </table>
    </admin:contentBox>

    <fmt:message key="restapi.settings.allowedips.title" var="allowedIPsTitle"/>
    <admin:contentBox title="${allowedIPsTitle}">
        <p>
            <fmt:message key="restapi.settings.allowedips.info"/>
        </p>
        <table>
            <tbody>
            <tr>
                <td style="width: 1%; white-space: nowrap; vertical-align: top">
                    <label for="allowedIPs"><fmt:message key="restapi.settings.allowedips.label"/></label>
                </td>
                <td>
                    <textarea name="allowedIPs" id="allowedIPs" cols="40" rows="3"><c:out value="${allowedIPs}"/></textarea>
                </td>
            </tr>
            </tbody>
        </table>
        <p>
            <fmt:message key="restapi.settings.allowedips.proxy-note">
                <fmt:param value="<a href=\"../../system-admin-console-access.jsp\">"/>
                <fmt:param value="</a>"/>
            </fmt:message>
        </p>
    </admin:contentBox>

    <fmt:message key="restapi.settings.logging.title" var="loggingTitle"/>
    <admin:contentBox title="${loggingTitle}">
        <p>
            <fmt:message key="restapi.settings.logging.info"/>
        </p>
        <table>
            <tbody>
            <tr>
                <td style="width: 1%; white-space: nowrap">
                    <input type="radio" name="loggingEnabled" value="true" id="loggingEnabledInputEnabled" ${loggingEnabled ? 'checked' : ''}>
                </td>
                <td>
                    <label for="loggingEnabledInputEnabled"><b><fmt:message key="restapi.settings.logging.enabled"/></b> - <fmt:message key="restapi.settings.logging.enabled.info"/></label>
                </td>
            </tr>
            <tr>
                <td style="width: 1%; white-space: nowrap">
                    <input type="radio" name="loggingEnabled" value="false" id="loggingEnabledInputDisabled" ${loggingEnabled ? '' : 'checked'}>
                </td>
                <td>
                    <label for="loggingEnabledInputDisabled"><b><fmt:message key="restapi.settings.logging.disabled"/></b> - <fmt:message key="restapi.settings.logging.disabled.info"/></label>
                </td>
            </tr>
            </tbody>
        </table>
    </admin:contentBox>

    <p>
        <fmt:message key="restapi.settings.documentation">
            <fmt:param value="<a href=\"docs/index.html\" target=\"_blank\">"/>
            <fmt:param value="</a>"/>
        </fmt:message>
    </p>

    <input type="submit" name="save" value="<fmt:message key="restapi.settings.save"/>">
</form>

</body>
</html>
