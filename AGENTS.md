# ShopNest catalog service
- Run unit tests with: mvn -q test -Dtest='!ProductCatalogServiceApplicationTests,!*IntegrationTests'
- Run integration tests manually (local only, never in CI/CD): set `$env:DB_PASSWORD='<local mysql root password>'` then `mvn -q test -Dtest='*IntegrationTests'` (needs local MySQL db `product_catalog_service_test` and profile `test`; see src/test/resources/application-test.properties)
- CI runs only the unit tests; the `*IntegrationTests` classes are excluded there.
- Tests live in src/test/java/com/practice/productcatalogservice/{service,controller}/. Service tests are plain Mockito; controller tests use the @WebMvcTest slice (MVC context, no DB, auth filter included).
- Never run plain `mvn test`: `ProductCatalogServiceApplicationTests` needs MySQL.
- Controller -> IProductService -> repository. DTOs only in and out of controllers.
- Constructor injection only.
