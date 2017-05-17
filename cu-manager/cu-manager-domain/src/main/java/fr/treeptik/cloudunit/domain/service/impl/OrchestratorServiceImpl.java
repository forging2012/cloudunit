package fr.treeptik.cloudunit.domain.service.impl;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.client.Traverson;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestOperations;
import org.springframework.web.multipart.MultipartFile;

import fr.treeptik.cloudunit.domain.core.Image;
import fr.treeptik.cloudunit.domain.service.OrchestratorService;
import fr.treeptik.cloudunit.orchestrator.resource.ImageResource;

@Component
@ConfigurationProperties("cloudunit.orchestrator")
public class OrchestratorServiceImpl implements OrchestratorService, InitializingBean {
	private static final ParameterizedTypeReference<Resources<ImageResource>> IMAGE_RESOURCES_TYPE = new ParameterizedTypeReference<Resources<ImageResource>>() {};

	@Autowired
    private RestOperations rest;
	
	private Traverson t;

	private URI baseUri;

	public URI getBaseUri() {
		return baseUri;
	}

	public void setBaseUri(URI baseUri) {
		this.baseUri = baseUri;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		t = new Traverson(baseUri, MediaTypes.HAL_JSON);
	}

	@Override
	public List<Image> findAllImages() {
		Resources<ImageResource> images = t.follow("cu:images").toObject(IMAGE_RESOURCES_TYPE);

		return images.getContent().stream()
				.map(ir -> new Image(ir.getName(), ir.getServiceName(), ir.getType()))
				.collect(Collectors.toList());
	}

	@Override
	public Optional<Image> findImageByName(String imageName) {
		return findAllImages().stream()
				.filter(i -> i.getName().equals(imageName)).findFirst();
	}
	
	@Override
	public void deploy(String containerName, String contextPath, MultipartFile file) {
		// TODO
		String uri = "http://localhost:8081/containers/" + containerName + "/deploy/" + contextPath;
		MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
		try {
			map.add("file", new ByteArrayResource(file.getBytes()));
		} catch (IOException e) {
			// TODO
		}
		rest.put(uri, map);
	}
	
	@Override
	public void undeploy(String containerName, String contextPath) {
		// TODO
	}

}
