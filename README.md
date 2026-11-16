# Spring PetClinic Multi-Service on Azure (`azure.springboot`)

A multi-module Maven `azd` template deploying two Spring Boot services to Azure
Container Apps using the `azure.springboot` extension (`host: springbootapp`):

1. **`petclinic-rest`** (`./petclinic-rest`): Spring PetClinic REST API backed by
   Azure Database for PostgreSQL 16 with system-assigned managed identity and
   Entra-only authentication (no database passwords or Key Vault).
2. **`petclinic-web`** (`./petclinic-web`): Database-free Spring Boot Thymeleaf
   UI portal that connects to `petclinic-rest` via `PETCLINIC_BACKEND_URL`
   (`SERVICE_PETCLINIC_REST_ENDPOINT_URL`).

**Demo only:** both endpoints are public and unauthenticated. Do not store
sensitive data.

## Architecture

![Spring PetClinic Multi-Service architecture](docs/images/architecture.svg)
## Two-Commit Authoring Comparison

Run `git show HEAD --stat` to compare this `azure.springboot` template directly
against the baseline `host: containerapp` commit (`HEAD~1`):

- **Removed**: `petclinic-rest/Dockerfile`, `petclinic-web/Dockerfile`,
  `.dockerignore`, and all PowerShell/SQL bootstrap scripts under `scripts/`.
- **Simplified `azure.yaml`**: replaces `docker:` blocks and
  `preup`/`preprovision`/`postprovision` script hooks with `language: java` and
  `host: springbootapp` on both Maven modules while preserving
  `uses: [petclinic-rest]` for endpoint ordering.
- **Extension-owned lifecycle**: `azure.springboot` packages both reactor
  modules with the root Maven Wrapper, generates runtime Java 17 container
  images, prompts once for missing PostgreSQL Entra administrator inputs (using
  the signed-in `azd` User object ID as default), reconciles `petclinic_app`
  locally after provisioning, and skips database steps for `petclinic-web`.

## Deploy

Prerequisites: [Azure Developer CLI (`azd`)](https://aka.ms/azd-install) 1.37.2+,
JDK 17+, and Docker.

1. Register the pre-merge nightly extension registry source and install
   `azure.springboot` (one-time per `azd` profile; once registered, `azd init -t`
   can also install it automatically, or you can
   [build from source](https://github.com/XiaofeiCao/azure-dev/blob/xiaofeicao-spring-boot-template-roadmap/cli/azd/extensions/azure.springboot/README.md#local-development-installation)):

   ```sh
   azd extension source add -n spring-nightly -t url \
     -l "https://raw.githubusercontent.com/XiaofeiCao/azure-dev/nightly/cli/azd/extensions/registry.nightly.json"
   azd extension install azure.springboot --source spring-nightly
   azd extension show azure.springboot
   ```

2. Initialize and deploy the template:

   ```sh
   azd auth login
   azd init -t https://github.com/XiaofeiCao/spring-petclinic-multiservice-azd
   azd up
   ```

For subscriptions requiring VNet integration on Azure Container Apps, enable the
option before the first provision:

```sh
azd env set AZURE_USE_VNET true
azd up
```
