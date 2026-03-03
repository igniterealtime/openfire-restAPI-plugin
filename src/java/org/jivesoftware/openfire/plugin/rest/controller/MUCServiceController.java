/*
 * Copyright (c) 2022.
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

package org.jivesoftware.openfire.plugin.rest.controller;

import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.muc.ConflictException;
import org.jivesoftware.openfire.muc.ForbiddenException;
import org.jivesoftware.openfire.muc.MultiUserChatService;
import org.jivesoftware.openfire.muc.NotAllowedException;
import org.jivesoftware.openfire.plugin.rest.RESTServicePlugin;
import org.jivesoftware.openfire.plugin.rest.entity.MUCServiceEntities;
import org.jivesoftware.openfire.plugin.rest.entity.MUCServiceEntity;
import org.jivesoftware.openfire.plugin.rest.exceptions.ExceptionType;
import org.jivesoftware.openfire.plugin.rest.exceptions.ServiceException;
import org.jivesoftware.util.AlreadyExistsException;
import org.jivesoftware.util.JiveGlobals;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.ws.rs.core.Response;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The Class MUCServiceController.
 */
public class MUCServiceController {
    private static final Logger LOG = LoggerFactory.getLogger(MUCServiceController.class);

    /** The Constant INSTANCE. */
    private static MUCServiceController INSTANCE = null;

    /**
     * Gets the single instance of MUCServiceController.
     *
     * @return single instance of MUCServiceController
     */
    public static MUCServiceController getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MUCServiceController();
        }
        return INSTANCE;
    }

    public static void log(String logMessage) {
        if (JiveGlobals.getBooleanProperty(RESTServicePlugin.SERVICE_LOGGING_ENABLED, false)) {
            LOG.info(logMessage);
        }
    }

    /**
     * Returns the MultiUserChatService instance for the provided name.
     *
     * This method returns any service that matches the provided name case-insensitively, but prefers a case-sensitive
     * match.
     *
     * @param serviceName The name of the service.
     * @return The service
     * @throws ServiceException When no service for the provided name exists.
     */
    @Nonnull
    protected static MultiUserChatService getService(@Nonnull final String serviceName) throws ServiceException
    {
        Set<MultiUserChatService> services = XMPPServer.getInstance().getMultiUserChatManager().getMultiUserChatServices()
            .stream().filter(multiUserChatService -> multiUserChatService.getServiceName().equalsIgnoreCase(serviceName))
            .collect(Collectors.toSet());

        if (services.isEmpty()) {
            throw new ServiceException("Chat service does not exist or is not accessible.", serviceName, ExceptionType.MUCSERVICE_NOT_FOUND, Response.Status.NOT_FOUND);
        }

        if (services.size() > 1) {
            // Multiple services by the name case-insensitive name. This is dodgy. Try finding an exact (by case) match.
            final Optional<MultiUserChatService> exactMatch = services.stream().filter(multiUserChatService -> multiUserChatService.getServiceName().equals(serviceName)).findAny();
            if (exactMatch.isPresent()) {
                return exactMatch.get();
            }
            // This is even more dodgy (and really shouldn't occur).
            LOG.warn("Found multiple services matching the service name '{}' when doing a case-insensitive lookup, but none when doing a case-sensitive lookup. Returning an arbitrary one of those that match case-insensitively.", serviceName);
        }

        return services.iterator().next();
    }

    /**
     * Creates the chat service.
     *
     * @param mucServiceEntity
     *            the MUC service entity
     * @throws ServiceException
     *             the service exception
     */
    public void createChatService(MUCServiceEntity mucServiceEntity) throws ServiceException {
        log("Create a chat service: " + mucServiceEntity.getServiceName());
        try {
            createService(mucServiceEntity);
        } catch (NotAllowedException | ForbiddenException e) {
            throw new ServiceException("Could not create the chat service", mucServiceEntity.getServiceName(),
                ExceptionType.NOT_ALLOWED, Response.Status.FORBIDDEN, e);
        } catch (ConflictException e) {
            throw new ServiceException("Could not create the chat service", mucServiceEntity.getServiceName(),
                ExceptionType.NOT_ALLOWED, Response.Status.CONFLICT, e);
        } catch (AlreadyExistsException e) {
            throw new ServiceException("Could not create the chat service", mucServiceEntity.getServiceName(),
                ExceptionType.ALREADY_EXISTS, Response.Status.CONFLICT, e);
        }
    }


    /**
     * Creates the service.
     *
     * @param mucServiceEntity
     *            the MUC service entity
     * @throws NotAllowedException
     *             the not allowed exception
     * @throws ForbiddenException
     *             the forbidden exception
     * @throws ConflictException
     *             the conflict exception
     * @throws AlreadyExistsException
     *             the already exists exception
     */
    private void createService(MUCServiceEntity mucServiceEntity) throws NotAllowedException,
        ForbiddenException, ConflictException, AlreadyExistsException
    {
        log("Create a chat service: " + mucServiceEntity.getServiceName());

        // Create the new service
        XMPPServer.getInstance().getMultiUserChatManager().createMultiUserChatService(
            mucServiceEntity.getServiceName(),
            mucServiceEntity.getDescription(),
            mucServiceEntity.isHidden()
        );
    }

    /**
     * Returns all available chat services.
     * @return
     *              all available chat services
     */
    public MUCServiceEntities getChatServices() {
        return new MUCServiceEntities(
            XMPPServer.getInstance().getMultiUserChatManager().getMultiUserChatServices().stream()
            .map(mucs -> new MUCServiceEntity(mucs.getServiceName(), mucs.getDescription(), mucs.isHidden()))
            .collect(Collectors.toList())
        );
    }
}
