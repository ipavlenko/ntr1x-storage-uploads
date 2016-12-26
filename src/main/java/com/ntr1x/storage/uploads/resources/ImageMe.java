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

import com.ntr1x.storage.core.model.Image;
import com.ntr1x.storage.security.model.ISession;
import com.ntr1x.storage.uploads.converter.ImageSettingsProvider;
import com.ntr1x.storage.uploads.services.IImageService;
import com.ntr1x.storage.uploads.services.IImageService.ImageCreate;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Api("Me")
@Component
@Path("/me/images")
public class ImageMe {
	
	private static final String SETTINGS_EXAMPLE =
        "{ \"items\":[ { \"name\": \"cover-240x100\", \"format\": \"png\", \"width\": 240, \"height\": 100, \"type\": \"COVER\" } ] }";
	
	@PersistenceContext
    private EntityManager em;
	
    @Inject
    private IImageService images;
    
    @Inject
    private Provider<ISession> session;
	
	@POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ "auth" })
    @Transactional
    @ApiOperation("Alternative for the POST /images/m2m method. Workaround for: https://github.com/OAI/OpenAPI-Specification/issues/222")
    public Image upload(
            @ApiParam(name = "file") @FormDataParam("file") InputStream stream,
            @FormDataParam("file") FormDataContentDisposition header,
            @ApiParam(example = SETTINGS_EXAMPLE) @FormDataParam("settings") String settings
    ) {

        return images.upload(new ImageCreate(
    		() -> {
    			File source = Files.createTempFile("upload", ".tmp").toFile();
	            FileUtils.copyInputStreamToFile(stream, source);
    	        return source;
    		},
    		session.get().getUser().getId(),
    		header == null ? null : header.getFileName(),
			new ImageSettingsProvider.ImageSettingsConverter().fromString(settings)
		));
    }
    
	// Workaround for https://github.com/OAI/OpenAPI-Specification/issues/222
    @POST
    @Path("/m2m")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ "auth" })
    @Transactional
    @ApiOperation("Alternative for the POST /images method. Doesn't work from the Swagger-UI. Discussed here: https://github.com/OAI/OpenAPI-Specification/issues/222")
    public Image upload(
			@ApiParam(name = "file") @FormDataParam("file") InputStream stream,
			@FormDataParam("file") FormDataContentDisposition header,
			@ApiParam(example = SETTINGS_EXAMPLE) @FormDataParam("settings") IImageService.ImageSettings settings
    ) {

        return images.upload(new ImageCreate(
    		() -> {
    			File source = Files.createTempFile("upload", ".tmp").toFile();
	            FileUtils.copyInputStreamToFile(stream, source);
    	        return source;
    		},
    		session.get().getUser().getId(),
    		header == null ? null : header.getFileName(),
			settings
		));
    }
}
