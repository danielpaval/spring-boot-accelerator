terraform {
  required_version = ">= 1.0"

  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "~> 4.57"
    }
  }

  # Uncomment below to use Azure Storage backend
  # backend "azurerm" {
  #   resource_group_name  = "tfstate"
  #   storage_account_name = "tfstate"
  #   container_name       = "tfstate"
  #   key                  = "spring-boot-api-demo.tfstate"
  # }
}

provider "azurerm" {
  features {}

  subscription_id = var.subscription_id
  tenant_id       = var.tenant_id
  client_id       = var.client_id
  client_secret   = var.client_secret
}
# Get current Azure context for dashboard URL
data "azurerm_client_config" "current" {}