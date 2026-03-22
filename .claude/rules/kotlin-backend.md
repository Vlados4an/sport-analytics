# Kotlin Backend Rules

## Database
- ALWAYS use JOOQ table definitions (`TABLE.COLUMN`). NEVER manual `DSL.field()`/`DSL.table()` with string names.
- DAO pattern: class with `DSLContext` or `JooqR2dbcContextFactory` injected
- Mappings: define `propertyToSqlMappings` mapping DTO properties to JOOQ fields
- JdbcMapper via SimpleFlatMapper for result set mapping

## Naming
- `GetXXX` = non-nullable, throws if not found. `FindXXX` = nullable
- Exceptions: domain-specific companion factories. NEVER `error("...")` or bare `throw Exception`

## DI
- Use `autoBind<T>()` for automatic constructor resolution
- Use `justInstance<T>()` to retrieve from container
- Group bindings in DI modules, import in container

## Formatting
- Blank line before `throw`/`return` (except after if/for/while/when)
- Blank line between distinct logical blocks
- Logging: `logger.error("msg {}", var, ex)`. NEVER string interpolation in logs
- Trailing commas in multi-line parameter lists

## Code Style
- Classes: PascalCase, Functions/variables: camelCase
- Constants: UPPERCASE_SNAKE_CASE
- Short functions (<20 lines), single-purpose
- Early returns to avoid nesting
- Prefer immutability (`val`, immutable collections)
- Follow SOLID, prefer composition over inheritance

## Testing
- Kotest FunSpec with descriptive test names
- MockK for mocking
- Integration tests preferred over heavy mocking
