# Common Mapper Pattern

## Overview

The `CommonMapper` interface provides a standardized abstraction for bidirectional mapping between JPA entities and DTOs. It leverages MapStruct to generate type-safe, performant mapping implementations at compile-time while reducing boilerplate code across the application.

## Architecture

### Core Interface

```java
public interface CommonMapper<ID extends Serializable, ENTITY extends CommonEntity<ID>, DTO extends CommonDto<ID>, PATCH_DTO>
```

The interface uses four generic type parameters:
- **ID**: The type of the entity's primary key (e.g., `Long`, `String`, `EnrollmentId`)
- **ENTITY**: The JPA entity class that implements `CommonEntity<ID>`
- **DTO**: The data transfer object that implements `CommonDto<ID>`
- **PATCH_DTO**: The DTO used for partial updates (use `Void` if not needed)

### Required Contracts

#### [`CommonEntity`](../src/main/java/com/example/common/entity/CommonEntity.java)
All entities must implement [`CommonEntity<ID>`](../src/main/java/com/example/common/entity/CommonEntity.java) which provides:
```java
ID getId();
void setId(ID id);
```

#### [`CommonDto`](../src/main/java/com/example/common/dto/CommonDto.java)
All DTOs must implement [`CommonDto<ID>`](../src/main/java/com/example/common/dto/CommonDto.java) which provides:
```java
ID getId();
void setId(ID id);
```

## Core Mapping Methods

### 1. Entity to DTO Mapping

```java
DTO map(ENTITY entity);
```

**Purpose**: Converts a JPA entity to a DTO for API responses.

**Direction**: `Entity → DTO`

**Use Case**: When returning data from the database to API clients.

**Example**:
```java
// In UserMapper
@Override
public abstract UserDto map(User user);

// Usage in service
User user = userRepository.findById(1L).orElseThrow();
UserDto dto = userMapper.map(user);
```

**Behavior**:
- Maps all fields from entity to DTO
- Handles nested entities by extracting their IDs
- Null entities return null DTOs
- Generated implementation by MapStruct

### 2. Full Update (PUT Operations)

```java
void update(DTO dto, @MappingTarget ENTITY entity);
```

**Purpose**: Updates an existing entity with all fields from a DTO.

**Direction**: `DTO → Entity (complete replacement)`

**Use Case**: HTTP PUT requests where all fields should be updated.

**Example**:
```java
// In CourseMapper
@Override
@Mapping(source = "categoryId", target = "category", qualifiedByName = "categoryIdToCategory")
@Mapping(source = "teacherId", target = "teacher", qualifiedByName = "userIdToUser")
public abstract void update(CourseDto courseDto, @MappingTarget Course course);

// Usage in service
Course course = courseRepository.findById(1L).orElseThrow();
courseMapper.update(courseDto, course);
courseRepository.save(course);
```

**Behavior**:
- Updates all mapped fields on the target entity
- Does NOT create a new entity (modifies existing)
- Handles relationship mapping (IDs to entity references)
- May ignore certain fields (e.g., `version`, audit fields)

### 3. Partial Update (PATCH Operations)

```java
void patch(PATCH_DTO patchDto, @MappingTarget ENTITY entity);
```

**Purpose**: Applies partial updates to an entity, modifying only provided fields.

**Direction**: `PatchDTO → Entity (selective update)`

**Use Case**: HTTP PATCH requests where only changed fields are sent.

**Example**:
```java
// In UserMapper with JsonNullable support
@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public void patch(UserPatchDto userPatchDto, @MappingTarget User user) {
    userPatchDto.getName().ifPresent(user::setName);
}

// Usage in service
User user = userRepository.findById(1L).orElseThrow();
userMapper.patch(userPatchDto, user);
userRepository.save(user);
```

**Behavior**:
- Only updates fields that are present in the patch DTO
- Uses [`JsonNullable<T>`](../src/main/java/com/example/common/mapper/JsonNullableMapper.java) to distinguish between null and absent fields
- `NullValuePropertyMappingStrategy.IGNORE` prevents null values from overwriting existing data
- If PATCH is not supported, use `Void` as the type parameter

## Advanced Features

### Reflection-based Entity Class Resolution

```java
default Class<ENTITY> getEntityClass() {
    ParameterizedType parameterizedType;
    if (this.getClass().getInterfaces().length > 0) {
        parameterizedType = (ParameterizedType) this.getClass().getInterfaces()[0].getGenericInterfaces()[0];
    } else {
        parameterizedType = (ParameterizedType) this.getClass().getSuperclass().getGenericInterfaces()[0];
    }
    return (Class<ENTITY>) parameterizedType.getActualTypeArguments()[1];
}
```

This utility method allows mappers to determine their entity class at runtime, useful for generic operations in services.

### Relationship Mapping with EntityManager

Mappers commonly need to convert IDs to entity references. This is done using named mapping methods:

```java
@Autowired
private EntityManager entityManager;

@Named("userIdToUser")
public User map(Long userId) {
    if (userId == null) {
        return null;
    }
    return entityManager.getReference(User.class, userId);
}
```

**Key Points**:
- `@Named` qualifier allows MapStruct to find this method for relationship mapping
- `entityManager.getReference()` creates a lazy-loaded proxy without hitting the database
- Used with `@Mapping(source = "userId", target = "user", qualifiedByName = "userIdToUser")`

## Implementation Examples

### Simple Mapper (No Relationships)

#### [`UserMapper`](../src/main/java/com/example/demo/mapper/UserMapper.java)

```java
@Mapper(componentModel = "spring", uses = JsonNullableMapper.class)
public abstract class UserMapper implements CommonMapper<Long, User, UserDto, UserPatchDto> {
    
    // map() method auto-generated by MapStruct
    // update() method auto-generated by MapStruct
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public void patch(UserPatchDto userPatchDto, @MappingTarget User user) {
        userPatchDto.getName().ifPresent(user::setName);
    }
}
```

#### [`CourseMapper`](../src/main/java/com/example/demo/mapper/CourseMapper.java)

### Complex Mapper (With Relationships)

```java
@Mapper(componentModel = "spring", uses = {JsonNullableMapper.class, UserMapper.class, CategoryMapper.class})
public abstract class CourseMapper implements CommonMapper<Long, Course, CourseDto, CoursePatchDto> {

    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private CategoryMapper categoryMapper;
    
    // Entity → DTO: Extract IDs from nested entities
    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "teacher.id", target = "teacherId")
    public abstract CourseDto map(Course course);

    // DTO → Entity: Convert IDs to entity references
    @Mapping(source = "categoryId", target = "category", qualifiedByName = "categoryIdToCategory")
    @Mapping(source = "teacherId", target = "teacher", qualifiedByName = "userIdToUser")
    public abstract void update(CourseDto courseDto, @MappingTarget Course course);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public void patch(CoursePatchDto coursePatchDto, @MappingTarget Course course) {
        coursePatchDto.getName().ifPresent(course::setName);
        coursePatchDto.getCategoryId().ifPresent(categoryId -> 
            course.setCategory(categoryMapper.map(categoryId))
        );
        coursePatchDto.getStartDate().ifPresent(course::setStartDate);
    }
}
#### [`EnrollmentMapper`](../src/main/java/com/example/demo/mapper/EnrollmentMapper.java)

```

### Composite Key Mapper

```java
@Mapper(componentModel = "spring", uses = {CourseMapper.class, UserMapper.class})
public interface EnrollmentMapper extends CommonMapper<EnrollmentId, Enrollment, EnrollmentDto, Void> {

    @Mapping(source = "id.courseId", target = "course", qualifiedByName = "courseIdToCourse")
    @Mapping(source = "id.studentId", target = "student", qualifiedByName = "userIdToUser")
    void update(EnrollmentDto enrollmentDto, @MappingTarget Enrollment enrollment);
}
```

## Benefits

### 1. **Type Safety**
- Compile-time validation of mapping configurations
- No runtime reflection overhead
- IDE support with autocomplete and refactoring

### 2. **Consistency**
- Standardized approach across all entity-DTO pairs
- Predictable behavior for map, update, and patch operations
- Common patterns for relationship handling

### 3. **Reduced Boilerplate**
- No manual getter/setter chains
- MapStruct generates implementation code
- Focus on business logic instead of mapping code

### 4. **Performance**
- Generated code is as fast as hand-written mapping
- No reflection at runtime
- Lazy loading with `EntityManager.getReference()`

### 5. **Maintainability**
- Single source of truth for mapping logic
- Easy to extend with custom mappings
- Clear separation of concerns

## Best Practices

### 1. Use `@MappingTarget` for Updates
Always use `@MappingTarget` to modify existing entities rather than creating new ones. This preserves JPA entity state and tracking.

### 2. Ignore Version Fields in Updates
```java
@Mapping(target = "version", ignore = true)
public abstract void update(CategoryDto dto, @MappingTarget Category entity);
```
JPA manages optimistic locking versions; don't update them manually.

### 3. Handle Null Relationships
Always null-check IDs before creating entity references:
```java
@Named("userIdToUser")
public User map(Long userId) {
    if (userId == null) {
### 4. Use [`JsonNullable`](../src/main/java/com/example/common/mapper/JsonNullableMapper.java) for PATCH
    }
    return entityManager.getReference(User.class, userId);
}
```

### 4. Use JsonNullable for PATCH
Distinguish between "not provided" and "set to null":
```java
patchDto.getName().ifPresent(user::setName);  // Only updates if present
```

### 5. Declare Mapper Dependencies
When mapping relationships, include other mappers in the `uses` attribute:
```java
@Mapper(componentModel = "spring", uses = {UserMapper.class, CategoryMapper.class})
```

### 6. Abstract Classes for Custom Logic
Use abstract classes instead of interfaces when you need to inject dependencies:
```java
public abstract class UserMapper implements CommonMapper<...> {
    @Autowired
    private EntityManager entityManager;
}
```

## Integration with Service Layer

Mappers integrate seamlessly with service classes:

```java
@Service
public class CourseService {
    @Autowired
    private CourseRepository courseRepository;
    
    @Autowired
    private CourseMapper courseMapper;
    
    public CourseDto getById(Long id) {
        Course course = courseRepository.findById(id).orElseThrow();
        return courseMapper.map(course);  // Entity → DTO
    }
    
    public CourseDto update(Long id, CourseDto dto) {
        Course course = courseRepository.findById(id).orElseThrow();
        courseMapper.update(dto, course);  // DTO → Entity
        Course saved = courseRepository.save(course);
        return courseMapper.map(saved);  // Entity → DTO
    }
    
    public CourseDto patch(Long id, CoursePatchDto patchDto) {
        Course course = courseRepository.findById(id).orElseThrow();
        courseMapper.patch(patchDto, course);  // PatchDTO → Entity
        Course saved = courseRepository.save(course);
        return courseMapper.map(saved);  // Entity → DTO
    }
}
```

## Troubleshooting

### MapStruct Not Generating Implementation
- Ensure annotation processing is enabled in your IDE
- Run `.\gradlew clean build` to regenerate mappers
- Check `build/generated/sources/annotationProcessor` for generated classes

### Circular Dependency Issues
- Use `@Lazy` annotation on mapper dependencies
- Consider restructuring relationships to avoid cycles

### Missing Mapping Methods
- Ensure the mapper interface/class is annotated with `@Mapper`
- Verify `componentModel = "spring"` is set
- Check that required mappers are listed in `uses` attribute

## Summary

The `CommonMapper` pattern provides a powerful, type-safe abstraction for entity-DTO conversion in both directions:

- **`map(entity)`**: Entity → DTO for reading data
- **`update(dto, entity)`**: Complete replacement for PUT operations
- **`patch(patchDto, entity)`**: Selective updates for PATCH operations

By standardizing this pattern across all domain entities, the application achieves consistency, maintainability, and performance while minimizing boilerplate code.

