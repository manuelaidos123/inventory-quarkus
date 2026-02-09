package com.redhat.cloudnative;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
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

import io.quarkus.cache.Cache;
import io.quarkus.cache.CacheInvalidate;
import io.quarkus.cache.CacheInvalidateAll;
import io.quarkus.cache.CacheName;
import io.quarkus.cache.CacheResult;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.net.URI;
import java.util.List;

@Path("/api/inventory")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Inventory", description = "Inventory management operations")
public class InventoryResource {

    @Inject
    @CacheName("inventory-cache")
    Cache inventoryCache;

    @GET
    @Operation(summary = "List all inventory items", description = "Returns a paginated list of inventory items with metadata")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Paginated list of inventory items", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = PaginatedResponse.class)))
    })
    public PaginatedResponse<Inventory> listAll(
            @Parameter(description = "Page number (0-based)") @QueryParam("page") @DefaultValue("0") int page,
            @Parameter(description = "Page size (max 100)") @QueryParam("size") @DefaultValue("20") int size) {
        // Limit page size to prevent performance issues
        int effectiveSize = Math.min(size, 100);
        List<Inventory> items = Inventory.findAll()
                .page(page, effectiveSize)
                .list();
        long total = Inventory.count();
        return PaginatedResponse.of(items, total, page, effectiveSize);
    }

    @GET
    @Path("/all")
    @Operation(summary = "List all inventory items without pagination", description = "Returns a simple list of all inventory items (use with caution for large datasets)")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "List of all inventory items", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Inventory.class)))
    })
    public List<Inventory> listAllWithoutPagination() {
        return Inventory.listAll();
    }

    @GET
    @Path("/count")
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(summary = "Count inventory items", description = "Returns the total number of inventory items")
    @APIResponse(responseCode = "200", description = "Total count of inventory items")
    public Long count() {
        return Inventory.count();
    }

    @GET
    @Path("/{itemId}")
    @Operation(summary = "Get inventory by ID", description = "Returns a single inventory item by its ID (cached)")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Inventory item found", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Inventory.class))),
            @APIResponse(responseCode = "404", description = "Inventory item not found", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @CacheResult(cacheName = "inventory-cache")
    public Inventory getAvailability(
            @Parameter(description = "Inventory item ID", required = true) @PathParam("itemId") Long itemId) {
        Inventory inventory = Inventory.findById(itemId);
        if (inventory == null) {
            throw new InventoryNotFoundException(itemId);
        }
        return inventory;
    }

    @GET
    @Path("/product/{productId}")
    @Operation(summary = "Get inventory by product ID", description = "Returns the inventory item for a specific product (cached)")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Inventory item found", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Inventory.class))),
            @APIResponse(responseCode = "404", description = "Inventory item not found for the product", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @CacheResult(cacheName = "inventory-product-cache")
    public Inventory getByProductId(
            @Parameter(description = "Product ID", required = true) @PathParam("productId") Long productId) {
        Inventory inventory = Inventory.findByProductId(productId);
        if (inventory == null) {
            throw new InventoryNotFoundException(productId);
        }
        return inventory;
    }

    @POST
    @Transactional
    @Operation(summary = "Create inventory item", description = "Creates a new inventory item")
    @APIResponses(value = {
            @APIResponse(responseCode = "201", description = "Inventory item created", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Inventory.class))),
            @APIResponse(responseCode = "400", description = "Invalid inventory data", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @CacheInvalidateAll(cacheName = "inventory-cache")
    @CacheInvalidateAll(cacheName = "inventory-product-cache")
    public Response create(
            @RequestBody(description = "Inventory item to create", required = true, content = @Content(schema = @Schema(implementation = Inventory.class))) @Valid Inventory inventory) {
        // Clear any provided ID to let the database auto-generate it
        inventory.id = null;
        inventory.persist();
        return Response.created(URI.create("/api/inventory/" + inventory.id))
                .entity(inventory)
                .build();
    }

    @PUT
    @Path("/{itemId}")
    @Transactional
    @Operation(summary = "Update inventory item", description = "Updates an existing inventory item completely")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Inventory item updated", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Inventory.class))),
            @APIResponse(responseCode = "400", description = "Invalid inventory data", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "404", description = "Inventory item not found", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @CacheInvalidate(cacheName = "inventory-cache")
    @CacheInvalidateAll(cacheName = "inventory-product-cache")
    public Inventory update(
            @Parameter(description = "Inventory item ID", required = true) @PathParam("itemId") Long itemId,
            @RequestBody(description = "Updated inventory data", required = true, content = @Content(schema = @Schema(implementation = Inventory.class))) @Valid Inventory updatedInventory) {
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
    @Operation(summary = "Update inventory quantity", description = "Updates only the quantity of an inventory item")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Quantity updated", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Inventory.class))),
            @APIResponse(responseCode = "400", description = "Invalid quantity value", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "404", description = "Inventory item not found", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @CacheInvalidate(cacheName = "inventory-cache")
    @CacheInvalidateAll(cacheName = "inventory-product-cache")
    public Inventory updateQuantity(
            @Parameter(description = "Inventory item ID", required = true) @PathParam("itemId") Long itemId,
            @RequestBody(description = "New quantity value", required = true, content = @Content(schema = @Schema(implementation = QuantityUpdateRequest.class))) @Valid QuantityUpdateRequest request) {
        Inventory inventory = Inventory.findById(itemId);
        if (inventory == null) {
            throw new InventoryNotFoundException(itemId);
        }
        inventory.quantity = request.getQuantity();
        inventory.persist();
        return inventory;
    }

    @DELETE
    @Path("/{itemId}")
    @Transactional
    @Operation(summary = "Delete inventory item", description = "Deletes an inventory item by its ID")
    @APIResponses(value = {
            @APIResponse(responseCode = "204", description = "Inventory item deleted"),
            @APIResponse(responseCode = "404", description = "Inventory item not found", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @CacheInvalidate(cacheName = "inventory-cache")
    @CacheInvalidateAll(cacheName = "inventory-product-cache")
    public Response delete(
            @Parameter(description = "Inventory item ID", required = true) @PathParam("itemId") Long itemId) {
        Inventory inventory = Inventory.findById(itemId);
        if (inventory == null) {
            throw new InventoryNotFoundException(itemId);
        }
        inventory.delete();
        return Response.noContent().build();
    }

    /**
     * Clear all caches - useful for administrative purposes
     */
    @DELETE
    @Path("/cache")
    @Operation(summary = "Clear all inventory caches", description = "Clears all cached inventory data")
    @APIResponse(responseCode = "204", description = "Caches cleared")
    @CacheInvalidateAll(cacheName = "inventory-cache")
    @CacheInvalidateAll(cacheName = "inventory-product-cache")
    public Response clearCaches() {
        return Response.noContent().build();
    }

}
