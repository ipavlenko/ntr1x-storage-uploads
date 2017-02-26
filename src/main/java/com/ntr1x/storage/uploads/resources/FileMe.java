package com.ntr1x.storage.uploads.resources;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.io.FileUtils;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.stereotype.Component;

import com.ntr1x.storage.core.filters.IUserScope;
import com.ntr1x.storage.core.model.Upload;
import com.ntr1x.storage.core.services.ISerializationService;
import com.ntr1x.storage.security.filters.IUserPrincipal;
import com.ntr1x.storage.uploads.services.IUploadService;
import com.ntr1x.storage.uploads.services.IUploadService.UploadCreate;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Api("Me")
@Component
@Path("/me/uploads/files")
public class FileMe {
	
	private static final String SETTINGS_EXAMPLE =
        "{ \"aspects\": [ \"upload\" ] }";
		
	
	@PersistenceContext
    private EntityManager em;
	
    @Inject
    private IUploadService uploads;
    
    @Inject
    private ISerializationService serialization;
    
    @Inject
    private Provider<IUserScope> scope;
    
    @Inject
    private Provider<IUserPrincipal> principal;
	
	@POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ "auth" })
    @Transactional
    @ApiOperation("Alternative for the POST /me/uploads/files/m2m. Workaround for: https://github.com/OAI/OpenAPI-Specification/issues/222")
    public Upload upload(
            @ApiParam(name = "file") @FormDataParam("file") InputStream stream,
            @FormDataParam("file") FormDataContentDisposition header,
            @ApiParam(example = SETTINGS_EXAMPLE) @FormDataParam("settings") String settings
    ) {

        return uploads.upload(
    		scope.get().getId(),
    		new UploadCreate(
	    		() -> {
	    			File source = Files.createTempFile("upload", ".tmp").toFile();
		            FileUtils.copyInputStreamToFile(stream, source);
	    	        return source;
	    		},
	    		principal.get().getUser().getId(),
	    		header == null ? null : header.getFileName(),
				serialization.parseJSONStringJackson(IUploadService.UploadSettings.class, settings)
			)
		);
    }
    
	// Workaround for https://github.com/OAI/OpenAPI-Specification/issues/222
    @POST
    @Path("/m2m")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ "auth" })
    @Transactional
    @ApiOperation("Doesn't work from the Swagger-UI. Discussed here: https://github.com/OAI/OpenAPI-Specification/issues/222")
    public Upload upload(
			@ApiParam(name = "file") @FormDataParam("file") InputStream stream,
			@FormDataParam("file") FormDataContentDisposition header,
			@ApiParam(example = SETTINGS_EXAMPLE) @FormDataParam("settings") IUploadService.UploadSettings settings
    ) {

        return uploads.upload(
    		scope.get().getId(),
    		new UploadCreate(
	    		() -> {
	    			File source = Files.createTempFile("upload", ".tmp").toFile();
		            FileUtils.copyInputStreamToFile(stream, source);
	    	        return source;
	    		},
	    		principal.get().getUser().getId(),
	    		header == null ? null : header.getFileName(),
				settings
			)
		);
    }
}
