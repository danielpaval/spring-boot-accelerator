resource "azurerm_resource_group" "main" {
  name     = "${var.project_name}-rg"
  location = var.location

  tags = {
    Project   = var.project_name
    ManagedBy = "Terraform"
  }
}
