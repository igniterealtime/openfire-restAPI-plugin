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

package org.jivesoftware.openfire.plugin.rest.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.List;

/**
 * The Class RESTExceptionMapper.
 */
@Provider
public class RESTExceptionMapper implements ExceptionMapper<ServiceException> {

    /** The log. */
    private static Logger LOG = LoggerFactory.getLogger(RESTExceptionMapper.class);
    
    /** The headers. */
    @Context
    private HttpHeaders headers;

    @Context
    private HttpServletRequest request;

    /**
     * Instantiates a new REST exception mapper.
     */
    public RESTExceptionMapper() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.ws.rs.ext.ExceptionMapper#toResponse(java.lang.Throwable)
     */
    public Response toResponse(ServiceException exception) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setResource(exception.getResource());
        errorResponse.setMessage(exception.getMessage());
        errorResponse.setException(exception.getException());

        if (exception.getStatus() != null && exception.getStatus().getStatusCode() == 500) {
            LOG.warn(
                exception.getException() + ": " + exception.getMessage() + " with resource "
                    + exception.getResource(), exception.getException());
        } else if (exception.getStatus().getStatusCode() == 404 && "HEAD".equalsIgnoreCase(request.getMethod())) {
            // This is an existence check that has a 'nope' answer that is perfectly valid. This should not be logged by default.
            LOG.debug(exception.getException() + ": " + exception.getMessage() + " with resource " + exception.getResource());
        } else {
            LOG.info(
                exception.getException() + ": " + exception.getMessage() + " with resource "
                    + exception.getResource(), exception.getException());
        }
        
        ResponseBuilder responseBuilder = Response.status(exception.getStatus()).entity(errorResponse);
        List<MediaType> accepts = headers.getAcceptableMediaTypes();

        // If accepts header is given, respect it
        if (accepts.size() == 1 && !accepts.get(0).isWildcardType()) {
            MediaType mediaType = accepts.get(0);
            responseBuilder = responseBuilder.type(mediaType);
        }
        else {
            if (headers.getMediaType() != null) {
                // if accept header is not given, take the content type media type
                responseBuilder = responseBuilder.type(headers.getMediaType());
            } else {
                // if nothing is provided, take XML
                responseBuilder = responseBuilder.type(MediaType.APPLICATION_XML);
            }
        }

        return responseBuilder.build();
    }

}
