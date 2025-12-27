resource "azurerm_log_analytics_workspace" "main" {
  name                = "${var.project_name}-law"
  resource_group_name = azurerm_resource_group.main.name
  location            = azurerm_resource_group.main.location
  sku                 = "PerGB2018"
  retention_in_days   = 30

  tags = {
    Project   = var.project_name
    ManagedBy = "Terraform"
  }
}

resource "azurerm_container_app_environment" "main" {
  name                       = "${var.project_name}-cae"
  resource_group_name        = azurerm_resource_group.main.name
  location                   = azurerm_resource_group.main.location
  log_analytics_workspace_id = azurerm_log_analytics_workspace.main.id

  tags = {
    Project   = var.project_name
    ManagedBy = "Terraform"
  }
}

resource "azurerm_key_vault_access_policy" "container_app" {
  key_vault_id       = azurerm_key_vault.main.id
  tenant_id          = var.tenant_id
  object_id          = azurerm_container_app.main.identity[0].principal_id
  secret_permissions = ["Get", "List"]
}

resource "azurerm_container_app" "main" {
  name                         = "${var.project_name}-app"
  container_app_environment_id = azurerm_container_app_environment.main.id
  resource_group_name          = azurerm_resource_group.main.name

  revision_mode = "Single"

  ingress {
    external_enabled = true
    target_port      = var.container_port
    transport        = "auto"

    traffic_weight {
      latest_revision = true
      percentage      = 100
    }
  }

  template {
    container {
      name   = var.project_name
      image  = var.container_image
      cpu    = var.container_cpu
      memory = var.container_memory

      env {
        name        = "DATASOURCE_URL"
        value       = "jdbc:sqlserver://${azurerm_mssql_server.main.fully_qualified_domain_name}:1433;database=${azurerm_mssql_database.main.name};encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;"
      }

      env {
        name        = "DATASOURCE_USERNAME"
        secret_name = "datasource-username"
      }

      env {
        name        = "DATASOURCE_PASSWORD"
        secret_name = "datasource-password"
      }

      env {
        name  = "JWT_ISSUER_URI"
        value = "https://sts.windows.net/${var.tenant_id}/"
      }

      env {
        name  = "ROLES_CLAIM_PATH"
        value = "roles"
      }
    }

    min_replicas = var.container_min_replicas
    max_replicas = var.container_max_replicas
  }

  secret {
    name                = "datasource-username"
    identity            = "System"
    key_vault_secret_id = azurerm_key_vault_secret.database_username.id
  }

  secret {
    name                = "datasource-password"
    identity            = "System"
    key_vault_secret_id = azurerm_key_vault_secret.database_password.id
  }

  # JWT issuer now injected via literal env var to avoid KV fetch issues

  identity {
    type = "SystemAssigned"
  }

  tags = {
    Project   = var.project_name
    ManagedBy = "Terraform"
  }
}
