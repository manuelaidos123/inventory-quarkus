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
    public Inventory getAvailability(@PathParam("itemId") Long itemId) {
        Inventory inventory = Inventory.findById(itemId);
        if (inventory == null) {
            throw new InventoryNotFoundException(itemId);
        }
        return inventory;
    }

    @POST
    @Transactional
    public Response create(Inventory inventory) {
        validateInventory(inventory);
        inventory.persist();
        return Response.created(URI.create("/api/inventory/" + inventory.id))
                .entity(inventory)
                .build();
    }

    @PUT
    @Path("/{itemId}")
    @Transactional
    public Inventory update(@PathParam("itemId") Long itemId, Inventory updatedInventory) {
        Inventory inventory = Inventory.findById(itemId);
        if (inventory == null) {
            throw new InventoryNotFoundException(itemId);
        }
        validateInventory(updatedInventory);
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

    private void validateInventory(Inventory inventory) {
        if (inventory == null) {
            throw new InvalidInventoryException("Inventory item cannot be null");
        }
        if (inventory.quantity < 0) {
            throw new InvalidInventoryException("Quantity cannot be negative");
        }
    }
}