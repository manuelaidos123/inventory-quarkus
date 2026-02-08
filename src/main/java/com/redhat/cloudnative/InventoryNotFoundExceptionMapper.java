package com.redhat.cloudnative;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class InventoryNotFoundExceptionMapper implements ExceptionMapper<InventoryNotFoundException> {

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(InventoryNotFoundException exception) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(Response.Status.NOT_FOUND.getStatusCode())
                .error("Not Found")
                .message(exception.getMessage())
                .path(uriInfo.getRequestUri().getPath())
                .build();

        return Response.status(Response.Status.NOT_FOUND)
                .entity(errorResponse)
                .type(javax.ws.rs.core.MediaType.APPLICATION_JSON)
                .build();
    }
}