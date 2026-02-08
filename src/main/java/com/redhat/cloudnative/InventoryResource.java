package com.redhat.cloudnative;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.List;

@Path("/api/inventory")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class InventoryResource {

    @GET
    public List<Inventory> listAll(@QueryParam("page") Integer page, @QueryParam("size") Integer size) {
        if (page != null && size != null) {
            return Inventory.findAll()
                    .page(page, size)
                    .list();
        }
        return Inventory.listAll();
    }

    @GET
    @Path("/count")
    @Produces(MediaType.TEXT_PLAIN)
    public Long count() {
        return Inventory.count();
    }

    @GET
    @Path("/{itemId}")
    public Response getAvailability(@PathParam("itemId") Long itemId) {
        Inventory inventory = Inventory.findById(itemId);
        if (inventory == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Inventory item not found with id: " + itemId + "\"}")
                    .build();
        }
        return Response.ok(inventory).build();
    }

    @POST
    @Transactional
    public Response create(Inventory inventory) {
        if (inventory == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Inventory item cannot be null\"}")
                    .build();
        }
        if (inventory.quantity < 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Quantity cannot be negative\"}")
                    .build();
        }
        inventory.persist();
        return Response.created(URI.create("/api/inventory/" + inventory.id))
                .entity(inventory)
                .build();
    }

    @PUT
    @Path("/{itemId}")
    @Transactional
    public Response update(@PathParam("itemId") Long itemId, Inventory updatedInventory) {
        Inventory inventory = Inventory.findById(itemId);
        if (inventory == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Inventory item not found with id: " + itemId + "\"}")
                    .build();
        }
        if (updatedInventory == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Inventory item cannot be null\"}")
                    .build();
        }
        if (updatedInventory.quantity < 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Quantity cannot be negative\"}")
                    .build();
        }
        inventory.quantity = updatedInventory.quantity;
        inventory.persist();
        return Response.ok(inventory).build();
    }

    @PATCH
    @Path("/{itemId}/quantity")
    @Transactional
    public Response updateQuantity(@PathParam("itemId") Long itemId, @QueryParam("quantity") Integer quantity) {
        Inventory inventory = Inventory.findById(itemId);
        if (inventory == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Inventory item not found with id: " + itemId + "\"}")
                    .build();
        }
        if (quantity == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Quantity parameter is required\"}")
                    .build();
        }
        if (quantity < 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Quantity cannot be negative\"}")
                    .build();
        }
        inventory.quantity = quantity;
        inventory.persist();
        return Response.ok(inventory).build();
    }

    @DELETE
    @Path("/{itemId}")
    @Transactional
    public Response delete(@PathParam("itemId") Long itemId) {
        Inventory inventory = Inventory.findById(itemId);
        if (inventory == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Inventory item not found with id: " + itemId + "\"}")
                    .build();
        }
        inventory.delete();
        return Response.noContent().build();
    }

    @DELETE
    @Transactional
    public Response deleteAll() {
        long deleted = Inventory.deleteAll();
        return Response.ok()
                .entity("{\"deleted\": " + deleted + "}")
                .build();
    }
}