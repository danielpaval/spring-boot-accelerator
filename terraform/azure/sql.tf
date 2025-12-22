resource "azurerm_mssql_server" "main" {
  name                         = "${var.project_name}-sql"
  resource_group_name          = azurerm_resource_group.main.name
  location                     = azurerm_resource_group.main.location
  version                      = "12.0"
  administrator_login          = var.sql_admin_username
  administrator_login_password = var.sql_admin_password

  azuread_administrator {
    login_username = var.azuread_admin_login
    object_id      = var.azuread_admin_object_id
    tenant_id      = var.tenant_id
  }

  tags = {
    Project      = var.project_name
    ManagedBy    = "Terraform"
    Environment  = "Development"
    ResourceType = "SQLServer"
    CostCenter   = "Engineering"
    Service      = "Database"
  }
}

resource "azurerm_mssql_database" "main" {
  name                        = "${var.project_name}-db"
  server_id                   = azurerm_mssql_server.main.id
  sku_name                    = "GP_S_Gen5_2"
  auto_pause_delay_in_minutes = 30
  min_capacity                = 0.5
  max_size_gb                 = 32

  tags = {
    Project      = var.project_name
    ManagedBy    = "Terraform"
    Environment  = "Development"
    ResourceType = "SQLDatabase"
    CostCenter   = "Engineering"
    Service      = "Database"
    SKU          = "GP_S_Gen5_2"
  }
}

resource "azurerm_mssql_firewall_rule" "allow_azure_services" {
  name             = "AllowAzureServices"
  server_id        = azurerm_mssql_server.main.id
  start_ip_address = "0.0.0.0"
  end_ip_address   = "0.0.0.0"
}

resource "azurerm_mssql_firewall_rule" "allow_local_dev" {
  name             = "AllowLocalDevelopment"
  server_id        = azurerm_mssql_server.main.id
  start_ip_address = var.local_ip_address
  end_ip_address   = var.local_ip_address
}
