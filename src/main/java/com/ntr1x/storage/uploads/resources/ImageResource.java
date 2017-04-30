package com.ntr1x.storage.uploads.resources;

import java.util.UUID;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.transaction.Transactional;
import javax.ws.rs.BeanParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import com.ntr1x.storage.core.filters.IUserScope;
import com.ntr1x.storage.core.model.Image;
import com.ntr1x.storage.core.services.IFileService;
import com.ntr1x.storage.core.transport.PageableQuery;
import com.ntr1x.storage.uploads.services.IImageService;
import com.ntr1x.storage.uploads.services.IImageService.ImagePageResponse;

import io.swagger.annotations.Api;

@Api("Uploads")
@Component
@Path("/uploads/images")
public class ImageResource {
    
    @Inject
    private IFileService files;

    @Inject
    private IImageService images;
    
    @Inject
    private Provider<IUserScope> scope;
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ "res:///images/:admin" })
    @Transactional
    public ImagePageResponse list(
        @QueryParam("aspect") String aspect,
        @BeanParam PageableQuery pageable
    ) {
        
        Page<Image> p = images.query(
            scope.get().getId(),
            aspect,
            pageable.toPageRequest()
        );
        
        return new ImagePageResponse(
            p.getTotalElements(),
            p.getNumber(),
            p.getSize(),
            p.getContent()
        );
    }
    
    @GET
    @Path("/i/{id}/{name}.{format}")
    @Transactional
    public Response selectImage(
        @PathParam("id") long id,
        @PathParam("name") String name,
        @PathParam("format") String format
    ) {
        
        Image upload = images.select(scope.get().getId(), id);
        
        return Response
            .ok(files.resolve(String.format("%s/%s.%s", upload.getUuid(), name, format)))
            .header("Content-Type", String.format("image/%s", format))
            .build()
        ;
    }
    
    @GET
    @Path("/u/{uuid}/{name}.{format}")
    @Transactional
    public Response selectImage(
        @PathParam("uuid") UUID uuid,
        @PathParam("name") String name,
        @PathParam("format") String format
    ) {
        // do not use scope here
        Image upload = images.select(null, uuid);
        
        return Response
            .ok(files.resolve(String.format("%s/%s.%s", upload.getUuid(), name, format)))
            .header("Content-Type", String.format("image/%s", format))
            .build()
        ;
    }
    
    @GET
    @Path("/i/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Image select(
        @PathParam("id") long id
    ) {
        Image upload = images.select(scope.get().getId(), id);
        return upload;
    }
    
    @GET
    @Path("/u/{uuid}")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Image select(
        @PathParam("uuid") UUID uuid
    ) {
        // do not use scope here
        return images.select(null, uuid);
    }
    
    @DELETE
    @Path("/i/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    @RolesAllowed({ "res:///images/i/{id}/:admin" })
    public Image remove(
        @PathParam("id") long id
    ) {
        return images.remove(scope.get().getId(), id);
    }
}
