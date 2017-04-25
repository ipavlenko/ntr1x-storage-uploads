package com.ntr1x.storage.uploads.services;

import java.io.File;
import java.util.Arrays;
import java.util.UUID;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.core.Response;

import org.apache.commons.io.FileUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.ntr1x.storage.core.model.Resource;
import com.ntr1x.storage.core.model.ResourceUpload;
import com.ntr1x.storage.core.model.Upload;
import com.ntr1x.storage.core.reflection.ResourceUtils;
import com.ntr1x.storage.core.services.IFileService;
import com.ntr1x.storage.security.model.User;
import com.ntr1x.storage.security.services.ISecurityService;
import com.ntr1x.storage.security.services.IUserService;
import com.ntr1x.storage.uploads.repository.UploadRepository;

@Service
public class UploadService implements IUploadService {
    
    @Inject
    private EntityManager em;
    
    @Inject
    private UploadRepository uploads;
    
    @Inject
    private IUserService users;
    
    @Inject
    private IFileService files;
    
    @Inject
    private ISecurityService security;
    
    @Override
    public Upload upload(long scope, UploadCreate create) {
        
        Upload upload = new Upload(); {
            upload.setScope(scope);
            upload.setUuid(UUID.randomUUID());
            upload.setOriginal(create.original);
            upload.setAspects(create.settings.aspects == null
                ? null
                : Arrays.asList(create.settings.aspects)
            );
        }
        
        User user = users.select(scope, create.user);
        
        em.persist(upload);
        em.flush();
        
        security.register(upload, ResourceUtils.alias(null, "uploads/i", upload));
        security.grant(upload.getScope(), user, upload.getAlias(), "admin");
        
        try {
            
            File source = create.file.call();
            File target = files.resolve(upload.getUuid().toString());
            
            FileUtils.copyFile(source, target);
            
        } catch (Exception e) {
            
            throw new ServerErrorException(Response.Status.INTERNAL_SERVER_ERROR, e);
        }
        
        return upload;
    }
    
    @Override
    public Upload remove(Long scope, long id) {
        
        Upload upload = uploads.select(scope, id); {
            
            em.remove(upload);
            em.flush();
        }
        
        return upload;
    }
    
    @Override
    public Upload select(Long scope, long id) {
        return uploads.select(scope, id);
    }
    
    @Override
    public Upload select(Long scope, UUID uuid) {
        return uploads.select(scope, uuid);
    }
    
    @Override
    public Page<Upload> query(Long scope, String aspect, Pageable pageable) {
        
        return aspect != null && !aspect.isEmpty()
            ? uploads.query(scope, aspect, pageable)
            : uploads.query(scope, pageable)
        ;
    }
    
    @Override
    public void createUploads(Resource resource, RelatedUpload[] uploads) {
        
        if (uploads != null) {
            
            for (RelatedUpload p : uploads) {
                
                ResourceUpload v = new ResourceUpload(); {
                    
                    Upload e = em.find(Upload.class, p.upload);
                    
                    v.setScope(resource.getScope());
                    v.setRelate(resource);
                    v.setUpload(e);
                    
                    em.persist(v);
                }
            }
            
            em.flush();
        }
    }
    
    @Override
    public void updateUploads(Resource resource, RelatedUpload[] uploads) {
        
        if (uploads != null) {
            
            for (RelatedUpload p : uploads) {
                
                switch (p.action) {
                
                    case CREATE: {
                        
                        ResourceUpload v = new ResourceUpload(); {
                            
                            Upload e = select(resource.getScope(), p.upload);
                    
                            v.setScope(resource.getScope());
                            v.setRelate(resource);
                            v.setUpload(e);
                            
                            em.persist(v);
                        }
                        break;
                    }
                    case UPDATE: {
                        // ignore
                        break;
                    }
                    case REMOVE: {
                        
                        ResourceUpload v = em.find(ResourceUpload.class, p.id); {
                            
                            if (v.getRelate().getId() != resource.getId() || v.getRelate().getScope() != resource.getScope()) {
                                throw new ForbiddenException("Upload relates to another scope or resource");
                            }
                            em.remove(v);
                        }
                        break;
                    }
                default:
                    break;
                }
            }
            
            em.flush();
        }
    }
}
