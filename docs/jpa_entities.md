# JPA Entity Architecture

## Overview

This project implements a standardized entity architecture that provides:
- **Consistent primary key handling** through abstract base classes
- **Optimistic locking** with version fields
- **Soft delete support** for logical deletion
- **Auditing integration** for tracking changes
- **Lombok integration** to reduce boilerplate code

All entity patterns implement the [`CommonEntity<ID extends Serializable>`](../src/main/java/com/example/common/entity/CommonEntity.java) interface, which provides a uniform way to access entity identifiers across the application.

## Entity Base Classes

### 1. [`AbstractAutoIncrementCommonEntity`](../src/main/java/com/example/common/entity/AbstractAutoIncrementCommonEntity.java)

**Purpose**: Base class for entities with auto-generated numeric primary keys.

**When to Use**: Most standard entities that don't require natural keys or composite keys.

**Key Features**:
- Auto-incrementing `Long` primary key using `IDENTITY` generation strategy
- Optimistic locking with `@Version` field
- Lombok annotations for boilerplate reduction
- Custom `equals()` and `hashCode()` based on ID only

**Implementation**:
```java
@MappedSuperclass
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Getter
@Setter
@EqualsAndHashCode(of = {"id"})
@ToString(exclude = {"version"})
public abstract class AbstractAutoIncrementCommonEntity implements CommonEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, updatable = false, nullable = false)
    private Long id;

    @Version
    @Column(name = "version")
    private Integer version;
}
```

**Key Points**:
- `@MappedSuperclass`: Indicates this is a base class for entities (not an entity itself)
- `@SuperBuilder`: Enables builder pattern for inheritance hierarchies
- `@EqualsAndHashCode(of = {"id"})`: Only uses ID for equality checks (important for JPA entities)
- `@ToString(exclude = {"version"})`: Prevents version field in toString output
- `updatable = false`: Prevents accidental ID modification

#### Example: `User` Entity

```java
@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Getter
@Setter
@Audited
public class User extends AbstractAutoIncrementCommonEntity implements DeletableEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "deleted", nullable = false)
    @Convert(converter = BooleanZeroOneConverter.class)
    @Builder.Default
    private boolean deleted = false;
}
```

#### Example: `Course` Entity with Auditing

```java
@Entity
@Table(name = "courses")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Audited
public class Course extends AbstractAutoIncrementCommonEntity {

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher", nullable = false)
    private User teacher;

    @CreatedBy
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @LastModifiedBy
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by", nullable = false)
    private User updatedBy;
}
```

### 2. [`AbstractCommonEntity<T extends Serializable>`](../src/main/java/com/example/common/entity/AbstractCommonEntity.java)

**Purpose**: Base class for entities with manually-assigned or natural primary keys.

**When to Use**: 
- Entities using natural keys (e.g., username, email, country code)
- Entities with non-numeric primary keys (e.g., String, UUID)
- Business keys that have meaning outside the system

**Key Features**:
- Generic type parameter for flexible primary key types
- Manual ID assignment (no auto-generation)
- Optimistic locking with `@Version` field initialized to 1
- Same Lombok benefits as `AbstractAutoIncrementCommonEntity`

**Implementation**:
```java
@MappedSuperclass
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Getter
@Setter
@EqualsAndHashCode(of = {"id"})
@ToString(exclude = {"version"})
public abstract class AbstractCommonEntity<T extends Serializable> implements CommonEntity<T> {

    @Id
    @Column(name = "id", unique = true, updatable = false, nullable = false)
    private T id;

    @Version
    @Column(name = "version")
    private Integer version = 1;
}
```

**Key Differences from Auto-Increment Version**:
- No `@GeneratedValue` annotation (manual assignment required)
- Generic type `T` allows any serializable type
- Version defaults to 1 (useful for tracking initial state)

#### Example: `Category` Entity

```java
@Entity
@Table(name = "categories")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Audited
public class Category extends AbstractCommonEntity<String> {

    @Column(name = "name", nullable = false)
    @NotBlank(message = "Name is required")
    private String name;
}
```

**Usage Pattern**:
```java
// ID must be set explicitly before saving
Category category = Category.builder()
    .id("TECH")  // Manually assigned natural key
    .name("Technology")
    .build();
categoryRepository.save(category);
```

### 3. Composite Primary Keys

**Purpose**: For entities that require multiple columns to uniquely identify a record.

**When to Use**:
- Many-to-many join tables with additional attributes
- Entities with natural composite keys (e.g., course + student enrollment)

**Implementation Requirements**:
1. Create an `@Embeddable` key class implementing `Serializable`
2. Entity implements `CommonEntity<CompositeKeyType>` directly
3. Use `@EmbeddedId` in the entity
4. Use `@MapsId` to link relationships to key components

#### Example: `EnrollmentId` (Composite Key Class)

```java
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentId implements Serializable {

    @Column(name = "course_id")
    private Long courseId;

    @Column(name = "student_id")
    private Long studentId;
}
```

**Key Points**:
- Must implement `Serializable` (required by JPA specification)
- Must override `equals()` and `hashCode()` (Lombok `@Data` handles this)
- All fields participate in equality checks

#### Example: `Enrollment` Entity

```java
@Entity
@Table(name = "enrollments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Audited
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

    @Column(name = "grade")
    @Enumerated(EnumType.STRING)
    private EnrollmentGrade grade;
}
```

**Key Annotations**:
- `@EmbeddedId`: Marks the composite key field
- `@MapsId`: Links relationship to a specific component of the composite key
- Prevents duplication of ID columns in both the key and the relationship

**Usage Pattern**:
```java
EnrollmentId id = new EnrollmentId(courseId, studentId);
Enrollment enrollment = Enrollment.builder()
    .id(id)
    .course(course)  // Also updates id.courseId via @MapsId
    .student(student)  // Also updates id.studentId via @MapsId
    .date(OffsetDateTime.now())
    .build();
enrollmentRepository.save(enrollment);
```

## Optimistic Locking

### How It Works

All base entity classes include a `version` field annotated with `@Version`. This enables optimistic locking to prevent lost updates in concurrent scenarios.

**Mechanism**:
1. When an entity is loaded, JPA reads the current version number
2. When saving, JPA includes the version in the WHERE clause
3. If the version changed (another transaction updated it), the update fails
4. JPA throws `OptimisticLockException` if the version doesn't match
5. On successful update, JPA automatically increments the version

**Example Scenario**:
```java
// Thread 1 loads user with version = 5
User user1 = userRepository.findById(1L).orElseThrow();

// Thread 2 loads same user with version = 5
User user2 = userRepository.findById(1L).orElseThrow();

// Thread 1 updates and saves (version becomes 6)
user1.setName("Alice");
userRepository.save(user1);  // Success

// Thread 2 tries to save (but expects version 5, finds 6)
user2.setName("Bob");
userRepository.save(user2);  // Throws OptimisticLockException
```

**Best Practices**:
- Don't include version in DTOs or manual updates
- Let JPA manage version field automatically
- Handle `OptimisticLockException` in service layer with retry logic or user feedback
- Exclude version from `toString()` (already done in base classes)

## Soft Deletes with `DeletableEntity`

### Overview

The [`DeletableEntity`](../src/main/java/com/example/common/entity/DeletableEntity.java) interface enables soft deletion - marking records as deleted without physically removing them from the database.

**Interface Definition**:
```java
public interface DeletableEntity {
    boolean isDeleted();
    void setDeleted(boolean deleted);
}
```

### Benefits

1. **Data Preservation**: Maintains historical data for auditing
2. **Referential Integrity**: Avoids foreign key constraint issues
3. **Undo Capability**: Deleted records can be restored
4. **Compliance**: Meets regulatory requirements for data retention
5. **Analytics**: Deleted records still available for reporting

### Implementation

#### Example: `User` Entity

```java
@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Getter
@Setter
@Audited
public class User extends AbstractAutoIncrementCommonEntity implements DeletableEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "deleted", nullable = false)
    @Convert(converter = BooleanZeroOneConverter.class)
    @Builder.Default
    private boolean deleted = false;
}
```

#### BooleanZeroOneConverter

Converts Java `boolean` to database `INTEGER` (0 or 1) for compatibility:

```java
@Converter
public class BooleanZeroOneConverter implements AttributeConverter<Boolean, Integer> {

    @Override
    public Integer convertToDatabaseColumn(Boolean attribute) {
        return Boolean.TRUE.equals(attribute) ? 1 : 0;
    }

    @Override
    public Boolean convertToEntityAttribute(Integer dbData) {
        return dbData != null && dbData.equals(1);
    }
}
```

**Why Use This Converter?**
- Some databases don't have native boolean type
- Provides consistent representation across different database vendors
- 0/1 is more readable than BIT or TINYINT representations

### Service Layer Integration

The `AbstractCommonService` automatically detects `DeletableEntity` and performs soft deletes:

```java
// In AbstractCommonService
public void deleteById(ID id) {
    ENTITY entity = findById(id);
    if (entity instanceof DeletableEntity deletableEntity) {
        deletableEntity.setDeleted(true);  // Soft delete
        repository.save(entity);
    } else {
        repository.deleteById(id);  // Hard delete
    }
}
```

**Querying Soft-Deleted Entities**:
```java
// Exclude deleted users
@Query("SELECT u FROM User u WHERE u.deleted = false")
List<User> findAllActive();

// Include deleted users
@Query("SELECT u FROM User u")
List<User> findAllIncludingDeleted();

// Only deleted users
@Query("SELECT u FROM User u WHERE u.deleted = true")
List<User> findAllDeleted();
```

## Lombok Integration

### Essential Annotations

#### `@SuperBuilder`
Enables builder pattern across inheritance hierarchies:

```java
User user = User.builder()
    .id(1L)           // From AbstractAutoIncrementCommonEntity
    .name("Alice")    // From User
    .deleted(false)   // From User
    .build();
```

#### `@EqualsAndHashCode(of = {"id"})`
**Critical for JPA entities**: Only uses ID field for equality checks.

**Why?**
- Collections (Set, Map) rely on equals/hashCode
- Lazy-loaded fields can cause issues if included
- ID uniquely identifies an entity
- Consistent behavior before and after persistence

**Incorrect Pattern** ❌:
```java
@EqualsAndHashCode  // Uses all fields - BAD for entities!
```

**Correct Pattern** ✅:
```java
@EqualsAndHashCode(of = {"id"})  // Only ID - GOOD!
```

#### `@ToString(exclude = {"version"})`
Prevents version field in string output (reduces noise).

Can also exclude lazy relationships to avoid initialization:
```java
@ToString(exclude = {"version", "courses", "enrollments"})
```

#### `@Builder.Default`
Provides default values when using builder:

```java
@Builder.Default
private boolean deleted = false;

// Works correctly in builder
User user = User.builder().name("Alice").build();  // deleted = false
```

### Annotation Combinations

**Recommended Pattern for Entities**:
```java
@Entity
@Table(name = "table_name")
@NoArgsConstructor        // Required by JPA
@AllArgsConstructor       // For @SuperBuilder
@SuperBuilder             // Builder pattern
@Getter                   // Getters for all fields
@Setter                   // Setters for all fields
public class MyEntity extends AbstractAutoIncrementCommonEntity {
    // fields...
}
```

**Alternative with `@Data`**:
```java
@Entity
@Data                     // @Getter + @Setter + @ToString + @EqualsAndHashCode
@Builder                  // Regular builder (no inheritance)
@NoArgsConstructor
@AllArgsConstructor
public class MyEntity implements CommonEntity<CompositeKey> {
    // Use for entities NOT extending base classes
}
```

## Relationship Best Practices

### Always Use `FetchType.LAZY`

```java
@ManyToOne(fetch = FetchType.LAZY)  // ✅ GOOD - Lazy loading
@JoinColumn(name = "category_id")
private Category category;

@ManyToOne  // ❌ BAD - Defaults to EAGER for @ManyToOne
@JoinColumn(name = "category_id")
private Category category;
```

**Why LAZY?**
- Prevents N+1 query problems
- Loads related entities only when accessed
- Better performance for large object graphs
- Use `@EntityGraph` or JOIN FETCH when you need eager loading

### Bidirectional Relationships

When implementing bidirectional relationships, consider:

```java
// Parent side
@OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
private Set<Enrollment> enrollments = new HashSet<>();

// Child side
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "course_id")
private Course course;
```

**Important**: 
- `mappedBy` indicates the owning side (child)
- Only the owning side's foreign key matters
- Consider excluding from `toString()` to avoid cycles
- Often better to avoid bidirectional relationships unless truly needed

## Auditing Support

### Hibernate Envers Integration

All domain entities use `@Audited` for automatic change tracking:

```java
@Entity
@Audited  // Creates _AUD table for history
public class Course extends AbstractAutoIncrementCommonEntity {
    // fields...
}
```

**Benefits**:
- Automatic history table creation
- Tracks all changes with timestamps
- Query historical state of entities
- Compliance and audit trail support

### Spring Data JPA Auditing

For tracking who created/modified entities:

```java
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Course extends AbstractAutoIncrementCommonEntity {

    @CreatedBy
    @ManyToOne(fetch = FetchType.LAZY)
    private User createdBy;

    @LastModifiedBy
    @ManyToOne(fetch = FetchType.LAZY)
    private User updatedBy;
}
```

**Setup Required**:
- Configure `@EnableJpaAuditing` in a configuration class
- Implement `AuditorAware<User>` to provide current user
- Annotations automatically populate on save

## Best Practices Summary

### 1. Choose the Right Base Class
- **Auto-increment Long ID**: `AbstractAutoIncrementCommonEntity`
- **Natural/Manual ID**: `AbstractCommonEntity<T>`
- **Composite Key**: Implement `CommonEntity<CompositeKeyType>` directly

### 2. Always Use Lombok Correctly
- `@SuperBuilder` for inheritance
- `@EqualsAndHashCode(of = {"id"})` for entities
- `@ToString(exclude = {"version", "lazyFields"})`
- `@Builder.Default` for default values

### 3. Lazy Load Everything
- Use `FetchType.LAZY` for all relationships
- Override with `@EntityGraph` when needed
- Avoid N+1 queries with proper fetch strategies

### 4. Leverage Optimistic Locking
- Don't modify version field manually
- Handle `OptimisticLockException` in service layer
- Exclude version from equals/hashCode/toString

### 5. Implement Soft Deletes When Appropriate
- Use `DeletableEntity` for user-facing entities
- Hard delete for temporary/cache data
- Always filter deleted records in queries

### 6. Use Auditing
- `@Audited` for change history (Envers)
- `@CreatedBy/@LastModifiedBy` for user tracking
- Combine with soft deletes for complete audit trail

### 7. Validate Constraints
- Use `nullable = false` at database level
- Add `@NotNull`, `@NotBlank` for validation
- Use `length` attribute for string columns

### 8. Name Consistency
- Table names: plural, snake_case (e.g., `course_enrollments`)
- Column names: snake_case (e.g., `created_by`)
- Entity names: singular, PascalCase (e.g., `CourseEnrollment`)

## Common Pitfalls

### ❌ Including All Fields in equals/hashCode
```java
@EqualsAndHashCode  // BAD - breaks with lazy loading
```

### ❌ Using EAGER Fetch
```java
@ManyToOne  // Defaults to EAGER - performance issue
```

### ❌ Modifying ID After Persistence
```java
user.setId(999L);  // BAD - ID should be immutable
```

### ❌ Forgetting NoArgsConstructor
```java
@Entity
@AllArgsConstructor  // BAD - JPA requires no-arg constructor
```

### ❌ Not Handling Composite Keys Correctly
```java
// BAD - separate foreign key and embedded ID
@EmbeddedId
private EnrollmentId id;

@JoinColumn(name = "course_id")  // Duplicates course_id!
private Course course;

// GOOD - use @MapsId
@MapsId("courseId")
@JoinColumn(name = "course_id")
private Course course;
```

## Summary

The entity architecture provides:

- **Three patterns** for primary keys: auto-increment, manual, and composite
- **Optimistic locking** via `@Version` for concurrency control
- **Soft delete support** through `DeletableEntity` interface
- **Auditing integration** with Envers and Spring Data JPA
- **Lombok integration** to minimize boilerplate code
- **Consistent practices** across all domain entities

By following these patterns, entities remain clean, maintainable, and integrate seamlessly with the service and mapper layers.
