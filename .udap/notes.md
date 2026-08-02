# employee-api — Build Notes

## Status
- [x] Project meta approved (us-east-2, ec2, spring-boot, Java 17)
- [x] Blueprint: springboot-ec2@1.0.0 selected and tailored
- [x] Architecture + pipeline designed and approved
- [x] Plan approved
- [x] All source files generated
- [ ] validate_project
- [ ] create_repo_and_push
- [ ] set_pipeline_secret (JWT_SECRET)
- [ ] deploy
- [ ] wait_for_run

## Key decisions
- Spring Boot 3.3.5 (scaffold shipped 4.1.0 — downgraded to 3.x for Java 17 compatibility and jjwt 0.12.6 support)
- Java 17 (matches EC2 Ubuntu 22.04 `openjdk-17-jre-headless` package)
- H2 in-memory DB — zero infra, demo-focused; Tier 1
- JWT via jjwt 0.12.6 (latest stable; uses Keys.hmacShaKeyFor, not deprecated SignatureAlgorithm)
- InMemoryUserDetailsManager — single admin user (admin/admin123); no persistent user store for Tier 1
- Test profile (application-test.properties) uses fixed JWT secret for deterministic tests
- Surefire HTML report: generated in CI via `mvn verify -Ptest-report`, uploaded as artifact; also copied to /opt/employee-api/test-report/ on the server for the post-deploy-report auxiliary workflow
- Integration tests (EmployeeRepositoryIT) use @DataJpaTest — matched by Failsafe (*IT.java pattern)
- Ansible copies JAR from `target/` downloaded by actions/download-artifact in the configure stage

## Known pitfalls applied
- Empty S3 backend block (backend "s3" {}) — backend-config flags in pipeline
- SSH key written via printf+env var (not echo) — newline-safe
- verify stage reads terraform output directly (self-sufficient job rule)
- configure stage also reads terraform output directly
- Health check uses --retry 12 --retry-delay 15 --retry-all-errors
- SSH_USER comes from secret, not hardcoded 'ubuntu'
- All TF_VAR_* in provision stage env
- Failsafe bound to integration-test + verify goals

## Environment
- AWS us-east-2, default VPC (vpc-09f1530b0a8847351), 3 subnets
- EIP quota: 5 (1 will be used)
- GitHub Actions VCS
