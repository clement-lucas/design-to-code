# Spring Boot Code Templates Reference

## Application Entry Point

```java
@SpringBootApplication
public class {AppName}Application {
    public static void main(String[] args) {
        SpringApplication.run({AppName}Application.class, args);
    }
}
```

## Security Configuration

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/css/**", "/js/**", "/webjars/**").permitAll()
                .requestMatchers("/login", "/error").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/top", true)
                .failureUrl("/login?error")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

## UserDetails Implementation

```java
public class LoginUserDetails implements UserDetails, Serializable {
    private static final long serialVersionUID = 1L;

    private final SystemAccount account;  // Must also be Serializable

    public LoginUserDetails(SystemAccount account) {
        this.account = account;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + account.getRoleId()));
    }

    @Override
    public String getPassword() { return account.getPassword(); }

    @Override
    public String getUsername() { return account.getUserId(); }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() {
        return !"1".equals(account.getLockedFlag());
    }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() {
        return !"1".equals(account.getDeleteFlag());
    }

    public SystemAccount getAccount() { return account; }
}
```

## Controller Pattern (CRUD Feature)

```java
@Controller
@RequestMapping("/{feature}")
public class {Feature}Controller {

    private final {Feature}Service service;

    public {Feature}Controller({Feature}Service service) {
        this.service = service;
    }

    // Search screen
    @GetMapping("/search")
    public String search(@ModelAttribute {Feature}SearchForm form, Model model) {
        model.addAttribute("results", service.search(form));
        return "{feature}/search";
    }

    // Create form
    @GetMapping("/create")
    public String createForm(@ModelAttribute {Feature}Form form) {
        return "{feature}/create";
    }

    // Create confirmation
    @PostMapping("/create/confirm")
    public String createConfirm(@Validated @ModelAttribute {Feature}Form form,
                                BindingResult result) {
        if (result.hasErrors()) return "{feature}/create";
        return "{feature}/createConfirm";
    }

    // Execute create
    @PostMapping("/create/execute")
    public String createExecute(@Validated @ModelAttribute {Feature}Form form,
                                BindingResult result,
                                @AuthenticationPrincipal LoginUserDetails user,
                                RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) return "{feature}/create";
        service.create(form, user);
        redirectAttributes.addFlashAttribute("message", "登録しました。");
        return "redirect:/{feature}/search";
    }
}
```

## Thymeleaf Layout Template

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:sec="http://www.thymeleaf.org/extras/spring-security">
<head th:fragment="head(title)">
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title th:text="${title}">App</title>
    <link rel="stylesheet" th:href="@{/webjars/bootstrap/css/bootstrap.min.css}">
    <link rel="stylesheet" th:href="@{/css/style.css}">
</head>
<body>
<nav th:fragment="header" class="navbar navbar-expand-lg navbar-dark bg-primary">
    <div class="container">
        <a class="navbar-brand" th:href="@{/top}">アプリ名</a>
        <div class="navbar-nav ms-auto">
            <span class="navbar-text text-white me-3"
                  sec:authorize="isAuthenticated()"
                  th:text="${#authentication.principal.account.userName}">User</span>
            <form th:action="@{/logout}" method="post" class="d-inline">
                <button type="submit" class="btn btn-outline-light btn-sm">ログアウト</button>
            </form>
        </div>
    </div>
</nav>

<!-- Page content fragment usage -->
<div class="container mt-4">
    <!-- Alert messages -->
    <div th:if="${message}" class="alert alert-success" th:text="${message}"></div>
    <div th:if="${errorMessage}" class="alert alert-danger" th:text="${errorMessage}"></div>
</div>

<script th:src="@{/webjars/jquery/jquery.min.js}"></script>
<script th:src="@{/webjars/bootstrap/js/bootstrap.bundle.min.js}"></script>
</body>
</html>
```

## Spring Batch Job Configuration (Spring Batch 5.x)

```java
@Configuration
public class {Job}BatchConfig {

    @Bean
    public Job {job}Job(JobRepository jobRepository, Step {step}Step) {
        return new JobBuilder("{jobName}", jobRepository)
                .start({step}Step)
                .build();
    }

    @Bean
    public Step {step}Step(JobRepository jobRepository,
                           PlatformTransactionManager transactionManager,
                           ItemReader<{Input}> reader,
                           ItemProcessor<{Input}, {Output}> processor,
                           ItemWriter<{Output}> writer) {
        return new StepBuilder("{stepName}", jobRepository)
                .<{Input}, {Output}>chunk(100, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }
}
```

## application.yml (H2 local development)

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL
    driver-class-name: org.h2.Driver
    username: sa
    password:
  h2:
    console:
      enabled: true
      path: /h2-console
  jpa:
    hibernate:
      ddl-auto: none
    show-sql: true
  sql:
    init:
      mode: always
      encoding: UTF-8
  session:
    store-type: jdbc
    jdbc:
      initialize-schema: always
  batch:
    jdbc:
      initialize-schema: always
    job:
      enabled: false

server:
  port: 8080
  servlet:
    encoding:
      charset: UTF-8
      force: true
```
