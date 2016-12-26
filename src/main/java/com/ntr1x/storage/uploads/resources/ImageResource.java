package com.ntr1x.storage.uploads.resources;

import java.util.UUID;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.stereotype.Component;

import com.ntr1x.storage.core.model.Image;
import com.ntr1x.storage.core.services.IFileService;
import com.ntr1x.storage.core.transport.PageResponse;
import com.ntr1x.storage.core.transport.PageableQuery;
import com.ntr1x.storage.uploads.services.IImageService;

import io.swagger.annotations.Api;

@Api("Uploads")
@Component
@Path("/images")
public class ImageResource {
    
    @Inject
    private IFileService files;

    @Inject
    private IImageService images;
    
    @PersistenceContext
    private EntityManager em;
    
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ "res:///images:admin" })
	@Transactional
	public PageResponse<Image> list(
	    @QueryParam("aspect") String aspect,
		@BeanParam PageableQuery pageable
	) {
		
		return new PageResponse<>(
			images.query(
				aspect,
				pageable.toPageRequest()
			)
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
	    
	    Image upload = images.select(id);
	    
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
        
        Image upload = images.select(uuid);
        
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
        Image upload = em.find(Image.class, id);
        return upload;
    }
	
	@GET
    @Path("/u/{uuid}")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Image select(
        @PathParam("uuid") UUID uuid
    ) {
	    return images.select(uuid);
    }
}