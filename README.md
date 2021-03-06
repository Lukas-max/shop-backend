# AuctioShop
## Backend part
Project by Łukasz Jankowski.

### Prerequisites
- Java 11
- Maven
- PostgreSQL database
### Build in:
- Spring Boot
- Spring Data JPA
- PostgreSQL
- Spring Security and JWT
- Spring Validation
- JUnit 5, Mockito, Hamcrest
- IntelliJ IDEA 2020.1 Ultimate Edition
### Other
- jjwt 0.9.1
- FlywayDb
- Test containers for postgreSql

This is the back end of the application. The other part -> [Shop - FrontEnd part!](https://github.com/Lukas-max/shop-frontend).
Data loading is done by Class implementing CommandLineRunner to postgreSQL database.

## RUN
To run this part of the app you need to:
- :ballot_box_with_check: Go to application.properties and set spring.datasource.initialization-mode to always. It will create your database columns in postgreSQL.
- :ballot_box_with_check: In application.properties set your spring.datasource.url to your databse. `Be aware that schema.sql was written to create tables for postgreSQL`
- :ballot_box_with_check: Go to bootdata package and uncomment @Component from LoadDatabase.class. LoadDatabase will load your database with data during application start.
- :ballot_box_with_check: Set other variables in properties shown below. Shop.admin.username and password are the values which admin user on startup will be created
- :ballot_box_with_check: Shop.token is the secret key for encrypting jwt.

```
spring.datasource.url=jdbc:postgresql://localhost:5432/[ your database name ]?useSSL=false&serverTimezone=UTC
spring.datasource.username=
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=none
spring.datasource.initialization-mode=never

Shop.admin.username=
Shop.admin.password=
Shop.token=
```

## RUN - ONLINE
The project is now available online on VPS: http://auctioshop.xyz/

### Features
- Loading data to database - products, categories and users (with one admin account).
- Spring security - basic and JSON Web Token auth (However the active implem. is now JWT auth)
- REST - providing endpoints for frontend app. 
 * Product categories endpoint  [ GET ]
 * Products endpoint [ GET, DELETE, POST, PUT ]
 * User endpoint. [ GET, DELETE, POST ]
 * CustomerOrder endpoint [ GET, DELETE, POST ]
- User registration and login.
- Global validation of passed data.
- Admin - adding, deleting, upgrading products.

## Endpoints
### Security 
| Method | URI | Action | Active |
|--------|-----|--------|--------|
| `POST` | `\user` | `JWT - generate Token` | `ON` |

### User Controller
| Method | URI | Action | Security |
|--------|-----|--------|----------|
| `GET` | `/api/users` | `Get a page of users` | `ADMIN` | 
| `GET` | `/api/users/{id}` | `Get a page of orders` | `ADMIN`, `USER` |
| `DELETE` | `/api/users/{id}` | `Delete user, users orders and address` | `ADMIN` |
| `POST` | `/api/users/register` | `Resgister user with  ROLE_USER` | `ALL` |

### Product Controller
Paging defaults refer to the values in front end. If change, change both sides of the app.
Defaults:
page: 0
size: 8
| Method | URI | Action | Security |
|--------|-----|--------|----------|
|  `GET` | `/api/products` | `Get page of products` | `ALL` |
| `GET` | `/api/products/product/{id}` | `Get product by its id` | `ALL` |
| `GET` | `/api/products/getByCategoryId` | `Get page of products by id` | `ALL` | 
| `GET` | `/api/products/name` | `Get page of products by name` | `ALL` |
| `POST` | `/api/products/` | `Add a new product` | `ADMIN` |
| `PUT` | `/api/products/` | `Update a product` | `ADMIN` |
| `DELETE` | `/api/products/{id}` | `Delete chosen product` | `ADMIN` |

### Product Category Controller
| Method | URI | Action | Security |
|--------|-----|--------|----------|
| `GET` | `/api/product_category` | `Get a list of product categories` | `ALL` |

### Order Controller
| Method | URI | Action | Security |
|--------|-----|--------|----------|
| `GET` | `/api/order` | `Get a page of orders with customer data` | `ADMIN` |
| `GET` | `/api/order/{id}` | `Get order by id. No customer data.` | `ALL` |
| `DELETE` | `/api/order/{id}` | `Delete Customer Order by it's id` | `ADMIN` |
| `POST` | `/api/order` | `Post order/ send order of purchase` | `ALL` |

## More info
More documentation is in the code.

## Tests
Provided unit tests for services. Integration tests for controllers with security testing of endpoints. And one integration test of ProductRepository, written with TestContainers for postgreSql. No auto configuration. No H2 database testing.

```java
    @Test
    @WithMockUser(authorities = "ADMIN")
    void getAllADMIN() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        given(orderService.getAllPageable(pageable))
                .willReturn(OrderTestUtils.getPageOfOrders());

        mockMvc.perform(get("/api/order?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()", is(2)))
                .andExpect(jsonPath("$.content[0].cartItems[0].name", is("God of War 4")))
                .andExpect(jsonPath("$.content[0].totalPrice", is(49.99)))
                .andExpect(jsonPath("$.content[0].user.username", is("Wojtek")))
                .andExpect(jsonPath("$.content[0].customer.lastName", is("Czarek")));

        then(orderService).should(times(1)).getAllPageable(pageable);
    }
 ```  
 Repository testing in a container:  
 ```java
 @DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProductRepositoryTest {

    @Container
    static PostgreSQLContainer database = new PostgreSQLContainer("postgres:11")
            .withDatabaseName("spring")
            .withPassword("spring")
            .withUsername("spring");

    @DynamicPropertySource
    static void getSourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", database::getJdbcUrl);
        registry.add("spring.datasource.username", database::getUsername);
        registry.add("spring.datasource.password", database::getPassword);
    }
 ```

## Controllers
### Product Controller
Mostly it does just what the table above says. Just when posting new product or updating it we validate the ProductRequest and then using the service we map it to Product class 
and persist it. When adding a product without image the application will set a standard 404 not found image. If an image is sent, it's sent in base64 data. Using new FileReader().readAsDataURL. So we split the String at the ','  and decode the second part.
Also in the repository we have our own method to persist an object without persisting it's image. That happens when we are updating a product without sending a new image file.

### Product Category Controller
This is used to dynamically populate the categories in sidebar-menu, and HTML option tag. (Like when adding a product or updating it). 
  
### Order Controller
Has a method to get order by id, get all orders, delete order by order id, or persist a purchase order. Get all orders is only for administrator, cause it returns page of orders with customer address and credentials. Get order by id is for the client. Also delete option is only for admin.

When posting the order we are doing validation of the order. It's mostly equal to front end validation, plus cors config should make that no bad data would be transferred.
The service classes map dto objects to Customer and CustomerOrder class. The number of items bought are being decrement if the stock has ran to low and total price and quantity is recounted. Then the database stock is decremented. If the objects drops to 0 in stock it's set to non active.
Also if we ran out of the items that a client wants to purchase, we send him a ResponseException that we ran out of products.

### User Controller
User register, deleting users and fetching user data.
The user data validation is on the front end side and here. Before adding a user we check if the user has set id or the username and email are in the database. If so we throw a exception that is visible on the front end side.

### Exception handling
Localy we use ResponseStatusException, and besides that for validation we have our own custom ResponseEntityExceptionHandler, that overrides handleMethodArgumentNotValid with custom exception message. And OrderNotFound exception when asking for order that does not yet exist.

### Security
JwtAuthenticationController takes the credentials through AuthenticationRequest object and authenticates against Springs Authentication Manager. We have our own custom implementation of UserDetailsService so it validates data against our own database and if correct sends UserDetails. We then again loadTheUserByUsername and by it we create Json web token which we send to the client with some other added data for now.

