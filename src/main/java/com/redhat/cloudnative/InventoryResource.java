package com.redhat.cloudnative;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

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
    public Inventory getAvailability(@PathParam("itemId") Long itemId) {
        Inventory inventory = Inventory.findById(itemId);
        if (inventory == null) {
            throw new InventoryNotFoundException(itemId);
        }
        return inventory;
    }

    @POST
    @Transactional
    public Response create(@Valid Inventory inventory) {
        inventory.persist();
        return Response.created(URI.create("/api/inventory/" + inventory.id))
                .entity(inventory)
                .build();
    }

    @PUT
    @Path("/{itemId}")
    @Transactional
    public Inventory update(@PathParam("itemId") Long itemId, @Valid Inventory updatedInventory) {
        Inventory inventory = Inventory.findById(itemId);
        if (inventory == null) {
            throw new InventoryNotFoundException(itemId);
        }
        inventory.quantity = updatedInventory.quantity;
        inventory.persist();
        return inventory;
    }

    @PATCH
    @Path("/{itemId}/quantity")
    @Transactional
    public Inventory updateQuantity(@PathParam("itemId") Long itemId, @QueryParam("quantity") Integer quantity) {
        if (quantity == null) {
            throw new InvalidInventoryException("Quantity parameter is required");
        }
        if (quantity < 0) {
            throw new InvalidInventoryException("Quantity cannot be negative");
        }
        Inventory inventory = Inventory.findById(itemId);
        if (inventory == null) {
            throw new InventoryNotFoundException(itemId);
        }
        inventory.quantity = quantity;
        inventory.persist();
        return inventory;
    }

    @DELETE
    @Path("/{itemId}")
    @Transactional
    public Response delete(@PathParam("itemId") Long itemId) {
        Inventory inventory = Inventory.findById(itemId);
        if (inventory == null) {
            throw new InventoryNotFoundException(itemId);
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