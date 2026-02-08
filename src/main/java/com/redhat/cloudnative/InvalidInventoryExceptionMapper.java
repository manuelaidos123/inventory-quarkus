package com.redhat.cloudnative;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class InvalidInventoryExceptionMapper implements ExceptionMapper<InvalidInventoryException> {

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(InvalidInventoryException exception) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(Response.Status.BAD_REQUEST.getStatusCode())
                .error("Bad Request")
                .message(exception.getMessage())
                .path(uriInfo.getRequestUri().getPath())
                .build();

        return Response.status(Response.Status.BAD_REQUEST)
                .entity(errorResponse)
                .type(javax.ws.rs.core.MediaType.APPLICATION_JSON)
                .build();
    }
}