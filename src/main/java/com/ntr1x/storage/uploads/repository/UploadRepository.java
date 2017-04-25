package com.ntr1x.storage.uploads.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ntr1x.storage.core.model.Upload;

public interface UploadRepository extends JpaRepository<Upload, Long> {
    
    @Query(
        " SELECT DISTINCT i"
      + " FROM"
      + "   Upload i"
      + "   JOIN i.aspects a"
      + " WHERE (:scope IS NULL OR i.scope = :scope)"
      + "   AND (:aspect IS NULL OR a = :aspect)"
    )
    Page<Upload> query(
        @Param("scope") Long scope,
        @Param("aspect") String aspect,
        Pageable pageable
    );
    
    @Query(
        " SELECT DISTINCT i"
      + " FROM Upload i"
      + " WHERE (:scope IS NULL OR i.scope = :scope)"
    )
    Page<Upload> query(
        @Param("scope") Long scope,
        Pageable pageable
    );
    
    @Query(
        " SELECT i"
      + " FROM Upload i"
      + " WHERE (:scope IS NULL OR i.scope = :scope)"
      + "   AND i.id = :id"
    )
    Upload select(@Param("scope") Long scope, @Param("id") long id);
    
    @Query(
        " SELECT i"
      + " FROM Upload i"
      + " WHERE (:scope IS NULL OR i.scope = :scope)"
      + "   AND i.uuid = :uuid"
    )
    Upload select(@Param("scope") Long scope, @Param("uuid") UUID uuid);
}
