# Micro Services Level-2(Fault Tolerance and Resilience)

# Agenda

- Understanding challenges with availability in microservices.
- Take a sample project use spring boot and “Hystrix” and make these microservices more resilient and fault tolerant.

# What is Fault Tolerance?

 Given an application if there is a fault how much tolerance is there for it (the impact it makes).

**Fault tolerance** refers to the system's ability to continue operating correctly even in the presence of faults or failures. In microservices, this often means designing the system so that if one service fails, the overall application can still function, albeit possibly with reduced capabilities.

# What is Resilience?

How many faults can a system tolerate and bounce back from a fault indicates how resilient the system is. (System kind of correcting itself).

**Resilience** is the broader concept that encompasses fault tolerance. It refers to the system's ability to recover from and adapt to failures, maintaining acceptable levels of service. While fault tolerance focuses on preventing failure from impacting the system, resilience is about how the system reacts and recovers when failures do occur.

![Untitled](https://github.com/user-attachments/assets/37eb7356-c4d5-41fe-8823-c970dde4691b)

We now have linked it to the external API with the below code and result looks like the right side image.

```java
@RestController
@RequestMapping("/movies")
public class MovieResource {
    @Value("${api.key}")
    private String apiKey;
    @Autowired
    private RestTemplate restTemplate;

    @RequestMapping("/{movieId}")
    public Movie getMovieInfo(@PathVariable("movieId") String movieId){
        MovieSummary movieSummary=restTemplate.getForObject(
                "https://api.themoviedb.org/3/movie/"+movieId+"?api_key="+apiKey,
                MovieSummary.class
        );
        return new Movie(movieId, movieSummary.getTitle(), movieSummary.getOverview());
    }
}
```

![image](https://github.com/user-attachments/assets/6adb2b73-bf25-4fff-acee-2de4029261d7)

# How do we make this resilient?

The above thing is not fault tolerant. because we don’t have any error handling no try-catch block nothing. 

## Issues with Microservices

### **Scenario -1 :** **A microservice instance goes down.**

Example if rating-service goes down what do we do. 
**Solution:** Run multiple Service instances. 

### **Scenario-2 : A microservice instance is slow.**

(Its a much much bigger problem.) 

![image 1](https://github.com/user-attachments/assets/3f2ac065-7c8a-4d55-b605-036ea2c76b13)

### **How threads work in a web server(tomcat)?**

Let’s say we have a web server and a request comes in then the web server has to process the request to get back a response. It spins up a thread to handle it. Tomcat says I need to create a thread to handle this request once done thread goes away. So when a request come in thread is created and it takes sometime to process it and by the time the thread is done there are multiple requests comes in simultaneously. If the requests come in faster than the threads are getting freed up then your resources get consumed. Tomcat server has a max number of concurrent threads that’s allowed. If that happens all the resources are consumed and we can’t do anything else. That’s why it gets slowed down. 

![image 2](https://github.com/user-attachments/assets/31d5017c-e8da-4791-a4f0-18e1b70962a9)

In the context of microservices. This is a web server that handles request A and request B. In case of A when a request comes it frees up soon but since B is slow when a request of B comes in it stays there for long time, more requests of B comes and max limit is reached, now when a request of A comes it has to wait until all other guys are cleared.
**Example:** Movie-catalog-service has two calls movie-info-service(B) and ratings-data-service(A) . Based on the above scenario the reason for ratings-data-service being slow is clarified.

**How do we solve this?
Solution: Timeouts.** something is taking longtime we remove threads. 

**How do we set timeouts?**

Using Spring Rest Template. If somebody is not returning within this time period give me an error.(This is not the ideal there is one more way. We will learn both).

- **Using Rest Template**

```java
@Bean
	@LoadBalanced
	public RestTemplate getRestTemplate() {
		HttpComponentsClientHttpRequestFactory clientHttpRequestFactory=new HttpComponentsClientHttpRequestFactory();
		clientHttpRequestFactory.setConnectTimeout(3000);
		return new RestTemplate(clientHttpRequestFactory);
	}
```

**Does this solve the problem?**

It solves a problem little bit. But in the pace of time out like 3 seconds and 3 requests. so for 3 request coming in only 1 thread is being removed. So, it solves the issue but its a partly solved problem at this point. So we are moving to ideal solution.

**One solution is to make movie catalog service a bit smart. and note which is being slow and not send request for that service for a bit. After sometime it checks if it’s recovered and accordingly send requests. This is a popular pattern for fault tolerance in microservices.**

## Circuit Breaker Pattern

<aside>
💡 **Circuit breaker pattern**

- Detect something is wrong
- Take temporary steps to avoid the situation getting worse
- Deactivate the "problem" component so that it doesn't affect downstream components
</aside>

![image 3](https://github.com/user-attachments/assets/81008932-cf38-4364-90fe-356b81bb05d3)

**How can we apply this to our micro service?**

We can’t apply circuit breaker to every micro service that’s calling another microservice because when there is a call it can technically lead to consumption of resources. something else can be slow because of which its taking up all the resources and threads are all consumed. Specially when a microservice is calling two microservices for those cases its more important to have circuit breaker.

**What to do when a circuit breaks?**

So when a request comes in to movie-catalog-service it knows it shouldn’t send the request to movie-info-service so instead of sending a request it has to return something or send a message.

Before talking about it let’s know

**When a circuit breaks?**

At any point of time if the last 3 requests timeout will break the circuit.

But 1 success 1 time out then we don’t break it at all. So we need a solution for this. we need to know the parameters.

### Circuit breaker parameters

**When does the circuit trip?**

- Last n requests to consider for the decision.
- How many of those should fail?
- Timeout duration. (At what point of time we consider something is fail)

**When does the circuit un-trip? (when does it get back to normal)**

- How long after a circuit trip to try again?

### Example

Last n requests to consider for the decision: 5

How many of those should fail: 3

Timeout duration: 2s

How long to wait (sleep window): 10s

Let’s  say these are the requests to a microservice

100ms: success

3s: timeout after 2s

300ms : success

3s: timeout

4s: timeout

We got 3 failures so now the circuit is gonna sleep(hold on to send requests for that particular window for a span of 10s) for 10s.

**Circuit needs to be tripped. Now what? How do you handle the requests**

![image 4](https://github.com/user-attachments/assets/8963ab62-aee9-4350-8f0e-459c3f181b0f)

<aside>
💡 **The answer is We need to fallback.**

- Throw an error.(Not recommended unless absolutely required.)
- Return a fallback “default” response.(Better option).
- Save previous responses(cache) and use that when possible.(Best)

</aside>

**Why circuit breakers?**

- Failing fast is a good thing than to take time and fail.
- circuit breakers provide with fallback functionality.
- Automatic Recovery.

![image 5](https://github.com/user-attachments/assets/4743c566-f73a-4018-afed-677a41675fba)

To implement all for this we have a framework called **HYSTRIX.**

# Hystrix

What is Hystrix?

Its an open source library originally created by netflix. 

It implements circuit breaker pattern so you don’t have to do that.

Give it the configuration params and it does the work.(Parameters change over time its kind of trial and error it learns and adapts with time.)

Works well with spring boot.

**Definition:** The Hystrix framework library helps to control the interaction between services by providing fault tolerance and latency tolerance. It improves overall resilience of the system by isolating the failing services and stopping the cascading effect of failures.

## Adding Hystrix to a Spring Boot microservice

- Add the Maven spring-cloud-starter-netflix-hystrix dependency
- Add @EnableCircuitBreaker to the application class
- Add @HystrixCommand to methods that need circuit breakers
- Configure Hystrix behaviour.

### Step-1:  Add the Maven spring-cloud-starter-netflix-hystrix dependency

```xml
<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-starter-netflix-hystrix</artifactId>
			<version>1.4.7.RELEASE</version>
		</dependency>
```

### Step-2: Add @EnableCircuitBreaker to the application class

This is deprecated.

### Step 3: Add @HystrixCommand to methods that need circuit breakers

```java
@RestController
@RequestMapping("/catalog")
public class MovieCatalogResource {
    @Autowired
    private RestTemplate restTemplate;
		@Autowired
    private DiscoveryClient discoveryClient;

    @Autowired
    private WebClient.Builder webClientBuilder;
		@RequestMapping("/{userId}")
    @HystrixCommand
    public List<CatalogItem> getCatalog(@PathVariable("userId") String userId){

        UserRating ratings=restTemplate.getForObject("http://ratings-data-service/ratingsdata/users/" + userId, UserRating.class);
        return ratings.getUserRating().stream().map(rating -> {
                    // For each movie ID, call movie info service and get details // calling the movie-info service and getting the movie info
                    Movie movie=restTemplate.getForObject("http://movie-info-service/movies/"+rating.getMovieId(), Movie.class);
                    //put them all together
            return new CatalogItem(movie.getName(),  movie.getDescription(), rating.getRating());
        }).collect(Collectors.toList());
    }
}
```

### Step 4: Configure Hystrix behaviour

 we configure hystrix behaviour by using fallback method

```java

    @HystrixCommand(fallbackMethod = "getFallbackCatalog")
    public List<CatalogItem> getCatalog(@PathVariable("userId") String userId){
   }
   //This should have same method signature but with Fallbackmethod name
    public List<CatalogItem> getFallbackCatalog(@PathVariable("userId") String userId) {
    return Arrays.asList(new CatalogItem("No movie","",0));
    }
}
```

### How does this work?

![image 6](https://github.com/user-attachments/assets/1a86d4e9-552f-4d7f-8542-b578f7ad0f1a)

1. You have your API class and the method inside the API class which is annotated with @HystrixCommand.
2. Hystrix wraps your API class in a proxy class.
3. When you ask an instance of API class then the instance of the proxy class will be gotten.  
4. The proxy class contains the circuit breaker logics.
5. When somebody makes a call Hystrix is constantly monitoring that what is returning back.
6. Proxy class - > get a call and passing to the actual method in the API class and get the response back and examining make sure and returning back.
7. When things fail then the proxy class call fallback method until recovery back.

### Problem with Hystrix Proxy

So when we create fallback method and assume to run those whenever a service is down. It doesn’t actually run because of the proxy class.

The proxy class is a wrapper around the instance of the API class. Lets say we have a service and we mark it as a bean or RestController. When we have a spring bean which has a hystrix command hystrix is wrapping it in a proxy. whoever is holding on to the instance and make a call the proxy instance is the one which has control it thinks its calling the method on API class but its actually calling the method on proxy class and the proxy has ability to say the service is down and calls the fallback. But the control is already inside. One method in the class calling another method in the same class. 

The only way we can solve this problem is to take the method into another class. With this we are making API class call the method of another instance and we autowire to get hystrix proxy.

How do we refactor it.

Create new class.

**Configuring Hystrix parameters**

```java
@HystrixCommand(fallbackMethod ="getFallbackUserRating",
commandProperties = {
@HystrixProperty(name = "execution.isolation. thread. timeoutInMilliseconds", value = "2000"),
@HystrixProperty(name = "circuitBreaker. requestVolumeThreshold", value = "5"),
@HystrixProperty(name = "circuitBreaker.errorThresholdPercentage", value = "50"),
@HystrixProperty(name = "circuitBreaker.sleepWindowInMilliseconds", value = "5000")

}

public UserRating getUserRating(@PathVariable("userId") String userId) {
return restTemplate.getFor0bject("http://ratings-data-service/ratingsdata/user/" + userId, UserRating.class);

)
```

# Hystrix Dashboard

One of the main benefits of Hystrix is the set of metrics it gathers about each HystrixCommand. The Hystrix Dashboard displays the health of each circuit breaker in an efficient manner.

```xml
<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-starter-netflix-hystrix-dashboard</artifactId>
		</dependency>
```

```java
@EnableHystrixDashboard
@EnableHystrix
```

```xml
Application.properties
spring.application.name=movie-catalog-service
server.port=8082
management.endpoints.web.exposure.include=hystrix.stream
```

### Bulkhead pattern

The Bulkhead pattern is a type of application design that is tolerant of failure. In a bulkhead architecture, also known as cell-based architecture, elements of an application are isolated into pools so that if one fails, the others will continue to function. It's named after the sectioned partitions (bulkheads) of a ship's hull. If the hull of a ship is compromised, only the damaged section fills with water, which prevents the ship from sinking.

Seperate Thread pools

Configure bulkhead

```java

@HystrixCommand(
fallbackMethod = "getFallbackCatalogItem",
threadPoolKey = "movieInfoPool",
threadPoolProperties = {
@HystrixProperty(name = "coreSize", value = "20"),
@HystrixProperty(name = "maxQueueSize", value = "10"),

public CatalogItem getCatalogItem(Rating rating) {
Movie movie = restTemplate.getFor0bject("http://movie-info-service/movies/" + rating.getMovieId(), Movie.class);
return new CatalogItem(movie.getName(), movie.getDescription(), rating.getRating());
}
```

# Summary

- Understanding possible some causes for failure in microservices
- Threads and pools and impacts of slow microservices
- Timeouts and its limitations
- Circuit breaker pattern
- Hystrix concepts and implementation
- Bulkhead Pattern
