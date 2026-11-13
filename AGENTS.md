# ShopNest catalog service
- Run unit tests with: mvn -q test -Dtest='GroqPitchServiceTests,ProductInventoryServiceTests,DummyProductServiceTests,CategoryServiceTests'
- All service tests live in src/test/java/com/practice/productcatalogservice/service/ and are plain Mockito (no Spring context, no DB).
- Never run plain `mvn test`: `ProductCatalogServiceApplicationTests` needs MySQL.
- Controller -> IProductService -> repository. DTOs only in and out of controllers.
- Constructor injection only.
