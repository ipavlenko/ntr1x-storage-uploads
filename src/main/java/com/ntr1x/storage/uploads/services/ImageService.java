package com.ntr1x.storage.uploads.services;

import java.io.File;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.core.Response;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.ntr1x.storage.core.model.Image;
import com.ntr1x.storage.core.model.Resource;
import com.ntr1x.storage.core.model.ResourceImage;
import com.ntr1x.storage.core.reflection.ResourceUtils;
import com.ntr1x.storage.core.services.IFileService;
import com.ntr1x.storage.core.services.IScaleImageService;
import com.ntr1x.storage.security.model.User;
import com.ntr1x.storage.security.services.ISecurityService;
import com.ntr1x.storage.security.services.IUserService;
import com.ntr1x.storage.uploads.repository.ImageRepository;

@Service
public class ImageService implements IImageService {
    
    @Inject
    private EntityManager em;
    
    @Inject
    private ImageRepository images;
    
    @Inject
    private IUserService users;
    
    @Inject
    private IScaleImageService scale;
    
    @Inject
    private IFileService files;
    
    @Inject
    private ISecurityService security;
    
    @Override
    public Image upload(long scope, ImageCreate create) {
    	
    	Image image = new Image(); {
    		image.setScope(scope);
            image.setUuid(UUID.randomUUID());
            image.setOriginal(create.original);
        };
        
        User user = users.select(scope, create.user);
        
        em.persist(image);
        em.flush();
        
        security.register(image, ResourceUtils.alias(null, "images/i", image));
		security.grant(user, image.getAlias(), "admin");
        
        try {
            
            File dir = files.resolve(image.getUuid().toString()); {
                dir.mkdirs();
            }
        
            File source = create.file.call();
            
            for (ImageSettings.Item item : create.settings.items) {
                
                File target = new File(dir, String.format("%s.%s", item.name, item.format));
                
                ImageIO.write(
            		scale.scale(
                        ImageIO.read(source),
                        item.type,
                        item.width,
                        item.height
                    ),
                    item.format,
                    target
                );
            }
            
        } catch (Exception e) {
            
            throw new ServerErrorException(Response.Status.INTERNAL_SERVER_ERROR, e);
        }
        
        return image;
    }
    
    @Override
    public Image remove(Long scope, long id) {
    	
    	Image image = images.select(scope, id); {
    		
    		em.remove(image);
        	em.flush();
    	}
    	
    	return image;
    }
    
    @Override
    public Image select(Long scope, long id) {
    	return images.select(scope, id);
    }
    
    @Override
	public Image select(Long scope, UUID uuid) {
		return images.select(scope, uuid);
	}
    
    @Override
    public Page<Image> query(Long scope, String aspect, Pageable pageable) {
    	
    	return aspect != null && !aspect.isEmpty()
            ? images.query(aspect, pageable)
            : images.findAll(pageable)
	    ;
    }
    
    @Override
    public void createImages(Resource resource, RelatedImage[] images) {
        
        if (images != null) {
            
            for (RelatedImage p : images) {
                
                ResourceImage v = new ResourceImage(); {
                    
                    Image e = em.find(Image.class, p.image);
                    
                    v.setScope(resource.getScope());
                    v.setRelate(resource);
                    v.setImage(e);
                    
                    em.persist(v);
                }
            }
            
            em.flush();
        }
    }
    
    @Override
    public void updateImages(Resource resource, RelatedImage[] images) {
        
        if (images != null) {
            
            for (RelatedImage p : images) {
                
                switch (p.action) {
                
                    case CREATE: {
                        
                        ResourceImage v = new ResourceImage(); {
                            
                            Image e = select(resource.getScope(), p.image);
                    
                            v.setScope(resource.getScope());
                            v.setRelate(resource);
                            v.setImage(e);
                            
                            em.persist(v);
                        }
                        break;
                    }
                    case UPDATE: {
                        // ignore
                        break;
                    }
                    case REMOVE: {
                        
                        ResourceImage v = em.find(ResourceImage.class, p.id); {
                        	
                        	if (v.getRelate().getId() != resource.getId() || v.getRelate().getScope() != resource.getScope()) {
                        		throw new ForbiddenException("Image relates to another scope or resource");
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
