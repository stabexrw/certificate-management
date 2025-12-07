# Deployment notes for Certificate Service

This document lists the minimum steps and environment variables required to deploy the backend securely.

Environment variables (recommended):

- `APP_SIGNATURE_SECRET` - HMAC secret used to sign certificates (required in production). Set this to a long, random value and keep it in a secrets manager.
- `APP_SIGNATURE_KEY_ID` - Optional identifier for the active signing key (defaults to `v1`).
- `SPRING_DATASOURCE_URL` / `SPRING_DATASOURCE_USERNAME` / `SPRING_DATASOURCE_PASSWORD` - Database connection.
- `SPRING_PROFILES_ACTIVE` - use `prod` for production properties if required.

How to run locally:

1. Set the signing secret in your shell:

```powershell
$env:APP_SIGNATURE_SECRET = 'replace-with-secure-random-value'
mvn -DskipTests spring-boot:run
```

2. The frontend will need the correct API URL; set `environment.prod.ts` or provide runtime configuration.

Security recommendations:

- Store `APP_SIGNATURE_SECRET` in a secret store (Azure Key Vault, AWS KMS/Secrets Manager, HashiCorp Vault) and inject it into the application at runtime.
- Rotate keys by creating a new secret, updating `APP_SIGNATURE_KEY_ID` and the application secret, and re-signing certificates as needed.
- Consider switching to asymmetric signing (RSA/ECDSA) and using the private key in a KMS for signing and publishing the public key for verification.
