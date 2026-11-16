# ShopNest catalog service
- Run unit tests with: mvn -q test -Dtest='!ProductCatalogServiceApplicationTests'
- Tests live in src/test/java/com/practice/productcatalogservice/{service,controller}/. Service tests are plain Mockito; controller tests use the @WebMvcTest slice (MVC context, no DB, auth filter included).
- Never run plain `mvn test`: `ProductCatalogServiceApplicationTests` needs MySQL.
- Controller -> IProductService -> repository. DTOs only in and out of controllers.
- Constructor injection only.
