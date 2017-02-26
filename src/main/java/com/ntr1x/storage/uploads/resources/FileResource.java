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
import com.ntr1x.storage.core.model.Upload;
import com.ntr1x.storage.core.services.IFileService;
import com.ntr1x.storage.core.transport.PageableQuery;
import com.ntr1x.storage.uploads.services.IUploadService;
import com.ntr1x.storage.uploads.services.IUploadService.UploadPageResponse;

import io.swagger.annotations.Api;

@Api("Uploads")
@Component
@Path("/uploads/files")
public class FileResource {
    
	@Inject
    private IFileService files;
	
    @Inject
    private IUploadService uploads;
    
    @Inject
    private Provider<IUserScope> scope;
    
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ "res:///uploads:admin" })
	@Transactional
	public UploadPageResponse list(
	    @QueryParam("aspect") String aspect,
		@BeanParam PageableQuery pageable
	) {
		
		Page<Upload> p = uploads.query(
			scope.get().getId(),
			aspect,
			pageable.toPageRequest()
		);
        
        return new UploadPageResponse(
    		p.getTotalElements(),
    		p.getNumber(),
    		p.getSize(),
    		p.getContent()
		);
	}
	
	@GET
	@Path("/i/{id}/file")
    @Transactional
    public Response selectFile(
        @PathParam("id") long id
    ) {
	    
	    Upload upload = uploads.select(scope.get().getId(), id);
	    
	    String original = upload.getOriginal();
	    
        return Response
            .ok(files.resolve(upload.getUuid().toString()))
            .header("Content-Type", MediaType.APPLICATION_OCTET_STREAM)
            .header("Content-Disposition", String.format("attachment; filename=\"%s\"", original == null ? "file" : original.replaceAll("\"","\\\"")))
            .build()
        ;
    }
	
	@GET
    @Path("/u/{uuid}/file")
    @Transactional
    public Response selectFile(
        @PathParam("uuid") UUID uuid
    ) {
		// do not use scope here
        Upload upload = uploads.select(null, uuid);
        
        String original = upload.getOriginal();
	    
        return Response
    		.ok(files.resolve(upload.getUuid().toString()))
            .header("Content-Type", MediaType.APPLICATION_OCTET_STREAM)
            .header("Content-Disposition", String.format("attachment; filename=\"%s\"", original == null ? "file" : original.replaceAll("\"","\\\"")))
            .build()
        ;
    }
	
	@GET
    @Path("/i/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Upload select(
        @PathParam("id") long id
    ) {
        Upload upload = uploads.select(scope.get().getId(), id);
        return upload;
    }
	
	@GET
    @Path("/u/{uuid}")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Upload select(
        @PathParam("uuid") UUID uuid
    ) {
		// do not use scope here
	    return uploads.select(null, uuid);
    }
	
	@DELETE
    @Path("/i/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    @RolesAllowed({ "res:///uploads/i/{id}:admin" })
    public Upload remove(
		@PathParam("id") long id
    ) {
	    return uploads.remove(scope.get().getId(), id);
    }
}
