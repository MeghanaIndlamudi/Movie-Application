package io.javabrains.movie_catalog_service.resources;

import io.javabrains.movie_catalog_service.models.CatalogItem;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

//make it a rest application by adding rest controller annotation
@RestController
@RequestMapping("/catalog")
public class MovieCatalogResource {
    @RequestMapping("/{userId}")
    public List<CatalogItem> getCatalog(@PathVariable("userId") String userId){
return Collections.singletonList(
        new CatalogItem("Bahubali","part-1",5)
);
    }
}
