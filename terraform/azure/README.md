# Terraform Azure Infrastructure

This directory contains Terraform configurations for deploying the Spring Boot API Demo project to Azure, including Container Apps, SQL Database, Key Vault, and Entra ID.

## Prerequisites

1. **Terraform**: Install version 1.0 or later from [terraform.io](https://www.terraform.io/downloads)
2. **Azure CLI**: Install from [Azure CLI docs](https://learn.microsoft.com/en-us/cli/azure/install-azure-cli)
3. **Azure Account**: An active Azure subscription with appropriate permissions
4. **Service Principal credentials** (for CI/CD) or Azure CLI authentication

## Setup

### 1. Authenticate with Azure

```powershell
az login
```

### 2. Create a Service Principal (Optional but recommended for CI/CD)

For automated deployments, create a service principal:

```powershell
az ad sp create-for-rbac --name spring-boot-api-demo-terraform --role Contributor --scopes /subscriptions/{subscription-id}
```

This will output credentials needed for `terraform.tfvars`.

### 3. Create terraform.tfvars

Create a `terraform.tfvars` file (never commit this to version control):

```hcl
subscription_id         = "your-subscription-id"
tenant_id               = "your-tenant-id"
client_id               = "your-client-id"
client_secret           = "your-client-secret"
sql_admin_password      = "your-sql-password"
azuread_admin_login     = "your-email@example.com"
azuread_admin_object_id = "your-object-id"
location                = "eastus2"
project_name            = "spring-boot-accelerator"
container_image         = "ghcr.io/your-org/your-image:latest"
```

Or set environment variables:
```powershell
$env:TF_VAR_subscription_id="..."
$env:TF_VAR_tenant_id="..."
$env:TF_VAR_client_id="..."
$env:TF_VAR_client_secret="..."
```

## Quick Start

### Initialize Terraform

```powershell
terraform init
```

### Validate Configuration

```powershell
terraform validate
```

### Plan Changes

```powershell
terraform plan
```

### Apply Configuration

```powershell
terraform apply
```

Or with auto-approve:
```powershell
terraform apply -auto-approve
```

### Destroy Resources

```powershell
terraform destroy
```

## File Structure

- **provider.tf**: Azure provider configuration and version requirements
- **variables.tf**: Input variables for the configuration
- **main.tf**: Resource group definition
- **container-app.tf**: Container App, Container App Environment, and Log Analytics Workspace
- **sql.tf**: Azure SQL Server and Database resources
- **key-vault.tf**: Azure Key Vault and secrets management
- **entra.tf**: Entra ID (Azure AD) application and service principal
- **outputs.tf**: Output values after deployment
- **terraform.tfvars**: Variable values (DO NOT COMMIT - gitignored)

## Resources Created

The Terraform configuration creates the following Azure resources:

1. **Resource Group** - Container for all resources
2. **SQL Server** - Azure SQL Server instance with firewall rules
3. **SQL Database** - Application database (Basic tier)
4. **Container App Environment** - Hosting environment for the container
5. **Container App** - The Spring Boot application with system-assigned managed identity
6. **Key Vault** - Secure storage for secrets with access policies
7. **Log Analytics Workspace** - Monitoring and logging (30-day retention)
8. **Entra ID Application** - OAuth2/OIDC authentication
9. **Entra ID Service Principal** - Application identity for authentication

## Terraform Outputs

After successful deployment, Terraform will output the following values:

### Resource Group
| Output | Description | Sensitive |
|--------|-------------|-----------|
| `resource_group_id` | ID of the created resource group | No |
| `resource_group_name` | Name of the created resource group | No |
| `location` | Azure region where resources are deployed | No |

### SQL Database
| Output | Description | Sensitive |
|--------|-------------|-----------|
| `sql_server_fqdn` | Fully qualified domain name of the SQL Server | No |
| `sql_database_name` | Name of the SQL Database | No |
| `sql_connection_string` | MSSQL connection string for the database | **Yes** |

### Container App
| Output | Description | Sensitive |
|--------|-------------|-----------|
| `container_app_url` | URL of the deployed Container App | No |
| `container_app_identity` | Principal ID of the Container App managed identity | No |

### Key Vault
| Output | Description | Sensitive |
|--------|-------------|-----------|
| `key_vault_id` | ID of the Key Vault | No |
| `key_vault_name` | Name of the Key Vault | No |

### OIDC / Authentication
| Output | Description | Sensitive |
|--------|-------------|-----------|
| `oidc_client_id` | Client ID for OIDC authentication | No |
| `oidc_issuer_uri` | Azure AD OIDC issuer URI | **Yes** |

## Viewing Outputs

### View all outputs:
```powershell
terraform output
```

### View a specific output:
```powershell
terraform output container_app_url
```

### View sensitive outputs:
```powershell
terraform output sql_connection_string
terraform output oidc_issuer_uri
```

### Export outputs as JSON:
```powershell
terraform output -json
```

### Export outputs to a file:
```powershell
terraform output -json > outputs.json
```

## Secrets Management

All secrets are stored in Azure Key Vault and referenced by the Container App using Key Vault references:

- `datasource-url` - Database connection URL
- `datasource-username` - Database username  
- `datasource-password` - Database password
- `jwt-issuer-uri` - JWT issuer URI for authentication
- `oidc-client-id` - OIDC client ID
- `oidc-client-secret` - OIDC client secret

The Container App uses a **System-Assigned Managed Identity** to access Key Vault secrets. An access policy is automatically created granting the Container App `Get` and `List` permissions on secrets.

## Force New Container App Revision

When using the `latest` image tag, Terraform won't detect changes to the actual container image. To force a new revision:

**Option 1: Using Terraform (Recommended)**

The configuration includes a `revision_suffix` with timestamp that forces new revisions on every apply:
```powershell
terraform apply -auto-approve
```

**Option 2: Using Azure CLI**
```powershell
az containerapp revision restart --name spring-boot-accelerator-app --resource-group spring-boot-accelerator-rg
```

**Option 3: Update the container image tag**

Instead of using `latest`, use specific version tags (e.g., `v1.0.0`, `build-123`) and update the `container_image` variable in `terraform.tfvars`.

## State Management

Terraform maintains a `terraform.tfstate` file that tracks deployed resources. 

⚠️ **Important**: 
- Never commit state files to version control
- State files are already included in `.gitignore`

For production environments:

1. Use [Azure Storage Backend](https://learn.microsoft.com/en-us/azure/developer/terraform/store-state-in-azure-storage) to store state remotely
2. Enable state locking for team deployments
3. Configure the backend in `provider.tf` (currently commented out)

Example backend configuration:
```hcl
backend "azurerm" {
  resource_group_name  = "tfstate"
  storage_account_name = "tfstate"
  container_name       = "tfstate"
  key                  = "spring-boot-api-demo.tfstate"
}
```

## Additional Commands

### Format configuration files:
```powershell
terraform fmt -recursive
```

### Show current state:
```powershell
terraform show
```

### List resources in state:
```powershell
terraform state list
```

### Check provider versions:
```powershell
terraform providers
```

### Upgrade providers:
```powershell
terraform init -upgrade
```

## Cleanup

To destroy all resources:
```powershell
terraform destroy
```

⚠️ **Warning:** This will permanently delete all resources including:
- The SQL database and all data
- Key Vault and all secrets
- Container App and all configurations
- Entra ID application registration

## Troubleshooting

### Authentication Issues

Check your current Azure account:
```powershell
az account show
```

Switch to a different subscription:
```powershell
az account set --subscription {subscription-id}
```

### Provider Version Issues

```powershell
terraform providers
terraform init -upgrade
```

### Container App Not Pulling Latest Image

If your container app isn't pulling the latest image:
1. Ensure the image was actually pushed to the registry
2. Use `terraform apply` to trigger a new revision with the timestamp suffix
3. Check Container App logs in Azure Portal for pull errors

### Key Vault Access Issues

Ensure the Container App's managed identity has the correct permissions:
```powershell
az keyvault show --name <vault-name> --query properties.accessPolicies
```

## Environment Variables

The Container App is configured with the following environment variables:

| Variable | Source | Description |
|----------|--------|-------------|
| `DATASOURCE_URL` | Key Vault Secret | JDBC connection URL |
| `DATASOURCE_USERNAME` | Key Vault Secret | Database username |
| `DATASOURCE_PASSWORD` | Key Vault Secret | Database password |
| `JWT_ISSUER_URI` | Key Vault Secret | JWT token issuer |
| `ROLES_CLAIM_PATH` | Direct Value | Path to roles in JWT token (set to "roles") |

## Notes

- Always use `;` (semicolon) on Windows PowerShell to separate commands
- Prefix Gradle wrapper commands with `.\` on Windows
- Custom application properties should start with an `application.` prefix
- Keep specific application-wide annotations (like `@EnableJpaAuditing`) in their own configuration classes
- The Container App uses auto-scaling with min/max replicas defined in variables

## Additional Resources

- [Azure Provider Documentation](https://registry.terraform.io/providers/hashicorp/azurerm/latest/docs)
- [Azure Container Apps Documentation](https://learn.microsoft.com/en-us/azure/container-apps/)
- [Terraform Best Practices](https://www.terraform.io/docs/cloud/guides/recommended-practices/index.html)

