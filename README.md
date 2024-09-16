Microservices Level-1: Communication and Service Discovery
Introduction
This project demonstrates building microservices using Spring Boot. It covers essential microservices concepts like service communication, service discovery, and best practices for maintaining loosely coupled services. The project includes three Spring Boot applications:

Movie Catalog Service
Movie Info Service
Ratings Data Service
Each service is designed to perform a specific business function and communicate with other services via REST APIs.

Key Features
Service Discovery: Dynamic discovery of services using Netflix Eureka.
RestTemplate and WebClient: Demonstrates synchronous (RestTemplate) and asynchronous (WebClient) API calls.
API Design: Microservices interact with each other to provide a unified response to the client.
Circuit Breaker and Resilience: Ensures service reliability and prevents failures from cascading.
Example Scenario
Imagine an e-commerce platform where different microservices handle separate business functions:

User Service: Handles user authentication and profile management.
Product Service: Manages the product catalog.
Order Service: Handles order processing and payments.
Notification Service: Sends notifications to users.
Spring Boot Applications
The project includes the following services:

Movie Catalog Service: Provides a list of movies with their ratings.
Movie Info Service: Provides information about a specific movie.
Ratings Data Service: Provides movie ratings for users.
How to Create Spring Boot Applications
There are three ways to create Spring Boot applications:

Using Maven with required dependencies.
Using Spring CLI.
Using Spring Initializr.
How to Make REST Calls
RestTemplate: Used for synchronous REST API calls.
WebClient: Used for asynchronous, non-blocking API calls.
Example of a RestTemplate call:

java
Copy code
String response = restTemplate.getForObject(url, String.class);
Example of a WebClient call:

java
Copy code
Movie movie = webClientBuilder.build()
                .get()
                .uri("http://localhost:8081/movies/foo" + rating.getMovieId())
                .retrieve()
                .bodyToMono(Movie.class)
                .block();
Service Discovery
Service discovery is implemented using Netflix Eureka, which registers services and enables dynamic discovery without hardcoding URLs. This ensures scalability, load balancing, and resilience.

Eureka Components
Eureka Server: A service registry where all instances register themselves.
Eureka Client: Services that register with Eureka Server and query it to discover other services.
Running the Application
Start the Eureka Server.
Run all three Spring Boot applications.
Access the Movie Catalog Service to see the consolidated data from the other two services.
How Eureka Works
Service Registration: Microservices register with Eureka.
Service Renewal: Services send periodic heartbeats to Eureka.
Service Discovery: Services query Eureka to find other services.
Self-Preservation Mode: Ensures resilience by not removing services during transient network failures.
Conclusion
This project demonstrates a basic microservices architecture with Spring Boot, service discovery using Eureka, and communication via REST APIs. It's an excellent foundation for building scalable, resilient microservice-based applications.
