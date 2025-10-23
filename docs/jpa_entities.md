# Entity Types

This project provides two abstract base classes for entities to standardize the handling of primary keys and optimistic locking. Both abstract entities implement the `CommonEntity<ID extends Serializable>` interface, which provides a common way to access the entity's identifier.

## `AbstractAutoIncrementCommonEntity`

This class is intended for entities that use an auto-incrementing `Long` as the primary key. It includes an `id` field annotated with `@GeneratedValue(strategy = GenerationType.IDENTITY)` and a `version` field for optimistic locking.

### Example: `Course` Entity

The `Course` entity uses an auto-incrementing ID, so it extends `AbstractAutoIncrementCommonEntity`.

```java
@Entity
@Table(name = "courses")
public class Course extends AbstractAutoIncrementCommonEntity {

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
}
```

## `AbstractCommonEntity<T extends Serializable>`

This generic class is for entities that have a non-numeric or manually assigned primary key. The type of the primary key is specified by the generic parameter `T`. It also includes a `version` field for optimistic locking.

### Example: `Category` Entity

The `Category` entity uses a `String` as its primary key (the category code), so it extends `AbstractCommonEntity<String>`.

```java
@Entity
@Table(name = "categories")
public class Category extends AbstractCommonEntity<String> {

    @Column(name = "name", nullable = false)
    private String name;

}
```

### Example: `Enrollment` Entity with Composite Primary Key

The `Enrollment` entity is an example of an entity with a composite primary key. It implements the `CommonEntity` interface directly, and uses the `@EmbeddedId` annotation to specify the composite key class.

The composite key class, `EnrollmentId`, must implement `Serializable` and be annotated with `@Embeddable`.

**`EnrollmentId.java`**
```java
@Embeddable
public class EnrollmentId implements Serializable {

    @Column(name = "course_id")
    private Long courseId;

    @Column(name = "student_id")
    private Long studentId;

}
```

**`Enrollment.java`**
```java
@Entity
@Table(name = "enrollments")
public class Enrollment implements CommonEntity<EnrollmentId> {

    @EmbeddedId
    private EnrollmentId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("courseId")
    @JoinColumn(name = "course_id")
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("studentId")
    @JoinColumn(name = "student_id")
    private User student;

    @Column(name = "date", nullable = false)
    private OffsetDateTime date;

}
```
