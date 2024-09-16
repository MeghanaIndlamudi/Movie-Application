![Untitled](https://github.com/user-attachments/assets/1e3296d9-2ce3-43c8-a202-996201f6a8fb)
![Untitled 6](https://github.com/user-attachments/assets/11ee4afc-43f4-43c6-8a15-a610578dd571)
![Untitled 5](https://github.com/user-attachments/assets/9642bc7f-d882-4d6f-9442-77e1497cffd6)
![Untitled 4](https://github.com/user-attachments/assets/01af9216-082d-496e-822f-32bd782e55a6)
![Untitled 3](https://github.com/user-attachments/assets/215d2bc5-6f32-45f9-ba60-59d35d4fdc7b)
![Untitled 2](https://github.com/user-attachments/assets/9159abae-5b8b-40d2-9d37-272a9b3ef052)
![Untitled 1](https://github.com/user-attachments/assets/2c92b912-726a-4c07-a576-20b8118ef12d)
# Micro Services Level-1 (Communication and Service Discovery)

# Introduction

Microservices architecture is an approach to software development where an application is composed of small, independent services that communicate over a network. Each service is designed to perform a specific business function and can be developed, deployed, and scaled independently.

With techniques like service discovery, circuit breakers, event-driven communication, and distributed tracing, developers can create applications that can handle high volumes and quickly adapt to changing demands.

> Each microservice is a Spring Boot application.
> 

## Example Scenario

<aside>
💡 Imagine you are developing an e-commerce platform. Instead of building a single monolithic application, you could create the following microservices:

- **User Service**: Handles user authentication and profile management.
- **Product Service**: Manages the product catalog.
- **Order Service**: Handles order processing and payment.
- **Inventory Service**: Manages stock levels and inventory updates.
- **Notification Service**: Sends emails and notifications to users.

Each of these services can be developed, deployed, and scaled independently, allowing you to manage complexity more effectively and adapt to changes quickly.

</aside>

## What is the key difference between monolithic and microservices?

![Untitled](Untitled.png)

---

## Key Characteristics of Microservices

- **Independence**: Each service can be developed, deployed, and scaled independently.
- **Single Responsibility**: Each service is responsible for a specific piece of business logic.
- **Communication**: Services communicate with each other using APIs, often over HTTP/HTTPS or messaging queues.
- **Decentralized Data Management**: Each microservice manages its own database, ensuring data is decoupled.
- **Technology Agnostic**: Different services can be written in different programming languages or use different technologies.

---

## When to Use Microservices?

- **Complex and Evolving Systems**: When building complex systems that need to evolve over time, microservices can provide the needed flexibility.
- **Need for Scalability**: When parts of the system have different scaling requirements.
- **Agile Development**: To support continuous integration and continuous deployment practices.
- **Independent Development**: When you want to allow multiple teams to work independently on different parts of the system.
- **Resilience Requirements**: When system uptime and fault tolerance are critical.

---

## Challenges of Microservices

- **Complexity**: Managing a distributed system can be more complex than a monolithic one.
- **Network Latency**: Communication between services over a network can introduce latency.
- **Data Consistency**: Ensuring data consistency across services can be challenging.
- **Deployment**: Managing the deployment of multiple services can require sophisticated DevOps practices.
- **Monitoring**: Requires robust monitoring and logging to track the health and performance of each service.

---

# Building an Application

![Create an API for this front-end application](Untitled%201.png)

Create an API for this front-end application

![This is our API and it return an object with id, name, description and user rating.](Untitled%202.png)

This is our API and it return an object with id, name, description and user rating.

![Untitled](Untitled%203.png)

- create 3 Spring Boot Projects
- Build movie catalog service API
- Build movie info service API
- Build ratings data service API
- Have movie catalog service call the other two services(the naive way)
- Implement a better way(Service Discovery)

## Create the 3 Spring Boot applications

<aside>
💡 Q. How do you create a spring boot application?

1.  Using Maven and adding the required dependencies.
2. Using Spring CLI
3. Using start.spring.io

</aside>

Spring Initializer extract 3 zip files(with web dependency)

- movie-info-service
- movie-catalog-service
- ratings-data-service

When you run Movie-catalog-service [localhost](http://localhost):8080 server is up and shows an error page which means I can’t even show an error.

Add an API to movie-catalog-service that returns a hard-coded list of movie + rating information.( we are creating a rest resources )

These are the 3 spring boot applications we created.

[GitHub - MeghanaIndlamudi/Movie-Application: Spring Boot Application](https://github.com/MeghanaIndlamudi/Movie-Application/tree/main)

## How to make rest call from your code?

how to make a call from movie-catalog API to movie-info service.

All the things that get transferred over the wire is a String. Make a request , get the response back and make an object out of it. This is called Rest.

Spring Boot comes with a client already built into it , already in your classpath - RestTemplate or Webclient(reactive programming (a bit harder programming in java which is asynchronous)).

<aside>
💡 In our case

1. `get all rated movie IDs`
2. `For each movie ID, call movie info service and get details`
3. `put them all together`

`// calling the movie-info service and getting the movie info` so for now we are hardcoding the bunch of responses from API to do that we need classes so we pasted the Rating class in movie-catalog service just instead of creating a new one.

</aside>

### Rest Template

resttemplate.getForobject()→ take any url makes a rest call to it and returns a string and can unmarshal into objects. 

> // Make a GET request and fetch the response as a String
        String response = restTemplate.getForObject(url, String.class);
> 

What Rest Template does is we can create a class that has similar properties as JSON rest template creates instance of class and populate those properties to it and gives you a fully formed object.

```java
return ratings.stream().map(rating-> {
            Movie movie=restTemplate.getForObject("http://localhost:8081/movies/foo"+rating.getMovieId(), Movie.class);
            return new CatalogItem(movie.getName(), "part-1", rating.getRating());
        })
        .collect(Collectors.toList());
```

Now let’s deviate a bit to see another way of doing this using Webclient a reactive way of doing this.

### WEB CLIENT

It is a part of reactive programming go make a call and then proceed like given a lambda execute it when you get the response until then proceed (passes a function through asynchronous call).

`import org.springframework.web.reactive.function.client.WebClient;`

**Definition:** Spring Web Client is a non-blocking and reactive web client to perform HTTP requests. It is also the replacement for the classic [**RestTemplate**](https://www.geeksforgeeks.org/spring-resttemplate/). It is a part of spring-webflux library and also offers support for both synchronous and asynchronous operations. The DefaultWebClient class implements this WebClient interface.

`WebClient.Builder` is not thread-safe.

Any time you wanna use Webclient in your application add a maven dependency like below before using it.

```xml
<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-webflux</artifactId>
		</dependency>
```

```java
//make APi call using WebClient
        return ratings.stream().map(rating-> {
            //Movie movie=restTemplate.getForObject("http://localhost:8081/movies/foo"+rating.getMovieId(), Movie.class);
            
            Movie movie=webClientBuilder.build()
                            .get()
                            .uri("http://localhost:8081/movies/foo"+rating.getMovieId())
                            .retrieve()
                            .bodyToMono(Movie.class)
                            .block();//makes it synchronous block execution until we recieve the result from bodytoMono
                    return new CatalogItem(movie.getName(), "part-1", rating.getRating());
        })
        .collect(Collectors.toList());
```

`List<Rating> ratings=restTemplate.getForObject("http://localhost:8083/ratingsdata/users/" + rating.getMovie(),ParameterizedTypeReference<ResponseWrapper<T>>(){});`

## So the final thing [In our case](https://www.notion.so/In-our-case-ba7b3570820d4ba28cedd108cbaac160?pvs=21)  is here:

```java
package io.javabrains.movie_catalog_service.resources;

import io.javabrains.movie_catalog_service.models.CatalogItem;
import io.javabrains.movie_catalog_service.models.Movie;
import io.javabrains.movie_catalog_service.models.Rating;
import io.javabrains.movie_catalog_service.models.UserRating;
import org.apache.catalina.filters.AddDefaultCharsetFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

//make it a rest application by adding rest controller annotation
@RestController
@RequestMapping("/catalog")
public class MovieCatalogResource {
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private WebClient.Builder webClientBuilder;
    @RequestMapping("/{userId}")
    public List<CatalogItem> getCatalog(@PathVariable("userId") String userId){
        //get all rated movie IDs
        UserRating ratings=restTemplate.getForObject("http://localhost:8083/ratingsdata/users/" + userId, UserRating.class);

        //make APi call using Rest template
						        return ratings.getUserRating().stream().map(rating -> {
        // For each movie ID, call movie info service and get details // calling the movie-info service and getting the movie info
                    Movie movie=restTemplate.getForObject("http://localhost:8081/movies/foo"+rating.getMovieId(), Movie.class);
       //put them all together
				            return new CatalogItem(movie.getName(), "part-1", rating.getRating());
        })
		               .collect(Collectors.toList());
    }
}

```

## What we did until now?

We created 3 services 1(movie-catalog-service) calling other 2(movie-info-service and rating-data-service).

### **What did we do wrong?**

Hard coded URLs.

### Why hard coded URLs are bad?

- Changes require code updates
- Dynamic URLs in the cloud
- Load balancing: Lets say we have a very popular microservice we make 2 or 3 of it(advantage of microservices) . If we continue with the hard code thing which URL are you gonna use. This is an issue.
- Multiple environments

Because of all these reasons we have a concept called service discovery.

# Service Discovery

Service discovery is a key concept in microservices architectures and distributed systems. It involves the automatic detection of devices and services on a network. 
Service discovery is used to dynamically discover and interact with different microservices in a distributed environment without hardcoding their locations (IP addresses and ports).

<aside>
💡 Spring Cloud uses client side service discovery

</aside>

![Untitled](Untitled%204.png)

Client-side service discovery.

← Step 1                       step 2  →

![Untitled](Untitled%205.png)

![Server side Service Discovery](Untitled%206.png)

Server side Service Discovery

### Types of Service Discovery:

1. **Client-Side Discovery**:
    - The service consumer is responsible for querying the service registry to find the service provider.
    - The client performs load balancing and chooses an appropriate instance to call.
    - Example: Netflix Eureka with Ribbon as the client-side load balancer.
2. **Server-Side Discovery**:
    - The client makes a request to a load balancer, which queries the service registry and forwards the request to an appropriate service instance.
    - The client is unaware of the actual service instances.
    - Example: AWS Elastic Load Balancer, Kubernetes Service.

# Technology: Eureka

Technology to implement service discovery that spring cloud uses and integrates with is Eureka(a commonly used.)

There are some other things like eureka that Netflix used and they made them open-source.

Eureka

Ribbon

Hysterix

Zuul

Steps to making this work

- Start up a Eureka server
- Have microservice register(publish) using Eureka client
- Have microservices locate(consume) using Eureka client.

Netflix Eureka is a service registry solution that is part of the Netflix OSS stack. It facilitates service discovery in microservices architectures by allowing services to register themselves and discover other services. Eureka provides the necessary infrastructure to dynamically locate services and ensure resilience and scalability in distributed systems.

### Key Components of Eureka:

1. **Eureka Server**:
    - Acts as a service registry where all the service instances register themselves.
    - Provides a REST API for service instances to register, renew leases, and deregister.
2. **Eureka Client**:
    - Any service that registers itself with the Eureka server and queries it for other registered services.
    - Typically implemented by microservices using the `spring-cloud-starter-netflix-eureka-client` dependency in Spring Cloud.

### How Eureka Works:

1. **Service Registration**:
    - Services (Eureka clients) register their instance information (hostname, IP address, port, status, etc.) with the Eureka server upon startup.
2. **Service Renewal**:
    - Eureka clients send periodic heartbeats to the Eureka server to renew their leases. If a client fails to renew its lease within a specified period, it is removed from the registry.
3. **Service Discovery**:
    - Eureka clients query the Eureka server to get a list of all registered service instances, enabling them to discover and communicate with other services.
4. **Self-Preservation**:
    - Eureka has a self-preservation mode to protect against transient network issues or client failures. During this mode, the Eureka server does not deregister instances that fail to send heartbeats for some time.

<aside>
💡 Every Eureka server is also a Eureka Client. Similar to microservices we can have multiple services of Eureka Server.

</aside>
