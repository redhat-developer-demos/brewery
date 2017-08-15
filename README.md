
# Brewery

Ever wanted to brew your beer using microservices? This repository will allow you to do so!

This repository is used throughout the Spring Cloud libraries builds as end to end testing set up. Check
[Acceptance Tests Readme](acceptance-tests/README.md) for more information.

## How to build and deploy it 

https://redhat-developer-demos.github.io/brewery/ 


## Brewery Components

Here is the business flow of the app. Below you'll see more detailed explanation with numbers corresponding
to the numbers in the diagram

NOTE: This section will be updated and subject to changes

![Diagram](img/Brewery.png)

And here additional tech related applications:

![Diagram](img/Tech_apps.png)

And here how the flow of apps look like (screenshot taken from Zipkin)

![Diagram](img/Dependency_graph.png)

### Presenting service (point of entry to the system)

Here is the UI

![UI](img/Brewery_UI.png)

- Go to the presenting service (http://presenting-myproject.$(minishift ip).nip.io) and order ingredients **(1)**
- A request from the presenting service is sent to the aggregating service when order is placed **(2)**
- A "PROCESS-ID" header is set and will be passed through each part of beer brewing

### Brewing service

Brewing service contains the following functionalities:

#### Aggregating

- Service contains a warehouse ("database") where is stores the ingredients
- Basing on the order placed it will contact the Zuul proxy to fetch ingredients **(3)**
- Once the ingredients have been received an event is emitted **(7)**
- You have to have all 4 ingredients reach their threshold (1000) to start maturing the beer 
- Once the brewing has been started an event is emitted **(7)**
- Once the threshold is met the application sends a request to the maturing service **(8)**
- Each time a request is sent to the aggregating service it returns as a response its warehouse state

#### Maturing

- It receives a request with ingredients needed to brew a beer
- The brewing process starts thanks to the `Thread.sleep` method
- Once it's done an event is emitted **(9)** 
- And a request to the bottling service is sent with number of worts **(10)**
- Presenting service is called to update the current status of the beer brewing process

#### Bottling

- Waits some time to bottle the beer
- Once it's done an event is emitted **(11)** 
- Presenting service is called to update the current status of the beer brewing process **(12)**

### Ingredients Service

- Returns a fixed value of ingredients **(5)**

### Reporting Service

- Listens to events and stores them in the "database"

### Zuul proxy

- Proxy over the "adapters" to external world to fetch ingredients
- Routes all requests to the respective "ingredient adapter" **(4)**
- For simplicity we have one ingredient adapter called "ingredients" that returns a stubbed quantity
- Returns back the ingredients to the aggregating **(6)**

## Project structure

```
├── acceptance-tests (code containing acceptace-tests of brewery)
├── brewing          (service that creates beer - consists of aggregating, maturing, bottling functionalities)
├── common           (common code for the services)
├── docker           (docker scripts for additional apps - e.g. graphite)
├── config-server    (set up for the config server)
├── eureka           (Eureka server needed for Eureka tests)
├── git-props        (properties for config-server to pick)
├── gradle           (gradle related stuff)
├── img              (the fabulous diagram of the brewery)
├── ingredients      (service returns ingredients)
├── presenting       (UI of the brewery)
├── reporting        (service that listens to events)
├── zipkin-server    (Zipkin Server for Sleuth Stream tests)
├── zookeeper        (embedded zookeeper)
└── zuul             (Zuul proxy that forwards requests to ingredients)
```

## Authors

The code is ported from https://github.com/spring-cloud-samples/brewery and adapted for Kubernetes/OpenShift

The authors of the initial version of the code are:
- Marcin Grzejszczak (marcingrzejszczak)
- Tomasz Szymanski (szimano)
