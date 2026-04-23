# Design Document - Controller APP

## Overview

Controller APP is an Android application built with Clean Architecture principles, implementing a three-layer architecture (Domain, Data, Presentation) with unidirectional data flow using MVI/MVVM pattern. The application provides version control validation, secure authentication, local data synchronization, and locality management.

The design emphasizes:
- Clear separation of concerns across architectural layers
- Reactive programming with Coroutines and Flow
- Dependency injection with Hilt
- Local persistence with Room
- Network communication with Retrofit
- Comprehensive error handling
- Testability through interface-based design

## Architecture

### Layer Structure

```
┌─────────────────────────────────────────┐
│         Presentation Layer              │
│  (Jetpack Compose UI + ViewModels)      │
└─────────────────────────────────────────┘
                  ↓ ↑
┌─────────────────────────────────────────┐
│           Domain Layer                  │
│    (Use Cases + Repository Interfaces)  │
└─────────────────────────────────────────┘
                  ↓ ↑
┌─────────────────────────────────────────┐
│            Data Layer                   │
│  (Repository Impl + API + Database)     │
└─────────────────────────────────────────┘
```

### Data Flow Pattern

The application uses unidirectional data flow:
1. UI emits user intents/actions
2. ViewModel processes intents through use cases
3. Use cases execute business logic via repositories
4. Repositories fetch/store data from API or database
5. Results flow back through layers as state updates
6. UI observes state and renders accordingly

### Technology Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose
- **Navigation**: Navigation Compose (Jetpack Navigation 3) with Serializable routes
- **DI**: Hilt
- **Database**: Room
- **Networking**: Retrofit + OkHttp
- **Async**: Coroutines + Flow
- **Architecture**: Clean Architecture with MVI/MVVM
- **Serialization**: Kotlinx Serialization for navigation arguments

## Components and Interfaces

### Domain Layer

#### Repository Interfaces

```kotlin
interface VersionRepository {
    fun getCurrentVersion(): Flow<Result<String>>
}

interface AuthRepository {
    fun authenticateUser(
        usuario: String,
        password: String,
        mac: String
    ): Flow<Result<AuthResponse>>
    
    fun saveUserSession(user: UserSession): Flow<Result<Unit>>
    fun getUserSession(): Flow<Result<UserSession?>>
    fun clearUserSession(): Flow<Result<Unit>>
}

interface DataSyncRepository {
    fun fetchDatabaseSchema(): Flow<Result<List<TableSchema>>>
    fun syncTables(schemas: List<TableSchema>): Flow<Result<Unit>>
    fun getAllTables(): Flow<Result<List<TableInfo>>>
    fun getTableData(tableName: String): Flow<Result<List<Map<String, Any>>>>
}

interface LocalitiesRepository {
    fun fetchLocalities(): Flow<Result<List<Locality>>>
}
```

#### Use Cases

```kotlin
class CheckVersionUseCase(
    private val versionRepository: VersionRepository,
    private val appVersion: String
) {
    operator fun invoke(): Flow<Result<VersionStatus>>
}

class LoginUserUseCase(
    private val authRepository: AuthRepository
) {
    operator fun invoke(
        usuario: String,
        password: String,
        mac: String
    ): Flow<Result<UserSession>>
}

class SyncDatabaseUseCase(
    private val dataSyncRepository: DataSyncRepository
) {
    operator fun invoke(): Flow<Result<SyncResult>>
}

class GetUserSessionUseCase(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Flow<Result<UserSession?>>
}

class GetTablesUseCase(
    private val dataSyncRepository: DataSyncRepository
) {
    operator fun invoke(): Flow<Result<List<TableInfo>>>
}

class GetTableDataUseCase(
    private val dataSyncRepository: DataSyncRepository
) {
    operator fun invoke(tableName: String): Flow<Result<List<Map<String, Any>>>>
}

class GetLocalitiesUseCase(
    private val localitiesRepository: LocalitiesRepository
) {
    operator fun invoke(): Flow<Result<List<Locality>>>
}

class LogoutUseCase(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Flow<Result<Unit>>
}
```

### Data Layer

#### API Service Interfaces

```kotlin
interface VersionApiService {
    @GET("api/ParametrosFramework/ConsultarParametrosFramework/VPStoreAppControl")
    suspend fun getCurrentVersion(): Response<VersionResponse>
}

interface AuthApiService {
    @POST("api/Seguridad/AuthenticaUsuarioApp")
    suspend fun authenticateUser(
        @Header("Usuario") usuario: String,
        @Header("Identificacion") identificacion: String,
        @Header("Accept") accept: String,
        @Header("IdUsuario") idUsuario: String,
        @Header("IdCentroServicio") idCentroServicio: String,
        @Header("NombreCentroServicio") nombreCentroServicio: String,
        @Header("IdAplicativoOrigen") idAplicativoOrigen: String,
        @Header("Content-Type") contentType: String,
        @Body request: AuthRequest
    ): Response<AuthResponse>
}

interface DataSyncApiService {
    @GET("api/SincronizadorDatos/ObtenerEsquema/true")
    suspend fun getDatabaseSchema(
        @Header("usuario") usuario: String
    ): Response<SchemaResponse>
}

interface LocalitiesApiService {
    @GET("api/ParametrosFramework/ObtenerLocalidadesRecogidas")
    suspend fun getLocalities(): Response<LocalitiesResponse>
}
```

#### Repository Implementations

```kotlin
class VersionRepositoryImpl(
    private val apiService: VersionApiService,
    private val networkHandler: NetworkHandler
) : VersionRepository {
    override fun getCurrentVersion(): Flow<Result<String>> = flow {
        emit(networkHandler.safeApiCall { apiService.getCurrentVersion() })
    }
}

class AuthRepositoryImpl(
    private val apiService: AuthApiService,
    private val userDao: UserDao,
    private val networkHandler: NetworkHandler
) : AuthRepository {
    override fun authenticateUser(
        usuario: String,
        password: String,
        mac: String
    ): Flow<Result<AuthResponse>> = flow {
        emit(networkHandler.safeApiCall { 
            apiService.authenticateUser(/* headers and body */) 
        })
    }
    
    override fun saveUserSession(user: UserSession): Flow<Result<Unit>> = flow {
        try {
            userDao.insertUser(user.toEntity())
            emit(Result.Success(Unit))
        } catch (e: Exception) {
            emit(Result.Error(DatabaseError("Failed to save session", e)))
        }
    }
    
    override fun getUserSession(): Flow<Result<UserSession?>> = flow {
        try {
            val user = userDao.getUser()
            emit(Result.Success(user?.toDomain()))
        } catch (e: Exception) {
            emit(Result.Error(DatabaseError("Failed to get session", e)))
        }
    }
    
    override fun clearUserSession(): Flow<Result<Unit>> = flow {
        try {
            userDao.clearUsers()
            emit(Result.Success(Unit))
        } catch (e: Exception) {
            emit(Result.Error(DatabaseError("Failed to clear session", e)))
        }
    }
}

class DataSyncRepositoryImpl(
    private val apiService: DataSyncApiService,
    private val database: AppDatabase,
    private val networkHandler: NetworkHandler
) : DataSyncRepository {
    override fun fetchDatabaseSchema(): Flow<Result<List<TableSchema>>> = flow {
        emit(networkHandler.safeApiCall { apiService.getDatabaseSchema("usuario") })
    }
    
    override fun syncTables(schemas: List<TableSchema>): Flow<Result<Unit>> = flow {
        try {
            // Sync logic
            emit(Result.Success(Unit))
        } catch (e: Exception) {
            emit(Result.Error(DatabaseError("Failed to sync tables", e)))
        }
    }
    
    override fun getAllTables(): Flow<Result<List<TableInfo>>> = flow {
        try {
            // Get tables logic
            emit(Result.Success(emptyList()))
        } catch (e: Exception) {
            emit(Result.Error(DatabaseError("Failed to get tables", e)))
        }
    }
    
    override fun getTableData(tableName: String): Flow<Result<List<Map<String, Any>>>> = flow {
        try {
            // Get table data logic
            emit(Result.Success(emptyList()))
        } catch (e: Exception) {
            emit(Result.Error(DatabaseError("Failed to get table data", e)))
        }
    }
}

class LocalitiesRepositoryImpl(
    private val apiService: LocalitiesApiService,
    private val networkHandler: NetworkHandler
) : LocalitiesRepository {
    override fun fetchLocalities(): Flow<Result<List<Locality>>> = flow {
        emit(networkHandler.safeApiCall { apiService.getLocalities() })
    }
}
```

#### Network Handler

```kotlin
class NetworkHandler(
    private val context: Context
) {
    fun isNetworkAvailable(): Boolean
    
    suspend fun <T> safeApiCall(
        apiCall: suspend () -> Response<T>
    ): Result<T>
}
```

### Database Layer

#### Room Database

```kotlin
@Database(
    entities = [
        UserEntity::class,
        DynamicTableEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun dynamicTableDao(): DynamicTableDao
}
```

#### DAOs

```kotlin
@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)
    
    @Query("SELECT * FROM users LIMIT 1")
    suspend fun getUser(): UserEntity?
    
    @Query("DELETE FROM users")
    suspend fun clearUsers()
}

@Dao
interface DynamicTableDao {
    suspend fun createTable(tableName: String, columns: List<ColumnDefinition>)
    suspend fun insertData(tableName: String, data: Map<String, Any>)
    suspend fun getAllTables(): List<String>
    suspend fun getTableData(tableName: String): List<Map<String, Any>>
    suspend fun dropTable(tableName: String)
}
```

### Presentation Layer

#### Navigation Routes

```kotlin
@Serializable
object SplashRoute

@Serializable
object LoginRoute

@Serializable
object HomeRoute

@Serializable
object TablesRoute

@Serializable
data class TableDetailRoute(val tableName: String)

@Serializable
object LocalitiesRoute
```

#### Navigation Graph

```kotlin
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = SplashRoute
    ) {
        composable<SplashRoute> {
            SplashScreen(
                onNavigateToLogin = { navController.navigate(LoginRoute) },
                onNavigateToHome = { navController.navigate(HomeRoute) }
            )
        }
        
        composable<LoginRoute> {
            LoginScreen(
                onLoginSuccess = { 
                    navController.navigate(HomeRoute) {
                        popUpTo<SplashRoute> { inclusive = true }
                    }
                }
            )
        }
        
        composable<HomeRoute> {
            HomeScreen(
                onNavigateToTables = { navController.navigate(TablesRoute) },
                onNavigateToLocalities = { navController.navigate(LocalitiesRoute) },
                onLogout = {
                    navController.navigate(LoginRoute) {
                        popUpTo<SplashRoute> { inclusive = true }
                    }
                }
            )
        }
        
        composable<TablesRoute> {
            TablesScreen(
                onNavigateBack = { navController.popBackStack() },
                onTableSelected = { tableName ->
                    navController.navigate(TableDetailRoute(tableName))
                }
            )
        }
        
        composable<TableDetailRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<TableDetailRoute>()
            TableDetailScreen(
                tableName = route.tableName,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable<LocalitiesRoute> {
            LocalitiesScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
```

#### ViewModels

```kotlin
class SplashViewModel(
    private val checkVersionUseCase: CheckVersionUseCase,
    private val getUserSessionUseCase: GetUserSessionUseCase
) : ViewModel() {
    private val _state = MutableStateFlow<SplashState>(SplashState.Loading)
    val state: StateFlow<SplashState> = _state.asStateFlow()
    
    fun checkVersionAndSession()
}

class LoginViewModel(
    private val loginUserUseCase: LoginUserUseCase
) : ViewModel() {
    private val _state = MutableStateFlow<LoginState>(LoginState.Idle)
    val state: StateFlow<LoginState> = _state.asStateFlow()
    
    fun login(usuario: String, password: String)
}

class HomeViewModel(
    private val getUserSessionUseCase: GetUserSessionUseCase,
    private val syncDatabaseUseCase: SyncDatabaseUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {
    private val _state = MutableStateFlow<HomeState>(HomeState.Loading)
    val state: StateFlow<HomeState> = _state.asStateFlow()
    
    fun loadUserData()
    fun syncDatabase()
    fun logout()
}

class TablesViewModel(
    private val getTablesUseCase: GetTablesUseCase,
    private val getTableDataUseCase: GetTableDataUseCase
) : ViewModel() {
    private val _state = MutableStateFlow<TablesState>(TablesState.Loading)
    val state: StateFlow<TablesState> = _state.asStateFlow()
    
    fun loadTables()
    fun loadTableData(tableName: String)
}

class LocalitiesViewModel(
    private val getLocalitiesUseCase: GetLocalitiesUseCase
) : ViewModel() {
    private val _state = MutableStateFlow<LocalitiesState>(LocalitiesState.Loading)
    val state: StateFlow<LocalitiesState> = _state.asStateFlow()
    
    fun loadLocalities()
}
```

#### UI States

```kotlin
sealed class SplashState {
    object Loading : SplashState()
    data class VersionMismatch(val message: String) : SplashState()
    object NavigateToLogin : SplashState()
    object NavigateToHome : SplashState()
    data class Error(val message: String) : SplashState()
}

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    object Success : LoginState()
    data class Error(val message: String) : LoginState()
}

sealed class HomeState {
    object Loading : HomeState()
    data class Success(val userSession: UserSession, val syncStatus: SyncStatus) : HomeState()
    data class Error(val message: String) : HomeState()
}

sealed class TablesState {
    object Loading : TablesState()
    data class TablesList(val tables: List<TableInfo>) : TablesState()
    data class TableData(val tableName: String, val data: List<Map<String, Any>>) : TablesState()
    data class Error(val message: String) : TablesState()
}

sealed class LocalitiesState {
    object Loading : LocalitiesState()
    data class Success(val localities: List<Locality>) : LocalitiesState()
    data class Error(val message: String) : LocalitiesState()
}
```

#### Composable Screens

```kotlin
@Composable
fun SplashScreen(
    viewModel: SplashViewModel = hiltViewModel(),
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    
    LaunchedEffect(Unit) {
        viewModel.checkVersionAndSession()
    }
    
    LaunchedEffect(state) {
        when (state) {
            is SplashState.NavigateToLogin -> onNavigateToLogin()
            is SplashState.NavigateToHome -> onNavigateToHome()
            else -> { /* Stay on splash */ }
        }
    }
    
    // Splash UI with version check status
}

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    
    LaunchedEffect(state) {
        if (state is LoginState.Success) {
            onLoginSuccess()
        }
    }
    
    // Login UI
}

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToTables: () -> Unit,
    onNavigateToLocalities: () -> Unit,
    onLogout: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    
    LaunchedEffect(Unit) {
        viewModel.loadUserData()
    }
    
    // Home UI with user info and navigation buttons
}

@Composable
fun TablesScreen(
    viewModel: TablesViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onTableSelected: (String) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    
    LaunchedEffect(Unit) {
        viewModel.loadTables()
    }
    
    // Tables list UI
}

@Composable
fun TableDetailScreen(
    tableName: String,
    viewModel: TablesViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    
    LaunchedEffect(tableName) {
        viewModel.loadTableData(tableName)
    }
    
    // Table data display UI
}

@Composable
fun LocalitiesScreen(
    viewModel: LocalitiesViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    
    LaunchedEffect(Unit) {
        viewModel.loadLocalities()
    }
    
    // Localities list UI
}
```

## Data Models

### Domain Models

```kotlin
data class VersionStatus(
    val localVersion: String,
    val apiVersion: String,
    val status: VersionComparisonStatus
)

enum class VersionComparisonStatus {
    UP_TO_DATE,
    UPDATE_NEEDED,
    AHEAD_OF_SERVER
}

data class UserSession(
    val usuario: String,
    val identificacion: String,
    val nombre: String
)

data class AuthResponse(
    val usuario: String,
    val identificacion: String,
    val nombre: String,
    val token: String? = null
)

data class TableSchema(
    val tableName: String,
    val columns: List<ColumnDefinition>
)

data class ColumnDefinition(
    val name: String,
    val type: String,
    val nullable: Boolean,
    val primaryKey: Boolean
)

data class TableInfo(
    val name: String,
    val recordCount: Int
)

data class Locality(
    val abreviacionCiudad: String,
    val nombreCompleto: String
)

data class SyncResult(
    val tablesCreated: Int,
    val tablesUpdated: Int,
    val success: Boolean
)

enum class SyncStatus {
    NOT_SYNCED,
    SYNCING,
    SYNCED,
    FAILED
}
```

### Data Layer Models

```kotlin
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: Int = 1,
    val usuario: String,
    val identificacion: String,
    val nombre: String
)

@Serializable
data class AuthRequest(
    val Mac: String,
    val NomAplicacion: String,
    val Password: String,
    val Path: String,
    val Usuario: String
)

@Serializable
data class VersionResponse(
    val version: String
)

@Serializable
data class SchemaResponse(
    val tables: List<TableSchemaDto>
)

@Serializable
data class TableSchemaDto(
    val tableName: String,
    val columns: List<ColumnDefinitionDto>
)

@Serializable
data class ColumnDefinitionDto(
    val name: String,
    val type: String,
    val nullable: Boolean,
    val primaryKey: Boolean
)

@Serializable
data class LocalitiesResponse(
    val localities: List<LocalityDto>
)

@Serializable
data class LocalityDto(
    val abreviacionCiudad: String,
    val nombreCompleto: String
)
```

### Mappers

```kotlin
fun UserEntity.toDomain(): UserSession
fun UserSession.toEntity(): UserEntity
fun AuthResponse.toUserSession(): UserSession
fun TableSchemaDto.toDomain(): TableSchema
fun LocalityDto.toDomain(): Locality
```


## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system—essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: Version Comparison Correctness

*For any* two version strings (local and API versions), the version comparison logic should correctly determine whether the local version is less than, equal to, or greater than the API version, and return the appropriate VersionComparisonStatus.

**Validates: Requirements 1.3, 1.4, 1.5, 1.6**

### Property 2: Splash Screen Navigation Logic

*For any* combination of version status and session existence, the splash screen should navigate to the correct destination: stay on splash for version mismatch, navigate to home if session exists and version is current, navigate to login if no session exists and version is current.

**Validates: Requirements 1.7, 1.8**

### Property 2: Splash Screen Navigation Logic

*For any* combination of version status and session existence, the splash screen should navigate to the correct destination: stay on splash for version mismatch, navigate to home if session exists and version is current, navigate to login if no session exists and version is current.

**Validates: Requirements 1.7, 1.8**

### Property 3: Authentication and Session Round-Trip

*For any* valid authentication credentials, if authentication succeeds and user data is stored, then retrieving the user session should return the same Usuario, Identificacion, and Nombre values that were stored.

**Validates: Requirements 2.3, 2.5, 4.1, 4.3, 10.1**

### Property 3: Authentication and Session Round-Trip

*For any* valid authentication credentials, if authentication succeeds and user data is stored, then retrieving the user session should return the same Usuario, Identificacion, and Nombre values that were stored.

**Validates: Requirements 2.3, 2.5, 4.1, 4.3, 11.1**

### Property 4: Session Persistence Across Restarts

*For any* authenticated user session, if the session is stored in the database, then after simulating an app restart (clearing in-memory state), retrieving the session should return the same user data.

**Validates: Requirements 4.5, 10.2**

### Property 4: Session Persistence Across Restarts

*For any* authenticated user session, if the session is stored in the database, then after simulating an app restart (clearing in-memory state), retrieving the session should return the same user data.

**Validates: Requirements 4.5, 11.2**

### Property 5: User Data Validation Rejects Empty Fields

*For any* user data where at least one required field (Usuario, Identificacion, or Nombre) is empty or whitespace-only, attempting to store that user data should fail with a validation error.

**Validates: Requirements 4.2, 9.4**

### Property 5: User Data Validation Rejects Empty Fields

*For any* user data where at least one required field (Usuario, Identificacion, or Nombre) is empty or whitespace-only, attempting to store that user data should fail with a validation error.

**Validates: Requirements 4.2, 10.4**

### Property 6: API Response Field Validation

*For any* API response, if required fields are missing or have incorrect types, then response validation should fail and return an appropriate error.

**Validates: Requirements 9.1, 9.2**

### Property 6: API Response Field Validation

*For any* API response, if required fields are missing or have incorrect types, then response validation should fail and return an appropriate error.

**Validates: Requirements 10.1, 10.2**

### Property 7: Database Schema Synchronization Idempotence

*For any* table schema, synchronizing the same schema multiple times should result in the same database state, with no duplicate tables or data corruption.

**Validates: Requirements 3.2, 3.5**

### Property 7: Database Schema Synchronization Idempotence

*For any* table schema, synchronizing the same schema multiple times should result in the same database state, with no duplicate tables or data corruption.

**Validates: Requirements 3.2, 3.5**

### Property 8: Table Data Preservation During Schema Updates

*For any* existing table with data, if the schema is updated with compatible changes (adding nullable columns), then all existing data should be preserved and remain queryable.

**Validates: Requirements 3.3**

### Property 8: Table Data Preservation During Schema Updates

*For any* existing table with data, if the schema is updated with compatible changes (adding nullable columns), then all existing data should be preserved and remain queryable.

**Validates: Requirements 3.3**

### Property 9: Network Error Handling Consistency

*For any* network operation that fails (timeout, no connectivity, or server error), the system should return a Result.Error with an appropriate error message and never throw an unhandled exception.

**Validates: Requirements 1.6, 2.4, 2.6, 7.4, 8.1, 8.2, 8.3**

### Property 9: Network Error Handling Consistency

*For any* network operation that fails (timeout, no connectivity, or server error), the system should return a Result.Error with an appropriate error message and never throw an unhandled exception.

**Validates: Requirements 1.9, 2.4, 2.6, 8.4, 9.1, 9.2, 9.3**

### Property 10: Logout Clears Session Completely

*For any* authenticated user session, after logout is executed, attempting to retrieve the user session should return null or an error indicating no session exists.

**Validates: Requirements 10.5**

### Property 10: Logout Clears Session Completely

*For any* authenticated user session, after logout is executed, attempting to retrieve the user session should return null or an error indicating no session exists.

**Validates: Requirements 11.5**

### Property 11: Table Display Handles Variable Schemas

*For any* table with arbitrary column names and types, the table data retrieval and display logic should successfully return the data as a list of maps without errors.

**Validates: Requirements 6.5**

### Property 11: Table Display Handles Variable Schemas

*For any* table with arbitrary column names and types, the table data retrieval and display logic should successfully return the data as a list of maps without errors.

**Validates: Requirements 7.5**

### Property 12: Localities Response Parsing

*For any* valid localities API response, all locality records should be successfully parsed with both AbreviacionCiudad and NombreCompleto fields present in the resulting domain models.

**Validates: Requirements 7.2**

### Property 12: Localities Response Parsing

*For any* valid localities API response, all locality records should be successfully parsed with both AbreviacionCiudad and NombreCompleto fields present in the resulting domain models.

**Validates: Requirements 8.2**

### Property 13: Exponential Backoff Retry Logic

*For any* retryable API failure, the retry delays should follow an exponential backoff pattern where each subsequent retry delay is greater than the previous one.

**Validates: Requirements 8.5**

### Property 13: Exponential Backoff Retry Logic

*For any* retryable API failure, the retry delays should follow an exponential backoff pattern where each subsequent retry delay is greater than the previous one.

**Validates: Requirements 9.5**

### Property 14: Database Transaction Rollback on Failure

*For any* table creation operation that fails, the database should rollback to its previous state with no partial changes or orphaned tables.

**Validates: Requirements 3.6**

### Property 15: Navigation Type Safety with Serializable Routes

*For any* navigation action using serializable routes, the navigation arguments should be correctly serialized and deserialized without data loss or type errors.

**Validates: Requirements 5.2, 5.3**

## Error Handling

### Error Types

The application defines a sealed class hierarchy for errors:

```kotlin
sealed class AppError {
    data class NetworkError(val message: String, val cause: Throwable? = null) : AppError()
    data class ApiError(val statusCode: Int, val message: String) : AppError()
    data class DatabaseError(val message: String, val cause: Throwable? = null) : AppError()
    data class ValidationError(val field: String, val message: String) : AppError()
    data class UnknownError(val message: String, val cause: Throwable? = null) : AppError()
}
```

### Error Handling Strategy

#### Network Layer

All API calls are wrapped in the `NetworkHandler.safeApiCall()` method:

```kotlin
suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): Result<T> {
    return try {
        if (!isNetworkAvailable()) {
            return Result.Error(NetworkError("No internet connection"))
        }
        
        val response = apiCall()
        
        when {
            response.isSuccessful && response.body() != null -> {
                Result.Success(response.body()!!)
            }
            response.isSuccessful && response.body() == null -> {
                Result.Error(ApiError(response.code(), "Empty response body"))
            }
            else -> {
                Result.Error(ApiError(response.code(), response.message()))
            }
        }
    } catch (e: SocketTimeoutException) {
        Result.Error(NetworkError("Connection timed out", e))
    } catch (e: IOException) {
        Result.Error(NetworkError("Network error: ${e.message}", e))
    } catch (e: Exception) {
        Result.Error(UnknownError("Unexpected error: ${e.message}", e))
    }
}
```

#### Repository Layer

Repositories handle data layer errors and convert them to domain errors:

```kotlin
override suspend fun saveUserSession(user: UserSession): Result<Unit> {
    return try {
        // Validate user data
        if (user.usuario.isBlank() || user.identificacion.isBlank() || user.nombre.isBlank()) {
            return Result.Error(ValidationError("user", "All user fields must be non-empty"))
        }
        
        userDao.insertUser(user.toEntity())
        Result.Success(Unit)
    } catch (e: SQLiteException) {
        Result.Error(DatabaseError("Failed to save user session", e))
    } catch (e: Exception) {
        Result.Error(UnknownError("Unexpected error saving user", e))
    }
}
```

#### Use Case Layer

Use cases handle business logic errors and propagate repository errors:

```kotlin
override suspend fun invoke(usuario: String, password: String, mac: String): Result<UserSession> {
    // Authenticate
    val authResult = authRepository.authenticateUser(usuario, password, mac)
    
    return when (authResult) {
        is Result.Success -> {
            // Save session
            val userSession = authResult.data.toUserSession()
            val saveResult = authRepository.saveUserSession(userSession)
            
            when (saveResult) {
                is Result.Success -> Result.Success(userSession)
                is Result.Error -> saveResult
            }
        }
        is Result.Error -> authResult
    }
}
```

#### Presentation Layer

ViewModels convert errors to user-friendly messages:

```kotlin
fun login(usuario: String, password: String) {
    viewModelScope.launch {
        _state.value = LoginState.Loading
        
        val result = loginUserUseCase(usuario, password, getMacAddress())
        
        _state.value = when (result) {
            is Result.Success -> LoginState.Success
            is Result.Error -> {
                val message = when (val error = result.error) {
                    is NetworkError -> "Network error: ${error.message}"
                    is ApiError -> "Authentication failed: ${error.message}"
                    is ValidationError -> "Invalid input: ${error.message}"
                    is DatabaseError -> "Database error: ${error.message}"
                    is UnknownError -> "Unexpected error: ${error.message}"
                }
                LoginState.Error(message)
            }
        }
    }
}
```

### Retry Logic

For retryable operations (network requests), implement exponential backoff:

```kotlin
suspend fun <T> retryWithExponentialBackoff(
    maxRetries: Int = 3,
    initialDelay: Long = 1000L,
    maxDelay: Long = 10000L,
    factor: Double = 2.0,
    block: suspend () -> Result<T>
): Result<T> {
    var currentDelay = initialDelay
    repeat(maxRetries) { attempt ->
        val result = block()
        
        if (result is Result.Success || !isRetryable(result)) {
            return result
        }
        
        if (attempt < maxRetries - 1) {
            delay(currentDelay)
            currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
        }
    }
    
    return block()
}

private fun <T> isRetryable(result: Result<T>): Boolean {
    return when (result) {
        is Result.Error -> when (result.error) {
            is NetworkError -> true
            is ApiError -> result.error.statusCode in 500..599
            else -> false
        }
        else -> false
    }
}
```

### Logging

All errors are logged with appropriate context:

```kotlin
private fun logError(tag: String, message: String, error: AppError) {
    when (error) {
        is NetworkError -> Log.e(tag, "$message: ${error.message}", error.cause)
        is ApiError -> Log.e(tag, "$message: [${error.statusCode}] ${error.message}")
        is DatabaseError -> Log.e(tag, "$message: ${error.message}", error.cause)
        is ValidationError -> Log.w(tag, "$message: ${error.field} - ${error.message}")
        is UnknownError -> Log.e(tag, "$message: ${error.message}", error.cause)
    }
}
```

## Testing Strategy

### Overview

The testing strategy follows a dual approach combining unit tests and property-based tests to ensure comprehensive coverage:

- **Unit Tests**: Verify specific examples, edge cases, and integration points
- **Property-Based Tests**: Verify universal properties across randomized inputs

### Property-Based Testing

Property-based tests validate correctness properties using randomized inputs. Each property test runs a minimum of 100 iterations to ensure comprehensive coverage.

**Library**: Kotest Property Testing (for Kotlin)

**Configuration**:
```kotlin
class PropertyTestConfig {
    companion object {
        const val ITERATIONS = 100
        const val EDGE_CASES_ITERATIONS = 20
    }
}
```

**Test Structure**:
```kotlin
class CheckVersionUseCasePropertyTest {
    @Test
    fun `property - version comparison correctness`() = runTest {
        checkAll(
            iterations = PropertyTestConfig.ITERATIONS,
            Arb.versionString(),
            Arb.versionString()
        ) { localVersion, apiVersion ->
            // Feature: controller-app, Property 1: Version Comparison Correctness
            val useCase = CheckVersionUseCase(mockRepository, localVersion)
            val result = useCase()
            
            // Verify comparison logic
            when {
                localVersion < apiVersion -> {
                    result.status shouldBe VersionComparisonStatus.UPDATE_NEEDED
                }
                localVersion > apiVersion -> {
                    result.status shouldBe VersionComparisonStatus.AHEAD_OF_SERVER
                }
                else -> {
                    result.status shouldBe VersionComparisonStatus.UP_TO_DATE
                }
            }
        }
    }
}
```

### Unit Testing

Unit tests verify specific scenarios and edge cases using the provider methods pattern.

**Test Structure**:
```kotlin
class LoginUserUseCaseTest {
    companion object {
        @JvmStatic
        fun provideLoginScenarios() = listOf(
            LoginScenario(
                name = "successful login with valid credentials",
                usuario = "pam.meredy21",
                password = "SW50ZXJyYXBpZGlzaW1vLg==",
                apiResponse = Result.Success(mockAuthResponse),
                expectedResult = Result.Success(mockUserSession)
            ),
            LoginScenario(
                name = "failed login with 401 status",
                usuario = "invalid",
                password = "invalid",
                apiResponse = Result.Error(ApiError(401, "Unauthorized")),
                expectedResult = Result.Error(ApiError(401, "Unauthorized"))
            ),
            LoginScenario(
                name = "network error during login",
                usuario = "pam.meredy21",
                password = "SW50ZXJyYXBpZGlzaW1vLg==",
                apiResponse = Result.Error(NetworkError("No connection")),
                expectedResult = Result.Error(NetworkError("No connection"))
            )
        )
    }
    
    @ParameterizedTest
    @MethodSource("provideLoginScenarios")
    fun `test login scenarios`(scenario: LoginScenario) = runTest {
        // Arrange
        val mockRepository = mockk<AuthRepository>()
        coEvery { 
            mockRepository.authenticateUser(any(), any(), any()) 
        } returns scenario.apiResponse
        
        if (scenario.apiResponse is Result.Success) {
            coEvery { 
                mockRepository.saveUserSession(any()) 
            } returns Result.Success(Unit)
        }
        
        val useCase = LoginUserUseCase(mockRepository)
        
        // Act
        val result = useCase(scenario.usuario, scenario.password, "")
        
        // Assert
        result shouldBe scenario.expectedResult
    }
}
```

### Test Coverage Requirements

#### Use Cases (Property-Based Tests)
- CheckVersionUseCase: Property 1
- LoginUserUseCase: Property 2, 4
- SyncDatabaseUseCase: Property 6, 7, 13
- GetUserSessionUseCase: Property 3, 9
- GetLocalitiesUseCase: Property 11

#### Repositories (Unit Tests)
- VersionRepositoryImpl: Network error handling
- AuthRepositoryImpl: Authentication flow, session management
- DataSyncRepositoryImpl: Schema sync, table operations
- LocalitiesRepositoryImpl: API integration

#### ViewModels (Unit Tests)
- SplashViewModel: Version check flow, session restoration
- LoginViewModel: Login flow, error states
- HomeViewModel: User data display, sync trigger
- TablesViewModel: Table listing, data display
- LocalitiesViewModel: Localities loading, error handling

#### Network Handler (Property-Based Tests)
- Property 8: Error handling consistency
- Property 12: Exponential backoff

#### Database Operations (Property-Based Tests)
- Property 3: Session persistence
- Property 7: Data preservation
- Property 10: Variable schema handling
- Property 13: Transaction rollback

### Test Tagging

Each property-based test must include a comment tag referencing the design property:

```kotlin
// Feature: controller-app, Property 1: Version Comparison Correctness
```

### Mocking Strategy

- Use MockK for Kotlin mocking
- Mock external dependencies (API services, DAOs)
- Use test doubles for repositories in use case tests
- Use fake implementations for integration tests

### Test Data Generators

For property-based tests, define custom generators:

```kotlin
fun Arb.Companion.versionString(): Arb<String> = arbitrary {
    val major = it.random.nextInt(1, 10)
    val minor = it.random.nextInt(0, 20)
    val patch = it.random.nextInt(0, 100)
    "$major.$minor.$patch"
}

fun Arb.Companion.userSession(): Arb<UserSession> = arbitrary {
    UserSession(
        usuario = Arb.string(5..20).bind(),
        identificacion = Arb.string(5..15).bind(),
        nombre = Arb.string(5..50).bind()
    )
}

fun Arb.Companion.tableSchema(): Arb<TableSchema> = arbitrary {
    TableSchema(
        tableName = Arb.string(5..20).bind(),
        columns = Arb.list(Arb.columnDefinition(), 1..10).bind()
    )
}
```

### Continuous Integration

- All tests must pass before merging
- Property tests run with 100 iterations in CI
- Unit tests run on every commit
- Test coverage target: 80% for use cases and repositories
