resource "azurerm_key_vault" "main" {
  name                            = replace("${var.project_name}kv", "-", "")
  location                        = azurerm_resource_group.main.location
  resource_group_name             = azurerm_resource_group.main.name
  enabled_for_disk_encryption     = true
  enabled_for_template_deployment = true
  tenant_id                       = var.tenant_id
  sku_name                        = "standard"
  purge_protection_enabled        = true

  access_policy {
    tenant_id = var.tenant_id
    object_id = var.azuread_admin_object_id

    secret_permissions = [
      "Get",
      "List",
      "Set",
      "Delete",
      "Purge",
      "Recover"
    ]
  }

  tags = {
    Project   = var.project_name
    ManagedBy = "Terraform"
  }
}

resource "azurerm_key_vault_secret" "database_username" {
  name         = "datasource-username"
  value        = var.sql_admin_username
  key_vault_id = azurerm_key_vault.main.id
}

resource "azurerm_key_vault_secret" "database_password" {
  name         = "datasource-password"
  value        = var.sql_admin_password
  key_vault_id = azurerm_key_vault.main.id
}
