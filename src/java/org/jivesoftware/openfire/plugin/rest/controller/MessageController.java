package org.jivesoftware.openfire.plugin.rest.controller;

import javax.ws.rs.core.Response;

import org.jivesoftware.openfire.SessionManager;
import org.jivesoftware.openfire.plugin.rest.entity.MessageEntity;
import org.jivesoftware.openfire.plugin.rest.exceptions.ExceptionType;
import org.jivesoftware.openfire.plugin.rest.exceptions.ServiceException;
import org.xmpp.packet.JID;

/**
 * The Class MessageController.
 */
public class MessageController {
    /** The Constant INSTANCE. */
    public static final MessageController INSTANCE = new MessageController();

    /**
     * Gets the single instance of MessageController.
     *
     * @return single instance of MessageController
     */
    public static MessageController getInstance() {
        return INSTANCE;
    }

    /**
     * Send broadcast message.
     *
     * @param messageEntity
     *            the message entity
     * @throws ServiceException
     *             the service exception
     */
    public void sendBroadcastMessage(MessageEntity messageEntity) throws ServiceException {
        if (messageEntity.getBody() != null && !messageEntity.getBody().isEmpty()) {
            SessionManager.getInstance().sendServerMessage(null, messageEntity.getBody());
        } else {
            throw new ServiceException("Message content/body is null or empty", "",
                    ExceptionType.ILLEGAL_ARGUMENT_EXCEPTION,
                    Response.Status.BAD_REQUEST);
        }
    }
    /**
     * Send broadcast message to a user.
     *
     * @param messageEntity the message entity
     * @param address       the recipient address
     * @param resource      the recipient resource (optional)
     * @throws ServiceException the service exception
     */
    public void sendMessageToUser(MessageEntity messageEntity, String address, String resource) throws ServiceException {
        if (messageEntity.getBody() == null || messageEntity.getBody().isEmpty()) {
            throw new ServiceException("Message content/body is null or empty", "",
                ExceptionType.ILLEGAL_ARGUMENT_EXCEPTION,
                Response.Status.BAD_REQUEST);
        }
        if (address == null) {
            throw new ServiceException("Invalid recipient", "",
                ExceptionType.ILLEGAL_ARGUMENT_EXCEPTION,
                Response.Status.BAD_REQUEST);
        }



        JID jabberId = new JID(address);
        if (resource != null) {
            jabberId = new JID(jabberId.getNode(), jabberId.getDomain(), resource);
        }

        try{
            SessionManager.getInstance().sendServerMessage(jabberId, null, messageEntity.getBody());
        } catch (Exception e) {
            throw new ServiceException("Error while sending Message","",
                ExceptionType.ILLEGAL_ARGUMENT_EXCEPTION,
                Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
}
