# LearnSphereX Database

The application uses MySQL database `learnspherex_db`.

## Local MySQL

1. Run `01_create_database.sql`.
2. Set `DB_USERNAME` and `DB_PASSWORD` if your MySQL credentials are different.
3. Start the Spring Boot application.
4. With `spring.jpa.hibernate.ddl-auto=update`, Hibernate creates/updates the application tables from the JPA entities.

## Docker MySQL

From the project root:

```bash
docker compose up -d
```

The compose file creates the same `learnspherex_db` database with the default development credentials shown in the compose file.

The application remains responsible for creating/updating the tables.
